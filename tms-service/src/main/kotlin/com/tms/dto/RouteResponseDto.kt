package com.tms.dto

data class RouteResponseDto(
    val routeId: String,
    val status: String,
    val distance: Double,
    val duration: Int,
    val estimatedCost: Double,
    val fuelConsumption: Double,
    val co2Emissions: Double,
    val geometry: GeometryDto,
    val legs: List<RouteLegDto>,
    val alerts: List<RouteAlertDto>
)

data class GeometryDto(
    val type: String,
    val coordinates: List<List<Double>>
)

data class RouteLegDto(
    val from: String,
    val to: String,
    val distance: Double,
    val duration: Int,
    val instructions: List<String>
)

data class RouteAlertDto(
    val type: String,
    val message: String,
    val severity: String
)