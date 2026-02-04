package com.nodeorb.freight.marketplace.gateway

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.service.OrderService
import com.nodeorb.freight.marketplace.service.OrderExecutionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Logger

/**
 * WebSocket Gateway для мгновенных обновлений прогресса заказов
 * Реализует Socket.io интеграцию для реального времени
 */
@Component
class MarketplaceGateway(
    @Autowired private val orderService: OrderService,
    @Autowired private val orderExecutionService: OrderExecutionService
) : TextWebSocketHandler() {

    companion object {
        private val logger = Logger.getLogger(MarketplaceGateway::class.java.name)
        private const val ORDER_PROGRESS_UPDATE_EVENT = "order.progress.update"
        private const val ORDER_SUBSCRIPTION_EVENT = "order.subscribe"
        private const val ORDER_UNSUBSCRIPTION_EVENT = "order.unsubscribe"
        private const val CONNECTION_STATE_EVENT = "connection.state"
    }

    /**
     * Хранилище активных сессий
     */
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    
    /**
     * Хранилище подписок пользователей на заказы
     */
    private val orderSubscriptions = ConcurrentHashMap<UUID, MutableSet<String>>()
    
    /**
     * Хранилище подключенных пользователей
     */
    private val userConnections = ConcurrentHashMap<String, UserConnectionInfo>()
    
    /**
     * Хранилище последних обновлений прогресса
     */
    private val lastProgressUpdates = ConcurrentHashMap<UUID, OrderProgressUpdate>()

    /**
     * Обработчик подключения клиента
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val sessionId = session.id
        val userId = extractUserId(session)
        
        sessions[sessionId] = session
        userConnections[sessionId] = UserConnectionInfo(
            userId = userId,
            userRole = extractUserRole(session),
            connectedAt = LocalDateTime.now(),
            clientType = extractClientType(session)
        )

        logger.info("Client connected: sessionId=$sessionId, userId=$userId")
        
        // Отправляем состояние подключения
        sendConnectionState(session)
    }

    /**
     * Обработчик получения сообщений от клиентов
     */
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val payload = message.payload
            logger.info("Received message from session ${session.id}: $payload")
            
            // Парсим сообщение
            val event = parseWebSocketEvent(payload)
            
            when (event.type) {
                ORDER_SUBSCRIPTION_EVENT -> handleSubscription(session, event)
                ORDER_UNSUBSCRIPTION_EVENT -> handleUnsubscription(session, event)
                else -> logger.warning("Unknown event type: ${event.type}")
            }
        } catch (e: Exception) {
            logger.severe("Error handling WebSocket message: ${e.message}")
            sendError(session, "Invalid message format")
        }
    }

    /**
     * Обработчик отключения клиента
     */
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val sessionId = session.id
        
        // Удаляем подписки пользователя
        userConnections.remove(sessionId)?.let { userConnection ->
            orderSubscriptions.forEach { (orderId, sessionIds) ->
                sessionIds.remove(sessionId)
                if (sessionIds.isEmpty()) {
                    orderSubscriptions.remove(orderId)
                }
            }
            
            logger.info("Client disconnected: sessionId=$sessionId, userId=${userConnection.userId}")
        }
        
        sessions.remove(sessionId)
    }

    /**
     * Обработчик исключений
     */
    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.severe("WebSocket transport error for session ${session.id}: ${exception.message}")
        try {
            session.close()
        } catch (e: Exception) {
            logger.severe("Error closing session: ${e.message}")
        }
    }

    /**
     * Подписка на обновления заказа
     */
    private fun handleSubscription(session: WebSocketSession, event: WebSocketEvent) {
        val subscriptionRequest = parseOrderSubscriptionRequest(event.data)
        val orderId = subscriptionRequest.orderId
        val sessionId = session.id

        // Добавляем подписку
        orderSubscriptions.computeIfAbsent(orderId) { mutableSetOf() }.add(sessionId)
        
        logger.info("User ${subscriptionRequest.userId} subscribed to order $orderId")

        // Отправляем текущий прогресс заказа
        sendCurrentOrderProgress(session, orderId)
    }

    /**
     * Отписка от обновлений заказа
     */
    private fun handleUnsubscription(session: WebSocketSession, event: WebSocketEvent) {
        val unsubscriptionRequest = parseOrderUnsubscriptionRequest(event.data)
        val orderId = unsubscriptionRequest.orderId
        val sessionId = session.id

        // Удаляем подписку
        orderSubscriptions[orderId]?.remove(sessionId)
        
        logger.info("User ${unsubscriptionRequest.userId} unsubscribed from order $orderId")
    }

    /**
     * Отправка обновления прогресса заказа
     */
    fun sendOrderProgressUpdate(orderId: UUID, progressUpdate: OrderProgressUpdate) {
        val sessionIds = orderSubscriptions[orderId] ?: return
        
        sessionIds.forEach { sessionId ->
            sessions[sessionId]?.let { session ->
                if (session.isOpen) {
                    try {
                        val message = createOrderProgressMessage(orderId, progressUpdate)
                        session.sendMessage(TextMessage(message))
                        logger.info("Sent progress update for order $orderId to session $sessionId")
                    } catch (e: Exception) {
                        logger.severe("Error sending progress update: ${e.message}")
                    }
                }
            }
        }
        
        // Сохраняем последнее обновление
        lastProgressUpdates[orderId] = progressUpdate
    }

    /**
     * Отправка текущего прогресса заказа
     */
    private fun sendCurrentOrderProgress(session: WebSocketSession, orderId: UUID) {
        try {
            val progress = orderService.getOrderProgress(orderId)
            val progressUpdate = OrderProgressUpdate(
                orderId = orderId,
                committedPercentage = progress.committedWeight.toDouble() / progress.totalWeight.toDouble(),
                pendingPercentage = progress.pendingWeight.toDouble() / progress.totalWeight.toDouble(),
                openPercentage = progress.openWeight.toDouble() / progress.totalWeight.toDouble(),
                progressStatus = progress.progressStatus,
                totalWeight = progress.totalWeight,
                totalVolume = progress.totalVolume,
                remainingWeight = progress.remainingWeight,
                remainingVolume = progress.remainingVolume,
                timestamp = LocalDateTime.now(),
                eventType = "CURRENT_STATE"
            )
            
            val message = createOrderProgressMessage(orderId, progressUpdate)
            session.sendMessage(TextMessage(message))
        } catch (e: Exception) {
            logger.severe("Error sending current progress: ${e.message}")
        }
    }

    /**
     * Отправка состояния подключения
     */
    private fun sendConnectionState(session: WebSocketSession) {
        val connectionState = OrderConnectionState(
            orderId = UUID.randomUUID(), // Для общего состояния
            connectedClients = sessions.size,
            connectedUsers = userConnections.values.toList(),
            lastUpdate = LocalDateTime.now()
        )
        
        val message = createConnectionStateMessage(connectionState)
        session.sendMessage(TextMessage(message))
    }

    /**
     * Отправка ошибки клиенту
     */
    private fun sendError(session: WebSocketSession, errorMessage: String) {
        val errorResponse = mapOf(
            "type" to "error",
            "message" to errorMessage,
            "timestamp" to LocalDateTime.now()
        )
        
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(errorResponse)))
    }

    /**
     * Создание сообщения обновления прогресса
     */
    private fun createOrderProgressMessage(orderId: UUID, progressUpdate: OrderProgressUpdate): String {
        val message = mapOf(
            "type" to ORDER_PROGRESS_UPDATE_EVENT,
            "orderId" to orderId,
            "data" to progressUpdate,
            "timestamp" to LocalDateTime.now()
        )
        
        return objectMapper.writeValueAsString(message)
    }

    /**
     * Создание сообщения состояния подключения
     */
    private fun createConnectionStateMessage(connectionState: OrderConnectionState): String {
        val message = mapOf(
            "type" to CONNECTION_STATE_EVENT,
            "data" to connectionState,
            "timestamp" to LocalDateTime.now()
        )
        
        return objectMapper.writeValueAsString(message)
    }

    /**
     * Извлечение userId из сессии
     */
    private fun extractUserId(session: WebSocketSession): UUID {
        return session.handshakeAttributes["userId"]?.let { UUID.fromString(it.toString()) }
            ?: UUID.randomUUID() // Fallback для тестирования
    }

    /**
     * Извлечение userRole из сессии
     */
    private fun extractUserRole(session: WebSocketSession): String {
        return session.handshakeAttributes["userRole"]?.toString() ?: "UNKNOWN"
    }

    /**
     * Извлечение clientType из сессии
     */
    private fun extractClientType(session: WebSocketSession): String {
        return session.handshakeAttributes["clientType"]?.toString() ?: "web"
    }

    /**
     * Парсинг WebSocket события
     */
    private fun parseWebSocketEvent(payload: String): WebSocketEvent {
        return objectMapper.readValue(payload, WebSocketEvent::class.java)
    }

    /**
     * Парсинг запроса на подписку
     */
    private fun parseOrderSubscriptionRequest(data: Map<String, Any>): OrderSubscriptionRequest {
        return OrderSubscriptionRequest(
            orderId = UUID.fromString(data["orderId"].toString()),
            userId = UUID.fromString(data["userId"].toString()),
            userRole = data["userRole"].toString(),
            connectedAt = LocalDateTime.now()
        )
    }

    /**
     * Парсинг запроса на отписку
     */
    private fun parseOrderUnsubscriptionRequest(data: Map<String, Any>): OrderUnsubscriptionRequest {
        return OrderUnsubscriptionRequest(
            orderId = UUID.fromString(data["orderId"].toString()),
            userId = UUID.fromString(data["userId"].toString()),
            disconnectedAt = LocalDateTime.now()
        )
    }

    /**
     * Получение количества подключенных клиентов для заказа
     */
    fun getConnectedClientsCount(orderId: UUID): Int {
        return orderSubscriptions[orderId]?.size ?: 0
    }

    /**
     * Проверка, подключен ли пользователь к заказу
     */
    fun isUserConnectedToOrder(userId: UUID, orderId: UUID): Boolean {
        return orderSubscriptions[orderId]?.any { sessionId ->
            userConnections[sessionId]?.userId == userId
        } ?: false
    }

    /**
     * Получение последнего обновления прогресса для заказа
     */
    fun getLastProgressUpdate(orderId: UUID): OrderProgressUpdate? {
        return lastProgressUpdates[orderId]
    }

    /**
     * Отправка массового обновления прогресса
     */
    fun sendBulkOrderProgressUpdate(bulkUpdate: BulkOrderProgressUpdate) {
        bulkUpdate.updates.forEach { progressUpdate ->
            sendOrderProgressUpdate(progressUpdate.orderId, progressUpdate)
        }
    }

    /**
     * Закрытие всех сессий (для тестирования и перезагрузки)
     */
    fun closeAllSessions() {
        sessions.values.forEach { session ->
            try {
                session.close()
            } catch (e: Exception) {
                logger.severe("Error closing session: ${e.message}")
            }
        }
        
        sessions.clear()
        orderSubscriptions.clear()
        userConnections.clear()
        lastProgressUpdates.clear()
    }

    // Вспомогательные классы и объекты

    /**
     * DTO для WebSocket события
     */
    data class WebSocketEvent(
        val type: String,
        val data: Map<String, Any>
    )

    /**
     * ObjectMapper для JSON сериализации
     */
    private val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
}

/**
 * Интерсептор для аутентификации WebSocket подключений
 */
class AuthenticationHandshakeInterceptor : HandshakeInterceptor {
    
    override fun beforeHandshake(
        request: org.springframework.http.server.ServerHttpRequest,
        response: org.springframework.http.server.ServerHttpResponse,
        wsHandler: org.springframework.web.socket.WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        // Извлечение аутентификационных данных из headers
        val headers = request.headers
        
        val userId = headers.getFirst("X-User-Id")
        val userRole = headers.getFirst("X-User-Role")
        val clientType = headers.getFirst("X-Client-Type") ?: "web"
        
        if (userId != null && userRole != null) {
            attributes["userId"] = userId
            attributes["userRole"] = userRole
            attributes["clientType"] = clientType
            return true
        }
        
        return false // Отклоняем подключение без аутентификации
    }

    override fun afterHandshake(
        request: org.springframework.http.server.ServerHttpRequest,
        response: org.springframework.http.server.ServerHttpResponse,
        wsHandler: org.springframework.web.socket.WebSocketHandler,
        exception: java.lang.Exception?
    ) {
        // Дополнительная логика после handshake
    }
}

/**
 * Слушатель событий для автоматической отправки обновлений
 */
@Component
class OrderProgressEventListener(
    @Autowired private val marketplaceGateway: MarketplaceGateway,
    @Autowired private val orderService: OrderService
) {

    companion object {
        private val logger = Logger.getLogger(OrderProgressEventListener::class.java.name)
    }

    /**
     * Слушатель события BidPlaced
     */
    @EventListener
    fun handleBidPlacedEvent(event: BidPlacedEvent) {
        try {
            val progressUpdate = createOrderProgressUpdate(event.orderId, "BidPlaced")
            marketplaceGateway.sendOrderProgressUpdate(event.orderId, progressUpdate)
            logger.info("Sent BidPlaced update for order ${event.orderId}")
        } catch (e: Exception) {
            logger.severe("Error handling BidPlaced event: ${e.message}")
        }
    }

    /**
     * Слушатель события BidAwarded
     */
    @EventListener
    fun handleBidAwardedEvent(event: BidAwardedEvent) {
        try {
            val progressUpdate = createOrderProgressUpdate(event.orderId, "BidAwarded")
            marketplaceGateway.sendOrderProgressUpdate(event.orderId, progressUpdate)
            logger.info("Sent BidAwarded update for order ${event.orderId}")
        } catch (e: Exception) {
            logger.severe("Error handling BidAwarded event: ${e.message}")
        }
    }

    /**
     * Слушатель события OrderUpdated
     */
    @EventListener
    fun handleOrderUpdatedEvent(event: OrderUpdatedEvent) {
        try {
            val progressUpdate = createOrderProgressUpdate(event.orderId, "OrderUpdated")
            marketplaceGateway.sendOrderProgressUpdate(event.orderId, progressUpdate)
            logger.info("Sent OrderUpdated update for order ${event.orderId}")
        } catch (e: Exception) {
            logger.severe("Error handling OrderUpdated event: ${e.message}")
        }
    }

    /**
     * Создание обновления прогресса на основе заказа
     */
    private fun createOrderProgressUpdate(orderId: UUID, eventType: String): OrderProgressUpdate {
        val progress = orderService.getOrderProgress(orderId)
        
        return OrderProgressUpdate(
            orderId = orderId,
            committedPercentage = progress.committedWeight.toDouble() / progress.totalWeight.toDouble(),
            pendingPercentage = progress.pendingWeight.toDouble() / progress.totalWeight.toDouble(),
            openPercentage = progress.openWeight.toDouble() / progress.totalWeight.toDouble(),
            progressStatus = progress.progressStatus,
            totalWeight = progress.totalWeight,
            totalVolume = progress.totalVolume,
            remainingWeight = progress.remainingWeight,
            remainingVolume = progress.remainingVolume,
            timestamp = LocalDateTime.now(),
            eventType = eventType
        )
    }
}

/**
 * DTO для событий BidPlaced
 */
data class BidPlacedEvent(
    val orderId: UUID,
    val bidId: UUID,
    val carrierId: UUID,
    val amount: java.math.BigDecimal,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * DTO для событий BidAwarded
 */
data class BidAwardedEvent(
    val orderId: UUID,
    val bidId: UUID,
    val carrierId: UUID,
    val amount: java.math.BigDecimal,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * DTO для событий OrderUpdated
 */
data class OrderUpdatedEvent(
    val orderId: UUID,
    val updateType: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)