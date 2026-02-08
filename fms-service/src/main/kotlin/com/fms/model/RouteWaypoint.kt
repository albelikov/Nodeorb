package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "route_waypoints")
data class RouteWaypoint(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    var route: Route? = null,

    @Column(nullable = false)
    var sequenceNumber: Int = 0,

    @Column(nullable = false)
    var location: String = "",

    var latitude: Double = 0.0,

    var longitude: Double = 0.0,

    var arrivalTime: LocalDateTime? = null,

    var departureTime: LocalDateTime? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)