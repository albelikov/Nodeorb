package com.nodeorb.freight.marketplace.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class MasterOrderEntityTest {

    @Test
    fun `should create master order with valid data`() {
        val shipperId = UUID.randomUUID()
        val pickupLocation = createMockPoint(50.4501, 30.5234) // Киев
        val deliveryLocation = createMockPoint(49.8397, 24.0297) // Львов
        
        val masterOrder = MasterOrderEntity(
            shipperId = shipperId,
            title = "Test LTL Order",
            description = "Test order for LTL shipping",
            cargoType = CargoType.GENERAL,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("1000.00"),
            remainingVolume = BigDecimal("50.00"),
            pickupLocation = pickupLocation,
            deliveryLocation = deliveryLocation,
            pickupAddress = "Kyiv, Ukraine",
            deliveryAddress = "Lviv, Ukraine",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("5000.00"),
            isLtlEnabled = true,
            minLoadPercentage = 0.8
        )

        assertNotNull(masterOrder.id)
        assertEquals(shipperId, masterOrder.shipperId)
        assertEquals("Test LTL Order", masterOrder.title)
        assertEquals(CargoType.GENERAL, masterOrder.cargoType)
        assertEquals(BigDecimal("1000.00"), masterOrder.totalWeight)
        assertEquals(BigDecimal("50.00"), masterOrder.totalVolume)
        assertEquals(BigDecimal("1000.00"), masterOrder.remainingWeight)
        assertEquals(BigDecimal("50.00"), masterOrder.remainingVolume)
        assertEquals(MasterOrderStatus.OPEN, masterOrder.status)
        assertTrue(masterOrder.isLtlEnabled)
        assertEquals(0.8, masterOrder.minLoadPercentage)
        assertNotNull(masterOrder.createdAt)
        assertNotNull(masterOrder.updatedAt)
    }

    @Test
    fun `should create partial order with valid data`() {
        val masterOrder = createTestMasterOrder()
        val partialOrder = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("500.00"),
            volume = BigDecimal("25.00"),
            percentage = 0.5,
            status = PartialOrderStatus.AVAILABLE
        )

        assertNotNull(partialOrder.id)
        assertEquals(masterOrder, partialOrder.masterOrder)
        assertEquals(BigDecimal("500.00"), partialOrder.weight)
        assertEquals(BigDecimal("25.00"), partialOrder.volume)
        assertEquals(0.5, partialOrder.percentage)
        assertEquals(PartialOrderStatus.AVAILABLE, partialOrder.status)
        assertNotNull(partialOrder.createdAt)
        assertNotNull(partialOrder.updatedAt)
    }

    @Test
    fun `should create SCM snapshot with valid data`() {
        val bidId = UUID.randomUUID()
        val carrierId = UUID.randomUUID()
        val masterOrderId = UUID.randomUUID()
        val snapshotDate = LocalDateTime.now()
        
        val scmSnapshot = ScmSnapshotEntity(
            bidId = bidId,
            carrierId = carrierId,
            masterOrderId = masterOrderId,
            snapshotDate = snapshotDate,
            complianceStatus = ComplianceStatus.COMPLIANT,
            complianceDetails = "All compliance checks passed",
            securityClearance = SecurityLevel.CONFIDENTIAL,
            securityDetails = "Valid security clearance",
            riskScore = 0.2,
            riskFactors = "Low risk factors",
            auditTrail = "Audit trail data"
        )

        assertNotNull(scmSnapshot.id)
        assertEquals(bidId, scmSnapshot.bidId)
        assertEquals(carrierId, scmSnapshot.carrierId)
        assertEquals(masterOrderId, scmSnapshot.masterOrderId)
        assertEquals(snapshotDate, scmSnapshot.snapshotDate)
        assertEquals(ComplianceStatus.COMPLIANT, scmSnapshot.complianceStatus)
        assertEquals("All compliance checks passed", scmSnapshot.complianceDetails)
        assertEquals(SecurityLevel.CONFIDENTIAL, scmSnapshot.securityClearance)
        assertEquals("Valid security clearance", scmSnapshot.securityDetails)
        assertEquals(0.2, scmSnapshot.riskScore)
        assertEquals("Low risk factors", scmSnapshot.riskFactors)
        assertEquals("Audit trail data", scmSnapshot.auditTrail)
        assertNotNull(scmSnapshot.createdAt)
    }

    @Test
    fun `should update master order status correctly`() {
        val masterOrder = createTestMasterOrder()
        
        // Test status transitions
        masterOrder.status = MasterOrderStatus.PARTIALLY_FILLED
        assertEquals(MasterOrderStatus.PARTIALLY_FILLED, masterOrder.status)
        
        masterOrder.status = MasterOrderStatus.FILLED
        assertEquals(MasterOrderStatus.FILLED, masterOrder.status)
        
        masterOrder.status = MasterOrderStatus.IN_PROGRESS
        assertEquals(MasterOrderStatus.IN_PROGRESS, masterOrder.status)
        
        masterOrder.status = MasterOrderStatus.COMPLETED
        assertEquals(MasterOrderStatus.COMPLETED, masterOrder.status)
        
        masterOrder.status = MasterOrderStatus.CANCELLED
        assertEquals(MasterOrderStatus.CANCELLED, masterOrder.status)
    }

    @Test
    fun `should update partial order status correctly`() {
        val partialOrder = createTestPartialOrder()
        
        // Test status transitions
        partialOrder.status = PartialOrderStatus.BIDDING
        assertEquals(PartialOrderStatus.BIDDING, partialOrder.status)
        
        partialOrder.status = PartialOrderStatus.AWARDED
        assertEquals(PartialOrderStatus.AWARDED, partialOrder.status)
        
        partialOrder.status = PartialOrderStatus.IN_PROGRESS
        assertEquals(PartialOrderStatus.IN_PROGRESS, partialOrder.status)
        
        partialOrder.status = PartialOrderStatus.COMPLETED
        assertEquals(PartialOrderStatus.COMPLETED, partialOrder.status)
        
        partialOrder.status = PartialOrderStatus.CANCELLED
        assertEquals(PartialOrderStatus.CANCELLED, partialOrder.status)
    }

    @Test
    fun `should assign carrier to partial order`() {
        val partialOrder = createTestPartialOrder()
        val carrierId = UUID.randomUUID()
        
        partialOrder.assignedCarrierId = carrierId
        assertEquals(carrierId, partialOrder.assignedCarrierId)
        
        val bidId = UUID.randomUUID()
        partialOrder.assignedBidId = bidId
        assertEquals(bidId, partialOrder.assignedBidId)
    }

    @Test
    fun `should handle null values in SCM snapshot`() {
        val scmSnapshot = ScmSnapshotEntity(
            bidId = UUID.randomUUID(),
            carrierId = UUID.randomUUID(),
            masterOrderId = UUID.randomUUID(),
            snapshotDate = LocalDateTime.now(),
            complianceStatus = ComplianceStatus.PENDING,
            complianceDetails = null, // Null details
            securityClearance = SecurityLevel.NONE,
            securityDetails = null, // Null security details
            riskScore = 0.5,
            riskFactors = null, // Null risk factors
            auditTrail = "Audit trail"
        )

        assertNull(scmSnapshot.complianceDetails)
        assertNull(scmSnapshot.securityDetails)
        assertNull(scmSnapshot.riskFactors)
        assertEquals(ComplianceStatus.PENDING, scmSnapshot.complianceStatus)
        assertEquals(SecurityLevel.NONE, scmSnapshot.securityClearance)
    }

    private fun createTestMasterOrder(): MasterOrderEntity {
        val shipperId = UUID.randomUUID()
        val pickupLocation = createMockPoint(50.4501, 30.5234)
        val deliveryLocation = createMockPoint(49.8397, 24.0297)
        
        return MasterOrderEntity(
            shipperId = shipperId,
            title = "Test Order",
            cargoType = CargoType.GENERAL,
            totalWeight = BigDecimal("1000.00"),
            totalVolume = BigDecimal("50.00"),
            remainingWeight = BigDecimal("1000.00"),
            remainingVolume = BigDecimal("50.00"),
            pickupLocation = pickupLocation,
            deliveryLocation = deliveryLocation,
            pickupAddress = "Test Pickup",
            deliveryAddress = "Test Delivery",
            requiredDeliveryDate = LocalDateTime.now().plusDays(7),
            maxBidAmount = BigDecimal("5000.00")
        )
    }

    private fun createTestPartialOrder(): PartialOrderEntity {
        val masterOrder = createTestMasterOrder()
        return PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("500.00"),
            volume = BigDecimal("25.00"),
            percentage = 0.5
        )
    }

    private fun createMockPoint(lat: Double, lon: Double): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
    }
}