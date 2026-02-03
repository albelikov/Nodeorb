package com.internal.services

import com.internal.integrations.SecurityEventBus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Сервис Compliance Oracles для проверки контрагентов по санкционным спискам
 * Реализует адаптеры для проверки контрагентов по санкционным спискам (Sanction Screening)
 * Это становится частью процесса авторизации каждой сделки
 */
@Service
class ComplianceOraclesService(
    private val securityEventBus: SecurityEventBus
) {

    companion object {
        private const val OFAC_API_URL = "https://api.ofac.treasury.gov/sanctions"
        private const val UN_API_URL = "https://api.un.org/sanctions"
        private const val BIS_API_URL = "https://api.bis.doc.gov/dpl"
        private const val DDTC_API_URL = "https://api.pmddtc.state.gov/dll"
    }

    /**
     * Проверка контрагента по санкционным спискам
     */
    @Transactional
    fun checkSanctions(
        entityId: String,
        entityType: String,
        entityName: String,
        entityCountry: String? = null
    ): SanctionCheckResult {
        // В реальной системе здесь будет вызов внешних API
        // Пока заглушка с имитацией проверки
        
        val checkResult = when (entityId) {
            "ENTITY-BLOCKED-OFAC" -> SanctionCheckResult(
                isBlocked = true,
                sanctionLists = listOf("OFAC-SDN", "UN-1267"),
                confidence = 0.95,
                details = "Entity found in OFAC SDN list",
                blockedBy = "OFAC"
            )
            "ENTITY-BLOCKED-UN" -> SanctionCheckResult(
                isBlocked = true,
                sanctionLists = listOf("UN-1267"),
                confidence = 0.90,
                details = "Entity found in UN 1267 list",
                blockedBy = "UN"
            )
            "ENTITY-BLOCKED-BIS" -> SanctionCheckResult(
                isBlocked = true,
                sanctionLists = listOf("BIS-DPL"),
                confidence = 0.85,
                details = "Entity found in BIS Denied Persons List",
                blockedBy = "BIS"
            )
            else -> SanctionCheckResult(
                isBlocked = false,
                sanctionLists = emptyList(),
                confidence = 1.0,
                details = "No matches found in sanction lists",
                blockedBy = null
            )
        }

        // Отправляем событие о проверке
        sendSanctionCheckEvent(entityId, entityType, checkResult)

        return checkResult
    }

    /**
     * Проверка Denied Party Screening
     */
    @Transactional
    fun checkDeniedParty(
        entityId: String,
        entityType: String,
        entityName: String,
        entityCountry: String? = null
    ): DeniedPartyCheckResult {
        // В реальной системе здесь будет вызов внешних API
        // Пока заглушка
        
        val checkResult = when (entityId) {
            "DENIED-PARTY-1" -> DeniedPartyCheckResult(
                isDenied = true,
                deniedLists = listOf("BIS-DPL", "DDTC-DLL"),
                confidence = 0.85,
                details = "Entity found in BIS Denied Persons List",
                deniedBy = "BIS"
            )
            "DENIED-PARTY-2" -> DeniedPartyCheckResult(
                isDenied = true,
                deniedLists = listOf("DDTC-DLL"),
                confidence = 0.90,
                details = "Entity found in DDTC Denied List",
                deniedBy = "DDTC"
            )
            else -> DeniedPartyCheckResult(
                isDenied = false,
                deniedLists = emptyList(),
                confidence = 1.0,
                details = "No matches found in denied party lists",
                deniedBy = null
            )
        }

        // Отправляем событие о проверке
        sendDeniedPartyCheckEvent(entityId, entityType, checkResult)

        return checkResult
    }

    /**
     * Проверка ITAR/EAR ограничений
     */
    @Transactional
    fun checkItarEar(
        entityId: String,
        entityType: String,
        entityName: String,
        entityCountry: String? = null
    ): ItarEarCheckResult {
        // Проверка ITAR (International Traffic in Arms Regulations)
        val itarResult = checkItarRestrictions(entityId, entityType, entityName, entityCountry)
        
        // Проверка EAR (Export Administration Regulations)
        val earResult = checkEarRestrictions(entityId, entityType, entityName, entityCountry)

        val isRestricted = itarResult.isRestricted || earResult.isRestricted
        val restrictionLists = mutableListOf<String>()
        restrictionLists.addAll(itarResult.restrictionLists)
        restrictionLists.addAll(earResult.restrictionLists)

        val finalResult = ItarEarCheckResult(
            isRestricted = isRestricted,
            restrictionLists = restrictionLists,
            confidence = if (isRestricted) 0.9 else 1.0,
            details = if (isRestricted) "ITAR/EAR restrictions detected" else "No restrictions",
            restrictedBy = if (isRestricted) {
                if (itarResult.isRestricted) "ITAR" else "EAR"
            } else null
        )

        // Отправляем событие
        sendItarEarCheckEvent(entityId, entityType, finalResult)

        return finalResult
    }

    /**
     * Проверка ITAR ограничений
     */
    private fun checkItarRestrictions(
        entityId: String,
        entityType: String,
        entityName: String,
        entityCountry: String?
    ): ItarCheckResult {
        // В реальной системе здесь будет вызов ITAR API
        return when (entityId) {
            "ITAR-RESTRICTED-1" -> ItarCheckResult(
                isRestricted = true,
                restrictionLists = listOf("ITAR-USML"),
                confidence = 0.95,
                details = "Entity restricted under ITAR USML",
                restrictedBy = "ITAR"
            )
            else -> ItarCheckResult(
                isRestricted = false,
                restrictionLists = emptyList(),
                confidence = 1.0,
                details = "No ITAR restrictions",
                restrictedBy = null
            )
        }
    }

    /**
     * Проверка EAR ограничений
     */
    private fun checkEarRestrictions(
        entityId: String,
        entityType: String,
        entityName: String,
        entityCountry: String?
    ): EarCheckResult {
        // В реальной системе здесь будет вызов EAR API
        return when (entityId) {
            "EAR-RESTRICTED-1" -> EarCheckResult(
                isRestricted = true,
                restrictionLists = listOf("EAR-CCL"),
                confidence = 0.90,
                details = "Entity restricted under EAR CCL",
                restrictedBy = "EAR"
            )
            else -> EarCheckResult(
                isRestricted = false,
                restrictionLists = emptyList(),
                confidence = 1.0,
                details = "No EAR restrictions",
                restrictedBy = null
            )
        }
    }

    /**
     * Комплексная проверка контрагента для авторизации сделки
     */
    @Transactional
    fun performComprehensiveComplianceCheck(
        entityId: String,
        entityType: String,
        entityName: String,
        entityCountry: String? = null
    ): ComprehensiveComplianceCheckResult {
        // Выполняем все проверки
        val sanctionCheck = checkSanctions(entityId, entityType, entityName, entityCountry)
        val deniedPartyCheck = checkDeniedParty(entityId, entityType, entityName, entityCountry)
        val itarEarCheck = checkItarEar(entityId, entityType, entityName, entityCountry)

        // Определяем общий результат
        val isCompliant = !sanctionCheck.isBlocked && 
                         !deniedPartyCheck.isDenied && 
                         !itarEarCheck.isRestricted

        val complianceResult = ComprehensiveComplianceCheckResult(
            entityId = entityId,
            entityType = entityType,
            isCompliant = isCompliant,
            sanctionCheck = sanctionCheck,
            deniedPartyCheck = deniedPartyCheck,
            itarEarCheck = itarEarCheck,
            overallConfidence = calculateOverallConfidence(sanctionCheck, deniedPartyCheck, itarEarCheck),
            checkedAt = Instant.now()
        )

        // Отправляем комплексное событие
        sendComprehensiveCheckEvent(complianceResult)

        return complianceResult
    }

    /**
     * Расчет общего уровня доверия
     */
    private fun calculateOverallConfidence(
        sanctionCheck: SanctionCheckResult,
        deniedPartyCheck: DeniedPartyCheckResult,
        itarEarCheck: ItarEarCheckResult
    ): Double {
        val sanctionConfidence = if (sanctionCheck.isBlocked) 0.0 else sanctionCheck.confidence
        val deniedConfidence = if (deniedPartyCheck.isDenied) 0.0 else deniedPartyCheck.confidence
        val itarEarConfidence = if (itarEarCheck.isRestricted) 0.0 else itarEarCheck.confidence

        return (sanctionConfidence + deniedConfidence + itarEarConfidence) / 3.0
    }

    /**
     * Отправка события о проверке санкций
     */
    private fun sendSanctionCheckEvent(
        entityId: String,
        entityType: String,
        result: SanctionCheckResult
    ) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "SANCTION_CHECK_${System.currentTimeMillis()}",
                eventType = "SANCTION_CHECK",
                timestamp = Instant.now(),
                userId = entityId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "entity_type" to entityType,
                    "is_blocked" to result.isBlocked.toString(),
                    "confidence" to result.confidence.toString(),
                    "sanction_lists" to result.sanctionLists.joinToString(","),
                    "blocked_by" to (result.blockedBy ?: ""),
                    "details" to result.details
                )
            )
        )
    }

    /**
     * Отправка события о проверке Denied Party
     */
    private fun sendDeniedPartyCheckEvent(
        entityId: String,
        entityType: String,
        result: DeniedPartyCheckResult
    ) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "DENIED_PARTY_CHECK_${System.currentTimeMillis()}",
                eventType = "DENIED_PARTY_CHECK",
                timestamp = Instant.now(),
                userId = entityId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "entity_type" to entityType,
                    "is_denied" to result.isDenied.toString(),
                    "confidence" to result.confidence.toString(),
                    "denied_lists" to result.deniedLists.joinToString(","),
                    "denied_by" to (result.deniedBy ?: ""),
                    "details" to result.details
                )
            )
        )
    }

    /**
     * Отправка события о проверке ITAR/EAR
     */
    private fun sendItarEarCheckEvent(
        entityId: String,
        entityType: String,
        result: ItarEarCheckResult
    ) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "ITAR_EAR_CHECK_${System.currentTimeMillis()}",
                eventType = "ITAR_EAR_CHECK",
                timestamp = Instant.now(),
                userId = entityId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "entity_type" to entityType,
                    "is_restricted" to result.isRestricted.toString(),
                    "confidence" to result.confidence.toString(),
                    "restriction_lists" to result.restrictionLists.joinToString(","),
                    "restricted_by" to (result.restrictedBy ?: ""),
                    "details" to result.details
                )
            )
        )
    }

    /**
     * Отправка комплексного события о проверке
     */
    private fun sendComprehensiveCheckEvent(result: ComprehensiveComplianceCheckResult) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "COMPREHENSIVE_COMPLIANCE_CHECK_${System.currentTimeMillis()}",
                eventType = "COMPREHENSIVE_COMPLIANCE_CHECK",
                timestamp = result.checkedAt,
                userId = result.entityId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "entity_type" to result.entityType,
                    "is_compliant" to result.isCompliant.toString(),
                    "overall_confidence" to result.overallConfidence.toString(),
                    "sanction_blocked" to result.sanctionCheck.isBlocked.toString(),
                    "denied_party" to result.deniedPartyCheck.isDenied.toString(),
                    "itar_ear_restricted" to result.itarEarCheck.isRestricted.toString()
                )
            )
        )
    }
}

/**
 * Модели для Compliance Oracles
 */

data class SanctionCheckResult(
    val isBlocked: Boolean,
    val sanctionLists: List<String>,
    val confidence: Double,
    val details: String,
    val blockedBy: String?
)

data class DeniedPartyCheckResult(
    val isDenied: Boolean,
    val deniedLists: List<String>,
    val confidence: Double,
    val details: String,
    val deniedBy: String?
)

data class ItarCheckResult(
    val isRestricted: Boolean,
    val restrictionLists: List<String>,
    val confidence: Double,
    val details: String,
    val restrictedBy: String?
)

data class EarCheckResult(
    val isRestricted: Boolean,
    val restrictionLists: List<String>,
    val confidence: Double,
    val details: String,
    val restrictedBy: String?
)

data class ItarEarCheckResult(
    val isRestricted: Boolean,
    val restrictionLists: List<String>,
    val confidence: Double,
    val details: String,
    val restrictedBy: String?
)

data class ComprehensiveComplianceCheckResult(
    val entityId: String,
    val entityType: String,
    val isCompliant: Boolean,
    val sanctionCheck: SanctionCheckResult,
    val deniedPartyCheck: DeniedPartyCheckResult,
    val itarEarCheck: ItarEarCheckResult,
    val overallConfidence: Double,
    val checkedAt: Instant
)

data class SecurityEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: String,
    val sourceService: String,
    val details: Map<String, String>
) <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/DynamicGeofencingService.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt
scm-service/src/main/kotlin/com/internal/services/DynamicGeofencingService.kt
scm-service/src/main/kotlin/com/internal/services/ComplianceOraclesService.kt

# Current Time
2/3/2026, 11:27:37 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
96,129 / 256K tokens used (38%)

# Current Mode
ACT MODE
</environment_details>