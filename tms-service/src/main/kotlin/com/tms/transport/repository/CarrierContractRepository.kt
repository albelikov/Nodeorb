package com.tms.transport.repository

import com.tms.transport.model.CarrierContract
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * Репозиторий для работы с контрактами перевозчиков
 */
@Repository
interface CarrierContractRepository : JpaRepository<CarrierContract, Long> {

    /**
     * Найти активные контракты
     */
    fun findByStatus(status: String): List<CarrierContract>

    /**
     * Найти контракты по ID перевозчика
     */
    fun findByCarrierId(carrierId: Long): List<CarrierContract>

    /**
     * Найти активные контракты по ID перевозчика
     */
    fun findByCarrierIdAndStatus(carrierId: Long, status: String): List<CarrierContract>

    /**
     * Найти контракт по номеру
     */
    fun findByContractNumber(contractNumber: String): CarrierContract?

    /**
     * Найти контракты, которые истекают в указанный период
     */
    fun findByValidToBetween(startDate: LocalDate, endDate: LocalDate): List<CarrierContract>

    /**
     * Найти контракты с автопродлением
     */
    fun findByAutoRenewalTrueAndStatusIn(statuses: List<String>): List<CarrierContract>

    /**
     * Найти активные контракты с поддержкой автопродления
     */
    fun findByAutoRenewalTrueAndStatus(status: String): List<CarrierContract>
}