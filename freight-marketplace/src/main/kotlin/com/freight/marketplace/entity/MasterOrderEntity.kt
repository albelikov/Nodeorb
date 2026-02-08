package com.freight.marketplace.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.locationtech.jts.geom.Point
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Мастер-заказ - основной заказ, который может быть разделен на несколько частичных заказов (LTL)
 * Поддерживает дробление объема для оптимизации загрузки транспортных средств
 */
@Entity
@Table(name = "master_orders")
data class MasterOrderEntity(
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
    var totalWeight: BigDecimal,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var totalVolume: BigDecimal,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var remainingWeight: BigDecimal,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var remainingVolume: BigDecimal,
    
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
    var status: MasterOrderStatus = MasterOrderStatus.OPEN,
    
    @Column(nullable = false)
    var isLtlEnabled: Boolean = true,
    
    @Column(nullable = false)
    var minLoadPercentage: Double = 0.8,
    
    @OneToMany(mappedBy = "masterOrder", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val partialOrders: MutableList<PartialOrderEntity> = mutableListOf(),
    
    @OneToMany(mappedBy = "masterOrder", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val bids: MutableList<BidEntity> = mutableListOf(),
    
    @CreationTimestamp
    @Column(nullable = false)
    val createdAt: LocalDateTime? = null,
    
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)

/**
 * Частичный заказ - часть мастер-заказа, создается при дроблении объема (LTL)
 * Каждый частичный заказ может быть выставлен на аукцион отдельно
 */
@Entity
@Table(name = "partial_orders")
data class PartialOrderEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_order_id", nullable = false)
    val masterOrder: MasterOrderEntity,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var weight: BigDecimal,
    
    @Column(nullable = false, precision = 15, scale = 2)
    var volume: BigDecimal,
    
    @Column(nullable = false)
    var percentage: Double,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PartialOrderStatus = PartialOrderStatus.AVAILABLE,
    
    @Column
    var assignedCarrierId: UUID? = null,
    
    @Column
    var assignedBidId: UUID? = null,
    
    @CreationTimestamp
    @Column(nullable = false)
    val createdAt: LocalDateTime? = null,
    
    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)

enum class MasterOrderStatus {
    OPEN,           // Заказ открыт для ставок
    PARTIALLY_FILLED, // Заказ частично заполнен
    FILLED,         // Заказ полностью заполнен
    IN_PROGRESS,    // Заказ в процессе выполнения
    COMPLETED,      // Заказ завершен
    CANCELLED       // Заказ отменен
}

enum class PartialOrderStatus {
    AVAILABLE,      // Частичный заказ доступен для ставок
    BIDDING,        // Идет аукцион по частичному заказу
    AWARDED,        // Частичный заказ выигран
    IN_PROGRESS,    // Частичный заказ в процессе
    COMPLETED,      // Частичный заказ завершен
    CANCELLED       // Частичный заказ отменен
}
