package com.fms.dto

import java.time.LocalDateTime
import java.util.*

data class FuelRefuelingDto(
    val id: UUID? = null,
    val vehicleId: UUID,
    val driverId: UUID? = null,
    val fuelType: String,
    val volume: Double,
    val cost: Double = 0.0,
    val location: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime? = null
)

data class FuelConsumptionDto(
    val id: UUID? = null,
    val vehicleId: UUID,
    val driverId: UUID? = null,
    val fuelType: String,
    val consumptionRate: Double = 0.0,
    val actualConsumption: Double = 0.0,
    val distance: Double = 0.0,
    val tripId: UUID? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime? = null
)