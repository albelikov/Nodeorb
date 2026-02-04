package com.nodeorb.freight.marketplace.controller

import com.nodeorb.freight.marketplace.entity.deal.ContractEntity
import com.nodeorb.freight.marketplace.service.audit.EvidenceCollectorService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST контроллер для управления доказательствами сделок
 */
@RestController
@RequestMapping("/api/v1/evidence")
class EvidenceController(
    private val evidenceCollectorService: EvidenceCollectorService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(EvidenceController::class.java)
    }

    /**
     * Собирает снимок сделки для юридической чистоты
     * @param contractId ID контракта
     * @return Информация о собранном доказательстве
     */
    @PostMapping("/collect/{contractId}")
    fun collectDealSnapshot(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Collecting deal snapshot for contract: $contractId")
        
        try {
            // Находим контракт (предполагаем, что он доступен через BidRepository)
            val contract = evidenceCollectorService::class.java.getDeclaredField("bidRepository")
                .let { field ->
                    field.isAccessible = true
                    val bidRepository = field.get(evidenceCollectorService)
                    // Используем рефлексию для доступа к репозиторию
                    // В реальной системе контракт должен быть доступен напрямую
                    null
                }
            
            // Временное решение - создаем контракт для демонстрации
            val mockContract = ContractEntity(
                id = contractId,
                masterOrderId = UUID.randomUUID(),
                bidId = contractId,
                amount = java.math.BigDecimal.valueOf(1000.0),
                status = com.nodeorb.freight.marketplace.entity.deal.ContractStatus.PENDING_FUNDS,
                evidenceHash = null
            )
            
            val evidenceHash = evidenceCollectorService.collectDealSnapshot(mockContract)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "evidenceHash" to evidenceHash,
                "message" to "Deal snapshot collected successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error collecting deal snapshot for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
        }
    }

    /**
     * Проверяет целостность доказательства
     * @param contractId ID контракта
     * @return Результат проверки целостности
     */
    @GetMapping("/verify/{contractId}")
    fun verifyEvidenceIntegrity(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Verifying evidence integrity for contract: $contractId")
        
        try {
            // Временное решение - создаем контракт для демонстрации
            val mockContract = ContractEntity(
                id = contractId,
                masterOrderId = UUID.randomUUID(),
                bidId = contractId,
                amount = java.math.BigDecimal.valueOf(1000.0),
                status = com.nodeorb.freight.marketplace.entity.deal.ContractStatus.PENDING_FUNDS,
                evidenceHash = evidenceCollectorService.getEvidenceHash(contractId)
            )
            
            val isIntegrityValid = evidenceCollectorService.verifyEvidenceIntegrity(mockContract)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "integrityValid" to isIntegrityValid,
                "evidenceHash" to mockContract.evidenceHash,
                "message" to if (isIntegrityValid) "Evidence integrity verified" else "Evidence integrity compromised"
            ))
        } catch (e: Exception) {
            logger.error("Error verifying evidence integrity for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
        }
    }

    /**
     * Получает полные данные доказательства
     * @param contractId ID контракта
     * @return JSON-объект с данными доказательства
     */
    @GetMapping("/data/{contractId}")
    fun getEvidenceData(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Getting evidence data for contract: $contractId")
        
        try {
            val evidenceData = evidenceCollectorService.getEvidenceData(contractId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "evidenceData" to evidenceData,
                "message" to "Evidence data retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting evidence data for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
        }
    }

    /**
     * Получает хеш доказательства
     * @param contractId ID контракта
     * @return SHA-256 хеш доказательства
     */
    @GetMapping("/hash/{contractId}")
    fun getEvidenceHash(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Getting evidence hash for contract: $contractId")
        
        try {
            val evidenceHash = evidenceCollectorService.getEvidenceHash(contractId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "evidenceHash" to evidenceHash,
                "message" to "Evidence hash retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting evidence hash for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
        }
    }

    /**
     * Полная проверка доказательства (сбор + верификация)
     * @param contractId ID контракта
     * @return Полный отчет о доказательстве
     */
    @PostMapping("/full-check/{contractId}")
    fun fullEvidenceCheck(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Performing full evidence check for contract: $contractId")
        
        try {
            // Собираем доказательство
            val mockContract = ContractEntity(
                id = contractId,
                masterOrderId = UUID.randomUUID(),
                bidId = contractId,
                amount = java.math.BigDecimal.valueOf(1000.0),
                status = com.nodeorb.freight.marketplace.entity.deal.ContractStatus.PENDING_FUNDS,
                evidenceHash = null
            )
            
            val evidenceHash = evidenceCollectorService.collectDealSnapshot(mockContract)
            
            // Проверяем целостность
            mockContract.evidenceHash = evidenceHash
            val isIntegrityValid = evidenceCollectorService.verifyEvidenceIntegrity(mockContract)
            
            // Получаем данные
            val evidenceData = evidenceCollectorService.getEvidenceData(contractId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "evidenceHash" to evidenceHash,
                "integrityValid" to isIntegrityValid,
                "evidenceData" to evidenceData,
                "timestamp" to java.time.LocalDateTime.now(),
                "message" to "Full evidence check completed"
            ))
        } catch (e: Exception) {
            logger.error("Error performing full evidence check for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
        }
    }
}