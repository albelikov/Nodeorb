package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.QuotaCheckResult
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.BidRepository
import com.nodeorb.freight.marketplace.repository.MasterOrderRepository
import com.nodeorb.freight.marketplace.repository.PartialOrderRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuotaServiceTest {

    private lateinit var quotaService: QuotaService
    private lateinit var bidRepository: BidRepository
    private lateinit var masterOrderRepository: MasterOrderRepository
    private lateinit var partialOrderRepository: PartialOrderRepository

    private val testCarrierId = UUID.randomUUID()
    private val testOrderId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        bidRepository = mockk(relaxed = true)
        masterOrderRepository = mockk(relaxed = true)
        partialOrderRepository = mockk(relaxed = true)

        quotaService = QuotaService(
            bidRepository,
            masterOrderRepository,
            partialOrderRepository
        )
    }

    @Test
    fun `checkCarrierQuota should return valid result`() {
        // Given
        val loadWeight = BigDecimal("50")
        
        every { bidRepository.findByCarrierIdAndStatusIn(any(), any()) } returns emptyList()
        every { bidRepository.findByCarrierIdAndStatus(any(), any()) } returns emptyList()

        // When
        val result = quotaService.checkCarrierQuota(testCarrierId, loadWeight)

        // Then
        assertTrue(result.isWithinQuota)
        assertEquals(BigDecimal.valueOf(200.0), result.quotaLimit)
        assertEquals(BigDecimal.ZERO, result.currentLoad)
        assertEquals(BigDecimal.valueOf(150.0), result.availableCapacity)
    }

    @Test
    fun `checkCarrierQuota should fail when exceeding quota`() {
        // Given
        val loadWeight = BigDecimal("250") // Больше лимита
        
        every { bidRepository.findByCarrierIdAndStatusIn(any(), any()) } returns emptyList()
        every { bidRepository.findByCarrierIdAndStatus(any(), any()) } returns emptyList()

        // When
        val result = quotaService.checkCarrierQuota(testCarrierId, loadWeight)

        // Then
        assertFalse(result.isWithinQuota)
        assertTrue(result.violations.isNotEmpty())
    }

    @Test
    fun `validateBidQuota should handle freight order`() {
        // Given
        val freightOrder = createTestFreightOrder()
        val masterOrder = createTestMasterOrder()
        
        every { bidRepository.findFreightOrderById(testOrderId) } returns freightOrder
        every { masterOrderRepository.findById(any()) } returns masterOrder
        every { bidRepository.findByCarrierIdAndStatusIn(any(), any()) } returns emptyList()
        every { bidRepository.findByCarrierIdAndStatus(any(), any()) } returns emptyList()

        // When
        val result = quotaService.validateBidQuota(testCarrierId, testOrderId, null, null)

        // Then
        assertTrue(result.isWithinQuota)
        verify { bidRepository.findFreightOrderById(testOrderId) }
    }

    @Test
    fun `validateBidQuota should handle master order`() {
        // Given
        val masterOrder = createTestMasterOrder()
        
        every { masterOrderRepository.findById(testOrderId) } returns masterOrder
        every { bidRepository.findByCarrierIdAndStatusIn(any(), any()) } returns emptyList()
        every { bidRepository.findByCarrierIdAndStatus(any(), any()) } returns emptyList()

        // When
        val result = quotaService.validateBidQuota(testCarrierId, null, testOrderId, null)

        // Then
        assertTrue(result.isWithinQuota)
        verify { masterOrderRepository.findById(testOrderId) }
    }

    @Test
    fun `validateBidQuota should handle partial order`() {
        // Given
        val partialOrder = createTestPartialOrder()
        val masterOrder = createTestMasterOrder()
        
        every { partialOrderRepository.findById(testOrderId) } returns partialOrder
        every { bidRepository.findByCarrierIdAndStatusIn(any(), any()) } returns emptyList()
        every { bidRepository.findByCarrierIdAndStatus(any(), any()) } returns emptyList()

        // When
        val result = quotaService.validateBidQuota(testCarrierId, null, null, testOrderId)

        // Then
        assertTrue(result.isWithinQuota)
        verify { partialOrderRepository.findById(testOrderId) }
    }

    @Test
    fun `validateBidQuota should throw exception for invalid order ID`() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            quotaService.validateBidQuota(testCarrierId, null, null, null)
        }
    }

    private fun createTestFreightOrder(): FreightOrderEntity {
        return FreightOrderEntity(
            id = testOrderId,
            shipperId = UUID.randomUUID(),
            title = "Test Order",
            cargoType = CargoType.GENERAL,
            weight = BigDecimal("1000"),
            volume = BigDecimal("50"),
            pickupLocation = mockk(),
            deliveryLocation = mockk(),
            pickupAddress = "Test Pickup",
            deliveryAddress = "Test Delivery",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("1500")
        )
    }

    private fun createTestMasterOrder(): MasterOrderEntity {
        return MasterOrderEntity(
            id = testOrderId,
            shipperId = UUID.randomUUID(),
            title = "Test Master Order",
            cargoType = CargoType.GENERAL,
            totalWeight = BigDecimal("1000"),
            totalVolume = BigDecimal("50"),
            remainingWeight = BigDecimal("1000"),
            remainingVolume = BigDecimal("50"),
            pickupLocation = mockk(),
            deliveryLocation = mockk(),
            pickupAddress = "Test Pickup",
            deliveryAddress = "Test Delivery",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("1500"),
            status = MasterOrderStatus.OPEN,
            isLtlEnabled = true,
            minLoadPercentage = 0.8
        )
    }

    private fun createTestPartialOrder(): PartialOrderEntity {
        return PartialOrderEntity(
            id = testOrderId,
            masterOrder = createTestMasterOrder(),
            weight = BigDecimal("500"),
            volume = BigDecimal("25"),
            percentage = 50.0,
            status = PartialOrderStatus.AVAILABLE
        )
    }
}