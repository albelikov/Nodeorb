package com.nodeorb.scmclient.interceptor

import com.nodeorb.scmclient.exception.BiometricException
import com.nodeorb.scmclient.exception.ScmSecurityBlockException
import com.nodeorb.scmclient.model.*
import com.nodeorb.scmclient.model.BiometricAuthType
import com.nodeorb.scmclient.model.BiometricChallenge
import com.nodeorb.scmclient.model.BiometricStatus
import com.nodeorb.scmclient.model.EnhancedValidationResult
import com.nodeorb.scmclient.model.ValidationResult
import com.nodeorb.scmclient.model.ValidationContext
import com.nodeorb.scmclient.handler.BiometricHandler
import com.nodeorb.scmclient.logging.SCMLogger
import com.nodeorb.scmclient.config.SCMClientConfig
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

/**
 * Розумний біометричний інтерцептор
 * Автоматично ініціює біометричну перевірку для критичних дій (RED/YELLOW verdicts)
 */
class SmartBiometricInterceptor(
    private val config: SCMClientConfig,
    private val biometricHandler: BiometricHandler,
    private val scmLogger: SCMLogger
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(SmartBiometricInterceptor::class.java)
        
        // Пороги ризику для ініціювання біометричної перевірки
        private const val RED_RISK_THRESHOLD = 80.0
        private const val YELLOW_RISK_THRESHOLD = 60.0
    }

    /**
     * Обробляє результат валідації та ініціює біометричну перевірку при необхідності
     */
    fun processValidationResult(
        context: ValidationContext,
        result: ValidationResult
    ): EnhancedValidationResult {
        val enhancedResult = EnhancedValidationResult(
            allowed = result.allowed,
            reason = result.reason,
            riskScore = result.riskScore,
            policyId = result.policyId,
            scmOffline = result.scmOffline,
            biometricStatus = BiometricStatus.NOT_REQUIRED,
            biometricEvidence = null,
            biometricChallenge = null
        )

        // Перевіряємо чи потрібна біометрична перевірка
        if (result.riskScore >= YELLOW_RISK_THRESHOLD && !result.allowed) {
            return initiateBiometricChallenge(context, enhancedResult, result)
        }

        return enhancedResult
    }

    /**
     * Ініціює біометричний challenge для критичних дій
     */
    private fun initiateBiometricChallenge(
        context: ValidationContext,
        enhancedResult: EnhancedValidationResult,
        originalResult: ValidationResult
    ): EnhancedValidationResult {
        val riskLevel = determineRiskLevel(originalResult.riskScore)
        val authType = determineAuthType(riskLevel)
        
        logger.info("Initiating biometric challenge for user: ${context.userId}, risk: ${originalResult.riskScore}, level: $riskLevel")
        
        // Генеруємо біометричний challenge
        val challenge = biometricHandler.generateBiometricChallenge(
            userId = context.userId,
            authType = authType
        )

        // Створюємо EnhancedValidationResult з вимогою біометричної перевірки
        return enhancedResult.copy(
            allowed = false, // Блокуємо операцію до біометричної перевірки
            reason = "High risk operation requires biometric verification. Risk score: ${originalResult.riskScore}",
            biometricStatus = BiometricStatus.REQUIRED,
            biometricChallenge = challenge
        )
    }

    /**
     * Визначає рівень ризику
     */
    private fun determineRiskLevel(riskScore: Double): RiskLevel {
        return when {
            riskScore >= RED_RISK_THRESHOLD -> RiskLevel.RED
            riskScore >= YELLOW_RISK_THRESHOLD -> RiskLevel.YELLOW
            else -> RiskLevel.GREEN
        }
    }

    /**
     * Визначає тип біометричної аутентифікації залежно від рівня ризику
     */
    private fun determineAuthType(riskLevel: RiskLevel): BiometricAuthType {
        return when (riskLevel) {
            RiskLevel.RED -> BiometricAuthType.FACE_ID // Найвищий рівень безпеки
            RiskLevel.YELLOW -> BiometricAuthType.TOUCH_ID // Середній рівень
            RiskLevel.GREEN -> BiometricAuthType.WEB_AUTHN // Для нижчого рівня
        }
    }

    /**
     * Обробляє біометричну перевірку та оновлює результат
     */
    fun processBiometricVerification(
        context: ValidationContext,
        signedChallenge: String,
        bioSessionId: String
    ): EnhancedValidationResult {
        // Перевіряємо біометричний підпис
        val verificationResult = biometricHandler.verifyBiometricSignature(
            signedChallenge = signedChallenge,
            bioSessionId = bioSessionId,
            userId = context.userId
        )

        if (verificationResult.success) {
            // Створюємо біометричні докази
            val biometricEvidence = biometricHandler.createBiometricEvidence(
                verificationResult = verificationResult,
                signedChallenge = signedChallenge
            )

            // Створюємо Evidence Package
            val evidencePackage = createEvidencePackage(
                context = context,
                verificationResult = verificationResult,
                biometricEvidence = biometricEvidence
            )

            // Логуємо успішну біометричну перевірку
            scmLogger.logBiometricEvidence(evidencePackage)

            return EnhancedValidationResult(
                allowed = true, // Дозволяємо операцію після успішної біометричної перевірки
                reason = "Operation allowed after successful biometric verification",
                riskScore = 0.0, // Ризик зменшується після біометричної перевірки
                policyId = null,
                scmOffline = false,
                biometricStatus = BiometricStatus.VERIFIED,
                biometricEvidence = biometricEvidence,
                biometricChallenge = null
            )
        } else {
            // Біометрична перевірка неуспішна
            return EnhancedValidationResult(
                allowed = false,
                reason = "Biometric verification failed: ${verificationResult.errorMessage}",
                riskScore = 100.0, // Максимальний ризик
                policyId = null,
                scmOffline = false,
                biometricStatus = BiometricStatus.FAILED,
                biometricEvidence = null,
                biometricChallenge = null
            )
        }
    }

    /**
     * Створює Evidence Package для аудиту
     */
    private fun createEvidencePackage(
        context: ValidationContext,
        verificationResult: BiometricVerificationResult,
        biometricEvidence: BiometricEvidence
    ): EvidencePackage {
        return EvidencePackage(
            operationId = UUID.randomUUID().toString(),
            userId = context.userId,
            operationType = "VALIDATION_WITH_BIOMETRIC",
            timestamp = Instant.now(),
            ipAddress = context.additionalContext["ip_address"] ?: "unknown",
            userAgent = context.additionalContext["user_agent"] ?: "unknown",
            geoLocation = GeoLocation(
                latitude = context.latitude,
                longitude = context.longitude
            ),
            biometricEvidence = BiometricallySignedEvidence(
                biometricallySigned = biometricEvidence.biometricallySigned,
                authType = biometricEvidence.authType,
                bioSessionId = biometricEvidence.bioSessionId,
                signaturePayload = biometricEvidence.signaturePayload,
                signedAt = biometricEvidence.signedAt,
                verificationResult = biometricEvidence.verificationResult
            ),
            additionalEvidence = mapOf(
                "risk_score" to context.value.toString(),
                "category" to context.category,
                "validation_reason" to "High risk operation"
            )
        )
    }

    /**
     * Перевіряє чи потрібна біометрична перевірка для операції
     */
    fun requiresBiometricVerification(result: ValidationResult): Boolean {
        return result.riskScore >= YELLOW_RISK_THRESHOLD && !result.allowed
    }

    /**
     * Створює BiometricException для фронтенду
     */
    fun createBiometricException(
        context: ValidationContext,
        result: ValidationResult,
        challenge: BiometricChallenge
    ): BiometricException {
        return BiometricException(
            message = "High risk operation requires biometric verification. Risk score: ${result.riskScore}",
            challenge = challenge,
            operation = "VALIDATION_WITH_BIOMETRIC",
            request = context
        )
    }

    /**
     * Рівні ризику
     */
    enum class RiskLevel {
        GREEN,   // Низький ризик
        YELLOW,  // Середній ризик
        RED      // Високий ризик
    }
}

/**
 * Розширення для SCMClient для підтримки біометричної перевірки
 */
fun SCMClient.validateCostWithBiometric(
    userId: String,
    orderId: String,
    category: String,
    value: Double,
    lat: Double,
    lon: Double
): EnhancedValidationResult {
    val context = ValidationContext(
        userId = userId,
        orderId = orderId,
        category = category,
        value = value,
        latitude = lat,
        longitude = lon
    )

    // Виконуємо стандартну валідацію
    val result = validateCost(userId, orderId, category, value, lat, lon)
    
    // Отримуємо SmartBiometricInterceptor
    val interceptor = SmartBiometricInterceptor(
        config = config,
        biometricHandler = BiometricHandler(config, scmLogger),
        scmLogger = scmLogger
    )
    
    // Обробляємо результат з біометричною перевіркою
    return interceptor.processValidationResult(context, result)
}

/**
 * Розширення для SCMClient для підтвердження біометричної перевірки
 */
fun SCMClient.confirmWithBiometrics(
    userId: String,
    signedChallenge: String,
    bioSessionId: String
): AuthResult {
    val context = ValidationContext(
        userId = userId,
        orderId = "biometric-verification",
        category = "BIOMETRIC_CONFIRMATION",
        value = 0.0,
        latitude = 0.0,
        longitude = 0.0
    )

    val interceptor = SmartBiometricInterceptor(
        config = config,
        biometricHandler = BiometricHandler(config, scmLogger),
        scmLogger = scmLogger
    )
    
    // Обробляємо біометричну перевірку
    val enhancedResult = interceptor.processBiometricVerification(
        context = context,
        signedChallenge = signedChallenge,
        bioSessionId = bioSessionId
    )
    
    return AuthResult(
        success = enhancedResult.biometricStatus == BiometricStatus.VERIFIED,
        bioSessionId = bioSessionId,
        authType = enhancedResult.biometricEvidence?.authType ?: BiometricAuthType.FACE_ID,
        signedChallenge = enhancedResult.biometricEvidence?.signaturePayload,
        errorMessage = if (enhancedResult.biometricStatus == BiometricStatus.FAILED) "Biometric verification failed" else null,
        authenticatedAt = Instant.now()
    )
}

/**
 * Розширення для SCMClient для подачі апеляції з біометричним підписом
 */
fun SCMClient.submitAppealWithSignature(
    appeal: AppealRequestWithSignature
): Boolean {
    try {
        // Перевіряємо біометричний підпис
        val verificationResult = biometricHandler.verifyBiometricSignature(
            signedChallenge = appeal.biometricSignature,
            bioSessionId = appeal.bioSessionId,
            userId = "system" // Для апеляцій використовуємо системного користувача
        )

        if (!verificationResult.success) {
            logger.warn("Biometric signature verification failed for appeal: ${appeal.recordHash}")
            return false
        }

        // Створюємо біометричні докази
        val biometricEvidence = biometricHandler.createBiometricEvidence(
            verificationResult = verificationResult,
            signedChallenge = appeal.biometricSignature
        )

        // Створюємо Evidence Package
        val evidencePackage = EvidencePackage(
            operationId = appeal.recordHash,
            userId = "system",
            operationType = "APPEAL_WITH_BIOMETRIC_SIGNATURE",
            timestamp = Instant.now(),
            ipAddress = "127.0.0.1",
            userAgent = "SCM-Client",
            biometricEvidence = BiometricallySignedEvidence(
                biometricallySigned = biometricEvidence.biometricallySigned,
                authType = biometricEvidence.authType,
                bioSessionId = biometricEvidence.bioSessionId,
                signaturePayload = biometricEvidence.signaturePayload,
                signedAt = biometricEvidence.signedAt,
                verificationResult = biometricEvidence.verificationResult
            ),
            additionalEvidence = appeal.additionalContext
        )

        // Логуємо Evidence Package
        scmLogger.logBiometricEvidence(evidencePackage)

        // Виконуємо стандартну подачу апеляції
        return submitAppeal(
            recordHash = appeal.recordHash,
            text = appeal.text,
            evidenceUrl = appeal.evidenceUrl
        )

    } catch (e: Exception) {
        logger.error("Error submitting appeal with biometric signature", e)
        return false
    }
}