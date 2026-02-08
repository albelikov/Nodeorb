package com.tms.controller

import com.tms.dto.LocationUpdateDto
import com.tms.dto.TrackingResponseDto
import com.tms.service.LocationTrackingService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tracking")
class TrackingController(
    private val locationTrackingService: LocationTrackingService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(TrackingController::class.java)
    }

    /**
     * Принимает обновление локации
     * @param locationUpdate Детали обновления
     * @return Ответ с текущей локацией и ETA
     */
    @PostMapping("/location")
    fun updateLocation(@RequestBody locationUpdate: LocationUpdateDto): ResponseEntity<TrackingResponseDto> {
        logger.info("Received location update request: $locationUpdate")
        try {
            val response = locationTrackingService.handleLocationUpdate(locationUpdate)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error processing location update: $locationUpdate", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает текущую локацию отправки
     * @param shipmentId ID отправки
     * @return Ответ с текущей локацией и ETA
     */
    @GetMapping("/shipments/{shipmentId}/location")
    fun getCurrentLocation(@PathVariable shipmentId: Long): ResponseEntity<TrackingResponseDto> {
        logger.info("Received request to get current location for shipment: $shipmentId")
        try {
            val response = locationTrackingService.getCurrentLocation(shipmentId)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error getting current location for shipment: $shipmentId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает историю локаций отправки
     * @param shipmentId ID отправки
     * @param from Начальная дата (опционально)
     * @return История локаций
     */
    @GetMapping("/shipments/{shipmentId}/history")
    fun getLocationHistory(
        @PathVariable shipmentId: Long,
        @RequestParam(required = false) from: String?
    ): ResponseEntity<Any> {
        logger.info("Received request to get location history for shipment: $shipmentId")
        try {
            // TODO: Implement get location history
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null)
        } catch (e: Exception) {
            logger.error("Error getting location history for shipment: $shipmentId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает все активные отправки на карте
     * @param bounds Ограничения карты (lat1,lon1,lat2,lon2)
     * @return Список активных отправок
     */
    @GetMapping("/shipments/active")
    fun getActiveShipments(@RequestParam bounds: String?): ResponseEntity<Any> {
        logger.info("Received request to get active shipments on map: bounds=$bounds")
        try {
            // TODO: Implement get active shipments
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null)
        } catch (e: Exception) {
            logger.error("Error getting active shipments on map: bounds=$bounds", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}