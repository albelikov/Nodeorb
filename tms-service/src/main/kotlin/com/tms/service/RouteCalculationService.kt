package com.tms.service

import com.tms.dto.*
import com.tms.model.Route
import com.tms.model.RouteWaypoint
import com.tms.repository.RouteRepository
import com.tms.repository.RouteWaypointRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.Instant

@Service
class RouteCalculationService(
    private val restTemplate: RestTemplate,
    private val routeRepository: RouteRepository,
    private val routeWaypointRepository: RouteWaypointRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(RouteCalculationService::class.java)
        private const val OSRM_BASE_URL = "http://router.project-osrm.org"
        private const val TRUCK_FUEL_CONSUMPTION_PER_KM = 0.2 // liters per km
        private const val TRUCK_CO2_EMISSION_PER_KM = 0.53 // kg per km
    }

    @Value("\${tms.routing.api.url:http://router.project-osrm.org}")
    private lateinit var routingApiUrl: String

    /**
     * Рассчитывает маршрут с помощью внешнего API (OSRM)
     */
    fun calculateRoute(request: RouteRequestDto): RouteResponseDto {
        logger.info("Calculating route from ${request.origin.address} to ${request.destination.address}")

        // 1. Формируем запрос к OSRM API
        val osrmResponse = fetchRouteFromOsrm(request)

        // 2. Рассчитываем дополнительные параметры (топливо, CO2)
        val distance = osrmResponse.distance / 1000.0 // meters to km
        val duration = osrmResponse.duration.toInt() // seconds
        val fuelConsumption = distance * TRUCK_FUEL_CONSUMPTION_PER_KM
        val co2Emissions = distance * TRUCK_CO2_EMISSION_PER_KM
        val estimatedCost = distance * 0.5 // 0.5 EUR per km (примерная ставка)

        // 3. Сохраняем маршрут в БД
        val route = saveRouteToDb(request, distance, duration, estimatedCost, fuelConsumption, co2Emissions)

        // 4. Формируем ответ
        return RouteResponseDto(
            routeId = route.routeNumber,
            status = "CALCULATED",
            distance = distance,
            duration = duration,
            estimatedCost = estimatedCost,
            fuelConsumption = fuelConsumption,
            co2Emissions = co2Emissions,
            geometry = GeometryDto(
                type = "LineString",
                coordinates = decodePolyline(osrmResponse.geometry)
            ),
            legs = listOf(
                RouteLegDto(
                    from = request.origin.address,
                    to = request.destination.address,
                    distance = distance,
                    duration = duration,
                    instructions = emptyList() // TODO: Add turn-by-turn instructions
                )
            ),
            alerts = emptyList() // TODO: Add alerts
        )
    }

    /**
     * Запрашивает маршрут из OSRM API
     */
    private fun fetchRouteFromOsrm(request: RouteRequestDto): OsrmResponseDto {
        val coordinates = buildCoordinatesString(request)
        val url = "$routingApiUrl/route/v1/driving/$coordinates?overview=full&geometries=polyline"
        logger.info("Calling OSRM API: $url")

        return restTemplate.getForObject(url, OsrmResponseDto::class.java)
            ?: throw RuntimeException("Failed to fetch route from OSRM")
    }

    /**
     * Формирует строку координат для OSRM API
     */
    private fun buildCoordinatesString(request: RouteRequestDto): String {
        val coordinates = mutableListOf(
            "${request.origin.longitude},${request.origin.latitude}",
            "${request.destination.longitude},${request.destination.latitude}"
        )
        request.waypoints?.forEach { waypoint ->
            coordinates.add("${waypoint.longitude},${waypoint.latitude}")
        }
        return coordinates.joinToString(";")
    }

    /**
     * Декодирует полилайн из OSRM API
     */
    private fun decodePolyline(encoded: String): List<List<Double>> {
        val coordinates = mutableListOf<List<Double>>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            coordinates.add(listOf(lng.toDouble() / 1e5, lat.toDouble() / 1e5))
        }

        return coordinates
    }

    /**
     * Сохраняет маршрут в базе данных
     */
    private fun saveRouteToDb(
        request: RouteRequestDto,
        distance: Double,
        duration: Int,
        estimatedCost: Double,
        fuelConsumption: Double,
        co2Emissions: Double
    ): Route {
        val route = Route(
            routeNumber = generateRouteNumber(),
            originAddress = request.origin.address,
            originLatitude = request.origin.latitude,
            originLongitude = request.origin.longitude,
            destinationAddress = request.destination.address,
            destinationLatitude = request.destination.latitude,
            destinationLongitude = request.destination.longitude,
            totalDistance = distance,
            totalDuration = duration,
            estimatedCost = estimatedCost,
            fuelConsumption = fuelConsumption,
            co2Emissions = co2Emissions,
            geometry = null, // TODO: Convert to WKT LineString
            vehicleType = request.vehicleType,
            optimizationType = request.preferences.optimization,
            status = "CALCULATED",
            calculatedAt = Instant.now(),
            createdBy = null
        )
        val savedRoute = routeRepository.save(route)

        // Сохраняем промежуточные точки
        request.waypoints?.forEachIndexed { index, waypoint ->
            val routeWaypoint = RouteWaypoint(
                route = savedRoute,
                sequenceOrder = index + 1,
                address = waypoint.address,
                latitude = waypoint.latitude,
                longitude = waypoint.longitude,
                arrivalTime = null,
                departureTime = null,
                stopDuration = null
            )
            routeWaypointRepository.save(routeWaypoint)
        }

        logger.info("Route saved to DB: ${savedRoute.routeNumber}")
        return savedRoute
    }

    /**
     * Генерирует уникальный номер маршрута
     */
    private fun generateRouteNumber(): String {
        return "ROUTE-${java.time.Year.now()}-${String.format("%05d", (Math.random() * 100000).toInt())}"
    }

    /**
     * DTO для ответа из OSRM API
     */
    data class OsrmResponseDto(
        val code: String,
        val routes: List<OsrmRouteDto>
    ) {
        val distance: Double get() = routes.first().distance
        val duration: Double get() = routes.first().duration
        val geometry: String get() = routes.first().geometry
    }

    data class OsrmRouteDto(
        val distance: Double,
        val duration: Double,
        val geometry: String
    )
}