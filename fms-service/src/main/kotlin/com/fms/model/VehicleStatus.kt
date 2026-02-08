package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "vehicle_status")
data class VehicleStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var vehicleId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var status: String = "available",

    var latitude: Double = 0.0,

    var longitude: Double = 0.0,

    var speed: Double = 0.0,

    var engineTemperature: Double = 0.0,

    var fuelLevel: Int = 0,

    var batteryVoltage: Double = 0.0,

    var engineRpm: Int = 0,

    var odometer: Int = 0,

    @Column(nullable = false)
    var lastUpdated: LocalDateTime = LocalDateTime.now()
)