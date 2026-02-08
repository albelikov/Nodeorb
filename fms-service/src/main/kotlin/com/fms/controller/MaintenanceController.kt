package com.fms.controller

import com.fms.model.VehicleMaintenance
import com.fms.model.VehicleMaintenanceRequest
import com.fms.service.MaintenanceService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/maintenance")
class MaintenanceController(private val maintenanceService: MaintenanceService) {

    companion object {
        private val logger = LoggerFactory.getLogger(MaintenanceController::class.java)
    }

    /**
     * Создает заявку на обслуживание
     * @param request Данные о заявке
     * @return Созданная заявка
     */
    @PostMapping("/requests")
    fun createMaintenanceRequest(@RequestBody request: VehicleMaintenanceRequest): ResponseEntity<VehicleMaintenanceRequest> {
        logger.info("Received request to create maintenance request for vehicle: ${request.vehicleId}")
        try {
            val createdRequest = maintenanceService.createMaintenanceRequest(request)
            return ResponseEntity.ok(createdRequest)
        } catch (e: Exception) {
            logger.error("Error creating maintenance request for vehicle: ${request.vehicleId}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает заявку на обслуживание по ID
     * @param id ID заявки
     * @return Заявка на обслуживание
     */
    @GetMapping("/requests/{id}")
    fun getMaintenanceRequestById(@PathVariable id: UUID): ResponseEntity<VehicleMaintenanceRequest> {
        logger.info("Received request to get maintenance request by ID: $id")
        try {
            val request = maintenanceService.getMaintenanceRequestById(id)
            return ResponseEntity.ok(request)
        } catch (e: Exception) {
            logger.error("Error getting maintenance request by ID: $id", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает все заявки на обслуживание
     * @return Список заявок
     */
    @GetMapping("/requests")
    fun getAllMaintenanceRequests(): ResponseEntity<List<VehicleMaintenanceRequest>> {
        logger.info("Received request to get all maintenance requests")
        try {
            val requests = maintenanceService.getAllMaintenanceRequests()
            return ResponseEntity.ok(requests)
        } catch (e: Exception) {
            logger.error("Error getting all maintenance requests", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает заявки на обслуживание по ID транспортного средства
     * @param vehicleId ID транспортного средства
     * @return Список заявок
     */
    @GetMapping("/requests/by-vehicle/{vehicleId}")
    fun getMaintenanceRequestsByVehicleId(@PathVariable vehicleId: UUID): ResponseEntity<List<VehicleMaintenanceRequest>> {
        logger.info("Received request to get maintenance requests by vehicle ID: $vehicleId")
        try {
            val requests = maintenanceService.getMaintenanceRequestsByVehicleId(vehicleId)
            return ResponseEntity.ok(requests)
        } catch (e: Exception) {
            logger.error("Error getting maintenance requests by vehicle ID: $vehicleId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает заявки на обслуживание по статусу
     * @param status Статус заявки
     * @return Список заявок
     */
    @GetMapping("/requests/by-status/{status}")
    fun getMaintenanceRequestsByStatus(@PathVariable status: String): ResponseEntity<List<VehicleMaintenanceRequest>> {
        logger.info("Received request to get maintenance requests by status: $status")
        try {
            val requests = maintenanceService.getMaintenanceRequestsByStatus(status)
            return ResponseEntity.ok(requests)
        } catch (e: Exception) {
            logger.error("Error getting maintenance requests by status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет статус заявки на обслуживание
     * @param id ID заявки
     * @param status Новый статус
     * @return Обновленная заявка
     */
    @PutMapping("/requests/{id}/status")
    fun updateMaintenanceRequestStatus(@PathVariable id: UUID, @RequestBody status: String): ResponseEntity<VehicleMaintenanceRequest> {
        logger.info("Received request to update maintenance request status: $id, status: $status")
        try {
            val updatedRequest = maintenanceService.updateMaintenanceRequestStatus(id, status)
            return ResponseEntity.ok(updatedRequest)
        } catch (e: Exception) {
            logger.error("Error updating maintenance request status: $id, status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Создает запись о обслуживании
     * @param maintenance Данные об обслуживании
     * @return Созданная запись
     */
    @PostMapping
    fun createMaintenance(@RequestBody maintenance: VehicleMaintenance): ResponseEntity<VehicleMaintenance> {
        logger.info("Received request to create maintenance record for vehicle: ${maintenance.vehicleId}")
        try {
            val createdMaintenance = maintenanceService.createMaintenance(maintenance)
            return ResponseEntity.ok(createdMaintenance)
        } catch (e: Exception) {
            logger.error("Error creating maintenance record for vehicle: ${maintenance.vehicleId}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает запись об обслуживании по ID
     * @param id ID записи
     * @return Запись об обслуживании
     */
    @GetMapping("/{id}")
    fun getMaintenanceById(@PathVariable id: UUID): ResponseEntity<VehicleMaintenance> {
        logger.info("Received request to get maintenance record by ID: $id")
        try {
            val maintenance = maintenanceService.getMaintenanceById(id)
            return ResponseEntity.ok(maintenance)
        } catch (e: Exception) {
            logger.error("Error getting maintenance record by ID: $id", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает все записи об обслуживании
     * @return Список записей
     */
    @GetMapping
    fun getAllMaintenances(): ResponseEntity<List<VehicleMaintenance>> {
        logger.info("Received request to get all maintenance records")
        try {
            val maintenances = maintenanceService.getAllMaintenances()
            return ResponseEntity.ok(maintenances)
        } catch (e: Exception) {
            logger.error("Error getting all maintenance records", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает записи об обслуживании по ID транспортного средства
     * @param vehicleId ID транспортного средства
     * @return Список записей
     */
    @GetMapping("/by-vehicle/{vehicleId}")
    fun getMaintenancesByVehicleId(@PathVariable vehicleId: UUID): ResponseEntity<List<VehicleMaintenance>> {
        logger.info("Received request to get maintenance records by vehicle ID: $vehicleId")
        try {
            val maintenances = maintenanceService.getMaintenancesByVehicleId(vehicleId)
            return ResponseEntity.ok(maintenances)
        } catch (e: Exception) {
            logger.error("Error getting maintenance records by vehicle ID: $vehicleId", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает записи об обслуживании по статусу
     * @param status Статус обслуживания
     * @return Список записей
     */
    @GetMapping("/by-status/{status}")
    fun getMaintenancesByStatus(@PathVariable status: String): ResponseEntity<List<VehicleMaintenance>> {
        logger.info("Received request to get maintenance records by status: $status")
        try {
            val maintenances = maintenanceService.getMaintenancesByStatus(status)
            return ResponseEntity.ok(maintenances)
        } catch (e: Exception) {
            logger.error("Error getting maintenance records by status: $status", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Обновляет запись об обслуживании
     * @param id ID записи
     * @param maintenance Данные для обновления
     * @return Обновленная запись
     */
    @PutMapping("/{id}")
    fun updateMaintenance(@PathVariable id: UUID, @RequestBody maintenance: VehicleMaintenance): ResponseEntity<VehicleMaintenance> {
        logger.info("Received request to update maintenance record: $id")
        try {
            val updatedMaintenance = maintenanceService.updateMaintenance(id, maintenance)
            return ResponseEntity.ok(updatedMaintenance)
        } catch (e: Exception) {
            logger.error("Error updating maintenance record: $id", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}