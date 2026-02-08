package com.fms.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "drivers")
data class Driver(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var firstName: String = "",

    @Column(nullable = false)
    var lastName: String = "",

    var middleName: String? = null,

    @Column(nullable = false, unique = true)
    var driverLicenseNumber: String = "",

    @Column(nullable = false)
    var driverLicenseCategory: String = "",

    @Column(nullable = false)
    var driverLicenseExpiryDate: LocalDateTime = LocalDateTime.now().plusYears(10),

    @Column(nullable = false)
    var phoneNumber: String = "",

    var email: String = "",

    var medicalCertificateExpiry: LocalDateTime = LocalDateTime.now().plusYears(1),

    var experience: Int = 0,

    var rating: Double = 5.0,

    @Column(nullable = false)
    var status: String = "available",

    var assignedVehicleId: UUID? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)