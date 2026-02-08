package com.fms.controller

import com.fms.model.Driver
import com.fms.service.DriverService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/drivers")
class DriverController(private val driverService: DriverService) {

    companion object {
        private val logger = LoggerFactory.getLogger(DriverController::class.java)
    }

    /**
     * Создает нового водителя
     * @param driver Данные о водителе
     * @return Созданный водитель
     */
    @PostMapping
    fun createDriver(@RequestBody driver: Driver): ResponseEntity<Driver> {
        logger.info("Received request to create driver: ${driver.firstName} ${driver.lastName}")
        try {
            val createdDriver = driverService.createDriver(driver)
            return ResponseEntity.ok(createdDriver)
        } catch (e: Exception) {
            logger.error("Error creating driver: ${driver.firstName} ${driver.lastName}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает водителя по ID
     * @param id ID водителя
     * @return Водитель
     */
    @GetMapping("/{id}")
    fun getDriverById(@PathVariable id: UUID): ResponseEntity<Driver> {
        logger.info("Received request to get driver by ID: $id")
        try {
            val driver = driverService.getDriverById(id)
            return ResponseEntity.ok(driver)
        } catch (e: Exception) {
            logger.error("Error getting driver by ID: $id", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает водителя по номеру водительского удостоверения
     * @param driverLicenseNumber Номер водительского удостоверения
     * @return Водитель
     */
    @GetMapping("/by-license/{driverLicenseNumber}")
    fun getDriverByLicenseNumber(@PathVariable driverLicenseNumber: String): ResponseEntity<Driver> {
        logger.info("Received request to get driver by license number: $driverLicenseNumber")
        try {
            val driver = driverService.getDriverByLicenseNumber(driverLicenseNumber)
            return if (driver != null) {
                ResponseEntity.ok(driver)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            }
        } catch (e: Exception) {
            logger.error("Error getting driver by license number: $driverLicenseNumber", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает водителя по номеру телефона
     * @param phoneNumber Номер телефона
     * @return Водитель
     */
    @GetMapping("/by-phone/{phoneNumber}")
    fun getDriverByPhoneNumber(@PathVariable phoneNumber: String): ResponseEntity<Driver> {
        logger.info("Received request to get driver by phone number: $phoneNumber")
        try {
            val driver = driverService.getDriverByPhoneNumber(phoneNumber)
            return if (driver != null) {
                ResponseEntity.ok(driver)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            }
        } catch (e: Exception) {
            logger.error("Error getting driver by phone number: $phoneNumber", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает всех водителей
     * @return Список водителей
     */
    @GetMapping
    fun getAllDrivers(): ResponseEntity<List<Driver>> {
        logger.info("Received request to get all drivers")
        try {
            val drivers = driverService.getAllDrivers()
            return ResponseEntity.ok(drivers)
        } catch (e: Exception) {
            logger.error("Error getting all drivers", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает водителей по статусу
     * @param status Статус водителя
     * @return Список водителей
     */
    @GetMapping("/by-status/{status}")
    fun getDriversByStatus(@PathVariable status: String): ResponseEntity<List<Driver>> {
        logger.info("Received request to get drivers by status: $status")
        try {
            val drivers = driverService.getDriversByStatus(status)
            return ResponseEntity.ok(drivers)
        } catch (e: Exception) {
            logger.error("Error getting drivers by status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет водителя
     * @param id ID водителя
     * @param driver Данные для обновления
     * @return Обновленный водитель
     */
    @PutMapping("/{id}")
    fun updateDriver(@PathVariable id: UUID, @RequestBody driver: Driver): ResponseEntity<Driver> {
        logger.info("Received request to update driver: $id")
        try {
            val updatedDriver = driverService.updateDriver(id, driver)
            return ResponseEntity.ok(updatedDriver)
        } catch (e: Exception) {
            logger.error("Error updating driver: $id", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет статус водителя
     * @param id ID водителя
     * @param status Новый статус
     * @return Обновленный водитель
     */
    @PutMapping("/{id}/status")
    fun updateDriverStatus(@PathVariable id: UUID, @RequestBody status: String): ResponseEntity<Driver> {
        logger.info("Received request to update driver status: $id, status: $status")
        try {
            val updatedDriver = driverService.updateDriverStatus(id, status)
            return ResponseEntity.ok(updatedDriver)
        } catch (e: Exception) {
            logger.error("Error updating driver status: $id, status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Удаляет водителя
     * @param id ID водителя
     * @return Ответ о результате операции
     */
    @DeleteMapping("/{id}")
    fun deleteDriver(@PathVariable id: UUID): ResponseEntity<Unit> {
        logger.info("Received request to delete driver: $id")
        try {
            driverService.deleteDriver(id)
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Error deleting driver: $id", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}