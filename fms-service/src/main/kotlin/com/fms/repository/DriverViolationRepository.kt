package com.fms.repository

import com.fms.model.DriverViolation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DriverViolationRepository : JpaRepository<DriverViolation, UUID> {
    fun findByDriverId(driverId: UUID): List<DriverViolation>
    fun findByViolationType(violationType: String): List<DriverViolation>
    fun findBySeverity(severity: String): List<DriverViolation>
}