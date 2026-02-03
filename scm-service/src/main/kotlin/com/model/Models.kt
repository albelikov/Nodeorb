package com.model

import com.internal.engine.policy.RiskLevel

/**
 * Модели данных для SCM сервиса
 */

/**
 * Запрос на оценку доступа
 */
data class EvaluateAccessRequest(
    val userId: String,
    val serviceId: String,
    val action: String,
    val context: Map<String, String>
)

/**
 * Ответ на оценку доступа
 */
data class EvaluateAccessResponse(
    val allowed: Boolean,
    val decisionId: String,
    val reason: String,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val requiresBiometrics: Boolean = false,
    val requiresAppeal: Boolean = false
)

/**
 * Запрос на валидацию ручного ввода
 */
data class ValidateManualInputRequest(
    val userId: String,
    val orderId: String,
    val serviceSource: String? = null,
    val materialsCost: Double,
    val laborCost: Double,
    val currency: String = "USD"
)

/**
 * Ответ валидации ручного ввода
 */
data class ValidateManualInputResponse(
    val status: String,
    val riskScore: Double,
    val requiresAppeal: Boolean,
    val requiresBiometrics: Boolean,
    val suggestedMedian: Double,
    val comment: String
)

/**
 * Результат валидации цен (используется в Market Oracle)
 */
data class ValidationVerdict(
    val status: String,           // GREEN, YELLOW, RED
    val riskScore: Double,        // 0.0 - 1.0
    val requiresAppeal: Boolean,
    val requiresBiometrics: Boolean,
    val suggestedMedian: Double,
    val comment: String
)

/**
 * Запрос на синхронизацию пользователя из Keycloak
 */
data class KeycloakSyncRequest(
    val userId: String,
    val email: String,
    val roles: List<String>,
    val attributes: Map<String, List<String>> = emptyMap()
)

/**
 * Запрос на изменение уровня доверия
 */
data class TrustScoreRequest(
    val increment: Double
)

/**
 * Запрос на блокировку пользователя
 */
data class BlockUserRequest(
    val reason: String
)

/**
 * Статистика по валидации цен
 */
data class PriceValidationStats(
    val totalValidations: Int,
    val greenVerdicts: Int,
    val yellowVerdicts: Int,
    val redVerdicts: Int,
    val averageDeviation: Double,
    val fraudAttempts: Int
)

/**
 * Статистика по безопасности
 */
data class SecurityStats(
    val totalEvents: Int,
    val accessDenied: Int,
    val priceAnomalies: Int,
    val geofenceViolations: Int,
    val biometricFailures: Int,
    val blockedUsers: Int
)

/**
 * Событие безопасности
 */
data class SecurityEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: java.time.Instant,
    val userId: String,
    val sourceService: String,
    val details: Map<String, String>
)

/**
 * Аудит-лог
 */
data class AuditLog(
    val logId: String,
    val eventType: String,
    val timestamp: java.time.Instant,
    val userId: String,
    val serviceId: String,
    val action: String,
    val resource: String,
    val result: String,
    val details: Map<String, Any>
)

/**
 * Данные пользователя из Keycloak
 */
data class KeycloakUserData(
    val userId: String,
    val email: String,
    val roles: List<String>,
    val attributes: Map<String, List<String>> = emptyMap()
)

/**
 * Compliance Passport (основная сущность)
 */
data class CompliancePassport(
    val userId: String,
    val entityType: String,
    val trustScore: Double,
    val complianceStatus: String,
    val isBiometricsEnabled: Boolean,
    val verificationData: Map<String, Any>,
    val expiresAt: java.time.Instant?
) {
    fun hasLicense(licenseType: String): Boolean {
        return verificationData["licenses"]?.let { licenses ->
            @Suppress("UNCHECKED_CAST")
            (licenses as? List<Map<String, Any>>)?.any { 
                it["type"] == licenseType && it["valid"] == true 
            } ?: false
        } ?: false
    }

    fun isCountryAllowed(country: String): Boolean {
        val allowedCountries = verificationData["allowed_countries"] as? List<String>
        return allowedCountries?.contains(country) ?: true
    }

    fun isLocationAllowed(location: String): Boolean {
        val allowedLocations = verificationData["allowed_locations"] as? List<String>
        return allowedLocations?.contains(location) ?: true
    }

    fun hasSecurityLevel(level: String): Boolean {
        val securityLevels = verificationData["security_levels"] as? List<String>
        return securityLevels?.contains(level) ?: false
    }
}