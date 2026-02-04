package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.OrderProgressDto
import com.nodeorb.freight.marketplace.dto.ShipperBidDto
import com.nodeorb.freight.marketplace.dto.ShipperOrderDto
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsServiceTest {

    private lateinit var analyticsService: AnalyticsService
    private lateinit var bidRepository: BidRepository
    private lateinit var freightOrderRepository: FreightOrderRepository
    private lateinit var masterOrderRepository: MasterOrderRepository
    private lateinit var partialOrderRepository: PartialOrderRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var scmSnapshotRepository: ScmSnapshotRepository

    private val testOrderId = UUID.randomUUID()
    private val testCarrierId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        bidRepository = mockk(relaxed = true)
        freightOrderRepository = mockk(relaxed = true)
        masterOrderRepository = mockk(relaxed = true)
        partialOrderRepository = mockk(relaxed = true)
        userProfileRepository = mockk(relaxed = true)
        scmSnapshotRepository = mockk(relaxed = true)

        analyticsService = AnalyticsService(
            bidRepository,
            freightOrderRepository,
            masterOrderRepository,
            partialOrderRepository,
            userProfileRepository,
            scmSnapshotRepository,
            mockk()
        )
    }

    @Test
    fun `getBidsForOrder should return sorted bids by matching score`() {
        // Given
        val bid1 = createTestBid(testCarrierId, 85.0)
        val bid2 = createTestBid(testCarrierId, 92.0)
        val bid3 = createTestBid(testCarrierId, 78.0)
        
        every { bidRepository.findByFreightOrderId(testOrderId) } returns listOf(bid1, bid2, bid3)
        every { userProfileRepository.findById(any()) } returns createTestUserProfile()
        every { scmSnapshotRepository.findByBidId(any()) } returns createTestScmSnapshot()

        // When
        val result = analyticsService.getBidsForOrder(testOrderId, "FREIGHT")

        // Then
        assertEquals(3, result.size)
        assertEquals(92.0, result[0].matchingScore)
        assertEquals(85.0, result[1].matchingScore)
        assertEquals(78.0, result[2].matchingScore)
    }

    @Test
    fun `getOrderWithBids should return order with bids`() {
        // Given
        val freightOrder = createTestFreightOrder()
        val bid = createTestBid(testCarrierId, 85.0)
        
        every { freightOrderRepository.findById(testOrderId) } returns freightOrder
        every { bidRepository.findByFreightOrderId(testOrderId) } returns listOf(bid)
        every { userProfileRepository.findById(any()) } returns createTestUserProfile()
        every { scmSnapshotRepository.findByBidId(any()) } returns createTestScmSnapshot()

        // When
        val result = analyticsService.getOrderWithBids(testOrderId, "FREIGHT")

        // Then
        assertEquals(testOrderId, result.orderId)
        assertEquals("Test Order", result.title)
        assertEquals("GENERAL", result.cargoType)
        assertEquals(1, result.bids.size)
        assertEquals(85.0, result.bids[0].matchingScore)
    }

    @Test
    fun `getOrderProgress should return correct progress for master order`() {
        // Given
        val masterOrder = createTestMasterOrder()
        val partialOrder1 = createTestPartialOrder(masterOrder, PartialOrderStatus.COMPLETED)
        val partialOrder2 = createTestPartialOrder(masterOrder, PartialOrderStatus.IN_PROGRESS)
        val partialOrder3 = createTestPartialOrder(masterOrder, PartialOrderStatus.AVAILABLE)
        
        every { masterOrderRepository.findById(testOrderId) } returns masterOrder
        every { partialOrderRepository.findByMasterOrder(masterOrder) } returns listOf(partialOrder1, partialOrder2, partialOrder3)

        // When
        val result = analyticsService.getOrderProgress(testOrderId, "MASTER")

        // Then
        assertEquals(testOrderId, result.orderId)
        assertEquals("MASTER", result.orderType)
        assertEquals(100.0, result.blueProgress) // Completed
        assertEquals(0.0, result.yellowProgress) // In Progress
        assertEquals(0.0, result.greyProgress) // Pending
        assertEquals(100.0, result.progressPercentage)
    }

    @Test
    fun `getOrderProgress should return correct progress for freight order`() {
        // Given
        val freightOrder = createTestFreightOrder()
        freightOrder.status = OrderStatus.COMPLETED
        
        every { freightOrderRepository.findById(testOrderId) } returns freightOrder

        // When
        val result = analyticsService.getOrderProgress(testOrderId, "FREIGHT")

        // Then
        assertEquals(testOrderId, result.orderId)
        assertEquals("FREIGHT", result.orderType)
        assertEquals(100.0, result.blueProgress) // Completed
        assertEquals(0.0, result.yellowProgress) // In Progress
        assertEquals(0.0, result.greyProgress) // Pending
        assertEquals(100.0, result.progressPercentage)
    }

    @Test
    fun `getOrderProgress should throw exception for unknown order type`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            analyticsService.getOrderProgress(testOrderId, "UNKNOWN")
        }
    }

    @Test
    fun `checkDangerousGoodsRules should return false for insufficient security clearance`() {
        // Given
        val bid = createTestBid(testCarrierId, 85.0)
        bid.freightOrder = createTestFreightOrder().apply { cargoType = CargoType.DANGEROUS }
        val scmSnapshot = createTestScmSnapshot().apply { securityClearance = SecurityLevel.NONE }

        // When
        val (passes, violations) = (analyticsService as AnalyticsService).checkDangerousGoodsRules(bid, scmSnapshot)

        // Then
        assertTrue(!passes)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.contains("security clearance") })
    }

    private fun createTestBid(carrierId: UUID, matchingScore: Double): BidEntity {
        return BidEntity(
            carrierId = carrierId,
            freightOrder = createTestFreightOrder(),
            masterOrder = null,
            partialOrder = null,
            amount = java.math.BigDecimal.valueOf(1000.0),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5),
            notes = "Test bid",
            status = com.nodeorb.freight.marketplace.entity.BidStatus.PENDING,
            matchingScore = matchingScore,
            scoreBreakdown = "Test breakdown",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createTestFreightOrder(): FreightOrderEntity {
        return FreightOrderEntity(
            id = testOrderId,
            shipperId = UUID.randomUUID(),
            title = "Test Order",
            cargoType = CargoType.GENERAL,
            weight = java.math.BigDecimal.valueOf(1000.0),
            volume = java.math.BigDecimal.valueOf(50.0),
            pickupLocation = mockk(),
            deliveryLocation = mockk(),
            pickupAddress = "Test Pickup",
            deliveryAddress = "Test Delivery",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = java.math.BigDecimal.valueOf(1500.0)
        )
    }

    private fun createTestMasterOrder(): MasterOrderEntity {
        return MasterOrderEntity(
            id = testOrderId,
            shipperId = UUID.randomUUID(),
            title = "Test Master Order",
            cargoType = CargoType.GENERAL,
            totalWeight = java.math.BigDecimal.valueOf(1000.0),
            totalVolume = java.math.BigDecimal.valueOf(50.0),
            remainingWeight = java.math.BigDecimal.valueOf(1000.0),
            remainingVolume = java.math.BigDecimal.valueOf(50.0),
            pickupLocation = mockk(),
            deliveryLocation = mockk(),
            pickupAddress = "Test Pickup",
            deliveryAddress = "Test Delivery",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = java.math.BigDecimal.valueOf(1500.0),
            status = MasterOrderStatus.OPEN,
            isLtlEnabled = true,
            minLoadPercentage = 0.8
        )
    }

    private fun createTestPartialOrder(masterOrder: MasterOrderEntity, status: PartialOrderStatus): PartialOrderEntity {
        return PartialOrderEntity(
            id = UUID.randomUUID(),
            masterOrder = masterOrder,
            weight = java.math.BigDecimal.valueOf(500.0),
            volume = java.math.BigDecimal.valueOf(25.0),
            percentage = 50.0,
            status = status
        )
    }

    private fun createTestUserProfile(): UserProfileEntity {
        return UserProfileEntity(
            userId = testCarrierId,
            companyName = "Test Carrier",
            rating = 4.5,
            totalOrders = 100,
            completedOrders = 95,
            joinedAt = LocalDateTime.now().minusMonths(6)
        )
    }

    private fun createTestScmSnapshot(): ScmSnapshotEntity {
        return ScmSnapshotEntity(
            id = UUID.randomUUID(),
            carrierId = testCarrierId,
            trustLevel = TrustLevel.TRUSTED,
            securityClearance = SecurityLevel.CONFIDENTIAL,
            riskScore = 85.0,
            complianceStatus = ComplianceStatus.COMPLIANT,
            snapshotDate = LocalDateTime.now()
        )
    }
}