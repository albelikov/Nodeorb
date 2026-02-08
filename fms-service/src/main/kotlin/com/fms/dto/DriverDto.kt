package com.fms.dto

import java.time.LocalDateTime
import java.util.*

data class DriverDto(
    val id: UUID? = null,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val driverLicenseNumber: String,
    val driverLicenseCategory: String,
    val driverLicenseExpiryDate: LocalDateTime,
    val phoneNumber: String,
    val email: String = "",
    val medicalCertificateExpiry: LocalDateTime,
    val experience: Int = 0,
    val rating: Double = 5.0,
    val status: String = "available",
    val assignedVehicleId: UUID? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class DriverStatusDto(
    val id: UUID? = null,
    val driverId: UUID,
    val status: String = "available",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Double = 0.0,
    val workingHoursToday: Double = 0.0,
    val restTimeRemaining: Double = 0.0,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)