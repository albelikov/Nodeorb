package com.fms.repository

import com.fms.model.VehicleStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VehicleStatusRepository : JpaRepository<VehicleStatus, UUID> {
    fun findByVehicleId(vehicleId: UUID): VehicleStatus?
}