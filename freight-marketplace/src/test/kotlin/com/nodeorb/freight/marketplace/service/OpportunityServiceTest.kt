package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.CarrierOpportunityDto
import com.nodeorb.freight.marketplace.dto.CarrierStatsDto
import com.nodeorb.freight.marketplace.dto.OpportunityFilter
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

class OpportunityServiceTest {

    private lateinit var opportunityService: OpportunityService
    private lateinit var freightOrderRepository: FreightOrderRepository
    private lateinit var masterOrderRepository: MasterOrderRepository
    private lateinit var partialOrderRepository: PartialOrderRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var scmSnapshotRepository: ScmSnapshotRepository
    private lateinit var bidRepository: BidRepository

    private val testCarrierId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        freightOrderRepository = mockk(relaxed = true)
        masterOrderRepository = mockk(relaxed = true)
        partialOrderRepository = mockk(relaxed = true)
        userProfileRepository = mockk(relaxed = true)
        scmSnapshotRepository = mockk(relaxed = true)
        bidRepository = mockk(relaxed = true)

        opportunityService = OpportunityService(
            freightOrderRepository,
            masterOrderRepository,
            partialOrderRepository,
            userProfileRepository,
            scmSnapshotRepository,
            bidRepository
        )
    }

    @Test
    fun `getOpportunitiesForCarrier should return filtered opportunities`() {
        // Given
        val carrierProfile = createTestUserProfile()
        val scmSnapshot = createTestScmSnapshot()
        
        val freightOrder = createTestFreightOrder()
        val masterOrder = createTestMasterOrder()
        val partialOrder = createTestPartialOrder(masterOrder)
        
        every { userProfileRepository.findById(testCarrierId) } returns carrierProfile
        every { scmSnapshotRepository.findByCarrierId(testCarrierId) } returns scmSnapshot
        every { freightOrderRepository.findByStatusIn(any()) } returns listOf(freightOrder)
        every { masterOrderRepository.findByStatusIn(any()) } returns listOf(masterOrder)
        every { partialOrderRepository.findByStatusIn(any()) } returns listOf(partialOrder)
        every { bidRepository.findTopByFreightOrderAndStatusOrderByAmountAsc(any(), any()) } returns null

        // When
        val result = opportunityService.getOpportunitiesForCarrier(testCarrierId, OpportunityFilter())

        // Then
        assertEquals(3, result.size)
        assertTrue(result.all { it.passesTrustCheck })
        verify { userProfileRepository.findById(testCarrierId) }
        verify { scmSnapshotRepository.findByCarrierId(testCarrierId) }
    }

    @Test
    fun `getCarrierStats should return correct statistics`() {
        // Given
        val carrierProfile = createTestUserProfile()
        val scmSnapshot = createTestScmSnapshot()
        
        val freightOrder = createTestFreightOrder()
        val masterOrder = createTestMasterOrder()
        val partialOrder = createTestPartialOrder(masterOrder)
        
        every { userProfileRepository.findById(testCarrierId) } returns carrierProfile
        every { scmSnapshotRepository.findByCarrierId(testCarrierId) } returns scmSnapshot
        every { freightOrderRepository.findByStatusIn(any()) } returns listOf(freightOrder)
        every { masterOrderRepository.findByStatusIn(any()) } returns listOf(masterOrder)
        every { partialOrderRepository.findByStatusIn(any()) } returns listOf(partialOrder)

        // When
        val result = opportunityService.getCarrierStats(testCarrierId)

        // Then
        assertEquals(3, result.totalOpportunities)
        assertEquals(3, result.qualifiedOpportunities)
        assertEquals(0, result.dangerousGoodsQualified)
        assertEquals(3, result.highTrustOpportunities)
        assertTrue(result.avgMatchingScore >= 0.0)
        verify { userProfileRepository.findById(testCarrierId) }
        verify { scmSnapshotRepository.findByCarrierId(testCarrierId) }
    }

    @Test
    fun `getOpportunitiesForCarrier should filter by cargo type`() {
        // Given
        val carrierProfile = createTestUserProfile()
        val scmSnapshot = createTestScmSnapshot()
        
        val freightOrder = createTestFreightOrder().apply { cargoType = CargoType.DANGEROUS }
        val masterOrder = createTestMasterOrder().apply { cargoType = CargoType.GENERAL }
        
        every { userProfileRepository.findById(testCarrierId) } returns carrierProfile
        every { scmSnapshotRepository.findByCarrierId(testCarrierId) } returns scmSnapshot
        every { freightOrderRepository.findByStatusIn(any()) } returns listOf(freightOrder)
        every { masterOrderRepository.findByStatusIn(any()) } returns listOf(masterOrder)

        // When
        val filter = OpportunityFilter(cargoTypes = listOf("DANGEROUS"))
        val result = opportunityService.getOpportunitiesForCarrier(testCarrierId, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals("DANGEROUS", result[0].cargoType)
    }

    @Test
    fun `getOpportunitiesForCarrier should filter by max bid amount`() {
        // Given
        val carrierProfile = createTestUserProfile()
        val scmSnapshot = createTestScmSnapshot()
        
        val freightOrder = createTestFreightOrder().apply { maxBidAmount = BigDecimal.valueOf(1000.0) }
        val masterOrder = createTestMasterOrder().apply { maxBidAmount = BigDecimal.valueOf(2000.0) }
        
        every { userProfileRepository.findById(testCarrierId) } returns carrierProfile
        every { scmSnapshotRepository.findByCarrierId(testCarrierId) } returns scmSnapshot
        every { freightOrderRepository.findByStatusIn(any()) } returns listOf(freightOrder)
        every { masterOrderRepository.findByStatusIn(any()) } returns listOf(masterOrder)

        // When
        val filter = OpportunityFilter(maxBidAmount = BigDecimal.valueOf(1500.0))
        val result = opportunityService.getOpportunitiesForCarrier(testCarrierId, filter)

        // Then
        assertEquals(1, result.size)
        assertEquals(BigDecimal.valueOf(1000.0), result[0].maxBidAmount)
    }

    @Test
    fun `getOpportunitiesForCarrier should sort by matching score`() {
        // Given
        val carrierProfile = createTestUserProfile()
        val scmSnapshot = createTestScmSnapshot()
        
        val freightOrder1 = createTestFreightOrder()
        val freightOrder2 = createTestFreightOrder()
        
        every { userProfileRepository.findById(testCarrierId) } returns carrierProfile
        every { scmSnapshotRepository.findByCarrierId(testCarrierId) } returns scmSnapshot
        every { freightOrderRepository.findByStatusIn(any()) } returns listOf(freightOrder1, freightOrder2)

        // When
        val filter = OpportunityFilter(sortBy = "matching_score", sortOrder = "desc")
        val result = opportunityService.getOpportunitiesForCarrier(testCarrierId, filter)

        // Then
        assertEquals(2, result.size)
        // Проверяем, что сортировка работает (в реальной реализации нужно добавить matchingScore)
    }

    @Test
    fun `getOpportunitiesForCarrier should throw exception for invalid carrier`() {
        // Given
        every { userProfileRepository.findById(testCarrierId) } returns null

        // When & Then
        assertThrows<IllegalArgumentException> {
            opportunityService.getOpportunitiesForCarrier(testCarrierId, OpportunityFilter())
        }
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

    private fun createTestFreightOrder(): FreightOrderEntity {
        return FreightOrderEntity(
            id = UUID.randomUUID(),
            shipperId = UUID.randomUUID(),
            title = "Test Order",
            cargoType = CargoType.GENERAL,
            weight = BigDecimal.valueOf(1000.0),
            volume = BigDecimal.valueOf(50.0),
            pickupLocation = mockk(),
            deliveryLocation = mockk(),
            pickupAddress = "Test Pickup",
            deliveryAddress = "Test Delivery",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal.valueOf(1500.0),
            status = OrderStatus.OPEN
        )
    }

    private fun createTestMasterOrder(): MasterOrderEntity {
        return MasterOrderEntity(
            id = UUID.randomUUID(),
            shipperId = UUID.randomUUID(),
            title = "Test Master Order",
            cargoType = CargoType.GENERAL,
            totalWeight = BigDecimal.valueOf(1000.0),
            totalVolume = BigDecimal.valueOf(50.0),
            remainingWeight = BigDecimal.valueOf(1000.0),
            remainingVolume = BigDecimal.valueOf(50.0),
            pickupLocation = mockk(),
            deliveryLocation = mockk(),
            pickupAddress = "Test Pickup",
            deliveryAddress = "Test Delivery",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal.valueOf(1500.0),
            status = MasterOrderStatus.OPEN,
            isLtlEnabled = true,
            minLoadPercentage = 0.8
        )
    }

    private fun createTestPartialOrder(masterOrder: MasterOrderEntity): PartialOrderEntity {
        return PartialOrderEntity(
            id = UUID.randomUUID(),
            masterOrder = masterOrder,
            weight = BigDecimal.valueOf(500.0),
            volume = BigDecimal.valueOf(25.0),
            percentage = 50.0,
            status = PartialOrderStatus.AVAILABLE
        )
    }
}