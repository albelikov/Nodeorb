package com.freight.marketplace.service

import com.freight.marketplace.dto.QuotaCheckResult
import com.freight.marketplace.dto.CarrierQuotaInfo
import com.freight.marketplace.entity.BidEntity
import com.freight.marketplace.entity.BidStatus
import com.freight.marketplace.entity.MasterOrderEntity
import com.freight.marketplace.entity.PartialOrderEntity
import com.freight.marketplace.repository.BidRepository
import com.freight.marketplace.repository.MasterOrderRepository
import com.freight.marketplace.repository.PartialOrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Сервис контроля квот перевозчиков
 * Проверяет, может ли перевозчик принять новый заказ с учетом его текущей загрузки
 */
@Service
class QuotaService(
    private val bidRepository: BidRepository,
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(QuotaService::class.java)
        private const val DEFAULT_QUOTA_LIMIT = 200.0 // тонн по умолчанию
        private const val QUOTA_BUFFER_PERCENT = 0.1 // 10% буфер для безопасности
    }

    /**
     * Проверяет, может ли перевозчик принять новый заказ
     * @param carrierId ID перевозчика
     * @param loadWeight Вес нового заказа в тоннах
     * @return Результат проверки квоты
     */
    @Transactional(readOnly = true)
    fun checkCarrierQuota(carrierId: java.util.UUID, loadWeight: BigDecimal): QuotaCheckResult {
        logger.info("Checking quota for carrier: $carrierId, load: $loadWeight")
        
        val quotaInfo = getCarrierQuotaInfo(carrierId)
        
        // Рассчитываем общую нагрузку после принятия нового заказа
        val totalLoadAfterNewOrder = quotaInfo.currentActiveLoad + quotaInfo.pendingLoad + 
                                    quotaInfo.committedLoad + loadWeight
        
        val availableCapacity = quotaInfo.quotaLimit - totalLoadAfterNewOrder
        val isWithinQuota = availableCapacity >= BigDecimal.ZERO
        
        val violations = mutableListOf<String>()
        if (!isWithinQuota) {
            violations.add("Превышение квоты: доступно ${availableCapacity.abs()} тонн")
        }
        
        return QuotaCheckResult(
            isWithinQuota = isWithinQuota,
            currentLoad = quotaInfo.currentActiveLoad,
            quotaLimit = quotaInfo.quotaLimit,
            availableCapacity = availableCapacity,
            violations = violations
        )
    }

    /**
     * Проверяет квоту для LTL заказа (частичного заказа)
     * @param carrierId ID перевозчика
     * @param partialOrder Частичный заказ
     * @return Результат проверки квоты
     */
    @Transactional(readOnly = true)
    fun checkLtlQuota(carrierId: java.util.UUID, partialOrder: PartialOrderEntity): QuotaCheckResult {
        val loadWeight = partialOrder.weight
        return checkCarrierQuota(carrierId, loadWeight)
    }

    /**
     * Проверяет квоту для полного заказа (FreightOrder)
     * @param carrierId ID перевозчика
     * @param masterOrder Мастер-заказ
     * @return Результат проверки квоты
     */
    @Transactional(readOnly = true)
    fun checkFullOrderQuota(carrierId: java.util.UUID, masterOrder: MasterOrderEntity): QuotaCheckResult {
        val loadWeight = masterOrder.totalWeight
        return checkCarrierQuota(carrierId, loadWeight)
    }

    /**
     * Получает информацию о квоте перевозчика
     */
    private fun getCarrierQuotaInfo(carrierId: java.util.UUID): CarrierQuotaInfo {
        // В реальной системе здесь будет вызов SCM сервиса для получения данных о квоте
        // Пока используем заглушку с фиксированными значениями
        
        val currentActiveLoad = getCurrentActiveLoad(carrierId)
        val pendingLoad = getPendingLoad(carrierId)
        val committedLoad = getCommittedLoad(carrierId)
        
        // В реальной системе лимит квоты должен приходить из SCM
        val quotaLimit = BigDecimal.valueOf(DEFAULT_QUOTA_LIMIT)
        
        return CarrierQuotaInfo(
            carrierId = carrierId,
            quotaLimit = quotaLimit,
            currentActiveLoad = currentActiveLoad,
            pendingLoad = pendingLoad,
            committedLoad = committedLoad,
            lastUpdated = LocalDateTime.now()
        )
    }

    /**
     * Рассчитывает текущую активную нагрузку перевозчика
     */
    private fun getCurrentActiveLoad(carrierId: java.util.UUID): BigDecimal {
        val activeBids = bidRepository.findByCarrierIdAndStatusIn(
            carrierId, 
            listOf(BidStatus.ACCEPTED, BidStatus.IN_PROGRESS)
        )
        
        return activeBids.sumOf { bid ->
            when {
                bid.freightOrder != null -> bid.freightOrder.weight
                bid.masterOrder != null -> bid.masterOrder.totalWeight
                bid.partialOrder != null -> bid.partialOrder.weight
                else -> BigDecimal.ZERO
            }
        }
    }

    /**
     * Рассчитывает нагрузку по ставкам в статусе PENDING
     */
    private fun getPendingLoad(carrierId: java.util.UUID): BigDecimal {
        val pendingBids = bidRepository.findByCarrierIdAndStatus(carrierId, BidStatus.PENDING)
        
        return pendingBids.sumOf { bid ->
            when {
                bid.freightOrder != null -> bid.freightOrder.weight
                bid.masterOrder != null -> bid.masterOrder.totalWeight
                bid.partialOrder != null -> bid.partialOrder.weight
                else -> BigDecimal.ZERO
            }
        }
    }

    /**
     * Рассчитывает нагрузку по ставкам в статусе COMMITTED
     */
    private fun getCommittedLoad(carrierId: java.util.UUID): BigDecimal {
        val committedBids = bidRepository.findByCarrierIdAndStatusIn(
            carrierId, 
            listOf(BidStatus.COMMITTED)
        )
        
        return committedBids.sumOf { bid ->
            when {
                bid.freightOrder != null -> bid.freightOrder.weight
                bid.masterOrder != null -> bid.masterOrder.totalWeight
                bid.partialOrder != null -> bid.partialOrder.weight
                else -> BigDecimal.ZERO
            }
        }
    }

    /**
     * Проверяет, может ли перевозчик принять ставку с учетом всех ограничений
     */
    @Transactional(readOnly = true)
    fun validateBidQuota(
        carrierId: java.util.UUID,
        freightOrderId: java.util.UUID?,
        masterOrderId: java.util.UUID?,
        partialOrderId: java.util.UUID?
    ): QuotaCheckResult {
        return when {
            freightOrderId != null -> {
                // Проверка для полного заказа
                val freightOrder = bidRepository.findFreightOrderById(freightOrderId)
                    ?: throw IllegalArgumentException("Freight order not found: $freightOrderId")
                checkFullOrderQuota(carrierId, freightOrder.masterOrder ?: 
                    throw IllegalArgumentException("Master order not found for freight order"))
            }
            masterOrderId != null -> {
                // Проверка для мастер-заказа
                val masterOrder = masterOrderRepository.findById(masterOrderId)
                    ?: throw IllegalArgumentException("Master order not found: $masterOrderId")
                checkFullOrderQuota(carrierId, masterOrder)
            }
            partialOrderId != null -> {
                // Проверка для частичного заказа
                val partialOrder = partialOrderRepository.findById(partialOrderId)
                    ?: throw IllegalArgumentException("Partial order not found: $partialOrderId")
                checkLtlQuota(carrierId, partialOrder)
            }
            else -> throw IllegalArgumentException("No order ID provided")
        }
    }
}