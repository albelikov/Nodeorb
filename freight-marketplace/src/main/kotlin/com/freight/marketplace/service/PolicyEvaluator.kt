package com.freight.marketplace.service

import com.freight.marketplace.entity.*
import com.freight.marketplace.exception.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import java.util.logging.Logger

/**
 * Сервис оценки бизнес-правил на основе типов груза и данных SCM
 */
@Service
class PolicyEvaluator {

    companion object {
        private val logger = Logger.getLogger(PolicyEvaluator::class.java.name)
        private const val HIGH_RISK_THRESHOLD = 0.7
        private const val ADR_LICENSE_REQUIRED = "ADR_Permit"
    }

    /**
     * Оценка соответствия перевозчика требованиям заказа
     */
    fun evaluateCarrierCompliance(
        carrierId: UUID,
        masterOrder: MasterOrderEntity,
        scmSnapshot: ScmSnapshotEntity
    ): ComplianceEvaluationResult {
        logger.info("Evaluating compliance for carrier $carrierId on order ${masterOrder.id}")

        val violations = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Проверка ADR лицензии для опасных грузов
        if (masterOrder.cargoType == CargoType.DANGEROUS) {
            val adrCompliance = checkADRCompliance(carrierId, scmSnapshot)
            if (!adrCompliance.isCompliant) {
                violations.add(adrCompliance.violationMessage)
            }
        }

        // Проверка рискового балла
        val riskCompliance = checkRiskCompliance(carrierId, scmSnapshot)
        if (!riskCompliance.isCompliant) {
            violations.add(riskCompliance.violationMessage)
        } else if (riskCompliance.requiresApproval) {
            warnings.add(riskCompliance.warningMessage)
        }

        // Проверка квоты перевозчика
        val quotaCompliance = checkQuotaCompliance(carrierId, masterOrder, scmSnapshot)
        if (!quotaCompliance.isCompliant) {
            violations.add(quotaCompliance.violationMessage)
        }

        val status = when {
            violations.isNotEmpty() -> ComplianceStatus.VIOLATION
            warnings.isNotEmpty() -> ComplianceStatus.WARNING
            else -> ComplianceStatus.COMPLIANT
        }

        return ComplianceEvaluationResult(
            carrierId = carrierId,
            masterOrderId = masterOrder.id!!,
            status = status,
            violations = violations,
            warnings = warnings,
            riskScore = scmSnapshot.riskScore,
            availableQuota = scmSnapshot.availableQuota,
            requiredQuota = masterOrder.totalVolume
        )
    }

    /**
     * Проверка наличия ADR лицензии для опасных грузов
     */
    private fun checkADRCompliance(carrierId: UUID, scmSnapshot: ScmSnapshotEntity): ComplianceCheck {
        if (scmSnapshot.licenses == null || !scmSnapshot.licenses.contains(ADR_LICENSE_REQUIRED)) {
            return ComplianceCheck(
                isCompliant = false,
                violationMessage = "Carrier $carrierId does not have required ADR permit for hazardous cargo"
            )
        }

        logger.info("Carrier $carrierId has ADR permit")
        return ComplianceCheck(isCompliant = true)
    }

    /**
     * Проверка рискового балла
     */
    private fun checkRiskCompliance(carrierId: UUID, scmSnapshot: ScmSnapshotEntity): ComplianceCheck {
        val riskScore = scmSnapshot.riskScore

        return when {
            riskScore > HIGH_RISK_THRESHOLD -> {
                ComplianceCheck(
                    isCompliant = false,
                    violationMessage = "Carrier $carrierId has high risk score ($riskScore) exceeding threshold ($HIGH_RISK_THRESHOLD)"
                )
            }
            riskScore > 0.5 -> {
                ComplianceCheck(
                    isCompliant = true,
                    requiresApproval = true,
                    warningMessage = "Carrier $carrierId has elevated risk score ($riskScore), manual approval required"
                )
            }
            else -> {
                ComplianceCheck(isCompliant = true)
            }
        }
    }

    /**
     * Проверка квоты перевозчика
     */
    private fun checkQuotaCompliance(
        carrierId: UUID,
        masterOrder: MasterOrderEntity,
        scmSnapshot: ScmSnapshotEntity
    ): ComplianceCheck {
        val requiredVolume = masterOrder.totalVolume
        val availableQuota = scmSnapshot.availableQuota

        if (availableQuota < requiredVolume) {
            return ComplianceCheck(
                isCompliant = false,
                violationMessage = "Carrier $carrierId does not have sufficient quota. Required: $requiredVolume, Available: $availableQuota"
            )
        }

        logger.info("Carrier $carrierId has sufficient quota: $availableQuota >= $requiredVolume")
        return ComplianceCheck(isCompliant = true)
    }

    /**
     * Проверка возможности подтверждения ставки (Awarding)
     */
    fun validateAwardingEligibility(
        carrierId: UUID,
        masterOrder: MasterOrderEntity,
        scmSnapshot: ScmSnapshotEntity
    ): AwardingEligibilityResult {
        logger.info("Validating awarding eligibility for carrier $carrierId on order ${masterOrder.id}")

        val complianceResult = evaluateCarrierCompliance(carrierId, masterOrder, scmSnapshot)

        return when (complianceResult.status) {
            ComplianceStatus.VIOLATION -> {
                AwardingEligibilityResult(
                    isEligible = false,
                    reason = "Compliance violations found",
                    violations = complianceResult.violations,
                    requiresApproval = false
                )
            }
            ComplianceStatus.WARNING -> {
                AwardingEligibilityResult(
                    isEligible = false,
                    reason = "High risk score requires manual approval",
                    violations = emptyList(),
                    requiresApproval = true
                )
            }
            ComplianceStatus.COMPLIANT -> {
                AwardingEligibilityResult(
                    isEligible = true,
                    reason = "All compliance checks passed",
                    violations = emptyList(),
                    requiresApproval = false
                )
            }
        }
    }

    /**
     * Проверка возможности подачи ставки
     */
    fun validateBidEligibility(
        carrierId: UUID,
        masterOrder: MasterOrderEntity,
        scmSnapshot: ScmSnapshotEntity
    ): BidEligibilityResult {
        logger.info("Validating bid eligibility for carrier $carrierId on order ${masterOrder.id}")

        val complianceResult = evaluateCarrierCompliance(carrierId, masterOrder, scmSnapshot)

        return when (complianceResult.status) {
            ComplianceStatus.VIOLATION -> {
                BidEligibilityResult(
                    isEligible = false,
                    reason = "Compliance violations prevent bidding",
                    violations = complianceResult.violations
                )
            }
            ComplianceStatus.WARNING -> {
                BidEligibilityResult(
                    isEligible = true, // Предупреждения не блокируют подачу ставки
                    reason = "Bid allowed with warnings",
                    violations = emptyList()
                )
            }
            ComplianceStatus.COMPLIANT -> {
                BidEligibilityResult(
                    isEligible = true,
                    reason = "All compliance checks passed",
                    violations = emptyList()
                )
            }
        }
    }

    /**
     * Обновление квоты перевозчика после подтверждения ставки
     */
    fun updateCarrierQuota(
        scmSnapshot: ScmSnapshotEntity,
        bidVolume: BigDecimal,
        action: QuotaAction
    ): ScmSnapshotEntity {
        logger.info("Updating carrier quota for snapshot ${scmSnapshot.id}. Action: $action, Volume: $bidVolume")

        val currentQuota = scmSnapshot.availableQuota
        val newQuota = when (action) {
            QuotaAction.BURN -> {
                if (currentQuota < bidVolume) {
                    throw InsufficientQuotaException("Insufficient quota for carrier ${scmSnapshot.carrierId}. Current: $currentQuota, Required: $bidVolume")
                }
                currentQuota - bidVolume
            }
            QuotaAction.RELEASE -> currentQuota + bidVolume
            QuotaAction.ALLOCATE -> currentQuota - bidVolume
        }

        scmSnapshot.availableQuota = newQuota
        scmSnapshot.quotaHistory.add(QuotaHistoryEntry(
            action = action,
            volume = bidVolume,
            previousQuota = currentQuota,
            newQuota = newQuota,
            timestamp = java.time.LocalDateTime.now()
        ))

        logger.info("Carrier quota updated: ${scmSnapshot.carrierId}. New quota: $newQuota")
        return scmSnapshot
    }

    /**
     * Проверка необходимости ручного аппрува
     */
    fun requiresManualApproval(scmSnapshot: ScmSnapshotEntity): Boolean {
        return scmSnapshot.riskScore > HIGH_RISK_THRESHOLD
    }

    /**
     * Получение рекомендаций по улучшению соответствия
     */
    fun getComplianceRecommendations(
        carrierId: UUID,
        masterOrder: MasterOrderEntity,
        scmSnapshot: ScmSnapshotEntity
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Рекомендации по ADR лицензии
        if (masterOrder.cargoType == CargoType.DANGEROUS && 
            (scmSnapshot.licenses == null || !scmSnapshot.licenses.contains(ADR_LICENSE_REQUIRED))) {
            recommendations.add("Obtain ADR permit to handle hazardous cargo")
        }

        // Рекомендации по рисковому баллу
        if (scmSnapshot.riskScore > 0.5) {
            recommendations.add("Improve compliance score to reduce risk rating")
            recommendations.add("Review and update security protocols")
        }

        // Рекомендации по квоте
        if (scmSnapshot.availableQuota < masterOrder.totalVolume) {
            recommendations.add("Increase carrier quota to handle larger orders")
            recommendations.add("Consider splitting order into smaller partial orders")
        }

        return recommendations
    }
}

/**
 * DTO для результата оценки соответствия
 */
data class ComplianceEvaluationResult(
    val carrierId: UUID,
    val masterOrderId: UUID,
    val status: ComplianceStatus,
    val violations: List<String>,
    val warnings: List<String>,
    val riskScore: Double,
    val availableQuota: BigDecimal,
    val requiredQuota: BigDecimal
)

/**
 * DTO для результата проверки возможности подачи ставки
 */
data class BidEligibilityResult(
    val isEligible: Boolean,
    val reason: String,
    val violations: List<String>
)

/**
 * DTO для результата проверки возможности подтверждения ставки
 */
data class AwardingEligibilityResult(
    val isEligible: Boolean,
    val reason: String,
    val violations: List<String>,
    val requiresApproval: Boolean
)

/**
 * DTO для проверки соответствия
 */
data class ComplianceCheck(
    val isCompliant: Boolean,
    val requiresApproval: Boolean = false,
    val violationMessage: String? = null,
    val warningMessage: String? = null
)

/**
 * DTO для истории квоты
 */
data class QuotaHistoryEntry(
    val action: QuotaAction,
    val volume: BigDecimal,
    val previousQuota: BigDecimal,
    val newQuota: BigDecimal,
    val timestamp: java.time.LocalDateTime
)

/**
 * Действия с квотой
 */
enum class QuotaAction {
    BURN,      // Сжигание квоты при подтверждении ставки
    RELEASE,   // Освобождение квоты при отмене
    ALLOCATE   // Резервирование квоты
}

/**
 * Статус соответствия
 */
enum class ComplianceStatus {
    COMPLIANT,  // Соответствует всем требованиям
    WARNING,    // Есть предупреждения, но не блокирует
    VIOLATION   // Нарушения, блокируют операции
}