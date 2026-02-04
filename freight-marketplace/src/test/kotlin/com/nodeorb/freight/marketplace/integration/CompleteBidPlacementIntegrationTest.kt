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
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.security.test.context.support.WithMockUser
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class CompleteBidPlacementIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    private lateinit var masterOrderRepository: MasterOrderRepository

    @Autowired
    private lateinit var bidRepository: BidRepository

    @Autowired
    private lateinit var partialOrderRepository: PartialOrderRepository

    @Autowired
    private lateinit var scmSnapshotRepository: ScmSnapshotRepository

    @Autowired
    private lateinit var trustTokenService: TrustTokenService

    private lateinit var mockMvc: MockMvc
    private lateinit var webTestClient: WebTestClient
    private lateinit var shipperId: UUID
    private lateinit var carrierId: UUID
    private lateinit var masterOrder: MasterOrderEntity

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        webTestClient = WebTestClient.bindToApplicationContext(webApplicationContext).build()

        shipperId = UUID.randomUUID()
        carrierId = UUID.randomUUID()

        // Создаем профиль перевозчика
        val userProfile = UserProfileEntity(
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
            description = "Test order for complete bid placement integration",
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
    @WithMockUser(username = "carrier@example.com", authorities = ["CARRIER"])
    fun `should complete bid placement flow with SCM integration`() {
        // Arrange
        val bidRequest = BidRequestDto(
            weight = BigDecimal("400.00"),
            volume = BigDecimal("20.00"),
            amount = BigDecimal("2000.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5),
            notes = "Complete integration test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act - Place bid via REST API
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/orders/${masterOrder.id}/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bidRequest))
        )

        // Assert - Check response
        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk)
        
        val responseContent = response.andReturn().response.contentAsString
        val bidResponse = objectMapper.readValue(responseContent, BidResponseDto::class.java)

        assertNotNull(bidResponse.bidId)
        assertNotNull(bidResponse.partialOrderId)
        assertNotNull(bidResponse.trustToken)
        assertEquals("COMPLIANT", bidResponse.complianceStatus)

        // Verify entities were created
        val bid = bidRepository.findById(bidResponse.bidId).orElseThrow()
        val partialOrder = partialOrderRepository.findById(bidResponse.partialOrderId).orElseThrow()

        assertEquals(carrierId, bid.carrierId)
        assertEquals(bidRequest.amount, bid.amount)
        assertEquals(masterOrder.id, partialOrder.masterOrder.id)
        assertEquals(bidRequest.weight, partialOrder.weight)
        assertEquals(bidRequest.volume, partialOrder.volume)
        assertEquals(PartialOrderStatus.AVAILABLE, partialOrder.status)

        // Verify SCM snapshot was created
        val snapshots = scmSnapshotRepository.findByMasterOrderId(masterOrder.id!!)
        assertTrue(snapshots.isNotEmpty())

        val snapshot = snapshots.first()
        assertEquals(ComplianceStatus.COMPLIANT, snapshot.complianceStatus)
        assertEquals(carrierId, snapshot.carrierId)
        assertEquals(masterOrder.id, snapshot.masterOrderId)
        assertTrue(snapshot.riskScore >= 0.0 && snapshot.riskScore <= 1.0)

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
    @WithMockUser(username = "carrier@example.com", authorities = ["CARRIER"])
    fun `should validate SCM limits during bid placement`() {
        // Arrange - Create a high-risk scenario
        val userProfile = userProfileRepository.findById(carrierId).orElseThrow()
        userProfile.rating = 2.5 // Lower rating to increase risk
        userProfileRepository.save(userProfile)

        val bidRequest = BidRequestDto(
            weight = BigDecimal("300.00"),
            volume = BigDecimal("15.00"),
            amount = BigDecimal("1500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(4),
            notes = "SCM limits validation test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/orders/${masterOrder.id}/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bidRequest))
        )

        // Assert
        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk)

        val responseContent = response.andReturn().response.contentAsString
        val bidResponse = objectMapper.readValue(responseContent, BidResponseDto::class.java)

        // Verify compliance check was performed
        assertNotNull(bidResponse.complianceStatus)
        val isCompliant = bidResponse.complianceStatus == "COMPLIANT"
        
        // Verify risk score was calculated
        assertTrue(bidResponse.riskScore >= 0.0 && bidResponse.riskScore <= 1.0)

        // Verify SCM snapshot contains risk information
        val snapshots = scmSnapshotRepository.findByMasterOrderId(masterOrder.id!!)
        val snapshot = snapshots.first()
        
        assertTrue(snapshot.riskScore >= 0.0 && snapshot.riskScore <= 1.0)
        assertTrue(snapshot.riskFactors.isNotEmpty() || snapshot.riskFactors.isEmpty())
    }

    @Test
    @WithMockUser(username = "carrier@example.com", authorities = ["CARRIER"])
    fun `should handle bid rejection due to SCM limits`() {
        // Arrange - Create a non-compliant carrier
        val nonCompliantCarrierId = UUID.randomUUID()
        val nonCompliantProfile = UserProfileEntity(
            userId = nonCompliantCarrierId,
            companyName = "Non-Compliant Carrier",
            rating = 1.5, // Very low rating
            totalOrders = 2, // Very low experience
            completedOrders = 1
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

        // Act & Assert - Should be rejected
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/orders/${masterOrder.id}/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bidRequest))
        )

        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
        
        val responseContent = response.andReturn().response.contentAsString
        assertTrue(responseContent.contains("does not meet compliance requirements"))
    }

    @Test
    @WithMockUser(username = "carrier@example.com", authorities = ["CARRIER"])
    fun `should validate volume limits during bid placement`() {
        // Arrange - Try to bid more than available
        val bidRequest = BidRequestDto(
            weight = BigDecimal("1500.00"), // Exceeds total weight
            volume = BigDecimal("75.00"),
            amount = BigDecimal("7500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(6),
            notes = "Volume limit test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act & Assert
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/orders/${masterOrder.id}/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bidRequest))
        )

        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
        
        val responseContent = response.andReturn().response.contentAsString
        assertTrue(responseContent.contains("exceeds available capacity"))
    }

    @Test
    @WithMockUser(username = "carrier@example.com", authorities = ["CARRIER"])
    fun `should validate minimum load percentage`() {
        // Arrange - Try to bid below minimum
        val bidRequest = BidRequestDto(
            weight = BigDecimal("100.00"), // Below minimum load percentage (80%)
            volume = BigDecimal("5.00"),
            amount = BigDecimal("500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(2),
            notes = "Minimum load test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act & Assert
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/orders/${masterOrder.id}/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bidRequest))
        )

        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
        
        val responseContent = response.andReturn().response.contentAsString
        assertTrue(responseContent.contains("below minimum load percentage"))
    }

    @Test
    @WithMockUser(username = "carrier@example.com", authorities = ["CARRIER"])
    fun `should update order progress after successful bid`() {
        // Arrange
        val initialProgress = orderService.getOrderProgress(masterOrder.id!!)

        val bidRequest = BidRequestDto(
            weight = BigDecimal("400.00"),
            volume = BigDecimal("20.00"),
            amount = BigDecimal("2000.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5),
            notes = "Progress update test bid",
            hazardous = false,
            temperatureControlled = false
        )

        // Act
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/orders/${masterOrder.id}/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bidRequest))
        )

        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())

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
    @WithMockUser(username = "carrier@example.com", authorities = ["CARRIER"])
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
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/orders/${hazardousOrder.id}/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bidRequest))
        )

        // Should be rejected for carrier without hazardous cargo license
        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest())
        
        val responseContent = response.andReturn().response.contentAsString
        assertTrue(responseContent.contains("does not meet compliance requirements"))
    }

    @Test
    @WithMockUser(username = "carrier@example.com", authorities = ["CARRIER"])
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

        // Act - Place bid
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/orders/${masterOrder.id}/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bidRequest))
        )

        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())

        val responseContent = response.andReturn().response.contentAsString
        val bidResponse = objectMapper.readValue(responseContent, BidResponseDto::class.java)

        // Validate trust token
        val tokenValidationRequest = TokenValidationRequest(token = bidResponse.trustToken)
        
        val tokenResponse = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/carrier/validate-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenValidationRequest))
        )

        tokenResponse.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())

        val tokenResponseContent = tokenResponse.andReturn().response.contentAsString
        val validationResult = objectMapper.readValue(tokenResponseContent, TokenValidationResult::class.java)

        assertTrue(validationResult.isValid)
        assertNotNull(validationResult.tokenInfo)
        assertEquals(carrierId, validationResult.tokenInfo!!.carrierId)
        assertEquals(masterOrder.id, validationResult.tokenInfo!!.masterOrderId)
        assertEquals(SecurityLevel.NONE.name, validationResult.tokenInfo!!.securityLevel)
        assertTrue(validationResult.tokenInfo!!.expiresAt > System.currentTimeMillis())
    }

    // Вспомогательные методы

    private fun createMockPoint(lat: Double, lon: Double): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
    }
}