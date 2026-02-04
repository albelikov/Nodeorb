package com.nodeorb.freight.marketplace.controller

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.service.OrderService
import com.nodeorb.freight.marketplace.service.OrderExecutionService
import com.nodeorb.freight.marketplace.gateway.MarketplaceGateway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.socket.TextMessage
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.logging.Logger

/**
 * REST Controller для управления прогрессом заказов
 * Интегрируется с WebSocket для мгновенных обновлений
 */
@RestController
@RequestMapping("/api/v1/orders")
class OrderProgressController(
    @Autowired private val orderService: OrderService,
    @Autowired private val orderExecutionService: OrderExecutionService,
    @Autowired private val marketplaceGateway: MarketplaceGateway
) {

    companion object {
        private val logger = Logger.getLogger(OrderProgressController::class.java.name)
    }

    /**
     * Получение текущего прогресса заказа
     */
    @GetMapping("/{orderId}/progress")
    fun getOrderProgress(@PathVariable orderId: UUID): ResponseEntity<OrderProgressDto> {
        try {
            val progress = orderService.getOrderProgress(orderId)
            return ResponseEntity.ok(progress)
        } catch (e: Exception) {
            logger.severe("Error getting order progress for order $orderId: ${e.message}")
            return ResponseEntity.notFound().build()
        }
    }

    /**
     * Получение обновления прогресса в формате для WebSocket
     */
    @GetMapping("/{orderId}/progress/update")
    fun getOrderProgressUpdate(@PathVariable orderId: UUID): ResponseEntity<OrderProgressUpdate> {
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
                eventType = "MANUAL_REQUEST"
            )
            
            return ResponseEntity.ok(progressUpdate)
        } catch (e: Exception) {
            logger.severe("Error getting order progress update for order $orderId: ${e.message}")
            return ResponseEntity.notFound().build()
        }
    }

    /**
     * Получение последнего обновления прогресса из кэша
     */
    @GetMapping("/{orderId}/progress/latest")
    fun getLatestProgressUpdate(@PathVariable orderId: UUID): ResponseEntity<OrderProgressUpdate> {
        val latestUpdate = marketplaceGateway.getLastProgressUpdate(orderId)
        
        return if (latestUpdate != null) {
            ResponseEntity.ok(latestUpdate)
        } else {
            // Если в кэше нет, получаем текущий прогресс
            getOrderProgressUpdate(orderId)
        }
    }

    /**
     * Получение состояния подключения к заказу
     */
    @GetMapping("/{orderId}/connection/state")
    fun getConnectionState(@PathVariable orderId: UUID): ResponseEntity<OrderConnectionState> {
        val connectedClients = marketplaceGateway.getConnectedClientsCount(orderId)
        val lastUpdate = marketplaceGateway.getLastProgressUpdate(orderId)
        
        val connectionState = OrderConnectionState(
            orderId = orderId,
            connectedClients = connectedClients,
            connectedUsers = emptyList(), // Можно расширить для получения реальных пользователей
            lastUpdate = lastUpdate?.timestamp ?: LocalDateTime.now(),
            currentProgress = lastUpdate
        )
        
        return ResponseEntity.ok(connectionState)
    }

    /**
     * Принудительная отправка обновления прогресса всем подписчикам
     */
    @PostMapping("/{orderId}/progress/broadcast")
    fun broadcastProgressUpdate(@PathVariable orderId: UUID): ResponseEntity<Map<String, Any>> {
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
                eventType = "MANUAL_BROADCAST"
            )
            
            marketplaceGateway.sendOrderProgressUpdate(orderId, progressUpdate)
            
            val response = mapOf(
                "success" to true,
                "orderId" to orderId,
                "subscribersCount" to marketplaceGateway.getConnectedClientsCount(orderId),
                "timestamp" to LocalDateTime.now()
            )
            
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.severe("Error broadcasting progress update for order $orderId: ${e.message}")
            val response = mapOf(
                "success" to false,
                "orderId" to orderId,
                "error" to e.message
            )
            return ResponseEntity.badRequest().body(response)
        }
    }

    /**
     * Получение метрик WebSocket соединений
     */
    @GetMapping("/progress/metrics")
    fun getWebSocketMetrics(): ResponseEntity<Map<String, Any>> {
        val metrics = mapOf(
            "activeConnections" to marketplaceGateway.sessions.size,
            "totalSubscriptions" to marketplaceGateway.orderSubscriptions.size,
            "totalOrdersWithSubscriptions" to marketplaceGateway.orderSubscriptions.keys.size,
            "timestamp" to LocalDateTime.now()
        )
        
        return ResponseEntity.ok(metrics)
    }

    /**
     * Проверка подключения пользователя к заказу
     */
    @GetMapping("/{orderId}/user/{userId}/subscribed")
    fun isUserSubscribed(
        @PathVariable orderId: UUID,
        @PathVariable userId: UUID
    ): ResponseEntity<Map<String, Boolean>> {
        val isSubscribed = marketplaceGateway.isUserConnectedToOrder(userId, orderId)
        
        val response = mapOf(
            "subscribed" to isSubscribed,
            "orderId" to orderId,
            "userId" to userId
        )
        
        return ResponseEntity.ok(response)
    }

    /**
     * Тестирование WebSocket соединения
     */
    @PostMapping("/progress/test-connection")
    fun testWebSocketConnection(@RequestBody testRequest: TestConnectionRequest): ResponseEntity<Map<String, Any>> {
        try {
            val progressUpdate = OrderProgressUpdate(
                orderId = testRequest.orderId,
                committedPercentage = testRequest.committedPercentage,
                pendingPercentage = testRequest.pendingPercentage,
                openPercentage = testRequest.openPercentage,
                progressStatus = testRequest.progressStatus,
                totalWeight = testRequest.totalWeight,
                totalVolume = testRequest.totalVolume,
                remainingWeight = testRequest.remainingWeight,
                remainingVolume = testRequest.remainingVolume,
                timestamp = LocalDateTime.now(),
                eventType = "TEST_MESSAGE",
                eventData = mapOf("test" to true, "message" to testRequest.testMessage)
            )
            
            marketplaceGateway.sendOrderProgressUpdate(testRequest.orderId, progressUpdate)
            
            val response = mapOf(
                "success" to true,
                "testMessage" to testRequest.testMessage,
                "subscribersCount" to marketplaceGateway.getConnectedClientsCount(testRequest.orderId),
                "timestamp" to LocalDateTime.now()
            )
            
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.severe("Error testing WebSocket connection: ${e.message}")
            val response = mapOf(
                "success" to false,
                "error" to e.message
            )
            return ResponseEntity.badRequest().body(response)
        }
    }

    /**
     * Получение истории изменений прогресса заказа
     */
    @GetMapping("/{orderId}/progress/history")
    fun getProgressHistory(@PathVariable orderId: UUID): ResponseEntity<List<OrderProgressUpdate>> {
        // Здесь можно реализовать получение истории из базы данных
        // Пока возвращаем пустой список
        return ResponseEntity.ok(emptyList())
    }

    /**
     * Сравнение текущего и предыдущего состояния прогресса
     */
    @PostMapping("/{orderId}/progress/compare")
    fun compareProgress(
        @PathVariable orderId: UUID,
        @RequestBody previousUpdate: OrderProgressUpdate
    ): ResponseEntity<Map<String, Any>> {
        val currentUpdate = marketplaceGateway.getLastProgressUpdate(orderId)
        
        if (currentUpdate == null) {
            return ResponseEntity.notFound().build()
        }
        
        val hasChanges = currentUpdate.hasChanges(previousUpdate)
        val colorChanged = currentUpdate.getProgressBarColor() != previousUpdate.getProgressBarColor()
        val fillLevelChanged = currentUpdate.getFillLevel() != previousUpdate.getFillLevel()
        
        val comparison = mapOf(
            "hasChanges" to hasChanges,
            "colorChanged" to colorChanged,
            "fillLevelChanged" to fillLevelChanged,
            "currentUpdate" to currentUpdate,
            "previousUpdate" to previousUpdate,
            "timestamp" to LocalDateTime.now()
        )
        
        return ResponseEntity.ok(comparison)
    }

    /**
     * Получение рекомендаций по улучшению прогресса
     */
    @GetMapping("/{orderId}/progress/recommendations")
    fun getProgressRecommendations(@PathVariable orderId: UUID): ResponseEntity<Map<String, Any>> {
        try {
            val progress = orderService.getOrderProgress(orderId)
            
            val recommendations = mutableListOf<String>()
            
            // Рекомендации на основе текущего прогресса
            when {
                progress.committedPercentage < 0.3 -> {
                    recommendations.add("Increase marketing efforts to attract more carriers")
                    recommendations.add("Consider lowering bid requirements to increase participation")
                }
                progress.pendingPercentage > 0.5 -> {
                    recommendations.add("Review pending bids and expedite approval process")
                    recommendations.add("Contact carriers with pending bids for faster response")
                }
                progress.openPercentage > 0.7 -> {
                    recommendations.add("Expand search radius for potential carriers")
                    recommendations.add("Consider increasing bid amount to attract more interest")
                }
                else -> {
                    recommendations.add("Current progress is good, continue monitoring")
                }
            }
            
            val response = mapOf(
                "orderId" to orderId,
                "currentProgress" to progress,
                "recommendations" to recommendations,
                "timestamp" to LocalDateTime.now()
            )
            
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.severe("Error getting progress recommendations for order $orderId: ${e.message}")
            return ResponseEntity.notFound().build()
        }
    }
}

/**
 * DTO для тестирования WebSocket соединения
 */
data class TestConnectionRequest(
    val orderId: UUID,
    val committedPercentage: Double,
    val pendingPercentage: Double,
    val openPercentage: Double,
    val progressStatus: String,
    val totalWeight: BigDecimal,
    val totalVolume: BigDecimal,
    val remainingWeight: BigDecimal,
    val remainingVolume: BigDecimal,
    val testMessage: String = "Test message from API"
)