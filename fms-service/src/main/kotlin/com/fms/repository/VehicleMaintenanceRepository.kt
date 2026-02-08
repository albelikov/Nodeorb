package com.fms.repository

import com.fms.model.VehicleMaintenance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VehicleMaintenanceRepository : JpaRepository<VehicleMaintenance, UUID> {
    fun findByVehicleId(vehicleId: UUID): List<VehicleMaintenance>
    fun findByStatus(status: String): List<VehicleMaintenance>
    fun findByMaintenanceType(maintenanceType: String): List<VehicleMaintenance>
}