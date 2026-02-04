package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.exception.ComplianceViolationException
import com.nodeorb.freight.marketplace.repository.MasterOrderRepository
import com.nodeorb.freight.marketplace.repository.PartialOrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertTrue

class AuctionGuardTest {

    private lateinit var auctionGuard: AuctionGuard
    private lateinit var masterOrderRepository: MasterOrderRepository
    private lateinit var partialOrderRepository: PartialOrderRepository

    private val testOrderId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        masterOrderRepository = mockk(relaxed = true)
        partialOrderRepository = mockk(relaxed = true)

        auctionGuard = AuctionGuard(
            masterOrderRepository,
            partialOrderRepository
        )
    }

    @Test
    fun `validateBidPlacement should pass for valid bid`() {
        // Given
        val bid = createTestBid()
        
        every { masterOrderRepository.findById(any()) } returns createTestMasterOrder()
        every { partialOrderRepository.findById(any()) } returns createTestPartialOrder()

        // When & Then
        val result = auctionGuard.validateBidPlacement(bid)
        assertTrue(result)
    }

    @Test
    fun `validateBidPlacement should throw exception for LTL atomicity violation`() {
        // Given
        val bid = createTestBidWithPartialOrder()
        val partialOrder = createTestPartialOrder()
        val masterOrder = createTestMasterOrder()
        
        every { partialOrderRepository.findById(any()) } returns partialOrder
        every { masterOrderRepository.findById(any()) } returns masterOrder

        // When & Then
        assertThrows<ComplianceViolationException> {
            auctionGuard.validateBidPlacement(bid)
        }
    }

    @Test
    fun `validateAuctionStart should pass for valid master order`() {
        // Given
        val masterOrder = createTestMasterOrder()
        
        every { masterOrderRepository.findById(testOrderId) } returns masterOrder

        // When & Then
        val result = auctionGuard.validateAuctionStart(testOrderId, "master")
        assertTrue(result)
        verify { masterOrderRepository.findById(testOrderId) }
    }

    @Test
    fun `validateAuctionStart should pass for valid partial order`() {
        // Given
        val partialOrder = createTestPartialOrder()
        
        every { partialOrderRepository.findById(testOrderId) } returns partialOrder

        // When & Then
        val result = auctionGuard.validateAuctionStart(testOrderId, "partial")
        assertTrue(result)
        verify { partialOrderRepository.findById(testOrderId) }
    }

    @Test
    fun `validateAuctionCompletion should pass for expired auction`() {
        // Given
        val masterOrder = createTestMasterOrder()
        masterOrder.requiredDeliveryDate = LocalDateTime.now().minusMinutes(10) // Аукцион уже завершен
        
        every { masterOrderRepository.findById(testOrderId) } returns masterOrder

        // When & Then
        val result = auctionGuard.validateAuctionCompletion(testOrderId, "master")
        assertTrue(result)
    }

    private fun createTestBid(): BidEntity {
        return BidEntity(
            carrierId = UUID.randomUUID(),
            freightOrder = createTestFreightOrder(),
            masterOrder = null,
            partialOrder = null,
            amount = java.math.BigDecimal.valueOf(1000.0),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5),
            notes = "Test bid",
            status = com.nodeorb.freight.marketplace.entity.BidStatus.PENDING,
            matchingScore = null,
            scoreBreakdown = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createTestBidWithPartialOrder(): BidEntity {
        return BidEntity(
            carrierId = UUID.randomUUID(),
            freightOrder = null,
            masterOrder = null,
            partialOrder = createTestPartialOrder(),
            amount = java.math.BigDecimal.valueOf(1000.0),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5),
            notes = "Test bid",
            status = com.nodeorb.freight.marketplace.entity.BidStatus.PENDING,
            matchingScore = null,
            scoreBreakdown = null,
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

    private fun createTestPartialOrder(): PartialOrderEntity {
        return PartialOrderEntity(
            id = testOrderId,
            masterOrder = createTestMasterOrder(),
            weight = java.math.BigDecimal.valueOf(500.0),
            volume = java.math.BigDecimal.valueOf(25.0),
            percentage = 50.0,
            status = PartialOrderStatus.AVAILABLE
        )
    }
}