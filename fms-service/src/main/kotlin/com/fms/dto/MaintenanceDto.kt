package com.fms.dto

import java.time.LocalDateTime
import java.util.*

data class VehicleMaintenanceRequestDto(
    val id: UUID? = null,
    val vehicleId: UUID,
    val requestType: String,
    val description: String = "",
    val priority: String = "normal",
    val status: String = "pending",
    val requestedBy: UUID? = null,
    val scheduledDate: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class VehicleMaintenanceDto(
    val id: UUID? = null,
    val vehicleId: UUID,
    val maintenanceType: String,
    val description: String = "",
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val cost: Double = 0.0,
    val partsUsed: List<String> = emptyList(),
    val serviceCenter: String = "",
    val technician: String = "",
    val status: String = "scheduled",
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class MaintenanceRecordDto(
    val id: UUID? = null,
    val vehicleId: UUID,
    val maintenanceType: String,
    val description: String = "",
    val cost: Double = 0.0,
    val partsUsed: List<String> = emptyList(),
    val serviceCenter: String = "",
    val technician: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime? = null
)