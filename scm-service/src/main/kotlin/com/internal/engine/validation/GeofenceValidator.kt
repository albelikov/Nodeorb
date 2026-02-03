package com.internal.engine.validation

import com.internal.integrations.SecurityEventBus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Валидатор геозон
 * Контроль местоположения пользователей и транспортных средств
 */
@Service
class GeofenceValidator(
    private val securityEventBus: SecurityEventBus
) {

    companion object {
        private const val DEFAULT_GEOFENCE_RADIUS = 500.0 // метров
        private const val MAX_SPEED_KMH = 120.0 // максимальная скорость в км/ч
    }

    /**
     * Проверка нахождения пользователя в разрешенной геозоне
     */
    @Transactional
    fun validateGeofence(
        userId: String,
        latitude: Double,
        longitude: Double,
        geofenceType: String,
        orderId: String? = null
    ): GeofenceValidationResult {
        val geofenceConfig = getGeofenceConfig(geofenceType)
        
        // Проверяем, находится ли пользователь в пределах геозоны
        val isInside = checkGeofenceBounds(
            latitude,
            longitude,
            geofenceConfig.centerLat,
            geofenceConfig.centerLon,
            geofenceConfig.radius
        )

        // Проверяем скорость перемещения
        val speedCheck = validateSpeed(userId, latitude, longitude)

        // Проверяем историю перемещений
        val movementHistory = validateMovementHistory(userId, latitude, longitude, geofenceType)

        val validationResult = GeofenceValidationResult(
            isInside = isInside,
            geofenceType = geofenceType,
            violationReason = if (isInside) null else "Outside geofence bounds",
            speedViolation = speedCheck.isViolation,
            speed = speedCheck.speed,
            timestamp = Instant.now(),
            orderId = orderId
        )

        // Отправляем событие при нарушении
        if (!isInside || speedCheck.isViolation) {
            securityEventBus.triggerGeofenceViolation(
                userId = userId,
                latitude = latitude,
                longitude = longitude,
                geofenceType = geofenceType,
                violationReason = validationResult.violationReason ?: "Speed violation"
            )
        }

        return validationResult
    }

    /**
     * Проверка маршрута на соответствие коридору безопасности
     */
    @Transactional
    fun validateRoute(
        userId: String,
        routePoints: List<RoutePoint>,
        corridorWidth: Double = 1000.0 // метров
    ): RouteValidationResult {
        val violations = mutableListOf<RouteViolation>()
        
        for (i in 1 until routePoints.size) {
            val prevPoint = routePoints[i - 1]
            val currPoint = routePoints[i]
            
            // Проверяем отклонение от прямой линии
            val deviation = calculateRouteDeviation(prevPoint, currPoint, corridorWidth)
            
            if (deviation > corridorWidth) {
                violations.add(
                    RouteViolation(
                        point = currPoint,
                        deviation = deviation,
                        violationType = "CORRIDOR_VIOLATION",
                        timestamp = Instant.now()
                    )
                )
            }
            
            // Проверяем скорость
            val speed = calculateSpeed(prevPoint, currPoint)
            if (speed > MAX_SPEED_KMH) {
                violations.add(
                    RouteViolation(
                        point = currPoint,
                        deviation = 0.0,
                        violationType = "SPEED_VIOLATION",
                        timestamp = Instant.now()
                    )
                )
            }
        }

        return RouteValidationResult(
            isValid = violations.isEmpty(),
            violations = violations,
            totalDistance = calculateTotalDistance(routePoints),
            estimatedTime = calculateEstimatedTime(routePoints)
        )
    }

    /**
     * Проверка подмены GPS-координат
     */
    @Transactional
    fun detectGpsSpoofing(
        userId: String,
        currentLocation: LocationPoint,
        previousLocation: LocationPoint?
    ): SpoofingDetectionResult {
        if (previousLocation == null) {
            return SpoofingDetectionResult(
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
            if (speed > MAX_SPEED_KMH * 2) { // Подозрительная скорость
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

        return SpoofingDetectionResult(
            isSpoofingDetected = confidence > 0.5,
            confidence = confidence,
            detectedAnomalies = anomalies
        )
    }

    /**
     * Получение конфигурации геозоны
     */
    private fun getGeofenceConfig(geofenceType: String): GeofenceConfig {
        return when (geofenceType) {
            "WAREHOUSE" -> GeofenceConfig(
                centerLat = 50.4501,
                centerLon = 30.5234,
                radius = 500.0
            )
            "CUSTOMS" -> GeofenceConfig(
                centerLat = 50.4644,
                centerLon = 30.5191,
                radius = 1000.0
            )
            "DELIVERY_ZONE" -> GeofenceConfig(
                centerLat = 50.4547,
                centerLon = 30.5238,
                radius = 200.0
            )
            else -> GeofenceConfig(
                centerLat = 50.4501,
                centerLon = 30.5234,
                radius = DEFAULT_GEOFENCE_RADIUS
            )
        }
    }

    /**
     * Проверка нахождения точки в геозоне
     */
    private fun checkGeofenceBounds(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
        radius: Double
    ): Boolean {
        val distance = calculateDistance(lat1, lon1, lat2, lon2)
        return distance <= radius
    }

    /**
     * Проверка скорости перемещения
     */
    private fun validateSpeed(userId: String, lat: Double, lon: Double): SpeedCheck {
        // В реальной системе здесь будет проверка истории перемещений
        // Пока заглушка
        return SpeedCheck(
            isViolation = false,
            speed = 0.0
        )
    }

    /**
     * Проверка истории перемещений
     */
    private fun validateMovementHistory(
        userId: String,
        lat: Double,
        lon: Double,
        geofenceType: String
    ): MovementHistory {
        // В реальной системе здесь будет анализ истории перемещений
        // Пока заглушка
        return MovementHistory(
            isSuspicious = false,
            pattern = "NORMAL"
        )
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
     * Расчет отклонения от маршрута
     */
    private fun calculateRouteDeviation(
        startPoint: RoutePoint,
        endPoint: RoutePoint,
        corridorWidth: Double
    ): Double {
        // Упрощенный расчет - в реальной системе будет сложнее
        return 0.0
    }

    /**
     * Расчет скорости между двумя точками
     */
    private fun calculateSpeed(prevPoint: RoutePoint, currPoint: RoutePoint): Double {
        val distance = calculateDistance(
            prevPoint.latitude, prevPoint.longitude,
            currPoint.latitude, currPoint.longitude
        )
        
        val timeDiff = currPoint.timestamp.toEpochMilli() - prevPoint.timestamp.toEpochMilli()
        val timeHours = timeDiff / (1000.0 * 60 * 60)
        
        return if (timeHours > 0) distance / timeHours else 0.0
    }

    /**
     * Расчет общего расстояния маршрута
     */
    private fun calculateTotalDistance(routePoints: List<RoutePoint>): Double {
        var totalDistance = 0.0
        for (i in 1 until routePoints.size) {
            totalDistance += calculateDistance(
                routePoints[i - 1].latitude, routePoints[i - 1].longitude,
                routePoints[i].latitude, routePoints[i].longitude
            )
        }
        return totalDistance
    }

    /**
     * Расчет оценочного времени в пути
     */
    private fun calculateEstimatedTime(routePoints: List<RoutePoint>): Double {
        val distance = calculateTotalDistance(routePoints)
        return distance / MAX_SPEED_KMH // часов
    }
}

/**
 * Конфигурация геозоны
 */
data class GeofenceConfig(
    val centerLat: Double,
    val centerLon: Double,
    val radius: Double
)

/**
 * Результат валидации геозоны
 */
data class GeofenceValidationResult(
    val isInside: Boolean,
    val geofenceType: String,
    val violationReason: String?,
    val speedViolation: Boolean,
    val speed: Double,
    val timestamp: Instant,
    val orderId: String?
)

/**
 * Точка маршрута
 */
data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val altitude: Double? = null
)

/**
 * Точка местоположения
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val altitude: Double? = null
)

/**
 * Результат валидации маршрута
 */
data class RouteValidationResult(
    val isValid: Boolean,
    val violations: List<RouteViolation>,
    val totalDistance: Double,
    val estimatedTime: Double
)

/**
 * Нарушение маршрута
 */
data class RouteViolation(
    val point: RoutePoint,
    val deviation: Double,
    val violationType: String,
    val timestamp: Instant
)

/**
 * Результат проверки на подмену GPS
 */
data class SpoofingDetectionResult(
    val isSpoofingDetected: Boolean,
    val confidence: Double,
    val detectedAnomalies: List<String>
)

/**
 * Проверка скорости
 */
data class SpeedCheck(
    val isViolation: Boolean,
    val speed: Double
)

/**
 * История перемещений
 */
data class MovementHistory(
    val isSuspicious: Boolean,
    val pattern: String
)