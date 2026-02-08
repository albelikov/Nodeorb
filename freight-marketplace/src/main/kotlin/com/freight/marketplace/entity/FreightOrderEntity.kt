package com.freight.marketplace.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.locationtech.jts.geom.Point
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "freight_orders")
data class FreightOrderEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    
    @Column(nullable = false)
    val shipperId: UUID,
    
    @Column(nullable = false)
    var title: String,
    
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var cargoType: CargoType,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var weight: BigDecimal,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var volume: BigDecimal,
    
    @Column(nullable = false)
    var pickupLocation: Point,
    
    @Column(nullable = false)
    var deliveryLocation: Point,
    
    @Column(nullable = false)
    var pickupAddress: String,
    
    @Column(nullable = false)
    var deliveryAddress: String,
    
    @Column(nullable = false)
    var requiredDeliveryDate: LocalDateTime,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var maxBidAmount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.OPEN,
    
    @OneToMany(mappedBy = "freightOrder", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val bids: MutableList<BidEntity> = mutableListOf(),
    
    @CreationTimestamp
    @Column(nullable = false)
    val createdAt: LocalDateTime? = null,
    
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)

@Entity
@Table(name = "bids")
data class BidEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    
    @Column(nullable = false)
    val carrierId: UUID,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freight_order_id", nullable = false)
    val freightOrder: FreightOrderEntity? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_order_id", nullable = false)
    val masterOrder: MasterOrderEntity? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partial_order_id", nullable = false)
    val partialOrder: PartialOrderEntity? = null,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal,
    
    @Column(nullable = false)
    var proposedDeliveryDate: LocalDateTime,
    
    @Column(columnDefinition = "TEXT")
    var notes: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BidStatus = BidStatus.PENDING,
    
    @Column(precision = 5, scale = 2)
    var matchingScore: Double? = null,
    
    @Column(columnDefinition = "TEXT")
    var scoreBreakdown: String? = null,
    
    @CreationTimestamp
    @Column(nullable = false)
    val createdAt: LocalDateTime? = null,
    
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)

@Entity
@Table(name = "user_profiles")
data class UserProfileEntity(
    @Id
    val userId: UUID,
    
    @Column(nullable = false)
    var companyName: String,
    
    @Column(nullable = false)
    var rating: Double = 5.0,
    
    @Column(nullable = false)
    var totalOrders: Int = 0,
    
    @Column(nullable = false)
    var completedOrders: Int = 0,
    
    @CreationTimestamp
    @Column(nullable = false)
    val joinedAt: LocalDateTime? = null,
    
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)

enum class CargoType {
    GENERAL,
    PERISHABLE,
    DANGEROUS,
    REFRIGERATED,
    BULK,
    CONTAINER,
    OVERSIZED
}

enum class OrderStatus {
    OPEN,
    AUCTION_ACTIVE,
    AWARDED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class BidStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED
}