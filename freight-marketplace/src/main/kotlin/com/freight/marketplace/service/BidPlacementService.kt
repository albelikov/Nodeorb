package com.freight.marketplace.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.freight.marketplace.dto.BidPlacementEvent
import com.freight.marketplace.dto.BidScoringEvent
import com.freight.marketplace.entity.BidEntity
import com.freight.marketplace.entity.FreightOrderEntity
import com.freight.marketplace.entity.MasterOrderEntity
import com.freight.marketplace.entity.PartialOrderEntity
import com.freight.marketplace.entity.BidStatus
import com.freight.marketplace.exception.ComplianceViolationException
import com.freight.marketplace.repository.BidRepository
import com.freight.marketplace.repository.FreightOrderRepository
import com.freight.marketplace.repository.MasterOrderRepository
import com.freight.marketplace.repository.PartialOrderRepository
import com.nodeorb.scmclient.SCMClient
import com.nodeorb.scmclient.SCMClientFactory
import com.nodeorb.scmclient.model.ValidationResult
import com.nodeorb.scmclient.exception.ScmSecurityBlockException
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Сервис для обработки размещения ставок с автоматическим запуском скоринга
 * Использует SCM Client SDK для проверки соответствия требованиям
 */
@Service
class BidPlacementService(
    private val bidRepository: BidRepository,
    private val freightOrderRepository: FreightOrderRepository,
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository,
    private val complianceService: ComplianceService,
    private val quotaService: QuotaService,
    private val auctionGuard: AuctionGuard,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(BidPlacementService::class.java)
        private const val BID_PLACEMENT_TOPIC = "bid-placement-events"
        private const val BID_SCORING_TOPIC = "bid-scoring-events"
    }

    private val scmClient: SCMClient = SCMClientFactory.createProductionClient(
        host = "scm-service",
        port = 9090
    )

    /**
     * Размещает новую ставку и запускает асинхронный расчет скоринга
     * Сначала проверяет соответствие требованиям через SCM Service
     */
    @Transactional
    fun placeBid(bidPlacementEvent: BidPlacementEvent): BidEntity {
        logger.info("Processing bid placement for carrier: ${bidPlacementEvent.carrierId}")
        
        // 1. Проверяем соответствие требованиям через SCM Service
        validateBidCompliance(bidPlacementEvent)
        
        // 2. Проверяем квоту перевозчика
        validateCarrierQuota(bidPlacementEvent)
        
        // 3. Проверяем anti-sniping и атомарность
        validateAuctionRules(bidPlacementEvent)
        
        // 4. Создаем ставку
        val bid = createBid(bidPlacementEvent)
        
        // 5. Сохраняем ставку
        val savedBid = bidRepository.save(bid)
        
        // 6. Публикуем событие о размещении ставки
        publishBidPlacementEvent(savedBid)
        
        // 7. Публикуем событие для расчета скоринга
        publishBidScoringEvent(savedBid)
        
        logger.info("Successfully placed bid: ${savedBid.id}")
        return savedBid
    }

    /**
     * Проверяет соответствие ставки требованиям через SCM Service
     */
    private fun validateBidCompliance(bidPlacementEvent: BidPlacementEvent) {
        logger.info("Validating bid compliance for carrier: ${bidPlacementEvent.carrierId}")
        
        try {
            // Выполняем проверку через SCM Service
            val validationResult = scmClient.validateCost(
                userId = bidPlacementEvent.carrierId.toString(),
                orderId = bidPlacementEvent.masterOrderId.toString(),
                category = "BID_PLACEMENT",
                value = bidPlacementEvent.amount,
                lat = bidPlacementEvent.route.pickupLocation.latitude,
                lon = bidPlacementEvent.route.pickupLocation.longitude
            )
            
            if (!validationResult.allowed) {
                throw ComplianceViolationException(
                    "Bid placement failed: ${validationResult.reason}"
                )
            }
            
            logger.info("Bid compliance validated successfully")
            
        } catch (e: ScmSecurityBlockException) {
            logger.warn("SCM security block: ${e.message}")
            throw ComplianceViolationException(
                "Bid placement blocked: ${e.message}"
            )
        } catch (e: Exception) {
            logger.error("Error validating bid compliance: ${e.message}", e)
            throw ComplianceViolationException(
                "Bid placement failed due to compliance check error"
            )
        }
    }

    /**
     * Создает сущность ставки
     */
    private fun createBid(bidPlacementEvent: BidPlacementEvent): BidEntity {
        val freightOrder = bidPlacementEvent.freightOrderId?.let {
            freightOrderRepository.findById(it)
                ?: throw IllegalArgumentException("Freight order not found: $it")
        }
        
        val masterOrder = bidPlacementEvent.masterOrderId?.let {
            masterOrderRepository.findById(it)
                ?: throw IllegalArgumentException("Master order not found: $it")
        }
        
        val partialOrder = bidPlacementEvent.partialOrderId?.let {
            partialOrderRepository.findById(it)
                ?: throw IllegalArgumentException("Partial order not found: $it")
        }
        
        // Проверяем, что сумма ставки не превышает лимит
        val maxAmount = when {
            freightOrder != null -> freightOrder.maxBidAmount
            masterOrder != null -> masterOrder.maxBidAmount
            partialOrder != null -> partialOrder.maxBidAmount
            else -> throw IllegalArgumentException("No order found for bid")
        }
        
        if (bidPlacementEvent.amount > maxAmount.toDouble()) {
            throw IllegalArgumentException("Bid amount exceeds maximum allowed: ${maxAmount.toDouble()}")
        }
        
        return BidEntity(
            carrierId = bidPlacementEvent.carrierId,
            freightOrder = freightOrder,
            masterOrder = masterOrder,
            partialOrder = partialOrder,
            amount = BigDecimal.valueOf(bidPlacementEvent.amount),
            proposedDeliveryDate = bidPlacementEvent.proposedDeliveryDate,
            notes = bidPlacementEvent.notes,
            status = BidStatus.PENDING,
            matchingScore = null, // Будет заполнено после расчета скоринга
            scoreBreakdown = null, // Будет заполнено после расчета скоринга
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * Публикует событие о размещении ставки
     */
    private fun publishBidPlacementEvent(bid: BidEntity) {
        val event = BidPlacementEvent(
            bidId = bid.id!!,
            carrierId = bid.carrierId,
            freightOrderId = bid.freightOrder?.id,
            masterOrderId = bid.masterOrder?.id,
            partialOrderId = bid.partialOrder?.id,
            amount = bid.amount.toDouble(),
            proposedDeliveryDate = bid.proposedDeliveryDate,
            notes = bid.notes,
            timestamp = System.currentTimeMillis()
        )
        
        kafkaTemplate.send(BID_PLACEMENT_TOPIC, bid.id.toString(), event)
        logger.info("Published bid placement event for bid: ${bid.id}")
    }

    /**
     * Публикует событие для расчета скоринга
     */
    private fun publishBidScoringEvent(bid: BidEntity) {
        val event = BidScoringEvent(
            bidId = bid.id!!,
            carrierId = bid.carrierId,
            freightOrderId = bid.freightOrder?.id,
            masterOrderId = bid.masterOrder?.id,
            partialOrderId = bid.partialOrder?.id,
            amount = bid.amount.toDouble(),
            timestamp = System.currentTimeMillis()
        )
        
        kafkaTemplate.send(BID_SCORING_TOPIC, bid.id.toString(), event)
        logger.info("Published bid scoring event for bid: ${bid.id}")
    }

    /**
     * Обновляет статус ставки
     */
    @Transactional
    fun updateBidStatus(bidId: UUID, status: BidStatus): BidEntity {
        val bid = bidRepository.findById(bidId)
            ?: throw IllegalArgumentException("Bid not found: $bidId")
        
        bid.status = status
        bid.updatedAt = LocalDateTime.now()
        
        return bidRepository.save(bid)
    }

    /**
     * Получает ставку с результатами скоринга
     */
    fun getBidWithScoring(bidId: UUID): BidEntity {
        val bid = bidRepository.findById(bidId)
            ?: throw IllegalArgumentException("Bid not found: $bidId")
        
        // Если скоринг еще не рассчитан, возвращаем ставку без баллов
        return bid
    }

    /**
     * Проверяет квоту перевозчика перед размещением ставки
     */
    private fun validateCarrierQuota(bidPlacementEvent: BidPlacementEvent) {
        val quotaResult = quotaService.validateBidQuota(
            bidPlacementEvent.carrierId,
            bidPlacementEvent.freightOrderId,
            bidPlacementEvent.masterOrderId,
            bidPlacementEvent.partialOrderId
        )
        
        if (!quotaResult.isWithinQuota) {
            throw ComplianceViolationException(
                "Quota violation: ${quotaResult.violations.joinToString(", ")}"
            )
        }
    }

    /**
     * Проверяет правила аукциона (anti-sniping и атомарность)
     */
    private fun validateAuctionRules(bidPlacementEvent: BidPlacementEvent) {
        // Создаем временную ставку для проверки
        val tempBid = createTempBidForValidation(bidPlacementEvent)
        
        // Проверяем anti-sniping и атомарность
        auctionGuard.validateBidPlacement(tempBid)
    }

    /**
     * Создает временную ставку для валидации правил аукциона
     */
    private fun createTempBidForValidation(bidPlacementEvent: BidPlacementEvent): BidEntity {
        return BidEntity(
            carrierId = bidPlacementEvent.carrierId,
            freightOrder = bidPlacementEvent.freightOrderId?.let { freightOrderRepository.findById(it) },
            masterOrder = bidPlacementEvent.masterOrderId?.let { masterOrderRepository.findById(it) },
            partialOrder = bidPlacementEvent.partialOrderId?.let { partialOrderRepository.findById(it) },
            amount = java.math.BigDecimal.valueOf(bidPlacementEvent.amount),
            proposedDeliveryDate = bidPlacementEvent.proposedDeliveryDate,
            notes = bidPlacementEvent.notes,
            status = com.freight.marketplace.entity.BidStatus.PENDING,
            matchingScore = null,
            scoreBreakdown = null,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
    }
}