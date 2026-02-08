package com.freight.marketplace.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import java.util.*

/**
 * Глобальный обработчик исключений для UI контроллеров
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Обработка валидации DTO
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage
            errors[fieldName] = errorMessage ?: "Invalid value"
        }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Input validation failed",
            errors = errors,
            path = ex.pathVariableNames.joinToString(", ")
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    /**
     * Обработка исключений доступа
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Access Denied",
            message = "You don't have permission to access this resource",
            path = getCurrentRequestPath()
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    /**
     * Обработка исключений аутентификации
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException::class)
    fun handleAuthenticationException(ex: AuthenticationCredentialsNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Authentication Required",
            message = "Authentication credentials are required to access this resource",
            path = getCurrentRequestPath()
        )

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * Обработка исключений бизнес-логики
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleBusinessExceptions(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Business Error",
            message = ex.message ?: "An error occurred",
            path = getCurrentRequestPath()
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    /**
     * Обработка исключений сущностей
     */
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Entity Not Found",
            message = ex.message ?: "Requested entity was not found",
            path = getCurrentRequestPath()
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Обработка исключений SCM
     */
    @ExceptionHandler(ScmValidationException::class)
    fun handleScmValidationException(ex: ScmValidationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "SCM Validation Failed",
            message = ex.message ?: "SCM validation failed",
            path = getCurrentRequestPath(),
            errors = ex.validationErrors
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    /**
     * Обработка исключений Trust Token
     */
    @ExceptionHandler(TokenValidationException::class)
    fun handleTokenValidationException(ex: TokenValidationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Token Validation Failed",
            message = ex.message ?: "Trust token validation failed",
            path = getCurrentRequestPath()
        )

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericExceptions(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = getCurrentRequestPath()
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    // Вспомогательные методы

    private fun getCurrentRequestPath(): String {
        // В реальной системе можно получить путь из HttpServletRequest
        return "unknown"
    }
}

/**
 * DTO для ответа об ошибке
 */
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val errors: Map<String, String>? = null,
    val path: String? = null
)

/**
 * Исключение для сущностей
 */
class EntityNotFoundException(message: String) : RuntimeException(message)

/**
 * Исключение для SCM валидации
 */
class ScmValidationException(message: String, val validationErrors: Map<String, String>? = null) : RuntimeException(message)

/**
 * Исключение для валидации токенов
 */
class TokenValidationException(message: String) : RuntimeException(message)