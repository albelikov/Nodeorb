package com.freight.marketplace.service

import com.freight.marketplace.dto.*
import com.freight.marketplace.entity.*
import com.freight.marketplace.repository.ScmSnapshotRepository
import com.freight.marketplace.exception.*
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.logging.Logger

/**
 * Расширенный сервис интеграции с SCM системой
 * Реализует жесткие бизнес-правила на основе типов груза
 */
@Service
@Transactional
class ExtendedScmIntegrationService(
    private val scmSnapshotRepository: ScmSnapshotRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val policyEvaluator: PolicyEvaluator
) {

    companion object {
        private val logger = Logger.getLogger(ExtendedScmIntegrationService::class.java.name)
    }

    /**
     * Проверка соответствия перевозчика требованиям безопасности с учетом ADR
     */
    fun checkCarrierComplianceWithADR(
        carrierId: UUID,
        masterOrder: MasterOrderEntity,
        bidId: UUID
    ): ComplianceCheckResult {
        logger.info("Checking ADR compliance for carrier $carrierId on order ${masterOrder.id}")

        // Получаем последний снимок SCM
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrder.id!!)
        
        if (lastSnapshot == null) {
            throw ScmSnapshotNotFoundException("No SCM snapshot found for carrier $carrierId on order ${masterOrder.id}")
        }

        // Оцениваем соответствие по бизнес-правилам
        val policyResult = policyEvaluator.evaluateCarrierCompliance(carrierId, masterOrder, lastSnapshot)

        // Формируем результат проверки
        val violations = policyResult.violations.joinToString(", ")
        val warnings = policyResult.warnings.joinToString(", ")

        val complianceStatus = when (policyResult.status) {
            com.freight.marketplace.service.ComplianceStatus.COMPLIANT -> "COMPLIANT"
            com.freight.marketplace.service.ComplianceStatus.WARNING -> "COMPLIANT_WITH_WARNINGS"
            com.freight.marketplace.service.ComplianceStatus.VIOLATION -> "NON_COMPLIANT"
        }

        return ComplianceCheckResult(
            carrierId = carrierId,
            masterOrderId = masterOrder.id!!,
            bidId = bidId,
            complianceStatus = complianceStatus,
            complianceDetails = "ADR-based compliance check completed. Violations: $violations. Warnings: $warnings",
            securityClearance = lastSnapshot.securityClearance.name,
            securityDetails = "ADR security check passed",
            riskScore = policyResult.riskScore,
            riskFactors = policyResult.warnings,
            trustToken = null,
            auditTrail = "ADR policy evaluation completed"
        )
    }

    /**
     * Проверка возможности подачи ставки с учетом ADR политики
     */
    fun validateBidWithADRPolicy(
        carrierId: UUID,
        masterOrder: MasterOrderEntity
    ): BidEligibilityResult {
        logger.info("Validating ADR bid eligibility for carrier $carrierId on order ${masterOrder.id}")

        // Получаем последний снимок SCM
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrder.id!!)
        
        if (lastSnapshot == null) {
            return BidEligibilityResult(
                isEligible = false,
                reason = "No SCM snapshot found",
                violations = listOf("No compliance data available")
            )
        }

        // Проверяем соответствие ADR политике
        return policyEvaluator.validateBidEligibility(carrierId, masterOrder, lastSnapshot)
    }

    /**
     * Проверка возможности подтверждения ставки (Awarding) с учетом ADR политики и рискового балла
     */
    fun validateAwardingWithADRRiskPolicy(
        carrierId: UUID,
        masterOrder: MasterOrderEntity
    ): AwardingEligibilityResult {
        logger.info("Validating ADR awarding eligibility for carrier $carrierId on order ${masterOrder.id}")

        // Получаем последний снимок SCM
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrder.id!!)
        
        if (lastSnapshot == null) {
            return AwardingEligibilityResult(
                isEligible = false,
                reason = "No SCM snapshot found",
                violations = listOf("No compliance data available"),
                requiresApproval = false
            )
        }

        // Проверяем соответствие ADR политике
        val result = policyEvaluator.validateAwardingEligibility(carrierId, masterOrder, lastSnapshot)

        // Дополнительная проверка рискового балла
        if (lastSnapshot.riskScore > 0.7) {
            return AwardingEligibilityResult(
                isEligible = false,
                reason = "High risk score requires manual approval",
                violations = listOf("Risk score ${lastSnapshot.riskScore} exceeds threshold 0.7"),
                requiresApproval = true
            )
        }

        return result
    }

    /**
     * Trust Token Burning - сжигание квоты при подтверждении ставки
     */
    fun burnCarrierQuota(
        carrierId: UUID,
        masterOrderId: UUID,
        bidVolume: BigDecimal
    ): ScmSnapshotEntity {
        logger.info("Burning quota for carrier $carrierId on order $masterOrderId. Volume: $bidVolume")

        // Получаем последний снимок SCM
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrderId)
        
        if (lastSnapshot == null) {
            throw ScmSnapshotNotFoundException("No SCM snapshot found for carrier $carrierId on order $masterOrderId")
        }

        // Обновляем квоту
        val updatedSnapshot = policyEvaluator.updateCarrierQuota(
            lastSnapshot,
            bidVolume,
            com.freight.marketplace.service.QuotaAction.BURN
        )

        // Сохраняем обновленный снимок
        val savedSnapshot = scmSnapshotRepository.save(updatedSnapshot)

        // Отправляем событие о сжигании квоты
        sendQuotaBurnEvent(savedSnapshot, bidVolume)

        return savedSnapshot
    }

    /**
     * Освобождение квоты при отмене ставки
     */
    fun releaseCarrierQuota(
        carrierId: UUID,
        masterOrderId: UUID,
        bidVolume: BigDecimal
    ): ScmSnapshotEntity {
        logger.info("Releasing quota for carrier $carrierId on order $masterOrderId. Volume: $bidVolume")

        // Получаем последний снимок SCM
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrderId)
        
        if (lastSnapshot == null) {
            throw ScmSnapshotNotFoundException("No SCM snapshot found for carrier $carrierId on order $masterOrderId")
        }

        // Обновляем квоту
        val updatedSnapshot = policyEvaluator.updateCarrierQuota(
            lastSnapshot,
            bidVolume,
            com.freight.marketplace.service.QuotaAction.RELEASE
        )

        // Сохраняем обновленный снимок
        val savedSnapshot = scmSnapshotRepository.save(updatedSnapshot)

        // Отправляем событие об освобождении квоты
        sendQuotaReleaseEvent(savedSnapshot, bidVolume)

        return savedSnapshot
    }

    /**
     * Проверка необходимости ручного аппрува из-за высокого рискового балла
     */
    fun checkManualApprovalRequired(
        carrierId: UUID,
        masterOrderId: UUID
    ): Boolean {
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrderId)
        
        return lastSnapshot?.let { policyEvaluator.requiresManualApproval(it) } ?: false
    }

    /**
     * Получение рекомендаций по улучшению соответствия
     */
    fun getComplianceRecommendations(
        carrierId: UUID,
        masterOrder: MasterOrderEntity
    ): List<String> {
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrder.id!!)
        
        return lastSnapshot?.let { 
            policyEvaluator.getComplianceRecommendations(carrierId, masterOrder, it)
        } ?: emptyList()
    }

    /**
     * Проверка наличия ADR лицензии у перевозчика
     */
    fun checkADRPermit(carrierId: UUID, masterOrderId: UUID): Boolean {
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrderId)
        
        return lastSnapshot?.let { snapshot ->
            snapshot.licenses?.contains("ADR_Permit") ?: false
        } ?: false
    }

    /**
     * Проверка рискового балла на критические значения
     */
    fun checkRiskScoreThreshold(carrierId: UUID, masterOrderId: UUID): RiskScoreCheckResult {
        val lastSnapshot = getLastComplianceSnapshot(carrierId, masterOrderId)
        
        return lastSnapshot?.let { snapshot ->
            when {
                snapshot.riskScore > 0.8 -> {
                    RiskScoreCheckResult(
                        isCritical = true,
                        riskScore = snapshot.riskScore,
                        message = "Critical risk score: ${snapshot.riskScore}"
                    )
                }
                snapshot.riskScore > 0.7 -> {
                    RiskScoreCheckResult(
                        isCritical = false,
                        riskScore = snapshot.riskScore,
                        message = "High risk score: ${snapshot.riskScore}, manual approval required"
                    )
                }
                snapshot.riskScore > 0.5 -> {
                    RiskScoreCheckResult(
                        isCritical = false,
                        riskScore = snapshot.riskScore,
                        message = "Elevated risk score: ${snapshot.riskScore}"
                    )
                }
                else -> {
                    RiskScoreCheckResult(
                        isCritical = false,
                        riskScore = snapshot.riskScore,
                        message = "Acceptable risk score: ${snapshot.riskScore}"
                    )
                }
            }
        } ?: RiskScoreCheckResult(
            isCritical = false,
            riskScore = 0.0,
            message = "No risk data available"
        )
    }

    /**
     * Получение истории соответствия перевозчика
     */
    fun getCarrierComplianceHistory(carrierId: UUID): List<ScmSnapshotEntity> {
        return scmSnapshotRepository.findByCarrierIdOrderByCreatedAtDesc(carrierId)
    }

    /**
     * Получение последнего снимка соответствия
     */
    fun getLastComplianceSnapshot(carrierId: UUID, masterOrderId: UUID): ScmSnapshotEntity? {
        return scmSnapshotRepository.findFirstByCarrierIdAndMasterOrderIdOrderByCreatedAtDesc(
            carrierId, 
            masterOrderId
        )
    }

    /**
     * Отправка события о сжигании квоты
     */
    private fun sendQuotaBurnEvent(snapshot: ScmSnapshotEntity, volume: BigDecimal) {
        val event = mapOf(
            "carrierId" to snapshot.carrierId,
            "masterOrderId" to snapshot.masterOrderId,
            "action" to "QUOTA_BURN",
            "volume" to volume,
            "newQuota" to snapshot.availableQuota,
            "timestamp" to LocalDateTime.now()
        )

        kafkaTemplate.send("scm.quota.burn", event)
    }

    /**
     * Отправка события об освобождении квоты
     */
    private fun sendQuotaReleaseEvent(snapshot: ScmSnapshotEntity, volume: BigDecimal) {
        val event = mapOf(
            "carrierId" to snapshot.carrierId,
            "masterOrderId" to snapshot.masterOrderId,
            "action" to "QUOTA_RELEASE",
            "volume" to volume,
            "newQuota" to snapshot.availableQuota,
            "timestamp" to LocalDateTime.now()
        )

        kafkaTemplate.send("scm.quota.release", event)
    }

    /**
     * Отправка события о ручном аппруве
     */
    fun sendManualApprovalRequiredEvent(
        carrierId: UUID,
        masterOrderId: UUID,
        riskScore: Double,
        reason: String
    ) {
        val event = mapOf(
            "carrierId" to carrierId,
            "masterOrderId" to masterOrderId,
            "riskScore" to riskScore,
            "reason" to reason,
            "timestamp" to LocalDateTime.now(),
            "type" to "MANUAL_APPROVAL_REQUIRED"
        )

        kafkaTemplate.send("scm.manual.approval", event)
    }

    /**
     * Отправка события о блокировке ставки
     */
    fun sendBidBlockedEvent(
        carrierId: UUID,
        masterOrderId: UUID,
        violations: List<String>
    ) {
        val event = mapOf(
            "carrierId" to carrierId,
            "masterOrderId" to masterOrderId,
            "violations" to violations,
            "timestamp" to LocalDateTime.now(),
            "type" to "BID_BLOCKED"
        )

        kafkaTemplate.send("scm.bid.blocked", event)
    }
}

/**
 * DTO для результата проверки рискового балла
 */
data class RiskScoreCheckResult(
    val isCritical: Boolean,
    val riskScore: Double,
    val message: String
)