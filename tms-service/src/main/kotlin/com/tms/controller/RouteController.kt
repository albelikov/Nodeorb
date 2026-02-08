package com.tms.controller

import com.tms.dto.RouteRequestDto
import com.tms.dto.RouteResponseDto
import com.tms.service.RouteCalculationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/routes")
class RouteController(
    private val routeCalculationService: RouteCalculationService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(RouteController::class.java)
    }

    /**
     * Рассчитывает маршрут по заданным параметрам
     */
    @PostMapping("/calculate")
    fun calculateRoute(@RequestBody request: RouteRequestDto): ResponseEntity<RouteResponseDto> {
        logger.info("Received route calculation request: $request")
        try {
            val response = routeCalculationService.calculateRoute(request)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error calculating route: $request", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает маршрут по номеру
     */
    @GetMapping("/{routeNumber}")
    fun getRouteByNumber(@PathVariable routeNumber: String): ResponseEntity<Any> {
        logger.info("Received request to get route by number: $routeNumber")
        try {
            // TODO: Implement get route by number
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null)
        } catch (e: Exception) {
            logger.error("Error getting route by number: $routeNumber", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает все маршруты
     */
    @GetMapping
    fun getAllRoutes(): ResponseEntity<Any> {
        logger.info("Received request to get all routes")
        try {
            // TODO: Implement get all routes
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null)
        } catch (e: Exception) {
            logger.error("Error getting all routes", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}