package com.nodeorb.freight.marketplace.dto

import java.util.UUID

/**
 * Событие подачи заявки на участие в аукционе
 * Используется для интеграции с Kafka и SCM системой
 */
data class BidPlacementEvent(
    val bidId: UUID,
    val carrierId: UUID,
    val masterOrderId: UUID,
    val amount: Double,
    val proposedDeliveryDate: String,
    val notes: String? = null,
    val route: RouteInfo,
    val cargoDetails: CargoDetails,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие результата проверки соответствия
 */
data class ComplianceResultEvent(
    val bidId: UUID,
    val carrierId: UUID,
    val masterOrderId: UUID,
    val complianceStatus: String,
    val complianceDetails: String? = null,
    val securityClearance: String,
    val securityDetails: String? = null,
    val riskScore: Double,
    val riskFactors: List<String> = emptyList(),
    val trustToken: String? = null,
    val auditTrail: String,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие генерации Trust Token
 */
data class TrustTokenEvent(
    val token: String,
    val carrierId: UUID,
    val masterOrderId: UUID,
    val bidId: UUID,
    val expiresAt: Long,
    val permissions: List<String>,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие ошибки проверки соответствия
 */
data class ComplianceErrorEvent(
    val bidId: UUID,
    val error: String,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие обновления статуса заявки
 */
data class BidStatusUpdateEvent(
    val bidId: UUID,
    val carrierId: UUID,
    val masterOrderId: UUID,
    val status: String,
    val reason: String? = null,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие создания SCM снимка
 */
data class ScmSnapshotEvent(
    val snapshotId: UUID,
    val bidId: UUID,
    val carrierId: UUID,
    val masterOrderId: UUID,
    val snapshotDate: String,
    val complianceStatus: String,
    val securityClearance: String,
    val riskScore: Double,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие проверки маршрута
 */
data class RouteCheckEvent(
    val routeId: UUID,
    val pickupLocation: LocationInfo,
    val deliveryLocation: LocationInfo,
    val waypoints: List<LocationInfo> = emptyList(),
    val riskLevel: String,
    val securityRecommendations: List<String> = emptyList(),
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие проверки груза
 */
data class CargoCheckEvent(
    val cargoId: UUID,
    val cargoType: String,
    val weight: Double,
    val volume: Double,
    val hazardous: Boolean,
    val temperatureControlled: Boolean,
    val value: Double? = null,
    val securityRequirements: List<String> = emptyList(),
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие аудита безопасности
 */
data class SecurityAuditEvent(
    val auditId: UUID,
    val carrierId: UUID,
    val masterOrderId: UUID,
    val securityLevel: String,
    val auditDetails: String,
    val recommendations: List<String> = emptyList(),
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие рисковой оценки
 */
data class RiskAssessmentEvent(
    val assessmentId: UUID,
    val carrierId: UUID,
    val masterOrderId: UUID,
    val riskScore: Double,
    val riskFactors: List<String>,
    val riskLevel: String,
    val mitigationMeasures: List<String> = emptyList(),
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие обновления профиля перевозчика
 */
data class CarrierProfileUpdateEvent(
    val carrierId: UUID,
    val companyName: String,
    val rating: Double,
    val totalOrders: Int,
    val completedOrders: Int,
    val complianceStatus: String,
    val securityClearance: String,
    val lastUpdated: String = java.time.LocalDateTime.now().toString(),
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

/**
 * Событие изменения статуса мастер-заказа
 */
data class MasterOrderStatusEvent(
    val masterOrderId: UUID,
    val shipperId: UUID,
    val status: String,
    val reason: String? = null,
    val partialOrdersCount: Int = 0,
    val filledPercentage: Double = 0.0,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)