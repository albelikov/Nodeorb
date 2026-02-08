package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "driver_assignments")
data class DriverAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var driverId: UUID = UUID.randomUUID(),

    var routeId: UUID? = null,

    var vehicleId: UUID? = null,

    var startTime: LocalDateTime? = null,

    var endTime: LocalDateTime? = null,

    @Column(nullable = false)
    var status: String = "pending",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)