package com.fms.service

import com.fms.model.VehicleMaintenance
import com.fms.model.VehicleMaintenanceRequest
import com.fms.repository.VehicleMaintenanceRepository
import com.fms.repository.VehicleMaintenanceRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class MaintenanceService(
    private val maintenanceRequestRepository: VehicleMaintenanceRequestRepository,
    private val maintenanceRepository: VehicleMaintenanceRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(MaintenanceService::class.java)
    }

    /**
     * Создает заявку на обслуживание
     * @param request Данные о заявке
     * @return Созданная заявка
     */
    @Transactional
    fun createMaintenanceRequest(request: VehicleMaintenanceRequest): VehicleMaintenanceRequest {
        logger.info("Creating maintenance request for vehicle: ${request.vehicleId}")
        request.createdAt = LocalDateTime.now()
        request.updatedAt = LocalDateTime.now()
        return maintenanceRequestRepository.save(request)
    }

    /**
     * Получает заявку на обслуживание по ID
     * @param id ID заявки
     * @return Заявка на обслуживание
     */
    fun getMaintenanceRequestById(id: UUID): VehicleMaintenanceRequest {
        return maintenanceRequestRepository.findById(id)
            .orElseThrow { RuntimeException("Maintenance request not found: $id") }
    }

    /**
     * Получает все заявки на обслуживание
     * @return Список всех заявок
     */
    fun getAllMaintenanceRequests(): List<VehicleMaintenanceRequest> {
        return maintenanceRequestRepository.findAll()
    }

    /**
     * Получает заявки на обслуживание по ID транспортного средства
     * @param vehicleId ID транспортного средства
     * @return Список заявок
     */
    fun getMaintenanceRequestsByVehicleId(vehicleId: UUID): List<VehicleMaintenanceRequest> {
        return maintenanceRequestRepository.findByVehicleId(vehicleId)
    }

    /**
     * Получает заявки на обслуживание по статусу
     * @param status Статус заявки
     * @return Список заявок
     */
    fun getMaintenanceRequestsByStatus(status: String): List<VehicleMaintenanceRequest> {
        return maintenanceRequestRepository.findByStatus(status)
    }

    /**
     * Обновляет статус заявки на обслуживание
     * @param id ID заявки
     * @param status Новый статус
     * @return Обновленная заявка
     */
    @Transactional
    fun updateMaintenanceRequestStatus(id: UUID, status: String): VehicleMaintenanceRequest {
        logger.info("Updating maintenance request status: $id, $status")
        val request = getMaintenanceRequestById(id)
        request.status = status
        request.updatedAt = LocalDateTime.now()
        return maintenanceRequestRepository.save(request)
    }

    /**
     * Создает запись о проведенном обслуживании
     * @param maintenance Данные об обслуживании
     * @return Созданная запись
     */
    @Transactional
    fun createMaintenance(maintenance: VehicleMaintenance): VehicleMaintenance {
        logger.info("Creating maintenance record for vehicle: ${maintenance.vehicleId}")
        maintenance.createdAt = LocalDateTime.now()
        maintenance.updatedAt = LocalDateTime.now()
        return maintenanceRepository.save(maintenance)
    }

    /**
     * Получает запись об обслуживании по ID
     * @param id ID записи
     * @return Запись об обслуживании
     */
    fun getMaintenanceById(id: UUID): VehicleMaintenance {
        return maintenanceRepository.findById(id)
            .orElseThrow { RuntimeException("Maintenance record not found: $id") }
    }

    /**
     * Получает все записи об обслуживании
     * @return Список всех записей
     */
    fun getAllMaintenances(): List<VehicleMaintenance> {
        return maintenanceRepository.findAll()
    }

    /**
     * Получает записи об обслуживании по ID транспортного средства
     * @param vehicleId ID транспортного средства
     * @return Список записей
     */
    fun getMaintenancesByVehicleId(vehicleId: UUID): List<VehicleMaintenance> {
        return maintenanceRepository.findByVehicleId(vehicleId)
    }

    /**
     * Получает записи об обслуживании по статусу
     * @param status Статус обслуживания
     * @return Список записей
     */
    fun getMaintenancesByStatus(status: String): List<VehicleMaintenance> {
        return maintenanceRepository.findByStatus(status)
    }

    /**
     * Обновляет запись об обслуживании
     * @param id ID записи
     * @param maintenance Данные для обновления
     * @return Обновленная запись
     */
    @Transactional
    fun updateMaintenance(id: UUID, maintenance: VehicleMaintenance): VehicleMaintenance {
        logger.info("Updating maintenance record: $id")
        val existingMaintenance = getMaintenanceById(id)
        existingMaintenance.maintenanceType = maintenance.maintenanceType
        existingMaintenance.description = maintenance.description
        existingMaintenance.startDate = maintenance.startDate
        existingMaintenance.endDate = maintenance.endDate
        existingMaintenance.cost = maintenance.cost
        existingMaintenance.partsUsed = maintenance.partsUsed
        existingMaintenance.serviceCenter = maintenance.serviceCenter
        existingMaintenance.technician = maintenance.technician
        existingMaintenance.status = maintenance.status
        existingMaintenance.updatedAt = LocalDateTime.now()
        return maintenanceRepository.save(existingMaintenance)
    }
}