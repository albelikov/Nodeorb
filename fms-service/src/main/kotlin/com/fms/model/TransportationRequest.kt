package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "transportation_requests")
data class TransportationRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var customerId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var requestType: String = "",

    var cargoType: String = "",

    var cargoWeight: Double = 0.0,

    var cargoVolume: Double = 0.0,

    @Column(nullable = false)
    var pickupLocation: String = "",

    @Column(nullable = false)
    var deliveryLocation: String = "",

    var pickupTime: LocalDateTime? = null,

    var deliveryTime: LocalDateTime? = null,

    var priority: String = "normal",

    @Column(nullable = false)
    var status: String = "pending",

    var price: Double = 0.0,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)