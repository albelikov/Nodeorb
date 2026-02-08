package com.fms.repository

import com.fms.model.Route
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RouteRepository : JpaRepository<Route, UUID> {
    fun findByRouteNumber(routeNumber: String): Route?
    fun findByOptimizationStatus(optimizationStatus: String): List<Route>
}