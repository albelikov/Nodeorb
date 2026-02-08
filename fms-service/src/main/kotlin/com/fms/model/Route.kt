package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "routes")
data class Route(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var routeNumber: String = "",

    @Column(nullable = false)
    var startLocation: String = "",

    @Column(nullable = false)
    var endLocation: String = "",

    @OneToMany(mappedBy = "route", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var waypoints: MutableList<RouteWaypoint> = mutableListOf(),

    var totalDistance: Double = 0.0,

    var estimatedTime: Double = 0.0,

    var optimizationStatus: String = "pending",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)