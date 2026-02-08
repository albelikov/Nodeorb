package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "driver_status")
data class DriverStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var driverId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var status: String = "available",

    var latitude: Double = 0.0,

    var longitude: Double = 0.0,

    var speed: Double = 0.0,

    var workingHoursToday: Double = 0.0,

    var restTimeRemaining: Double = 0.0,

    @Column(nullable = false)
    var lastUpdated: LocalDateTime = LocalDateTime.now()
)