package com.fms.repository

import com.fms.model.MaintenanceRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MaintenanceRecordRepository : JpaRepository<MaintenanceRecord, UUID> {
    fun findByVehicleId(vehicleId: UUID): List<MaintenanceRecord>
    fun findByMaintenanceType(maintenanceType: String): List<MaintenanceRecord>
}