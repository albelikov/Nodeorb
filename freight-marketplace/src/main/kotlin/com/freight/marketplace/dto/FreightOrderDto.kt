package com.freight.marketplace.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.freight.marketplace.entity.CargoType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FreightOrderDto(
    val id: UUID? = null,
    val shipperId: UUID,
    val title: String,
    val description: String?,
    val cargoType: CargoType,
    val weight: BigDecimal,
    val volume: BigDecimal,
    val pickupLocation: LocationDto,
    val deliveryLocation: LocationDto,
    val requiredDeliveryDate: LocalDateTime,
    val maxBidAmount: BigDecimal,
    val status: OrderStatus = OrderStatus.OPEN,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val bids: List<BidDto>? = emptyList()
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BidDto(
    val id: UUID? = null,
    val carrierId: UUID,
    val freightOrderId: UUID,
    val amount: BigDecimal,
    val proposedDeliveryDate: LocalDateTime,
    val notes: String?,
    val status: BidStatus = BidStatus.PENDING,
    val score: Double? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val country: String,
    val postalCode: String?
)

enum class OrderStatus {
    OPEN,
    AUCTION_ACTIVE,
    AWARDED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class BidStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserProfileDto(
    val userId: UUID,
    val companyName: String,
    val rating: Double,
    val totalOrders: Int,
    val completedOrders: Int,
    val joinedAt: LocalDateTime
)