package com.fms.controller

import com.fms.model.Vehicle
import com.fms.service.VehicleService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/vehicles")
class VehicleController(private val vehicleService: VehicleService) {

    companion object {
        private val logger = LoggerFactory.getLogger(VehicleController::class.java)
    }

    /**
     * Создает новое транспортное средство
     * @param vehicle Данные о транспортном средстве
     * @return Созданное транспортное средство
     */
    @PostMapping
    fun createVehicle(@RequestBody vehicle: Vehicle): ResponseEntity<Vehicle> {
        logger.info("Received request to create vehicle: ${vehicle.vehicleNumber}")
        try {
            val createdVehicle = vehicleService.createVehicle(vehicle)
            return ResponseEntity.ok(createdVehicle)
        } catch (e: Exception) {
            logger.error("Error creating vehicle: ${vehicle.vehicleNumber}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает транспортное средство по ID
     * @param id ID транспортного средства
     * @return Транспортное средство
     */
    @GetMapping("/{id}")
    fun getVehicleById(@PathVariable id: UUID): ResponseEntity<Vehicle> {
        logger.info("Received request to get vehicle by ID: $id")
        try {
            val vehicle = vehicleService.getVehicleById(id)
            return ResponseEntity.ok(vehicle)
        } catch (e: Exception) {
            logger.error("Error getting vehicle by ID: $id", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает транспортное средство по номеру
     * @param vehicleNumber Номер транспортного средства
     * @return Транспортное средство
     */
    @GetMapping("/by-number/{vehicleNumber}")
    fun getVehicleByNumber(@PathVariable vehicleNumber: String): ResponseEntity<Vehicle> {
        logger.info("Received request to get vehicle by number: $vehicleNumber")
        try {
            val vehicle = vehicleService.getVehicleByNumber(vehicleNumber)
            return if (vehicle != null) {
                ResponseEntity.ok(vehicle)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            }
        } catch (e: Exception) {
            logger.error("Error getting vehicle by number: $vehicleNumber", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает транспортное средство по VIN
     * @param vin VIN номер
     * @return Транспортное средство
     */
    @GetMapping("/by-vin/{vin}")
    fun getVehicleByVin(@PathVariable vin: String): ResponseEntity<Vehicle> {
        logger.info("Received request to get vehicle by VIN: $vin")
        try {
            val vehicle = vehicleService.getVehicleByVin(vin)
            return if (vehicle != null) {
                ResponseEntity.ok(vehicle)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            }
        } catch (e: Exception) {
            logger.error("Error getting vehicle by VIN: $vin", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает все транспортные средства
     * @return Список транспортных средств
     */
    @GetMapping
    fun getAllVehicles(): ResponseEntity<List<Vehicle>> {
        logger.info("Received request to get all vehicles")
        try {
            val vehicles = vehicleService.getAllVehicles()
            return ResponseEntity.ok(vehicles)
        } catch (e: Exception) {
            logger.error("Error getting all vehicles", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает транспортные средства по статусу
     * @param status Статус транспортного средства
     * @return Список транспортных средств
     */
    @GetMapping("/by-status/{status}")
    fun getVehiclesByStatus(@PathVariable status: String): ResponseEntity<List<Vehicle>> {
        logger.info("Received request to get vehicles by status: $status")
        try {
            val vehicles = vehicleService.getVehiclesByStatus(status)
            return ResponseEntity.ok(vehicles)
        } catch (e: Exception) {
            logger.error("Error getting vehicles by status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает транспортные средства по типу
     * @param vehicleType Тип транспортного средства
     * @return Список транспортных средств
     */
    @GetMapping("/by-type/{vehicleType}")
    fun getVehiclesByType(@PathVariable vehicleType: String): ResponseEntity<List<Vehicle>> {
        logger.info("Received request to get vehicles by type: $vehicleType")
        try {
            val vehicles = vehicleService.getVehiclesByType(vehicleType)
            return ResponseEntity.ok(vehicles)
        } catch (e: Exception) {
            logger.error("Error getting vehicles by type: $vehicleType", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет транспортное средство
     * @param id ID транспортного средства
     * @param vehicle Данные для обновления
     * @return Обновленное транспортное средство
     */
    @PutMapping("/{id}")
    fun updateVehicle(@PathVariable id: UUID, @RequestBody vehicle: Vehicle): ResponseEntity<Vehicle> {
        logger.info("Received request to update vehicle: $id")
        try {
            val updatedVehicle = vehicleService.updateVehicle(id, vehicle)
            return ResponseEntity.ok(updatedVehicle)
        } catch (e: Exception) {
            logger.error("Error updating vehicle: $id", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет статус транспортного средства
     * @param id ID транспортного средства
     * @param status Новый статус
     * @return Обновленное транспортное средство
     */
    @PutMapping("/{id}/status")
    fun updateVehicleStatus(@PathVariable id: UUID, @RequestBody status: String): ResponseEntity<Vehicle> {
        logger.info("Received request to update vehicle status: $id, status: $status")
        try {
            val updatedVehicle = vehicleService.updateVehicleStatus(id, status)
            return ResponseEntity.ok(updatedVehicle)
        } catch (e: Exception) {
            logger.error("Error updating vehicle status: $id, status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Удаляет транспортное средство
     * @param id ID транспортного средства
     * @return Ответ о результате операции
     */
    @DeleteMapping("/{id}")
    fun deleteVehicle(@PathVariable id: UUID): ResponseEntity<Unit> {
        logger.info("Received request to delete vehicle: $id")
        try {
            vehicleService.deleteVehicle(id)
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Error deleting vehicle: $id", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}