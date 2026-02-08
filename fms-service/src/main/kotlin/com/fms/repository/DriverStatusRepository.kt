package com.fms.repository

import com.fms.model.DriverStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DriverStatusRepository : JpaRepository<DriverStatus, UUID> {
    fun findByDriverId(driverId: UUID): DriverStatus?
}