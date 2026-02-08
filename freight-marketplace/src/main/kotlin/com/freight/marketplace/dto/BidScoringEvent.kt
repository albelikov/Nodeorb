package com.freight.marketplace.dto

import java.util.UUID

/**
 * Событие для асинхронного расчета скоринга ставки
 * Публикуется в Kafka при сохранении новой ставки
 */
data class BidScoringEvent(
    val bidId: UUID,
    val carrierId: UUID,
    val freightOrderId: UUID?,
    val masterOrderId: UUID?,
    val partialOrderId: UUID?,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Результат расчета скоринга
 * Публикуется в Kafka после завершения расчета
 */
data class BidScoringResult(
    val bidId: UUID,
    val matchingScore: Double,
    val scoreBreakdown: String, // JSON строка с детализацией
    val status: ScoringStatus,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ScoringStatus {
    SUCCESS,
    FAILED,
    PARTIAL // часть данных недоступна, но расчет выполнен с пониженной точностью
}