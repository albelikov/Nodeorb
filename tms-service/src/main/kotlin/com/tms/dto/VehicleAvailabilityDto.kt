package com.tms.dto

data class VehicleAvailabilityDto(
    val vehicleId: String,
    val type: String,
    val capacity: Double,
    val available: Boolean,
    val location: String
)

data class VehicleAvailabilityCheckDto(
    val origin: String,
    val destination: String,
    val weight: Double,
    val dimensions: DimensionsDto
)