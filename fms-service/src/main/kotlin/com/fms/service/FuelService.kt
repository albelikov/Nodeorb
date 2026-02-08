package com.fms.service

import com.fms.model.FuelConsumption
import com.fms.model.FuelRefueling
import com.fms.repository.FuelConsumptionRepository
import com.fms.repository.FuelRefuelingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class FuelService(
    private val refuelingRepository: FuelRefuelingRepository,
    private val consumptionRepository: FuelConsumptionRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(FuelService::class.java)
    }

    /**
     * Создает запись о заправке топливом
     * @param refueling Данные о заправке
     * @return Созданная запись
     */
    @Transactional
    fun createFuelRefueling(refueling: FuelRefueling): FuelRefueling {
        logger.info("Creating fuel refueling record for vehicle: ${refueling.vehicleId}")
        refueling.createdAt = LocalDateTime.now()
        return refuelingRepository.save(refueling)
    }

    /**
     * Получает запись о заправке по ID
     * @param id ID записи
     * @return Запись о заправке
     */
    fun getFuelRefuelingById(id: UUID): FuelRefueling {
        return refuelingRepository.findById(id)
            .orElseThrow { RuntimeException("Fuel refueling record not found: $id") }
    }

    /**
     * Получает все записи о заправках
     * @return Список всех записей
     */
    fun getAllFuelRefuelings(): List<FuelRefueling> {
        return refuelingRepository.findAll()
    }

    /**
     * Получает записи о заправках по ID транспортного средства
     * @param vehicleId ID транспортного средства
     * @return Список записей
     */
    fun getFuelRefuelingsByVehicleId(vehicleId: UUID): List<FuelRefueling> {
        return refuelingRepository.findByVehicleId(vehicleId)
    }

    /**
     * Получает записи о заправках по ID водителя
     * @param driverId ID водителя
     * @return Список записей
     */
    fun getFuelRefuelingsByDriverId(driverId: UUID): List<FuelRefueling> {
        return refuelingRepository.findByDriverId(driverId)
    }

    /**
     * Создает запись о расходе топлива
     * @param consumption Данные о расходе
     * @return Созданная запись
     */
    @Transactional
    fun createFuelConsumption(consumption: FuelConsumption): FuelConsumption {
        logger.info("Creating fuel consumption record for vehicle: ${consumption.vehicleId}")
        consumption.createdAt = LocalDateTime.now()
        return consumptionRepository.save(consumption)
    }

    /**
     * Получает запись о расходе топлива по ID
     * @param id ID записи
     * @return Запись о расходе
     */
    fun getFuelConsumptionById(id: UUID): FuelConsumption {
        return consumptionRepository.findById(id)
            .orElseThrow { RuntimeException("Fuel consumption record not found: $id") }
    }

    /**
     * Получает все записи о расходе топлива
     * @return Список всех записей
     */
    fun getAllFuelConsumptions(): List<FuelConsumption> {
        return consumptionRepository.findAll()
    }

    /**
     * Получает записи о расходе топлива по ID транспортного средства
     * @param vehicleId ID транспортного средства
     * @return Список записей
     */
    fun getFuelConsumptionsByVehicleId(vehicleId: UUID): List<FuelConsumption> {
        return consumptionRepository.findByVehicleId(vehicleId)
    }

    /**
     * Получает записи о расходе топлива по ID водителя
     * @param driverId ID водителя
     * @return Список записей
     */
    fun getFuelConsumptionsByDriverId(driverId: UUID): List<FuelConsumption> {
        return consumptionRepository.findByDriverId(driverId)
    }

    /**
     * Получает записи о расходе топлива по ID рейса
     * @param tripId ID рейса
     * @return Список записей
     */
    fun getFuelConsumptionsByTripId(tripId: UUID): List<FuelConsumption> {
        return consumptionRepository.findByTripId(tripId)
    }
}