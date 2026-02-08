package com.fms.repository

import com.fms.model.FuelConsumption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FuelConsumptionRepository : JpaRepository<FuelConsumption, UUID> {
    fun findByVehicleId(vehicleId: UUID): List<FuelConsumption>
    fun findByDriverId(driverId: UUID): List<FuelConsumption>
    fun findByTripId(tripId: UUID): List<FuelConsumption>
}