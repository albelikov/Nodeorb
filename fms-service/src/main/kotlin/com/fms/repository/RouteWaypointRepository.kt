package com.fms.repository

import com.fms.model.RouteWaypoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RouteWaypointRepository : JpaRepository<RouteWaypoint, UUID> {
    fun findByRouteId(routeId: UUID): List<RouteWaypoint>
}