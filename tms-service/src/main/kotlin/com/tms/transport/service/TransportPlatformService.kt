package com.tms.transport.service

import com.tms.transport.config.TransportPlatformConfig
import com.tms.transport.model.CarrierContract
import com.tms.transport.model.TransportMode
import com.tms.transport.repository.CarrierContractRepository
import com.tms.transport.repository.TransportModeRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Сервис для работы с транспортной платформой
 */
@Service
class TransportPlatformService(
    private val config: TransportPlatformConfig,
    private val transportModeRepository: TransportModeRepository,
    private val carrierContractRepository: CarrierContractRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransportPlatformService::class.java)
        private const val ACTIVE_STATUS = "ACTIVE"
        private const val PENDING_STATUS = "PENDING"
    }

    /**
     * Получить доступные виды транспорта
     */
    fun getAvailableTransportModes(): List<TransportMode> {
        return transportModeRepository.findByActiveTrue()
    }

    /**
     * Получить типы транспорта для мультимодальных перевозок
     */
    fun getMultimodalTransportModes(): List<TransportMode> {
        return transportModeRepository.findBySupportsMultimodalTrueAndActiveTrue()
    }

    /**
     * Получить активные контракты с перевозчиками
     */
    fun getActiveCarrierContracts(): List<CarrierContract> {
        return carrierContractRepository.findByStatus(ACTIVE_STATUS)
    }

    /**
     * Получить контракты для перевозчика
     */
    fun getCarrierContracts(carrierId: Long): List<CarrierContract> {
        return carrierContractRepository.findByCarrierId(carrierId)
    }

    /**
     * Получить активные контракты для перевозчика
     */
    fun getActiveCarrierContracts(carrierId: Long): List<CarrierContract> {
        return carrierContractRepository.findByCarrierIdAndStatus(carrierId, ACTIVE_STATUS)
    }

    /**
     * Найти подходящий контракт по параметрам груза
     */
    fun findContractForCargo(
        carrierId: Long,
        weight: Double,
        volume: Double
    ): CarrierContract? {
        val contracts = getActiveCarrierContracts(carrierId)
        return contracts.firstOrNull {
            weight >= it.minWeight && weight <= it.maxWeight &&
            volume >= it.minVolume && volume <= it.maxVolume
        }
    }

    /**
     * Рассчитать стоимость перевозки на основе контракта
     */
    fun calculateShippingCost(
        contract: CarrierContract,
        distance: Double,
        weight: Double,
        volume: Double
    ): Double {
        val baseCost = contract.baseRate
        val distanceCost = contract.perKmRate * distance
        val weightCost = contract.perKgRate * weight
        val volumeCost = contract.perM3Rate * volume

        var totalCost = baseCost + distanceCost + weightCost + volumeCost

        // Если включена надбавка за топливо
        if (contract.fuelSurchargeEnabled) {
            // В реальной системе здесь будет расчет надбавки на основе текущей цены топлива
            val fuelSurcharge = totalCost * 0.1
            totalCost += fuelSurcharge
        }

        return totalCost
    }

    /**
     * Получить контракты, которые истекают скоро
     */
    fun getExpiringContracts(days: Int = config.contracts.notificationDays): List<CarrierContract> {
        val endDate = LocalDate.now().plusDays(days.toLong())
        return carrierContractRepository.findByValidToBetween(LocalDate.now(), endDate)
    }

    /**
     * Проверить, поддерживается ли указанный вид транспорта
     */
    fun isTransportModeSupported(code: String): Boolean {
        return config.multimodal.modes.contains(code.lowercase()) &&
               transportModeRepository.findByCode(code)?.active == true
    }

    /**
     * Получить конфигурацию платформы
     */
    fun getPlatformConfig() = config

    /**
     * Проверить, включена ли поддержка мультимодальных перевозок
     */
    fun isMultimodalEnabled() = config.multimodal.enabled

    /**
     * Проверить, включена ли поддержка последней мили
     */
    fun isLastMileEnabled() = config.lastMile.enabled

    /**
     * Получить провайдеров последней мили
     */
    fun getLastMileProviders() = config.lastMile.providers
}