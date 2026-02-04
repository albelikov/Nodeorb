package com.nodeorb.scmclient.exception

/**
 * Виняток, який виникає при блокуванні SCM
 * Використовується для перетворення gRPC PERMISSION_DENIED у зрозумілий для бізнесу виняток
 */
class ScmSecurityBlockException(
    message: String,
    val reason: String,
    val operation: String,
    val request: Any? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Повертає деталі блокування для відображення у фронтенді
     */
    fun getBlockDetails(): BlockDetails {
        return BlockDetails(
            reason = reason,
            operation = operation,
            message = message,
            request = request
        )
    }

    /**
     * Деталі блокування для фронтенду
     */
    data class BlockDetails(
        val reason: String,
        val operation: String,
        val message: String,
        val request: Any? = null
    )
}