package com.fms.repository

import com.fms.model.RouteDeviation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RouteDeviationRepository : JpaRepository<RouteDeviation, UUID> {
    fun findByVehicleId(vehicleId: UUID): List<RouteDeviation>
    fun findByDriverId(driverId: UUID): List<RouteDeviation>
    fun findByRouteId(routeId: UUID): List<RouteDeviation>
    fun findByStatus(status: String): List<RouteDeviation>
}