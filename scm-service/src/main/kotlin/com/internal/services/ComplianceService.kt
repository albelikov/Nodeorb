package com.internal.services

import com.internal.engine.policy.PolicyEngine
import com.internal.engine.validation.MarketOracle
import com.internal.integrations.KeycloakIntegration
import com.internal.integrations.SecurityEventBus
import com.internal.repository.ComplianceRepository
import com.model.CompliancePassport
import com.model.EvaluateAccessRequest
import com.model.EvaluateAccessResponse
import com.model.ValidationVerdict
import com.model.ValidateManualInputRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Основной сервис для управления соответствием и безопасностью
 * Объединяет все компоненты SCM в единую бизнес-логику
 */
@Service
class ComplianceService(
    private val policyEngine: PolicyEngine,
    private val marketOracle: MarketOracle,
    private val keycloakIntegration: KeycloakIntegration,
    private val securityEventBus: SecurityEventBus,
    private val complianceRepository: ComplianceRepository
) {

    companion object {
        private const val DEFAULT_TRUST_SCORE = 50.0
        private const val MAX_PRICE_DEVIATION_GREEN = 0.15
        private const val MAX_PRICE_DEVIATION_YELLOW = 0.40
    }

    /**
     * Полная проверка доступа с учетом всех политик
     */
    @Transactional(readOnly = true)
    fun checkAccess(request: EvaluateAccessRequest): EvaluateAccessResponse {
        return policyEngine.evaluateAccess(request)
    }

    /**
     * Валидация ручного ввода цен с полным циклом
     */
    @Transactional
    fun validateManualInput(request: ValidateManualInputRequest): ValidationVerdict {
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

        // Отправляем события при аномалиях
        if (verdict.status == "RED" || verdict.status == "YELLOW") {
            securityEventBus.triggerPriceAnomaly(
                request.userId,
                request.orderId,
                calculateDeviation(verdict.suggestedMedian, request.materialsCost + request.laborCost),
                verdict.suggestedMedian
            )
        }

        return verdict
    }

    /**
     * Создание нового Compliance Passport
     */
    @Transactional
    fun createCompliancePassport(userId: String, entityType: String): CompliancePassport {
        val passport = CompliancePassport(
            userId = userId,
            entityType = entityType,
            trustScore = DEFAULT_TRUST_SCORE,
            complianceStatus = "PENDING",
            isBiometricsEnabled = false,
            verificationData = emptyMap(),
            expiresAt = null
        )

        // Сохраняем в репозиторий
        val entity = CompliancePassportEntity(
            userId = passport.userId,
            entityType = passport.entityType,
            trustScore = passport.trustScore,
            complianceStatus = passport.complianceStatus,
            isBiometricsEnabled = passport.isBiometricsEnabled,
            verificationData = "{}",
            expiresAt = passport.expiresAt
        )

        complianceRepository.save(entity)

        return passport
    }

    /**
     * Обновление Compliance Passport
     */
    @Transactional
    fun updateCompliancePassport(
        userId: String,
        updates: Map<String, Any>
    ): CompliancePassport {
        val existingPassport = complianceRepository.getCompliancePassport(userId)
            ?: throw RuntimeException("Compliance passport not found for user: $userId")

        val updatedData = existingPassport.verificationData.toMutableMap()
        updatedData.putAll(updates)

        val updatedPassport = existingPassport.copy(
            verificationData = updatedData,
            expiresAt = updates["expiresAt"] as? Instant ?: existingPassport.expiresAt
        )

        val entity = CompliancePassportEntity(
            userId = updatedPassport.userId,
            entityType = updatedPassport.entityType,
            trustScore = updatedPassport.trustScore,
            complianceStatus = updatedPassport.complianceStatus,
            isBiometricsEnabled = updatedPassport.isBiometricsEnabled,
            verificationData = "{}", // В реальной системе здесь будет сериализация
            expiresAt = updatedPassport.expiresAt
        )

        complianceRepository.save(entity)

        return updatedPassport
    }

    /**
     * Повышение уровня доверия
     */
    @Transactional
    fun increaseTrustScore(userId: String, increment: Double, reason: String): Double {
        keycloakIntegration.increaseTrustScore(userId, increment)
        
        // Отправляем событие о повышении доверия
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "TRUST_SCORE_INCREASED_${System.currentTimeMillis()}",
                eventType = "TRUST_SCORE_INCREASED",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "increment" to increment.toString(),
                    "reason" to reason
                )
            )
        )

        return getTrustScore(userId)
    }

    /**
     * Понижение уровня доверия
     */
    @Transactional
    fun decreaseTrustScore(userId: String, decrement: Double, reason: String): Double {
        keycloakIntegration.decreaseTrustScore(userId, decrement)
        
        // Отправляем событие о понижении доверия
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "TRUST_SCORE_DECREASED_${System.currentTimeMillis()}",
                eventType = "TRUST_SCORE_DECREASED",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "decrement" to decrement.toString(),
                    "reason" to reason
                )
            )
        )

        return getTrustScore(userId)
    }

    /**
     * Получение уровня доверия пользователя
     */
    fun getTrustScore(userId: String): Double {
        val passport = complianceRepository.getCompliancePassport(userId)
            ?: throw RuntimeException("Compliance passport not found for user: $userId")
        
        return passport.trustScore
    }

    /**
     * Блокировка пользователя
     */
    @Transactional
    fun blockUser(userId: String, reason: String) {
        keycloakIntegration.blockUser(userId, reason)
        
        // Отправляем событие о блокировке
        securityEventBus.blockUser(userId, reason, "SCM_SERVICE")
    }

    /**
     * Разблокировка пользователя
     */
    @Transactional
    fun unblockUser(userId: String) {
        keycloakIntegration.unblockUser(userId)
        
        // Отправляем событие о разблокировке
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "USER_UNBLOCKED_${System.currentTimeMillis()}",
                eventType = "USER_UNBLOCKED",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf("reason" to "Manual unblock")
            )
        )
    }

    /**
     * Проверка соответствия требованиям безопасности
     */
    fun checkCompliance(userId: String): ComplianceStatus {
        val passport = complianceRepository.getCompliancePassport(userId)
            ?: return ComplianceStatus.NOT_FOUND

        return when {
            passport.complianceStatus == "BLACKLISTED" -> ComplianceStatus.BLOCKED
            passport.complianceStatus != "VERIFIED" -> ComplianceStatus.PENDING
            passport.trustScore < 25.0 -> ComplianceStatus.LOW_TRUST
            passport.trustScore < 50.0 -> ComplianceStatus.MEDIUM_TRUST
            else -> ComplianceStatus.VERIFIED
        }
    }

    /**
     * Получение статистики по пользователю
     */
    fun getUserStats(userId: String): UserStats {
        val passport = complianceRepository.getCompliancePassport(userId)
            ?: throw RuntimeException("Compliance passport not found for user: $userId")

        // В реальной системе здесь будет агрегация данных из ClickHouse
        return UserStats(
            userId = userId,
            trustScore = passport.trustScore,
            complianceStatus = passport.complianceStatus,
            totalValidations = 100,
            successfulValidations = 85,
            failedValidations = 15,
            lastActivity = Instant.now()
        )
    }

    /**
     * Синхронизация пользователя из Keycloak
     */
    @Transactional
    fun syncUserFromKeycloak(userId: String, email: String, roles: List<String>): CompliancePassport {
        val userData = KeycloakUserData(
            userId = userId,
            email = email,
            roles = roles
        )

        keycloakIntegration.syncUserFromKeycloak(userData)

        return complianceRepository.getCompliancePassport(userId)
            ?: throw RuntimeException("Failed to create compliance passport for user: $userId")
    }

    /**
     * Проверка лицензий пользователя
     */
    fun checkLicenses(userId: String, requiredLicenses: List<String>): LicenseCheckResult {
        val passport = complianceRepository.getCompliancePassport(userId)
            ?: return LicenseCheckResult(false, emptyList())

        val missingLicenses = mutableListOf<String>()
        
        for (license in requiredLicenses) {
            if (!passport.hasLicense(license)) {
                missingLicenses.add(license)
            }
        }

        return LicenseCheckResult(
            hasAllLicenses = missingLicenses.isEmpty(),
            missingLicenses = missingLicenses
        )
    }

    // Вспомогательные методы

    private fun calculateDeviation(median: Double, actual: Double): Double {
        return if (median > 0) (actual - median) / median else 1.0
    }
}

/**
 * Модели для бизнес-логики
 */

enum class ComplianceStatus {
    NOT_FOUND,
    PENDING,
    LOW_TRUST,
    MEDIUM_TRUST,
    VERIFIED,
    BLOCKED
}

data class UserStats(
    val userId: String,
    val trustScore: Double,
    val complianceStatus: String,
    val totalValidations: Int,
    val successfulValidations: Int,
    val failedValidations: Int,
    val lastActivity: Instant
)

data class LicenseCheckResult(
    val hasAllLicenses: Boolean,
    val missingLicenses: List<String>
)