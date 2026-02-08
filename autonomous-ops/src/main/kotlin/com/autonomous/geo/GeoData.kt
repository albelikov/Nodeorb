package com.autonomous.geo

import org.springframework.stereotype.Service

@Service
class GeoData {

    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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

    fun getBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val y = Math.sin(Math.toRadians(lon2 - lon1)) * Math.cos(Math.toRadians(lat2))
        val x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.cos(Math.toRadians(lon2 - lon1))

        var bearing = Math.toDegrees(Math.atan2(y, x))
        bearing = (bearing + 360) % 360

        return bearing
    }

    fun getMidpoint(lat1: Double, lon1: Double, lat2: Double, lon2: Double): GeoPoint {
        val phi1 = Math.toRadians(lat1)
        val lambda1 = Math.toRadians(lon1)
        val phi2 = Math.toRadians(lat2)
        val lambda2 = Math.toRadians(lon2)

        val bx = Math.cos(phi2) * Math.cos(lambda2 - lambda1)
        val by = Math.cos(phi2) * Math.sin(lambda2 - lambda1)

        val phi3 = Math.atan2(
            Math.sin(phi1) + Math.sin(phi2),
            Math.sqrt((Math.cos(phi1) + bx) * (Math.cos(phi1) + bx) + by * by)
        )

        val lambda3 = lambda1 + Math.atan2(by, Math.cos(phi1) + bx)

        return GeoPoint(Math.toDegrees(phi3), Math.toDegrees(lambda3))
    }

    fun isPointInRadius(point: GeoPoint, center: GeoPoint, radius: Double): Boolean {
        val distance = getDistance(point.latitude, point.longitude, center.latitude, center.longitude)
        return distance <= radius
    }
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)