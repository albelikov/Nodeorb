package com.fms.repository

import com.fms.model.FuelRefueling
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FuelRefuelingRepository : JpaRepository<FuelRefueling, UUID> {
    fun findByVehicleId(vehicleId: UUID): List<FuelRefueling>
    fun findByDriverId(driverId: UUID): List<FuelRefueling>
}