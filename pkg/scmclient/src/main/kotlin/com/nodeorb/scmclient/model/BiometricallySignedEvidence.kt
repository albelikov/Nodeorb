package com.nodeorb.scmclient.model

import java.time.Instant

/**
 * Біометрично підписані докази для Evidence Package
 * Використовується для юридичної доказовості того, що дію вчинила конкретна людина
 */
data class BiometricallySignedEvidence(
    /**
     * Чи була дія підтверджена біометрично
     */
    val biometricallySigned: Boolean,
    
    /**
     * Тип біометричної перевірки
     */
    val authType: BiometricAuthType,
    
    /**
     * Ідентифікатор сесії
     */
    val bioSessionId: String,
    
    /**
     * Підписаний challenge
     */
    val signaturePayload: String? = null,
    
    /**
     * Час підпису
     */
    val signedAt: Instant,
    
    /**
     * Результат перевірки
     */
    val verificationResult: BiometricVerificationResult? = null,
    
    /**
     * Додаткові метадані
     */
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Evidence Package для ClickHouse
 * Містить всі докази для юридичної доказовості
 */
data class EvidencePackage(
    /**
     * Ідентифікатор операції
     */
    val operationId: String,
    
    /**
     * Ідентифікатор користувача
     */
    val userId: String,
    
    /**
     * Тип операції
     */
    val operationType: String,
    
    /**
     * Час операції
     */
    val timestamp: Instant,
    
    /**
     * IP адреса
     */
    val ipAddress: String,
    
    /**
     * User Agent
     */
    val userAgent: String,
    
    /**
     * Геолокація
     */
    val geoLocation: GeoLocation? = null,
    
    /**
     * Біометричні докази
     */
    val biometricEvidence: BiometricallySignedEvidence? = null,
    
    /**
     * Додаткові докази
     */
    val additionalEvidence: Map<String, String> = emptyMap(),
    
    /**
     * Хеш доказів для цілісності
     */
    val evidenceHash: String? = null
)

/**
 * Геолокація
 */
data class GeoLocation(
    /**
     * Широта
     */
    val latitude: Double,
    
    /**
     * Довгота
     */
    val longitude: Double,
    
    /**
     * Точність
     */
    val accuracy: Double? = null,
    
    /**
     * Адреса
     */
    val address: String? = null
)

/**
 * Compliance Passport
 * Зберігає біометричні та WebAuthn дані користувача
 */
data class CompliancePassport(
    /**
     * Ідентифікатор користувача
     */
    val userId: String,
    
    /**
     * Зареєстровані WebAuthn credentials
     */
    val webAuthnCredentials: List<PublicKeyCredential> = emptyList(),
    
    /**
     * Біометричні дані (хеші)
     */
    val biometricData: List<BiometricData> = emptyList(),
    
    /**
     * Час створення
     */
    val createdAt: Instant,
    
    /**
     * Останнє оновлення
     */
    val updatedAt: Instant,
    
    /**
     * Статус верифікації
     */
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING
)

/**
 * Біометричні дані користувача
 */
data class BiometricData(
    /**
     * Тип біометрії
     */
    val authType: BiometricAuthType,
    
    /**
     * Хеш біометричних даних
     */
    val biometricHash: String,
    
    /**
     * Час реєстрації
     */
    val registeredAt: Instant,
    
    /**
     * Останнє використання
     */
    val lastUsedAt: Instant? = null,
    
    /**
     * Статус
     */
    val status: BiometricStatus = BiometricStatus.VERIFIED
)

/**
 * Статус верифікації
 */
enum class VerificationStatus {
    PENDING,    // Очікується
    VERIFIED,   // Підтверджено
    REJECTED,   // Відхилено
    EXPIRED     // Застаріло
}

/**
 * Audit Log Entry
 * Запис для аудиту біометричних операцій
 */
data class BiometricAuditLog(
    /**
     * Ідентифікатор запису
     */
    val logId: String,
    
    /**
     * Ідентифікатор користувача
     */
    val userId: String,
    
    /**
     * Тип операції
     */
    val operationType: BiometricOperationType,
    
    /**
     * Результат операції
     */
    val result: BiometricOperationResult,
    
    /**
     * Час операції
     */
    val timestamp: Instant,
    
    /**
     * IP адреса
     */
    val ipAddress: String,
    
    /**
     * User Agent
     */
    val userAgent: String,
    
    /**
     * Сесія
     */
    val sessionId: String? = null,
    
    /**
     * Додаткові деталі
     */
    val details: Map<String, String> = emptyMap()
)

/**
 * Типи біометричних операцій
 */
enum class BiometricOperationType {
    CHALLENGE_GENERATED,    // Challenge створено
    CHALLENGE_VERIFIED,     // Challenge перевірено
    AUTHENTICATION_SUCCESS, // Аутентифікація успішна
    AUTHENTICATION_FAILED,  // Аутентифікація неуспішна
    CREDENTIAL_REGISTERED,  // Credential зареєстровано
    CREDENTIAL_VERIFIED,    // Credential перевірено
    SESSION_EXPIRED,        // Сесія закінчилася
    SECURITY_ALERT          // Безпекова тривога
}

/**
 * Результати біометричних операцій
 */
enum class BiometricOperationResult {
    SUCCESS,        // Успішно
    FAILED,         // Невдало
    EXPIRED,        // Застаріло
    INVALID,        // Недійсно
    BLOCKED,        // Заблоковано
    RATE_LIMITED    // Ліміт перевищено
}