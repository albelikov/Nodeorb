package com.fms.service

import com.fms.model.Driver
import com.fms.repository.DriverRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class DriverService(private val driverRepository: DriverRepository) {

    companion object {
        private val logger = LoggerFactory.getLogger(DriverService::class.java)
    }

    /**
     * Создает нового водителя
     * @param driver Данные о водителе
     * @return Созданный водитель
     */
    @Transactional
    fun createDriver(driver: Driver): Driver {
        logger.info("Creating driver: ${driver.firstName} ${driver.lastName}")
        driver.createdAt = LocalDateTime.now()
        driver.updatedAt = LocalDateTime.now()
        return driverRepository.save(driver)
    }

    /**
     * Получает водителя по ID
     * @param id ID водителя
     * @return Водитель
     */
    fun getDriverById(id: UUID): Driver {
        return driverRepository.findById(id)
            .orElseThrow { RuntimeException("Driver not found: $id") }
    }

    /**
     * Получает водителя по номеру водительского удостоверения
     * @param driverLicenseNumber Номер водительского удостоверения
     * @return Водитель
     */
    fun getDriverByLicenseNumber(driverLicenseNumber: String): Driver? {
        return driverRepository.findByDriverLicenseNumber(driverLicenseNumber)
    }

    /**
     * Получает водителя по номеру телефона
     * @param phoneNumber Номер телефона
     * @return Водитель
     */
    fun getDriverByPhoneNumber(phoneNumber: String): Driver? {
        return driverRepository.findByPhoneNumber(phoneNumber)
    }

    /**
     * Получает всех водителей
     * @return Список всех водителей
     */
    fun getAllDrivers(): List<Driver> {
        return driverRepository.findAll()
    }

    /**
     * Получает водителей по статусу
     * @param status Статус водителя
     * @return Список водителей
     */
    fun getDriversByStatus(status: String): List<Driver> {
        return driverRepository.findByStatus(status)
    }

    /**
     * Обновляет водителя
     * @param id ID водителя
     * @param driver Данные для обновления
     * @return Обновленный водитель
     */
    @Transactional
    fun updateDriver(id: UUID, driver: Driver): Driver {
        logger.info("Updating driver: $id")
        val existingDriver = getDriverById(id)
        existingDriver.firstName = driver.firstName
        existingDriver.lastName = driver.lastName
        existingDriver.middleName = driver.middleName
        existingDriver.driverLicenseNumber = driver.driverLicenseNumber
        existingDriver.driverLicenseCategory = driver.driverLicenseCategory
        existingDriver.driverLicenseExpiryDate = driver.driverLicenseExpiryDate
        existingDriver.phoneNumber = driver.phoneNumber
        existingDriver.email = driver.email
        existingDriver.medicalCertificateExpiry = driver.medicalCertificateExpiry
        existingDriver.experience = driver.experience
        existingDriver.rating = driver.rating
        existingDriver.status = driver.status
        existingDriver.assignedVehicleId = driver.assignedVehicleId
        existingDriver.updatedAt = LocalDateTime.now()
        return driverRepository.save(existingDriver)
    }

    /**
     * Удаляет водителя
     * @param id ID водителя
     */
    @Transactional
    fun deleteDriver(id: UUID) {
        logger.info("Deleting driver: $id")
        val driver = getDriverById(id)
        driverRepository.delete(driver)
    }

    /**
     * Обновляет статус водителя
     * @param id ID водителя
     * @param status Новый статус
     * @return Обновленный водитель
     */
    @Transactional
    fun updateDriverStatus(id: UUID, status: String): Driver {
        logger.info("Updating driver status: $id, $status")
        val driver = getDriverById(id)
        driver.status = status
        driver.updatedAt = LocalDateTime.now()
        return driverRepository.save(driver)
    }
}