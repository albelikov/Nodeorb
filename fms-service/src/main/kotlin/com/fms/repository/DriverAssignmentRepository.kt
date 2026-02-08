package com.fms.repository

import com.fms.model.DriverAssignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DriverAssignmentRepository : JpaRepository<DriverAssignment, UUID> {
    fun findByDriverId(driverId: UUID): List<DriverAssignment>
    fun findByVehicleId(vehicleId: UUID): List<DriverAssignment>
    fun findByRouteId(routeId: UUID): List<DriverAssignment>
    fun findByStatus(status: String): List<DriverAssignment>
}