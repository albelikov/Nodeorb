package com.fms.repository

import com.fms.model.LocationUpdate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LocationUpdateRepository : JpaRepository<LocationUpdate, UUID> {
    fun findByVehicleId(vehicleId: UUID): List<LocationUpdate>
    fun findByDriverId(driverId: UUID): List<LocationUpdate>
}