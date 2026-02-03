package com.internal.engine.policy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.internal.engine.validation.MarketOracle
import com.internal.repository.ComplianceRepository
import com.model.EvaluateAccessRequest
import com.model.EvaluateAccessResponse
import com.model.RiskLevel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Движок политик для оценки доступа на основе ABAC (Attribute-Based Access Control)
 * Интегрируется с OPA (Open Policy Agent) для сложных правил
 */
@Service
class PolicyEngine(
    private val complianceRepository: ComplianceRepository,
    private val marketOracle: MarketOracle,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val MAX_PRICE_DEVIATION_GREEN = 0.15
        private const val MAX_PRICE_DEVIATION_YELLOW = 0.40
    }

    /**
     * Оценка доступа на основе контекста и политик
     */
    @Transactional(readOnly = true)
    fun evaluateAccess(request: EvaluateAccessRequest): EvaluateAccessResponse {
        val passport = complianceRepository.getCompliancePassport(request.userId)
        
        // Проверка базовых требований
        val basicCheck = checkBasicRequirements(request, passport)
        if (!basicCheck.allowed) {
            return basicCheck
        }

        // Проверка специфических действий
        return when (request.action) {
            "place_bid" -> evaluateBidPlacement(request, passport)
            "view_itar_cargo" -> evaluateITARCargoAccess(request, passport)
            "access_sensitive_data" -> evaluateSensitiveDataAccess(request, passport)
            else -> createDefaultResponse(true, "Action allowed by default policy")
        }
    }

    /**
     * Проверка базовых требований (роль, статус, доверие)
     */
    private fun checkBasicRequirements(
        request: EvaluateAccessRequest,
        passport: CompliancePassport?
    ): EvaluateAccessResponse {
        // Проверка существования паспорта
        if (passport == null) {
            return createDefaultResponse(false, "User compliance passport not found")
        }

        // Проверка статуса соответствия
        if (passport.complianceStatus != "VERIFIED") {
            return createDefaultResponse(
                false, 
                "User compliance status: ${passport.complianceStatus}"
            )
        }

        // Проверка срока действия
        if (passport.expiresAt != null && passport.expiresAt < Instant.now()) {
            return createDefaultResponse(false, "Compliance passport expired")
        }

        // Проверка уровня доверия
        if (passport.trustScore < 50.0) {
            return createDefaultResponse(
                false,
                "Trust score too low: ${passport.trustScore}"
            )
        }

        return createDefaultResponse(true, "Basic requirements satisfied")
    }

    /**
     * Оценка размещения ставки (включает валидацию цен)
     */
    private fun evaluateBidPlacement(
        request: EvaluateAccessRequest,
        passport: CompliancePassport
    ): EvaluateAccessResponse {
        // Проверка лицензии на перевозку
        val cargoType = request.context["cargo_type"] ?: "GENERAL"
        if (cargoType == "ADR" && !passport.hasLicense("ADR")) {
            return createDefaultResponse(false, "ADR license required for hazardous cargo")
        }

        // Проверка ручного ввода цен (если есть)
        val materialsCost = request.context["materials_cost"]?.toDoubleOrNull()
        val laborCost = request.context["labor_cost"]?.toDoubleOrNull()
        
        if (materialsCost != null && laborCost != null) {
            val validation = marketOracle.validateManualInput(
                request.userId,
                request.context["order_id"] ?: "",
                materialsCost,
                laborCost,
                request.context["currency"] ?: "USD"
            )

            return when (validation.status) {
                "GREEN" -> createDefaultResponse(true, "Price validation passed")
                "YELLOW" -> createDefaultResponse(
                    false,
                    "Price deviation detected, requires appeal",
                    requiresAppeal = true
                )
                "RED" -> createDefaultResponse(
                    false,
                    "Significant price deviation, requires biometrics",
                    requiresBiometrics = true
                )
                else -> createDefaultResponse(false, "Unknown validation status")
            }
        }

        return createDefaultResponse(true, "Bid placement allowed")
    }

    /**
     * Оценка доступа к ITAR/военным грузам
     */
    private fun evaluateITARCargoAccess(
        request: EvaluateAccessRequest,
        passport: CompliancePassport
    ): EvaluateAccessResponse {
        // Проверка специальных лицензий
        if (!passport.hasLicense("ITAR") && !passport.hasLicense("EAR")) {
            return createDefaultResponse(false, "ITAR/EAR license required")
        }

        // Проверка геолокации (если указана)
        val userCountry = request.context["user_country"]
        if (userCountry != null && !passport.isCountryAllowed(userCountry)) {
            return createDefaultResponse(
                false,
                "User country not allowed for ITAR cargo: $userCountry"
            )
        }

        // Требуется биометрическая проверка для высокорисковых грузов
        return createDefaultResponse(
            true,
            "ITAR access allowed",
            requiresBiometrics = true
        )
    }

    /**
     * Оценка доступа к чувствительным данным
     */
    private fun evaluateSensitiveDataAccess(
        request: EvaluateAccessRequest,
        passport: CompliancePassport
    ): EvaluateAccessResponse {
        // Проверка уровня допуска
        val requiredLevel = request.context["required_security_level"] ?: "CONFIDENTIAL"
        if (!passport.hasSecurityLevel(requiredLevel)) {
            return createDefaultResponse(
                false,
                "Insufficient security level. Required: $requiredLevel"
            )
        }

        // Проверка геолокации
        val userLocation = request.context["user_location"]
        if (userLocation != null && !passport.isLocationAllowed(userLocation)) {
            return createDefaultResponse(
                false,
                "Access denied from location: $userLocation"
            )
        }

        return createDefaultResponse(true, "Sensitive data access allowed")
    }

    /**
     * Создание стандартного ответа
     */
    private fun createDefaultResponse(
        allowed: Boolean,
        reason: String,
        riskLevel: RiskLevel = RiskLevel.LOW,
        requiresBiometrics: Boolean = false,
        requiresAppeal: Boolean = false
    ): EvaluateAccessResponse {
        return EvaluateAccessResponse(
            allowed = allowed,
            decisionId = generateDecisionId(),
            reason = reason,
            riskLevel = riskLevel,
            requiresBiometrics = requiresBiometrics,
            requiresAppeal = requiresAppeal
        )
    }

    /**
     * Генерация уникального ID решения для аудита
     */
    private fun generateDecisionId(): String {
        return "DEC-${System.currentTimeMillis()}-${kotlin.random.Random.nextInt(1000, 9999)}"
    }
}

/**
 * Модель Compliance Passport
 */
data class CompliancePassport(
    val userId: String,
    val entityType: String,
    val trustScore: Double,
    val complianceStatus: String,
    val isBiometricsEnabled: Boolean,
    val verificationData: Map<String, Any>,
    val expiresAt: Instant?
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