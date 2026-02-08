package com.nodeorb.scmclient.example

import com.nodeorb.scmclient.SCMClientFactory
import com.nodeorb.scmclient.SCMClient
import com.nodeorb.scmclient.config.SCMClientConfig
import com.nodeorb.scmclient.exception.ScmSecurityBlockException
import com.nodeorb.scmclient.model.ValidationResult
import com.nodeorb.scmclient.model.ValidationContext
import com.nodeorb.scmclient.model.AppealRequest
import com.nodeorb.scmclient.model.AccessCheckRequest
import com.nodeorb.scmclient.model.AccessCheckResult
import com.nodeorb.scmclient.interceptor.ContextInterceptor
import org.slf4j.LoggerFactory

/**
 * Приклад використання SCM Client у Freight Marketplace
 * Демонструє, як інтегрувати SDK у мікросервіс
 */
class FreightMarketplaceExample {

    companion object {
        private val logger = LoggerFactory.getLogger(FreightMarketplaceExample::class.java)
    }

    // SCM Client для Freight Marketplace
    private val scmClient: SCMClient

    init {
        // Створюємо SCM Client для Freight Marketplace
        // Використовуємо фабрику для створення production клієнта
        scmClient = SCMClientFactory.createProductionClient(
            host = "scm-service",
            port = 9090
        )
    }

    /**
     * Приклад використання validateCost для перевірки вартості ставки
     */
    fun validateBidCost(
        userId: String,
        orderId: String,
        bidAmount: Double,
        latitude: Double,
        longitude: Double
    ): ValidationResult {
        logger.info("Validating bid cost for user: $userId, order: $orderId, amount: $bidAmount")
        
        try {
            // Встановлюємо контекст для поточного запиту
            setRequestContext(userId, latitude, longitude)
            
            // Викликаємо SCM для валідації вартості
            val result = scmClient.validateCost(
                userId = userId,
                orderId = orderId,
                category = "BID_PLACEMENT",
                value = bidAmount,
                lat = latitude,
                lon = longitude
            )
            
            logger.info("Bid cost validation result: allowed=${result.allowed}, reason=${result.reason}")
            
            return result
            
        } catch (e: ScmSecurityBlockException) {
            logger.warn("Bid cost validation blocked by SCM: ${e.message}")
            
            // Повертаємо результат з блокуванням
            return ValidationResult(
                allowed = false,
                reason = e.reason,
                riskScore = 100.0,
                policyId = null,
                scmOffline = false
            )
        } catch (e: Exception) {
            logger.error("Error validating bid cost", e)
            throw e
        }
    }

    /**
     * Приклад використання submitAppeal для подання апеляції
     */
    fun submitBidAppeal(
        recordHash: String,
        reason: String,
        evidenceUrl: String
    ): Boolean {
        logger.info("Submitting appeal for record: $recordHash")
        
        try {
            val appealRequest = AppealRequest(
                recordHash = recordHash,
                text = reason,
                evidenceUrl = evidenceUrl
            )
            
            // Встановлюємо контекст
            setRequestContext("system", 0.0, 0.0)
            
            // Подаемо апеляцію
            val success = scmClient.submitAppeal(
                recordHash = appealRequest.recordHash,
                text = appealRequest.text,
                evidenceUrl = appealRequest.evidenceUrl
            )
            
            logger.info("Appeal submission result: $success")
            return success
            
        } catch (e: Exception) {
            logger.error("Error submitting appeal", e)
            return false
        }
    }

    /**
     * Приклад використання checkAccess для перевірки доступу до дії
     */
    fun checkUserAccess(
        userId: String,
        action: String,
        orderId: String,
        orderType: String
    ): AccessCheckResult {
        logger.info("Checking access for user: $userId, action: $action, order: $orderId")
        
        try {
            val context = mapOf(
                "order_id" to orderId,
                "order_type" to orderType,
                "timestamp" to System.currentTimeMillis().toString()
            )
            
            // Встановлюємо контекст
            setRequestContext(userId, 0.0, 0.0)
            
            // Перевіряємо доступ
            val result = scmClient.checkAccess(
                userId = userId,
                action = action,
                context = context
            )
            
            logger.info("Access check result: allowed=${result.allowed}, reason=${result.reason}")
            return result
            
        } catch (e: Exception) {
            logger.error("Error checking access", e)
            throw e
        }
    }

    /**
     * Приклад комплексної перевірки перед розміщенням ставки
     */
    fun validateBidPlacement(
        userId: String,
        orderId: String,
        orderType: String,
        bidAmount: Double,
        latitude: Double,
        longitude: Double
    ): ValidationResult {
        logger.info("Validating bid placement for user: $userId, order: $orderId")
        
        // 1. Перевірка доступу до дії
        val accessResult = checkUserAccess(
            userId = userId,
            action = "PLACE_BID",
            orderId = orderId,
            orderType = orderType
        )
        
        if (!accessResult.allowed) {
            logger.warn("User access denied: ${accessResult.reason}")
            return ValidationResult(
                allowed = false,
                reason = "Access denied: ${accessResult.reason}",
                riskScore = 100.0,
                policyId = accessResult.policyId,
                scmOffline = accessResult.scmOffline
            )
        }
        
        // 2. Перевірка вартості ставки
        val costResult = validateBidCost(
            userId = userId,
            orderId = orderId,
            bidAmount = bidAmount,
            latitude = latitude,
            longitude = longitude
        )
        
        return costResult
    }

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
            deviceId = "web-browser-${userId}",
            requestId = java.util.UUID.randomUUID().toString()
        )
        
        ContextInterceptor().setContext(context)
    }

    /**
     * Приклад використання метрик Circuit Breaker
     */
    fun getCircuitBreakerStatus(): String {
        val metrics = scmClient.getCircuitBreakerMetrics()
        
        return """
            Circuit Breaker Status:
            - State: ${metrics.state}
            - Failure Rate: ${metrics.failureRate}%
            - Total Calls: ${metrics.numberOfCalls}
            - Failed Calls: ${metrics.numberOfFailedCalls}
            - Successful Calls: ${metrics.numberOfSuccessfulCalls}
            - Not Permitted Calls: ${metrics.numberOfNotPermittedCalls}
        """.trimIndent()
    }

    /**
     * Приклад конфігурації для різних середовищ
     */
    fun createEnvironmentSpecificClient(environment: String): SCMClient {
        return when (environment.uppercase()) {
            "PRODUCTION" -> SCMClientFactory.createProductionClient("scm-service-prod", 9090)
            "DEVELOPMENT" -> SCMClientFactory.createDevelopmentClient("scm-service-dev", 9090)
            "TEST" -> SCMClientFactory.createTestClient("scm-service-test", 9090)
            else -> SCMClientFactory.createClient("localhost", 9090)
        }
    }

    /**
     * Приклад обробки помилок SCM
     */
    fun handleScmError(userId: String, orderId: String, error: Exception): ValidationResult {
        return when (error) {
            is ScmSecurityBlockException -> {
                logger.warn("SCM security block for user: $userId, order: $orderId")
                
                // Логуємо блокування для подальшого аналізу
                logger.info("Security block details: ${error.getBlockDetails()}")
                
                // Повертаємо результат з блокуванням
                ValidationResult(
                    allowed = false,
                    reason = error.reason,
                    riskScore = 100.0,
                    policyId = null,
                    scmOffline = false
                )
            }
            else -> {
                logger.error("Unexpected SCM error for user: $userId, order: $orderId", error)
                
                // Для production можна використовувати FAIL_CLOSED
                ValidationResult(
                    allowed = false,
                    reason = "SCM service error: ${error.message}",
                    riskScore = 0.0,
                    policyId = null,
                    scmOffline = true
                )
            }
        }
    }

    /**
     * Закриття клієнта
     */
    fun shutdown() {
        scmClient.shutdown()
        logger.info("SCM Client shutdown completed")
    }
}

/**
 * Приклад використання у Spring Boot сервісі
 */
/*
@Service
class BidService {
    
    private val scmClient = SCMClientFactory.createProductionClient("scm-service", 9090)
    
    fun placeBid(bidRequest: BidRequest): BidResult {
        return try {
            val validationResult = scmClient.validateCost(
                userId = bidRequest.userId,
                orderId = bidRequest.orderId,
                category = "BID_PLACEMENT",
                value = bidRequest.amount,
                lat = bidRequest.latitude,
                lon = bidRequest.longitude
            )
            
            if (validationResult.allowed) {
                // Створюємо ставку
                createBid(bidRequest)
            } else {
                throw ScmSecurityBlockException(
                    message = "Bid placement blocked by SCM",
                    reason = validationResult.reason,
                    operation = "PLACE_BID"
                )
            }
            
        } catch (e: ScmSecurityBlockException) {
            // Логуємо блокування
            logger.warn("Bid placement blocked: ${e.message}")
            
            // Повертаємо форму апеляції
            BidResult.blocked(e.getBlockDetails())
        }
    }
}
*/