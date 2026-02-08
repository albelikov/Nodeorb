package com.tms.transport.controller

import com.tms.transport.config.TransportPlatformConfig
import com.tms.transport.model.CarrierContract
import com.tms.transport.model.TransportMode
import com.tms.transport.service.TransportPlatformService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST контроллер для работы с транспортной платформой
 */
@RestController
@RequestMapping("/api/v1/transport-platform")
class TransportPlatformController(
    private val transportPlatformService: TransportPlatformService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransportPlatformController::class.java)
    }

    /**
     * Получить доступные виды транспорта
     */
    @GetMapping("/transport-modes")
    fun getAvailableTransportModes(): ResponseEntity<List<TransportMode>> {
        logger.info("Получение доступных видов транспорта")
        val modes = transportPlatformService.getAvailableTransportModes()
        return ResponseEntity.ok(modes)
    }

    /**
     * Получить типы транспорта для мультимодальных перевозок
     */
    @GetMapping("/transport-modes/multimodal")
    fun getMultimodalTransportModes(): ResponseEntity<List<TransportMode>> {
        logger.info("Получение типов транспорта для мультимодальных перевозок")
        val modes = transportPlatformService.getMultimodalTransportModes()
        return ResponseEntity.ok(modes)
    }

    /**
     * Получить активные контракты с перевозчиками
     */
    @GetMapping("/contracts")
    fun getActiveCarrierContracts(): ResponseEntity<List<CarrierContract>> {
        logger.info("Получение активных контрактов с перевозчиками")
        val contracts = transportPlatformService.getActiveCarrierContracts()
        return ResponseEntity.ok(contracts)
    }

    /**
     * Получить контракты для перевозчика
     */
    @GetMapping("/contracts/carrier/{carrierId}")
    fun getCarrierContracts(@PathVariable carrierId: Long): ResponseEntity<List<CarrierContract>> {
        logger.info("Получение контрактов для перевозчика: $carrierId")
        val contracts = transportPlatformService.getCarrierContracts(carrierId)
        return ResponseEntity.ok(contracts)
    }

    /**
     * Получить активные контракты для перевозчика
     */
    @GetMapping("/contracts/carrier/{carrierId}/active")
    fun getActiveCarrierContracts(@PathVariable carrierId: Long): ResponseEntity<List<CarrierContract>> {
        logger.info("Получение активных контрактов для перевозчика: $carrierId")
        val contracts = transportPlatformService.getActiveCarrierContracts(carrierId)
        return ResponseEntity.ok(contracts)
    }

    /**
     * Найти подходящий контракт по параметрам груза
     */
    @GetMapping("/contracts/carrier/{carrierId}/find")
    fun findContractForCargo(
        @PathVariable carrierId: Long,
        @RequestParam weight: Double,
        @RequestParam volume: Double
    ): ResponseEntity<CarrierContract> {
        logger.info("Поиск контракта для груза: перевозчик=$carrierId, вес=$weight кг, объем=$volume м³")
        val contract = transportPlatformService.findContractForCargo(carrierId, weight, volume)
        return if (contract != null) {
            ResponseEntity.ok(contract)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Рассчитать стоимость перевозки на основе контракта
     */
    @GetMapping("/contracts/{contractId}/calculate-cost")
    fun calculateShippingCost(
        @PathVariable contractId: Long,
        @RequestParam distance: Double,
        @RequestParam weight: Double,
        @RequestParam volume: Double
    ): ResponseEntity<Map<String, Double>> {
        logger.info("Расчет стоимости перевозки по контракту: $contractId")
        // В реальной системе нужно получить контракт из базы данных
        return ResponseEntity.ok(mapOf("cost" to 0.0))
    }

    /**
     * Получить контракты, которые истекают скоро
     */
    @GetMapping("/contracts/expiring")
    fun getExpiringContracts(@RequestParam(required = false) days: Int? = null): ResponseEntity<List<CarrierContract>> {
        logger.info("Получение контрактов, которые истекают")
        val contracts = if (days != null) {
            transportPlatformService.getExpiringContracts(days)
        } else {
            transportPlatformService.getExpiringContracts()
        }
        return ResponseEntity.ok(contracts)
    }

    /**
     * Проверить, поддерживается ли указанный вид транспорта
     */
    @GetMapping("/transport-modes/supported")
    fun isTransportModeSupported(@RequestParam code: String): ResponseEntity<Map<String, Boolean>> {
        logger.info("Проверка поддержки вида транспорта: $code")
        val supported = transportPlatformService.isTransportModeSupported(code)
        return ResponseEntity.ok(mapOf("supported" to supported))
    }

    /**
     * Получить конфигурацию платформы
     */
    @GetMapping("/config")
    fun getPlatformConfig(): ResponseEntity<TransportPlatformConfig> {
        logger.info("Получение конфигурации транспортной платформы")
        val config = transportPlatformService.getPlatformConfig()
        return ResponseEntity.ok(config)
    }

    /**
     * Проверить, включена ли поддержка мультимодальных перевозок
     */
    @GetMapping("/features/multimodal")
    fun isMultimodalEnabled(): ResponseEntity<Map<String, Boolean>> {
        logger.info("Проверка поддержки мультимодальных перевозок")
        val enabled = transportPlatformService.isMultimodalEnabled()
        return ResponseEntity.ok(mapOf("enabled" to enabled))
    }

    /**
     * Проверить, включена ли поддержка последней мили
     */
    @GetMapping("/features/last-mile")
    fun isLastMileEnabled(): ResponseEntity<Map<String, Boolean>> {
        logger.info("Проверка поддержки последней мили")
        val enabled = transportPlatformService.isLastMileEnabled()
        return ResponseEntity.ok(mapOf("enabled" to enabled))
    }

    /**
     * Получить провайдеров последней мили
     */
    @GetMapping("/last-mile/providers")
    fun getLastMileProviders(): ResponseEntity<List<String>> {
        logger.info("Получение провайдеров последней мили")
        val providers = transportPlatformService.getLastMileProviders()
        return ResponseEntity.ok(providers)
    }

    /**
     * Проверить доступность платформы
     */
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        logger.info("Проверка состояния транспортной платформы")
        return ResponseEntity.ok(mapOf("status" to "OK", "service" to "transport-platform"))
    }
}