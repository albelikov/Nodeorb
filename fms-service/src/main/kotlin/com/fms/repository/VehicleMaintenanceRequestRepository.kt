package com.fms.repository

import com.fms.model.VehicleMaintenanceRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VehicleMaintenanceRequestRepository : JpaRepository<VehicleMaintenanceRequest, UUID> {
    fun findByVehicleId(vehicleId: UUID): List<VehicleMaintenanceRequest>
    fun findByStatus(status: String): List<VehicleMaintenanceRequest>
    fun findByPriority(priority: String): List<VehicleMaintenanceRequest>
}