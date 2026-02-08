package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "driver_schedules")
data class DriverSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var driverId: UUID = UUID.randomUUID(),

    @ElementCollection
    @CollectionTable(name = "driver_week_schedule", joinColumns = [JoinColumn(name = "schedule_id")])
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "schedule")
    var weekSchedule: MutableMap<String, String> = mutableMapOf(),

    var workingHours: Double = 40.0,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)