package com.tms.service

import com.tms.model.LocationHistory
import com.tms.model.Shipment
import com.tms.repository.LocationHistoryRepository
import com.tms.repository.ShipmentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class ETACalculationService(
    private val locationHistoryRepository: LocationHistoryRepository,
    private val shipmentRepository: ShipmentRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ETACalculationService::class.java)
        private const val AVERAGE_SPEED = 80.0 // km/h ( примерная скорость для грузовика)
    }

    /**
     * Рассчитывает ETA (estimated time of arrival) для отправки
     * @param shipment Отправка
     * @return ETA в формате ISO-8601
     */
    fun calculateETA(shipment: Shipment): String {
        logger.info("Calculating ETA for shipment: ${shipment.shipmentNumber}")

        // Получаем последнюю известную локацию
        val lastLocation = locationHistoryRepository.findByShipmentIdOrderByTimestampDesc(shipment.id!!)
            .firstOrNull()

        if (lastLocation == null) {
            logger.warn("No location history found for shipment: ${shipment.shipmentNumber}")
            // Если нет истории локаций, возвращаем исходный estimatedArrival
            return shipment.estimatedArrival?.atOffset(ZoneOffset.UTC)?.format(DateTimeFormatter.ISO_DATE_TIME)
                ?: Instant.now().plusSeconds(3600 * 24).atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_DATE_TIME)
        }

        // Вычисляем оставшееся расстояние (примерное)
        val remainingDistance = calculateRemainingDistance(lastLocation, shipment)

        // Вычисляем ETA
        val travelTimeHours = remainingDistance / AVERAGE_SPEED
        val eta = lastLocation.timestamp.plusSeconds((travelTimeHours * 3600).toLong())

        logger.info("ETA calculated for shipment: ${shipment.shipmentNumber} - $eta")
        return eta.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)
    }

    /**
     * Вычисляет оставшееся расстояние от текущей локации до назначения
     * @param lastLocation Последняя известная локация
     * @param shipment Отправка
     * @return Оставшееся расстояние в км
     */
    fun calculateRemainingDistance(lastLocation: LocationHistory, shipment: Shipment): Double {
        // Используем формулу расстояния между двумя точками (плоская земля)
        val lat1 = Math.toRadians(lastLocation.latitude)
        val lon1 = Math.toRadians(lastLocation.longitude)
        val lat2 = Math.toRadians(shipment.deliveryLatitude)
        val lon2 = Math.toRadians(shipment.deliveryLongitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val earthRadius = 6371.0 // km
        val distance = earthRadius * c

        logger.debug("Remaining distance from ${lastLocation.latitude}, ${lastLocation.longitude} to " +
                "${shipment.deliveryLatitude}, ${shipment.deliveryLongitude}: $distance km")
        return distance
    }

    /**
     * Вычисляет процент завершенности маршрута
     * @param shipment Отправка
     * @return Процент завершенности (0.0 - 100.0)
     */
    fun calculatePercentComplete(shipment: Shipment): Double {
        // Получаем последнюю известную локацию
        val lastLocation = locationHistoryRepository.findByShipmentIdOrderByTimestampDesc(shipment.id!!)
            .firstOrNull()

        if (lastLocation == null) {
            logger.warn("No location history found for shipment: ${shipment.shipmentNumber}")
            return 0.0
        }

        // Вычисляем общее расстояние маршрута
        val totalDistance = calculateTotalDistance(shipment)
        // Вычисляем расстояние от начала маршрута до текущей точки
        val distanceTraveled = calculateDistanceFromStart(lastLocation, shipment)
        // Вычисляем процент завершенности
        val percentComplete = (distanceTraveled / totalDistance) * 100.0

        logger.debug("Percent complete for shipment: ${shipment.shipmentNumber} - $percentComplete%")
        return Math.min(100.0, Math.max(0.0, percentComplete))
    }

    /**
     * Вычисляет общее расстояние маршрута
     * @param shipment Отправка
     * @return Общее расстояние в км
     */
    fun calculateTotalDistance(shipment: Shipment): Double {
        // Используем формулу расстояния между двумя точками (плоская земля)
        val lat1 = Math.toRadians(shipment.pickupLatitude)
        val lon1 = Math.toRadians(shipment.pickupLongitude)
        val lat2 = Math.toRadians(shipment.deliveryLatitude)
        val lon2 = Math.toRadians(shipment.deliveryLongitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val earthRadius = 6371.0 // km
        val distance = earthRadius * c

        logger.debug("Total distance for shipment: ${shipment.shipmentNumber} - $distance km")
        return distance
    }

    /**
     * Вычисляет расстояние от начала маршрута до текущей точки
     * @param lastLocation Последняя известная локация
     * @param shipment Отправка
     * @return Расстояние в км
     */
    private fun calculateDistanceFromStart(lastLocation: LocationHistory, shipment: Shipment): Double {
        // Используем формулу расстояния между двумя точками (плоская земля)
        val lat1 = Math.toRadians(shipment.pickupLatitude)
        val lon1 = Math.toRadians(shipment.pickupLongitude)
        val lat2 = Math.toRadians(lastLocation.latitude)
        val lon2 = Math.toRadians(lastLocation.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val earthRadius = 6371.0 // km
        val distance = earthRadius * c

        logger.debug("Distance from start for shipment: ${shipment.shipmentNumber} - $distance km")
        return distance
    }
}