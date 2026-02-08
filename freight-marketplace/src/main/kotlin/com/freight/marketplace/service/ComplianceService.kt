package com.freight.marketplace.service

import com.freight.marketplace.dto.*
import com.freight.marketplace.entity.*
import com.freight.marketplace.repository.UserProfileRepository
import com.freight.marketplace.repository.ScmSnapshotRepository
import com.nodeorb.scmclient.SCMClient
import com.nodeorb.scmclient.SCMClientFactory
import com.nodeorb.scmclient.model.ValidationResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * Сервис проверки соответствия (Compliance Service)
 * Использует SCM Client SDK для проверки Compliance Passport перевозчика
 */
@Service
@Transactional
class ComplianceService(
    private val userProfileRepository: UserProfileRepository,
    private val scmSnapshotRepository: ScmSnapshotRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ComplianceService::class.java)
    }

    private val scmClient: SCMClient = SCMClientFactory.createProductionClient(
        host = "scm-service",
        port = 9090
    )

    /**
     * Проверка Compliance Passport перевозчика через SCM Service
     */
    fun checkCompliancePassport(
        carrierId: UUID,
        masterOrderId: UUID,
        cargoType: String,
        route: RouteInfo
    ): ComplianceCheckResult {
        logger.info("Checking compliance passport for carrier: $carrierId, order: $masterOrderId")
        
        val userProfile = userProfileRepository.findById(carrierId)
            .orElseThrow { RuntimeException("Carrier profile not found: $carrierId") }

        // Выполняем проверку через SCM Service
        val validationResult = scmClient.validateCost(
            userId = carrierId.toString(),
            orderId = masterOrderId.toString(),
            category = "BID_PLACEMENT",
            value = 0.0, // Стоимость будет передана позже
            lat = route.pickupLocation.latitude,
            lon = route.pickupLocation.longitude
        )

        // Создаем SCM снимок
        val scmSnapshot = createComplianceSnapshot(
            carrierId, masterOrderId, validationResult
        )

        return ComplianceCheckResult(
            carrierId = carrierId,
            masterOrderId = masterOrderId,
            bidId = UUID.randomUUID(), // Временный ID, будет обновлен позже
            complianceStatus = if (validationResult.allowed) "COMPLIANT" else "NON_COMPLIANT",
            complianceDetails = validationResult.reason,
            securityClearance = validationResult.securityLevel.ifEmpty { SecurityLevel.NONE.name },
            securityDetails = validationResult.securityDetails,
            riskScore = validationResult.riskScore,
            riskFactors = validationResult.violations,
            trustToken = validationResult.trustToken,
            auditTrail = buildComplianceAuditTrail(validationResult, scmSnapshot)
        )
    }

    /**
     * Проверка соответствия ставки требованиям через SCM Service
     */
    fun checkCompliance(
        carrierId: UUID,
        freightOrderId: UUID?,
        masterOrderId: UUID?,
        partialOrderId: UUID?
    ): ComplianceResult {
        logger.info("Checking compliance for carrier: $carrierId")
        
        // Для простоты используется masterOrderId, если он доступен
        val orderId = masterOrderId ?: freightOrderId ?: partialOrderId
            ?: throw IllegalArgumentException("No order id provided")

        // Выполняем проверку через SCM Service
        // В реальном сценарии нужно передать больше параметров (цена, геопозиция)
        val validationResult = scmClient.validateCost(
            userId = carrierId.toString(),
            orderId = orderId.toString(),
            category = "BID_PLACEMENT",
            value = 0.0,
            lat = 0.0,
            lon = 0.0
        )

        return ComplianceResult(
            isCompliant = validationResult.allowed,
            violations = validationResult.violations,
            trustToken = validationResult.trustToken
        )
    }

    /**
     * Создание SCM снимка
     */
    private fun createComplianceSnapshot(
        carrierId: UUID,
        masterOrderId: UUID,
        validationResult: ValidationResult
    ): ScmSnapshotEntity {
        val complianceStatus = if (validationResult.allowed) {
            ComplianceStatus.COMPLIANT
        } else {
            ComplianceStatus.NON_COMPLIANT
        }

        val securityLevel = if (validationResult.securityLevel.isNotEmpty()) {
            SecurityLevel.valueOf(validationResult.securityLevel)
        } else {
            SecurityLevel.NONE
        }

        val snapshot = ScmSnapshotEntity(
            bidId = UUID.randomUUID(),
            carrierId = carrierId,
            masterOrderId = masterOrderId,
            snapshotDate = LocalDateTime.now(),
            complianceStatus = complianceStatus,
            complianceDetails = validationResult.reason,
            securityClearance = securityLevel,
            securityDetails = validationResult.securityDetails,
            riskScore = validationResult.riskScore,
            riskFactors = validationResult.violations.joinToString(", "),
            auditTrail = "SCM compliance check completed"
        )

        return scmSnapshotRepository.save(snapshot)
    }

    /**
     * Построение аудит-трейла проверки соответствия
     */
    private fun buildComplianceAuditTrail(
        validationResult: ValidationResult,
        snapshot: ScmSnapshotEntity
    ): String {
        return """
            Compliance Check Audit Trail:
            Status: ${if (validationResult.allowed) "COMPLIANT" else "NON_COMPLIANT"}
            Details: ${validationResult.reason}
            Risk Score: ${validationResult.riskScore}
            Risk Factors: ${validationResult.violations.joinToString(", ")}
            Security Level: ${validationResult.securityLevel}
            Security Details: ${validationResult.securityDetails}
            Snapshot ID: ${snapshot.id}
            Snapshot Date: ${snapshot.snapshotDate}
            Audit Timestamp: ${LocalDateTime.now()}
        """.trimIndent()
    }

    /**
     * Результат проверки соответствия
     */
    data class ComplianceResult(
        val isCompliant: Boolean,
        val violations: List<String>,
        val trustToken: String? = null
    )
}