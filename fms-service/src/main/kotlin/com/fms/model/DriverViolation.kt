package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "driver_violations")
data class DriverViolation(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var driverId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var violationType: String = "",

    @Column(nullable = false)
    var violationDate: LocalDateTime = LocalDateTime.now(),

    var location: String = "",

    var severity: String = "minor",

    var penalty: Double = 0.0,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)