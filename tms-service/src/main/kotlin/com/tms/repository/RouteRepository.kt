package com.tms.repository

import com.tms.model.Route
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RouteRepository : JpaRepository<Route, Long> {

    fun findByRouteNumber(routeNumber: String): Route?
}