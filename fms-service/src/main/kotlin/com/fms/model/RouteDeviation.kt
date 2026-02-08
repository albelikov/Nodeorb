package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "route_deviations")
data class RouteDeviation(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var vehicleId: UUID = UUID.randomUUID(),

    var driverId: UUID? = null,

    var routeId: UUID? = null,

    var deviationDistance: Double = 0.0,

    var deviationTime: Int = 0,

    var currentLatitude: Double = 0.0,

    var currentLongitude: Double = 0.0,

    var expectedLatitude: Double = 0.0,

    var expectedLongitude: Double = 0.0,

    @Column(nullable = false)
    var status: String = "active",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)