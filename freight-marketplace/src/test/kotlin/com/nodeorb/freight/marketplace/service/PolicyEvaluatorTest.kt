package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.exception.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@ExtendWith(MockKExtension::class)
class PolicyEvaluatorTest {

    @MockK
    private lateinit var scmSnapshotRepository: ScmSnapshotRepository

    @InjectMockKs
    private lateinit var policyEvaluator: PolicyEvaluator

    private lateinit var masterOrder: MasterOrderEntity
    private lateinit var scmSnapshot: ScmSnapshotEntity
    private lateinit var carrierId: UUID

    @BeforeEach
    fun setUp() {
        carrierId = UUID.randomUUID()
        
        masterOrder = MasterOrderEntity(
            shipperId = UUID.randomUUID(),
            title = "Test Order",
            cargoType = CargoType.HAZARDOUS,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("1000.00"),
            remainingVolume = BigDecimal("50.00"),
            pickupLocation = createMockPoint(50.4501, 30.5234),
            deliveryLocation = createMockPoint(49.8397, 24.0297),
            pickupAddress = "Kyiv, Ukraine",
            deliveryAddress = "Lviv, Ukraine",
            requiredDeliveryDate = java.time.LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("5000.00"),
            isLtlEnabled = true,
            minLoadPercentage = 0.8
        )

        scmSnapshot = ScmSnapshotEntity(
            carrierId = carrierId,
            masterOrderId = UUID.randomUUID(),
            bidId = UUID.randomUUID(),
            complianceStatus = ComplianceStatus.COMPLIANT,
            riskScore = 0.3,
            riskFactors = "Low risk",
            securityClearance = SecurityLevel.NONE,
            licenses = "General_Cargo",
            complianceHistory = "Clean record",
            createdAt = java.time.LocalDateTime.now(),
            availableQuota = BigDecimal("100.00")
        )
    }

    @Test
    fun `should evaluate compliance for hazardous cargo without ADR permit`() {
        // Arrange
        masterOrder.cargoType = CargoType.HAZARDOUS
        scmSnapshot.licenses = "General_Cargo" // Нет ADR_Permit

        // Act
        val result = policyEvaluator.evaluateCarrierCompliance(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertEquals(com.nodeorb.freight.marketplace.service.ComplianceStatus.VIOLATION, result.status)
        assertTrue(result.violations.any { it.contains("ADR permit") })
    }

    @Test
    fun `should evaluate compliance for hazardous cargo with ADR permit`() {
        // Arrange
        masterOrder.cargoType = CargoType.HAZARDOUS
        scmSnapshot.licenses = "General_Cargo, ADR_Permit"

        // Act
        val result = policyEvaluator.evaluateCarrierCompliance(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertEquals(com.nodeorb.freight.marketplace.service.ComplianceStatus.COMPLIANT, result.status)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `should evaluate compliance with high risk score`() {
        // Arrange
        scmSnapshot.riskScore = 0.8 // Выше порога 0.7

        // Act
        val result = policyEvaluator.evaluateCarrierCompliance(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertEquals(com.nodeorb.freight.marketplace.service.ComplianceStatus.VIOLATION, result.status)
        assertTrue(result.violations.any { it.contains("high risk score") })
    }

    @Test
    fun `should evaluate compliance with elevated risk score`() {
        // Arrange
        scmSnapshot.riskScore = 0.6 // Выше 0.5, но ниже 0.7

        // Act
        val result = policyEvaluator.evaluateCarrierCompliance(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertEquals(com.nodeorb.freight.marketplace.service.ComplianceStatus.WARNING, result.status)
        assertTrue(result.warnings.any { it.contains("elevated risk score") })
    }

    @Test
    fun `should evaluate compliance with insufficient quota`() {
        // Arrange
        scmSnapshot.availableQuota = BigDecimal("40.00") // Меньше требуемого объема 50.00

        // Act
        val result = policyEvaluator.evaluateCarrierCompliance(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertEquals(com.nodeorb.freight.marketplace.service.ComplianceStatus.VIOLATION, result.status)
        assertTrue(result.violations.any { it.contains("insufficient quota") })
    }

    @Test
    fun `should validate bid eligibility for compliant carrier`() {
        // Arrange
        scmSnapshot.riskScore = 0.3
        scmSnapshot.availableQuota = BigDecimal("100.00")

        // Act
        val result = policyEvaluator.validateBidEligibility(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertTrue(result.isEligible)
        assertEquals("All compliance checks passed", result.reason)
    }

    @Test
    fun `should validate bid eligibility for carrier with violations`() {
        // Arrange
        scmSnapshot.riskScore = 0.8 // Высокий риск

        // Act
        val result = policyEvaluator.validateBidEligibility(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertFalse(result.isEligible)
        assertTrue(result.reason.contains("violations"))
    }

    @Test
    fun `should validate awarding eligibility for compliant carrier`() {
        // Arrange
        scmSnapshot.riskScore = 0.3
        scmSnapshot.availableQuota = BigDecimal("100.00")

        // Act
        val result = policyEvaluator.validateAwardingEligibility(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertTrue(result.isEligible)
        assertEquals("All compliance checks passed", result.reason)
        assertFalse(result.requiresApproval)
    }

    @Test
    fun `should validate awarding eligibility requiring manual approval`() {
        // Arrange
        scmSnapshot.riskScore = 0.75 // Выше порога 0.7

        // Act
        val result = policyEvaluator.validateAwardingEligibility(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertFalse(result.isEligible)
        assertTrue(result.requiresApproval)
        assertTrue(result.reason.contains("manual approval"))
    }

    @Test
    fun `should update carrier quota correctly`() {
        // Arrange
        val initialQuota = BigDecimal("100.00")
        val bidVolume = BigDecimal("20.00")
        scmSnapshot.availableQuota = initialQuota

        // Act
        val updatedSnapshot = policyEvaluator.updateCarrierQuota(
            scmSnapshot,
            bidVolume,
            com.nodeorb.freight.marketplace.service.QuotaAction.BURN
        )

        // Assert
        assertEquals(initialQuota - bidVolume, updatedSnapshot.availableQuota)
        assertEquals(1, updatedSnapshot.quotaHistory.size)
        
        val historyEntry = updatedSnapshot.quotaHistory.first()
        assertEquals(com.nodeorb.freight.marketplace.service.QuotaAction.BURN, historyEntry.action)
        assertEquals(bidVolume, historyEntry.volume)
        assertEquals(initialQuota, historyEntry.previousQuota)
        assertEquals(initialQuota - bidVolume, historyEntry.newQuota)
    }

    @Test
    fun `should throw exception when insufficient quota for burning`() {
        // Arrange
        val initialQuota = BigDecimal("10.00")
        val bidVolume = BigDecimal("20.00") // Больше доступной квоты
        scmSnapshot.availableQuota = initialQuota

        // Act & Assert
        try {
            policyEvaluator.updateCarrierQuota(
                scmSnapshot,
                bidVolume,
                com.nodeorb.freight.marketplace.service.QuotaAction.BURN
            )
            throw AssertionError("Expected InsufficientQuotaException")
        } catch (e: InsufficientQuotaException) {
            assertTrue(e.message!!.contains("Insufficient quota"))
        }
    }

    @Test
    fun `should release quota correctly`() {
        // Arrange
        val initialQuota = BigDecimal("80.00")
        val releasedVolume = BigDecimal("20.00")
        scmSnapshot.availableQuota = initialQuota

        // Act
        val updatedSnapshot = policyEvaluator.updateCarrierQuota(
            scmSnapshot,
            releasedVolume,
            com.nodeorb.freight.marketplace.service.QuotaAction.RELEASE
        )

        // Assert
        assertEquals(initialQuota + releasedVolume, updatedSnapshot.availableQuota)
    }

    @Test
    fun `should check manual approval requirement correctly`() {
        // Arrange
        scmSnapshot.riskScore = 0.75 // Выше порога 0.7

        // Act
        val requiresApproval = policyEvaluator.requiresManualApproval(scmSnapshot)

        // Assert
        assertTrue(requiresApproval)
    }

    @Test
    fun `should get compliance recommendations correctly`() {
        // Arrange
        masterOrder.cargoType = CargoType.HAZARDOUS
        scmSnapshot.licenses = "General_Cargo" // Нет ADR_Permit
        scmSnapshot.riskScore = 0.6 // Выше 0.5
        scmSnapshot.availableQuota = BigDecimal("40.00") // Меньше требуемого

        // Act
        val recommendations = policyEvaluator.getComplianceRecommendations(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertEquals(3, recommendations.size)
        assertTrue(recommendations.any { it.contains("ADR permit") })
        assertTrue(recommendations.any { it.contains("compliance score") })
        assertTrue(recommendations.any { it.contains("increase carrier quota") })
    }

    @Test
    fun `should get empty recommendations for compliant carrier`() {
        // Arrange
        scmSnapshot.licenses = "General_Cargo, ADR_Permit"
        scmSnapshot.riskScore = 0.3
        scmSnapshot.availableQuota = BigDecimal("100.00")

        // Act
        val recommendations = policyEvaluator.getComplianceRecommendations(carrierId, masterOrder, scmSnapshot)

        // Assert
        assertTrue(recommendations.isEmpty())
    }

    private fun createMockPoint(lat: Double, lon: Double): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
    }
}