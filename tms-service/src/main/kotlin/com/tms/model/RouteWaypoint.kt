package com.tms.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "route_waypoints")
data class RouteWaypoint(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    var route: Route,

    @Column(nullable = false)
    var sequenceOrder: Int,

    var address: String?,

    @Column(nullable = false)
    var latitude: Double,

    @Column(nullable = false)
    var longitude: Double,

    var arrivalTime: Instant?,

    var departureTime: Instant?,

    var stopDuration: Int?
) {
    @PrePersist
    @PreUpdate
    fun ensureUniqueSequence() {
        if (sequenceOrder < 0) {
            throw IllegalArgumentException("Sequence order must be non-negative")
        }
    }
}