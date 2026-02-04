package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Сервис для Carrier Opportunity Feed
 * Фильтрует заказы по Trust Token перевозчика и предоставляет релевантные возможности
 */
@Service
class OpportunityService(
    private val freightOrderRepository: FreightOrderRepository,
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository,
    private val userProfileRepository: UserProfileRepository,
    private val scmSnapshotRepository: ScmSnapshotRepository,
    private val bidRepository: BidRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(OpportunityService::class.java)
    }

    /**
     * Получает ленту возможностей для перевозчика
     */
    @Transactional(readOnly = true)
    fun getOpportunitiesForCarrier(carrierId: UUID, filter: OpportunityFilter): List<CarrierOpportunityDto> {
        logger.info("Getting opportunities for carrier: $carrierId")
        
        val carrierProfile = userProfileRepository.findById(carrierId)
            ?: throw IllegalArgumentException("Carrier not found: $carrierId")
        
        val scmSnapshot = scmSnapshotRepository.findByCarrierId(carrierId)
        
        val carrierTrustLevel = scmSnapshot?.trustLevel ?: TrustLevel.BASIC
        val carrierSecurityClearance = scmSnapshot?.securityClearance ?: SecurityLevel.NONE
        
        // Получаем все доступные заказы
        val allOpportunities = mutableListOf<CarrierOpportunityDto>()
        
        // Добавляем обычные заказы
        allOpportunities.addAll(getFreightOpportunities(carrierId, carrierTrustLevel, carrierSecurityClearance))
        
        // Добавляем мастер-заказы
        allOpportunities.addAll(getMasterOpportunities(carrierId, carrierTrustLevel, carrierSecurityClearance))
        
        // Добавляем частичные заказы
        allOpportunities.addAll(getPartialOpportunities(carrierId, carrierTrustLevel, carrierSecurityClearance))
        
        // Фильтрация по параметрам
        val filteredOpportunities = allOpportunities.filter { opportunity ->
            filterOpportunity(opportunity, filter, carrierTrustLevel, carrierSecurityClearance)
        }
        
        // Сортировка
        return sortOpportunities(filteredOpportunities, filter)
    }

    /**
     * Получает статистику для Carrier Dashboard
     */
    @Transactional(readOnly = true)
    fun getCarrierStats(carrierId: UUID): CarrierStatsDto {
        logger.info("Getting stats for carrier: $carrierId")
        
        val scmSnapshot = scmSnapshotRepository.findByCarrierId(carrierId)
        val carrierTrustLevel = scmSnapshot?.trustLevel ?: TrustLevel.BASIC
        val carrierSecurityClearance = scmSnapshot?.securityClearance ?: SecurityLevel.NONE
        
        val allOpportunities = mutableListOf<CarrierOpportunityDto>()
        allOpportunities.addAll(getFreightOpportunities(carrierId, carrierTrustLevel, carrierSecurityClearance))
        allOpportunities.addAll(getMasterOpportunities(carrierId, carrierTrustLevel, carrierSecurityClearance))
        allOpportunities.addAll(getPartialOpportunities(carrierId, carrierTrustLevel, carrierSecurityClearance))
        
        val qualifiedOpportunities = allOpportunities.filter { it.passesTrustCheck }
        val dangerousGoodsQualified = qualifiedOpportunities.count { it.cargoType == "DANGEROUS" }
        val highTrustOpportunities = qualifiedOpportunities.count { it.trustScore >= 80.0 }
        
        val avgMatchingScore = if (qualifiedOpportunities.isNotEmpty()) {
            qualifiedOpportunities.mapNotNull { it.matchingScore }.average()
        } else 0.0
        
        return CarrierStatsDto(
            totalOpportunities = allOpportunities.size,
            qualifiedOpportunities = qualifiedOpportunities.size,
            dangerousGoodsQualified = dangerousGoodsQualified,
            highTrustOpportunities = highTrustOpportunities,
            avgMatchingScore = avgMatchingScore,
            trustLevel = carrierTrustLevel.name,
            securityClearance = carrierSecurityClearance.name
        )
    }

    /**
     * Получает обычные заказы для перевозчика
     */
    private fun getFreightOpportunities(
        carrierId: UUID,
        carrierTrustLevel: TrustLevel,
        carrierSecurityClearance: SecurityLevel
    ): List<CarrierOpportunityDto> {
        val orders = freightOrderRepository.findByStatusIn(listOf(OrderStatus.OPEN, OrderStatus.AUCTION_ACTIVE))
        
        return orders.map { order ->
            val currentBestBid = bidRepository.findTopByFreightOrderAndStatusOrderByAmountAsc(order, BidStatus.PENDING)?.amount
            val trustScore = calculateTrustScore(carrierTrustLevel, carrierSecurityClearance, order.cargoType)
            val passesTrustCheck = trustScore >= 50.0 // Минимальный порог для участия
            
            CarrierOpportunityDto(
                orderId = order.id!!,
                orderType = "FREIGHT",
                title = order.title,
                cargoType = order.cargoType.name,
                weight = order.weight,
                volume = order.volume,
                pickupAddress = order.pickupAddress,
                deliveryAddress = order.deliveryAddress,
                requiredDeliveryDate = order.requiredDeliveryDate,
                maxBidAmount = order.maxBidAmount,
                currentBestBid = currentBestBid,
                matchingScore = null,
                trustLevelRequired = getRequiredTrustLevel(order.cargoType).name,
                securityClearanceRequired = getRequiredSecurityClearance(order.cargoType).name,
                carrierTrustLevel = carrierTrustLevel.name,
                carrierSecurityClearance = carrierSecurityClearance.name,
                passesTrustCheck = passesTrustCheck,
                trustScore = trustScore,
                status = order.status.name,
                createdAt = order.createdAt!!,
                auctionEndTime = order.requiredDeliveryDate
            )
        }
    }

    /**
     * Получает мастер-заказы для перевозчика
     */
    private fun getMasterOpportunities(
        carrierId: UUID,
        carrierTrustLevel: TrustLevel,
        carrierSecurityClearance: SecurityLevel
    ): List<CarrierOpportunityDto> {
        val orders = masterOrderRepository.findByStatusIn(listOf(MasterOrderStatus.OPEN, MasterOrderStatus.AUCTION_ACTIVE))
        
        return orders.map { order ->
            val trustScore = calculateTrustScore(carrierTrustLevel, carrierSecurityClearance, order.cargoType)
            val passesTrustCheck = trustScore >= 60.0 // Выше порог для мастер-заказов
            
            CarrierOpportunityDto(
                orderId = order.id!!,
                orderType = "MASTER",
                title = order.title,
                cargoType = order.cargoType.name,
                weight = order.totalWeight,
                volume = order.totalVolume,
                pickupAddress = order.pickupAddress,
                deliveryAddress = order.deliveryAddress,
                requiredDeliveryDate = order.requiredDeliveryDate,
                maxBidAmount = order.maxBidAmount,
                currentBestBid = null,
                matchingScore = null,
                trustLevelRequired = getRequiredTrustLevel(order.cargoType).name,
                securityClearanceRequired = getRequiredSecurityClearance(order.cargoType).name,
                carrierTrustLevel = carrierTrustLevel.name,
                carrierSecurityClearance = carrierSecurityClearance.name,
                passesTrustCheck = passesTrustCheck,
                trustScore = trustScore,
                status = order.status.name,
                createdAt = order.createdAt!!,
                auctionEndTime = order.requiredDeliveryDate
            )
        }
    }

    /**
     * Получает частичные заказы для перевозчика
     */
    private fun getPartialOpportunities(
        carrierId: UUID,
        carrierTrustLevel: TrustLevel,
        carrierSecurityClearance: SecurityLevel
    ): List<CarrierOpportunityDto> {
        val partialOrders = partialOrderRepository.findByStatusIn(listOf(PartialOrderStatus.AVAILABLE, PartialOrderStatus.BIDDING))
        
        return partialOrders.map { partialOrder ->
            val masterOrder = partialOrder.masterOrder
            val trustScore = calculateTrustScore(carrierTrustLevel, carrierSecurityClearance, masterOrder.cargoType)
            val passesTrustCheck = trustScore >= 70.0 // Самый высокий порог для частичных заказов
            
            CarrierOpportunityDto(
                orderId = partialOrder.id!!,
                orderType = "PARTIAL",
                title = "Partial: ${masterOrder.title} (${partialOrder.percentage}%)",
                cargoType = masterOrder.cargoType.name,
                weight = partialOrder.weight,
                volume = partialOrder.volume,
                pickupAddress = masterOrder.pickupAddress,
                deliveryAddress = masterOrder.deliveryAddress,
                requiredDeliveryDate = masterOrder.requiredDeliveryDate,
                maxBidAmount = masterOrder.maxBidAmount,
                currentBestBid = null,
                matchingScore = null,
                trustLevelRequired = getRequiredTrustLevel(masterOrder.cargoType).name,
                securityClearanceRequired = getRequiredSecurityClearance(masterOrder.cargoType).name,
                carrierTrustLevel = carrierTrustLevel.name,
                carrierSecurityClearance = carrierSecurityClearance.name,
                passesTrustCheck = passesTrustCheck,
                trustScore = trustScore,
                status = partialOrder.status.name,
                createdAt = partialOrder.createdAt!!,
                auctionEndTime = masterOrder.requiredDeliveryDate
            )
        }
    }

    /**
     * Фильтрует возможности по параметрам
     */
    private fun filterOpportunity(
        opportunity: CarrierOpportunityDto,
        filter: OpportunityFilter,
        carrierTrustLevel: TrustLevel,
        carrierSecurityClearance: SecurityLevel
    ): Boolean {
        // Фильтр по типу груза
        if (filter.cargoTypes.isNotEmpty() && !filter.cargoTypes.contains(opportunity.cargoType)) {
            return false
        }
        
        // Фильтр по минимальному уровню доверия
        if (filter.minTrustLevel != null) {
            val requiredLevel = TrustLevel.valueOf(filter.minTrustLevel.uppercase())
            if (carrierTrustLevel < requiredLevel) {
                return false
            }
        }
        
        // Фильтр по максимальной сумме ставки
        if (filter.maxBidAmount != null && opportunity.maxBidAmount > filter.maxBidAmount) {
            return false
        }
        
        // Фильтр по статусу заказа
        if (!filter.orderStatuses.contains(opportunity.status)) {
            return false
        }
        
        // Фильтр по соответствию требованиям
        if (!opportunity.passesTrustCheck) {
            return false
        }
        
        return true
    }

    /**
     * Сортирует возможности
     */
    private fun sortOpportunities(
        opportunities: List<CarrierOpportunityDto>,
        filter: OpportunityFilter
    ): List<CarrierOpportunityDto> {
        return when (filter.sortBy.lowercase()) {
            "matching_score" -> opportunities.sortedByDescending { it.matchingScore ?: 0.0 }
            "amount" -> opportunities.sortedByDescending { it.maxBidAmount }
            "trust_score" -> opportunities.sortedByDescending { it.trustScore }
            else -> opportunities.sortedByDescending { it.createdAt }
        }.let { sorted ->
            if (filter.sortOrder.lowercase() == "asc") sorted.reversed() else sorted
        }
    }

    /**
     * Рассчитывает уровень доверия для конкретного заказа
     */
    private fun calculateTrustScore(
        carrierTrustLevel: TrustLevel,
        carrierSecurityClearance: SecurityLevel,
        cargoType: CargoType
    ): Double {
        var score = when (carrierTrustLevel) {
            TrustLevel.BASIC -> 30.0
            TrustLevel.VERIFIED -> 60.0
            TrustLevel.TRUSTED -> 80.0
            TrustLevel.PREMIUM -> 95.0
        }
        
        // Повышение за уровень безопасности
        when (carrierSecurityClearance) {
            SecurityLevel.NONE -> score -= 10.0
            SecurityLevel.CONFIDENTIAL -> score += 10.0
            SecurityLevel.SECRET -> score += 20.0
            SecurityLevel.TOP_SECRET -> score += 30.0
        }
        
        // Понижение для опасных грузов
        if (cargoType == CargoType.DANGEROUS && carrierSecurityClearance == SecurityLevel.NONE) {
            score -= 30.0
        }
        
        // Повышение для обычных грузов
        if (cargoType == CargoType.GENERAL) {
            score += 10.0
        }
        
        return score.coerceIn(0.0, 100.0)
    }

    /**
     * Получает требуемый уровень доверия для типа груза
     */
    private fun getRequiredTrustLevel(cargoType: CargoType): TrustLevel {
        return when (cargoType) {
            CargoType.DANGEROUS -> TrustLevel.TRUSTED
            CargoType.PERISHABLE -> TrustLevel.VERIFIED
            CargoType.REFRIGERATED -> TrustLevel.VERIFIED
            CargoType.OVERSIZED -> TrustLevel.VERIFIED
            else -> TrustLevel.BASIC
        }
    }

    /**
     * Получает требуемый уровень безопасности для типа груза
     */
    private fun getRequiredSecurityClearance(cargoType: CargoType): SecurityLevel {
        return when (cargoType) {
            CargoType.DANGEROUS -> SecurityLevel.CONFIDENTIAL
            CargoType.PERISHABLE -> SecurityLevel.NONE
            CargoType.REFRIGERATED -> SecurityLevel.NONE
            CargoType.OVERSIZED -> SecurityLevel.NONE
            else -> SecurityLevel.NONE
        }
    }
}