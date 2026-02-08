package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "vehicle_maintenance_requests")
data class VehicleMaintenanceRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var vehicleId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var requestType: String = "",

    var description: String = "",

    var priority: String = "normal",

    @Column(nullable = false)
    var status: String = "pending",

    var requestedBy: UUID? = null,

    var scheduledDate: LocalDateTime? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)