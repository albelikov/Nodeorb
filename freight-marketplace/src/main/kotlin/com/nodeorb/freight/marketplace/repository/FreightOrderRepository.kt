package com.nodeorb.freight.marketplace.repository

import com.nodeorb.freight.marketplace.entity.FreightOrderEntity
import com.nodeorb.freight.marketplace.entity.OrderStatus
import com.nodeorb.freight.marketplace.entity.BidEntity
import com.nodeorb.freight.marketplace.entity.BidStatus
import com.nodeorb.freight.marketplace.entity.UserProfileEntity
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FreightOrderRepository : JpaRepository<FreightOrderEntity, UUID> {
    
    fun findByShipperId(shipperId: UUID, pageable: Pageable): Page<FreightOrderEntity>
    
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<FreightOrderEntity>
    
    @Query("""
        SELECT fo FROM FreightOrderEntity fo 
        WHERE fo.status = 'OPEN' 
        AND ST_DWithin(fo.pickupLocation, :currentLocation, :maxDistance)
        AND ST_DWithin(fo.deliveryLocation, :currentLocation, :maxDistance * 2)
    """)
    fun findNearbyOrders(
        @Param("currentLocation") currentLocation: Point,
        @Param("maxDistance") maxDistance: Double,
        pageable: Pageable
    ): Page<FreightOrderEntity>
    
    @Query("""
        SELECT COUNT(fo) FROM FreightOrderEntity fo 
        WHERE fo.shipperId = :shipperId 
        AND fo.status = 'COMPLETED'
    """)
    fun countCompletedOrdersByShipper(@Param("shipperId") shipperId: UUID): Long
    
    @Query("""
        SELECT fo FROM FreightOrderEntity fo 
        WHERE fo.cargoType = :cargoType 
        AND fo.status IN ('OPEN', 'AUCTION_ACTIVE')
        AND fo.maxBidAmount <= :maxBudget
    """)
    fun findByCargoTypeAndBudget(
        @Param("cargoType") cargoType: String,
        @Param("maxBudget") maxBudget: Double,
        pageable: Pageable
    ): Page<FreightOrderEntity>
}

interface BidRepository : JpaRepository<BidEntity, UUID> {
    
    fun findByFreightOrderId(orderId: UUID): List<BidEntity>
    
    fun findByCarrierId(carrierId: UUID, pageable: Pageable): Page<BidEntity>
    
    fun findByFreightOrderIdAndCarrierId(orderId: UUID, carrierId: UUID): BidEntity?
    
    fun findByStatus(status: BidStatus, pageable: Pageable): Page<BidEntity>
    
    @Query("""
        SELECT COUNT(b) FROM BidEntity b 
        WHERE b.carrierId = :carrierId 
        AND b.status = 'ACCEPTED'
    """)
    fun countAcceptedBidsByCarrier(@Param("carrierId") carrierId: UUID): Long
}

interface UserProfileRepository : JpaRepository<UserProfileEntity, UUID> {
    
    fun findByCompanyNameContainingIgnoreCase(companyName: String, pageable: Pageable): Page<UserProfileEntity>
    
    @Query("""
        SELECT AVG(up.rating) FROM UserProfileEntity up 
        WHERE up.totalOrders > 0
    """)
    fun getAverageRating(): Double?
}