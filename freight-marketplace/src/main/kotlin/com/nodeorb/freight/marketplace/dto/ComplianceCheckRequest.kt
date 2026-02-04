package com.nodeorb.freight.marketplace.dto

import java.util.UUID

/**
 * Запрос на проверку соответствия перевозчика
 */
data class ComplianceCheckRequest(
    val carrierId: UUID,
    val masterOrderId: UUID,
    val bidId: UUID,
    val cargoType: String,
    val route: RouteInfo,
    val cargoDetails: CargoDetails
)

/**
 * Информация о маршруте для проверки соответствия
 */
data class RouteInfo(
    val pickupLocation: LocationInfo,
    val deliveryLocation: LocationInfo,
    val waypoints: List<LocationInfo> = emptyList()
)

/**
 * Информация о географической точке
 */
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val region: String? = null,
    val city: String? = null
)

/**
 * Детали груза для проверки соответствия
 */
data class CargoDetails(
    val cargoType: String,
    val weight: Double,
    val volume: Double,
    val hazardous: Boolean = false,
    val temperatureControlled: Boolean = false,
    val value: Double? = null
)

/**
 * Результат проверки соответствия
 */
data class ComplianceCheckResult(
    val carrierId: UUID,
    val masterOrderId: UUID,
    val bidId: UUID,
    val complianceStatus: String,
    val complianceDetails: String? = null,
    val securityClearance: String,
    val securityDetails: String? = null,
    val riskScore: Double,
    val riskFactors: List<String> = emptyList(),
    val trustToken: String? = null,
    val auditTrail: String
)

/**
 * Информация о доверенном токене
 */
data class TrustTokenInfo(
    val token: String,
    val carrierId: UUID,
    val expiresAt: Long,
    val permissions: List<String>,
    val metadata: Map<String, String> = emptyMap()
)