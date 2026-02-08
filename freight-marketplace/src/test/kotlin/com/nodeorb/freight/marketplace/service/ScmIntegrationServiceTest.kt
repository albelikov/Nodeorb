package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.core.KafkaTemplate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class ScmIntegrationServiceTest {

    @MockK
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @MockK
    private lateinit var scmSnapshotRepository: ScmSnapshotRepository

    @MockK
    private lateinit var masterOrderRepository: MasterOrderRepository

    @MockK
    private lateinit var bidRepository: BidRepository

    @MockK
    private lateinit var userProfileRepository: UserProfileRepository

    @InjectMockKs
    private lateinit var scmIntegrationService: ScmIntegrationService

    private lateinit var bidEvent: BidPlacementEvent
    private lateinit var masterOrder: MasterOrderEntity
    private lateinit var bid: BidEntity
    private lateinit var userProfile: UserProfileEntity

    @BeforeEach
    fun setUp() {
        // Создаем тестовые данные
        val carrierId = UUID.randomUUID()
        val masterOrderId = UUID.randomUUID()
        val bidId = UUID.randomUUID()

        bidEvent = BidPlacementEvent(
            bidId = bidId,
            carrierId = carrierId,
            masterOrderId = masterOrderId,
            amount = 2500.0,
            proposedDeliveryDate = "2024-01-15T10:00:00",
            notes = "Test bid",
            route = RouteInfo(
                pickupLocation = LocationInfo(50.4501, 30.5234, "UA", "Kyiv", "Kyiv"),
                deliveryLocation = LocationInfo(49.8397, 24.0297, "UA", "Lviv", "Lviv")
            ),
            cargoDetails = CargoDetails(
                cargoType = "GENERAL",
                weight = 500.0,
                volume = 25.0,
                hazardous = false,
                temperatureControlled = false
            )
        )

        masterOrder = MasterOrderEntity(
            shipperId = UUID.randomUUID(),
            title = "Test Order",
            cargoType = CargoType.GENERAL,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("1000.00"),
            remainingVolume = BigDecimal("50.00"),
            pickupLocation = createMockPoint(50.4501, 30.5234),
            deliveryLocation = createMockPoint(49.8397, 24.0297),
            pickupAddress = "Kyiv, Ukraine",
            deliveryAddress = "Lviv, Ukraine",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("5000.00")
        )

        bid = BidEntity(
            carrierId = carrierId,
            amount = BigDecimal("2500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5)
        )

        userProfile = UserProfileEntity(
            userId = carrierId,
            companyName = "Test Carrier",
            rating = 4.5,
            totalOrders = 50,
            completedOrders = 48
        )
    }

    @Test
    fun `should process bid placement event successfully`() {
        // Arrange
        every { masterOrderRepository.findById(bidEvent.masterOrderId) } returns Optional.of(masterOrder)
        every { bidRepository.findById(bidEvent.bidId) } returns Optional.of(bid)
        every { userProfileRepository.findById(bidEvent.carrierId) } returns Optional.of(userProfile)
        
        val savedSnapshot = mockk<ScmSnapshotEntity>(relaxed = true)
        every { scmSnapshotRepository.save(any()) } returns savedSnapshot
        
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        scmIntegrationService.handleBidPlacementEvent(bidEvent)

        // Assert
        verify {
            masterOrderRepository.findById(bidEvent.masterOrderId)
            bidRepository.findById(bidEvent.bidId)
            userProfileRepository.findById(bidEvent.carrierId)
            scmSnapshotRepository.save(any())
            kafkaTemplate.send(any(), any())
        }
    }

    @Test
    fun `should create scm snapshot when processing bid`() {
        // Arrange
        every { masterOrderRepository.findById(bidEvent.masterOrderId) } returns Optional.of(masterOrder)
        every { bidRepository.findById(bidEvent.bidId) } returns Optional.of(bid)
        every { userProfileRepository.findById(bidEvent.carrierId) } returns Optional.of(userProfile)
        
        val capturedSnapshot = slot<ScmSnapshotEntity>()
        every { scmSnapshotRepository.save(capture(capturedSnapshot)) } answers { firstArg() }
        
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        scmIntegrationService.handleBidPlacementEvent(bidEvent)

        // Assert
        val snapshot = capturedSnapshot.captured
        assert(snapshot.bidId == bidEvent.bidId)
        assert(snapshot.carrierId == bidEvent.carrierId)
        assert(snapshot.masterOrderId == bidEvent.masterOrderId)
        assert(snapshot.complianceStatus == ComplianceStatus.PENDING)
        assert(snapshot.securityClearance == SecurityLevel.NONE)
    }

    @Test
    fun `should handle compliance check error`() {
        // Arrange
        every { masterOrderRepository.findById(bidEvent.masterOrderId) } throws RuntimeException("Database error")
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act & Assert
        scmIntegrationService.handleBidPlacementEvent(bidEvent)

        verify {
            kafkaTemplate.send("compliance.errors", any())
        }
    }

    @Test
    fun `should generate trust token for compliant carrier`() {
        // Arrange
        every { masterOrderRepository.findById(bidEvent.masterOrderId) } returns Optional.of(masterOrder)
        every { bidRepository.findById(bidEvent.bidId) } returns Optional.of(bid)
        every { userProfileRepository.findById(bidEvent.carrierId) } returns Optional.of(userProfile)
        
        every { scmSnapshotRepository.save(any()) } returns mockk()
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        scmIntegrationService.handleBidPlacementEvent(bidEvent)

        // Assert
        verify {
            kafkaTemplate.send("trust.tokens", any<TrustTokenInfo>())
        }
    }

    @Test
    fun `should not generate trust token for non-compliant carrier`() {
        // Arrange
        val nonCompliantProfile = userProfile.copy(rating = 2.5, totalOrders = 5)
        
        every { masterOrderRepository.findById(bidEvent.masterOrderId) } returns Optional.of(masterOrder)
        every { bidRepository.findById(bidEvent.bidId) } returns Optional.of(bid)
        every { userProfileRepository.findById(bidEvent.carrierId) } returns Optional.of(nonCompliantProfile)
        
        every { scmSnapshotRepository.save(any()) } returns mockk()
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        scmIntegrationService.handleBidPlacementEvent(bidEvent)

        // Assert
        verify(exactly = 0) {
            kafkaTemplate.send("trust.tokens", any<TrustTokenInfo>())
        }
    }

    @Test
    fun `should check route security correctly`() {
        // Arrange
        val highRiskRoute = bidEvent.copy(
            route = RouteInfo(
                pickupLocation = LocationInfo(50.4501, 30.5234, "UA"),
                deliveryLocation = LocationInfo(33.5138, 36.2765, "SY") // Сирия - высокорисковая страна
            )
        )

        every { masterOrderRepository.findById(highRiskRoute.masterOrderId) } returns Optional.of(masterOrder)
        every { bidRepository.findById(highRiskRoute.bidId) } returns Optional.of(bid)
        every { userProfileRepository.findById(highRiskRoute.carrierId) } returns Optional.of(userProfile)
        
        every { scmSnapshotRepository.save(any()) } returns mockk()
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        scmIntegrationService.handleBidPlacementEvent(highRiskRoute)

        // Assert
        verify {
            kafkaTemplate.send("compliance.checks", any<ComplianceCheckResult>())
        }
    }

    @Test
    fun `should check cargo security correctly`() {
        // Arrange
        val hazardousCargoEvent = bidEvent.copy(
            cargoDetails = bidEvent.cargoDetails.copy(hazardous = true)
        )

        every { masterOrderRepository.findById(hazardousCargoEvent.masterOrderId) } returns Optional.of(masterOrder)
        every { bidRepository.findById(hazardousCargoEvent.bidId) } returns Optional.of(bid)
        every { userProfileRepository.findById(hazardousCargoEvent.carrierId) } returns Optional.of(userProfile)
        
        every { scmSnapshotRepository.save(any()) } returns mockk()
        every { kafkaTemplate.send(any(), any()) } returns mockk()

        // Act
        scmIntegrationService.handleBidPlacementEvent(hazardousCargoEvent)

        // Assert
        verify {
            kafkaTemplate.send("compliance.checks", any<ComplianceCheckResult>())
        }
    }

    private fun createMockPoint(lat: Double, lon: Double): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
    }
}