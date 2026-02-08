package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "vehicle_maintenance")
data class VehicleMaintenance(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var vehicleId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var maintenanceType: String = "",

    var description: String = "",

    var startDate: LocalDateTime? = null,

    var endDate: LocalDateTime? = null,

    var cost: Double = 0.0,

    @ElementCollection
    var partsUsed: MutableList<String> = mutableListOf(),

    var serviceCenter: String = "",

    var technician: String = "",

    @Column(nullable = false)
    var status: String = "scheduled",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)