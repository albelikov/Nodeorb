package com.nodeorb.freight.marketplace.service.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.nodeorb.freight.marketplace.entity.deal.ContractStatus
import com.nodeorb.freight.marketplace.service.finance.EscrowService
import com.nodeorb.freight.marketplace.service.deal.DealManagementService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * Сервис маршрутизации уведомлений
 * Преобразует события Kafka в сообщения для пользователей
 */
@Service
class NotificationRouterService(
    private val templateManager: TemplateManager,
    private val notificationSender: NotificationSender
) {

    companion object {
        private val logger = LoggerFactory.getLogger(NotificationRouterService::class.java)
        
        // Топики для прослушивания
        private const val ORDER_EVENTS_TOPIC = "order.events"
        private const val BID_EVENTS_TOPIC = "bid.events"
        private const val FINANCE_EVENTS_TOPIC = "finance.events"
    }

    /**
     * Обрабатывает события заказов
     */
    @KafkaListener(topics = [ORDER_EVENTS_TOPIC], groupId = "notification-service")
    @Transactional
    fun handleOrderEvent(message: String) {
        try {
            val event = parseEvent(message)
            logger.info("Processing order event: ${event.type}")
            
            when (event.type) {
                "ORDER_CANCELLED" -> handleOrderCancelled(event)
                "ORDER_AWARDED" -> handleOrderAwarded(event)
                "ORDER_PROGRESS_UPDATE" -> handleOrderProgressUpdate(event)
                else -> logger.debug("Unhandled order event type: ${event.type}")
            }
        } catch (e: Exception) {
            logger.error("Error processing order event: $message", e)
        }
    }

    /**
     * Обрабатывает события ставок
     */
    @KafkaListener(topics = [BID_EVENTS_TOPIC], groupId = "notification-service")
    @Transactional
    fun handleBidEvent(message: String) {
        try {
            val event = parseEvent(message)
            logger.info("Processing bid event: ${event.type}")
            
            when (event.type) {
                "BID_PLACED" -> handleBidPlaced(event)
                "BID_AWARDED" -> handleBidAwarded(event)
                "BID_REJECTED" -> handleBidRejected(event)
                "SNIPER_BID_DETECTED" -> handleSniperBidDetected(event)
                else -> logger.debug("Unhandled bid event type: ${event.type}")
            }
        } catch (e: Exception) {
            logger.error("Error processing bid event: $message", e)
        }
    }

    /**
     * Обрабатывает финансовые события
     */
    @KafkaListener(topics = [FINANCE_EVENTS_TOPIC], groupId = "notification-service")
    @Transactional
    fun handleFinanceEvent(message: String) {
        try {
            val event = parseEvent(message)
            logger.info("Processing finance event: ${event.type}")
            
            when (event.type) {
                "ESCROW_LOCKED" -> handleEscrowLocked(event)
                "ESCROW_FUNDING_CONFIRMED" -> handleEscrowFundingConfirmed(event)
                "ESCROW_RELEASED" -> handleEscrowReleased(event)
                "ESCROW_DISPUTED" -> handleEscrowDisputed(event)
                else -> logger.debug("Unhandled finance event type: ${event.type}")
            }
        } catch (e: Exception) {
            logger.error("Error processing finance event: $message", e)
        }
    }

    /**
     * Обработка отмены заказа (P0)
     */
    private fun handleOrderCancelled(event: Event) {
        val orderId = event.data["orderId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P0,
            channels = setOf(Channel.PUSH, Channel.SMS),
            templateKey = "order.cancelled",
            templateData = mapOf(
                "orderId" to orderId,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка подтверждения заказа (P0)
     */
    private fun handleOrderAwarded(event: Event) {
        val orderId = event.data["orderId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P0,
            channels = setOf(Channel.PUSH, Channel.SMS),
            templateKey = "order.awarded",
            templateData = mapOf(
                "orderId" to orderId,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка обновления прогресса заказа (P2)
     */
    private fun handleOrderProgressUpdate(event: Event) {
        val orderId = event.data["orderId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val progress = event.data["progress"] as? String ?: "0%"
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P2,
            channels = setOf(Channel.IN_APP),
            templateKey = "order.progress.update",
            templateData = mapOf(
                "orderId" to orderId,
                "progress" to progress,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка размещения ставки (P1)
     */
    private fun handleBidPlaced(event: Event) {
        val bidId = event.data["bidId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val orderId = event.data["orderId"] as? String ?: return
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P1,
            channels = setOf(Channel.PUSH, Channel.IN_APP),
            templateKey = "bid.placed",
            templateData = mapOf(
                "bidId" to bidId,
                "orderId" to orderId,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка подтверждения ставки (P1)
     */
    private fun handleBidAwarded(event: Event) {
        val bidId = event.data["bidId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val orderId = event.data["orderId"] as? String ?: return
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P1,
            channels = setOf(Channel.PUSH, Channel.IN_APP),
            templateKey = "bid.awarded",
            templateData = mapOf(
                "bidId" to bidId,
                "orderId" to orderId,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка отклонения ставки (P2)
     */
    private fun handleBidRejected(event: Event) {
        val bidId = event.data["bidId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val orderId = event.data["orderId"] as? String ?: return
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P2,
            channels = setOf(Channel.IN_APP),
            templateKey = "bid.rejected",
            templateData = mapOf(
                "bidId" to bidId,
                "orderId" to orderId,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка снайперской ставки (P0)
     */
    private fun handleSniperBidDetected(event: Event) {
        val bidId = event.data["bidId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val orderId = event.data["orderId"] as? String ?: return
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P0,
            channels = setOf(Channel.PUSH, Channel.SMS),
            templateKey = "bid.sniper.detected",
            templateData = mapOf(
                "bidId" to bidId,
                "orderId" to orderId,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка блокировки средств (P1)
     */
    private fun handleEscrowLocked(event: Event) {
        val contractId = event.data["contractId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val amount = event.data["amount"] as? Double ?: 0.0
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P1,
            channels = setOf(Channel.PUSH, Channel.IN_APP),
            templateKey = "escrow.locked",
            templateData = mapOf(
                "contractId" to contractId,
                "amount" to amount,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка подтверждения финансирования (P1)
     */
    private fun handleEscrowFundingConfirmed(event: Event) {
        val contractId = event.data["contractId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val amount = event.data["amount"] as? Double ?: 0.0
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P1,
            channels = setOf(Channel.PUSH, Channel.IN_APP),
            templateKey = "escrow.funding.confirmed",
            templateData = mapOf(
                "contractId" to contractId,
                "amount" to amount,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка освобождения средств (P2)
     */
    private fun handleEscrowReleased(event: Event) {
        val contractId = event.data["contractId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val amount = event.data["amount"] as? Double ?: 0.0
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P2,
            channels = setOf(Channel.IN_APP),
            templateKey = "escrow.released",
            templateData = mapOf(
                "contractId" to contractId,
                "amount" to amount,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Обработка спора (P0)
     */
    private fun handleEscrowDisputed(event: Event) {
        val contractId = event.data["contractId"] as? String ?: return
        val carrierId = event.data["carrierId"] as? String ?: return
        val reason = event.data["reason"] as? String ?: "Unknown reason"
        
        val notification = Notification(
            userId = UUID.fromString(carrierId),
            priority = Priority.P0,
            channels = setOf(Channel.PUSH, Channel.SMS),
            templateKey = "escrow.disputed",
            templateData = mapOf(
                "contractId" to contractId,
                "reason" to reason,
                "timestamp" to event.timestamp
            ),
            createdAt = LocalDateTime.now()
        )
        
        notificationSender.send(notification)
    }

    /**
     * Парсит JSON-сообщение в Event
     */
    private fun parseEvent(message: String): Event {
        return ObjectMapper().readValue(message, Event::class.java)
    }
}

/**
 * Приоритеты уведомлений
 */
enum class Priority {
    P0, // Critical: Арбитраж, снайперские ставки, отмена заказа
    P1, // Important: Новая ставка, подтверждение фонда
    P2  // Normal: Изменение прогресс-бара, маркетинг
}

/**
 * Каналы доставки уведомлений
 */
enum class Channel {
    PUSH,   // Push-уведомления
    SMS,    // SMS-сообщения (эмуляция)
    IN_APP  // In-app уведомления
}

/**
 * Структура уведомления
 */
data class Notification(
    val userId: UUID,
    val priority: Priority,
    val channels: Set<Channel>,
    val templateKey: String,
    val templateData: Map<String, Any>,
    val createdAt: LocalDateTime
)

/**
 * Структура события
 */
data class Event(
    val type: String,
    val data: Map<String, Any>,
    val timestamp: Long
)