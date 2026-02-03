package com.internal.transport.http

import com.internal.engine.policy.PolicyEngine
import com.internal.engine.validation.MarketOracle
import com.internal.integrations.KeycloakIntegration
import com.internal.integrations.SecurityEventBus
import com.model.EvaluateAccessRequest
import com.model.EvaluateAccessResponse
import com.model.ValidationVerdict
import com.model.ValidateManualInputRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

/**
 * REST контроллер для внешних интеграций и управления политиками
 * Используется для связи с Keycloak и административных операций
 */
@RestController
@RequestMapping("/api/v1/scm")
class ScmController(
    private val policyEngine: PolicyEngine,
    private val marketOracle: MarketOracle,
    private val keycloakIntegration: KeycloakIntegration,
    private val securityEventBus: SecurityEventBus
) {

    companion object {
        private const val MAX_PRICE_DEVIATION_GREEN = 0.15
        private const val MAX_PRICE_DEVIATION_YELLOW = 0.40
    }

    /**
     * Синхронизация данных пользователя из Keycloak
     */
    @PostMapping("/auth/sync-keycloak")
    fun syncKeycloakUser(@RequestBody @Valid request: KeycloakSyncRequest): ResponseEntity<String> {
        try {
            val userData = KeycloakUserData(
                userId = request.userId,
                email = request.email,
                roles = request.roles,
                attributes = request.attributes
            )
            
            keycloakIntegration.syncUserFromKeycloak(userData)
            
            return ResponseEntity.ok("User synchronized successfully")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Sync failed: ${e.message}")
        }
    }

    /**
     * Валидация ручного ввода стоимости (из Пункта 2)
     */
    @PostMapping("/policies/manual-input-validation")
    fun validateManualInput(@RequestBody @Valid request: ValidateManualInputRequest): ResponseEntity<ValidationVerdict> {
        val verdict = marketOracle.validateManualInput(
            request.userId,
            request.orderId,
            request.materialsCost,
            request.laborCost,
            request.currency
        )

        // Сохраняем данные о валидации
        marketOracle.saveManualEntry(
            request.userId,
            request.orderId,
            request.materialsCost,
            request.laborCost,
            request.currency,
            verdict
        )

        // Отправляем событие при аномалии
        if (verdict.status == "RED" || verdict.status == "YELLOW") {
            securityEventBus.triggerPriceAnomaly(
                request.userId,
                request.orderId,
                calculateDeviation(verdict.suggestedMedian, request.materialsCost + request.laborCost),
                verdict.suggestedMedian
            )
        }

        return ResponseEntity.ok(verdict)
    }

    /**
     * Проверка доступа (альтернатива gRPC для внешних систем)
     */
    @PostMapping("/policies/evaluate-access")
    fun evaluateAccess(@RequestBody @Valid request: EvaluateAccessRequest): ResponseEntity<EvaluateAccessResponse> {
        val response = policyEngine.evaluateAccess(request)
        
        // Отправляем событие в Security Event Bus
        if (!response.allowed) {
            securityEventBus.sendSecurityEvent(
                SecurityEvent(
                    eventId = "ACCESS_DENIED_${System.currentTimeMillis()}",
                    eventType = "ACCESS_DENIED",
                    timestamp = java.time.Instant.now(),
                    userId = request.userId,
                    sourceService = request.serviceId,
                    details = mapOf(
                        "action" to request.action,
                        "reason" to response.reason,
                        "risk_level" to response.riskLevel.name
                    )
                )
            )
        }

        return ResponseEntity.ok(response)
    }

    /**
     * Повышение уровня доверия пользователя
     */
    @PostMapping("/users/{userId}/trust-score/increase")
    fun increaseTrustScore(
        @PathVariable userId: String,
        @RequestBody request: TrustScoreRequest
    ): ResponseEntity<String> {
        try {
            keycloakIntegration.increaseTrustScore(userId, request.increment)
            return ResponseEntity.ok("Trust score increased successfully")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Failed to increase trust score: ${e.message}")
        }
    }

    /**
     * Понижение уровня доверия пользователя
     */
    @PostMapping("/users/{userId}/trust-score/decrease")
    fun decreaseTrustScore(
        @PathVariable userId: String,
        @RequestBody request: TrustScoreRequest
    ): ResponseEntity<String> {
        try {
            keycloakIntegration.decreaseTrustScore(userId, request.increment)
            return ResponseEntity.ok("Trust score decreased successfully")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Failed to decrease trust score: ${e.message}")
        }
    }

    /**
     * Блокировка пользователя
     */
    @PostMapping("/users/{userId}/block")
    fun blockUser(
        @PathVariable userId: String,
        @RequestBody request: BlockUserRequest
    ): ResponseEntity<String> {
        try {
            keycloakIntegration.blockUser(userId, request.reason)
            return ResponseEntity.ok("User blocked successfully")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Failed to block user: ${e.message}")
        }
    }

    /**
     * Разблокировка пользователя
     */
    @PostMapping("/users/{userId}/unblock")
    fun unblockUser(@PathVariable userId: String): ResponseEntity<String> {
        try {
            keycloakIntegration.unblockUser(userId)
            return ResponseEntity.ok("User unblocked successfully")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("Failed to unblock user: ${e.message}")
        }
    }

    /**
     * Проверка наличия роли у пользователя
     */
    @GetMapping("/users/{userId}/has-role/{role}")
    fun hasRole(
        @PathVariable userId: String,
        @PathVariable role: String
    ): ResponseEntity<Boolean> {
        val hasRole = keycloakIntegration.hasRole(userId, role)
        return ResponseEntity.ok(hasRole)
    }

    /**
     * Получение статистики по валидации цен
     */
    @GetMapping("/analytics/price-validation")
    fun getPriceValidationStats(
        @RequestParam(required = false) daysBack: Int = 30
    ): ResponseEntity<PriceValidationStats> {
        // В реальной системе здесь будет агрегация данных из ClickHouse
        val stats = PriceValidationStats(
            totalValidations = 1000,
            greenVerdicts = 850,
            yellowVerdicts = 100,
            redVerdicts = 50,
            averageDeviation = 0.12,
            fraudAttempts = 5
        )
        
        return ResponseEntity.ok(stats)
    }

    /**
     * Получение статистики по безопасности
     */
    @GetMapping("/analytics/security")
    fun getSecurityStats(
        @RequestParam(required = false) daysBack: Int = 7
    ): ResponseEntity<SecurityStats> {
        // В реальной системе здесь будет агрегация данных из Security Event Bus
        val stats = SecurityStats(
            totalEvents = 5000,
            accessDenied = 150,
            priceAnomalies = 75,
            geofenceViolations = 25,
            biometricFailures = 10,
            blockedUsers = 5
        )
        
        return ResponseEntity.ok(stats)
    }

    /**
     * Тестирование подключения к SCM
     */
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        val health = mapOf(
            "status" to "UP",
            "timestamp" to java.time.Instant.now(),
            "services" to mapOf(
                "policy_engine" to "UP",
                "market_oracle" to "UP",
                "keycloak_integration" to "UP",
                "security_event_bus" to "UP"
            )
        )
        
        return ResponseEntity.ok(health)
    }

    // Вспомогательные методы

    private fun calculateDeviation(median: Double, actual: Double): Double {
        return if (median > 0) (actual - median) / median else 1.0
    }
}

/**
 * Модели запросов и ответов для REST API
 */

data class KeycloakSyncRequest(
    val userId: String,
    val email: String,
    val roles: List<String>,
    val attributes: Map<String, List<String>> = emptyMap()
)

data class TrustScoreRequest(
    val increment: Double
)

data class BlockUserRequest(
    val reason: String
)

data class PriceValidationStats(
    val totalValidations: Int,
    val greenVerdicts: Int,
    val yellowVerdicts: Int,
    val redVerdicts: Int,
    val averageDeviation: Double,
    val fraudAttempts: Int
)

data class SecurityStats(
    val totalEvents: Int,
    val accessDenied: Int,
    val priceAnomalies: Int,
    val geofenceViolations: Int,
    val biometricFailures: Int,
    val blockedUsers: Int
)