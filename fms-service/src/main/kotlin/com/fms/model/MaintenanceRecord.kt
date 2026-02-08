package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "maintenance_records")
data class MaintenanceRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var vehicleId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var maintenanceType: String = "",

    var description: String = "",

    var cost: Double = 0.0,

    @ElementCollection
    var partsUsed: MutableList<String> = mutableListOf(),

    var serviceCenter: String = "",

    var technician: String = "",

    @Column(nullable = false)
    var date: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)