package com.internal.repository

import com.model.CompliancePassport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Репозиторий для работы с данными соответствия (Compliance Passport)
 */
@Repository
interface ComplianceRepository : JpaRepository<CompliancePassportEntity, String> {

    @Query("SELECT cp FROM CompliancePassportEntity cp WHERE cp.userId = :userId")
    fun getCompliancePassport(userId: String): CompliancePassport?

    @Query("SELECT cp FROM CompliancePassportEntity cp WHERE cp.entityType = :entityType AND cp.complianceStatus = 'VERIFIED'")
    fun findByEntityTypeAndVerified(entityType: String): List<CompliancePassport>

    @Query("SELECT cp FROM CompliancePassportEntity cp WHERE cp.trustScore < :minScore")
    fun findByTrustScoreLessThan(minScore: Double): List<CompliancePassport>

    @Query("SELECT cp FROM CompliancePassportEntity cp WHERE cp.expiresAt < :expirationDate")
    fun findByExpirationDateBefore(expirationDate: Instant): List<CompliancePassport>

    @Query("UPDATE CompliancePassportEntity cp SET cp.trustScore = :newScore WHERE cp.userId = :userId")
    fun updateTrustScore(userId: String, newScore: Double): Int

    @Query("UPDATE CompliancePassportEntity cp SET cp.complianceStatus = :status WHERE cp.userId = :userId")
    fun updateComplianceStatus(userId: String, status: String): Int
}

/**
 * Сущность Compliance Passport для JPA
 */
data class CompliancePassportEntity(
    val userId: String,
    val entityType: String,
    val trustScore: Double,
    val complianceStatus: String,
    val isBiometricsEnabled: Boolean,
    val verificationData: String, // JSON строка
    val expiresAt: Instant?
) {
    companion object {
        const val TABLE_NAME = "compliance_passports"
    }
}

/**
 * Модель Compliance Passport для бизнес-логики
 */
data class CompliancePassport(
    val userId: String,
    val entityType: String,
    val trustScore: Double,
    val complianceStatus: String,
    val isBiometricsEnabled: Boolean,
    val verificationData: Map<String, Any>,
    val expiresAt: Instant?
) {
    fun hasLicense(licenseType: String): Boolean {
        return verificationData["licenses"]?.let { licenses ->
            @Suppress("UNCHECKED_CAST")
            (licenses as? List<Map<String, Any>>)?.any { 
                it["type"] == licenseType && it["valid"] == true 
            } ?: false
        } ?: false
    }

    fun isCountryAllowed(country: String): Boolean {
        val allowedCountries = verificationData["allowed_countries"] as? List<String>
        return allowedCountries?.contains(country) ?: true
    }

    fun isLocationAllowed(location: String): Boolean {
        val allowedLocations = verificationData["allowed_locations"] as? List<String>
        return allowedLocations?.contains(location) ?: true
    }

    fun hasSecurityLevel(level: String): Boolean {
        val securityLevels = verificationData["security_levels"] as? List<String>
        return securityLevels?.contains(level) ?: false
    }
}