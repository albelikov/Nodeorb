package com.nodeorb.scmclient.model

import java.security.PublicKey
import java.time.Instant
import java.util.*

/**
 * Моделі даних для біометричної аутентифікації та WebAuthn
 */

/**
 * Запит на біометричну перевірку
 */
data class BiometricVerificationRequest(
    /**
     * Ідентифікатор користувача
     */
    val userId: String,
    
    /**
     * Тип біометричної перевірки
     */
    val authType: BiometricAuthType = BiometricAuthType.FACE_ID,
    
    /**
     * Додатковий контекст
     */
    val context: Map<String, String> = emptyMap()
)

/**
 * Результат біометричної перевірки
 */
data class BiometricVerificationResult(
    /**
     * Чи була перевірка успішною
     */
    val success: Boolean,
    
    /**
     * Ідентифікатор сесії
     */
    val bioSessionId: String,
    
    /**
     * Час перевірки
     */
    val timestamp: Instant,
    
    /**
     * Тип аутентифікації
     */
    val authType: BiometricAuthType,
    
    /**
     * Підписаний challenge
     */
    val signedChallenge: String? = null,
    
    /**
     * Повідомлення про помилку
     */
    val errorMessage: String? = null
)

/**
 * WebAuthn Public Key Credential
 */
data class PublicKeyCredential(
    /**
     * Ідентифікатор сертифіката
     */
    val credentialId: String,
    
    /**
     * Публічний ключ у форматі PEM
     */
    val publicKeyPem: String,
    
    /**
     * Тип аутентифікатора
     */
    val authenticatorType: AuthenticatorType,
    
    /**
     * Час створення
     */
    val createdAt: Instant,
    
    /**
     * Ідентифікатор користувача
     */
    val userId: String,
    
    /**
     * Назва пристрою
     */
    val deviceName: String? = null,
    
    /**
     * Останній час використання
     */
    val lastUsedAt: Instant? = null
)

/**
 * Типи аутентифікаторів WebAuthn
 */
enum class AuthenticatorType {
    PLATFORM,    // Вбудовані (FaceID, TouchID)
    CROSS_PLATFORM // Зовнішні (YubiKey, Google Titan)
}

/**
 * WebAuthn Challenge для підпису
 */
data class WebAuthnChallenge(
    /**
     * Унікальний ідентифікатор challenge
     */
    val challengeId: String,
    
    /**
     * Сам challenge для підпису
     */
    val challenge: String,
    
    /**
     * Час створення
     */
    val createdAt: Instant,
    
    /**
     * Час закінчення дії
     */
    val expiresAt: Instant,
    
    /**
     * Оригін для WebAuthn
     */
    val origin: String,
    
    /**
     * RP (Relying Party) ідентифікатор
     */
    val rpId: String
)

/**
 * WebAuthn Signed Challenge
 */
data class SignedChallenge(
    /**
     * Ідентифікатор challenge
     */
    val challengeId: String,
    
    /**
     * Підписаний challenge
     */
    val signedChallenge: String,
    
    /**
     * Credential ID
     */
    val credentialId: String,
    
    /**
     * User Handle
     */
    val userHandle: String,
    
    /**
     * Атрибути аутентифікації
     */
    val authenticatorData: String,
    
    /**
     * Сигнатура
     */
    val signature: String,
    
    /**
     * Час підпису
     */
    val signedAt: Instant
)

/**
 * Biometric Evidence для Evidence Package
 */
data class BiometricEvidence(
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
    val verificationResult: BiometricVerificationResult? = null
)

/**
 * Enhanced Validation Result with Biometric Support
 */
data class EnhancedValidationResult(
    /**
     * Чи дозволена операція
     */
    val allowed: Boolean,
    
    /**
     * Причина рішення
     */
    val reason: String,
    
    /**
     * Рівень ризику (0.0 - 100.0)
     */
    val riskScore: Double,
    
    /**
     * Ідентифікатор політики
     */
    val policyId: String?,
    
    /**
     * Чи SCM був недоступний
     */
    val scmOffline: Boolean = false,
    
    /**
     * Статус біометричної перевірки
     */
    val biometricStatus: BiometricStatus = BiometricStatus.NOT_REQUIRED,
    
    /**
     * Біометричні докази
     */
    val biometricEvidence: BiometricEvidence? = null,
    
    /**
     * Challenge для біометричної перевірки (якщо потрібна)
     */
    val biometricChallenge: BiometricChallenge? = null
)

/**
 * Статус біометричної перевірки
 */
enum class BiometricStatus {
    NOT_REQUIRED,  // Не потрібна
    REQUIRED,      // Потрібна
    PENDING,       // Очікується
    VERIFIED,      // Підтверджена
    FAILED         // Невдала
}

/**
 * Biometric Challenge (перенесено з BiometricException для уніфікації)
 */
data class BiometricChallenge(
    /**
     * Унікальний ідентифікатор сесії біометричної перевірки
     */
    val bioSessionId: String,
    
    /**
     * Challenge для підпису
     */
    val challenge: String,
    
    /**
     * Час створення challenge
     */
    val createdAt: Long,
    
    /**
     * Час закінчення дії challenge
     */
    val expiresAt: Long,
    
    /**
     * Тип біометричної перевірки
     */
    val authType: BiometricAuthType = BiometricAuthType.FACE_ID
)

/**
 * Типи біометричної аутентифікації
 */
enum class BiometricAuthType {
    FACE_ID,
    TOUCH_ID,
    FINGERPRINT,
    IRIS_SCAN,
    VOICE_RECOGNITION,
    WEB_AUTHN
}

/**
 * Auth Result for Biometric Confirmation
 */
data class AuthResult(
    /**
     * Чи аутентифікація успішна
     */
    val success: Boolean,
    
    /**
     * Ідентифікатор сесії
     */
    val bioSessionId: String,
    
    /**
     * Тип аутентифікації
     */
    val authType: BiometricAuthType,
    
    /**
     * Підписаний challenge
     */
    val signedChallenge: String? = null,
    
    /**
     * Повідомлення про помилку
     */
    val errorMessage: String? = null,
    
    /**
     * Час аутентифікації
     */
    val authenticatedAt: Instant
)

/**
 * Appeal Request with Biometric Signature
 */
data class AppealRequestWithSignature(
    /**
     * Хеш запису
     */
    val recordHash: String,
    
    /**
     * Текст апеляції
     */
    val text: String,
    
    /**
     * URL доказів
     */
    val evidenceUrl: String,
    
    /**
     * Біометричний підпис
     */
    val biometricSignature: String,
    
    /**
     * Ідентифікатор сесії
     */
    val bioSessionId: String,
    
    /**
     * Додатковий контекст
     */
    val additionalContext: Map<String, String> = emptyMap()
)