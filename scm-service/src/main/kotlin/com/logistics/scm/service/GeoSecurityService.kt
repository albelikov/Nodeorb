package com.logistics.scm.service

import com.logistics.scm.service.ClickHouseService
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.*

class GeoSecurityService(
    private val clickHouseService: ClickHouseService
) {
    
    // Constants for geofencing and anti-spoofing
    private val MAX_DISTANCE_METERS = 500.0
    private val MAX_SPEED_KMH = 1200.0 // Impossible speed threshold (1000 km/h)
    private val EARTH_RADIUS_METERS = 6371000.0
    
    // Known VPN/Proxy IP ranges (simplified list)
    private val knownVpnRanges = setOf(
        "10.0.0.0/8",     // Private networks
        "172.16.0.0/12",  // Private networks
        "192.168.0.0/16", // Private networks
        "100.64.0.0/10"   // CGNAT (often used by VPNs)
    )
    
    // Known VPN/Proxy User-Agents
    private val knownVpnUserAgents = setOf(
        "VPN",
        "Proxy",
        "Tor",
        "Anonymizer",
        "HideMyAss",
        "NordVPN",
        "ExpressVPN",
        "CyberGhost"
    )
    
    data class GeoVerificationResult(
        val geoVerified: Boolean,
        val distanceMeters: Double,
        val spoofingDetected: Boolean,
        val vpnDetected: Boolean,
        val riskLevel: RiskLevel,
        val verificationTimestamp: String
    )
    
    enum class RiskLevel {
        LOW, MEDIUM, HIGH, SPOOFING_DETECTED, VPN_DETECTED
    }
    
    data class LocationEvent(
        val orderId: String,
        val latitude: Double,
        val longitude: Double,
        val timestamp: String,
        val ipAddress: String,
        val userAgent: String
    )
    
    fun verifyLocation(orderId: String, lat: Double, lon: Double): GeoVerificationResult {
        try {
            // Get expected location from TMS (simulated here)
            val expectedLocation = getExpectedLocation(orderId)
            
            // Calculate distance
            val distance = calculateDistance(lat, lon, expectedLocation.latitude, expectedLocation.longitude)
            
            // Check geofencing
            val geoVerified = distance <= MAX_DISTANCE_METERS
            
            // Check for spoofing
            val spoofingDetected = detectSpoofing(orderId, lat, lon)
            
            // Check for VPN/Proxy
            val vpnDetected = detectVpn(orderId)
            
            // Determine risk level
            val riskLevel = determineRiskLevel(geoVerified, spoofingDetected, vpnDetected)
            
            return GeoVerificationResult(
                geoVerified = geoVerified,
                distanceMeters = distance,
                spoofingDetected = spoofingDetected,
                vpnDetected = vpnDetected,
                riskLevel = riskLevel,
                verificationTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to verify location for order $orderId: ${e.message}", e)
        }
    }
    
    private fun getExpectedLocation(orderId: String): Location {
        // In a real implementation, this would call TMS service
        // For now, we'll simulate based on order ID or get from ClickHouse
        return try {
            val location = getLocationFromClickHouse(orderId)
            if (location != null) location
            else getDefaultLocation(orderId)
        } catch (e: Exception) {
            getDefaultLocation(orderId)
        }
    }
    
    private fun getLocationFromClickHouse(orderId: String): Location? {
        val selectSQL = """
            SELECT input_data
            FROM scm_audit_log
            WHERE order_id = ? AND event_type = 'VALIDATION'
            ORDER BY event_time DESC
            LIMIT 1
        """.trimIndent()
        
        try {
            val connection = clickHouseService.getConnection()
            connection.prepareStatement(selectSQL).use { statement ->
                statement.setString(1, orderId)
                
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val locationData = resultSet.getString("input_data")
                        return parseLocationFromData(locationData)
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail
        }
        
        return null
    }
    
    private fun parseLocationFromData(data: String): Location? {
        // Parse location from stored data (simplified format)
        // Format: "lat,lon" or JSON
        return try {
            if (data.contains(",")) {
                val parts = data.split(",")
                if (parts.size >= 2) {
                    Location(
                        latitude = parts[0].toDoubleOrNull() ?: 0.0,
                        longitude = parts[1].toDoubleOrNull() ?: 0.0
                    )
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getDefaultLocation(orderId: String): Location {
        // Generate default location based on order ID hash
        val hash = orderId.hashCode()
        val lat = 50.4501 + (hash % 1000) / 10000.0
        val lon = 30.5234 + (hash % 1000) / 10000.0
        return Location(latitude = lat, longitude = lon)
    }
    
    private fun detectSpoofing(orderId: String, currentLat: Double, currentLon: Double): Boolean {
        try {
            val previousEvent = getPreviousLocationEvent(orderId)
            if (previousEvent == null) return false
            
            val timeDiff = calculateTimeDifference(previousEvent.timestamp)
            if (timeDiff <= 0) return false
            
            val distance = calculateDistance(
                currentLat, currentLon,
                previousEvent.latitude, previousEvent.longitude
            )
            
            // Calculate speed in km/h
            val distanceKm = distance / 1000.0
            val timeHours = timeDiff / 3600.0
            val speedKmh = distanceKm / timeHours
            
            return speedKmh > MAX_SPEED_KMH
            
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun detectVpn(orderId: String): Boolean {
        try {
            val currentEvent = getCurrentLocationEvent(orderId)
            if (currentEvent == null) return false
            
            // Check User-Agent
            val userAgent = currentEvent.userAgent.lowercase()
            val isKnownVpn = knownVpnUserAgents.any { vpnAgent ->
                userAgent.contains(vpnAgent.lowercase())
            }
            
            if (isKnownVpn) return true
            
            // Check IP address
            val ipAddress = currentEvent.ipAddress
            return isPrivateOrVpnIp(ipAddress)
            
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun getPreviousLocationEvent(orderId: String): LocationEvent? {
        val selectSQL = """
            SELECT order_id, input_data, event_time
            FROM scm_audit_log
            WHERE order_id = ? AND event_type = 'VALIDATION'
            ORDER BY event_time DESC
            LIMIT 2
        """.trimIndent()
        
        try {
            val connection = clickHouseService.getConnection()
            connection.prepareStatement(selectSQL).use { statement ->
                statement.setString(1, orderId)
                
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        resultSet.next() // Skip the most recent, get the previous one
                        if (resultSet.isAfterLast) return null
                        
                        val lat = resultSet.getString("input_data")?.split(",")?.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                        val lon = resultSet.getString("input_data")?.split(",")?.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                        val timestamp = resultSet.getString("event_time")
                        
                        return LocationEvent(
                            orderId = orderId,
                            latitude = lat,
                            longitude = lon,
                            timestamp = timestamp,
                            ipAddress = "unknown", // Would be stored separately
                            userAgent = "unknown"  // Would be stored separately
                        )
                    }
                }
            }
        } catch (e: Exception) {
            return null
        }
        
        return null
    }
    
    private fun getCurrentLocationEvent(orderId: String): LocationEvent? {
        val selectSQL = """
            SELECT order_id, input_data, event_time
            FROM scm_audit_log
            WHERE order_id = ? AND event_type = 'VALIDATION'
            ORDER BY event_time DESC
            LIMIT 1
        """.trimIndent()
        
        try {
            val connection = clickHouseService.getConnection()
            connection.prepareStatement(selectSQL).use { statement ->
                statement.setString(1, orderId)
                
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val lat = resultSet.getString("input_data")?.split(",")?.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                        val lon = resultSet.getString("input_data")?.split(",")?.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                        val timestamp = resultSet.getString("event_time")
                        
                        return LocationEvent(
                            orderId = orderId,
                            latitude = lat,
                            longitude = lon,
                            timestamp = timestamp,
                            ipAddress = "unknown", // Would be stored separately
                            userAgent = "unknown"  // Would be stored separately
                        )
                    }
                }
            }
        } catch (e: Exception) {
            return null
        }
        
        return null
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }
    
    private fun calculateTimeDifference(timestamp: String): Double {
        try {
            val current = LocalDateTime.now()
            val previous = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val duration = java.time.Duration.between(previous, current)
            return duration.seconds.toDouble()
        } catch (e: Exception) {
            return 0.0
        }
    }
    
    private fun isPrivateOrVpnIp(ipAddress: String): Boolean {
        try {
            val ipParts = ipAddress.split(".").map { it.toInt() }
            if (ipParts.size != 4) return false
            
            // Check private IP ranges
            when (ipParts[0]) {
                10 -> return true // 10.0.0.0/8
                172 -> if (ipParts[1] in 16..31) return true // 172.16.0.0/12
                192 -> if (ipParts[1] == 168) return true // 192.168.0.0/16
                100 -> if (ipParts[1] in 64..127) return true // 100.64.0.0/10
            }
            
            // Additional checks for common VPN ranges
            // This is a simplified check - in production, you'd use a comprehensive database
            
        } catch (e: Exception) {
            return false
        }
        
        return false
    }
    
    private fun determineRiskLevel(geoVerified: Boolean, spoofingDetected: Boolean, vpnDetected: Boolean): RiskLevel {
        return when {
            spoofingDetected -> RiskLevel.SPOOFING_DETECTED
            vpnDetected -> RiskLevel.VPN_DETECTED
            !geoVerified -> RiskLevel.HIGH
            else -> RiskLevel.LOW
        }
    }
    
    data class Location(
        val latitude: Double,
        val longitude: Double
    )
}