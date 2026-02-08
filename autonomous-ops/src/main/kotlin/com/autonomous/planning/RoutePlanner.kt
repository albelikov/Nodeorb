package com.autonomous.planning

import org.springframework.stereotype.Service

@Service
class RoutePlanner {

    fun planRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double): List<Waypoint> {
        // Simple route planning - straight line
        val distance = calculateDistance(startLat, startLon, endLat, endLon)
        val numWaypoints = (distance / 1000).toInt() + 1 // 1km per waypoint

        val waypoints = mutableListOf<Waypoint>()
        for (i in 0..numWaypoints) {
            val fraction = i.toDouble() / numWaypoints
            val lat = startLat + fraction * (endLat - startLat)
            val lon = startLon + fraction * (endLon - startLon)
            waypoints.add(Waypoint(i.toString(), lat, lon))
        }

        return waypoints
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Earth radius in meters
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

data class Waypoint(
    val id: String,
    val latitude: Double,
    val longitude: Double
)