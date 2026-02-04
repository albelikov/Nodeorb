package com.nodeorb.scmclient

import com.nodeorb.scmclient.exception.ScmSecurityBlockException
import com.nodeorb.scmclient.model.ValidationResult
import com.nodeorb.scmclient.model.ValidationContext
import com.nodeorb.scmclient.model.AppealRequest
import com.nodeorb.scmclient.model.AccessCheckRequest
import com.nodeorb.scmclient.model.AccessCheckResult
import com.nodeorb.scmclient.config.SCMClientConfig
import com.nodeorb.scmclient.interceptor.ContextInterceptor
import com.nodeorb.scmclient.resilience.CircuitBreakerManager
import com.nodeorb.scmclient.logging.SCMLogger
import com.nodeorb.scmclient.grpc.SCMServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import io.grpc.stub.MetadataUtils
import io.grpc.Metadata
import io.grpc.Status
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * SCM Client SDK для інтеграції з SCM-service
 * Надає простий інтерфейс для виклику SCM операцій з автоматичною обробкою помилок,
 * авторизацією та resilience механізмами
 */
class SCMClient(
    private val config: SCMClientConfig
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(SCMClient::class.java)
    }

    private val channel: ManagedChannel
    private val contextInterceptor: ContextInterceptor
    private val circuitBreakerManager: CircuitBreakerManager
    private val scmLogger: SCMLogger
    private val scmServiceGrpc: SCMServiceGrpc

    init {
        // Створюємо gRPC канал
        channel = ManagedChannelBuilder.forAddress(config.host, config.port)
            .usePlaintext() // Для production використовувати TLS
            .enableRetry()
            .maxRetryAttempts(config.maxRetries)
            .keepAliveTime(config.keepAliveTime, TimeUnit.SECONDS)
            .keepAliveTimeout(config.keepAliveTimeout, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build()

        // Ініціалізуємо компоненти
        contextInterceptor = ContextInterceptor()
        circuitBreakerManager = CircuitBreakerManager(config)
        scmLogger = SCMLogger(config)
        scmServiceGrpc = SCMServiceGrpc(channel, contextInterceptor)

        // Логуємо конфігурацію
        scmLogger.logClientConfig()
    }

    /**
     * Валідація вартості операції
     * 
     * @param userId Ідентифікатор користувача
     * @param orderId Ідентифікатор замовлення
     * @param category Категорія операції
     * @param value Вартість операції
     * @param lat Широта
     * @param lon Довгота
     * @return Результат валідації
     */
    fun validateCost(
        userId: String,
        orderId: String,
        category: String,
        value: Double,
        lat: Double,
        lon: Double
    ): ValidationResult {
        val context = ValidationContext(
            userId = userId,
            orderId = orderId,
            category = category,
            value = value,
            latitude = lat,
            longitude = lon
        )

        return validateCostWithContext(context)
    }

    /**
     * Валідація вартості з контекстом
     */
    private fun validateCostWithContext(context: ValidationContext): ValidationResult {
        val operationName = "validateCost"
        
        return circuitBreakerManager.executeWithCircuitBreaker(operationName) {
            try {
                scmLogger.logRequest(operationName, context)
                
                // Створюємо метадані з контекстом
                val metadata = contextInterceptor.createMetadata(context)
                
                // Викликаємо gRPC метод
                val response = scmServiceGrpc.validateCost(context, metadata)
                
                val result = ValidationResult(
                    allowed = response.allowed,
                    reason = response.reason,
                    riskScore = response.riskScore,
                    policyId = response.policyId,
                    scmOffline = false
                )
                
                scmLogger.logResponse(operationName, context, result)
                result
                
            } catch (e: StatusRuntimeException) {
                handleGrpcError(operationName, context, e)
            } catch (e: Exception) {
                logger.error("Unexpected error in validateCost", e)
                throw e
            }
        }
    }

    /**
     * Подання апеляції
     * 
     * @param recordHash Хеш запису
     * @param text Текст апеляції
     * @param evidenceUrl URL доказів
     * @return True, якщо апеляція прийнята
     */
    fun submitAppeal(
        recordHash: String,
        text: String,
        evidenceUrl: String
    ): Boolean {
        val request = AppealRequest(
            recordHash = recordHash,
            text = text,
            evidenceUrl = evidenceUrl
        )

        return submitAppealWithContext(request)
    }

    /**
     * Подання апеляції з контекстом
     */
    private fun submitAppealWithContext(request: AppealRequest): Boolean {
        val operationName = "submitAppeal"
        
        return circuitBreakerManager.executeWithCircuitBreaker(operationName) {
            try {
                scmLogger.logRequest(operationName, request)
                
                // Створюємо метадані з контекстом
                val metadata = contextInterceptor.createMetadata(request)
                
                // Викликаємо gRPC метод
                val response = scmServiceGrpc.submitAppeal(request, metadata)
                
                scmLogger.logResponse(operationName, request, response)
                response.success
                
            } catch (e: StatusRuntimeException) {
                handleGrpcError(operationName, request, e)
                false
            } catch (e: Exception) {
                logger.error("Unexpected error in submitAppeal", e)
                throw e
            }
        }
    }

    /**
     * Перевірка доступу (для майбутнього OPA)
     * 
     * @param userId Ідентифікатор користувача
     * @param action Дія
     * @param context Контекст
     * @return Результат перевірки доступу
     */
    fun checkAccess(
        userId: String,
        action: String,
        context: Map<String, String>
    ): AccessCheckResult {
        val request = AccessCheckRequest(
            userId = userId,
            action = action,
            context = context
        )

        return checkAccessWithContext(request)
    }

    /**
     * Перевірка доступу з контекстом
     */
    private fun checkAccessWithContext(request: AccessCheckRequest): AccessCheckResult {
        val operationName = "checkAccess"
        
        return circuitBreakerManager.executeWithCircuitBreaker(operationName) {
            try {
                scmLogger.logRequest(operationName, request)
                
                // Створюємо метадані з контекстом
                val metadata = contextInterceptor.createMetadata(request)
                
                // Викликаємо gRPC метод
                val response = scmServiceGrpc.checkAccess(request, metadata)
                
                val result = AccessCheckResult(
                    allowed = response.allowed,
                    reason = response.reason,
                    policyId = response.policyId,
                    scmOffline = false
                )
                
                scmLogger.logResponse(operationName, request, result)
                result
                
            } catch (e: StatusRuntimeException) {
                handleGrpcError(operationName, request, e)
            } catch (e: Exception) {
                logger.error("Unexpected error in checkAccess", e)
                throw e
            }
        }
    }

    /**
     * Обробка gRPC помилок
     */
    private fun <T> handleGrpcError(
        operationName: String,
        request: Any,
        e: StatusRuntimeException
    ): T {
        when (e.status.code) {
            Status.Code.PERMISSION_DENIED -> {
                logger.warn("SCM permission denied for operation: $operationName")
                throw ScmSecurityBlockException(
                    message = "Access denied by SCM policy",
                    reason = e.status.description ?: "Permission denied",
                    operation = operationName,
                    request = request
                )
            }
            Status.Code.UNAVAILABLE,
            Status.Code.DEADLINE_EXCEEDED,
            Status.Code.UNAVAILABLE -> {
                logger.warn("SCM service unavailable: ${e.message}")
                // Повертаємо результат з міткою scm_offline
                @Suppress("UNCHECKED_CAST")
                when (operationName) {
                    "validateCost" -> ValidationResult(
                        allowed = config.defaultAllowOnFailure,
                        reason = "SCM offline",
                        riskScore = 0.0,
                        policyId = null,
                        scmOffline = true
                    ) as T
                    "checkAccess" -> AccessCheckResult(
                        allowed = config.defaultAllowOnFailure,
                        reason = "SCM offline",
                        policyId = null,
                        scmOffline = true
                    ) as T
                    else -> throw e
                }
            }
            else -> {
                logger.error("gRPC error in operation: $operationName", e)
                throw e
            }
        }
    }

    /**
     * Отримує метрики Circuit Breaker
     */
    fun getCircuitBreakerMetrics() = circuitBreakerManager.getCircuitBreakerMetrics()

    /**
     * Скидає метрики Circuit Breaker
     */
    fun resetCircuitBreaker() = circuitBreakerManager.resetCircuitBreaker()

    /**
     * Закриття каналу
     */
    fun shutdown() {
        try {
            scmServiceGrpc.shutdown()
            channel.shutdown()
                .awaitTermination(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            logger.error("Error shutting down SCM client", e)
            Thread.currentThread().interrupt()
        }
    }
}