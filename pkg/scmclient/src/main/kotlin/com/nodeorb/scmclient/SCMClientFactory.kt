package com.nodeorb.scmclient

import com.nodeorb.scmclient.config.SCMClientConfig
import com.nodeorb.scmclient.config.CircuitBreakerConfig
import com.nodeorb.scmclient.exception.ScmSecurityBlockException
import com.nodeorb.scmclient.model.ValidationResult
import com.nodeorb.scmclient.model.ValidationContext
import com.nodeorb.scmclient.model.AppealRequest
import com.nodeorb.scmclient.model.AccessCheckRequest
import com.nodeorb.scmclient.model.AccessCheckResult
import com.nodeorb.scmclient.interceptor.ContextInterceptor
import com.nodeorb.scmclient.resilience.CircuitBreakerManager
import com.nodeorb.scmclient.logging.SCMLogger
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import io.grpc.stub.MetadataUtils
import io.grpc.Metadata
import io.grpc.Status
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Фабрика для створення SCM Client
 * Надає зручний спосіб створення клієнта з різними конфігураціями
 */
object SCMClientFactory {

    private val logger = LoggerFactory.getLogger(SCMClientFactory::class.java)

    /**
     * Створює SCM Client з базовою конфігурацією
     */
    fun createClient(host: String, port: Int): SCMClient {
        val config = SCMClientConfig(
            host = host,
            port = port
        )
        return createClient(config)
    }

    /**
     * Створює SCM Client з повною конфігурацією
     */
    fun createClient(config: SCMClientConfig): SCMClient {
        logger.info("Creating SCM Client with config: host=${config.host}, port=${config.port}")
        
        return SCMClient(config)
    }

    /**
     * Створює SCM Client для production середовища
     */
    fun createProductionClient(host: String, port: Int): SCMClient {
        val config = SCMClientConfig(
            host = host,
            port = port,
            maxRetries = 5,
            keepAliveTime = 60,
            keepAliveTimeout = 10,
            operationTimeout = 60,
            defaultAllowOnFailure = false, // FAIL_CLOSED для production
            enableLogging = true,
            logLevel = "INFO",
            circuitBreakerConfig = CircuitBreakerConfig(
                failureRateThreshold = 50.0,
                minimumNumberOfCalls = 10,
                waitDurationInOpenState = java.time.Duration.ofSeconds(60),
                slidingWindowSize = 20,
                slidingWindowType = "COUNT_BASED",
                enabled = true
            )
        )
        return createClient(config)
    }

    /**
     * Створює SCM Client для development середовища
     */
    fun createDevelopmentClient(host: String, port: Int): SCMClient {
        val config = SCMClientConfig(
            host = host,
            port = port,
            maxRetries = 3,
            keepAliveTime = 30,
            keepAliveTimeout = 5,
            operationTimeout = 30,
            defaultAllowOnFailure = true, // FAIL_OPEN для development
            enableLogging = true,
            logLevel = "DEBUG",
            circuitBreakerConfig = CircuitBreakerConfig(
                failureRateThreshold = 25.0,
                minimumNumberOfCalls = 5,
                waitDurationInOpenState = java.time.Duration.ofSeconds(30),
                slidingWindowSize = 10,
                slidingWindowType = "COUNT_BASED",
                enabled = true
            )
        )
        return createClient(config)
    }

    /**
     * Створює SCM Client для тестового середовища
     */
    fun createTestClient(host: String, port: Int): SCMClient {
        val config = SCMClientConfig(
            host = host,
            port = port,
            maxRetries = 1,
            keepAliveTime = 10,
            keepAliveTimeout = 2,
            operationTimeout = 10,
            defaultAllowOnFailure = true, // FAIL_OPEN для тестів
            enableLogging = true,
            logLevel = "DEBUG",
            circuitBreakerConfig = CircuitBreakerConfig(
                failureRateThreshold = 10.0,
                minimumNumberOfCalls = 2,
                waitDurationInOpenState = java.time.Duration.ofSeconds(10),
                slidingWindowSize = 5,
                slidingWindowType = "COUNT_BASED",
                enabled = false // Вимкнено для тестів
            )
        )
        return createClient(config)
    }

    /**
     * Створює SCM Client з кастомною стратегією обробки помилок
     */
    fun createClientWithStrategy(
        host: String,
        port: Int,
        failOpenOnFailure: Boolean
    ): SCMClient {
        val config = SCMClientConfig(
            host = host,
            port = port,
            defaultAllowOnFailure = failOpenOnFailure
        )
        return createClient(config)
    }

    /**
     * Створює SCM Client з TLS підключенням
     */
    fun createSecureClient(
        host: String,
        port: Int,
        useTls: Boolean = true
    ): SCMClient {
        val config = SCMClientConfig(
            host = host,
            port = port,
            defaultAllowOnFailure = false
        )
        
        // Для production використовувати TLS
        if (useTls) {
            logger.info("Creating secure SCM Client with TLS")
        } else {
            logger.warn("Creating SCM Client without TLS (use only for development)")
        }
        
        return createClient(config)
    }
}