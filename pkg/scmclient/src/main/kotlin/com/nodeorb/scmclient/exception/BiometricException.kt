package com.nodeorb.scmclient.exception

/**
 * Виняток, який виникає при необхідності біометричної перевірки
 * Використовується для ініціювання Step-up Authentication
 */
class BiometricException(
    message: String,
    val challenge: BiometricChallenge,
    val operation: String,
    val request: Any? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Повертає деталі біометричної перевірки для фронтенду
     */
    fun getBiometricDetails(): BiometricDetails {
        return BiometricDetails(
            challenge = challenge,
            message = message,
            operation = operation,
            request = request
        )
    }

    /**
     * Деталі біометричної перевірки для фронтенду
     */
    data class BiometricDetails(
        val challenge: BiometricChallenge,
        val message: String,
        val operation: String,
        val request: Any? = null
    )
}

/**
 * Біометричний challenge для перевірки
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
    VOICE_RECOGNITION
}