package com.tms.service

import com.nodeorb.shared.cargo.CargoDetails
import com.nodeorb.shared.cargo.CargoType
import com.tms.dto.*
import com.tms.integration.FmsIntegrationService
import com.tms.integration.OmsIntegrationService
import com.tms.model.*
import com.tms.repository.ShipmentRepository
import com.tms.config.KafkaConfig
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class ShipmentService(
    private val shipmentRepository: ShipmentRepository,
    private val routeRepository: com.tms.repository.RouteRepository,
    private val locationHistoryRepository: com.tms.repository.LocationHistoryRepository,
    private val omsIntegrationService: OmsIntegrationService,
    private val fmsIntegrationService: FmsIntegrationService,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ShipmentService::class.java)
    }

    /**
     * Создает новый отгрузку с проверкой деталей заказа и доступности ТС
     * @param shipmentDto Детали отгрузки
     * @return Созданная отгрузка
     */
    @Transactional
    fun createShipment(shipmentDto: ShipmentDto): Shipment {
        logger.info("Creating shipment for orderId: ${shipmentDto.orderId}")

        // 1. Запрашиваем детали заказа из OMS
        val order = omsIntegrationService.getOrderDetails(shipmentDto.orderId)
        logger.info("Order details retrieved: $order")

        // 2. Проверяем доступность транспортных средств из FMS
        val availabilityCheck = VehicleAvailabilityCheckDto(
            origin = shipmentDto.origin,
            destination = shipmentDto.destination,
            weight = shipmentDto.weight,
            dimensions = shipmentDto.dimensions
        )
        val availableVehicles = fmsIntegrationService.checkVehicleAvailability(availabilityCheck)
        if (availableVehicles.isEmpty()) {
            logger.error("No vehicles available for shipment: $shipmentDto")
            throw RuntimeException("No vehicles available for shipment")
        }
        logger.info("Available vehicles: ${availableVehicles.size}")

        // 3. Создаем отгрузку
        val shipment = Shipment(
            shipmentNumber = generateShipmentNumber(),
            orderId = shipmentDto.orderId.toLong(),
            route = null,
            pickupAddress = shipmentDto.origin,
            pickupLatitude = 0.0, // TODO: Get from address
            pickupLongitude = 0.0,
            pickupDateTimeStart = Instant.now().plusSeconds(3600),
            pickupDateTimeEnd = Instant.now().plusSeconds(7200),
            actualPickupDateTime = null,
            deliveryAddress = shipmentDto.destination,
            deliveryLatitude = 0.0, // TODO: Get from address
            deliveryLongitude = 0.0,
            deliveryDateTimeStart = Instant.now().plusSeconds(36000),
            deliveryDateTimeEnd = Instant.now().plusSeconds(43200),
            actualDeliveryDateTime = null,
            cargo = CargoDetails(
                weight = java.math.BigDecimal.valueOf(shipmentDto.weight),
                volume = java.math.BigDecimal.valueOf(shipmentDto.dimensions.length * shipmentDto.dimensions.width * shipmentDto.dimensions.height),
                packageCount = 1,
                cargoType = CargoType.GENERAL,
                description = null,
                specialHandling = null,
                temperatureMin = null,
                temperatureMax = null,
                hazmat = false,
                hazmatClass = null
            ),
            baseRate = null,
            fuelSurcharge = null,
            accessorialCharges = null,
            totalCost = null,
            status = ShipmentStatus.CREATED,
            currentLatitude = null,
            currentLongitude = null,
            lastLocationUpdate = null,
            estimatedArrival = null,
            carrierId = availableVehicles.first().vehicleId.toLong(),
            vehicleId = null,
            driverId = null
        )
        val savedShipment = shipmentRepository.save(shipment)
        logger.info("Shipment created: $savedShipment")

        // 4. Публикуем событие SHIPMENT_CREATED
        val event = ShipmentCreatedEvent(
            shipmentId = savedShipment.id!!.toString(),
            orderId = savedShipment.orderId.toString(),
            origin = savedShipment.pickupAddress,
            destination = savedShipment.deliveryAddress,
            weight = savedShipment.cargo.weight.toDouble(),
            dimensions = shipmentDto.dimensions
        )
        kafkaTemplate.send(KafkaConfig.SHIPMENT_CREATED_TOPIC, event)
        logger.info("Shipment created event published: $event")

        return savedShipment
    }

    /**
     * Создает отгрузку автоматически при закрытии сделки
     * @param dealClosedEvent Событие закрытия сделки
     * @return Созданная отгрузка
     */
    @Transactional
    fun createShipmentFromDeal(dealClosedEvent: DealClosedEvent): Shipment {
        logger.info("Creating shipment from deal closed event: $dealClosedEvent")

        // Проверяем, не была ли уже создана отгрузка для этого заказа
        val existingShipments = shipmentRepository.findByOrderId(dealClosedEvent.orderId.toLong())
        if (existingShipments.isNotEmpty()) {
            logger.warn("Shipment already exists for orderId: ${dealClosedEvent.orderId}")
            return existingShipments.first()
        }

        // Создаем отгрузку
        val shipment = Shipment(
            shipmentNumber = generateShipmentNumber(),
            orderId = dealClosedEvent.orderId.toLong(),
            route = null,
            pickupAddress = dealClosedEvent.origin,
            pickupLatitude = 0.0, // TODO: Get from address
            pickupLongitude = 0.0,
            pickupDateTimeStart = Instant.now().plusSeconds(3600),
            pickupDateTimeEnd = Instant.now().plusSeconds(7200),
            actualPickupDateTime = null,
            deliveryAddress = dealClosedEvent.destination,
            deliveryLatitude = 0.0, // TODO: Get from address
            deliveryLongitude = 0.0,
            deliveryDateTimeStart = Instant.now().plusSeconds(36000),
            deliveryDateTimeEnd = Instant.now().plusSeconds(43200),
            actualDeliveryDateTime = null,
            cargo = CargoDetails(
                weight = java.math.BigDecimal.valueOf(dealClosedEvent.weight),
                volume = java.math.BigDecimal.valueOf(dealClosedEvent.dimensions.length * dealClosedEvent.dimensions.width * dealClosedEvent.dimensions.height),
                packageCount = 1,
                cargoType = CargoType.GENERAL,
                description = null,
                specialHandling = null,
                temperatureMin = null,
                temperatureMax = null,
                hazmat = false,
                hazmatClass = null
            ),
            baseRate = null,
            fuelSurcharge = null,
            accessorialCharges = null,
            totalCost = null,
            status = ShipmentStatus.CREATED,
            currentLatitude = null,
            currentLongitude = null,
            lastLocationUpdate = null,
            estimatedArrival = null,
            carrierId = dealClosedEvent.carrierId.toLong(),
            vehicleId = null,
            driverId = null
        )
        val savedShipment = shipmentRepository.save(shipment)
        logger.info("Shipment created from deal: $savedShipment")

        // Публикуем событие SHIPMENT_CREATED
        val event = ShipmentCreatedEvent(
            shipmentId = savedShipment.id!!.toString(),
            orderId = savedShipment.orderId.toString(),
            origin = savedShipment.pickupAddress,
            destination = savedShipment.deliveryAddress,
            weight = savedShipment.cargo.weight.toDouble(),
            dimensions = dealClosedEvent.dimensions
        )
        kafkaTemplate.send(KafkaConfig.SHIPMENT_CREATED_TOPIC, event)
        logger.info("Shipment created event published: $event")

        return savedShipment
    }

    /**
     * Генерирует уникальный номер отправки
     */
    private fun generateShipmentNumber(): String {
        return "SHP-${java.time.Year.now()}-${String.format("%05d", (Math.random() * 100000).toInt())}"
    }

    /**
     * Получает отгрузку по ID
     * @param shipmentId ID отгрузки
     * @return Отгрузка
     */
    fun getShipmentById(shipmentId: Long): Shipment {
        return shipmentRepository.findById(shipmentId)
            .orElseThrow { RuntimeException("Shipment not found: $shipmentId") }
    }

    /**
     * Получает отгрузку по номеру
     * @param shipmentNumber Номер отгрузки
     * @return Отгрузка
     */
    fun getShipmentByNumber(shipmentNumber: String): Shipment? {
        return shipmentRepository.findByShipmentNumber(shipmentNumber)
    }

    /**
     * Получает все отгрузки
     * @return Список всех отгрузок
     */
    fun getAllShipments(): List<Shipment> {
        return shipmentRepository.findAll()
    }

    /**
     * Получает отгрузки по статусу
     * @param status Статус отгрузки
     * @return Список отгрузок
     */
    fun getShipmentsByStatus(status: ShipmentStatus): List<Shipment> {
        return shipmentRepository.findByStatus(status)
    }

    /**
     * Получает отгрузки по ID заказа
     * @param orderId ID заказа
     * @return Список отгрузок
     */
    fun getShipmentsByOrderId(orderId: Long): List<Shipment> {
        return shipmentRepository.findByOrderId(orderId)
    }

    /**
     * Обновляет статус отгрузки
     * @param shipmentId ID отгрузки
     * @param status Новый статус
     * @return Обновленная отгрузка
     */
    @Transactional
    fun updateShipmentStatus(shipmentId: Long, status: ShipmentStatus): Shipment {
        val shipment = getShipmentById(shipmentId)
        shipment.status = status
        shipment.updatedAt = Instant.now()
        return shipmentRepository.save(shipment)
    }
}
