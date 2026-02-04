package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.entity.BidEntity
import com.nodeorb.freight.marketplace.entity.BidStatus
import com.nodeorb.freight.marketplace.entity.MasterOrderEntity
import com.nodeorb.freight.marketplace.entity.PartialOrderEntity
import com.nodeorb.freight.marketplace.exception.ComplianceViolationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Сервис защиты аукционов от снайперов
 * Реализует anti-sniping логику и атомарную валидацию лотов
 */
@Service
class AuctionGuard(
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AuctionGuard::class.java)
        
        // Параметры anti-sniping
        private const val SNIPING_THRESHOLD_MINUTES = 5
        private const val AUCTION_EXTENSION_MINUTES = 5
        
        // Параметры атомарной валидации
        private const val MAX_COMMITTED_PERCENTAGE = 100.0
    }

    /**
     * Проверяет возможность размещения ставки с учетом anti-sniping правил
     * @param bid Ставка для проверки
     * @return true, если ставка может быть размещена
     */
    @Transactional(readOnly = true)
    fun validateBidPlacement(bid: BidEntity): Boolean {
        logger.info("Validating bid placement for bid: ${bid.id}")
        
        // Проверяем anti-sniping для всех типов заказов
        val shouldExtendAuction = checkAntiSniping(bid)
        
        if (shouldExtendAuction) {
            logger.info("Anti-sniping detected, extending auction time for bid: ${bid.id}")
            extendAuctionTime(bid)
        }
        
        // Проверяем атомарную валидацию для LTL заказов
        if (bid.partialOrder != null) {
            validateLtlAtomicity(bid.partialOrder)
        }
        
        return true
    }

    /**
     * Проверяет, является ли ставка снайперской (подана менее чем за 5 минут до окончания)
     */
    private fun checkAntiSniping(bid: BidEntity): Boolean {
        val auctionEndTime = getAuctionEndTime(bid)
        
        if (auctionEndTime == null) {
            logger.warn("Cannot determine auction end time for bid: ${bid.id}")
            return false
        }
        
        val timeUntilEnd = ChronoUnit.MINUTES.between(LocalDateTime.now(), auctionEndTime)
        
        logger.info("Time until auction end for bid ${bid.id}: $timeUntilEnd minutes")
        
        return timeUntilEnd > 0 && timeUntilEnd <= SNIPING_THRESHOLD_MINUTES
    }

    /**
     * Получает время окончания аукциона для ставки
     */
    private fun getAuctionEndTime(bid: BidEntity): LocalDateTime? {
        return when {
            bid.freightOrder != null -> bid.freightOrder.requiredDeliveryDate
            bid.masterOrder != null -> bid.masterOrder.requiredDeliveryDate
            bid.partialOrder != null -> bid.partialOrder.masterOrder.requiredDeliveryDate
            else -> null
        }
    }

    /**
     * Продлевает время аукциона на 5 минут
     */
    private fun extendAuctionTime(bid: BidEntity) {
        val endTime = getAuctionEndTime(bid) ?: return
        
        val newEndTime = endTime.plusMinutes(AUCTION_EXTENSION_MINUTES)
        
        when {
            bid.freightOrder != null -> {
                bid.freightOrder.requiredDeliveryDate = newEndTime
                logger.info("Extended auction time for freight order ${bid.freightOrder.id} to $newEndTime")
            }
            bid.masterOrder != null -> {
                bid.masterOrder.requiredDeliveryDate = newEndTime
                logger.info("Extended auction time for master order ${bid.masterOrder.id} to $newEndTime")
            }
            bid.partialOrder != null -> {
                bid.partialOrder.masterOrder.requiredDeliveryDate = newEndTime
                logger.info("Extended auction time for partial order ${bid.partialOrder.id} to $newEndTime")
            }
        }
    }

    /**
     * Проверяет атомарную валидацию для LTL заказов
     * Сумма всех ставок в статусе PENDING + COMMITTED не должна превышать 100% объема
     */
    private fun validateLtlAtomicity(partialOrder: PartialOrderEntity) {
        val masterOrder = partialOrder.masterOrder
        
        // Получаем все ставки по этому частичному заказу
        val pendingBids = getBidsByPartialOrderAndStatus(partialOrder.id!!, BidStatus.PENDING)
        val committedBids = getBidsByPartialOrderAndStatus(partialOrder.id!!, BidStatus.COMMITTED)
        
        val totalPendingPercentage = pendingBids.sumOf { bid ->
            when {
                bid.partialOrder != null -> bid.partialOrder.percentage
                else -> 0.0
            }
        }
        
        val totalCommittedPercentage = committedBids.sumOf { bid ->
            when {
                bid.partialOrder != null -> bid.partialOrder.percentage
                else -> 0.0
            }
        }
        
        val totalPercentage = totalPendingPercentage + totalCommittedPercentage
        
        logger.info("LTL atomicity check for partial order ${partialOrder.id}: " +
                   "pending: ${totalPendingPercentage}%, " +
                   "committed: ${totalCommittedPercentage}%, " +
                   "total: ${totalPercentage}%")
        
        if (totalPercentage > MAX_COMMITTED_PERCENTAGE) {
            throw ComplianceViolationException(
                "LTL atomicity violation: total committed percentage ($totalPercentage%) " +
                "exceeds maximum allowed (${MAX_COMMITTED_PERCENTAGE}%)"
            )
        }
    }

    /**
     * Получает ставки по частичному заказу и статусу
     */
    private fun getBidsByPartialOrderAndStatus(partialOrderId: java.util.UUID, status: BidStatus): List<BidEntity> {
        // В реальной системе здесь будет вызов репозитория
        // Пока возвращаем пустой список как заглушку
        return emptyList()
    }

    /**
     * Проверяет, может ли быть начат аукцион по заказу
     */
    @Transactional(readOnly = true)
    fun validateAuctionStart(orderId: java.util.UUID, orderType: String): Boolean {
        return when (orderType.lowercase()) {
            "master" -> validateMasterOrderAuction(orderId)
            "partial" -> validatePartialOrderAuction(orderId)
            else -> throw IllegalArgumentException("Unknown order type: $orderType")
        }
    }

    /**
     * Проверяет возможность начала аукциона по мастер-заказу
     */
    private fun validateMasterOrderAuction(masterOrderId: java.util.UUID): Boolean {
        val masterOrder = masterOrderRepository.findById(masterOrderId)
            ?: throw IllegalArgumentException("Master order not found: $masterOrderId")
        
        // Проверяем, что заказ еще не заполнен
        if (masterOrder.status == com.nodeorb.freight.marketplace.entity.MasterOrderStatus.FILLED) {
            throw ComplianceViolationException("Master order is already filled")
        }
        
        // Проверяем, что есть оставшийся объем для LTL
        if (masterOrder.isLtlEnabled && masterOrder.remainingWeight <= java.math.BigDecimal.ZERO) {
            throw ComplianceViolationException("No remaining capacity for LTL orders")
        }
        
        return true
    }

    /**
     * Проверяет возможность начала аукциона по частичному заказу
     */
    private fun validatePartialOrderAuction(partialOrderId: java.util.UUID): Boolean {
        val partialOrder = partialOrderRepository.findById(partialOrderId)
            ?: throw IllegalArgumentException("Partial order not found: $partialOrderId")
        
        // Проверяем, что частичный заказ доступен
        if (partialOrder.status != com.nodeorb.freight.marketplace.entity.PartialOrderStatus.AVAILABLE) {
            throw ComplianceViolationException("Partial order is not available for bidding")
        }
        
        // Проверяем, что есть место в мастер-заказе
        val masterOrder = partialOrder.masterOrder
        if (masterOrder.remainingWeight < partialOrder.weight) {
            throw ComplianceViolationException("Not enough capacity in master order")
        }
        
        return true
    }

    /**
     * Проверяет, может ли быть завершен аукцион
     */
    @Transactional(readOnly = true)
    fun validateAuctionCompletion(orderId: java.util.UUID, orderType: String): Boolean {
        return when (orderType.lowercase()) {
            "master" -> validateMasterOrderCompletion(orderId)
            "partial" -> validatePartialOrderCompletion(orderId)
            else -> throw IllegalArgumentException("Unknown order type: $orderType")
        }
    }

    /**
     * Проверяет возможность завершения аукциона по мастер-заказу
     */
    private fun validateMasterOrderCompletion(masterOrderId: java.util.UUID): Boolean {
        val masterOrder = masterOrderRepository.findById(masterOrderId)
            ?: throw IllegalArgumentException("Master order not found: $masterOrderId")
        
        // Проверяем, что время аукциона истекло
        if (masterOrder.requiredDeliveryDate.isAfter(LocalDateTime.now())) {
            throw ComplianceViolationException("Auction time has not expired yet")
        }
        
        return true
    }

    /**
     * Проверяет возможность завершения аукциона по частичному заказу
     */
    private fun validatePartialOrderCompletion(partialOrderId: java.util.UUID): Boolean {
        val partialOrder = partialOrderRepository.findById(partialOrderId)
            ?: throw IllegalArgumentException("Partial order not found: $partialOrderId")
        
        // Проверяем, что время аукциона истекло
        if (partialOrder.masterOrder.requiredDeliveryDate.isAfter(LocalDateTime.now())) {
            throw ComplianceViolationException("Auction time has not expired yet")
        }
        
        return true
    }
}