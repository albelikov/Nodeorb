package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "location_updates")
data class LocationUpdate(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var vehicleId: UUID = UUID.randomUUID(),

    var driverId: UUID? = null,

    var latitude: Double = 0.0,

    var longitude: Double = 0.0,

    var speed: Double = 0.0,

    @Column(nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now(),

    var accuracy: Double = 0.0,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)