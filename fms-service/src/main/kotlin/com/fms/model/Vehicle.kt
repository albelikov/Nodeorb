package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "vehicles")
data class Vehicle(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var vehicleNumber: String = "",

    @Column(nullable = false)
    var vehicleType: String = "",

    @Column(nullable = false)
    var make: String = "",

    @Column(nullable = false)
    var model: String = "",

    @Column(nullable = false)
    var year: Int = 0,

    @Column(nullable = false, unique = true)
    var vin: String = "",

    @Column(nullable = false, unique = true)
    var registrationNumber: String = "",

    var currentMileage: Int = 0,

    @Column(nullable = false)
    var fuelType: String = "",

    var fuelLevel: Int = 0,

    var capacity: Double = 0.0,

    var volume: Double = 0.0,

    var bodyType: String = "",

    @Column(nullable = false)
    var status: String = "available",

    @OneToMany(mappedBy = "vehicle", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var documents: MutableList<VehicleDocument> = mutableListOf(),

    @ElementCollection
    var photos: MutableList<String> = mutableListOf(),

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)