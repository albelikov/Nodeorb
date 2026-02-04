package com.nodeorb.freight.marketplace.service.deal

import com.nodeorb.freight.marketplace.entity.BidEntity
import com.nodeorb.freight.marketplace.entity.MasterOrderEntity
import com.nodeorb.freight.marketplace.entity.deal.ContractStatus
import com.nodeorb.freight.marketplace.exception.FreightMarketplaceException
import com.nodeorb.freight.marketplace.repository.BidRepository
import com.nodeorb.freight.marketplace.repository.MasterOrderRepository
import com.nodeorb.freight.marketplace.service.finance.EscrowService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Сервис управления сделками и переходом заказов в активную фазу
 */
@Service
class DealManagementService(
    private val bidRepository: BidRepository,
    private val masterOrderRepository: MasterOrderRepository,
    private val escrowService: EscrowService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(DealManagementService::class.java)
    }

    /**
     * Подтверждает ставку и переводит заказ в активную фазу
     * @param bidId ID ставки
     * @return Обновленная ставка
     */
    @Transactional
    fun awardBid(bidId: UUID): BidEntity {
        logger.info("Awarding bid: $bidId")
        
        // 1. Находим ставку
        val bid = bidRepository.findById(bidId)
            ?: throw FreightMarketplaceException("Bid not found: $bidId")
        
        // 2. Проверяем, что ставка еще не подтверждена
        if (bid.status.name == "ACCEPTED") {
            throw FreightMarketplaceException("Bid already awarded: $bidId")
        }
        
        // 3. Проверяем, что ставка принадлежит активному заказу
        val masterOrder = bid.masterOrder
            ?: throw FreightMarketplaceException("Master order not found for bid: $bidId")
        
        if (masterOrder.status.name != "AUCTION_ACTIVE") {
            throw FreightMarketplaceException("Cannot award bid: master order status is ${masterOrder.status}, expected AUCTION_ACTIVE")
        }
        
        // 4. Блокируем средства на эскроу-счете
        val contract = escrowService.lockFunds(bidId)
        
        // 5. Подтверждаем финансирование
        escrowService.confirmFunding(contract.id!!)
        
        // 6. Переводим заказ в активную фазу
        masterOrder.status = com.nodeorb.freight.marketplace.entity.OrderStatus.IN_PROGRESS
        masterOrderRepository.save(masterOrder)
        
        // 7. Обновляем статус ставки
        bid.status = com.nodeorb.freight.marketplace.entity.BidStatus.ACCEPTED
        bid.updatedAt = java.time.LocalDateTime.now()
        val updatedBid = bidRepository.save(bid)
        
        logger.info("Successfully awarded bid: $bidId, contract: ${contract.id}")
        return updatedBid
    }

    /**
     * Отменяет подтверждение ставки
     * @param bidId ID ставки
     * @return Обновленная ставка
     */
    @Transactional
    fun cancelAward(bidId: UUID): BidEntity {
        logger.info("Cancelling award for bid: $bidId")
        
        // 1. Находим ставку
        val bid = bidRepository.findById(bidId)
            ?: throw FreightMarketplaceException("Bid not found: $bidId")
        
        // 2. Проверяем, что ставка подтверждена
        if (bid.status.name != "ACCEPTED") {
            throw FreightMarketplaceException("Cannot cancel award: bid status is ${bid.status}, expected ACCEPTED")
        }
        
        // 3. Находим контракт
        val contract = escrowService.getContractByBidId(bidId)
            ?: throw FreightMarketplaceException("Contract not found for bid: $bidId")
        
        // 4. Проверяем, что контракт еще не в статусе IN_TRANSIT
        if (contract.status == ContractStatus.IN_TRANSIT || contract.status == ContractStatus.RELEASED) {
            throw FreightMarketplaceException("Cannot cancel award: contract status is ${contract.status}")
        }
        
        // 5. Отменяем финансирование
        escrowService.markAsDisputed(contract.id!!, "Award cancelled by shipper")
        
        // 6. Возвращаем заказ в состояние AUCTION_ACTIVE
        val masterOrder = bid.masterOrder
            ?: throw FreightMarketplaceException("Master order not found for bid: $bidId")
        
        masterOrder.status = com.nodeorb.freight.marketplace.entity.OrderStatus.AUCTION_ACTIVE
        masterOrderRepository.save(masterOrder)
        
        // 7. Отменяем статус ставки
        bid.status = com.nodeorb.freight.marketplace.entity.BidStatus.PENDING
        bid.updatedAt = java.time.LocalDateTime.now()
        val updatedBid = bidRepository.save(bid)
        
        logger.info("Successfully cancelled award for bid: $bidId")
        return updatedBid
    }

    /**
     * Отмечает начало транспортировки
     * @param bidId ID ставки
     * @return Обновленный контракт
     */
    @Transactional
    fun markTransportationStarted(bidId: UUID): com.nodeorb.freight.marketplace.entity.deal.ContractEntity {
        logger.info("Marking transportation started for bid: $bidId")
        
        // 1. Находим контракт
        val contract = escrowService.getContractByBidId(bidId)
            ?: throw FreightMarketplaceException("Contract not found for bid: $bidId")
        
        // 2. Переводим контракт в статус IN_TRANSIT
        return escrowService.markInTransit(contract.id!!)
    }

    /**
     * Завершает сделку после успешной доставки
     * @param bidId ID ставки
     * @param evidenceHash Хэш доказательства доставки (e-POD)
     * @return Обновленный контракт
     */
    @Transactional
    fun completeDeal(bidId: UUID, evidenceHash: String? = null): com.nodeorb.freight.marketplace.entity.deal.ContractEntity {
        logger.info("Completing deal for bid: $bidId")
        
        // 1. Находим контракт
        val contract = escrowService.getContractByBidId(bidId)
            ?: throw FreightMarketplaceException("Contract not found for bid: $bidId")
        
        // 2. Освобождаем средства
        return escrowService.releaseFunds(contract.id!!, evidenceHash)
    }

    /**
     * Инициирует спор по сделке
     * @param bidId ID ставки
     * @param reason Причина спора
     * @return Обновленный контракт
     */
    @Transactional
    fun initiateDispute(bidId: UUID, reason: String): com.nodeorb.freight.marketplace.entity.deal.ContractEntity {
        logger.info("Initiating dispute for bid: $bidId, reason: $reason")
        
        // 1. Находим контракт
        val contract = escrowService.getContractByBidId(bidId)
            ?: throw FreightMarketplaceException("Contract not found for bid: $bidId")
        
        // 2. Переводим контракт в статус DISPUTED
        return escrowService.markAsDisputed(contract.id!!, reason)
    }

    /**
     * Получает статус сделки
     * @param bidId ID ставки
     * @return Статус контракта
     */
    fun getDealStatus(bidId: UUID): ContractStatus? {
        val contract = escrowService.getContractByBidId(bidId)
        return contract?.status
    }

    /**
     * Проверяет, может ли быть подтверждена ставка
     * @param bidId ID ставки
     * @return true, если ставку можно подтвердить
     */
    fun canAwardBid(bidId: UUID): Boolean {
        return try {
            val bid = bidRepository.findById(bidId)
                ?: return false
            
            val masterOrder = bid.masterOrder
                ?: return false
            
            // Проверяем, что заказ в состоянии AUCTION_ACTIVE
            if (masterOrder.status.name != "AUCTION_ACTIVE") {
                return false
            }
            
            // Проверяем, что ставка не подтверждена
            if (bid.status.name == "ACCEPTED") {
                return false
            }
            
            // Проверяем, что контракт еще не создан
            val existingContract = escrowService.getContractByBidId(bidId)
            if (existingContract != null) {
                return false
            }
            
            true
        } catch (e: Exception) {
            logger.error("Error checking if bid can be awarded: $bidId", e)
            false
        }
    }

    /**
     * Получает информацию о сделке
     * @param bidId ID ставки
     * @return Информация о контракте и статусе
     */
    fun getDealInfo(bidId: UUID): Map<String, Any> {
        val bid = bidRepository.findById(bidId)
            ?: throw FreightMarketplaceException("Bid not found: $bidId")
        
        val contract = escrowService.getContractByBidId(bidId)
        
        return mapOf(
            "bidId" to bidId,
            "carrierId" to bid.carrierId,
            "amount" to bid.amount.toDouble(),
            "bidStatus" to bid.status.name,
            "contract" to (contract?.let { mapOf(
                "id" to it.id,
                "status" to it.status.name,
                "amount" to it.amount.toDouble(),
                "evidenceHash" to it.evidenceHash,
                "createdAt" to it.createdAt
            ) } ?: null),
            "canAward" to canAwardBid(bidId)
        )
    }
}