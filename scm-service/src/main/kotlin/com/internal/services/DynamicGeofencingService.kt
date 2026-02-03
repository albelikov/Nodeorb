package com.internal.services

import com.internal.engine.validation.AccessValidator
import com.internal.integrations.SecurityEventBus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Сервис динамической геозонной валидации
 * Проверяет атрибуты payload.geo_lat/lon и блокирует доступ к данным груза
 * в сервисах TMS/WMS при выходе за границы разрешенного маршрута
 */
@Service
class DynamicGeofencingService(
    private val accessValidator: AccessValidator,
    private val securityEventBus: SecurityEventBus
) {

    companion object {
        private const val ROUTE_CORRIDOR_WIDTH = 1000.0 // метров
        private const val MAX_DISTANCE_FROM_ROUTE = 500.0 // метров
    }

    /**
     * Проверка доступа к данным груза на основе географических координат
     */
    @Transactional
    fun validateCargoAccess(
        userId: String,
        orderId: String,
        payloadGeoLat: Double,
        payloadGeoLon: Double,
        cargoLatitude: Double,
        cargoLongitude: Double
    ): GeofencingValidationResult {
        // Проверяем, находится ли устройство в пределах коридора маршрута
        val isWithinRoute = isWithinAllowedRoute(
            payloadGeoLat, payloadGeoLon,
            cargoLatitude, cargoLongitude
        )

        // Проверяем расстояние от устройства до груза
        val distanceToCargo = calculateDistance(
            payloadGeoLat, payloadGeoLon,
            cargoLatitude, cargoLongitude
        )

        // Проверка на подмену GPS-координат
        val gpsSpoofingDetected = detectGpsSpoofing(
            userId,
            DeviceLocation(payloadGeoLat, payloadGeoLon, Instant.now()),
            null // В реальной системе здесь будет предыдущая локация
        )

        // Определяем, разрешен ли доступ
        val accessGranted = isWithinRoute && 
                           distanceToCargo <= MAX_DISTANCE_FROM_ROUTE &&
                           !gpsSpoofingDetected.isSpoofingDetected

        val validationResult = GeofencingValidationResult(
            userId = userId,
            orderId = orderId,
            isWithinRoute = isWithinRoute,
            distanceToCargo = distanceToCargo,
            accessGranted = accessGranted,
            timestamp = Instant.now(),
            payloadGeoLat = payloadGeoLat,
            payloadGeoLon = payloadGeoLon,
            cargoLatitude = cargoLatitude,
            cargoLongitude = cargoLongitude,
            gpsSpoofingDetected = gpsSpoofingDetected.isSpoofingDetected,
            spoofingConfidence = gpsSpoofingDetected.confidence,
            spoofingAnomalies = gpsSpoofingDetected.detectedAnomalies
        )

        // Отправляем событие о проверке доступа
        sendGeofencingEvent(validationResult)

        // Если доступ заблокирован, отправляем уведомление в TMS/WMS
        if (!accessGranted) {
            sendAccessBlockedEvent(validationResult)
        }

        return validationResult
    }

    /**
     * Проверка нахождения в пределах разрешенного коридора маршрута
     */
    private fun isWithinAllowedRoute(
        deviceLat: Double,
        deviceLon: Double,
        cargoLat: Double,
        cargoLon: Double
    ): Boolean {
        // В реальной системе здесь будет сложная логика проверки маршрута
        // с использованием GIS данных и реальных маршрутов
        
        val distance = calculateDistance(deviceLat, deviceLon, cargoLat, cargoLon)
        
        // Проверяем, находится ли устройство в пределах коридора маршрута
        return distance <= ROUTE_CORRIDOR_WIDTH
    }

    /**
     * Проверка подмены GPS-координат устройства
     */
    @Transactional
    fun detectGpsSpoofing(
        userId: String,
        currentLocation: DeviceLocation,
        previousLocation: DeviceLocation?
    ): GpsSpoofingDetectionResult {
        if (previousLocation == null) {
            return GpsSpoofingDetectionResult(
                isSpoofingDetected = false,
                confidence = 0.0,
                detectedAnomalies = emptyList()
            )
        }

        val anomalies = mutableListOf<String>()
        var confidence = 0.0

        // Проверка на телепортацию (слишком большое расстояние за короткое время)
        val distance = calculateDistance(
            previousLocation.latitude, previousLocation.longitude,
            currentLocation.latitude, currentLocation.longitude
        )
        
        val timeDiff = currentLocation.timestamp.toEpochMilli() - previousLocation.timestamp.toEpochMilli()
        val timeHours = timeDiff / (1000.0 * 60 * 60)
        
        if (timeHours > 0) {
            val speed = distance / timeHours
            if (speed > 200.0) { // Подозрительная скорость для наземного транспорта
                anomalies.add("Teleportation detected: impossible speed")
                confidence += 0.8
            }
        }

        // Проверка на статичные координаты (подмена GPS-эмитатором)
        if (distance < 1.0 && timeDiff > 60000) { // Меньше 1 метра за 1 минуту
            anomalies.add("Static coordinates detected")
            confidence += 0.6
        }

        // Проверка на резкие изменения высоты
        if (currentLocation.altitude != null && previousLocation.altitude != null) {
            val altitudeDiff = kotlin.math.abs(currentLocation.altitude - previousLocation.altitude)
            if (altitudeDiff > 1000.0) { // Резкое изменение высоты
                anomalies.add("Sudden altitude change detected")
                confidence += 0.4
            }
        }

        // Проверка на подозрительные паттерны перемещения
        if (isSuspiciousMovementPattern(currentLocation, previousLocation)) {
            anomalies.add("Suspicious movement pattern detected")
            confidence += 0.5
        }

        val result = GpsSpoofingDetectionResult(
            isSpoofingDetected = confidence > 0.5,
            confidence = confidence,
            detectedAnomalies = anomalies
        )

        // Отправляем событие при обнаружении подмены
        if (result.isSpoofingDetected) {
            sendGpsSpoofingAlert(userId, currentLocation, confidence, anomalies)
        }

        return result
    }

    /**
     * Проверка подозрительных паттернов перемещения
     */
    private fun isSuspiciousMovementPattern(current: DeviceLocation, previous: DeviceLocation): Boolean {
        // Проверка на резкие изменения направления
        val directionChange = calculateDirectionChange(previous, current)
        
        // Проверка на движение по прямым линиям (характерно для GPS-эмитаторов)
        val isStraightLineMovement = checkStraightLineMovement(current, previous)
        
        return directionChange > 90.0 || isStraightLineMovement
    }

    /**
     * Расчет изменения направления
     */
    private fun calculateDirectionChange(prev: DeviceLocation, curr: DeviceLocation): Double {
        // Упрощенный расчет - в реальной системе будет сложнее
        return 0.0
    }

    /**
     * Проверка движения по прямым линиям
     */
    private fun checkStraightLineMovement(curr: DeviceLocation, prev: DeviceLocation): Boolean {
        // Упрощенная проверка - в реальной системе будет анализ истории перемещений
        return false
    }

    /**
     * Проверка доступа к чувствительным данным с учетом геолокации
     */
    @Transactional
    fun validateSensitiveDataAccess(
        userId: String,
        dataType: String,
        userLatitude: Double,
        userLongitude: Double,
        requiredSecurityLevel: String
    ): SensitiveDataAccessResult {
        // Проверка уровня безопасности пользователя
        val hasRequiredLevel = checkSecurityLevel(userId, requiredSecurityLevel)
        
        // Проверка географических ограничений
        val isLocationAllowed = checkGeographicRestrictions(userLatitude, userLongitude, dataType)
        
        // Проверка нахождения в безопасной зоне
        val isInSecureZone = checkSecureZone(userLatitude, userLongitude)

        val accessResult = SensitiveDataAccessResult(
            userId = userId,
            dataType = dataType,
            hasRequiredSecurityLevel = hasRequiredLevel,
            isLocationAllowed = isLocationAllowed,
            isInSecureZone = isInSecureZone,
            accessGranted = hasRequiredLevel && isLocationAllowed && isInSecureZone,
            timestamp = Instant.now()
        )

        // Отправляем событие о проверке доступа к чувствительным данным
        sendSensitiveDataAccessEvent(accessResult)

        return accessResult
    }

    /**
     * Проверка уровня безопасности пользователя
     */
    private fun checkSecurityLevel(userId: String, requiredLevel: String): Boolean {
        // В реальной системе здесь будет проверка из Compliance Passport
        // Пока заглушка
        return true
    }

    /**
     * Проверка географических ограничений
     */
    private fun checkGeographicRestrictions(latitude: Double, longitude: Double, dataType: String): Boolean {
        // В реальной системе здесь будет проверка по базе данных географических ограничений
        // Пока заглушка
        return true
    }

    /**
     * Проверка нахождения в безопасной зоне
     */
    private fun checkSecureZone(latitude: Double, longitude: Double): Boolean {
        // В реальной системе здесь будет проверка по базе данных безопасных зон
        // Пока заглушка
        return true
    }

    /**
     * Расчет расстояния между двумя точками (в метрах)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Радиус Земли в метрах
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = (kotlin.math.sin(latDistance / 2) * kotlin.math.sin(latDistance / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(lonDistance / 2) * kotlin.math.sin(lonDistance / 2))
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return R * c
    }

    /**
     * Отправка события о геозонной проверке
     */
    private fun sendGeofencingEvent(result: GeofencingValidationResult) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "GEOFENCING_CHECK_${System.currentTimeMillis()}",
                eventType = "GEOFENCING_CHECK",
                timestamp = result.timestamp,
                userId = result.userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "order_id" to result.orderId,
                    "is_within_route" to result.isWithinRoute.toString(),
                    "distance_to_cargo" to result.distanceToCargo.toString(),
                    "access_granted" to result.accessGranted.toString(),
                    "payload_geo_lat" to result.payloadGeoLat.toString(),
                    "payload_geo_lon" to result.payloadGeoLon.toString(),
                    "cargo_latitude" to result.cargoLatitude.toString(),
                    "cargo_longitude" to result.cargoLongitude.toString()
                )
            )
        )
    }

    /**
     * Отправка события о блокировке доступа
     */
    private fun sendAccessBlockedEvent(result: GeofencingValidationResult) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "ACCESS_BLOCKED_${System.currentTimeMillis()}",
                eventType = "ACCESS_BLOCKED",
                timestamp = result.timestamp,
                userId = result.userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "order_id" to result.orderId,
                    "reason" to "Outside allowed route corridor",
                    "payload_geo_lat" to result.payloadGeoLat.toString(),
                    "payload_geo_lon" to result.payloadGeoLon.toString(),
                    "cargo_latitude" to result.cargoLatitude.toString(),
                    "cargo_longitude" to result.cargoLongitude.toString()
                )
            )
        )
    }

    /**
     * Отправка события о подмене GPS
     */
    private fun sendGpsSpoofingAlert(
        userId: String,
        deviceLocation: DeviceLocation,
        confidence: Double,
        anomalies: List<String>
    ) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "GPS_SPOOFING_ALERT_${System.currentTimeMillis()}",
                eventType = "GPS_SPOOFING_ALERT",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "device_lat" to deviceLocation.latitude.toString(),
                    "device_lon" to deviceLocation.longitude.toString(),
                    "confidence" to confidence.toString(),
                    "anomalies" to anomalies.joinToString(",")
                )
            )
        )
    }

    /**
     * Отправка события о проверке доступа к чувствительным данным
     */
    private fun sendSensitiveDataAccessEvent(result: SensitiveDataAccessResult) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "SENSITIVE_DATA_ACCESS_CHECK_${System.currentTimeMillis()}",
                eventType = "SENSITIVE_DATA_ACCESS_CHECK",
                timestamp = result.timestamp,
                userId = result.userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "data_type" to result.dataType,
                    "required_security_level" to "CONFIDENTIAL",
                    "has_required_level" to result.hasRequiredSecurityLevel.toString(),
                    "is_location_allowed" to result.isLocationAllowed.toString(),
                    "is_in_secure_zone" to result.isInSecureZone.toString(),
                    "access_granted" to result.accessGranted.toString(),
                    "user_lat" to result.userLatitude.toString(),
                    "user_lon" to result.userLongitude.toString()
                )
            )
        )
    }
}

/**
 * Модели для геозонной валидации
 */

data class GeofencingValidationResult(
    val userId: String,
    val orderId: String,
    val isWithinRoute: Boolean,
    val distanceToCargo: Double,
    val accessGranted: Boolean,
    val timestamp: Instant,
    val payloadGeoLat: Double,
    val payloadGeoLon: Double,
    val cargoLatitude: Double,
    val cargoLongitude: Double,
    val gpsSpoofingDetected: Boolean = false,
    val spoofingConfidence: Double = 0.0,
    val spoofingAnomalies: List<String> = emptyList()
)

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val altitude: Double? = null,
    val accuracy: Double? = null
)

data class GpsSpoofingDetectionResult(
    val isSpoofingDetected: Boolean,
    val confidence: Double,
    val detectedAnomalies: List<String>
)

data class SensitiveDataAccessResult(
    val userId: String,
    val dataType: String,
    val hasRequiredSecurityLevel: Boolean,
    val isLocationAllowed: Boolean,
    val isInSecureZone: Boolean,
    val accessGranted: Boolean,
    val timestamp: Instant,
    val userLatitude: Double,
    val userLongitude: Double
)

data class SecurityEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: String,
    val sourceService: String,
    val details: Map<String, String>
) <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt
scm-service/src/main/kotlin/com/internal/services/DynamicGeofencingService.kt

# Current Time
2/3/2026, 11:26:27 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
90,234 / 256K tokens used (35%)

# Current Mode
ACT MODE
</environment_details>