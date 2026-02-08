package com.tms.model

import com.nodeorb.shared.cargo.CargoDetails
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "shipments")
data class Shipment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var shipmentNumber: String,

    // Order reference
    var orderId: Long?,

    // Route
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    var route: Route?,

    // Carrier & Vehicle
    var carrierId: Long?,
    var vehicleId: Long?,
    var driverId: Long?,

    // Pickup
    var pickupAddress: String,
    var pickupLatitude: Double,
    var pickupLongitude: Double,
    var pickupDateTimeStart: Instant,
    var pickupDateTimeEnd: Instant,
    var actualPickupDateTime: Instant?,

    // Delivery
    var deliveryAddress: String,
    var deliveryLatitude: Double,
    var deliveryLongitude: Double,
    var deliveryDateTimeStart: Instant,
    var deliveryDateTimeEnd: Instant,
    var actualDeliveryDateTime: Instant?,

    // Cargo
    @Embedded
    var cargo: CargoDetails,

    // Costs
    var baseRate: BigDecimal?,
    var fuelSurcharge: BigDecimal?,
    var accessorialCharges: BigDecimal?,
    var totalCost: BigDecimal?,

    // Status
    @Enumerated(EnumType.STRING)
    var status: ShipmentStatus,

    // Tracking
    var currentLatitude: Double?,
    var currentLongitude: Double?,
    var lastLocationUpdate: Instant?,
    var estimatedArrival: Instant?,

    // Documents
    var hasProofOfDelivery: Boolean = false,
    var hasBillOfLading: Boolean = false,
    var hasCMR: Boolean = false,

    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)

