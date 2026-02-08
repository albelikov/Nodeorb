package com.autonomous.planning

import org.springframework.stereotype.Service

@Service
class RouteOptimizer {

    fun optimizeRoute(route: List<Waypoint>, trafficData: List<TrafficPoint>): List<Waypoint> {
        // Simple optimization - avoid high traffic areas
        return route.filter { waypoint ->
            !isHighTraffic(waypoint, trafficData)
        }
    }

    private fun isHighTraffic(waypoint: Waypoint, trafficData: List<TrafficPoint>): Boolean {
        return trafficData.any { traffic ->
            val distance = calculateDistance(waypoint.latitude, waypoint.longitude, traffic.latitude, traffic.longitude)
            distance < 1000 && traffic.trafficLevel == TrafficLevel.HIGH
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }
}

data class TrafficPoint(
    val latitude: Double,
    val longitude: Double,
    val trafficLevel: TrafficLevel
)

enum class TrafficLevel {
    LOW, MEDIUM, HIGH
}