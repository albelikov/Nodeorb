package com.nodeorb.scmclient.logging

import com.nodeorb.scmclient.config.SCMClientConfig
import com.nodeorb.scmclient.model.ValidationContext
import com.nodeorb.scmclient.model.ValidationResult
import com.nodeorb.scmclient.model.AppealRequest
import com.nodeorb.scmclient.model.AccessCheckRequest
import com.nodeorb.scmclient.model.AccessCheckResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Автоматичне логування для SCM Client
 * Надає зручне логування кожного запиту та відповіді для легкого дебагу
 */
class SCMLogger(private val config: SCMClientConfig) {
    
    private val logger: Logger = LoggerFactory.getLogger(SCMLogger::class.java)

    /**
     * Логує початок запиту
     */
    fun logRequest(operationName: String, request: Any) {
        if (!config.enableLogging) return

        when (request) {
            is ValidationContext -> logValidationRequest(operationName, request)
            is AppealRequest -> logAppealRequest(operationName, request)
            is AccessCheckRequest -> logAccessCheckRequest(operationName, request)
            else -> logGenericRequest(operationName, request)
        }
    }

    /**
     * Логує відповідь
     */
    fun logResponse(operationName: String, request: Any, response: Any) {
        if (!config.enableLogging) return

        when (response) {
            is ValidationResult -> logValidationResponse(operationName, request, response)
            is Boolean -> logAppealResponse(operationName, request, response)
            is AccessCheckResult -> logAccessCheckResponse(operationName, request, response)
            else -> logGenericResponse(operationName, request, response)
        }
    }

    /**
     * Логує помилку
     */
    fun logError(operationName: String, request: Any, error: Throwable) {
        if (!config.enableLogging) return

        logger.error(
            "SCM Client error in operation: $operationName, request: ${serializeRequest(request)}, error: ${error.message}",
            error
        )
    }

    /**
     * Логує валідаційний запит
     */
    private fun logValidationRequest(operationName: String, request: ValidationContext) {
        logger.info(
            "SCM Client: {} - userId: {}, orderId: {}, category: {}, value: {}, lat: {}, lon: {}",
            operationName,
            request.userId,
            request.orderId,
            request.category,
            request.value,
            request.latitude,
            request.longitude
        )
    }

    /**
     * Логує валідаційну відповідь
     */
    private fun logValidationResponse(
        operationName: String,
        request: ValidationContext,
        response: ValidationResult
    ) {
        logger.info(
            "SCM Client: {} - userId: {}, orderId: {}, allowed: {}, reason: {}, riskScore: {}, policyId: {}, scmOffline: {}",
            operationName,
            request.userId,
            request.orderId,
            response.allowed,
            response.reason,
            response.riskScore,
            response.policyId,
            response.scmOffline
        )
    }

    /**
     * Логує запит апеляції
     */
    private fun logAppealRequest(operationName: String, request: AppealRequest) {
        logger.info(
            "SCM Client: {} - recordHash: {}, text: {}, evidenceUrl: {}",
            operationName,
            request.recordHash,
            request.text.take(100), // Обмежуємо довжину тексту
            request.evidenceUrl
        )
    }

    /**
     * Логує відповідь апеляції
     */
    private fun logAppealResponse(operationName: String, request: AppealRequest, response: Boolean) {
        logger.info(
            "SCM Client: {} - recordHash: {}, success: {}",
            operationName,
            request.recordHash,
            response
        )
    }

    /**
     * Логує запит перевірки доступу
     */
    private fun logAccessCheckRequest(operationName: String, request: AccessCheckRequest) {
        logger.info(
            "SCM Client: {} - userId: {}, action: {}, context: {}",
            operationName,
            request.userId,
            request.action,
            request.context
        )
    }

    /**
     * Логує відповідь перевірки доступу
     */
    private fun logAccessCheckResponse(
        operationName: String,
        request: AccessCheckRequest,
        response: AccessCheckResult
    ) {
        logger.info(
            "SCM Client: {} - userId: {}, action: {}, allowed: {}, reason: {}, policyId: {}, scmOffline: {}",
            operationName,
            request.userId,
            request.action,
            response.allowed,
            response.reason,
            response.policyId,
            response.scmOffline
        )
    }

    /**
     * Логує загальний запит
     */
    private fun logGenericRequest(operationName: String, request: Any) {
        logger.info(
            "SCM Client: {} - request: {}",
            operationName,
            serializeRequest(request)
        )
    }

    /**
     * Логує загальну відповідь
     */
    private fun logGenericResponse(operationName: String, request: Any, response: Any) {
        logger.info(
            "SCM Client: {} - request: {}, response: {}",
            operationName,
            serializeRequest(request),
            serializeResponse(response)
        )
    }

    /**
     * Сереалізує запит для логування
     */
    private fun serializeRequest(request: Any): String {
        return try {
            when (request) {
                is ValidationContext -> "ValidationContext(userId=${request.userId}, orderId=${request.orderId}, category=${request.category}, value=${request.value})"
                is AppealRequest -> "AppealRequest(recordHash=${request.recordHash}, text=${request.text.take(50)})"
                is AccessCheckRequest -> "AccessCheckRequest(userId=${request.userId}, action=${request.action}, context=${request.context.size} items)"
                else -> request.toString()
            }
        } catch (e: Exception) {
            "Serialization error: ${e.message}"
        }
    }

    /**
     * Сереалізує відповідь для логування
     */
    private fun serializeResponse(response: Any): String {
        return try {
            when (response) {
                is ValidationResult -> "ValidationResult(allowed=${response.allowed}, reason=${response.reason}, riskScore=${response.riskScore})"
                is AccessCheckResult -> "AccessCheckResult(allowed=${response.allowed}, reason=${response.reason})"
                is Boolean -> response.toString()
                else -> response.toString()
            }
        } catch (e: Exception) {
            "Serialization error: ${e.message}"
        }
    }

    /**
     * Логує метрики Circuit Breaker
     */
    fun logCircuitBreakerMetrics(metrics: Any) {
        if (!config.enableLogging) return

        logger.info("SCM Client Circuit Breaker Metrics: {}", metrics)
    }

    /**
     * Логує конфігурацію клієнта (без чутливих даних)
     */
    fun logClientConfig() {
        if (!config.enableLogging) return

        logger.info(
            "SCM Client Configuration: host={}, port={}, maxRetries={}, defaultAllowOnFailure={}, enableLogging={}",
            config.host,
            config.port,
            config.maxRetries,
            config.defaultAllowOnFailure,
            config.enableLogging
        )
    }
}