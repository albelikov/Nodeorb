package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.UserProfileRepository
import com.nodeorb.freight.marketplace.repository.ScmSnapshotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.logging.Logger

/**
 * Сервис проверки соответствия (Compliance Service)
 * Реализует логику проверки Compliance Passport перевозчика
 */
@Service
@Transactional
class ComplianceService(
    private val userProfileRepository: UserProfileRepository,
    private val scmSnapshotRepository: ScmSnapshotRepository
) {

    companion object {
        private val logger = Logger.getLogger(ComplianceService::class.java.name)
    }

    /**
     * Проверка Compliance Passport перевозчика
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

        // Основные проверки соответствия
        val complianceResult = performBasicComplianceChecks(userProfile, cargoType, route)
        
        // Проверка лицензий и сертификатов
        val licenseResult = checkLicensesAndCertificates(userProfile, cargoType)
        
        // Проверка финансовой стабильности
        val financialResult = checkFinancialStability(userProfile)
        
        // Проверка истории инцидентов
        val incidentResult = checkIncidentHistory(userProfile)
        
        // Комбинируем все результаты
        val finalResult = combineComplianceResults(
            complianceResult, licenseResult, financialResult, incidentResult
        )

        // Создаем SCM снимок
        val scmSnapshot = createComplianceSnapshot(
            carrierId, masterOrderId, finalResult
        )

        return ComplianceCheckResult(
            carrierId = carrierId,
            masterOrderId = masterOrderId,
            bidId = UUID.randomUUID(), // Временный ID, будет обновлен позже
            complianceStatus = finalResult.status,
            complianceDetails = finalResult.details,
            securityClearance = SecurityLevel.NONE.name, // Будет определен позже
            securityDetails = "Security check pending",
            riskScore = calculateRiskScore(userProfile, cargoType, route),
            riskFactors = finalResult.riskFactors,
            trustToken = null,
            auditTrail = buildComplianceAuditTrail(finalResult, scmSnapshot)
        )
    }

    /**
     * Основные проверки соответствия
     */
    private fun performBasicComplianceChecks(
        userProfile: UserProfileEntity,
        cargoType: String,
        route: RouteInfo
    ): ComplianceCheck {
        val issues = mutableListOf<String>()
        var status = ComplianceStatus.COMPLIANT

        // Проверка опыта работы
        if (userProfile.totalOrders < 10) {
            issues.add("Insufficient order history (${userProfile.totalOrders} orders)")
            status = ComplianceStatus.NON_COMPLIANT
        }

        // Проверка рейтинга
        if (userProfile.rating < 3.0) {
            issues.add("Low rating (${userProfile.rating}/5.0)")
            status = ComplianceStatus.NON_COMPLIANT
        }

        // Проверка коэффициента выполнения заказов
        val completionRate = userProfile.completedOrders.toDouble() / userProfile.totalOrders
        if (completionRate < 0.8) {
            issues.add("Low completion rate (${completionRate * 100}%)")
            status = ComplianceStatus.NON_COMPLIANT
        }

        // Проверка типа груза
        if (cargoType == "DANGEROUS" && !hasHazardousCargoLicense(userProfile)) {
            issues.add("Missing hazardous cargo license")
            status = ComplianceStatus.NON_COMPLIANT
        }

        // Проверка маршрута
        val routeCheck = checkRouteCompliance(route)
        if (!routeCheck.isCompliant) {
            issues.add("Route compliance issues: ${routeCheck.issues.joinToString(", ")}")
            status = ComplianceStatus.NON_COMPLIANT
        }

        val details = if (issues.isEmpty()) {
            "All basic compliance checks passed"
        } else {
            issues.joinToString("; ")
        }

        return ComplianceCheck(
            status = status.name,
            details = details,
            riskFactors = issues
        )
    }

    /**
     * Проверка лицензий и сертификатов
     */
    private fun checkLicensesAndCertificates(
        userProfile: UserProfileEntity,
        cargoType: String
    ): ComplianceCheck {
        val requiredLicenses = getRequiredLicenses(cargoType)
        val missingLicenses = mutableListOf<String>()
        
        // В реальной системе здесь будет проверка базы данных лицензий
        // Для примера используем упрощенную логику
        if (cargoType == "DANGEROUS" && !hasHazardousCargoLicense(userProfile)) {
            missingLicenses.add("Hazardous Materials Transportation License")
        }
        
        if (cargoType == "PERISHABLE" && !hasRefrigeratedCargoLicense(userProfile)) {
            missingLicenses.add("Refrigerated Cargo Handling Certificate")
        }

        val status = if (missingLicenses.isEmpty()) {
            ComplianceStatus.COMPLIANT
        } else {
            ComplianceStatus.NON_COMPLIANT
        }

        val details = if (missingLicenses.isEmpty()) {
            "All required licenses and certificates present"
        } else {
            "Missing licenses: ${missingLicenses.joinToString(", ")}"
        }

        return ComplianceCheck(
            status = status.name,
            details = details,
            riskFactors = missingLicenses
        )
    }

    /**
     * Проверка финансовой стабильности
     */
    private fun checkFinancialStability(userProfile: UserProfileEntity): ComplianceCheck {
        // В реальной системе здесь будет проверка финансовых показателей
        // Для примера используем упрощенную логику на основе количества заказов
        
        val financialStability = when {
            userProfile.totalOrders >= 100 -> ComplianceStatus.COMPLIANT
            userProfile.totalOrders >= 50 -> ComplianceStatus.PENDING
            else -> ComplianceStatus.NON_COMPLIANT
        }

        val details = when (financialStability) {
            ComplianceStatus.COMPLIANT -> "Financially stable based on order history"
            ComplianceStatus.PENDING -> "Financial stability requires additional verification"
            else -> "Insufficient financial history"
        }

        return ComplianceCheck(
            status = financialStability.name,
            details = details,
            riskFactors = if (financialStability == ComplianceStatus.NON_COMPLIANT) {
                listOf("Insufficient financial history")
            } else {
                emptyList()
            }
        )
    }

    /**
     * Проверка истории инцидентов
     */
    private fun checkIncidentHistory(userProfile: UserProfileEntity): ComplianceCheck {
        // В реальной системе здесь будет проверка базы данных инцидентов
        // Для примера используем упрощенную логику
        
        val incidentRate = calculateIncidentRate(userProfile)
        val status = when {
            incidentRate == 0.0 -> ComplianceStatus.COMPLIANT
            incidentRate < 0.05 -> ComplianceStatus.PENDING
            else -> ComplianceStatus.NON_COMPLIANT
        }

        val details = when (status) {
            ComplianceStatus.COMPLIANT -> "No incident history"
            ComplianceStatus.PENDING -> "Minor incidents detected, requires review"
            else -> "High incident rate detected"
        }

        return ComplianceCheck(
            status = status.name,
            details = details,
            riskFactors = if (incidentRate > 0) {
                listOf("Incident history detected")
            } else {
                emptyList()
            }
        )
    }

    /**
     * Проверка соответствия маршрута
     */
    private fun checkRouteCompliance(route: RouteInfo): RouteCompliance {
        val highRiskCountries = setOf("SY", "YE", "AF", "SO", "IQ")
        val restrictedCountries = setOf("IR", "KP", "CU", "SD")
        
        val allCountries = listOf(route.pickupLocation.country, route.deliveryLocation.country) + 
                          route.waypoints.map { it.country }
        
        val issues = mutableListOf<String>()
        
        // Проверка высокорисковых стран
        val highRiskCountriesFound = allCountries.filter { it in highRiskCountries }
        if (highRiskCountriesFound.isNotEmpty()) {
            issues.add("High risk countries in route: ${highRiskCountriesFound.joinToString(", ")}")
        }
        
        // Проверка санкционированных стран
        val restrictedCountriesFound = allCountries.filter { it in restrictedCountries }
        if (restrictedCountriesFound.isNotEmpty()) {
            issues.add("Restricted countries in route: ${restrictedCountriesFound.joinToString(", ")}")
        }

        return RouteCompliance(
            isCompliant = issues.isEmpty(),
            issues = issues
        )
    }

    /**
     * Комбинирование результатов проверки
     */
    private fun combineComplianceResults(
        basic: ComplianceCheck,
        licenses: ComplianceCheck,
        financial: ComplianceCheck,
        incidents: ComplianceCheck
    ): ComplianceCheck {
        val allStatuses = listOf(basic.status, licenses.status, financial.status, incidents.status)
        
        val finalStatus = when {
            allStatuses.contains(ComplianceStatus.NON_COMPLIANT.name) -> ComplianceStatus.NON_COMPLIANT
            allStatuses.contains(ComplianceStatus.PENDING.name) -> ComplianceStatus.PENDING
            else -> ComplianceStatus.COMPLIANT
        }

        val allDetails = listOfNotNull(
            basic.details, licenses.details, financial.details, incidents.details
        )
        
        val allRiskFactors = listOf(
            basic.riskFactors, licenses.riskFactors, financial.riskFactors, incidents.riskFactors
        ).flatten()

        return ComplianceCheck(
            status = finalStatus.name,
            details = allDetails.joinToString("; "),
            riskFactors = allRiskFactors
        )
    }

    /**
     * Создание SCM снимка
     */
    private fun createComplianceSnapshot(
        carrierId: UUID,
        masterOrderId: UUID,
        result: ComplianceCheck
    ): ScmSnapshotEntity {
        val snapshot = ScmSnapshotEntity(
            bidId = UUID.randomUUID(),
            carrierId = carrierId,
            masterOrderId = masterOrderId,
            snapshotDate = LocalDateTime.now(),
            complianceStatus = ComplianceStatus.valueOf(result.status),
            complianceDetails = result.details,
            securityClearance = SecurityLevel.NONE,
            securityDetails = "Security check pending",
            riskScore = 0.0, // Будет рассчитан позже
            riskFactors = result.riskFactors.joinToString(", "),
            auditTrail = "Compliance check completed"
        )

        return scmSnapshotRepository.save(snapshot)
    }

    /**
     * Расчет рискового балла
     */
    private fun calculateRiskScore(
        userProfile: UserProfileEntity,
        cargoType: String,
        route: RouteInfo
    ): Double {
        var riskScore = 0.0

        // Факторы риска на основе профиля
        when {
            userProfile.rating < 4.0 -> riskScore += 0.2
            userProfile.rating < 3.0 -> riskScore += 0.3
        }

        val completionRate = userProfile.completedOrders.toDouble() / userProfile.totalOrders
        when {
            completionRate < 0.9 -> riskScore += 0.2
            completionRate < 0.8 -> riskScore += 0.3
        }

        // Факторы риска на основе типа груза
        when (cargoType) {
            "DANGEROUS" -> riskScore += 0.4
            "PERISHABLE" -> riskScore += 0.2
            "VALUABLE" -> riskScore += 0.3
        }

        // Факторы риска на основе маршрута
        val routeRisk = calculateRouteRisk(route)
        riskScore += routeRisk

        return riskScore.coerceIn(0.0, 1.0)
    }

    /**
     * Расчет риска маршрута
     */
    private fun calculateRouteRisk(route: RouteInfo): Double {
        var risk = 0.0
        
        val highRiskCountries = setOf("SY", "YE", "AF", "SO")
        val mediumRiskCountries = setOf("UA", "RU", "BY", "IR")
        
        val allCountries = listOf(route.pickupLocation.country, route.deliveryLocation.country) + 
                          route.waypoints.map { it.country }
        
        allCountries.forEach { country ->
            when {
                country in highRiskCountries -> risk += 0.5
                country in mediumRiskCountries -> risk += 0.3
            }
        }

        return risk
    }

    /**
     * Построение аудит-трейла проверки соответствия
     */
    private fun buildComplianceAuditTrail(
        result: ComplianceCheck,
        snapshot: ScmSnapshotEntity
    ): String {
        return """
            Compliance Check Audit Trail:
            Status: ${result.status}
            Details: ${result.details}
            Risk Factors: ${result.riskFactors.joinToString(", ")}
            Snapshot ID: ${snapshot.id}
            Snapshot Date: ${snapshot.snapshotDate}
            Audit Timestamp: ${LocalDateTime.now()}
        """.trimIndent()
    }

    // Вспомогательные функции
    private fun hasHazardousCargoLicense(userProfile: UserProfileEntity): Boolean {
        // В реальной системе здесь будет проверка базы данных лицензий
        return userProfile.totalOrders > 50 // Упрощенная проверка
    }

    private fun hasRefrigeratedCargoLicense(userProfile: UserProfileEntity): Boolean {
        // В реальной системе здесь будет проверка базы данных лицензий
        return userProfile.totalOrders > 20 // Упрощенная проверка
    }

    private fun calculateIncidentRate(userProfile: UserProfileEntity): Double {
        // В реальной системе здесь будет расчет на основе истории инцидентов
        return 0.0 // Упрощенная проверка
    }

    private fun getRequiredLicenses(cargoType: String): List<String> {
        return when (cargoType) {
            "DANGEROUS" -> listOf("Hazardous Materials License", "Special Handling Certificate")
            "PERISHABLE" -> listOf("Refrigerated Cargo Certificate", "Food Safety License")
            "VALUABLE" -> listOf("High Value Cargo License", "Security Clearance")
            else -> listOf("General Transportation License")
        }
    }

    // Вспомогательные классы
    private data class ComplianceCheck(
        val status: String,
        val details: String,
        val riskFactors: List<String> = emptyList()
    )

    private data class RouteCompliance(
        val isCompliant: Boolean,
        val issues: List<String>
    )
}