package com.fms.dto

import java.time.LocalDateTime
import java.util.*

data class TransportationRequestDto(
    val id: UUID? = null,
    val customerId: UUID,
    val requestType: String,
    val cargoType: String = "",
    val cargoWeight: Double = 0.0,
    val cargoVolume: Double = 0.0,
    val pickupLocation: String,
    val deliveryLocation: String,
    val pickupTime: LocalDateTime? = null,
    val deliveryTime: LocalDateTime? = null,
    val priority: String = "normal",
    val status: String = "pending",
    val price: Double = 0.0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)