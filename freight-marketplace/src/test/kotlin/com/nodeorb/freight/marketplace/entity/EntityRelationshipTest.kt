package com.nodeorb.freight.marketplace.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class EntityRelationshipTest {

    @Test
    fun `should establish master order to partial orders relationship`() {
        val masterOrder = createTestMasterOrder()
        
        // Create partial orders
        val partialOrder1 = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("300.00"),
            volume = BigDecimal("15.00"),
            percentage = 0.3
        )
        
        val partialOrder2 = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("700.00"),
            volume = BigDecimal("35.00"),
            percentage = 0.7
        )
        
        // Add to master order
        masterOrder.partialOrders.add(partialOrder1)
        masterOrder.partialOrders.add(partialOrder2)
        
        // Verify relationship
        assertEquals(2, masterOrder.partialOrders.size)
        assertTrue(masterOrder.partialOrders.contains(partialOrder1))
        assertTrue(masterOrder.partialOrders.contains(partialOrder2))
        
        // Verify bidirectional relationship
        assertEquals(masterOrder, partialOrder1.masterOrder)
        assertEquals(masterOrder, partialOrder2.masterOrder)
    }

    @Test
    fun `should establish master order to bids relationship`() {
        val masterOrder = createTestMasterOrder()
        
        // Create bids
        val bid1 = createTestBid()
        bid1.masterOrder = masterOrder
        
        val bid2 = createTestBid()
        bid2.masterOrder = masterOrder
        
        // Add to master order
        masterOrder.bids.add(bid1)
        masterOrder.bids.add(bid2)
        
        // Verify relationship
        assertEquals(2, masterOrder.bids.size)
        assertTrue(masterOrder.bids.contains(bid1))
        assertTrue(masterOrder.bids.contains(bid2))
        
        // Verify bidirectional relationship
        assertEquals(masterOrder, bid1.masterOrder)
        assertEquals(masterOrder, bid2.masterOrder)
    }

    @Test
    fun `should establish partial order to bid relationship`() {
        val masterOrder = createTestMasterOrder()
        val partialOrder = createTestPartialOrder(masterOrder)
        
        val bid = createTestBid()
        bid.partialOrder = partialOrder
        
        // Verify relationship
        assertEquals(partialOrder, bid.partialOrder)
        assertEquals(masterOrder, partialOrder.masterOrder)
    }

    @Test
    fun `should establish bid to scm snapshot relationship`() {
        val bid = createTestBid()
        val scmSnapshot = createTestScmSnapshot()
        
        // Note: In the current design, SCM snapshot is linked by bid_id
        // rather than direct entity relationship for audit purposes
        assertEquals(bid.id, scmSnapshot.bidId)
    }

    @Test
    fun `should handle cascade operations correctly`() {
        val masterOrder = createTestMasterOrder()
        
        // Create partial orders
        val partialOrder1 = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("500.00"),
            volume = BigDecimal("25.00"),
            percentage = 0.5
        )
        
        val partialOrder2 = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("500.00"),
            volume = BigDecimal("25.00"),
            percentage = 0.5
        )
        
        masterOrder.partialOrders.add(partialOrder1)
        masterOrder.partialOrders.add(partialOrder2)
        
        // Verify cascade configuration
        assertEquals(2, masterOrder.partialOrders.size)
        assertEquals(masterOrder, partialOrder1.masterOrder)
        assertEquals(masterOrder, partialOrder2.masterOrder)
        
        // Test removing partial order
        masterOrder.partialOrders.remove(partialOrder1)
        assertEquals(1, masterOrder.partialOrders.size)
        assertFalse(masterOrder.partialOrders.contains(partialOrder1))
    }

    @Test
    fun `should validate entity constraints`() {
        val masterOrder = createTestMasterOrder()
        
        // Test weight and volume constraints
        assertTrue(masterOrder.totalWeight > BigDecimal.ZERO)
        assertTrue(masterOrder.totalVolume > BigDecimal.ZERO)
        assertEquals(masterOrder.totalWeight, masterOrder.remainingWeight)
        assertEquals(masterOrder.totalVolume, masterOrder.remainingVolume)
        
        // Test percentage constraints for partial orders
        val partialOrder = createTestPartialOrder(masterOrder)
        assertTrue(partialOrder.percentage > 0.0)
        assertTrue(partialOrder.percentage <= 1.0)
        
        // Test that partial order weight/volume doesn't exceed master
        assertTrue(partialOrder.weight <= masterOrder.totalWeight)
        assertTrue(partialOrder.volume <= masterOrder.totalVolume)
    }

    @Test
    fun `should handle null relationships correctly`() {
        val masterOrder = createTestMasterOrder()
        
        // Master order without partial orders
        assertEquals(0, masterOrder.partialOrders.size)
        
        // Master order without bids
        assertEquals(0, masterOrder.bids.size)
        
        // Partial order without assignment
        val partialOrder = createTestPartialOrder(masterOrder)
        assertNull(partialOrder.assignedCarrierId)
        assertNull(partialOrder.assignedBidId)
        
        // Bid without partial order assignment
        val bid = createTestBid()
        assertNull(bid.partialOrder)
    }

    @Test
    fun `should maintain data consistency in relationships`() {
        val masterOrder = createTestMasterOrder()
        
        // Create partial orders that sum up to master order
        val partialOrder1 = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("400.00"),
            volume = BigDecimal("20.00"),
            percentage = 0.4
        )
        
        val partialOrder2 = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("600.00"),
            volume = BigDecimal("30.00"),
            percentage = 0.6
        )
        
        masterOrder.partialOrders.add(partialOrder1)
        masterOrder.partialOrders.add(partialOrder2)
        
        // Verify total consistency
        val totalPartialWeight = masterOrder.partialOrders.sumOf { it.weight }
        val totalPartialVolume = masterOrder.partialOrders.sumOf { it.volume }
        val totalPartialPercentage = masterOrder.partialOrders.sumOf { it.percentage }
        
        assertEquals(masterOrder.totalWeight, totalPartialWeight)
        assertEquals(masterOrder.totalVolume, totalPartialVolume)
        assertEquals(1.0, totalPartialPercentage)
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

    private fun createTestPartialOrder(masterOrder: MasterOrderEntity): PartialOrderEntity {
        return PartialOrderEntity(
            masterOrder = masterOrder,
            weight = BigDecimal("500.00"),
            volume = BigDecimal("25.00"),
            percentage = 0.5
        )
    }

    private fun createTestBid(): BidEntity {
        return BidEntity(
            carrierId = UUID.randomUUID(),
            amount = BigDecimal("2500.00"),
            proposedDeliveryDate = LocalDateTime.now().plusDays(5)
        )
    }

    private fun createTestScmSnapshot(): ScmSnapshotEntity {
        return ScmSnapshotEntity(
            bidId = UUID.randomUUID(),
            carrierId = UUID.randomUUID(),
            masterOrderId = UUID.randomUUID(),
            snapshotDate = LocalDateTime.now(),
            complianceStatus = ComplianceStatus.COMPLIANT,
            complianceDetails = "Compliance check passed",
            securityClearance = SecurityLevel.NONE,
            securityDetails = "No security restrictions",
            riskScore = 0.1,
            riskFactors = "Low risk",
            auditTrail = "Audit trail data"
        )
    }

    private fun createMockPoint(lat: Double, lon: Double): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
    }
}