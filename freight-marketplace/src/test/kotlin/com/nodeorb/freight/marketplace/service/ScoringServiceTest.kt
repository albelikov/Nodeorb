package com.nodeorb.freight.marketplace.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nodeorb.freight.marketplace.dto.ScoreBreakdown
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.BidRepository
import com.nodeorb.freight.marketplace.repository.ScmSnapshotRepository
import com.nodeorb.freight.marketplace.repository.UserProfileRepository
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScoringServiceTest {

    private lateinit var scoringService: ScoringService
    private lateinit var bidRepository: BidRepository
    private lateinit var scmSnapshotRepository: ScmSnapshotRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var objectMapper: ObjectMapper

    private val testBidId = UUID.randomUUID()
    private val testCarrierId = UUID.randomUUID()
    private val testOrderId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        bidRepository = mockk(relaxed = true)
        scmSnapshotRepository = mockk(relaxed = true)
        userProfileRepository = mockk(relaxed = true)
        objectMapper = ObjectMapper()

        scoringService = ScoringService(
            bidRepository,
            scmSnapshotRepository,
            userProfileRepository,
            objectMapper
        )
    }

    @Test
    fun `calculateScore should return valid ScoreBreakdown`() {
        // Given
        val bid = createTestBid()
        val scmSnapshot = createTestScmSnapshot()
        val userProfile = createTestUserProfile()

        every { scmSnapshotRepository.findByBidId(testBidId) } returns scmSnapshot
        every { userProfileRepository.findById(testCarrierId) } returns userProfile
        every { bidRepository.findSimilarBids(testOrderId) } returns listOf(
            createTestBid(amount = BigDecimal("1000")),
            createTestBid(amount = BigDecimal("1100")),
            createTestBid(amount = BigDecimal("1200"))
        )

        // When
        val result = scoringService.calculateScore(bid)

        // Then
        assertNotNull(result)
        assertEquals(100.0, result.priceScore)
        assertEquals(80.0, result.trustScore)
        assertEquals(90.0, result.slaScore)
        assertTrue(result.totalScore in 0.0..100.0)
    }

    @Test
    fun `calculateScore should handle missing SCM data`() {
        // Given
        val bid = createTestBid()
        
        every { scmSnapshotRepository.findByBidId(testBidId) } returns null
        every { userProfileRepository.findById(testCarrierId) } returns createTestUserProfile()
        every { bidRepository.findSimilarBids(testOrderId) } returns emptyList()

        // When
        val result = scoringService.calculateScore(bid)

        // Then
        assertNotNull(result)
        assertEquals(0.0, result.trustScore) // Нет данных SCM - минимальный балл
    }

    @Test
    fun `calculateScore should handle missing user profile`() {
        // Given
        val bid = createTestBid()
        val scmSnapshot = createTestScmSnapshot()

        every { scmSnapshotRepository.findByBidId(testBidId) } returns scmSnapshot
        every { userProfileRepository.findById(testCarrierId) } returns null
        every { bidRepository.findSimilarBids(testOrderId) } returns emptyList()

        // When
        val result = scoringService.calculateScore(bid)

        // Then
        assertNotNull(result)
        assertEquals(50.0, result.slaScore) // Нет профиля - средний балл
    }

    @Test
    fun `calculateAndSaveScore should save bid with scoring results`() {
        // Given
        val bid = createTestBid()
        val scmSnapshot = createTestScmSnapshot()
        val userProfile = createTestUserProfile()

        every { bidRepository.findById(testBidId) } returns bid
        every { scmSnapshotRepository.findByBidId(testBidId) } returns scmSnapshot
        every { userProfileRepository.findById(testCarrierId) } returns userProfile
        every { bidRepository.findSimilarBids(testOrderId) } returns listOf(
            createTestBid(amount = BigDecimal("1000")),
            createTestBid(amount = BigDecimal("1100")),
            createTestBid(amount = BigDecimal("1200"))
        )

        // When
        scoringService.calculateAndSaveScore(testBidId)

        // Then
        verify { bidRepository.save(any()) }
        assertNotNull(bid.matchingScore)
        assertTrue(bid.matchingScore!! in 0.0..100.0)
        assertNotNull(bid.scoreBreakdown)
    }

    @Test
    fun `calculateAndSaveScore should throw exception for non-existent bid`() {
        // Given
        every { bidRepository.findById(testBidId) } returns null

        // When & Then
        assertThrows<IllegalArgumentException> {
            scoringService.calculateAndSaveScore(testBidId)
        }
    }

    private fun createTestBid(
        amount: BigDecimal = BigDecimal("1100"),
        carrierId: UUID = testCarrierId,
        orderId: UUID = testOrderId
    ): BidEntity {
        return BidEntity(
            id = testBidId,
            carrierId = carrierId,
            freightOrder = FreightOrderEntity(
                id = orderId,
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
            ),
            masterOrder = null,
            partialOrder = null,
            amount = amount,
            proposedDeliveryDate = LocalDateTime.now().plusDays(5),
            notes = "Test bid",
            status = BidStatus.PENDING,
            matchingScore = null,
            scoreBreakdown = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    private fun createTestScmSnapshot(): ScmSnapshotEntity {
        return ScmSnapshotEntity(
            id = UUID.randomUUID(),
            bidId = testBidId,
            carrierId = testCarrierId,
            masterOrderId = testOrderId,
            snapshotDate = LocalDateTime.now(),
            complianceStatus = ComplianceStatus.COMPLIANT,
            complianceDetails = "All checks passed",
            securityClearance = SecurityLevel.NONE,
            securityDetails = "No restrictions",
            riskScore = 80.0,
            riskFactors = "Low risk",
            auditTrail = "Compliance check completed",
            createdAt = LocalDateTime.now()
        )
    }

    private fun createTestUserProfile(): UserProfileEntity {
        return UserProfileEntity(
            userId = testCarrierId,
            companyName = "Test Carrier",
            rating = 4.5,
            totalOrders = 100,
            completedOrders = 95,
            joinedAt = LocalDateTime.now().minusMonths(6),
            updatedAt = LocalDateTime.now()
        )
    }
}