package com.tms.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "location_history")
data class LocationHistory(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var shipmentId: Long,

    @Column(nullable = false)
    var vehicleId: Long,

    @Column(nullable = false)
    var latitude: Double,

    @Column(nullable = false)
    var longitude: Double,

    var altitude: Double?,

    var speed: Double?,

    var heading: Double?,

    var accuracy: Double?,

    @Column(nullable = false)
    var timestamp: Instant,

    var source: String?,

    // PostGIS
    @Column(columnDefinition = "GEOGRAPHY(POINT, 4326)")
    var location: String?
)