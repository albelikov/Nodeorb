package com.freight.marketplace.service

import com.freight.marketplace.dto.BidPlacementEvent
import com.freight.marketplace.dto.ComplianceCheckResult
import com.freight.marketplace.dto.ScmSnapshotEntity
import com.freight.marketplace.repository.ScmSnapshotRepository
import com.freight.marketplace.repository.MasterOrderRepository
import com.freight.marketplace.repository.BidRepository
import com.freight.marketplace.repository.UserProfileRepository
import com.nodeorb.scmclient.SCMClient
import com.nodeorb.scmclient.SCMClientFactory
import com.nodeorb.scmclient.model.ValidationResult
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * Сервис интеграции с SCM (Security & Compliance Management)
 * Использует SCM Client SDK для проверки соответствия требованиям
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
        private val logger = LoggerFactory.getLogger(ScmIntegrationService::class.java)
        private const val COMPLIANCE_TOPIC = "compliance.checks"
        private const val TRUST_TOKEN_TOPIC = "trust.tokens"
    }

    private val scmClient: SCMClient = SCMClientFactory.createProductionClient(
        host = "scm-service",
        port = 9090
    )

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
            
            // Запускаем проверку соответствия через SCM Service
            val validationResult = performScmValidation(bidEvent)
            
            // Сохраняем результат проверки
            updateScmSnapshotWithResult(scmSnapshot, validationResult)
            
            // Генерируем Trust Token при успешной проверке
            if (validationResult.allowed) {
                val trustToken = validationResult.trustToken
                if (trustToken != null) {
                    sendTrustTokenEvent(trustToken)
                }
            }
            
            // Отправляем результат проверки
            val complianceResult = mapValidationResultToComplianceCheckResult(bidEvent, validationResult)
            sendComplianceResultEvent(complianceResult)
            
        } catch (e: Exception) {
            logger.error("Error processing bid placement event: ${e.message}", e)
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
            complianceStatus = com.freight.marketplace.entity.ComplianceStatus.PENDING,
            complianceDetails = "Compliance check initiated",
            securityClearance = com.freight.marketplace.entity.SecurityLevel.NONE,
            securityDetails = "Security check pending",
            riskScore = 0.0,
            riskFactors = "Initial risk assessment",
            auditTrail = "Bid placement event received"
        )

        return scmSnapshotRepository.save(snapshot)
    }

    /**
     * Выполнение проверки соответствия через SCM Service
     */
    private fun performScmValidation(bidEvent: BidPlacementEvent): ValidationResult {
        logger.info("Performing SCM validation for bid: ${bidEvent.bidId}")
        
        return scmClient.validateCost(
            userId = bidEvent.carrierId.toString(),
            orderId = bidEvent.masterOrderId.toString(),
            category = "BID_PLACEMENT",
            value = bidEvent.amount,
            lat = bidEvent.route.pickupLocation.latitude,
            lon = bidEvent.route.pickupLocation.longitude
        )
    }

    /**
     * Обновление SCM снимка с результатами проверки
     */
    private fun updateScmSnapshotWithResult(
        snapshot: ScmSnapshotEntity,
        validationResult: ValidationResult
    ) {
        val complianceStatus = if (validationResult.allowed) {
            com.freight.marketplace.entity.ComplianceStatus.COMPLIANT
        } else {
            com.freight.marketplace.entity.ComplianceStatus.NON_COMPLIANT
        }
        
        val securityLevel = if (validationResult.securityLevel.isNotEmpty()) {
            com.freight.marketplace.entity.SecurityLevel.valueOf(validationResult.securityLevel)
        } else {
            com.freight.marketplace.entity.SecurityLevel.NONE
        }

        snapshot.complianceStatus = complianceStatus
        snapshot.complianceDetails = validationResult.reason
        snapshot.securityClearance = securityLevel
        snapshot.securityDetails = validationResult.securityDetails
        snapshot.riskScore = validationResult.riskScore
        snapshot.riskFactors = validationResult.violations.joinToString(", ")
        snapshot.auditTrail = "SCM validation completed: ${validationResult.reason}"
        
        scmSnapshotRepository.save(snapshot)
    }

    /**
     * Маппинг результата валидации SCM на ComplianceCheckResult
     */
    private fun mapValidationResultToComplianceCheckResult(
        bidEvent: BidPlacementEvent,
        validationResult: ValidationResult
    ): ComplianceCheckResult {
        val complianceStatus = if (validationResult.allowed) "COMPLIANT" else "NON_COMPLIANT"
        val securityLevel = if (validationResult.securityLevel.isNotEmpty()) {
            validationResult.securityLevel
        } else {
            com.freight.marketplace.entity.SecurityLevel.NONE.name
        }

        return ComplianceCheckResult(
            carrierId = bidEvent.carrierId,
            masterOrderId = bidEvent.masterOrderId,
            bidId = bidEvent.bidId,
            complianceStatus = complianceStatus,
            complianceDetails = validationResult.reason,
            securityClearance = securityLevel,
            securityDetails = validationResult.securityDetails,
            riskScore = validationResult.riskScore,
            riskFactors = validationResult.violations,
            trustToken = validationResult.trustToken,
            auditTrail = "SCM validation: ${validationResult.reason}"
        )
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
    private fun sendTrustTokenEvent(token: String) {
        kafkaTemplate.send(TRUST_TOKEN_TOPIC, token)
        logger.info("Trust token sent for carrier")
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
        logger.warn("Compliance error sent for bid $bidId: $errorMessage")
    }
}