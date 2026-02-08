package com.freight.marketplace.controller

import com.freight.marketplace.entity.deal.ContractEntity
import com.freight.marketplace.entity.deal.ContractStatus
import com.freight.marketplace.service.arbitration.ArbitrationService
import com.freight.marketplace.service.arbitration.ResolutionType
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST контроллер для административного управления арбитражем
 */
@RestController
@RequestMapping("/api/v1/admin/arbitration")
class AdminArbitrationController(
    private val arbitrationService: ArbitrationService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AdminArbitrationController::class.java)
    }

    /**
     * Получает список всех контрактов в статусе DISPUTED
     * @return Список контрактов в споре
     */
    @GetMapping("/disputed-contracts")
    fun getDisputedContracts(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting all disputed contracts")

        try {
            val disputedContracts = arbitrationService.getAllDisputedContracts()

            val result = mutableMapOf<String, Any>()
            result["success"] = true
            result["count"] = disputedContracts.size
            result["contracts"] = disputedContracts.map { contract ->
                val contractMap = mutableMapOf<String, Any>()
                contractMap["id"] = contract.id!!
                contractMap["bidId"] = contract.bidId!!
                contractMap["masterOrderId"] = contract.masterOrderId!!
                contractMap["amount"] = contract.amount.toDouble()
                contractMap["status"] = contract.status.name
                contractMap["evidenceHash"] = contract.evidenceHash!!
                contractMap["createdAt"] = contract.createdAt!!
                contractMap["updatedAt"] = contract.updatedAt!!
                contractMap
            }
            result["message"] = "Disputed contracts retrieved successfully"
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error getting disputed contracts", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    /**
     * Получает детальную информацию о споре
     * @param contractId ID контракта
     * @return Детали спора
     */
    @GetMapping("/dispute/{contractId}")
    fun getDisputeDetails(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Getting dispute details for contract: $contractId")

        try {
            val disputeDetails = arbitrationService.getDisputeDetails(contractId)

            val result = mutableMapOf<String, Any>()
            result["success"] = true
            result["contractId"] = contractId
            val contractMap = mutableMapOf<String, Any>()
            contractMap["id"] = disputeDetails.contract.id!!
            contractMap["bidId"] = disputeDetails.contract.bidId!!
            contractMap["masterOrderId"] = disputeDetails.contract.masterOrderId!!
            contractMap["amount"] = disputeDetails.contract.amount.toDouble()
            contractMap["status"] = disputeDetails.contract.status.name
            contractMap["evidenceHash"] = disputeDetails.contract.evidenceHash!!
            contractMap["createdAt"] = disputeDetails.contract.createdAt!!
            contractMap["updatedAt"] = disputeDetails.contract.updatedAt!!
            result["contract"] = contractMap
            
            val integrityCheckMap = mutableMapOf<String, Any>()
            integrityCheckMap["isValid"] = disputeDetails.integrityCheck.isValid
            integrityCheckMap["message"] = disputeDetails.integrityCheck.message!!
            integrityCheckMap["timestamp"] = disputeDetails.integrityCheck.timestamp!!
            result["integrityCheck"] = integrityCheckMap
            
            result["evidenceData"] = disputeDetails.evidenceData!!
            result["disputeReason"] = disputeDetails.disputeReason!!
            result["createdAt"] = disputeDetails.createdAt!!
            result["message"] = "Dispute details retrieved successfully"
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error getting dispute details for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
        }
    }

    /**
     * Открывает спор по контракту
     * @param contractId ID контракта
     * @param reason Причина спора
     * @return Обновленный контракт
     */
    @PostMapping("/dispute/{contractId}/open")
    fun openDispute(
        @PathVariable contractId: UUID,
        @RequestParam reason: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Opening dispute for contract: $contractId, reason: $reason")

        try {
            val contract = arbitrationService.openDispute(contractId, reason)

            val result = mutableMapOf<String, Any>()
            result["success"] = true
            result["contractId"] = contractId
            val contractMap = mutableMapOf<String, Any>()
            contractMap["id"] = contract.id!!
            contractMap["bidId"] = contract.bidId!!
            contractMap["masterOrderId"] = contract.masterOrderId!!
            contractMap["amount"] = contract.amount.toDouble()
            contractMap["status"] = contract.status.name
            contractMap["evidenceHash"] = contract.evidenceHash!!
            contractMap["updatedAt"] = contract.updatedAt!!
            result["contract"] = contractMap
            result["message"] = "Dispute opened successfully"
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error opening dispute for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            )))
        }
    }

    /**
     * Разрешает спор по контракту
     * @param contractId ID контракта
     * @param decision Решение арбитра
     * @return Обновленный контракт
     */
    @PostMapping("/dispute/{contractId}/resolve")
    fun resolveDispute(
        @PathVariable contractId: UUID,
        @RequestParam decision: ResolutionType
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Resolving dispute for contract: $contractId, decision: $decision")

        try {
            val contract = arbitrationService.resolveDispute(contractId, decision)

            val result = mutableMapOf<String, Any>()
            result["success"] = true
            result["contractId"] = contractId
            result["decision"] = decision.name
            val contractMap = mutableMapOf<String, Any>()
            contractMap["id"] = contract.id!!
            contractMap["bidId"] = contract.bidId!!
            contractMap["masterOrderId"] = contract.masterOrderId!!
            contractMap["amount"] = contract.amount.toDouble()
            contractMap["status"] = contract.status.name
            contractMap["evidenceHash"] = contract.evidenceHash!!
            contractMap["updatedAt"] = contract.updatedAt!!
            result["contract"] = contractMap
            result["message"] = "Dispute resolved successfully"
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error resolving dispute for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            )))
        }
    }

    /**
     * Проверяет целостность данных сделки
     * @param contractId ID контракта
     * @return Результат проверки целостности
     */
    @GetMapping("/dispute/{contractId}/integrity-check")
    fun checkEvidenceIntegrity(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Checking evidence integrity for contract: $contractId")

        try {
            val contract = arbitrationService.getContractById(contractId)
            val integrityCheck = arbitrationService.verifyEvidenceIntegrity(contract)

            val result = mutableMapOf<String, Any>()
            result["success"] = true
            result["contractId"] = contractId
            val integrityCheckMap = mutableMapOf<String, Any>()
            integrityCheckMap["isValid"] = integrityCheck.isValid
            integrityCheckMap["message"] = integrityCheck.message!!
            integrityCheckMap["timestamp"] = integrityCheck.timestamp!!
            result["integrityCheck"] = integrityCheckMap
            result["message"] = "Evidence integrity check completed"
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error checking evidence integrity for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            )))
        }
    }

    /**
     * Получает статистику по спорам
     * @return Статистика по спорам
     */
    @GetMapping("/statistics")
    fun getDisputeStatistics(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting dispute statistics")

        try {
            val allDisputedContracts = arbitrationService.getAllDisputedContracts()

            val statistics = mapOf(
                "totalDisputed" to allDisputedContracts.size,
                "byStatus" to allDisputedContracts.groupBy { it.status }
                    .mapValues { it.value.size },
                "totalAmountDisputed" to allDisputedContracts.sumOf { it.amount.toDouble() },
                "recentDisputes" to allDisputedContracts.sortedByDescending { it.updatedAt }
                    .take(10)
                    .map { contract ->
                        mapOf(
                            "id" to contract.id,
                            "bidId" to contract.bidId,
                            "amount" to contract.amount.toDouble(),
                            "status" to contract.status.name,
                            "updatedAt" to contract.updatedAt
                        )
                    }
            )

            val result = mutableMapOf<String, Any>()
            result["success"] = true
            result["statistics"] = statistics
            result["message"] = "Dispute statistics retrieved successfully"
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error getting dispute statistics", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            )))
        }
    }

    /**
     * Получает контракт по ID
     * @param contractId ID контракта
     * @return Контракт
     */
    @GetMapping("/contract/{contractId}")
    fun getContract(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Getting contract: $contractId")

        try {
            val contract = arbitrationService.getContractById(contractId)

            val result = mutableMapOf<String, Any>()
            result["success"] = true
            result["contractId"] = contractId
            val contractMap = mutableMapOf<String, Any>()
            contractMap["id"] = contract.id!!
            contractMap["bidId"] = contract.bidId!!
            contractMap["masterOrderId"] = contract.masterOrderId!!
            contractMap["amount"] = contract.amount.toDouble()
            contractMap["status"] = contract.status.name
            contractMap["evidenceHash"] = contract.evidenceHash!!
            contractMap["createdAt"] = contract.createdAt!!
            contractMap["updatedAt"] = contract.updatedAt!!
            result["contract"] = contractMap
            result["message"] = "Contract retrieved successfully"
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error getting contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            )))
        }
    }

    /**
     * Фильтрация контрактов по статусу
     * @param status Статус контракта
     * @return Список контрактов
     */
    @GetMapping("/contracts/status/{status}")
    fun getContractsByStatus(@PathVariable status: ContractStatus): ResponseEntity<Map<String, Any>> {
        logger.info("Getting contracts by status: $status")

        try {
            val contracts = arbitrationService.getAllDisputedContracts()
                .filter { it.status == status }

            val result = mutableMapOf<String, Any>()
            result["success"] = true
            result["status"] = status.name
            result["count"] = contracts.size
            result["contracts"] = contracts.map { contract ->
                val contractMap = mutableMapOf<String, Any>()
                contractMap["id"] = contract.id!!
                contractMap["bidId"] = contract.bidId!!
                contractMap["masterOrderId"] = contract.masterOrderId!!
                contractMap["amount"] = contract.amount.toDouble()
                contractMap["status"] = contract.status.name
                contractMap["evidenceHash"] = contract.evidenceHash!!
                contractMap["createdAt"] = contract.createdAt!!
                contractMap["updatedAt"] = contract.updatedAt!!
                contractMap
            }
            result["message"] = "Contracts retrieved successfully"
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error getting contracts by status: $status", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "status" to status.name,
                "error" to e.message
            ))
        }
    }
}