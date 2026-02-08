package com.nodeorb.freight.marketplace.controller

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketarketplace.service.OrderService
import com.nodeorb.freight.marketplace.service.OrderExecutionService
import com.nodeorb.freight.marketplace.gateway.MarketplaceGateway
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@ExtendWith(MockKExtension::class)
class OrderProgressControllerTest {

    @MockK
    private lateinit var orderService: OrderService

    @MockK
    private lateinit var orderExecutionService: OrderExecutionService

    @MockK
    private lateinit var marketplaceGateway: MarketplaceGateway

    @InjectMockKs
    private lateinit var orderProgressController: OrderProgressController

    private lateinit var orderId: UUID
    private lateinit var userId: UUID

    @BeforeEach
    fun setUp() {
        orderId = UUID.randomUUID()
        userId = UUID.randomUUID()
    }

    @Test
    fun `should get order progress successfully`() {
        // Arrange
        val progressDto = OrderProgressDto(
            orderId = orderId,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            committedWeight = BigDecimal("400.00"),
            committedVolume = BigDecimal("20.00"),
            pendingWeight = BigDecimal("300.00"),
            pendingVolume = BigDecimal("15.00"),
            openWeight = BigDecimal("300.00"),
            openVolume = BigDecimal("15.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            timestamp = LocalDateTime.now()
        )

        every { orderService.getOrderProgress(orderId) } returns progressDto

        // Act
        val response = orderProgressController.getOrderProgress(orderId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(progressDto, response.body)
    }

    @Test
    fun `should return not found when order progress not found`() {
        // Arrange
        every { orderService.getOrderProgress(orderId) } throws RuntimeException("Order not found")

        // Act
        val response = orderProgressController.getOrderProgress(orderId)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should get order progress update successfully`() {
        // Arrange
        val progressDto = OrderProgressDto(
            orderId = orderId,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            committedWeight = BigDecimal("400.00"),
            committedVolume = BigDecimal("20.00"),
            pendingWeight = BigDecimal("300.00"),
            pendingVolume = BigDecimal("15.00"),
            openWeight = BigDecimal("300.00"),
            openVolume = BigDecimal("15.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            timestamp = LocalDateTime.now()
        )

        every { orderService.getOrderProgress(orderId) } returns progressDto

        // Act
        val response = orderProgressController.getOrderProgressUpdate(orderId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val progressUpdate = response.body!!
        assertEquals(orderId, progressUpdate.orderId)
        assertEquals(0.4, progressUpdate.committedPercentage)
        assertEquals(0.3, progressUpdate.pendingPercentage)
        assertEquals(0.3, progressUpdate.openPercentage)
        assertEquals("PENDING", progressUpdate.progressStatus)
    }

    @Test
    fun `should get latest progress update from cache`() {
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

        every { marketplaceGateway.getLastProgressUpdate(orderId) } returns progressUpdate

        // Act
        val response = orderProgressController.getLatestProgressUpdate(orderId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(progressUpdate, response.body)
    }

    @Test
    fun `should get latest progress update from service when cache is empty`() {
        // Arrange
        every { marketplaceGateway.getLastProgressUpdate(orderId) } returns null
        
        val progressDto = OrderProgressDto(
            orderId = orderId,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            committedWeight = BigDecimal("400.00"),
            committedVolume = BigDecimal("20.00"),
            pendingWeight = BigDecimal("300.00"),
            pendingVolume = BigDecimal("15.00"),
            openWeight = BigDecimal("300.00"),
            openVolume = BigDecimal("15.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            timestamp = LocalDateTime.now()
        )

        every { orderService.getOrderProgress(orderId) } returns progressDto

        // Act
        val response = orderProgressController.getLatestProgressUpdate(orderId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val progressUpdate = response.body!!
        assertEquals(orderId, progressUpdate.orderId)
        assertEquals(0.4, progressUpdate.committedPercentage)
    }

    @Test
    fun `should get connection state successfully`() {
        // Arrange
        val connectedClients = 3
        val lastUpdate = OrderProgressUpdate(
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

        every { marketplaceGateway.getConnectedClientsCount(orderId) } returns connectedClients
        every { marketplaceGateway.getLastProgressUpdate(orderId) } returns lastUpdate

        // Act
        val response = orderProgressController.getConnectionState(orderId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val connectionState = response.body!!
        assertEquals(orderId, connectionState.orderId)
        assertEquals(connectedClients, connectionState.connectedClients)
        assertEquals(lastUpdate, connectionState.currentProgress)
    }

    @Test
    fun `should broadcast progress update successfully`() {
        // Arrange
        val progressDto = OrderProgressDto(
            orderId = orderId,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            committedWeight = BigDecimal("400.00"),
            committedVolume = BigDecimal("20.00"),
            pendingWeight = BigDecimal("300.00"),
            pendingVolume = BigDecimal("15.00"),
            openWeight = BigDecimal("300.00"),
            openVolume = BigDecimal("15.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            timestamp = LocalDateTime.now()
        )

        every { orderService.getOrderProgress(orderId) } returns progressDto
        every { marketplaceGateway.sendOrderProgressUpdate(any(), any()) } just Runs
        every { marketplaceGateway.getConnectedClientsCount(orderId) } returns 2

        // Act
        val response = orderProgressController.broadcastProgressUpdate(orderId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertTrue(responseBody["success"] as Boolean)
        assertEquals(orderId, responseBody["orderId"])
        assertEquals(2, responseBody["subscribersCount"])
    }

    @Test
    fun `should return error when broadcasting fails`() {
        // Arrange
        every { orderService.getOrderProgress(orderId) } throws RuntimeException("Order not found")

        // Act
        val response = orderProgressController.broadcastProgressUpdate(orderId)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body!!
        assertFalse(responseBody["success"] as Boolean)
        assertEquals("Order not found", responseBody["error"])
    }

    @Test
    fun `should get WebSocket metrics successfully`() {
        // Arrange
        every { marketplaceGateway.sessions.size } returns 5
        every { marketplaceGateway.orderSubscriptions.size } returns 3

        // Act
        val response = orderProgressController.getWebSocketMetrics()

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val metrics = response.body!!
        assertEquals(5, metrics["activeConnections"])
        assertEquals(3, metrics["totalSubscriptions"])
        assertTrue(metrics.containsKey("timestamp"))
    }

    @Test
    fun `should check user subscription correctly`() {
        // Arrange
        val isSubscribed = true
        every { marketplaceGateway.isUserConnectedToOrder(userId, orderId) } returns isSubscribed

        // Act
        val response = orderProgressController.isUserSubscribed(orderId, userId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertEquals(isSubscribed, responseBody["subscribed"])
        assertEquals(orderId, responseBody["orderId"])
        assertEquals(userId, responseBody["userId"])
    }

    @Test
    fun `should test WebSocket connection successfully`() {
        // Arrange
        val testRequest = TestConnectionRequest(
            orderId = orderId,
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            testMessage = "Test message"
        )

        every { marketplaceGateway.sendOrderProgressUpdate(any(), any()) } just Runs
        every { marketplaceGateway.getConnectedClientsCount(orderId) } returns 2

        // Act
        val response = orderProgressController.testWebSocketConnection(testRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertTrue(responseBody["success"] as Boolean)
        assertEquals("Test message", responseBody["testMessage"])
        assertEquals(2, responseBody["subscribersCount"])
    }

    @Test
    fun `should return error when testing WebSocket connection fails`() {
        // Arrange
        val testRequest = TestConnectionRequest(
            orderId = orderId,
            committedPercentage = 0.4,
            pendingPercentage = 0.3,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            testMessage = "Test message"
        )

        every { marketplaceGateway.sendOrderProgressUpdate(any(), any()) } throws RuntimeException("Connection error")

        // Act
        val response = orderProgressController.testWebSocketConnection(testRequest)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val responseBody = response.body!!
        assertFalse(responseBody["success"] as Boolean)
        assertEquals("Connection error", responseBody["error"])
    }

    @Test
    fun `should get progress history successfully`() {
        // Act
        val response = orderProgressController.getProgressHistory(orderId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(emptyList<OrderProgressUpdate>(), response.body)
    }

    @Test
    fun `should compare progress correctly`() {
        // Arrange
        val previousUpdate = OrderProgressUpdate(
            orderId = orderId,
            committedPercentage = 0.3,
            pendingPercentage = 0.4,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            timestamp = LocalDateTime.now().minusMinutes(1),
            eventType = "BidPlaced"
        )

        val currentUpdate = OrderProgressUpdate(
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
            eventType = "BidAwarded"
        )

        every { marketplaceGateway.getLastProgressUpdate(orderId) } returns currentUpdate

        // Act
        val response = orderProgressController.compareProgress(orderId, previousUpdate)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val comparison = response.body!!
        assertTrue(comparison["hasChanges"] as Boolean)
        assertFalse(comparison["colorChanged"] as Boolean)
        assertTrue(comparison["fillLevelChanged"] as Boolean)
        assertEquals(currentUpdate, comparison["currentUpdate"])
        assertEquals(previousUpdate, comparison["previousUpdate"])
    }

    @Test
    fun `should return not found when comparing progress with no current update`() {
        // Arrange
        val previousUpdate = OrderProgressUpdate(
            orderId = orderId,
            committedPercentage = 0.3,
            pendingPercentage = 0.4,
            openPercentage = 0.3,
            progressStatus = "PENDING",
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("300.00"),
            remainingVolume = BigDecimal("15.00"),
            timestamp = LocalDateTime.now().minusMinutes(1),
            eventType = "BidPlaced"
        )

        every { marketplaceGateway.getLastProgressUpdate(orderId) } returns null

        // Act
        val response = orderProgressController.compareProgress(orderId, previousUpdate)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should get progress recommendations successfully`() {
        // Arrange
        val progressDto = OrderProgressDto(
            orderId = orderId,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            committedWeight = BigDecimal("200.00"),
            committedVolume = BigDecimal("10.00"),
            pendingWeight = BigDecimal("300.00"),
            pendingVolume = BigDecimal("15.00"),
            openWeight = BigDecimal("500.00"),
            openVolume = BigDecimal("25.00"),
            remainingWeight = BigDecimal("500.00"),
            remainingVolume = BigDecimal("25.00"),
            committedPercentage = 0.2,
            pendingPercentage = 0.3,
            openPercentage = 0.5,
            progressStatus = "OPEN",
            timestamp = LocalDateTime.now()
        )

        every { orderService.getOrderProgress(orderId) } returns progressDto

        // Act
        val response = orderProgressController.getProgressRecommendations(orderId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertEquals(orderId, responseBody["orderId"])
        assertEquals(progressDto, responseBody["currentProgress"])
        val recommendations = responseBody["recommendations"] as List<String>
        assertTrue(recommendations.contains("Increase marketing efforts to attract more carriers"))
        assertTrue(recommendations.contains("Consider lowering bid requirements to increase participation"))
    }

    @Test
    fun `should return not found when getting recommendations for non-existent order`() {
        // Arrange
        every { orderService.getOrderProgress(orderId) } throws RuntimeException("Order not found")

        // Act
        val response = orderProgressController.getProgressRecommendations(orderId)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}