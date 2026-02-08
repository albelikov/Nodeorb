package com.fms.dto

import java.time.LocalDateTime
import java.util.*

data class VehicleDto(
    val id: UUID? = null,
    val vehicleNumber: String,
    val vehicleType: String,
    val make: String,
    val model: String,
    val year: Int,
    val vin: String,
    val registrationNumber: String,
    val currentMileage: Int = 0,
    val fuelType: String,
    val fuelLevel: Int = 0,
    val capacity: Double = 0.0,
    val volume: Double = 0.0,
    val bodyType: String = "",
    val status: String = "available",
    val documents: List<VehicleDocumentDto> = emptyList(),
    val photos: List<String> = emptyList(),
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class VehicleDocumentDto(
    val id: UUID? = null,
    val documentType: String,
    val documentNumber: String,
    val issueDate: LocalDateTime,
    val expiryDate: LocalDateTime? = null,
    val fileUrl: String = ""
)