package com.internal.engine.validation

import com.internal.repository.ComplianceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Проверка санкционных списков и Denied Party Screening
 * Автоматическая проверка контрагентов по международным санкционным спискам
 */
@Service
class SanctionChecker(
    private val complianceRepository: ComplianceRepository
) {

    companion object {
        private val SANCTION_LISTS = listOf(
            "OFAC", "EU", "UN", "UK", "CA", "AU", "JP"
        )
    }

    /**
     * Проверка пользователя на наличие в санкционных списках
     */
    @Transactional(readOnly = true)
    fun checkSanctions(userId: String): SanctionCheckResult {
        val passport = complianceRepository.getCompliancePassport(userId)
            ?: return SanctionCheckResult(
                isSanctioned = false,
                sanctionLists = emptyList(),
                lastCheck = Instant.now(),
                riskLevel = "LOW"
            )

        // Проверяем данные пользователя по санкционным спискам
        val sanctionMatches = checkAgainstSanctionLists(passport)
        
        return SanctionCheckResult(
            isSanctioned = sanctionMatches.isNotEmpty(),
            sanctionLists = sanctionMatches,
            lastCheck = Instant.now(),
            riskLevel = determineRiskLevel(sanctionMatches)
        )
    }

    /**
     * Проверка компании на наличие в санкционных списках
     */
    @Transactional(readOnly = true)
    fun checkCompanySanctions(companyId: String): SanctionCheckResult {
        // В реальной системе здесь будет проверка по базе данных компаний
        // Пока заглушка
        return SanctionCheckResult(
            isSanctioned = false,
            sanctionLists = emptyList(),
            lastCheck = Instant.now(),
            riskLevel = "LOW"
        )
    }

    /**
     * Проверка по всем санкционным спискам
     */
    private fun checkAgainstSanctionLists(passport: CompliancePassport): List<String> {
        val matches = mutableListOf<String>()
        
        // В реальной системе здесь будет интеграция с внешними API санкционных списков
        // Пока заглушка - проверяем по внутренним данным
        
        val fullName = passport.verificationData["full_name"] as? String ?: ""
        val company = passport.verificationData["company"] as? String ?: ""
        val country = passport.verificationData["country"] as? String ?: ""
        
        // Пример проверки (в реальной системе будет сложнее)
        if (fullName.contains("Sanctioned", ignoreCase = true)) {
            matches.add("OFAC")
        }
        
        if (country in listOf("Country1", "Country2")) {
            matches.add("UN")
        }
        
        return matches
    }

    /**
     * Определение уровня риска на основе санкционных списков
     */
    private fun determineRiskLevel(matches: List<String>): String {
        return when {
            matches.contains("OFAC") || matches.contains("UN") -> "CRITICAL"
            matches.contains("EU") || matches.contains("UK") -> "HIGH"
            matches.isNotEmpty() -> "MEDIUM"
            else -> "LOW"
        }
    }

    /**
     * Проверка страны на санкции
     */
    fun checkCountrySanctions(countryCode: String): CountrySanctionResult {
        // В реальной системе здесь будет проверка по актуальным данным
        val sanctionedCountries = setOf("RU", "IR", "KP", "SY")
        
        return CountrySanctionResult(
            isSanctioned = countryCode in sanctionedCountries,
            sanctionLists = if (countryCode in sanctionedCountries) listOf("UN", "OFAC") else emptyList(),
            restrictions = getCountryRestrictions(countryCode)
        )
    }

    /**
     * Получение ограничений для страны
     */
    private fun getCountryRestrictions(countryCode: String): List<String> {
        return when (countryCode) {
            "RU" -> listOf("Financial restrictions", "Technology export ban", "Energy sector restrictions")
            "IR" -> listOf("Nuclear program restrictions", "Financial sanctions", "Arms embargo")
            "KP" -> listOf("Weapons embargo", "Luxury goods ban", "Financial restrictions")
            "SY" -> listOf("Arms embargo", "Financial sanctions", "Travel restrictions")
            else -> emptyList()
        }
    }

    /**
     * Проверка контрагента (пользователя или компании) на санкции
     */
    fun checkCounterparty(counterpartyId: String, counterpartyType: String): SanctionCheckResult {
        return when (counterpartyType.lowercase()) {
            "user" -> checkSanctions(counterpartyId)
            "company" -> checkCompanySanctions(counterpartyId)
            else -> SanctionCheckResult(
                isSanctioned = false,
                sanctionLists = emptyList(),
                lastCheck = Instant.now(),
                riskLevel = "LOW"
            )
        }
    }
}

/**
 * Результат проверки на санкции
 */
data class SanctionCheckResult(
    val isSanctioned: Boolean,
    val sanctionLists: List<String>,
    val lastCheck: Instant,
    val riskLevel: String
)

/**
 * Результат проверки страны на санкции
 */
data class CountrySanctionResult(
    val isSanctioned: Boolean,
    val sanctionLists: List<String>,
    val restrictions: List<String>
)