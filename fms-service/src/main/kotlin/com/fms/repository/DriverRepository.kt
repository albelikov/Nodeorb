package com.fms.repository

import com.fms.model.Driver
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DriverRepository : JpaRepository<Driver, UUID> {
    fun findByDriverLicenseNumber(driverLicenseNumber: String): Driver?
    fun findByPhoneNumber(phoneNumber: String): Driver?
    fun findByEmail(email: String): Driver?
    fun findByStatus(status: String): List<Driver>
    fun findByAssignedVehicleId(assignedVehicleId: UUID): Driver?
}