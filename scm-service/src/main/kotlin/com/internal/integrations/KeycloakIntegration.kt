package com.internal.integrations

import com.fasterxml.jackson.databind.ObjectMapper
import com.internal.repository.ComplianceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Интеграция с Keycloak для синхронизации пользовательских данных
 */
@Service
class KeycloakIntegration(
    private val complianceRepository: ComplianceRepository,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(KeycloakIntegration::class.java)

    companion object {
        private const val DEFAULT_TRUST_SCORE = 50.0
    }

    /**
     * Синхронизация данных пользователя из Keycloak
     */
    @Transactional
    fun syncUserFromKeycloak(keycloakUserData: KeycloakUserData) {
        logger.info("Syncing user from Keycloak: ${keycloakUserData.userId}")

        try {
            // Проверяем существование пользователя в нашей системе
            val existingPassport = complianceRepository.getCompliancePassport(keycloakUserData.userId)

            if (existingPassport != null) {
                // Обновляем существующий паспорт
                updateExistingPassport(existingPassport, keycloakUserData)
            } else {
                // Создаем новый паспорт
                createNewPassport(keycloakUserData)
            }

            logger.info("User sync completed successfully: ${keycloakUserData.userId}")
        } catch (e: Exception) {
            logger.error("Failed to sync user from Keycloak: ${keycloakUserData.userId}", e)
            throw RuntimeException("User synchronization failed", e)
        }
    }

    /**
     * Создание нового Compliance Passport
     */
    private fun createNewPassport(userData: KeycloakUserData) {
        val verificationData = buildVerificationData(userData)
        
        val passportEntity = CompliancePassportEntity(
            userId = userData.userId,
            entityType = determineEntityType(userData.roles),
            trustScore = DEFAULT_TRUST_SCORE,
            complianceStatus = "PENDING",
            isBiometricsEnabled = false,
            verificationData = objectMapper.writeValueAsString(verificationData),
            expiresAt = null
        )

        complianceRepository.save(passportEntity)
    }

    /**
     * Обновление существующего паспорта
     */
    private fun updateExistingPassport(
        existingPassport: CompliancePassport,
        userData: KeycloakUserData
    ) {
        val updatedData = buildVerificationData(userData, existingPassport.verificationData)
        
        val passportEntity = CompliancePassportEntity(
            userId = userData.userId,
            entityType = determineEntityType(userData.roles),
            trustScore = existingPassport.trustScore,
            complianceStatus = existingPassport.complianceStatus,
            isBiometricsEnabled = existingPassport.isBiometricsEnabled,
            verificationData = objectMapper.writeValueAsString(updatedData),
            expiresAt = existingPassport.expiresAt
        )

        complianceRepository.save(passportEntity)
    }

    /**
     * Повышение уровня доверия пользователя
     */
    @Transactional
    fun increaseTrustScore(userId: String, increment: Double) {
        val passport = complianceRepository.getCompliancePassport(userId)
            ?: throw RuntimeException("User not found: $userId")

        val newScore = (passport.trustScore + increment).coerceIn(0.0, 100.0)
        complianceRepository.updateTrustScore(userId, newScore)
        
        logger.info("Trust score updated for user $userId: ${passport.trustScore} -> $newScore")
    }

    /**
     * Понижение уровня доверия пользователя
     */
    @Transactional
    fun decreaseTrustScore(userId: String, decrement: Double) {
        val passport = complianceRepository.getCompliancePassport(userId)
            ?: throw RuntimeException("User not found: $userId")

        val newScore = (passport.trustScore - decrement).coerceIn(0.0, 100.0)
        complianceRepository.updateTrustScore(userId, newScore)
        
        logger.info("Trust score decreased for user $userId: ${passport.trustScore} -> $newScore")
        
        // Проверка на блокировку
        if (newScore < 25.0) {
            blockUser(userId, "Trust score below threshold")
        }
    }

    /**
     * Блокировка пользователя
     */
    @Transactional
    fun blockUser(userId: String, reason: String) {
        complianceRepository.updateComplianceStatus(userId, "BLACKLISTED")
        logger.warn("User $userId blocked: $reason")
    }

    /**
     * Разблокировка пользователя
     */
    @Transactional
    fun unblockUser(userId: String) {
        complianceRepository.updateComplianceStatus(userId, "VERIFIED")
        logger.info("User $userId unblocked")
    }

    /**
     * Проверка наличия роли у пользователя
     */
    fun hasRole(userId: String, role: String): Boolean {
        val passport = complianceRepository.getCompliancePassport(userId)
        return passport?.verificationData?.get("roles")?.let { roles ->
            @Suppress("UNCHECKED_CAST")
            (roles as? List<String>)?.contains(role) ?: false
        } ?: false
    }

    /**
     * Построение данных верификации
     */
    private fun buildVerificationData(
        userData: KeycloakUserData,
        existingData: Map<String, Any>? = null
    ): Map<String, Any> {
        val verificationData = existingData?.toMutableMap() ?: mutableMapOf()

        // Обновляем базовые данные
        verificationData["email"] = userData.email
        verificationData["roles"] = userData.roles
        verificationData["lastSync"] = Instant.now()

        // Добавляем лицензии на основе ролей
        val licenses = buildLicenses(userData.roles)
        verificationData["licenses"] = licenses

        // Добавляем уровни доступа
        val securityLevels = buildSecurityLevels(userData.roles)
        verificationData["security_levels"] = securityLevels

        // Добавляем разрешенные страны (по умолчанию все)
        verificationData["allowed_countries"] = listOf("*")

        return verificationData
    }

    /**
     * Построение списка лицензий на основе ролей
     */
    private fun buildLicenses(roles: List<String>): List<Map<String, Any>> {
        val licenses = mutableListOf<Map<String, Any>>()

        if ("admin" in roles) {
            licenses.add(mapOf("type" to "ADMIN", "valid" to true))
        }

        if ("carrier" in roles) {
            licenses.add(mapOf("type" to "CARRIER", "valid" to true))
        }

        if ("shipper" in roles) {
            licenses.add(mapOf("type" to "SHIPPER", "valid" to true))
        }

        if ("hazmat" in roles) {
            licenses.add(mapOf("type" to "ADR", "valid" to true))
        }

        if ("itar" in roles) {
            licenses.add(mapOf("type" to "ITAR", "valid" to true))
            licenses.add(mapOf("type" to "EAR", "valid" to true))
        }

        return licenses
    }

    /**
     * Построение уровней доступа на основе ролей
     */
    private fun buildSecurityLevels(roles: List<String>): List<String> {
        val levels = mutableListOf<String>()

        if ("admin" in roles) {
            levels.addAll(listOf("CONFIDENTIAL", "SECRET", "TOP_SECRET"))
        } else if ("itar" in roles) {
            levels.addAll(listOf("CONFIDENTIAL", "SECRET"))
        } else {
            levels.add("CONFIDENTIAL")
        }

        return levels
    }

    /**
     * Определение типа сущности на основе ролей
     */
    private fun determineEntityType(roles: List<String>): String {
        return when {
            "carrier" in roles -> "CARRIER"
            "shipper" in roles -> "SHIPPER"
            "admin" in roles -> "WAREHOUSE"
            else -> "SHIPPER"
        }
    }
}

/**
 * Модель данных пользователя из Keycloak
 */
data class KeycloakUserData(
    val userId: String,
    val email: String,
    val roles: List<String>,
    val attributes: Map<String, List<String>> = emptyMap()
)