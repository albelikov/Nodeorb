package com.nodeorb.scmclient.handler

import com.nodeorb.scmclient.exception.BiometricException
import com.nodeorb.scmclient.model.*
import com.nodeorb.scmclient.model.BiometricAuthType
import com.nodeorb.scmclient.model.BiometricChallenge
import com.nodeorb.scmclient.model.BiometricStatus
import com.nodeorb.scmclient.model.EnhancedValidationResult
import com.nodeorb.scmclient.model.WebAuthnChallenge
import com.nodeorb.scmclient.model.WebAuthnSignedChallenge
import com.nodeorb.scmclient.model.AuthResult
import com.nodeorb.scmclient.model.BiometricVerificationRequest
import com.nodeorb.scmclient.model.BiometricVerificationResult
import com.nodeorb.scmclient.model.BiometricEvidence
import com.nodeorb.scmclient.model.BiometricallySignedEvidence
import com.nodeorb.scmclient.model.SignedChallenge
import com.nodeorb.scmclient.model.PublicKeyCredential
import com.nodeorb.scmclient.model.AuthenticatorType
import com.nodeorb.scmclient.logging.SCMLogger
import com.nodeorb.scmclient.config.SCMClientConfig
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.time.Instant
import java.time.Duration
import java.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.EllipticCurve
import java.security.spec.ECParameterSpec
import java.security.spec.ECGenParameterSpec
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory
import java.security.Signature
import java.security.MessageDigest
import java.util.Base64
import java.time.Instant
import java.time.Duration
import java.util.UUID

/**
 * Обробник біометричної аутентифікації та WebAuthn
 * Надає функції для генерації challenge, перевірки підписів та управління сесіями
 */
class BiometricHandler(
    private val config: SCMClientConfig,
    private val scmLogger: SCMLogger
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(BiometricHandler::class.java)
        
        // Константи для WebAuthn
        private const val CHALLENGE_EXPIRY_SECONDS = 300L // 5 хвилин
        private const val BIO_SESSION_EXPIRY_SECONDS = 600L // 10 хвилин
        private const val WEBAUTHN_RP_ID = "nodeorb.com"
        private const val WEBAUTHN_ORIGIN = "https://nodeorb.com"
        
        // Алгоритми підпису
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        private const val MESSAGE_DIGEST_ALGORITHM = "SHA-256"
    }

    // Зберігання активних сесій
    private val activeChallenges = mutableMapOf<String, WebAuthnChallenge>()
    private val activeBioSessions = mutableMapOf<String, BiometricChallenge>()
    private val userCredentials = mutableMapOf<String, MutableList<PublicKeyCredential>>()

    /**
     * Генерує біометричний challenge для користувача
     */
    fun generateBiometricChallenge(
        userId: String,
        authType: BiometricAuthType = BiometricAuthType.FACE_ID
    ): BiometricChallenge {
        val bioSessionId = UUID.randomUUID().toString()
        val challenge = generateRandomChallenge()
        val createdAt = Instant.now()
        val expiresAt = createdAt.plusSeconds(BIO_SESSION_EXPIRY_SECONDS)

        val biometricChallenge = BiometricChallenge(
            bioSessionId = bioSessionId,
            challenge = challenge,
            createdAt = createdAt.toEpochMilli(),
            expiresAt = expiresAt.toEpochMilli(),
            authType = authType
        )

        // Зберігаємо сесію
        activeBioSessions[bioSessionId] = biometricChallenge

        scmLogger.logBiometricChallenge(userId, bioSessionId, authType)
        logger.info("Generated biometric challenge for user: $userId, session: $bioSessionId, type: $authType")

        return biometricChallenge
    }

    /**
     * Генерує WebAuthn challenge для користувача
     */
    fun generateWebAuthnChallenge(userId: String): WebAuthnChallenge {
        val challengeId = UUID.randomUUID().toString()
        val challenge = generateRandomChallenge()
        val createdAt = Instant.now()
        val expiresAt = createdAt.plusSeconds(CHALLENGE_EXPIRY_SECONDS)

        val webAuthnChallenge = WebAuthnChallenge(
            challengeId = challengeId,
            challenge = challenge,
            createdAt = createdAt,
            expiresAt = expiresAt,
            origin = WEBAUTHN_ORIGIN,
            rpId = WEBAUTHN_RP_ID
        )

        // Зберігаємо challenge
        activeChallenges[challengeId] = webAuthnChallenge

        scmLogger.logWebAuthnChallenge(userId, challengeId)
        logger.info("Generated WebAuthn challenge for user: $userId, challenge: $challengeId")

        return webAuthnChallenge
    }

    /**
     * Перевіряє біометричний підпис
     */
    fun verifyBiometricSignature(
        signedChallenge: String,
        bioSessionId: String,
        userId: String
    ): BiometricVerificationResult {
        val challenge = activeBioSessions[bioSessionId]
        
        if (challenge == null) {
            logger.warn("Invalid bio session ID: $bioSessionId")
            return BiometricVerificationResult(
                success = false,
                bioSessionId = bioSessionId,
                timestamp = Instant.now(),
                authType = BiometricAuthType.FACE_ID,
                errorMessage = "Invalid bio session ID"
            )
        }

        if (Instant.now().toEpochMilli() > challenge.expiresAt) {
            logger.warn("Bio session expired: $bioSessionId")
            return BiometricVerificationResult(
                success = false,
                bioSessionId = bioSessionId,
                timestamp = Instant.now(),
                authType = challenge.authType,
                errorMessage = "Bio session expired"
            )
        }

        // Перевіряємо підпис (спрощена перевірка)
        val isValid = verifySignature(challenge.challenge, signedChallenge, userId)
        
        val result = BiometricVerificationResult(
            success = isValid,
            bioSessionId = bioSessionId,
            timestamp = Instant.now(),
            authType = challenge.authType,
            signedChallenge = if (isValid) signedChallenge else null,
            errorMessage = if (isValid) null else "Invalid signature"
        )

        if (isValid) {
            scmLogger.logBiometricVerificationSuccess(userId, bioSessionId, challenge.authType)
            logger.info("Biometric verification successful for user: $userId, session: $bioSessionId")
        } else {
            scmLogger.logBiometricVerificationFailure(userId, bioSessionId, challenge.authType, result.errorMessage!!)
        }

        return result
    }

    /**
     * Перевіряє WebAuthn підпис
     */
    fun verifyWebAuthnSignature(
        signedChallenge: SignedChallenge,
        userId: String
    ): BiometricVerificationResult {
        val challenge = activeChallenges[signedChallenge.challengeId]
        
        if (challenge == null) {
            logger.warn("Invalid challenge ID: ${signedChallenge.challengeId}")
            return BiometricVerificationResult(
                success = false,
                bioSessionId = signedChallenge.challengeId,
                timestamp = Instant.now(),
                authType = BiometricAuthType.WEB_AUTHN,
                errorMessage = "Invalid challenge ID"
            )
        }

        if (Instant.now().isAfter(challenge.expiresAt)) {
            logger.warn("Challenge expired: ${signedChallenge.challengeId}")
            return BiometricVerificationResult(
                success = false,
                bioSessionId = signedChallenge.challengeId,
                timestamp = Instant.now(),
                authType = BiometricAuthType.WEB_AUTHN,
                errorMessage = "Challenge expired"
            )
        }

        // Отримуємо публічний ключ користувача
        val credential = getUserCredential(userId, signedChallenge.credentialId)
        
        if (credential == null) {
            logger.warn("No credential found for user: $userId, credential: ${signedChallenge.credentialId}")
            return BiometricVerificationResult(
                success = false,
                bioSessionId = signedChallenge.challengeId,
                timestamp = Instant.now(),
                authType = BiometricAuthType.WEB_AUTHN,
                errorMessage = "No credential found"
            )
        }

        // Перевіряємо підпис
        val isValid = verifyWebAuthnSignature(
            challenge.challenge,
            signedChallenge.authenticatorData,
            signedChallenge.signature,
            credential.publicKeyPem
        )

        val result = BiometricVerificationResult(
            success = isValid,
            bioSessionId = signedChallenge.challengeId,
            timestamp = Instant.now(),
            authType = BiometricAuthType.WEB_AUTHN,
            signedChallenge = if (isValid) signedChallenge.signedChallenge else null,
            errorMessage = if (isValid) null else "Invalid WebAuthn signature"
        )

        if (isValid) {
            scmLogger.logWebAuthnVerificationSuccess(userId, signedChallenge.challengeId)
            logger.info("WebAuthn verification successful for user: $userId, challenge: ${signedChallenge.challengeId}")
        } else {
            scmLogger.logWebAuthnVerificationFailure(userId, signedChallenge.challengeId, result.errorMessage!!)
        }

        return result
    }

    /**
     * Підтверджує біометричну аутентифікацію
     */
    fun confirmBiometricAuthentication(
        userId: String,
        signedChallenge: String,
        bioSessionId: String
    ): AuthResult {
        val verificationResult = verifyBiometricSignature(signedChallenge, bioSessionId, userId)
        
        val authResult = AuthResult(
            success = verificationResult.success,
            bioSessionId = bioSessionId,
            authType = verificationResult.authType,
            signedChallenge = verificationResult.signedChallenge,
            errorMessage = verificationResult.errorMessage,
            authenticatedAt = verificationResult.timestamp
        )

        if (verificationResult.success) {
            // Оновлюємо час використання сесії
            val challenge = activeBioSessions[bioSessionId]
            if (challenge != null) {
                challenge.copy(expiresAt = Instant.now().plusSeconds(BIO_SESSION_EXPIRY_SECONDS).toEpochMilli())
            }
        }

        return authResult
    }

    /**
     * Створює біометричні докази для Evidence Package
     */
    fun createBiometricEvidence(
        verificationResult: BiometricVerificationResult,
        signedChallenge: String? = null
    ): BiometricEvidence {
        return BiometricEvidence(
            biometricallySigned = verificationResult.success,
            authType = verificationResult.authType,
            bioSessionId = verificationResult.bioSessionId,
            signaturePayload = signedChallenge ?: verificationResult.signedChallenge,
            signedAt = verificationResult.timestamp,
            verificationResult = verificationResult
        )
    }

    /**
     * Створює біометрично підписані докази для Evidence Package
     */
    fun createBiometricallySignedEvidence(
        verificationResult: BiometricVerificationResult,
        signedChallenge: String? = null
    ): BiometricallySignedEvidence {
        return BiometricallySignedEvidence(
            biometricallySigned = verificationResult.success,
            authType = verificationResult.authType,
            bioSessionId = verificationResult.bioSessionId,
            signaturePayload = signedChallenge ?: verificationResult.signedChallenge,
            signedAt = verificationResult.timestamp,
            verificationResult = verificationResult
        )
    }

    /**
     * Зберігає публічний ключ WebAuthn для користувача
     */
    fun saveWebAuthnCredential(
        userId: String,
        credentialId: String,
        publicKeyPem: String,
        authenticatorType: AuthenticatorType,
        deviceName: String? = null
    ): Boolean {
        try {
            val credential = PublicKeyCredential(
                credentialId = credentialId,
                publicKeyPem = publicKeyPem,
                authenticatorType = authenticatorType,
                createdAt = Instant.now(),
                userId = userId,
                deviceName = deviceName
            )

            val userCreds = userCredentials.getOrPut(userId) { mutableListOf() }
            userCreds.add(credential)

            scmLogger.logWebAuthnCredentialSaved(userId, credentialId, authenticatorType)
            logger.info("Saved WebAuthn credential for user: $userId, credential: $credentialId")

            return true
        } catch (e: Exception) {
            logger.error("Error saving WebAuthn credential for user: $userId", e)
            return false
        }
    }

    /**
     * Отримує публічний ключ WebAuthn для користувача
     */
    fun getUserCredential(userId: String, credentialId: String): PublicKeyCredential? {
        val userCreds = userCredentials[userId] ?: return null
        return userCreds.find { it.credentialId == credentialId }
    }

    /**
     * Перевіряє чи є сесія активною
     */
    fun isBioSessionActive(bioSessionId: String): Boolean {
        val challenge = activeBioSessions[bioSessionId]
        return challenge != null && Instant.now().toEpochMilli() <= challenge.expiresAt
    }

    /**
     * Очищає застарілі сесії
     */
    fun cleanupExpiredSessions() {
        val now = Instant.now().toEpochMilli()
        
        // Очищаємо застарілі challenge
        activeChallenges.entries.removeIf { (_, challenge) ->
            now > challenge.expiresAt.toEpochMilli()
        }
        
        // Очищаємо застарілі біометричні сесії
        activeBioSessions.entries.removeIf { (_, challenge) ->
            now > challenge.expiresAt
        }
    }

    // Приватні методи

    /**
     * Генерує випадковий challenge
     */
    private fun generateRandomChallenge(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * Перевіряє підпис (спрощена реалізація)
     */
    private fun verifySignature(originalChallenge: String, signedChallenge: String, userId: String): Boolean {
        // Це спрощена перевірка для демонстрації
        // У реальній реалізації потрібно використовувати відповідний алгоритм перевірки підпису
        return try {
            // Для демонстрації повертаємо true, якщо підпис не порожній
            signedChallenge.isNotEmpty() && signedChallenge.length > 10
        } catch (e: Exception) {
            logger.error("Error verifying signature for user: $userId", e)
            false
        }
    }

    /**
     * Перевіряє WebAuthn підпис
     */
    private fun verifyWebAuthnSignature(
        challenge: String,
        authenticatorData: String,
        signature: String,
        publicKeyPem: String
    ): Boolean {
        return try {
            // Декодуємо публічний ключ
            val publicKey = decodePublicKey(publicKeyPem)
            
            // Створюємо повідомлення для перевірки
            val message = challenge + authenticatorData
            
            // Декодуємо сигнатуру
            val signatureBytes = Base64.getDecoder().decode(signature)
            
            // Перевіряємо підпис
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(message.toByteArray())
            
            sig.verify(signatureBytes)
        } catch (e: Exception) {
            logger.error("Error verifying WebAuthn signature", e)
            false
        }
    }

    /**
     * Декодує публічний ключ з PEM формату
     */
    private fun decodePublicKey(publicKeyPem: String): PublicKey {
        val publicKeyContent = publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        
        val keyBytes = Base64.getDecoder().decode(publicKeyContent)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(keySpec)
    }
}