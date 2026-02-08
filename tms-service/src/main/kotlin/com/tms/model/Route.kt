package com.tms.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "routes")
data class Route(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var routeNumber: String,

    // Origin & Destination
    @Column(nullable = false)
    var originAddress: String,

    @Column(nullable = false)
    var originLatitude: Double,

    @Column(nullable = false)
    var originLongitude: Double,

    @Column(nullable = false)
    var destinationAddress: String,

    @Column(nullable = false)
    var destinationLatitude: Double,

    @Column(nullable = false)
    var destinationLongitude: Double,

    // Route details
    var totalDistance: Double?,

    var totalDuration: Int?,

    var estimatedCost: Double?,

    var fuelConsumption: Double?,

    var co2Emissions: Double?,

    // Geometry (PostGIS)
    @Column(columnDefinition = "GEOMETRY(LineString, 4326)")
    var geometry: String?,

    // Metadata
    var vehicleType: String?,

    var optimizationType: String?,

    var status: String?,

    var calculatedAt: Instant = Instant.now(),

    var createdBy: Long?
)