package com.nodeorb.scmclient.example

import com.nodeorb.scmclient.SCMClientFactory
import com.nodeorb.scmclient.SCMClient
import com.nodeorb.scmclient.exception.BiometricException
import com.nodeorb.scmclient.exception.ScmSecurityBlockException
import com.nodeorb.scmclient.model.*
import com.nodeorb.scmclient.model.BiometricAuthType
import com.nodeorb.scmclient.model.BiometricChallenge
import com.nodeorb.scmclient.model.BiometricStatus
import com.nodeorb.scmclient.model.EnhancedValidationResult
import com.nodeorb.scmclient.model.ValidationResult
import com.nodeorb.scmclient.model.ValidationContext
import com.nodeorb.scmclient.model.AppealRequestWithSignature
import com.nodeorb.scmclient.model.AuthResult
import com.nodeorb.scmclient.model.BiometricEvidence
import com.nodeorb.scmclient.model.BiometricallySignedEvidence
import com.nodeorb.scmclient.model.EvidencePackage
import com.nodeorb.scmclient.model.GeoLocation
import com.nodeorb.scmclient.interceptor.ContextInterceptor
import com.nodeorb.scmclient.interceptor.SmartBiometricInterceptor
import com.nodeorb.scmclient.handler.BiometricHandler
import com.nodeorb.scmclient.logging.SCMLogger
import com.nodeorb.scmclient.config.SCMClientConfig
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

/**
 * Приклад використання біометричної аутентифікації у Freight Marketplace
 * Демонструє Step-up Authentication для критичних дій
 */
class BiometricMarketplaceExample {

    companion object {
        private val logger = LoggerFactory.getLogger(BiometricMarketplaceExample::class.java)
    }

    // SCM Client з підтримкою біометричної аутентифікації
    private val scmClient: SCMClient
    private val biometricHandler: BiometricHandler
    private val smartInterceptor: SmartBiometricInterceptor

    init {
        // Створюємо SCM Client для Freight Marketplace
        scmClient = SCMClientFactory.createProductionClient(
            host = "scm-service",
            port = 9090
        )

        // Ініціалізуємо компоненти
        val config = SCMClientConfig(
            host = "scm-service",
            port = 9090,
            enableLogging = true,
            logLevel = "INFO"
        )
        
        val scmLogger = SCMLogger(config)
        biometricHandler = BiometricHandler(config, scmLogger)
        smartInterceptor = SmartBiometricInterceptor(config, biometricHandler, scmLogger)
    }

    /**
     * Приклад комплексної перевірки перед розміщенням високоризикової ставки
     */
    fun validateHighRiskBidPlacement(
        userId: String,
        orderId: String,
        orderType: String,
        bidAmount: Double,
        latitude: Double,
        longitude: Double
    ): EnhancedValidationResult {
        logger.info("Validating high-risk bid placement for user: $userId, order: $orderId, amount: $bidAmount")
        
        try {
            // Встановлюємо контекст запиту
            setRequestContext(userId, latitude, longitude)
            
            // Виконуємо валідацію з біометричною перевіркою
            val result = scmClient.validateCostWithBiometric(
                userId = userId,
                orderId = orderId,
                category = "BID_PLACEMENT",
                value = bidAmount,
                lat = latitude,
                lon = longitude
            )
            
            logger.info("Bid validation result: allowed=${result.allowed}, biometricStatus=${result.biometricStatus}")
            
            // Якщо потрібна біометрична перевірка, ініціюємо BiometricException
            if (result.biometricStatus == BiometricStatus.REQUIRED) {
                val challenge = result.biometricChallenge
                if (challenge != null) {
                    throw BiometricException(
                        message = "High risk bid requires biometric verification",
                        challenge = challenge,
                        operation = "BID_PLACEMENT",
                        request = ValidationContext(
                            userId = userId,
                            orderId = orderId,
                            category = "BID_PLACEMENT",
                            value = bidAmount,
                            latitude = latitude,
                            longitude = longitude
                        )
                    )
                }
            }
            
            return result
            
        } catch (e: BiometricException) {
            logger.warn("Biometric verification required for bid: ${e.message}")
            throw e // Передаємо виключення фронтенду для показу форми аутентифікації
        } catch (e: ScmSecurityBlockException) {
            logger.warn("Bid placement blocked by SCM: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Error validating high-risk bid", e)
            throw e
        }
    }

    /**
     * Приклад підтвердження біометричної аутентифікації
     */
    fun confirmBiometricAuthentication(
        userId: String,
        signedChallenge: String,
        bioSessionId: String
    ): AuthResult {
        logger.info("Confirming biometric authentication for user: $userId, session: $bioSessionId")
        
        try {
            // Підтверджуємо біометричну аутентифікацію
            val result = scmClient.confirmWithBiometrics(
                userId = userId,
                signedChallenge = signedChallenge,
                bioSessionId = bioSessionId
            )
            
            if (result.success) {
                logger.info("Biometric authentication successful for user: $userId")
                
                // Створюємо Evidence Package для аудиту
                createEvidencePackageForAuthentication(userId, result)
            } else {
                logger.warn("Biometric authentication failed for user: $userId, error: ${result.errorMessage}")
            }
            
            return result
            
        } catch (e: Exception) {
            logger.error("Error confirming biometric authentication", e)
            throw e
        }
    }

    /**
     * Приклад подачі апеляції з біометричним підписом
     */
    fun submitAppealWithBiometricSignature(
        recordHash: String,
        text: String,
        evidenceUrl: String,
        biometricSignature: String,
        bioSessionId: String
    ): Boolean {
        logger.info("Submitting appeal with biometric signature for record: $recordHash")
        
        try {
            val appeal = AppealRequestWithSignature(
                recordHash = recordHash,
                text = text,
                evidenceUrl = evidenceUrl,
                biometricSignature = biometricSignature,
                bioSessionId = bioSessionId,
                additionalContext = mapOf(
                    "appeal_type" to "bid_rejection",
                    "timestamp" to Instant.now().toString()
                )
            )
            
            // Подаемо апеляцію з біометричним підписом
            val success = scmClient.submitAppealWithSignature(appeal)
            
            if (success) {
                logger.info("Appeal with biometric signature submitted successfully")
            } else {
                logger.warn("Failed to submit appeal with biometric signature")
            }
            
            return success
            
        } catch (e: Exception) {
            logger.error("Error submitting appeal with biometric signature", e)
            return false
        }
    }

    /**
     * Приклад WebAuthn реєстрації пристрою
     */
    fun registerWebAuthnDevice(
        userId: String,
        credentialId: String,
        publicKeyPem: String,
        deviceName: String? = null
    ): Boolean {
        logger.info("Registering WebAuthn device for user: $userId")
        
        try {
            // Зберігаємо WebAuthn credential
            val success = biometricHandler.saveWebAuthnCredential(
                userId = userId,
                credentialId = credentialId,
                publicKeyPem = publicKeyPem,
                authenticatorType = AuthenticatorType.PLATFORM,
                deviceName = deviceName
            )
            
            if (success) {
                logger.info("WebAuthn device registered successfully for user: $userId")
            } else {
                logger.warn("Failed to register WebAuthn device for user: $userId")
            }
            
            return success
            
        } catch (e: Exception) {
            logger.error("Error registering WebAuthn device", e)
            return false
        }
    }

    /**
     * Приклад WebAuthn аутентифікації
     */
    fun authenticateWithWebAuthn(
        userId: String,
        signedChallenge: SignedChallenge
    ): BiometricVerificationResult {
        logger.info("Authenticating user with WebAuthn: $userId")
        
        try {
            // Перевіряємо WebAuthn підпис
            val result = biometricHandler.verifyWebAuthnSignature(signedChallenge, userId)
            
            if (result.success) {
                logger.info("WebAuthn authentication successful for user: $userId")
                
                // Створюємо Evidence Package для WebAuthn аутентифікації
                createEvidencePackageForWebAuthn(userId, signedChallenge, result)
            } else {
                logger.warn("WebAuthn authentication failed for user: $userId")
            }
            
            return result
            
        } catch (e: Exception) {
            logger.error("Error authenticating with WebAuthn", e)
            throw e
        }
    }

    /**
     * Створює Evidence Package для біометричної аутентифікації
     */
    private fun createEvidencePackageForAuthentication(
        userId: String,
        authResult: AuthResult
    ) {
        val evidencePackage = EvidencePackage(
            operationId = UUID.randomUUID().toString(),
            userId = userId,
            operationType = "BIOMETRIC_AUTHENTICATION",
            timestamp = Instant.now(),
            ipAddress = "127.0.0.1", // Має бути отримано з контексту
            userAgent = "FreightMarketplace/1.0",
            geoLocation = GeoLocation(
                latitude = 0.0,
                longitude = 0.0
            ),
            biometricEvidence = BiometricallySignedEvidence(
                biometricallySigned = authResult.success,
                authType = authResult.authType,
                bioSessionId = authResult.bioSessionId,
                signaturePayload = authResult.signedChallenge,
                signedAt = authResult.authenticatedAt
            ),
            additionalEvidence = mapOf(
                "operation" to "bid_placement",
                "authentication_method" to "biometric"
            )
        )

        // Логуємо Evidence Package
        logger.info("Evidence Package created for biometric authentication: ${evidencePackage.operationId}")
    }

    /**
     * Створює Evidence Package для WebAuthn аутентифікації
     */
    private fun createEvidencePackageForWebAuthn(
        userId: String,
        signedChallenge: SignedChallenge,
        verificationResult: BiometricVerificationResult
    ) {
        val evidencePackage = EvidencePackage(
            operationId = UUID.randomUUID().toString(),
            userId = userId,
            operationType = "WEBAUTHN_AUTHENTICATION",
            timestamp = Instant.now(),
            ipAddress = "127.0.0.1",
            userAgent = "FreightMarketplace/1.0",
            geoLocation = GeoLocation(
                latitude = 0.0,
                longitude = 0.0
            ),
            biometricEvidence = BiometricallySignedEvidence(
                biometricallySigned = verificationResult.success,
                authType = BiometricAuthType.WEB_AUTHN,
                bioSessionId = signedChallenge.challengeId,
                signaturePayload = signedChallenge.signedChallenge,
                signedAt = verificationResult.timestamp
            ),
            additionalEvidence = mapOf(
                "credential_id" to signedChallenge.credentialId,
                "user_handle" to signedChallenge.userHandle,
                "authentication_method" to "webauthn"
            )
        )

        // Логуємо Evidence Package
        logger.info("Evidence Package created for WebAuthn authentication: ${evidencePackage.operationId}")
    }

    /**
     * Приклад використання у Spring Boot сервісі
     */
    /*
    @Service
    class BiometricBidService {
        
        private val scmClient = SCMClientFactory.createProductionClient("scm-service", 9090)
        
        fun placeHighRiskBid(bidRequest: HighRiskBidRequest): BidResult {
            return try {
                // Виконуємо валідацію з біометричною перевіркою
                val result = scmClient.validateCostWithBiometric(
                    userId = bidRequest.userId,
                    orderId = bidRequest.orderId,
                    category = "BID_PLACEMENT",
                    value = bidRequest.amount,
                    lat = bidRequest.latitude,
                    lon = bidRequest.longitude
                )
                
                when (result.biometricStatus) {
                    BiometricStatus.REQUIRED -> {
                        // Повертаємо BiometricException для фронтенду
                        throw BiometricException(
                            message = "High risk bid requires biometric verification",
                            challenge = result.biometricChallenge!!,
                            operation = "BID_PLACEMENT"
                        )
                    }
                    BiometricStatus.VERIFIED -> {
                        // Створюємо ставку
                        createBid(bidRequest)
                    }
                    else -> {
                        throw ScmSecurityBlockException(
                            message = "Bid placement blocked",
                            reason = result.reason,
                            operation = "BID_PLACEMENT"
                        )
                    }
                }
                
            } catch (e: BiometricException) {
                // Логуємо потребу біометричної перевірки
                logger.warn("Biometric verification required for bid: ${e.message}")
                throw e
            }
        }
        
        fun confirmBiometricBid(
            userId: String,
            signedChallenge: String,
            bioSessionId: String
        ): AuthResult {
            return scmClient.confirmWithBiometrics(
                userId = userId,
                signedChallenge = signedChallenge,
                bioSessionId = bioSessionId
            )
        }
    }
    */

    /**
     * Приклад обробки BiometricException у контролері
     */
    /*
    @RestController
    @RequestMapping("/api/bids")
    class BiometricBidController {
        
        @PostMapping("/place")
        fun placeBid(@RequestBody bidRequest: BidRequest): ResponseEntity<Any> {
            try {
                val result = biometricBidService.placeHighRiskBid(bidRequest)
                return ResponseEntity.ok(result)
            } catch (e: BiometricException) {
                // Повертаємо challenge для фронтенду
                val biometricDetails = e.getBiometricDetails()
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf(
                        "error" to "biometric_required",
                        "challenge" to biometricDetails.challenge,
                        "message" to biometricDetails.message
                    ))
            }
        }
        
        @PostMapping("/confirm-biometric")
        fun confirmBiometric(
            @RequestBody request: BiometricConfirmationRequest
        ): ResponseEntity<AuthResult> {
            val result = biometricBidService.confirmBiometricBid(
                userId = request.userId,
                signedChallenge = request.signedChallenge,
                bioSessionId = request.bioSessionId
            )
            
            return ResponseEntity.ok(result)
        }
    }
    */

    /**
     * Встановлює контекст запиту для поточного потоку
     */
    private fun setRequestContext(userId: String, latitude: Double, longitude: Double) {
        val context = ContextInterceptor.RequestContext(
            userId = userId,
            ipAddress = "127.0.0.1", // Має бути отримано з HTTP запиту
            userAgent = "FreightMarketplace/1.0",
            geoLat = latitude,
            geoLon = longitude,
            deviceId = "web-browser-$userId",
            requestId = UUID.randomUUID().toString()
        )
        
        ContextInterceptor().setContext(context)
    }

    /**
     * Закриття клієнта
     */
    fun shutdown() {
        scmClient.shutdown()
        logger.info("Biometric Marketplace Example shutdown completed")
    }
}