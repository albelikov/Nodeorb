package com.nodeorb.freight.marketplace.controller

import com.nodeorb.freight.marketplace.entity.deal.ContractEntity
import com.nodeorb.freight.marketplace.entity.deal.ContractStatus
import com.nodeorb.freight.marketplace.service.arbitration.ArbitrationService
import com.nodeorb.freight.marketplace.service.arbitration.DisputeDetails
import com.nodeorb.freight.marketplace.service.arbitration.ResolutionType
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
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "count" to disputedContracts.size,
                "contracts" to disputedContracts.map { contract ->
                    mapOf(
                        "id" to contract.id,
                        "bidId" to contract.bidId,
                        "masterOrderId" to contract.masterOrderId,
                        "amount" to contract.amount.toDouble(),
                        "status" to contract.status.name,
                        "disputeReason" to contract.evidenceHash,
                        "createdAt" to contract.createdAt,
                        "updatedAt" to contract.updatedAt
                    )
                },
                "message" to "Disputed contracts retrieved successfully"
            ))
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
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "contract" to mapOf(
                    "id" to disputeDetails.contract.id,
                    "bidId" to disputeDetails.contract.bidId,
                    "masterOrderId" to disputeDetails.contract.masterOrderId,
                    "amount" to disputeDetails.contract.amount.toDouble(),
                    "status" to disputeDetails.contract.status.name,
                    "evidenceHash" to disputeDetails.contract.evidenceHash,
                    "createdAt" to disputeDetails.contract.createdAt,
                    "updatedAt" to disputeDetails.contract.updatedAt
                ),
                "integrityCheck" to mapOf(
                    "isValid" to disputeDetails.integrityCheck.isValid,
                    "message" to disputeDetails.integrityCheck.message,
                    "timestamp" to disputeDetails.integrityCheck.timestamp
                ),
                "evidenceData" to disputeDetails.evidenceData,
                "disputeReason" to disputeDetails.disputeReason,
                "createdAt" to disputeDetails.createdAt,
                "message" to "Dispute details retrieved successfully"
            ))
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
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "contract" to mapOf(
                    "id" to contract.id,
                    "bidId" to contract.bidId,
                    "masterOrderId" to contract.masterOrderId,
                    "amount" to contract.amount.toDouble(),
                    "status" to contract.status.name,
                    "evidenceHash" to contract.evidenceHash,
                    "updatedAt" to contract.updatedAt
                ),
                "message" to "Dispute opened successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error opening dispute for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
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
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "decision" to decision.name,
                "contract" to mapOf(
                    "id" to contract.id,
                    "bidId" to contract.bidId,
                    "masterOrderId" to contract.masterOrderId,
                    "amount" to contract.amount.toDouble(),
                    "status" to contract.status.name,
                    "evidenceHash" to contract.evidenceHash,
                    "updatedAt" to contract.updatedAt
                ),
                "message" to "Dispute resolved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error resolving dispute for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
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
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "integrityCheck" to mapOf(
                    "isValid" to integrityCheck.isValid,
                    "message" to integrityCheck.message,
                    "timestamp" to integrityCheck.timestamp
                ),
                "message" to "Evidence integrity check completed"
            ))
        } catch (e: Exception) {
            logger.error("Error checking evidence integrity for contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
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
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "statistics" to statistics,
                "message" to "Dispute statistics retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting dispute statistics", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
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
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "contractId" to contractId,
                "contract" to mapOf(
                    "id" to contract.id,
                    "bidId" to contract.bidId,
                    "masterOrderId" to contract.masterOrderId,
                    "amount" to contract.amount.toDouble(),
                    "status" to contract.status.name,
                    "evidenceHash" to contract.evidenceHash,
                    "createdAt" to contract.createdAt,
                    "updatedAt" to contract.updatedAt
                ),
                "message" to "Contract retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting contract: $contractId", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "contractId" to contractId,
                "error" to e.message
            ))
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
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "status" to status.name,
                "count" to contracts.size,
                "contracts" to contracts.map { contract ->
                    mapOf(
                        "id" to contract.id,
                        "bidId" to contract.bidId,
                        "masterOrderId" to contract.masterOrderId,
                        "amount" to contract.amount.toDouble(),
                        "status" to contract.status.name,
                        "evidenceHash" to contract.evidenceHash,
                        "createdAt" to contract.createdAt,
                        "updatedAt" to contract.updatedAt
                    )
                },
                "message" to "Contracts retrieved successfully"
            ))
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