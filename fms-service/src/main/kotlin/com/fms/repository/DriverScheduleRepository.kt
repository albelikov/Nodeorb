package com.fms.repository

import com.fms.model.DriverSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DriverScheduleRepository : JpaRepository<DriverSchedule, UUID> {
    fun findByDriverId(driverId: UUID): DriverSchedule?
}