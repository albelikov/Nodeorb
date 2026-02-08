package com.freight.marketplace.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO для заказа в Carrier Opportunity Feed
 * Содержит только те заказы, которые перевозчик может взять по своим допускам
 */
data class CarrierOpportunityDto(
    val orderId: UUID,
    val orderType: String, // MASTER, PARTIAL, FREIGHT
    val title: String,
    val cargoType: String,
    val weight: BigDecimal,
    val volume: BigDecimal,
    val pickupAddress: String,
    val deliveryAddress: String,
    val requiredDeliveryDate: LocalDateTime,
    val maxBidAmount: BigDecimal,
    val currentBestBid: BigDecimal?,
    val matchingScore: Double?,
    val trustLevelRequired: String,
    val securityClearanceRequired: String,
    
    // Информация о соответствии перевозчику
    val carrierTrustLevel: String,
    val carrierSecurityClearance: String,
    val passesTrustCheck: Boolean,
    val trustScore: Double,
    
    val status: String,
    val createdAt: LocalDateTime,
    val auctionEndTime: LocalDateTime
)

/**
 * DTO для перевозчика в Opportunity Feed
 */
data class CarrierProfileDto(
    val carrierId: UUID,
    val companyName: String,
    val trustLevel: String,
    val securityClearance: String,
    val rating: Double,
    val totalOrders: Int,
    val completedOrders: Int,
    val onTimeDeliveryRate: Double,
    val currentLoad: BigDecimal,
    val quotaLimit: BigDecimal,
    val availableCapacity: BigDecimal
)

/**
 * Фильтры для Opportunity Feed
 */
data class OpportunityFilter(
    val cargoTypes: List<String> = emptyList(),
    val minTrustLevel: String? = null,
    val maxDistance: Double? = null,
    val maxBidAmount: BigDecimal? = null,
    val orderStatuses: List<String> = listOf("OPEN", "AUCTION_ACTIVE"),
    val sortBy: String = "matching_score", // matching_score, amount, distance, trust_score
    val sortOrder: String = "desc"
)

/**
 * Статистика для Carrier Dashboard
 */
data class CarrierStatsDto(
    val totalOpportunities: Int,
    val qualifiedOpportunities: Int,
    val dangerousGoodsQualified: Int,
    val highTrustOpportunities: Int,
    val avgMatchingScore: Double,
    val trustLevel: String,
    val securityClearance: String
)