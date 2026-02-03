package com.internal.engine.validation

import com.internal.integrations.SecurityEventBus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Валидатор доступа с динамической геозоной
 * Проверяет доступ к деталям груза на основе географического положения
 */
@Service
class AccessValidator(
    private val geofenceValidator: GeofenceValidator,
    private val securityEventBus: SecurityEventBus
) {

    companion object {
        private const val ROUTE_CORRIDOR_WIDTH = 1000.0 // метров
        private const val MAX_DISTANCE_FROM_ROUTE = 500.0 // метров
    }

    /**
     * Проверка доступа к деталям груза с учетом геозоны
     */
    @Transactional
    fun validateCargoAccess(
        userId: String,
        orderId: String,
        deviceLatitude: Double,
        deviceLongitude: Double,
        cargoLatitude: Double,
        cargoLongitude: Double
    ): CargoAccessValidationResult {
        // Проверяем, находится ли устройство в пределах коридора маршрута
        val isWithinRoute = isWithinAllowedRoute(
            deviceLatitude, deviceLongitude,
            cargoLatitude, cargoLongitude
        )

        // Проверяем расстояние от устройства до груза
        val distanceToCargo = calculateDistance(
            deviceLatitude, deviceLongitude,
            cargoLatitude, cargoLongitude
        )

        val accessResult = CargoAccessValidationResult(
            userId = userId,
            orderId = orderId,
            isWithinRoute = isWithinRoute,
            distanceToCargo = distanceToCargo,
            accessGranted = isWithinRoute && distanceToCargo <= MAX_DISTANCE_FROM_ROUTE,
            timestamp = Instant.now()
        )

        // Отправляем событие о проверке доступа
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "CARGO_ACCESS_CHECK_${System.currentTimeMillis()}",
                eventType = "CARGO_ACCESS_CHECK",
                timestamp = accessResult.timestamp,
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "order_id" to orderId,
                    "is_within_route" to accessResult.isWithinRoute.toString(),
                    "distance_to_cargo" to accessResult.distanceToCargo.toString(),
                    "access_granted" to accessResult.accessGranted.toString(),
                    "device_lat" to deviceLatitude.toString(),
                    "device_lon" to deviceLongitude.toString(),
                    "cargo_lat" to cargoLatitude.toString(),
                    "cargo_lon" to cargoLongitude.toString()
                )
            )
        )

        return accessResult
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
        // Пока упрощенная проверка расстояния между устройством и грузом
        
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
            securityEventBus.triggerGpsSpoofingAlert(
                userId = userId,
                deviceLocation = currentLocation,
                confidence = confidence,
                anomalies = anomalies
            )
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
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "SENSITIVE_DATA_ACCESS_CHECK_${System.currentTimeMillis()}",
                eventType = "SENSITIVE_DATA_ACCESS_CHECK",
                timestamp = accessResult.timestamp,
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "data_type" to dataType,
                    "required_security_level" to requiredSecurityLevel,
                    "has_required_level" to accessResult.hasRequiredSecurityLevel.toString(),
                    "is_location_allowed" to accessResult.isLocationAllowed.toString(),
                    "is_in_secure_zone" to accessResult.isInSecureZone.toString(),
                    "access_granted" to accessResult.accessGranted.toString(),
                    "user_lat" to userLatitude.toString(),
                    "user_lon" to userLongitude.toString()
                )
            )
        )

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
}

/**
 * Модели для валидации доступа
 */

data class CargoAccessValidationResult(
    val userId: String,
    val orderId: String,
    val isWithinRoute: Boolean,
    val distanceToCargo: Double,
    val accessGranted: Boolean,
    val timestamp: Instant
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
    val timestamp: Instant
) <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/ExternalComplianceAdapter.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/ExternalComplianceAdapter.kt

# Current Time
2/3/2026, 11:14:01 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
32,022 / 256K tokens used (13%)

# Current Mode
ACT MODE
</environment_details>