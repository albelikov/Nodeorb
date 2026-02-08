package com.fms.repository

import com.fms.model.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VehicleRepository : JpaRepository<Vehicle, UUID> {
    fun findByVehicleNumber(vehicleNumber: String): Vehicle?
    fun findByVin(vin: String): Vehicle?
    fun findByRegistrationNumber(registrationNumber: String): Vehicle?
    fun findByStatus(status: String): List<Vehicle>
    fun findByVehicleType(vehicleType: String): List<Vehicle>
}