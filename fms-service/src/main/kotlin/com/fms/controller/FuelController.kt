package com.fms.controller

import com.fms.model.FuelConsumption
import com.fms.model.FuelRefueling
import com.fms.service.FuelService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/fuel")
class FuelController(private val fuelService: FuelService) {

    companion object {
        private val logger = LoggerFactory.getLogger(FuelController::class.java)
    }

    /**
     * Создает запись о заправке топливом
     * @param refueling Данные о заправке
     * @return Созданная запись
     */
    @PostMapping("/refueling")
    fun createFuelRefueling(@RequestBody refueling: FuelRefueling): ResponseEntity<FuelRefueling> {
        logger.info("Received request to create fuel refueling record for vehicle: ${refueling.vehicleId}")
        try {
            val createdRefueling = fuelService.createFuelRefueling(refueling)
            return ResponseEntity.ok(createdRefueling)
        } catch (e: Exception) {
            logger.error("Error creating fuel refueling record for vehicle: ${refueling.vehicleId}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает запись о заправке по ID
     * @param id ID записи
     * @return Запись о заправке
     */
    @GetMapping("/refueling/{id}")
    fun getFuelRefuelingById(@PathVariable id: UUID): ResponseEntity<FuelRefueling> {
        logger.info("Received request to get fuel refueling record by ID: $id")
        try {
            val refueling = fuelService.getFuelRefuelingById(id)
            return ResponseEntity.ok(refueling)
        } catch (e: Exception) {
            logger.error("Error getting fuel refueling record by ID: $id", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает все записи о заправках
     * @return Список записей
     */
    @GetMapping("/refueling")
    fun getAllFuelRefuelings(): ResponseEntity<List<FuelRefueling>> {
        logger.info("Received request to get all fuel refueling records")
        try {
            val refuelings = fuelService.getAllFuelRefuelings()
            return ResponseEntity.ok(refuelings)
        } catch (e: Exception) {
            logger.error("Error getting all fuel refueling records", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает записи о заправках по ID транспортного средства
     * @param vehicleId ID транспортного средства
     * @return Список записей
     */
    @GetMapping("/refueling/by-vehicle/{vehicleId}")
    fun getFuelRefuelingsByVehicleId(@PathVariable vehicleId: UUID): ResponseEntity<List<FuelRefueling>> {
        logger.info("Received request to get fuel refueling records by vehicle ID: $vehicleId")
        try {
            val refuelings = fuelService.getFuelRefuelingsByVehicleId(vehicleId)
            return ResponseEntity.ok(refuelings)
        } catch (e: Exception) {
            logger.error("Error getting fuel refueling records by vehicle ID: $vehicleId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает записи о заправках по ID водителя
     * @param driverId ID водителя
     * @return Список записей
     */
    @GetMapping("/refueling/by-driver/{driverId}")
    fun getFuelRefuelingsByDriverId(@PathVariable driverId: UUID): ResponseEntity<List<FuelRefueling>> {
        logger.info("Received request to get fuel refueling records by driver ID: $driverId")
        try {
            val refuelings = fuelService.getFuelRefuelingsByDriverId(driverId)
            return ResponseEntity.ok(refuelings)
        } catch (e: Exception) {
            logger.error("Error getting fuel refueling records by driver ID: $driverId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Создает запись о расходе топлива
     * @param consumption Данные о расходе
     * @return Созданная запись
     */
    @PostMapping("/consumption")
    fun createFuelConsumption(@RequestBody consumption: FuelConsumption): ResponseEntity<FuelConsumption> {
        logger.info("Received request to create fuel consumption record for vehicle: ${consumption.vehicleId}")
        try {
            val createdConsumption = fuelService.createFuelConsumption(consumption)
            return ResponseEntity.ok(createdConsumption)
        } catch (e: Exception) {
            logger.error("Error creating fuel consumption record for vehicle: ${consumption.vehicleId}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает запись о расходе топлива по ID
     * @param id ID записи
     * @return Запись о расходе
     */
    @GetMapping("/consumption/{id}")
    fun getFuelConsumptionById(@PathVariable id: UUID): ResponseEntity<FuelConsumption> {
        logger.info("Received request to get fuel consumption record by ID: $id")
        try {
            val consumption = fuelService.getFuelConsumptionById(id)
            return ResponseEntity.ok(consumption)
        } catch (e: Exception) {
            logger.error("Error getting fuel consumption record by ID: $id", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает все записи о расходе топлива
     * @return Список записей
     */
    @GetMapping("/consumption")
    fun getAllFuelConsumptions(): ResponseEntity<List<FuelConsumption>> {
        logger.info("Received request to get all fuel consumption records")
        try {
            val consumptions = fuelService.getAllFuelConsumptions()
            return ResponseEntity.ok(consumptions)
        } catch (e: Exception) {
            logger.error("Error getting all fuel consumption records", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает записи о расходе топлива по ID транспортного средства
     * @param vehicleId ID транспортного средства
     * @return Список записей
     */
    @GetMapping("/consumption/by-vehicle/{vehicleId}")
    fun getFuelConsumptionsByVehicleId(@PathVariable vehicleId: UUID): ResponseEntity<List<FuelConsumption>> {
        logger.info("Received request to get fuel consumption records by vehicle ID: $vehicleId")
        try {
            val consumptions = fuelService.getFuelConsumptionsByVehicleId(vehicleId)
            return ResponseEntity.ok(consumptions)
        } catch (e: Exception) {
            logger.error("Error getting fuel consumption records by vehicle ID: $vehicleId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает записи о расходе топлива по ID водителя
     * @param driverId ID водителя
     * @return Список записей
     */
    @GetMapping("/consumption/by-driver/{driverId}")
    fun getFuelConsumptionsByDriverId(@PathVariable driverId: UUID): ResponseEntity<List<FuelConsumption>> {
        logger.info("Received request to get fuel consumption records by driver ID: $driverId")
        try {
            val consumptions = fuelService.getFuelConsumptionsByDriverId(driverId)
            return ResponseEntity.ok(consumptions)
        } catch (e: Exception) {
            logger.error("Error getting fuel consumption records by driver ID: $driverId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает записи о расходе топлива по ID рейса
     * @param tripId ID рейса
     * @return Список записей
     */
    @GetMapping("/consumption/by-trip/{tripId}")
    fun getFuelConsumptionsByTripId(@PathVariable tripId: UUID): ResponseEntity<List<FuelConsumption>> {
        logger.info("Received request to get fuel consumption records by trip ID: $tripId")
        try {
            val consumptions = fuelService.getFuelConsumptionsByTripId(tripId)
            return ResponseEntity.ok(consumptions)
        } catch (e: Exception) {
            logger.error("Error getting fuel consumption records by trip ID: $tripId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}