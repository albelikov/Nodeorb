package com.nodeorb.freight.marketplace.gateway

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.service.OrderService
import com.nodeorb.freight.marketplace.service.OrderExecutionService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.server.HandshakeFailureException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@ExtendWith(MockKExtension::class)
class MarketplaceGatewayTest {

    @MockK
    private lateinit var orderService: OrderService

    @MockK
    private lateinit var orderExecutionService: OrderExecutionService

    @InjectMockKs
    private lateinit var marketplaceGateway: MarketplaceGateway

    private lateinit var mockSession: WebSocketSession
    private lateinit var mockSession2: WebSocketSession
    private lateinit var orderId: UUID
    private lateinit var userId: UUID

    @BeforeEach
    fun setUp() {
        mockSession = mockk<WebSocketSession>()
        mockSession2 = mockk<WebSocketSession>()
        orderId = UUID.randomUUID()
        userId = UUID.randomUUID()

        every { mockSession.id } returns "session-1"
        every { mockSession.isOpen } returns true
        every { mockSession.handshakeAttributes } returns mutableMapOf(
            "userId" to userId.toString(),
            "userRole" to "CARRIER",
            "clientType" to "web"
        )
        every { mockSession.sendMessage(any()) } just Runs

        every { mockSession2.id } returns "session-2"
        every { mockSession2.isOpen } returns true
        every { mockSession2.handshakeAttributes } returns mutableMapOf(
            "userId" to UUID.randomUUID().toString(),
            "userRole" to "SHIPPER",
            "clientType" to "mobile"
        )
        every { mockSession2.sendMessage(any()) } just Runs

        every { mockSession.close() } just Runs
        every { mockSession2.close() } just Runs
    }

    @Test
    fun `should handle client connection correctly`() {
        // Act
        marketplaceGateway.afterConnectionEstablished(mockSession)

        // Assert
        assertTrue(marketplaceGateway.sessions.containsKey("session-1"))
        assertTrue(marketplaceGateway.userConnections.containsKey("session-1"))
        
        val userConnection = marketplaceGateway.userConnections["session-1"]
        assertEquals(userId, userConnection?.userId)
        assertEquals("CARRIER", userConnection?.userRole)
        assertEquals("web", userConnection?.clientType)
    }

    @Test
    fun `should handle order subscription correctly`() {
        // Arrange
        marketplaceGateway.afterConnectionEstablished(mockSession)
        
        val subscriptionRequest = OrderSubscriptionRequest(
            orderId = orderId,
            userId = userId,
            userRole = "CARRIER"
        )
        
        val event = MarketplaceGateway.WebSocketEvent(
            type = "order.subscribe",
            data = mapOf(
                "orderId" to orderId.toString(),
                "userId" to userId.toString(),
                "userRole" to "CARRIER"
            )
        )

        // Act
        marketplaceGateway.handleTextMessage(mockSession, TextMessage(objectMapper.writeValueAsString(event)))

        // Assert
        assertTrue(marketplaceGateway.orderSubscriptions.containsKey(orderId))
        assertTrue(marketplaceGateway.orderSubscriptions[orderId]!!.contains("session-1"))
    }

    @Test
    fun `should handle order unsubscription correctly`() {
        // Arrange
        marketplaceGateway.afterConnectionEstablished(mockSession)
        
        // Сначала подписываемся
        marketplaceGateway.orderSubscriptions.computeIfAbsent(orderId) { mutableSetOf() }.add("session-1")
        
        val unsubscriptionRequest = OrderUnsubscriptionRequest(
            orderId = orderId,
            userId = userId
        )
        
        val event = MarketplaceGateway.WebSocketEvent(
            type = "order.unsubscribe",
            data = mapOf(
                "orderId" to orderId.toString(),
                "userId" to userId.toString()
            )
        )

        // Act
        marketplaceGateway.handleTextMessage(mockSession, TextMessage(objectMapper.writeValueAsString(event)))

        // Assert
        assertFalse(marketplaceGateway.orderSubscriptions[orderId]!!.contains("session-1"))
    }

    @Test
    fun `should send order progress update to subscribed clients`() {
        // Arrange
        marketplaceGateway.afterConnectionEstablished(mockSession)
        marketplaceGateway.afterConnectionEstablished(mockSession2)
        
        marketplaceGateway.orderSubscriptions.computeIfAbsent(orderId) { mutableSetOf() }.addAll(listOf("session-1", "session-2"))
        
        val progressUpdate = OrderProgressUpdate(
            orderId = orderId,
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            timestamp = LocalDateTime.now(),
            eventType = "BidPlaced"
        )

        // Act
        marketplaceGateway.sendOrderProgressUpdate(orderId, progressUpdate)

        // Assert
        verify { mockSession.sendMessage(any()) }
        verify { mockSession2.sendMessage(any()) }
        
        assertEquals(progressUpdate, marketplaceGateway.lastProgressUpdates[orderId])
    }

    @Test
    fun `should not send update to unsubscribed clients`() {
        // Arrange
        marketplaceGateway.afterConnectionEstablished(mockSession)
        marketplaceGateway.afterConnectionEstablished(mockSession2)
        
        marketplaceGateway.orderSubscriptions.computeIfAbsent(orderId) { mutableSetOf() }.add("session-1") // Только session-1 подписан
        
        val progressUpdate = OrderProgressUpdate(
            orderId = orderId,
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            timestamp = LocalDateTime.now(),
            eventType = "BidPlaced"
        )

        // Act
        marketplaceGateway.sendOrderProgressUpdate(orderId, progressUpdate)

        // Assert
        verify { mockSession.sendMessage(any()) }
        verify(exactly = 0) { mockSession2.sendMessage(any()) }
    }

    @Test
    fun `should handle client disconnection correctly`() {
        // Arrange
        marketplaceGateway.afterConnectionEstablished(mockSession)
        marketplaceGateway.orderSubscriptions.computeIfAbsent(orderId) { mutableSetOf() }.add("session-1")
        
        val closeStatus = CloseStatus.NORMAL

        // Act
        marketplaceGateway.afterConnectionClosed(mockSession, closeStatus)

        // Assert
        assertFalse(marketplaceGateway.sessions.containsKey("session-1"))
        assertFalse(marketplaceGateway.userConnections.containsKey("session-1"))
        assertFalse(marketplaceGateway.orderSubscriptions[orderId]!!.contains("session-1"))
    }

    @Test
    fun `should validate progress update data correctly`() {
        // Arrange
        val validUpdate = OrderProgressUpdate(
            orderId = orderId,
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            timestamp = LocalDateTime.now(),
            eventType = "BidPlaced"
        )

        // Act & Assert
        // Конструктор должен пройти валидацию
        assertEquals(0.4, validUpdate.committedPercentage)
        assertEquals(0.3, validUpdate.pendingPercentage)
        assertEquals(0.3, validUpdate.openPercentage)
        assertEquals(1.0, validUpdate.committedPercentage + validUpdate.pendingPercentage + validUpdate.openPercentage, 0.001)
    }

    @Test
    fun `should get correct connected clients count`() {
        // Arrange
        marketplaceGateway.afterConnectionEstablished(mockSession)
        marketplaceGateway.afterConnectionEstablished(mockSession2)
        
        marketplaceGateway.orderSubscriptions.computeIfAbsent(orderId) { mutableSetOf() }.addAll(listOf("session-1", "session-2"))

        // Act
        val count = marketplaceGateway.getConnectedClientsCount(orderId)

        // Assert
        assertEquals(2, count)
    }

    @Test
    fun `should check user subscription correctly`() {
        // Arrange
        val userId2 = UUID.randomUUID()
        marketplaceGateway.afterConnectionEstablished(mockSession)
        marketplaceGateway.afterConnectionEstablished(mockSession2)
        
        marketplaceGateway.orderSubscriptions.computeIfAbsent(orderId) { mutableSetOf() }.add("session-1")
        
        marketplaceGateway.userConnections["session-1"] = UserConnectionInfo(
            userId = userId,
            userRole = "CARRIER",
            connectedAt = LocalDateTime.now(),
            clientType = "web"
        )
        
        marketplaceGateway.userConnections["session-2"] = UserConnectionInfo(
            userId = userId2,
            userRole = "SHIPPER",
            connectedAt = LocalDateTime.now(),
            clientType = "mobile"
        )

        // Act
        val isUser1Subscribed = marketplaceGateway.isUserConnectedToOrder(userId, orderId)
        val isUser2Subscribed = marketplaceGateway.isUserConnectedToOrder(userId2, orderId)

        // Assert
        assertTrue(isUser1Subscribed)
        assertFalse(isUser2Subscribed)
    }

    @Test
    fun `should get last progress update correctly`() {
        // Arrange
        val progressUpdate = OrderProgressUpdate(
            orderId = orderId,
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            timestamp = LocalDateTime.now(),
            eventType = "BidPlaced"
        )

        marketplaceGateway.lastProgressUpdates[orderId] = progressUpdate

        // Act
        val lastUpdate = marketplaceGateway.getLastProgressUpdate(orderId)

        // Assert
        assertEquals(progressUpdate, lastUpdate)
    }

    @Test
    fun `should send bulk order progress updates correctly`() {
        // Arrange
        marketplaceGateway.afterConnectionEstablished(mockSession)
        marketplaceGateway.afterConnectionEstablished(mockSession2)
        
        val orderId2 = UUID.randomUUID()
        marketplaceGateway.orderSubscriptions.computeIfAbsent(orderId) { mutableSetOf() }.add("session-1")
        marketplaceGateway.orderSubscriptions.computeIfAbsent(orderId2) { mutableSetOf() }.add("session-2")
        
        val bulkUpdate = BulkOrderProgressUpdate(
            updates = listOf(
                OrderProgressUpdate(
                    orderId = orderId,
                    committedPercentage = 0.4,
                    pendingPercentage = 0.3,
                    openPercentage = 0.3,
                    progressStatus = "PENDING",
                    totalWeight = BigDecimal("1000.00"),
                    totalVolume = BigDecimal("50.00"),
                    remainingWeight = BigDecimal("300.00"),
                    remainingVolume = BigDecimal("15.00"),
                    timestamp = LocalDateTime.now(),
                    eventType = "BidPlaced"
                ),
                OrderProgressUpdate(
                    orderId = orderId2,
                    committedPercentage = 0.6,
                    pendingPercentage = 0.2,
                    openPercentage = 0.2,
                    progressStatus = "COMMITTED",
                    totalWeight = BigDecimal("2000.00"),
                    totalVolume = BigDecimal("100.00"),
                    remainingWeight = BigDecimal("400.00"),
                    remainingVolume = BigDecimal("20.00"),
                    timestamp = LocalDateTime.now(),
                    eventType = "BidAwarded"
                )
            )
        )

        // Act
        marketplaceGateway.sendBulkOrderProgressUpdate(bulkUpdate)

        // Assert
        verify { mockSession.sendMessage(any()) }
        verify { mockSession2.sendMessage(any()) }
        
        assertEquals(2, marketplaceGateway.lastProgressUpdates.size)
        assertTrue(marketplaceGateway.lastProgressUpdates.containsKey(orderId))
        assertTrue(marketplaceGateway.lastProgressUpdates.containsKey(orderId2))
    }

    @Test
    fun `should close all sessions correctly`() {
        // Arrange
        marketplaceGateway.afterConnectionEstablished(mockSession)
        marketplaceGateway.afterConnectionEstablished(mockSession2)

        // Act
        marketplaceGateway.closeAllSessions()

        // Assert
        assertTrue(marketplaceGateway.sessions.isEmpty())
        assertTrue(marketplaceGateway.orderSubscriptions.isEmpty())
        assertTrue(marketplaceGateway.userConnections.isEmpty())
        assertTrue(marketplaceGateway.lastProgressUpdates.isEmpty())
    }

    // Вспомогательные методы

    private val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
}

/**
 * Тест для OrderProgressEventListener
 */
@ExtendWith(MockKExtension::class)
class OrderProgressEventListenerTest {

    @MockK
    private lateinit var marketplaceGateway: MarketplaceGateway

    @MockK
    private lateinit var orderService: OrderService

    @InjectMockKs
    private lateinit var orderProgressEventListener: OrderProgressEventListener

    private lateinit var orderId: UUID

    @BeforeEach
    fun setUp() {
        orderId = UUID.randomUUID()
        
        every { marketplaceGateway.sendOrderProgressUpdate(any(), any()) } just Runs
        every { orderService.getOrderProgress(any()) } returns mockk()
    }

    @Test
    fun `should handle BidPlaced event correctly`() {
        // Arrange
        val bidPlacedEvent = BidPlacedEvent(
            orderId = orderId,
            bidId = UUID.randomUUID(),
            carrierId = UUID.randomUUID(),
            amount = BigDecimal("1000.00")
        )

        // Act
        orderProgressEventListener.handleBidPlacedEvent(bidPlacedEvent)

        // Assert
        verify { marketplaceGateway.sendOrderProgressUpdate(orderId, any()) }
    }

    @Test
    fun `should handle BidAwarded event correctly`() {
        // Arrange
        val bidAwardedEvent = BidAwardedEvent(
            orderId = orderId,
            bidId = UUID.randomUUID(),
            carrierId = UUID.randomUUID(),
            amount = BigDecimal("1000.00")
        )

        // Act
        orderProgressEventListener.handleBidAwardedEvent(bidAwardedEvent)

        // Assert
        verify { marketplaceGateway.sendOrderProgressUpdate(orderId, any()) }
    }

    @Test
    fun `should handle OrderUpdated event correctly`() {
        // Arrange
        val orderUpdatedEvent = OrderUpdatedEvent(
            orderId = orderId,
            updateType = "STATUS_CHANGED"
        )

        // Act
        orderProgressEventListener.handleOrderUpdatedEvent(orderUpdatedEvent)

        // Assert
        verify { marketplaceGateway.sendOrderProgressUpdate(orderId, any()) }
    }
}