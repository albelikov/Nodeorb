package com.freight.marketplace.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO для ставки в Shipper Dashboard
 * Содержит информацию о ставке с расчетом соответствия правилам
 */
data class ShipperBidDto(
    val bidId: UUID,
    val carrierId: UUID,
    val carrierName: String,
    val amount: BigDecimal,
    val proposedDeliveryDate: LocalDateTime,
    val notes: String?,
    val matchingScore: Double?,
    val scoreBreakdown: String?,
    val status: String,
    val createdAt: LocalDateTime,
    
    // Информация о соответствии правилам
    val passesDangerousGoodsRules: Boolean,
    val ruleViolations: List<String>,
    
    // Дополнительная информация о перевозчике
    val carrierRating: Double,
    val totalOrders: Int,
    val completedOrders: Int,
    val onTimeDeliveryRate: Double
)

/**
 * DTO для заказа в Shipper Dashboard
 */
data class ShipperOrderDto(
    val orderId: UUID,
    val title: String,
    val cargoType: String,
    val weight: BigDecimal,
    val volume: BigDecimal,
    val pickupAddress: String,
    val deliveryAddress: String,
    val requiredDeliveryDate: LocalDateTime,
    val maxBidAmount: BigDecimal,
    val status: String,
    val bids: List<ShipperBidDto>,
    val createdAt: LocalDateTime
)

/**
 * DTO для агрегированного прогресса заказа в Shipper Dashboard
 */
data class ShipperOrderProgressDto(
    val orderId: UUID,
    val orderType: String, // MASTER, PARTIAL, FREIGHT
    val totalWeight: BigDecimal,
    val totalVolume: BigDecimal,
    val allocatedWeight: BigDecimal,
    val allocatedVolume: BigDecimal,
    val pendingWeight: BigDecimal,
    val pendingVolume: BigDecimal,
    val progressPercentage: Double,
    
    // Цвета прогресса (Blue/Yellow/Grey)
    val blueProgress: Double,   // Завершенные заказы
    val yellowProgress: Double, // В процессе
    val greyProgress: Double,   // Ожидание
    val status: String
)

/**
 * DTO для частичного заказа в прогрессе в Shipper Dashboard
 */
data class ShipperPartialOrderProgressDto(
    val partialOrderId: UUID,
    val percentage: Double,
    val weight: BigDecimal,
    val volume: BigDecimal,
    val status: String,
    val assignedCarrierId: UUID?,
    val allocatedAt: LocalDateTime?
)
