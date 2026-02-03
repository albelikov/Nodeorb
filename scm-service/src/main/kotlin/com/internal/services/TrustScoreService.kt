package com.internal.services

import com.internal.integrations.KeycloakIntegration
import com.internal.integrations.SecurityEventBus
import com.internal.repository.ComplianceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Сервис динамического расчета Trust Score 2.0
 * Анализирует историю пользователя для определения уровня доверия
 * Реализует алгоритм динамического пересчета рейтинга доверия пользователя
 * на основе его поведения (точность ввода цен, попытки доступа вне геозон, история комплаенс-проверок)
 */
@Service
class TrustScoreService(
    private val complianceRepository: ComplianceRepository,
    private val keycloakIntegration: KeycloakIntegration,
    private val securityEventBus: SecurityEventBus
) {

    companion object {
        private const val BASE_TRUST_SCORE = 50.0
        private const val MAX_TRUST_SCORE = 100.0
        private const val MIN_TRUST_SCORE = 0.0
        
        // Коэффициенты для различных факторов
        private const val PRICE_ACCURACY_WEIGHT = 0.3
        private const val APPEAL_SUCCESS_WEIGHT = 0.25
        private const val BIOMETRICS_COMPLIANCE_WEIGHT = 0.2
        private const val GEOGRAPHIC_COMPLIANCE_WEIGHT = 0.15
        private const val TIME_FACTOR_WEIGHT = 0.1
        
        // Пороги для различных действий
        private const val LOW_TRUST_THRESHOLD = 25.0
        private const val MEDIUM_TRUST_THRESHOLD = 50.0
        private const val HIGH_TRUST_THRESHOLD = 75.0
    }

    /**
     * Расчет динамического Trust Score для пользователя
     */
    @Transactional(readOnly = true)
    fun calculateTrustScore(userId: String): Double {
        val history = getUserHistory(userId)
        
        // Рассчитываем компоненты Trust Score
        val priceAccuracyScore = calculatePriceAccuracyScore(history)
        val appealSuccessScore = calculateAppealSuccessScore(history)
        val biometricsComplianceScore = calculateBiometricsComplianceScore(history)
        val geographicComplianceScore = calculateGeographicComplianceScore(history)
        val timeFactorScore = calculateTimeFactorScore(history)

        // Взвешенное среднее
        val trustScore = (
            priceAccuracyScore * PRICE_ACCURACY_WEIGHT +
            appealSuccessScore * APPEAL_SUCCESS_WEIGHT +
            biometricsComplianceScore * BIOMETRICS_COMPLIANCE_WEIGHT +
            geographicComplianceScore * GEOGRAPHIC_COMPLIANCE_WEIGHT +
            timeFactorScore * TIME_FACTOR_WEIGHT
        )

        return trustScore.coerceIn(MIN_TRUST_SCORE, MAX_TRUST_SCORE)
    }

    /**
     * Обновление Trust Score пользователя
     */
    @Transactional
    fun updateTrustScore(userId: String): Double {
        val newTrustScore = calculateTrustScore(userId)
        
        // Сохраняем новый Trust Score
        keycloakIntegration.updateTrustScore(userId, newTrustScore)
        
        // Отправляем событие об обновлении Trust Score
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "TRUST_SCORE_UPDATED_${System.currentTimeMillis()}",
                eventType = "TRUST_SCORE_UPDATED",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "new_trust_score" to newTrustScore.toString(),
                    "trust_level" to getTrustLevel(newTrustScore)
                )
            )
        )

        return newTrustScore
    }

    /**
     * Получение уровня доверия по Trust Score
     */
    fun getTrustLevel(trustScore: Double): String {
        return when {
            trustScore < LOW_TRUST_THRESHOLD -> "CRITICAL"
            trustScore < MEDIUM_TRUST_THRESHOLD -> "LOW"
            trustScore < HIGH_TRUST_THRESHOLD -> "MEDIUM"
            else -> "HIGH"
        }
    }

    /**
     * Понижение Trust Score за нарушения
     */
    @Transactional
    fun penalizeUser(userId: String, penaltyPoints: Double, reason: String) {
        val currentScore = getTrustScore(userId)
        val newScore = (currentScore - penaltyPoints).coerceAtLeast(MIN_TRUST_SCORE)
        
        keycloakIntegration.updateTrustScore(userId, newScore)
        
        // Отправляем событие о понижении
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "TRUST_SCORE_PENALIZED_${System.currentTimeMillis()}",
                eventType = "TRUST_SCORE_PENALIZED",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "penalty_points" to penaltyPoints.toString(),
                    "reason" to reason,
                    "new_trust_score" to newScore.toString()
                )
            )
        )
    }

    /**
     * Поощрение пользователя за хорошее поведение
     */
    @Transactional
    fun rewardUser(userId: String, rewardPoints: Double, reason: String) {
        val currentScore = getTrustScore(userId)
        val newScore = (currentScore + rewardPoints).coerceAtMost(MAX_TRUST_SCORE)
        
        keycloakIntegration.updateTrustScore(userId, newScore)
        
        // Отправляем событие о поощрении
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "TRUST_SCORE_REWARDED_${System.currentTimeMillis()}",
                eventType = "TRUST_SCORE_REWARDED",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "reward_points" to rewardPoints.toString(),
                    "reason" to reason,
                    "new_trust_score" to newScore.toString()
                )
            )
        )
    }

    /**
     * Получение текущего Trust Score пользователя
     */
    fun getTrustScore(userId: String): Double {
        val passport = complianceRepository.getCompliancePassport(userId)
            ?: return BASE_TRUST_SCORE
        
        return passport.trustScore
    }

    /**
     * Получение истории пользователя для анализа
     */
    private fun getUserHistory(userId: String): UserHistory {
        val validations = complianceRepository.getValidationsByUserId(userId)
        val accessChecks = complianceRepository.getAccessChecksByUserId(userId)
        val geofenceChecks = complianceRepository.getGeofenceChecksByUserId(userId)
        val appeals = complianceRepository.getAppealsByUserId(userId)

        return UserHistory(
            userId = userId,
            validations = validations,
            accessChecks = accessChecks,
            geofenceChecks = geofenceChecks,
            appeals = appeals
        )
    }

    /**
     * Расчет оценки точности цен
     */
    private fun calculatePriceAccuracyScore(history: UserHistory): Double {
        if (history.validations.isEmpty()) return BASE_TRUST_SCORE

        val accurateValidations = history.validations.count { validation ->
            validation.riskVerdict == "GREEN"
        }

        val accuracyRate = accurateValidations.toDouble() / history.validations.size
        return (accuracyRate * 100.0).coerceIn(MIN_TRUST_SCORE, MAX_TRUST_SCORE)
    }

    /**
     * Расчет оценки успешности апелляций
     */
    private fun calculateAppealSuccessScore(history: UserHistory): Double {
        if (history.appeals.isEmpty()) return BASE_TRUST_SCORE

        val successfulAppeals = history.appeals.count { appeal ->
            appeal.status == "APPROVED"
        }

        val successRate = successfulAppeals.toDouble() / history.appeals.size
        return (successRate * 100.0).coerceIn(MIN_TRUST_SCORE, MAX_TRUST_SCORE)
    }

    /**
     * Расчет оценки соблюдения биометрических требований
     */
    private fun calculateBiometricsComplianceScore(history: UserHistory): Double {
        val biometricChecks = history.accessChecks.filter { check ->
            check.requiresBiometrics == true
        }

        if (biometricChecks.isEmpty()) return BASE_TRUST_SCORE

        val compliantChecks = biometricChecks.count { check ->
            check.accessGranted == true
        }

        val complianceRate = compliantChecks.toDouble() / biometricChecks.size
        return (complianceRate * 100.0).coerceIn(MIN_TRUST_SCORE, MAX_TRUST_SCORE)
    }

    /**
     * Расчет оценки географического соответствия
     */
    private fun calculateGeographicComplianceScore(history: UserHistory): Double {
        if (history.geofenceChecks.isEmpty()) return BASE_TRUST_SCORE

        val compliantChecks = history.geofenceChecks.count { check ->
            check.isInside == true && check.violationReason == null
        }

        val complianceRate = compliantChecks.toDouble() / history.geofenceChecks.size
        return (complianceRate * 100.0).coerceIn(MIN_TRUST_SCORE, MAX_TRUST_SCORE)
    }

    /**
     * Расчет временного фактора (чем дольше пользователь активен, тем выше доверие)
     */
    private fun calculateTimeFactorScore(history: UserHistory): Double {
        val now = Instant.now()
        val oldestEvent = listOf(
            history.validations.minByOrNull { it.createdAt },
            history.accessChecks.minByOrNull { it.timestamp },
            history.geofenceChecks.minByOrNull { it.timestamp }
        ).filterNotNull().minByOrNull { it.createdAt }

        if (oldestEvent == null) return BASE_TRUST_SCORE

        val daysActive = java.time.Duration.between(oldestEvent.createdAt, now).toDays()
        
        // За каждый месяц активности +1 балл, максимум 50 баллов
        val timeScore = (daysActive / 30.0).coerceAtMost(50.0)
        return timeScore
    }

    /**
     * Получение рекомендаций по улучшению Trust Score
     */
    fun getTrustScoreRecommendations(userId: String): List<String> {
        val history = getUserHistory(userId)
        val recommendations = mutableListOf<String>()

        // Проверка точности цен
        val priceAccuracy = calculatePriceAccuracyScore(history)
        if (priceAccuracy < 80.0) {
            recommendations.add("Улучшайте точность вводимых цен - избегайте отклонений более 15% от рыночной медианы")
        }

        // Проверка апелляций
        val appealSuccess = calculateAppealSuccessScore(history)
        if (appealSuccess < 80.0) {
            recommendations.add("Повышайте качество апелляций - предоставляйте более убедительные доказательства")
        }

        // Проверка биометрического соответствия
        val biometricCompliance = calculateBiometricsComplianceScore(history)
        if (biometricCompliance < 80.0) {
            recommendations.add("Соблюдайте требования биометрической аутентификации")
        }

        // Проверка географического соответствия
        val geoCompliance = calculateGeographicComplianceScore(history)
        if (geoCompliance < 80.0) {
            recommendations.add("Соблюдайте географические ограничения и не выходите за пределы разрешенных зон")
        }

        return recommendations
    }

    /**
     * Проверка необходимости дополнительных мер безопасности
     */
    fun getSecurityRequirements(userId: String): SecurityRequirements {
        val trustScore = getTrustScore(userId)
        val trustLevel = getTrustLevel(trustScore)

        return SecurityRequirements(
            trustScore = trustScore,
            trustLevel = trustLevel,
            requiresBiometrics = trustScore < HIGH_TRUST_THRESHOLD,
            requiresAppeal = trustScore < MEDIUM_TRUST_THRESHOLD,
            requiresManualReview = trustScore < LOW_TRUST_THRESHOLD,
            restrictedActions = getRestrictedActions(trustLevel)
        )
    }

    /**
     * Получение списка ограниченных действий для уровня доверия
     */
    private fun getRestrictedActions(trustLevel: String): List<String> {
        return when (trustLevel) {
            "CRITICAL" -> listOf(
                "place_bid",
                "view_itar_cargo",
                "access_sensitive_data",
                "modify_order"
            )
            "LOW" -> listOf(
                "view_itar_cargo",
                "access_sensitive_data"
            )
            "MEDIUM" -> listOf(
                "access_sensitive_data"
            )
            else -> emptyList()
        }
    }

    /**
     * Динамическое обновление Trust Score на основе нового события
     */
    @Transactional
    fun updateTrustScoreForEvent(userId: String, eventType: String, eventData: Map<String, Any>): Double {
        // Получаем текущий Trust Score
        val currentScore = getTrustScore(userId)
        
        // Рассчитываем корректировку на основе события
        val adjustment = calculateEventAdjustment(eventType, eventData)
        
        // Обновляем Trust Score
        val newScore = (currentScore + adjustment).coerceIn(MIN_TRUST_SCORE, MAX_TRUST_SCORE)
        
        // Сохраняем обновленный Trust Score
        keycloakIntegration.updateTrustScore(userId, newScore)
        
        // Отправляем событие об обновлении
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "TRUST_SCORE_ADJUSTED_${System.currentTimeMillis()}",
                eventType = "TRUST_SCORE_ADJUSTED",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "event_type" to eventType,
                    "adjustment" to adjustment.toString(),
                    "old_trust_score" to currentScore.toString(),
                    "new_trust_score" to newScore.toString(),
                    "trust_level" to getTrustLevel(newScore)
                )
            )
        )

        return newScore
    }

    /**
     * Расчет корректировки Trust Score на основе события
     */
    private fun calculateEventAdjustment(eventType: String, eventData: Map<String, Any>): Double {
        return when (eventType) {
            "MANUAL_COST_VALIDATION" -> {
                val status = eventData["status"] as? String ?: "UNKNOWN"
                when (status) {
                    "APPROVED" -> 2.0  // Поощрение за точность
                    "AUDIT_REQUIRED" -> -1.0  // Небольшое понижение
                    "REJECTED" -> -5.0  // Сильное понижение
                    else -> 0.0
                }
            }
            "GEOFENCING_VIOLATION" -> {
                -3.0  // Понижение за нарушение геозон
            }
            "SANCTION_CHECK_FAILED" -> {
                -10.0  // Сильное понижение за попытку работы с заблокированными контрагентами
            }
            "BIOMETRICS_FAILED" -> {
                -2.0  // Небольшое понижение за неудачную биометрическую проверку
            }
            else -> 0.0
        }
    }
}

/**
 * Модели для Trust Score
 */

data class UserHistory(
    val userId: String,
    val validations: List<ManualEntryValidation>,
    val accessChecks: List<AccessCheck>,
    val geofenceChecks: List<GeofenceCheck>,
    val appeals: List<Appeal>
)

data class SecurityRequirements(
    val trustScore: Double,
    val trustLevel: String,
    val requiresBiometrics: Boolean,
    val requiresAppeal: Boolean,
    val requiresManualReview: Boolean,
    val restrictedActions: List<String>
)

data class Appeal(
    val id: String,
    val validationId: String,
    val status: String, // PENDING, APPROVED, REJECTED
    val justification: String,
    val createdAt: Instant
)

data class ManualEntryValidation(
    val id: String? = null,
    val userId: String,
    val orderId: String,
    val materialsCost: Double,
    val laborCost: Double,
    val currency: String,
    val riskVerdict: String,
    val aiConfidenceScore: Double,
    val requiresAppeal: Boolean,
    val appealStatus: String,
    val createdAt: Instant
)

data class AccessCheck(
    val id: String,
    val userId: String,
    val orderId: String,
    val accessGranted: Boolean,
    val timestamp: Instant,
    val reason: String? = null
)

data class GeofenceCheck(
    val id: String,
    val userId: String,
    val orderId: String,
    val isInside: Boolean,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val violationReason: String? = null
)

data class SecurityEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: String,
    val sourceService: String,
    val details: Map<String, String>
) <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt
scm-service/src/main/kotlin/com/internal/services/DynamicGeofencingService.kt
scm-service/src/main/kotlin/com/internal/services/ComplianceOraclesService.kt
scm-service/src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt
scm-service/src/main/kotlin/com/internal/services/TrustScoreService.kt

# Current Time
2/3/2026, 11:30:10 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
110,835 / 256K tokens used (43%)

# Current Mode
ACT MODE
</environment_details>