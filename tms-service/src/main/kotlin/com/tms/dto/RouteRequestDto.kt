package com.tms.dto

data class RouteRequestDto(
    val origin: LocationDto,
    val destination: LocationDto,
    val waypoints: List<LocationDto>? = null,
    val vehicleType: String,
    val cargo: CargoDetailsDto,
    val preferences: RoutePreferencesDto,
    val timeWindows: TimeWindowsDto
)

data class LocationDto(
    val address: String,
    val latitude: Double,
    val longitude: Double
)

data class CargoDetailsDto(
    val weight: Double,
    val volume: Double,
    val type: String,
    val hazmat: Boolean
)

data class RoutePreferencesDto(
    val optimization: String,
    val avoidTolls: Boolean,
    val avoidHighways: Boolean
)

data class TimeWindowsDto(
    val pickup: TimeWindowDto,
    val delivery: TimeWindowDto
)

data class TimeWindowDto(
    val start: String,
    val end: String
)