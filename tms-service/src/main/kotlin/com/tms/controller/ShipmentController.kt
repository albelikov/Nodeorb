package com.tms.controller

import com.tms.dto.ShipmentDto
import com.tms.model.Shipment
import com.tms.model.ShipmentStatus
import com.tms.service.ShipmentService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/shipments")
class ShipmentController(
    private val shipmentService: ShipmentService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ShipmentController::class.java)
    }

    /**
     * Создает новую отгрузку
     * @param shipmentDto Детали отгрузки
     * @return Созданная отгрузка
     */
    @PostMapping
    fun createShipment(@RequestBody shipmentDto: ShipmentDto): ResponseEntity<Shipment> {
        logger.info("Received request to create shipment: $shipmentDto")
        try {
            val shipment = shipmentService.createShipment(shipmentDto)
            return ResponseEntity.ok(shipment)
        } catch (e: Exception) {
            logger.error("Error creating shipment: $shipmentDto", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает отгрузку по ID
     * @param shipmentId ID отгрузки
     * @return Отгрузка
     */
    @GetMapping("/{shipmentId}")
    fun getShipmentById(@PathVariable shipmentId: Long): ResponseEntity<Shipment> {
        logger.info("Received request to get shipment by ID: $shipmentId")
        try {
            val shipment = shipmentService.getShipmentById(shipmentId)
            return ResponseEntity.ok(shipment)
        } catch (e: Exception) {
            logger.error("Error getting shipment by ID: $shipmentId", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает отгрузку по номеру
     * @param shipmentNumber Номер отгрузки
     * @return Отгрузка
     */
    @GetMapping("/by-number/{shipmentNumber}")
    fun getShipmentByNumber(@PathVariable shipmentNumber: String): ResponseEntity<Shipment> {
        logger.info("Received request to get shipment by number: $shipmentNumber")
        try {
            val shipment = shipmentService.getShipmentByNumber(shipmentNumber)
            return if (shipment != null) {
                ResponseEntity.ok(shipment)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            }
        } catch (e: Exception) {
            logger.error("Error getting shipment by number: $shipmentNumber", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает все отгрузки
     * @return Список отгрузок
     */
    @GetMapping
    fun getAllShipments(): ResponseEntity<List<Shipment>> {
        logger.info("Received request to get all shipments")
        try {
            val shipments = shipmentService.getAllShipments()
            return ResponseEntity.ok(shipments)
        } catch (e: Exception) {
            logger.error("Error getting all shipments", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает отгрузки по статусу
     * @param status Статус отгрузки
     * @return Список отгрузок
     */
    @GetMapping("/by-status/{status}")
    fun getShipmentsByStatus(@PathVariable status: ShipmentStatus): ResponseEntity<List<Shipment>> {
        logger.info("Received request to get shipments by status: $status")
        try {
            val shipments = shipmentService.getShipmentsByStatus(status)
            return ResponseEntity.ok(shipments)
        } catch (e: Exception) {
            logger.error("Error getting shipments by status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает отгрузки по ID заказа
     * @param orderId ID заказа
     * @return Список отгрузок
     */
    @GetMapping("/by-order/{orderId}")
    fun getShipmentsByOrderId(@PathVariable orderId: Long): ResponseEntity<List<Shipment>> {
        logger.info("Received request to get shipments by order ID: $orderId")
        try {
            val shipments = shipmentService.getShipmentsByOrderId(orderId)
            return ResponseEntity.ok(shipments)
        } catch (e: Exception) {
            logger.error("Error getting shipments by order ID: $orderId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет статус отгрузки
     * @param shipmentId ID отгрузки
     * @param status Новый статус
     * @return Обновленная отгрузка
     */
    @PutMapping("/{shipmentId}/status")
    fun updateShipmentStatus(
        @PathVariable shipmentId: Long,
        @RequestBody status: ShipmentStatus
    ): ResponseEntity<Shipment> {
        logger.info("Received request to update shipment status: $shipmentId, status: $status")
        try {
            val shipment = shipmentService.updateShipmentStatus(shipmentId, status)
            return ResponseEntity.ok(shipment)
        } catch (e: Exception) {
            logger.error("Error updating shipment status: $shipmentId, status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}
