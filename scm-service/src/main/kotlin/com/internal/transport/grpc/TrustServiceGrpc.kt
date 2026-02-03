package com.internal.transport.grpc

import com.internal.engine.policy.PolicyEngine
import com.internal.engine.validation.MarketOracle
import com.internal.integrations.SecurityEventBus
import com.model.EvaluateAccessRequest
import com.model.EvaluateAccessResponse
import com.model.RiskLevel
import com.model.ValidateManualInputRequest
import com.model.ValidateManualInputResponse
import io.grpc.stub.StreamObserver
import nodeorb.scm.v1.*
import org.lognet.springboot.grpc.GRpcService
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Реализация gRPC сервиса TrustService
 * Обеспечивает высокопроизводительное взаимодействие между микросервисами
 */
@GRpcService
class TrustServiceGrpc(
    private val policyEngine: PolicyEngine,
    private val marketOracle: MarketOracle,
    private val securityEventBus: SecurityEventBus
) : TrustServiceGrpcKt.TrustServiceCoroutineImplBase() {

    companion object {
        private const val MAX_PRICE_DEVIATION_GREEN = 0.15
        private const val MAX_PRICE_DEVIATION_YELLOW = 0.40
    }

    /**
     * Проверка прав на действие с учетом контекста (ABAC)
     */
    @Transactional(readOnly = true)
    override suspend fun evaluateAccess(request: EvaluateAccessRequestProto): EvaluateAccessResponseProto {
        val evaluateRequest = convertToModel(request)
        
        val response = policyEngine.evaluateAccess(evaluateRequest)
        
        // Отправляем событие в Security Event Bus
        if (!response.allowed) {
            securityEventBus.sendSecurityEvent(
                SecurityEvent(
                    eventId = "ACCESS_DENIED_${System.currentTimeMillis()}",
                    eventType = "ACCESS_DENIED",
                    timestamp = Instant.now(),
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

        return convertToProto(response)
    }

    /**
     * Получение Trust Token для конкретной сделки
     */
    override suspend fun issueTrustToken(request: IssueTrustTokenRequestProto): IssueTrustTokenResponseProto {
        val trustToken = generateTrustToken(
            request.userId,
            request.entityId,
            request.payloadHash
        )

        return IssueTrustTokenResponseProto.newBuilder()
            .setTrustToken(trustToken)
            .setExpiresAt(System.currentTimeMillis() + 3600000) // 1 час
            .build()
    }

    /**
     * Получение актуального Compliance Passport участника
     */
    override suspend fun getCompliancePassport(request: GetCompliancePassportRequestProto): GetCompliancePassportResponseProto {
        // В реальной системе здесь будет вызов к базе данных
        // Пока возвращаем заглушку
        
        return GetCompliancePassportResponseProto.newBuilder()
            .setUserId(request.userId)
            .setEntityType(request.entityType)
            .setTrustScore(75.0)
            .setComplianceStatus("VERIFIED")
            .setIsBiometricsEnabled(false)
            .setExpiresAt(System.currentTimeMillis() + 86400000) // 24 часа
            .build()
    }

    /**
     * Валидация ручного ввода цен (Market Oracle)
     */
    override suspend fun validateManualInput(request: ValidateManualInputRequestProto): ValidateManualInputResponseProto {
        val validationRequest = convertToValidationModel(request)
        
        val response = marketOracle.validateManualInput(
            validationRequest.userId,
            validationRequest.orderId,
            validationRequest.materialsCost,
            validationRequest.laborCost,
            validationRequest.currency
        )

        // Сохраняем данные о валидации
        marketOracle.saveManualEntry(
            validationRequest.userId,
            validationRequest.orderId,
            validationRequest.materialsCost,
            validationRequest.laborCost,
            validationRequest.currency,
            response
        )

        // Отправляем событие при аномалии
        if (response.status == "RED" || response.status == "YELLOW") {
            securityEventBus.triggerPriceAnomaly(
                validationRequest.userId,
                validationRequest.orderId,
                calculateDeviation(response.suggestedMedian, validationRequest.materialsCost + validationRequest.laborCost),
                response.suggestedMedian
            )
        }

        return convertToValidationProto(response)
    }

    /**
     * Создание апелляции на результат валидации
     */
    override suspend fun createAppeal(request: CreateAppealRequestProto): CreateAppealResponseProto {
        // В реальной системе здесь будет логика создания апелляции
        
        return CreateAppealResponseProto.newBuilder()
            .setAppealId("APPEAL_${System.currentTimeMillis()}")
            .setStatus("PENDING")
            .build()
    }

    /**
     * Проверка биометрической аутентификации
     */
    override suspend fun verifyBiometrics(request: VerifyBiometricsRequestProto): VerifyBiometricsResponseProto {
        // В реальной системе здесь будет интеграция с WebAuthn
        val verified = verifyBiometricData(request.biometricData)
        
        if (!verified) {
            securityEventBus.triggerBiometricFailure(
                request.userId,
                request.sessionId,
                "Biometric verification failed"
            )
        }

        return VerifyBiometricsResponseProto.newBuilder()
            .setVerified(verified)
            .setVerificationId("BIO_${System.currentTimeMillis()}")
            .setReason(if (verified) "Success" else "Verification failed")
            .build()
    }

    /**
     * Проверка геозон (Geofencing)
     */
    override suspend fun checkGeofence(request: CheckGeofenceRequestProto): CheckGeofenceResponseProto {
        val insideGeofence = checkGeofenceBounds(
            request.latitude,
            request.longitude,
            request.geofenceType
        )

        if (!insideGeofence) {
            securityEventBus.triggerGeofenceViolation(
                request.userId,
                request.latitude,
                request.longitude,
                request.geofenceType,
                "User outside geofence bounds"
            )
        }

        return CheckGeofenceResponseProto.newBuilder()
            .setInsideGeofence(insideGeofence)
            .setGeofenceId("GEOFENCE_${request.geofenceType}")
            .setViolationReason(if (insideGeofence) "" else "Outside geofence")
            .build()
    }

    /**
     * Проверка часов работы (ELD)
     */
    override suspend fun checkHoursOfService(request: CheckHoursOfServiceRequestProto): CheckHoursOfServiceResponseProto {
        val compliant = checkHoursOfServiceCompliance(
            request.driverId,
            request.vehicleId,
            request.currentTime
        )

        val remainingHours = if (compliant) 8 else 0
        val nextBreak = if (compliant) 480 else 0 // 8 часов в минутах

        if (!compliant) {
            securityEventBus.triggerHoursOfServiceViolation(
                request.driverId,
                request.vehicleId,
                remainingHours,
                "Hours of service violation"
            )
        }

        return CheckHoursOfServiceResponseProto.newBuilder()
            .setCompliant(compliant)
            .setRemainingHours(remainingHours)
            .setNextRequiredBreak(nextBreak)
            .setViolationReason(if (compliant) "" else "Exceeded maximum driving hours")
            .build()
    }

    // Вспомогательные методы

    private fun convertToModel(request: EvaluateAccessRequestProto): EvaluateAccessRequest {
        return EvaluateAccessRequest(
            userId = request.userId,
            serviceId = request.serviceId,
            action = request.action,
            context = request.contextMap
        )
    }

    private fun convertToProto(response: EvaluateAccessResponse): EvaluateAccessResponseProto {
        val builder = EvaluateAccessResponseProto.newBuilder()
            .setAllowed(response.allowed)
            .setDecisionId(response.decisionId)
            .setReason(response.reason)
            .setRequiresBiometrics(response.requiresBiometrics)
            .setRequiresAppeal(response.requiresAppeal)

        // Конвертация уровня риска
        val riskLevel = when (response.riskLevel) {
            RiskLevel.LOW -> RiskLevelProto.RISK_LEVEL_LOW
            RiskLevel.MEDIUM -> RiskLevelProto.RISK_LEVEL_MEDIUM
            RiskLevel.HIGH -> RiskLevelProto.RISK_LEVEL_HIGH
            RiskLevel.CRITICAL -> RiskLevelProto.RISK_LEVEL_CRITICAL
            else -> RiskLevelProto.RISK_LEVEL_UNSPECIFIED
        }
        builder.riskLevel = riskLevel

        return builder.build()
    }

    private fun convertToValidationModel(request: ValidateManualInputRequestProto): ValidateManualInputRequest {
        return ValidateManualInputRequest(
            userId = request.userId,
            orderId = request.orderId,
            serviceSource = request.serviceSource,
            materialsCost = request.materialsCost,
            laborCost = request.laborCost,
            currency = request.currency
        )
    }

    private fun convertToValidationProto(response: ValidationVerdict): ValidateManualInputResponseProto {
        return ValidateManualInputResponseProto.newBuilder()
            .setStatus(response.status)
            .setRiskScore(response.riskScore)
            .setRequiresAppeal(response.requiresAppeal)
            .setRequiresBiometrics(response.requiresBiometrics)
            .setSuggestedMedian(response.suggestedMedian)
            .setComment(response.comment)
            .build()
    }

    private fun generateTrustToken(userId: String, entityId: String, payloadHash: String): String {
        // В реальной системе здесь будет генерация JWT токена с подписью
        return "TRUST_${userId}_${entityId}_${System.currentTimeMillis()}"
    }

    private fun verifyBiometricData(biometricData: String): Boolean {
        // В реальной системе здесь будет интеграция с WebAuthn
        // Пока заглушка - считаем, что биометрия прошла
        return true
    }

    private fun checkGeofenceBounds(latitude: Double, longitude: Double, geofenceType: String): Boolean {
        // В реальной системе здесь будет проверка против геозон
        // Пока заглушка - считаем, что пользователь в пределах
        return true
    }

    private fun checkHoursOfServiceCompliance(driverId: String, vehicleId: String, currentTime: Long): Boolean {
        // В реальной системе здесь будет проверка против ELD данных
        // Пока заглушка - считаем, что соответствие есть
        return true
    }

    private fun calculateDeviation(median: Double, actual: Double): Double {
        return if (median > 0) (actual - median) / median else 1.0
    }
}