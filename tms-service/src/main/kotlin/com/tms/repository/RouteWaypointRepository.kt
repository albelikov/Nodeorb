package com.tms.repository

import com.tms.model.RouteWaypoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RouteWaypointRepository : JpaRepository<RouteWaypoint, Long> {

    fun findByRouteId(routeId: Long): List<RouteWaypoint>
}