package com.freight.marketplace.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * DTO для обновления прогресса заказа
 * Используется для отправки мгновенных обновлений клиентам через WebSocket
 */
data class OrderProgressUpdate(
    /**
     * Идентификатор заказа
     */
    val orderId: UUID,
    
    /**
     * Процент заполненности (уже подтвержденные ставки)
     */
    val committedPercentage: Double,
    
    /**
     * Процент в ожидании (находящиеся на рассмотрении ставки)
     */
    val pendingPercentage: Double,
    
    /**
     * Процент открытых (свободных для ставок)
     */
    val openPercentage: Double,
    
    /**
     * Текущий статус прогресса
     */
    val progressStatus: String,
    
    /**
     * Общий вес заказа
     */
    val totalWeight: BigDecimal,
    
    /**
     * Общий объем заказа
     */
    val totalVolume: BigDecimal,
    
    /**
     * Оставшийся вес
     */
    val remainingWeight: BigDecimal,
    
    /**
     * Оставшийся объем
     */
    val remainingVolume: BigDecimal,
    
    /**
     * Время обновления
     */
    val timestamp: LocalDateTime,
    
    /**
     * Тип события (BidPlaced, BidAwarded, OrderUpdated и т.д.)
     */
    val eventType: String,
    
    /**
     * Дополнительные данные события
     */
    val eventData: Map<String, Any> = emptyMap()
) {
    /**
     * Валидация данных прогресса
     */
    init {
        require(committedPercentage >= 0.0 && committedPercentage <= 1.0) {
            "Committed percentage must be between 0.0 and 1.0"
        }
        require(pendingPercentage >= 0.0 && pendingPercentage <= 1.0) {
            "Pending percentage must be between 0.0 and 1.0"
        }
        require(openPercentage >= 0.0 && openPercentage <= 1.0) {
            "Open percentage must be between 0.0 and 1.0"
        }
        
        val totalPercentage = committedPercentage + pendingPercentage + openPercentage
        require(Math.abs(totalPercentage - 1.0) < 0.001) {
            "Sum of percentages must equal 1.0, but was $totalPercentage"
        }
    }
    
    /**
     * Проверка, изменились ли данные по сравнению с предыдущим обновлением
     */
    fun hasChanges(previousUpdate: OrderProgressUpdate?): Boolean {
        if (previousUpdate == null) return true
        
        return committedPercentage != previousUpdate.committedPercentage ||
               pendingPercentage != previousUpdate.pendingPercentage ||
               openPercentage != previousUpdate.openPercentage ||
               progressStatus != previousUpdate.progressStatus
    }
    
    /**
     * Получение цвета прогресс-бара на основе статуса
     */
    fun getProgressBarColor(): String {
        return when (progressStatus) {
            "COMMITTED" -> "blue"    // Синий - все подтверждено
            "PENDING" -> "yellow"    // Желтый - в ожидании
            "OPEN" -> "gray"         // Серый - свободно
            else -> "gray"
        }
    }
    
    /**
     * Получение уровня заполненности для визуализации
     */
    fun getFillLevel(): Double {
        return committedPercentage + (pendingPercentage * 0.5) // Ожидание на 50% заполнено
    }
}

/**
 * DTO для события обновления прогресса заказа
 */
data class OrderProgressEvent(
    /**
     * Идентификатор заказа
     */
    val orderId: UUID,
    
    /**
     * Данные обновления прогресса
     */
    val progressUpdate: OrderProgressUpdate,
    
    /**
     * Идентификатор пользователя, инициировавшего изменение
     */
    val userId: UUID? = null,
    
    /**
     * Тип действия (PLACE_BID, AWARD_BID, CANCEL_BID и т.д.)
     */
    val actionType: String,
    
    /**
     * Дополнительные метаданные
     */
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * DTO для подключения клиента к обновлениям заказа
 */
data class OrderSubscriptionRequest(
    /**
     * Идентификатор заказа для отслеживания
     */
    val orderId: UUID,
    
    /**
     * Идентификатор пользователя
     */
    val userId: UUID,
    
    /**
     * Роль пользователя (CARRIER, SHIPPER, ADMIN)
     */
    val userRole: String,
    
    /**
     * Время подключения
     */
    val connectedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * DTO для отключения от обновлений заказа
 */
data class OrderUnsubscriptionRequest(
    /**
     * Идентификатор заказа
     */
    val orderId: UUID,
    
    /**
     * Идентификатор пользователя
     */
    val userId: UUID,
    
    /**
     * Время отключения
     */
    val disconnectedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * DTO для состояния подключения к заказу
 */
data class OrderConnectionState(
    /**
     * Идентификатор заказа
     */
    val orderId: UUID,
    
    /**
     * Количество подключенных клиентов
     */
    val connectedClients: Int,
    
    /**
     * Список подключенных пользователей
     */
    val connectedUsers: List<UserConnectionInfo>,
    
    /**
     * Время последнего обновления
     */
    val lastUpdate: LocalDateTime,
    
    /**
     * Текущий прогресс заказа
     */
    val currentProgress: OrderProgressUpdate? = null
)

/**
 * DTO для информации о подключенном пользователе
 */
data class UserConnectionInfo(
    /**
     * Идентификатор пользователя
     */
    val userId: UUID,
    
    /**
     * Роль пользователя
     */
    val userRole: String,
    
    /**
     * Время подключения
     */
    val connectedAt: LocalDateTime,
    
    /**
     * Тип клиента (web, mobile, desktop)
     */
    val clientType: String = "web"
)

/**
 * DTO для массового обновления прогресса нескольких заказов
 */
data class BulkOrderProgressUpdate(
    /**
     * Список обновлений прогресса для разных заказов
     */
    val updates: List<OrderProgressUpdate>,
    
    /**
     * Время отправки обновления
     */
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    /**
     * Идентификатор отправителя
     */
    val senderId: UUID? = null
) {
    /**
     * Проверка, есть ли обновления
     */
    fun hasUpdates(): Boolean = updates.isNotEmpty()
    
    /**
     * Получение количества обновленных заказов
     */
    fun getUpdatedOrderCount(): Int = updates.size
}