package com.internal.services

import com.internal.integrations.SecurityEventBus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Интерфейс для внешних комплаенс-адаптеров
 * Реализует проверку контрагентов по санкционным спискам
 */
interface ExternalComplianceAdapter {
    /**
     * Проверка контрагента по санкционным спискам
     */
    fun checkSanctions(entityId: String, entityType: String): SanctionCheckResult
    
    /**
     * Проверка Denied Party Screening
     */
    fun checkDeniedParty(entityId: String, entityType: String): DeniedPartyCheckResult
    
    /**
     * Получение статуса проверки по ID
     */
    fun getCheckStatus(checkId: String): CheckStatus
}

/**
 * Реализация адаптера для OFAC/UN санкционных списков
 */
@Service
class OfacComplianceAdapter(
    private val securityEventBus: SecurityEventBus
) : ExternalComplianceAdapter {

    companion object {
        private const val OFAC_API_URL = "https://api.ofac.treasury.gov/sanctions"
        private const val UN_API_URL = "https://api.un.org/sanctions"
    }

    /**
     * Проверка по санкционным спискам OFAC/UN
     */
    @Transactional
    override fun checkSanctions(entityId: String, entityType: String): SanctionCheckResult {
        // В реальной системе здесь будет вызов внешнего API
        // Пока заглушка с имитацией проверки
        
        val checkResult = when (entityId) {
            "ENTITY-BLOCKED-OFAC" -> SanctionCheckResult(
                isBlocked = true,
                sanctionLists = listOf("OFAC-SDN", "UN-1267"),
                confidence = 0.95,
                details = "Entity found in OFAC SDN list"
            )
            "ENTITY-BLOCKED-UN" -> SanctionCheckResult(
                isBlocked = true,
                sanctionLists = listOf("UN-1267"),
                confidence = 0.90,
                details = "Entity found in UN 1267 list"
            )
            else -> SanctionCheckResult(
                isBlocked = false,
                sanctionLists = emptyList(),
                confidence = 1.0,
                details = "No matches found"
            )
        }

        // Отправляем событие о проверке
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "SANCTION_CHECK_${System.currentTimeMillis()}",
                eventType = "SANCTION_CHECK",
                timestamp = Instant.now(),
                userId = entityId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "entity_type" to entityType,
                    "is_blocked" to checkResult.isBlocked.toString(),
                    "confidence" to checkResult.confidence.toString(),
                    "sanction_lists" to checkResult.sanctionLists.joinToString(",")
                )
            )
        )

        return checkResult
    }

    /**
     * Проверка Denied Party Screening
     */
    @Transactional
    override fun checkDeniedParty(entityId: String, entityType: String): DeniedPartyCheckResult {
        // В реальной системе здесь будет вызов внешнего API
        // Пока заглушка
        
        val checkResult = when (entityId) {
            "DENIED-PARTY-1" -> DeniedPartyCheckResult(
                isDenied = true,
                deniedLists = listOf("BIS-DPL", "DDTC-DLL"),
                confidence = 0.85,
                details = "Entity found in BIS Denied Persons List"
            )
            else -> DeniedPartyCheckResult(
                isDenied = false,
                deniedLists = emptyList(),
                confidence = 1.0,
                details = "No matches found"
            )
        }

        // Отправляем событие о проверке
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "DENIED_PARTY_CHECK_${System.currentTimeMillis()}",
                eventType = "DENIED_PARTY_CHECK",
                timestamp = Instant.now(),
                userId = entityId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "entity_type" to entityType,
                    "is_denied" to checkResult.isDenied.toString(),
                    "confidence" to checkResult.confidence.toString(),
                    "denied_lists" to checkResult.deniedLists.joinToString(",")
                )
            )
        )

        return checkResult
    }

    /**
     * Получение статуса проверки
     */
    override fun getCheckStatus(checkId: String): CheckStatus {
        // В реальной системе здесь будет проверка статуса асинхронной проверки
        return CheckStatus(
            checkId = checkId,
            status = "COMPLETED",
            completedAt = Instant.now(),
            result = "SUCCESS"
        )
    }
}

/**
 * Реализация адаптера для ITAR/EAR проверок
 */
@Service
class ItarComplianceAdapter(
    private val securityEventBus: SecurityEventBus
) : ExternalComplianceAdapter {

    companion object {
        private const val ITAR_API_URL = "https://api.pmddtc.state.gov/itar"
        private const val EAR_API_URL = "https://api.bis.doc.gov/ear"
    }

    /**
     * Проверка ITAR/EAR ограничений
     */
    @Transactional
    override fun checkSanctions(entityId: String, entityType: String): SanctionCheckResult {
        // Проверка ITAR (International Traffic in Arms Regulations)
        val itarResult = checkItarRestrictions(entityId, entityType)
        
        // Проверка EAR (Export Administration Regulations)
        val earResult = checkEarRestrictions(entityId, entityType)

        val isBlocked = itarResult.isBlocked || earResult.isBlocked
        val sanctionLists = mutableListOf<String>()
        sanctionLists.addAll(itarResult.sanctionLists)
        sanctionLists.addAll(earResult.sanctionLists)

        val finalResult = SanctionCheckResult(
            isBlocked = isBlocked,
            sanctionLists = sanctionLists,
            confidence = if (isBlocked) 0.9 else 1.0,
            details = if (isBlocked) "ITAR/EAR restrictions detected" else "No restrictions"
        )

        // Отправляем событие
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "ITAR_EAR_CHECK_${System.currentTimeMillis()}",
                eventType = "ITAR_EAR_CHECK",
                timestamp = Instant.now(),
                userId = entityId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "entity_type" to entityType,
                    "itar_blocked" to itarResult.isBlocked.toString(),
                    "ear_blocked" to earResult.isBlocked.toString(),
                    "confidence" to finalResult.confidence.toString()
                )
            )
        )

        return finalResult
    }

    /**
     * Проверка ITAR ограничений
     */
    private fun checkItarRestrictions(entityId: String, entityType: String): SanctionCheckResult {
        // В реальной системе здесь будет вызов ITAR API
        return when (entityId) {
            "ITAR-RESTRICTED-1" -> SanctionCheckResult(
                isBlocked = true,
                sanctionLists = listOf("ITAR-USML"),
                confidence = 0.95,
                details = "Entity restricted under ITAR USML"
            )
            else -> SanctionCheckResult(
                isBlocked = false,
                sanctionLists = emptyList(),
                confidence = 1.0,
                details = "No ITAR restrictions"
            )
        }
    }

    /**
     * Проверка EAR ограничений
     */
    private fun checkEarRestrictions(entityId: String, entityType: String): SanctionCheckResult {
        // В реальной системе здесь будет вызов EAR API
        return when (entityId) {
            "EAR-RESTRICTED-1" -> SanctionCheckResult(
                isBlocked = true,
                sanctionLists = listOf("EAR-CCL"),
                confidence = 0.90,
                details = "Entity restricted under EAR CCL"
            )
            else -> SanctionCheckResult(
                isBlocked = false,
                sanctionLists = emptyList(),
                confidence = 1.0,
                details = "No EAR restrictions"
            )
        }
    }

    /**
     * Проверка Denied Party Screening для ITAR/EAR
     */
    @Transactional
    override fun checkDeniedParty(entityId: String, entityType: String): DeniedPartyCheckResult {
        // В реальной системе здесь будет вызов соответствующих API
        return DeniedPartyCheckResult(
            isDenied = false,
            deniedLists = emptyList(),
            confidence = 1.0,
            details = "No ITAR/EAR denied party matches"
        )
    }

    /**
     * Получение статуса проверки
     */
    override fun getCheckStatus(checkId: String): CheckStatus {
        return CheckStatus(
            checkId = checkId,
            status = "COMPLETED",
            completedAt = Instant.now(),
            result = "SUCCESS"
        )
    }
}

/**
 * Модели для комплаенс-проверок
 */

data class SanctionCheckResult(
    val isBlocked: Boolean,
    val sanctionLists: List<String>,
    val confidence: Double,
    val details: String
)

data class DeniedPartyCheckResult(
    val isDenied: Boolean,
    val deniedLists: List<String>,
    val confidence: Double,
    val details: String
)

data class CheckStatus(
    val checkId: String,
    val status: String, // PENDING, COMPLETED, FAILED
    val completedAt: Instant?,
    val result: String // SUCCESS, FAILED, TIMEOUT
)

/**
 * Модель для хранения результатов проверок
 */
data class ComplianceCheckRecord(
    val id: String? = null,
    val entityId: String,
    val entityType: String,
    val checkType: String, // SANCTION, DENIED_PARTY, ITAR, EAR
    val result: String, // ALLOWED, BLOCKED
    val confidence: Double,
    val details: String,
    val checkedAt: Instant,
    val source: String // OFAC, UN, ITAR, EAR
) <environment_details>
# Visual Studio Code - Insiders Visible Files
(No visible files)

# Visual Studio Code - Insiders Open Tabs
(No open tabs)

# Current Time
2/3/2026, 11:13:30 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
30,189 / 256K tokens used (12%)

# Current Mode
ACT MODE
</environment_details>