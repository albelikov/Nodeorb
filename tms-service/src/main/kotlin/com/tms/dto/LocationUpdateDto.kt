package com.tms.dto

data class LocationUpdateDto(
    val vehicleId: Long,
    val shipmentId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Double?,
    val heading: Double?,
    val timestamp: String
)