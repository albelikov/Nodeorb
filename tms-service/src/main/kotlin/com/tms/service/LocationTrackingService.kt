package com.tms.service

import com.tms.dto.LocationUpdateDto
import com.tms.dto.TrackingResponseDto
import com.tms.model.LocationHistory
import com.tms.model.Shipment
import com.tms.repository.LocationHistoryRepository
import com.tms.repository.ShipmentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class LocationTrackingService(
    private val locationHistoryRepository: LocationHistoryRepository,
    private val shipmentRepository: ShipmentRepository,
    private val etaCalculationService: ETACalculationService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(LocationTrackingService::class.java)
    }

    /**
     * Обрабатывает обновление локации
     * @param locationUpdate Детали обновления
     * @return Ответ с текущей локацией и ETA
     */
    @Transactional
    fun handleLocationUpdate(locationUpdate: LocationUpdateDto): TrackingResponseDto {
        logger.info("Handling location update: $locationUpdate")

        // Получаем отправку
        val shipment = shipmentRepository.findById(locationUpdate.shipmentId)
            .orElseThrow { RuntimeException("Shipment not found: ${locationUpdate.shipmentId}") }

        // Создаем запись в истории локаций
        val locationHistory = createLocationHistory(locationUpdate, shipment)
        locationHistoryRepository.save(locationHistory)

        // Обновляем текущие координаты и последнее обновление в отправке
        updateShipmentLocation(shipment, locationHistory)

        // Рассчитываем ETA и процент завершенности
        val eta = etaCalculationService.calculateETA(shipment)
        val percentComplete = etaCalculationService.calculatePercentComplete(shipment)
        val remainingDistance = etaCalculationService.calculateRemainingDistance(locationHistory, shipment)

        logger.info("Location update processed for shipment: ${shipment.shipmentNumber}")

        // Формируем ответ
        return TrackingResponseDto(
            shipmentId = shipment.id!!,
            currentLocation = com.tms.dto.CurrentLocationDto(
                latitude = locationHistory.latitude,
                longitude = locationHistory.longitude,
                speed = locationHistory.speed,
                lastUpdate = locationHistory.timestamp.atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_DATE_TIME)
            ),
            estimatedArrival = eta,
            distanceRemaining = remainingDistance,
            percentComplete = percentComplete
        )
    }

    /**
     * Создает запись в истории локаций
     * @param locationUpdate Детали обновления
     * @param shipment Отправка
     * @return Запись в истории локаций
     */
    private fun createLocationHistory(locationUpdate: LocationUpdateDto, shipment: Shipment): LocationHistory {
        // Парсим timestamp
        val timestamp = Instant.parse(locationUpdate.timestamp)

        return LocationHistory(
            shipmentId = shipment.id!!,
            vehicleId = locationUpdate.vehicleId,
            latitude = locationUpdate.latitude,
            longitude = locationUpdate.longitude,
            altitude = null, // TODO: Добавить поддержку altitude
            speed = locationUpdate.speed,
            heading = locationUpdate.heading,
            accuracy = null, // TODO: Добавить поддержку accuracy
            timestamp = timestamp,
            source = "GPS", // TODO: Добавить поддержку других источников
            location = null // TODO: Преобразовать в PostGIS geography
        )
    }

    /**
     * Обновляет текущие координаты и последнее обновление в отправке
     * @param shipment Отправка
     * @param locationHistory Запись в истории локаций
     */
    private fun updateShipmentLocation(shipment: Shipment, locationHistory: LocationHistory) {
        shipment.currentLatitude = locationHistory.latitude
        shipment.currentLongitude = locationHistory.longitude
        shipment.lastLocationUpdate = locationHistory.timestamp
        // Обновляем estimatedArrival
        val eta = etaCalculationService.calculateETA(shipment)
        shipment.estimatedArrival = Instant.parse(eta)
        shipment.updatedAt = Instant.now()
        shipmentRepository.save(shipment)
        logger.debug("Shipment location updated: ${shipment.shipmentNumber}")
    }

    /**
     * Получает текущую локацию отправки
     * @param shipmentId ID отправки
     * @return Ответ с текущей локацией и ETA
     */
    fun getCurrentLocation(shipmentId: Long): TrackingResponseDto {
        logger.info("Getting current location for shipment: $shipmentId")

        // Получаем отправку
        val shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow { RuntimeException("Shipment not found: $shipmentId") }

        // Получаем последнюю запись в истории локаций
        val lastLocation = locationHistoryRepository.findByShipmentIdOrderByTimestampDesc(shipmentId)
            .firstOrNull()

        if (lastLocation == null) {
            logger.warn("No location history found for shipment: $shipmentId")
            // Если нет истории локаций, возвращаем пустую локацию
            return TrackingResponseDto(
                shipmentId = shipmentId,
                currentLocation = com.tms.dto.CurrentLocationDto(
                    latitude = 0.0,
                    longitude = 0.0,
                    speed = null,
                    lastUpdate = Instant.now().atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_DATE_TIME)
                ),
                estimatedArrival = shipment.estimatedArrival?.atOffset(ZoneOffset.UTC)
                    ?.format(DateTimeFormatter.ISO_DATE_TIME)
                    ?: Instant.now().plusSeconds(3600 * 24).atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_DATE_TIME),
                distanceRemaining = etaCalculationService.calculateTotalDistance(shipment),
                percentComplete = 0.0
            )
        }

        // Рассчитываем ETA и процент завершенности
        val eta = etaCalculationService.calculateETA(shipment)
        val percentComplete = etaCalculationService.calculatePercentComplete(shipment)
        val remainingDistance = etaCalculationService.calculateRemainingDistance(lastLocation, shipment)

        logger.info("Current location retrieved for shipment: $shipmentId")

        return TrackingResponseDto(
            shipmentId = shipmentId,
            currentLocation = com.tms.dto.CurrentLocationDto(
                latitude = lastLocation.latitude,
                longitude = lastLocation.longitude,
                speed = lastLocation.speed,
                lastUpdate = lastLocation.timestamp.atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_DATE_TIME)
            ),
            estimatedArrival = eta,
            distanceRemaining = remainingDistance,
            percentComplete = percentComplete
        )
    }
}