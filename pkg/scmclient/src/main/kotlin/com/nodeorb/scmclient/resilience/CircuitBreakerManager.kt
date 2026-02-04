package com.nodeorb.scmclient.resilience

import com.nodeorb.scmclient.config.SCMClientConfig
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Менеджер Circuit Breaker для SCM Client
 * Інтегрує Resilience4j для забезпечення стійкості до відмов SCM сервісу
 */
class CircuitBreakerManager(private val config: SCMClientConfig) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(CircuitBreakerManager::class.java)
    }

    private val circuitBreakerRegistry: CircuitBreakerRegistry
    private val retryRegistry: RetryRegistry
    private val circuitBreaker: CircuitBreaker
    private val retry: Retry

    init {
        // Налаштовуємо Circuit Breaker
        val circuitBreakerConfig = CircuitBreakerConfig.custom<CircuitBreakerConfig>()
            .failureRateThreshold(config.circuitBreakerConfig.failureRateThreshold)
            .minimumNumberOfCalls(config.circuitBreakerConfig.minimumNumberOfCalls)
            .waitDurationInOpenState(config.circuitBreakerConfig.waitDurationInOpenState)
            .slidingWindowSize(config.circuitBreakerConfig.slidingWindowSize)
            .slidingWindowType(
                if (config.circuitBreakerConfig.slidingWindowType == "COUNT_BASED") 
                    CircuitBreakerConfig.SlidingWindowType.COUNT_BASED 
                else 
                    CircuitBreakerConfig.SlidingWindowType.TIME_BASED
            )
            .enableAutomaticTransitionFromOpenToHalfOpen()
            .recordExceptions(RuntimeException::class.java)
            .ignoreExceptions(IllegalArgumentException::class.java)
            .build()

        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig)
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("scm-service")

        // Налаштовуємо Retry
        val retryConfig = RetryConfig.custom<RetryConfig>()
            .maxAttempts(config.maxRetries)
            .waitDuration(Duration.ofSeconds(1))
            .retryOnException { throwable -> 
                throwable is RuntimeException && throwable !is IllegalArgumentException 
            }
            .build()

        retryRegistry = RetryRegistry.of(retryConfig)
        retry = retryRegistry.retry("scm-service")

        // Додаємо слухачів для логування подій
        setupEventListeners()
    }

    /**
     * Виконує операцію з Circuit Breaker
     */
    fun <T> executeWithCircuitBreaker(operationName: String, operation: () -> T): T {
        if (!config.circuitBreakerConfig.enabled) {
            logger.debug("Circuit breaker disabled, executing operation directly: $operationName")
            return operation()
        }

        return try {
            // Спробуємо виконати операцію з Circuit Breaker
            val decoratedOperation = CircuitBreaker.decorateCallable(circuitBreaker, operation)
            val result = decoratedOperation.call()
            
            logger.debug("Operation $operationName completed successfully")
            result
            
        } catch (e: Exception) {
            logger.warn("Operation $operationName failed: ${e.message}")
            
            // Перевіряємо стан Circuit Breaker
            when (circuitBreaker.state) {
                CircuitBreaker.State.OPEN -> {
                    logger.warn("Circuit breaker is OPEN, using fallback strategy")
                    handleCircuitBreakerOpen(operationName)
                }
                CircuitBreaker.State.HALF_OPEN -> {
                    logger.warn("Circuit breaker is HALF_OPEN, operation may fail")
                    throw e
                }
                else -> {
                    logger.error("Operation $operationName failed with unknown error")
                    throw e
                }
            }
        }
    }

    /**
     * Обробляє відкритий стан Circuit Breaker
     */
    private fun <T> handleCircuitBreakerOpen(operationName: String): T {
        when (operationName) {
            "validateCost" -> {
                @Suppress("UNCHECKED_CAST")
                return com.nodeorb.scmclient.model.ValidationResult(
                    allowed = config.defaultAllowOnFailure,
                    reason = "SCM service unavailable (Circuit Breaker OPEN)",
                    riskScore = 0.0,
                    policyId = null,
                    scmOffline = true
                ) as T
            }
            "checkAccess" -> {
                @Suppress("UNCHECKED_CAST")
                return com.nodeorb.scmclient.model.AccessCheckResult(
                    allowed = config.defaultAllowOnFailure,
                    reason = "SCM service unavailable (Circuit Breaker OPEN)",
                    policyId = null,
                    scmOffline = true
                ) as T
            }
            "submitAppeal" -> {
                @Suppress("UNCHECKED_CAST")
                return false as T
            }
            else -> {
                throw IllegalStateException("Unknown operation: $operationName")
            }
        }
    }

    /**
     * Налаштовує слухачів подій Circuit Breaker
     */
    private fun setupEventListeners() {
        circuitBreaker.getEventPublisher()
            .onStateTransition { event ->
                logger.info(
                    "Circuit breaker state transition: {} -> {} for operation: {}",
                    event.stateTransition.fromState,
                    event.stateTransition.toState,
                    event.name
                )
            }
            .onFailureRateExceeded { event ->
                logger.warn(
                    "Circuit breaker failure rate exceeded: {}% for operation: {}",
                    event.failureRate,
                    event.name
                )
            }
            .onCallNotPermitted { event ->
                logger.warn(
                    "Circuit breaker call not permitted for operation: {}",
                    event.name
                )
            }

        retry.getEventPublisher()
            .onRetry { event ->
                logger.info(
                    "Retry attempt {} for operation: {}",
                    event.numberOfRetryAttempts,
                    event.name
                )
            }
            .onSuccess { event ->
                logger.info(
                    "Retry succeeded after {} attempts for operation: {}",
                    event.numberOfRetryAttempts,
                    event.name
                )
            }
            .onFailure { event ->
                logger.error(
                    "Retry failed after {} attempts for operation: {}",
                    event.numberOfRetryAttempts,
                    event.name
                )
            }
    }

    /**
     * Отримує статистику Circuit Breaker
     */
    fun getCircuitBreakerMetrics(): CircuitBreakerMetrics {
        return CircuitBreakerMetrics(
            state = circuitBreaker.state.name,
            failureRate = circuitBreaker.metrics.failureRate,
            numberOfNotPermittedCalls = circuitBreaker.metrics.numberOfNotPermittedCalls,
            numberOfCalls = circuitBreaker.metrics.numberOfCalls,
            numberOfFailedCalls = circuitBreaker.metrics.numberOfFailedCalls,
            numberOfSuccessfulCalls = circuitBreaker.metrics.numberOfSuccessfulCalls
        )
    }

    /**
     * Скидає статистику Circuit Breaker
     */
    fun resetCircuitBreaker() {
        circuitBreaker.reset()
        logger.info("Circuit breaker metrics reset")
    }

    /**
     * Метрики Circuit Breaker
     */
    data class CircuitBreakerMetrics(
        val state: String,
        val failureRate: Double,
        val numberOfNotPermittedCalls: Long,
        val numberOfCalls: Long,
        val numberOfFailedCalls: Long,
        val numberOfSuccessfulCalls: Long
    )
}