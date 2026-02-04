package com.nodeorb.freight.marketplace.dto

import java.math.BigDecimal

/**
 * Результат проверки квоты перевозчика
 */
data class QuotaCheckResult(
    val isWithinQuota: Boolean,
    val currentLoad: BigDecimal,
    val quotaLimit: BigDecimal,
    val availableCapacity: BigDecimal,
    val violations: List<String> = emptyList()
)

/**
 * Данные о квоте перевозчика из SCM
 */
data class CarrierQuotaInfo(
    val carrierId: java.util.UUID,
    val quotaLimit: BigDecimal,
    val currentActiveLoad: BigDecimal,
    val pendingLoad: BigDecimal,
    val committedLoad: BigDecimal,
    val lastUpdated: java.time.LocalDateTime
)