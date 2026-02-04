package com.nodeorb.freight.marketplace.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO для отображения прогресса заказа
 */
data class OrderProgressDto(
    val masterOrderId: UUID,
    val totalWeight: BigDecimal,
    val totalVolume: BigDecimal,
    val remainingWeight: BigDecimal,
    val remainingVolume: BigDecimal,
    val filledPercentage: Double,
    val status: String,
    val progressStatus: ProgressStatus,
    val committedWeight: BigDecimal,
    val committedVolume: BigDecimal,
    val pendingWeight: BigDecimal,
    val pendingVolume: BigDecimal,
    val openWeight: BigDecimal,
    val openVolume: BigDecimal,
    val partialOrders: List<PartialOrderProgressDto>,
    val estimatedCompletionTime: LocalDateTime?,
    val autoCancellationTime: LocalDateTime?
)

/**
 * DTO для отображения прогресса частичного заказа
 */
data class PartialOrderProgressDto(
    val partialOrderId: UUID,
    val weight: BigDecimal,
    val volume: BigDecimal,
    val percentage: Double,
    val status: String,
    val assignedCarrierId: UUID?,
    val estimatedPickupTime: LocalDateTime?,
    val estimatedDeliveryTime: LocalDateTime?
)

/**
 * Статус прогресса заказа
 */
enum class ProgressStatus {
    OPEN,           // Заказ открыт для ставок
    PENDING,        // Заказ частично заполнен, ожидает завершения
    COMMITTED,      // Заказ полностью заполнен и подтвержден
    IN_PROGRESS,    // Заказ в процессе выполнения
    COMPLETED,      // Заказ завершен
    CANCELLED       // Заказ отменен
)

/**
 * DTO для автоматической проверки заполненности заказа
 */
data class OrderAutoCheckDto(
    val masterOrderId: UUID,
    val shipperId: UUID,
    val pickupTime: LocalDateTime,
    val currentFilledPercentage: Double,
    val minLoadPercentage: Double,
    val isAutoCancellationEnabled: Boolean,
    val cancellationTime: LocalDateTime,
    val partialOrders: List<PartialOrderAutoCheckDto>
)

/**
 * DTO для проверки частичного заказа
 */
data class PartialOrderAutoCheckDto(
    val partialOrderId: UUID,
    val status: String,
    val assignedCarrierId: UUID?,
    val assignedBidId: UUID?,
    val weight: BigDecimal,
    val volume: BigDecimal,
    val isExpired: Boolean,
    val timeUntilExpiration: Long? // в минутах
)

/**
 * Результат автоматической проверки заказа
 */
data class AutoCheckResult(
    val masterOrderId: UUID,
    val action: AutoCheckAction,
    val reason: String,
    val affectedPartialOrders: List<UUID> = emptyList(),
    val timestamp: LocalDateTime
)

/**
 * Действие при автоматической проверке
 */
enum class AutoCheckAction {
    NONE,                   // Нет действий
    CANCEL_PARTIAL_ORDER,   // Отменить частичный заказ
    CANCEL_MASTER_ORDER,    // Отменить мастер-заказ
    SEND_REMINDER,          // Отправить напоминание
    ESCALATE_TO_SHIPPER     // Передать на ручное управление
}