package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.ScmSnapshotRepository
import com.nodeorb.freight.marketplace.repository.MasterOrderRepository
import com.nodeorb.freight.marketplace.repository.BidRepository
import com.nodeorb.freight.marketplace.repository.UserProfileRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.logging.Logger

/**
 * Сервис интеграции с SCM (Supply Chain Management)
 * Подписывается на события из Kafka, проверяет Compliance Passport перевозчика
 * и генерирует Trust Token
 */
@Service
@Transactional
class ScmIntegrationService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val scmSnapshotRepository: ScmSnapshotRepository,
    private val masterOrderRepository: MasterOrderRepository,
    private val bidRepository: BidRepository,
    private val userProfileRepository: UserProfileRepository
) {

    companion object {
        private val logger = Logger.getLogger(ScmIntegrationService::class.java.name)
        private const val COMPLIANCE_TOPIC = "compliance.checks"
        private const val TRUST_TOKEN_TOPIC = "trust.tokens"
    }

    /**
     * Подписка на события подачи заявок
     * При получении события создает SCM снимок и запускает проверку соответствия
     */
    @KafkaListener(topics = ["bid.placement"], groupId = "freight-marketplace")
    fun handleBidPlacementEvent(bidEvent: BidPlacementEvent) {
        try {
            logger.info("Processing bid placement event: ${bidEvent.bidId}")
            
            // Создаем SCM снимок
            val scmSnapshot = createScmSnapshot(bidEvent)
            
            // Запускаем проверку соответствия
            val complianceResult = performComplianceCheck(bidEvent, scmSnapshot)
            
            // Сохраняем результат проверки
            updateScmSnapshotWithResult(scmSnapshot, complianceResult)
            
            // Генерируем Trust Token при успешной проверке
            if (complianceResult.complianceStatus == "COMPLIANT") {
                val trustToken = generateTrustToken(bidEvent, complianceResult)
                sendTrustTokenEvent(trustToken)
            }
            
            // Отправляем результат проверки
            sendComplianceResultEvent(complianceResult)
            
        } catch (e: Exception) {
            logger.severe("Error processing bid placement event: ${e.message}")
            // Отправляем событие об ошибке
            sendComplianceErrorEvent(bidEvent.bidId, e.message ?: "Unknown error")
        }
    }

    /**
     * Создание SCM снимка при подаче заявки
     */
    private fun createScmSnapshot(bidEvent: BidPlacementEvent): ScmSnapshotEntity {
        val masterOrder = masterOrderRepository.findById(bidEvent.masterOrderId)
            .orElseThrow { RuntimeException("Master order not found: ${bidEvent.masterOrderId}") }
        
        val bid = bidRepository.findById(bidEvent.bidId)
            .orElseThrow { RuntimeException("Bid not found: ${bidEvent.bidId}") }

        val snapshot = ScmSnapshotEntity(
            bidId = bidEvent.bidId,
            carrierId = bidEvent.carrierId,
            masterOrderId = bidEvent.masterOrderId,
            snapshotDate = LocalDateTime.now(),
            complianceStatus = ComplianceStatus.PENDING,
            complianceDetails = "Compliance check initiated",
            securityClearance = SecurityLevel.NONE,
            securityDetails = "Security check pending",
            riskScore = 0.0,
            riskFactors = "Initial risk assessment",
            auditTrail = "Bid placement event received"
        )

        return scmSnapshotRepository.save(snapshot)
    }

    /**
     * Выполнение проверки соответствия
     */
    private fun performComplianceCheck(
        bidEvent: BidPlacementEvent,
        scmSnapshot: ScmSnapshotEntity
    ): ComplianceCheckResult {
        val masterOrder = masterOrderRepository.findById(bidEvent.masterOrderId)
            .orElseThrow { RuntimeException("Master order not found") }
        
        val userProfile = userProfileRepository.findById(bidEvent.carrierId)
            .orElseThrow { RuntimeException("Carrier profile not found") }

        // Проверка Compliance Passport
        val complianceResult = checkCompliancePassport(bidEvent, masterOrder, userProfile)
        
        // Проверка безопасности
        val securityResult = checkSecurityRequirements(bidEvent, masterOrder)
        
        // Оценка рисков
        val riskAssessment = performRiskAssessment(bidEvent, userProfile)

        return ComplianceCheckResult(
            carrierId = bidEvent.carrierId,
            masterOrderId = bidEvent.masterOrderId,
            bidId = bidEvent.bidId,
            complianceStatus = complianceResult.status,
            complianceDetails = complianceResult.details,
            securityClearance = securityResult.clearance,
            securityDetails = securityResult.details,
            riskScore = riskAssessment.score,
            riskFactors = riskAssessment.factors,
            trustToken = null, // Будет заполнен позже
            auditTrail = buildAuditTrail(complianceResult, securityResult, riskAssessment)
        )
    }

    /**
     * Проверка Compliance Passport перевозчика
     */
    private fun checkCompliancePassport(
        bidEvent: BidPlacementEvent,
        masterOrder: MasterOrderEntity,
        userProfile: UserProfileEntity
    ): ComplianceCheck {
        val complianceStatus = when {
            userProfile.totalOrders < 10 -> {
                ComplianceStatus.NON_COMPLIANT to "Insufficient order history"
            }
            userProfile.rating < 3.0 -> {
                ComplianceStatus.NON_COMPLIANT to "Low rating"
            }
            userProfile.completedOrders.toDouble() / userProfile.totalOrders < 0.8 -> {
                ComplianceStatus.NON_COMPLIANT to "Low completion rate"
            }
            else -> {
                ComplianceStatus.COMPLIANT to "All compliance checks passed"
            }
        }

        return ComplianceCheck(
            status = complianceStatus.first.name,
            details = complianceStatus.second
        )
    }

    /**
     * Проверка требований безопасности
     */
    private fun checkSecurityRequirements(
        bidEvent: BidPlacementEvent,
        masterOrder: MasterOrderEntity
    ): SecurityCheck {
        // Проверка маршрута на безопасность
        val routeSecurity = checkRouteSecurity(bidEvent.route)
        
        // Проверка типа груза
        val cargoSecurity = checkCargoSecurity(bidEvent.cargoDetails)
        
        // Определение уровня безопасности
        val securityLevel = when {
            routeSecurity.hasHighRisk || cargoSecurity.isHazardous -> SecurityLevel.RESTRICTED
            routeSecurity.hasMediumRisk || cargoSecurity.requiresSpecialHandling -> SecurityLevel.SECRET
            cargoSecurity.isTemperatureControlled -> SecurityLevel.CONFIDENTIAL
            else -> SecurityLevel.NONE
        }

        return SecurityCheck(
            clearance = securityLevel.name,
            details = "Route: ${routeSecurity.details}, Cargo: ${cargoSecurity.details}"
        )
    }

    /**
     * Проверка безопасности маршрута
     */
    private fun checkRouteSecurity(route: RouteInfo): RouteSecurity {
        val highRiskCountries = setOf("SY", "YE", "AF", "SO") // Пример высокорисковых стран
        val mediumRiskCountries = setOf("UA", "RU", "BY") // Пример среднерисковых стран
        
        val allCountries = listOf(route.pickupLocation.country, route.deliveryLocation.country) + 
                          route.waypoints.map { it.country }
        
        val hasHighRisk = allCountries.any { it in highRiskCountries }
        val hasMediumRisk = allCountries.any { it in mediumRiskCountries }
        
        val details = when {
            hasHighRisk -> "High risk countries detected"
            hasMediumRisk -> "Medium risk countries detected"
            else -> "Route is secure"
        }

        return RouteSecurity(
            hasHighRisk = hasHighRisk,
            hasMediumRisk = hasMediumRisk,
            details = details
        )
    }

    /**
     * Проверка безопасности груза
     */
    private fun checkCargoSecurity(cargoDetails: CargoDetails): CargoSecurity {
        val isHazardous = cargoDetails.hazardous
        val requiresSpecialHandling = cargoDetails.temperatureControlled || cargoDetails.value != null
        
        val details = when {
            isHazardous -> "Hazardous materials require special handling"
            requiresSpecialHandling -> "Special handling required"
            else -> "Standard cargo"
        }

        return CargoSecurity(
            isHazardous = isHazardous,
            requiresSpecialHandling = requiresSpecialHandling,
            details = details
        )
    }

    /**
     * Оценка рисков
     */
    private fun performRiskAssessment(
        bidEvent: BidPlacementEvent,
        userProfile: UserProfileEntity
    ): RiskAssessment {
        var riskScore = 0.0
        val riskFactors = mutableListOf<String>()

        // Факторы риска
        when {
            userProfile.rating < 4.0 -> {
                riskScore += 0.3
                riskFactors.add("Low carrier rating")
            }
            userProfile.completedOrders.toDouble() / userProfile.totalOrders < 0.9 -> {
                riskScore += 0.2
                riskFactors.add("Low completion rate")
            }
            bidEvent.cargoDetails.hazardous -> {
                riskScore += 0.4
                riskFactors.add("Hazardous cargo")
            }
            bidEvent.cargoDetails.value != null && bidEvent.cargoDetails.value!! > 100000.0 -> {
                riskScore += 0.3
                riskFactors.add("High value cargo")
            }
        }

        return RiskAssessment(
            score = riskScore.coerceIn(0.0, 1.0),
            factors = riskFactors
        )
    }

    /**
     * Обновление SCM снимка с результатами проверки
     */
    private fun updateScmSnapshotWithResult(
        snapshot: ScmSnapshotEntity,
        result: ComplianceCheckResult
    ) {
        snapshot.complianceStatus = ComplianceStatus.valueOf(result.complianceStatus)
        snapshot.complianceDetails = result.complianceDetails
        snapshot.securityClearance = SecurityLevel.valueOf(result.securityClearance)
        snapshot.securityDetails = result.securityDetails
        snapshot.riskScore = result.riskScore
        snapshot.riskFactors = result.riskFactors.joinToString(", ")
        snapshot.auditTrail = result.auditTrail
        
        scmSnapshotRepository.save(snapshot)
    }

    /**
     * Генерация Trust Token
     */
    private fun generateTrustToken(
        bidEvent: BidPlacementEvent,
        result: ComplianceCheckResult
    ): TrustTokenInfo {
        val token = UUID.randomUUID().toString()
        val expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 часа
        
        val permissions = mutableListOf("BID_SUBMISSION", "ORDER_ACCESS")
        if (result.securityClearance == "CONFIDENTIAL") permissions.add("CONFIDENTIAL_CARGO")
        if (result.securityClearance == "SECRET") permissions.add("SECRET_CARGO")
        if (result.securityClearance == "RESTRICTED") permissions.add("RESTRICTED_CARGO")

        return TrustTokenInfo(
            token = token,
            carrierId = bidEvent.carrierId,
            expiresAt = expiresAt,
            permissions = permissions,
            metadata = mapOf(
                "masterOrderId" to bidEvent.masterOrderId.toString(),
                "bidId" to bidEvent.bidId.toString(),
                "complianceStatus" to result.complianceStatus,
                "riskScore" to result.riskScore.toString()
            )
        )
    }

    /**
     * Построение аудит-трейла
     */
    private fun buildAuditTrail(
        compliance: ComplianceCheck,
        security: SecurityCheck,
        risk: RiskAssessment
    ): String {
        return """
            Compliance: ${compliance.status} - ${compliance.details}
            Security: ${security.clearance} - ${security.details}
            Risk: Score=${risk.score}, Factors=${risk.factors.joinToString(", ")}
            Timestamp: ${LocalDateTime.now()}
        """.trimIndent()
    }

    /**
     * Отправка события с результатом проверки
     */
    private fun sendComplianceResultEvent(result: ComplianceCheckResult) {
        kafkaTemplate.send(COMPLIANCE_TOPIC, result)
        logger.info("Compliance check result sent for bid ${result.bidId}")
    }

    /**
     * Отправка события с Trust Token
     */
    private fun sendTrustTokenEvent(token: TrustTokenInfo) {
        kafkaTemplate.send(TRUST_TOKEN_TOPIC, token)
        logger.info("Trust token sent for carrier ${token.carrierId}")
    }

    /**
     * Отправка события об ошибке
     */
    private fun sendComplianceErrorEvent(bidId: UUID, errorMessage: String) {
        val errorEvent = mapOf(
            "bidId" to bidId,
            "error" to errorMessage,
            "timestamp" to LocalDateTime.now()
        )
        kafkaTemplate.send("compliance.errors", errorEvent)
        logger.warning("Compliance error sent for bid $bidId: $errorMessage")
    }

    // Вспомогательные классы для внутреннего использования
    private data class ComplianceCheck(
        val status: String,
        val details: String
    )

    private data class SecurityCheck(
        val clearance: String,
        val details: String
    )

    private data class RouteSecurity(
        val hasHighRisk: Boolean,
        val hasMediumRisk: Boolean,
        val details: String
    )

    private data class CargoSecurity(
        val isHazardous: Boolean,
        val requiresSpecialHandling: Boolean,
        val details: String
    )

    private data class RiskAssessment(
        val score: Double,
        val factors: List<String>
    )
}