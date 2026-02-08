package com.fms.service

import com.fms.model.Vehicle
import com.fms.repository.VehicleRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class VehicleService(private val vehicleRepository: VehicleRepository) {

    companion object {
        private val logger = LoggerFactory.getLogger(VehicleService::class.java)
    }

    /**
     * Создает новое транспортное средство
     * @param vehicle Данные о транспортном средстве
     * @return Созданное транспортное средство
     */
    @Transactional
    fun createVehicle(vehicle: Vehicle): Vehicle {
        logger.info("Creating vehicle: ${vehicle.vehicleNumber}")
        vehicle.createdAt = LocalDateTime.now()
        vehicle.updatedAt = LocalDateTime.now()
        return vehicleRepository.save(vehicle)
    }

    /**
     * Получает транспортное средство по ID
     * @param id ID транспортного средства
     * @return Транспортное средство
     */
    fun getVehicleById(id: UUID): Vehicle {
        return vehicleRepository.findById(id)
            .orElseThrow { RuntimeException("Vehicle not found: $id") }
    }

    /**
     * Получает транспортное средство по номеру
     * @param vehicleNumber Номер транспортного средства
     * @return Транспортное средство
     */
    fun getVehicleByNumber(vehicleNumber: String): Vehicle? {
        return vehicleRepository.findByVehicleNumber(vehicleNumber)
    }

    /**
     * Получает транспортное средство по VIN
     * @param vin VIN номер
     * @return Транспортное средство
     */
    fun getVehicleByVin(vin: String): Vehicle? {
        return vehicleRepository.findByVin(vin)
    }

    /**
     * Получает все транспортные средства
     * @return Список всех транспортных средств
     */
    fun getAllVehicles(): List<Vehicle> {
        return vehicleRepository.findAll()
    }

    /**
     * Получает транспортные средства по статусу
     * @param status Статус транспортного средства
     * @return Список транспортных средств
     */
    fun getVehiclesByStatus(status: String): List<Vehicle> {
        return vehicleRepository.findByStatus(status)
    }

    /**
     * Получает транспортные средства по типу
     * @param vehicleType Тип транспортного средства
     * @return Список транспортных средств
     */
    fun getVehiclesByType(vehicleType: String): List<Vehicle> {
        return vehicleRepository.findByVehicleType(vehicleType)
    }

    /**
     * Обновляет транспортное средство
     * @param id ID транспортного средства
     * @param vehicle Данные для обновления
     * @return Обновленное транспортное средство
     */
    @Transactional
    fun updateVehicle(id: UUID, vehicle: Vehicle): Vehicle {
        logger.info("Updating vehicle: $id")
        val existingVehicle = getVehicleById(id)
        existingVehicle.vehicleNumber = vehicle.vehicleNumber
        existingVehicle.vehicleType = vehicle.vehicleType
        existingVehicle.make = vehicle.make
        existingVehicle.model = vehicle.model
        existingVehicle.year = vehicle.year
        existingVehicle.vin = vehicle.vin
        existingVehicle.registrationNumber = vehicle.registrationNumber
        existingVehicle.currentMileage = vehicle.currentMileage
        existingVehicle.fuelType = vehicle.fuelType
        existingVehicle.fuelLevel = vehicle.fuelLevel
        existingVehicle.capacity = vehicle.capacity
        existingVehicle.volume = vehicle.volume
        existingVehicle.bodyType = vehicle.bodyType
        existingVehicle.status = vehicle.status
        existingVehicle.documents = vehicle.documents
        existingVehicle.photos = vehicle.photos
        existingVehicle.updatedAt = LocalDateTime.now()
        return vehicleRepository.save(existingVehicle)
    }

    /**
     * Удаляет транспортное средство
     * @param id ID транспортного средства
     */
    @Transactional
    fun deleteVehicle(id: UUID) {
        logger.info("Deleting vehicle: $id")
        val vehicle = getVehicleById(id)
        vehicleRepository.delete(vehicle)
    }

    /**
     * Обновляет статус транспортного средства
     * @param id ID транспортного средства
     * @param status Новый статус
     * @return Обновленное транспортное средство
     */
    @Transactional
    fun updateVehicleStatus(id: UUID, status: String): Vehicle {
        logger.info("Updating vehicle status: $id, $status")
        val vehicle = getVehicleById(id)
        vehicle.status = status
        vehicle.updatedAt = LocalDateTime.now()
        return vehicleRepository.save(vehicle)
    }
}