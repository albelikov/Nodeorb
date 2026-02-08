package com.tms.dto

data class TrackingResponseDto(
    val shipmentId: Long,
    val currentLocation: CurrentLocationDto,
    val estimatedArrival: String,
    val distanceRemaining: Double,
    val percentComplete: Double
)

data class CurrentLocationDto(
    val latitude: Double,
    val longitude: Double,
    val speed: Double?,
    val lastUpdate: String
)