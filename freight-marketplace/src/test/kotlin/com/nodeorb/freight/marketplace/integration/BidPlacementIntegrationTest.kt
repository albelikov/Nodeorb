package com.nodeorb.freight.marketplace.integration

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.*
import com.nodeorb.freight.marketplace.service.OrderService
import com.nodeorb.freight.marketplace.service.ScmIntegrationService
import com.nodeorb.freight.marketplace.service.ComplianceService
import com.nodeorb.freight.marketplace.service.TrustTokenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BidPlacementIntegrationTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var scmIntegrationService: ScmIntegrationService

    @Autowired
    private lateinit var complianceService: ComplianceService

    @Autowired
    private lateinit var trustTokenService: TrustTokenService

    @Autowired
    private lateinit var masterOrderRepository: MasterOrderRepository

    @Autowired
    private lateinit var partialOrderRepository: PartialOrderRepository

    @Autowired
    private lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    private lateinit var bidRepository: BidRepository

    @Autowired
    private lateinit var scmSnapshotRepository: ScmSnapshotRepository

    private lateinit var shipperId: UUID
    private lateinit var carrierId: UUID
    private lateinit var masterOrder: MasterOrderEntity
    private lateinit var userProfile: UserProfileEntity

    @BeforeEach
    fun setUp() {
        shipperId = UUID.randomUUID()
        carrierId = UUID.randomUUID()

        // Создаем профиль перевозчика
        userProfile = UserProfileEntity(
            userId = carrierId,
            companyName = "Test Carrier",
            rating = 4.5,
            totalOrders = 50,
            completedOrders = 48
        )
        userProfileRepository.save(userProfile)

        // Создаем мастер-заказ
        masterOrder = orderService.createMasterOrder(
            shipperId = shipperId,
            title = "Integration Test Order",
            description = "Test order for bid placement integration",
            cargoType = CargoType.GENERAL,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            pickupLocation = createMockPoint(50.4501, 30.5234),
            deliveryLocation = createMockPoint(49.8397, 24.0297),
            pickupAddress = "Kyiv, Ukraine",
            deliveryAddress = "Lviv, Ukraine",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("5000.00"),
            isLtlEnabled = true,
            minLoadPercentage = 0.8
        )
    }

    @Test
    fun `should successfully place bid and trigger SCM integration`() {
        // Arrange
        val bidRequest = BidRequestDto(
            weight = BigDecimal("400.00"),
            volume = BigDecimal("20.00"),
            amount = BigDecimal("2000.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5),
            notes = "Test bid for integration test",
            hazardous = false,
            temperatureControlled = false
        )

        // Act
        val bidResponse = placeBid(carrierId, masterOrder.id!!, bidRequest)

        // Assert
        assertNotNull(bidResponse)
        assertNotNull(bidResponse.bidId)
        assertNotNull(bidResponse.partialOrderId)
        assertNotNull(bidResponse.trustToken)
        assertEquals("COMPLIANT", bidResponse.complianceStatus)
        assertTrue(bidResponse.riskScore >= 0.0 && bidResponse.riskScore <= 1.0)

        // Verify entities were created
        val bid = bidRepository.findById(bidResponse.bidId).orElseThrow()
        val partialOrder = partialOrderRepository.findById(bidResponse.partialOrderId).orElseThrow()

        assertEquals(carrierId, bid.carrierId)
        assertEquals(bidRequest.amount, bid.amount)
        assertEquals(masterOrder.id, partialOrder.masterOrder.id)
        assertEquals(bidRequest.weight, partialOrder.weight)
        assertEquals(bidRequest.volume, partialOrder.volume)
        assertEquals(PartialOrderStatus.AVAILABLE, partialOrder.status)
    }

    @Test
    fun `should perform compliance check during bid placement`() {
        // Arrange
        val bidRequest = BidRequestDto(
            weight = BigDecimal("300.00"),
            volume = BigDecimal("15.00"),
            amount = BigDecimal("1500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(4),
            notes = "Compliance test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act
        val bidResponse = placeBid(carrierId, masterOrder.id!!, bidRequest)

        // Assert
        assertNotNull(bidResponse)
        assertEquals("COMPLIANT", bidResponse.complianceStatus)

        // Verify SCM snapshot was created
        val snapshots = scmSnapshotRepository.findByMasterOrderId(masterOrder.id!!)
        assertTrue(snapshots.isNotEmpty())

        val snapshot = snapshots.first()
        assertEquals(ComplianceStatus.COMPLIANT, snapshot.complianceStatus)
        assertEquals(carrierId, snapshot.carrierId)
        assertEquals(masterOrder.id, snapshot.masterOrderId)
        assertTrue(snapshot.riskScore >= 0.0 && snapshot.riskScore <= 1.0)
    }

    @Test
    fun `should generate trust token during bid placement`() {
        // Arrange
        val bidRequest = BidRequestDto(
            weight = BigDecimal("250.00"),
            volume = BigDecimal("12.50"),
            amount = BigDecimal("1250.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(3),
            notes = "Trust token test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act
        val bidResponse = placeBid(carrierId, masterOrder.id!!, bidRequest)

        // Assert
        assertNotNull(bidResponse.trustToken)
        assertTrue(bidResponse.trustToken.startsWith("TRUST_"))

        // Verify trust token is valid
        val validationResult = trustTokenService.validateTrustToken(bidResponse.trustToken)
        assertTrue(validationResult is TrustTokenService.TokenValidationResult.VALID)

        val tokenData = (validationResult as TrustTokenService.TokenValidationResult.VALID).tokenData
        assertEquals(carrierId, tokenData.carrierId)
        assertEquals(masterOrder.id, tokenData.masterOrderId)
        assertEquals("COMPLIANT", tokenData.complianceStatus)
        assertTrue(tokenData.permissions.contains("BID_SUBMISSION"))
    }

    @Test
    fun `should reject bid with insufficient volume`() {
        // Arrange
        val bidRequest = BidRequestDto(
            weight = BigDecimal("100.00"), // Below minimum load percentage
            volume = BigDecimal("5.00"),
            amount = BigDecimal("500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(2),
            notes = "Low volume test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act & Assert
        try {
            placeBid(carrierId, masterOrder.id!!, bidRequest)
        } catch (e: RuntimeException) {
            assertTrue(e.message!!.contains("below minimum load percentage"))
        }
    }

    @Test
    fun `should reject bid exceeding available capacity`() {
        // Arrange
        val bidRequest = BidRequestDto(
            weight = BigDecimal("1500.00"), // Exceeds total weight
            volume = BigDecimal("75.00"),
            amount = BigDecimal("7500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(6),
            notes = "Exceed capacity test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act & Assert
        try {
            placeBid(carrierId, masterOrder.id!!, bidRequest)
        } catch (e: RuntimeException) {
            assertTrue(e.message!!.contains("exceeds available capacity"))
        }
    }

    @Test
    fun `should handle non-compliant carrier`() {
        // Arrange - Create a non-compliant carrier
        val nonCompliantCarrierId = UUID.randomUUID()
        val nonCompliantProfile = UserProfileEntity(
            userId = nonCompliantCarrierId,
            companyName = "Non-Compliant Carrier",
            rating = 2.0, // Low rating
            totalOrders = 5, // Low experience
            completedOrders = 3
        )
        userProfileRepository.save(nonCompliantProfile)

        val bidRequest = BidRequestDto(
            weight = BigDecimal("300.00"),
            volume = BigDecimal("15.00"),
            amount = BigDecimal("1500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(4),
            notes = "Non-compliant carrier test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act & Assert
        try {
            placeBid(nonCompliantCarrierId, masterOrder.id!!, bidRequest)
        } catch (e: RuntimeException) {
            assertTrue(e.message!!.contains("does not meet compliance requirements"))
        }
    }

    @Test
    fun `should update order progress after bid placement`() {
        // Arrange
        val initialProgress = orderService.getOrderProgress(masterOrder.id!!)

        // Act
        val bidRequest = BidRequestDto(
            weight = BigDecimal("400.00"),
            volume = BigDecimal("20.00"),
            amount = BigDecimal("2000.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5),
            notes = "Progress test bid",
            hazardous = false,
            temperatureControlled = false
        )

        placeBid(carrierId, masterOrder.id!!, bidRequest)

        // Assert
        val updatedProgress = orderService.getOrderProgress(masterOrder.id!!)
        
        assertEquals(0.0, initialProgress.filledPercentage)
        assertEquals(0.4, updatedProgress.filledPercentage) // 400/1000 = 0.4
        
        assertEquals(ProgressStatus.OPEN, initialProgress.progressStatus)
        assertEquals(ProgressStatus.PENDING, updatedProgress.progressStatus)
        
        assertEquals(BigDecimal("0.00"), initialProgress.committedWeight)
        assertEquals(BigDecimal("400.00"), updatedProgress.committedWeight)
    }

    @Test
    fun `should handle hazardous cargo requirements`() {
        // Arrange - Create order with hazardous cargo
        val hazardousOrder = orderService.createMasterOrder(
            shipperId = shipperId,
            title = "Hazardous Cargo Order",
            cargoType = CargoType.HAZARDOUS,
            totalWeight = BigDecimal("500.00"),
            totalVolume = BigDecimal("25.00"),
            pickupLocation = createMockPoint(50.4501, 30.5234),
            deliveryLocation = createMockPoint(49.8397, 24.0297),
            pickupAddress = "Kyiv, Ukraine",
            deliveryAddress = "Lviv, Ukraine",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("3000.00")
        )

        val bidRequest = BidRequestDto(
            weight = BigDecimal("250.00"),
            volume = BigDecimal("12.50"),
            amount = BigDecimal("1500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(4),
            notes = "Hazardous cargo test bid",
            hazardous = true,
            temperatureControlled = false
        )

        // Act & Assert
        try {
            placeBid(carrierId, hazardousOrder.id!!, bidRequest)
        } catch (e: RuntimeException) {
            // Expected for carrier without hazardous cargo license
            assertTrue(e.message!!.contains("does not meet compliance requirements"))
        }
    }

    @Test
    fun `should validate trust token after bid placement`() {
        // Arrange
        val bidRequest = BidRequestDto(
            weight = BigDecimal("300.00"),
            volume = BigDecimal("15.00"),
            amount = BigDecimal("1500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(4),
            notes = "Token validation test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act
        val bidResponse = placeBid(carrierId, masterOrder.id!!, bidRequest)

        // Assert
        val validationResult = trustTokenService.validateTrustToken(bidResponse.trustToken)
        assertTrue(validationResult is TrustTokenService.TokenValidationResult.VALID)

        val tokenData = (validationResult as TrustTokenService.TokenValidationResult.VALID).tokenData
        assertEquals(carrierId, tokenData.carrierId)
        assertEquals(masterOrder.id, tokenData.masterOrderId)
        assertEquals(SecurityLevel.NONE, tokenData.securityLevel)
        assertTrue(tokenData.expiresAt > System.currentTimeMillis())
    }

    // Вспомогательные методы

    private fun placeBid(carrierId: UUID, orderId: UUID, bidRequest: BidRequestDto): BidResponseDto {
        // Здесь должен быть реальный HTTP запрос, но для интеграционного теста
        // мы можем вызвать сервис напрямую или использовать TestRestTemplate
        
        // Для упрощения вызываем сервис напрямую
        // В реальном интеграционном тесте нужно использовать @WebMvcTest или TestRestTemplate
        
        throw NotImplementedError("This should be implemented with TestRestTemplate or WebTestClient")
    }

    private fun createMockPoint(lat: Double, lon: Double): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
    }
}