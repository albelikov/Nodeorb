package com.freight.marketplace.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Исключение для нарушений комплаенса
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class ComplianceViolationException(
    message: String,
    val violations: List<String> = emptyList(),
    val carrierId: String? = null,
    val masterOrderId: String? = null
) : RuntimeException(message) {

    constructor(message: String, violations: List<String>) : this(message, violations, null, null)
    
    constructor(message: String) : this(message, emptyList(), null, null)
}

/**
 * Исключение для ADR нарушений
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class ADRViolationException(
    message: String,
    val carrierId: String,
    val masterOrderId: String,
    val requiredLicense: String
) : RuntimeException(message)

/**
 * Исключение для высокого рискового балла
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class HighRiskScoreException(
    message: String,
    val carrierId: String,
    val masterOrderId: String,
    val riskScore: Double,
    val threshold: Double
) : RuntimeException(message)

/**
 * Исключение для недостаточной квоты
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class InsufficientQuotaException(
    message: String,
    val carrierId: String,
    val requiredQuota: String,
    val availableQuota: String
) : RuntimeException(message)

/**
 * Исключение для отсутствующего SCM снимка
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ScmSnapshotNotFoundException(
    message: String
) : RuntimeException(message)

/**
 * Исключение для необходимости ручного аппрува
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class ManualApprovalRequiredException(
    message: String,
    val carrierId: String,
    val masterOrderId: String,
    val riskScore: Double,
    val reason: String
) : RuntimeException(message)

/**
 * Исключение для блокировки операций из-за нарушений
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class OperationBlockedException(
    message: String,
    val violations: List<String>,
    val blockedOperation: String,
    val carrierId: String? = null,
    val masterOrderId: String? = null
) : RuntimeException(message)

/**
 * Исключение для недопустимых бизнес-правил
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class BusinessRuleViolationException(
    message: String,
    val ruleName: String,
    val details: Map<String, Any> = emptyMap()
) : RuntimeException(message)