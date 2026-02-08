package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "fuel_consumption")
data class FuelConsumption(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var vehicleId: UUID = UUID.randomUUID(),

    var driverId: UUID? = null,

    @Column(nullable = false)
    var fuelType: String = "",

    var consumptionRate: Double = 0.0,

    var actualConsumption: Double = 0.0,

    var distance: Double = 0.0,

    var tripId: UUID? = null,

    @Column(nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)