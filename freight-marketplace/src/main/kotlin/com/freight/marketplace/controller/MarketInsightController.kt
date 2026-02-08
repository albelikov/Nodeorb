package com.freight.marketplace.controller

import com.freight.marketplace.service.insight.MarketInsightService
import com.freight.marketplace.service.insight.RouteInfo
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * REST контроллер для MarketInsight Service
 * Предоставляет API для получения рыночной аналитики и цен на топливо
 */
@RestController
@RequestMapping("/api/v1/node-insight")
class MarketInsightController(
    private val marketInsightService: MarketInsightService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(MarketInsightController::class.java)
        private val geometryFactory = GeometryFactory()
    }

    /**
     * Получает коэффициент изменения цены на топливо
     */
    @GetMapping("/fuel-surcharge")
    fun getFuelSurcharge(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting fuel surcharge")

        try {
            val fuelSurcharge = marketInsightService.getFuelSurcharge()

return ResponseEntity.ok(mapOf<String, Any>(
                "success" to true,
                "fuelSurcharge" to fuelSurcharge,
                "message" to "Fuel surcharge retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting fuel surcharge", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            )))
        }
    }

    /**
     * Валидирует рыночную цену заказа
     */
    @PostMapping("/validate-price")
    fun validateMarketPrice(
        @RequestParam orderPrice: Double,
        @RequestParam pickupLat: Double,
        @RequestParam pickupLon: Double,
        @RequestParam deliveryLat: Double,
        @RequestParam deliveryLon: Double
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Validating market price for order: $orderPrice")

        try {
            val pickupPoint = createPoint(pickupLat, pickupLon)
            val deliveryPoint = createPoint(deliveryLat, deliveryLon)

            val route = RouteInfo(pickupPoint, deliveryPoint)
            val validation = marketInsightService.validateMarketPrice(BigDecimal(orderPrice), route)

return ResponseEntity.ok(mapOf<String, Any>(
                "success" to true,
                "validation" to validation,
                "message" to "Market price validated successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error validating market price", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "orderPrice" to orderPrice,
                "pickupLat" to pickupLat,
                "pickupLon" to pickupLon,
                "deliveryLat" to deliveryLat,
                "deliveryLon" to deliveryLon,
                "error" to e.message
            )))
        }
    }

    /**
     * Получает статистику по кэшу
     */
    @GetMapping("/cache-statistics")
    fun getCacheStatistics(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting cache statistics")

        try {
            val statistics = marketInsightService.getCacheStatistics()

return ResponseEntity.ok(mapOf<String, Any>(
                "success" to true,
                "statistics" to statistics,
                "message" to "Cache statistics retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting cache statistics", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            )))
        }
    }

    /**
     * Очищает кэш цен на маршруты
     */
    @PostMapping("/clear-cache")
    fun clearRoutePricesCache(): ResponseEntity<Map<String, Any>> {
        logger.info("Clearing route prices cache")

        try {
            marketInsightService.clearRoutePricesCache()

return ResponseEntity.ok(mapOf<String, Any>(
                "success" to true,
                "message" to "Route prices cache cleared successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error clearing route prices cache", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            )))
        }
    }

    /**
     * Создает геометрическую точку из координат
     */
    private fun createPoint(lat: Double, lon: Double): Point {
        val coordinate = Coordinate(lon, lat) // В JTS порядок: longitude, latitude
        return geometryFactory.createPoint(coordinate)
    }
}