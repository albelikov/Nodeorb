package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "fuel_refueling")
data class FuelRefueling(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var vehicleId: UUID = UUID.randomUUID(),

    var driverId: UUID? = null,

    @Column(nullable = false)
    var fuelType: String = "",

    @Column(nullable = false)
    var volume: Double = 0.0,

    var cost: Double = 0.0,

    var location: String = "",

    @Column(nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)