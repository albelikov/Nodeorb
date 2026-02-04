package com.nodeorb.freight.marketplace.controller

import com.nodeorb.freight.marketplace.entity.deal.ContractStatus
import com.nodeorb.freight.marketplace.service.deal.DealManagementService
import com.nodeorb.freight.marketplace.service.finance.EscrowService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST контроллер для управления финансовыми операциями и сделками
 */
@RestController
@RequestMapping("/api/v1/finance")
class FinanceController(
    private val escrowService: EscrowService,
    private val dealManagementService: DealManagementService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(FinanceController::class.java)
    }

    /**
     * Блокирует средства на эскроу-счете
     * @param bidId ID ставки
     * @return Информация о созданном контракте
     */
    @PostMapping("/escrow/lock/{bidId}")
    fun lockFunds(@PathVariable bidId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Locking funds for bid: $bidId")
        
        val contract = escrowService.lockFunds(bidId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "contractId" to contract.id,
            "bidId" to contract.bidId,
            "masterOrderId" to contract.masterOrderId,
            "amount" to contract.amount.toDouble(),
            "status" to contract.status.name,
            "message" to "Funds locked successfully"
        ))
    }

    /**
     * Подтверждает финансирование
     * @param contractId ID контракта
     * @return Информация об обновленном контракте
     */
    @PostMapping("/escrow/confirm/{contractId}")
    fun confirmFunding(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Confirming funding for contract: $contractId")
        
        val contract = escrowService.confirmFunding(contractId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "contractId" to contract.id,
            "status" to contract.status.name,
            "message" to "Funding confirmed successfully"
        ))
    }

    /**
     * Отмечает начало транспортировки
     * @param contractId ID контракта
     * @return Информация об обновленном контракте
     */
    @PostMapping("/escrow/in-transit/{contractId}")
    fun markInTransit(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Marking contract as in transit: $contractId")
        
        val contract = escrowService.markInTransit(contractId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "contractId" to contract.id,
            "status" to contract.status.name,
            "message" to "Contract marked as in transit"
        ))
    }

    /**
     * Освобождает средства после доставки
     * @param contractId ID контракта
     * @param evidenceHash Хэш доказательства доставки
     * @return Информация об обновленном контракте
     */
    @PostMapping("/escrow/release/{contractId}")
    fun releaseFunds(
        @PathVariable contractId: UUID,
        @RequestParam(required = false) evidenceHash: String? = null
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Releasing funds for contract: $contractId")
        
        val contract = escrowService.releaseFunds(contractId, evidenceHash)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "contractId" to contract.id,
            "status" to contract.status.name,
            "evidenceHash" to contract.evidenceHash,
            "message" to "Funds released successfully"
        ))
    }

    /**
     * Инициирует спор по сделке
     * @param contractId ID контракта
     * @param reason Причина спора
     * @return Информация об обновленном контракте
     */
    @PostMapping("/escrow/dispute/{contractId}")
    fun initiateDispute(
        @PathVariable contractId: UUID,
        @RequestParam reason: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Initiating dispute for contract: $contractId, reason: $reason")
        
        val contract = escrowService.markAsDisputed(contractId, reason)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "contractId" to contract.id,
            "status" to contract.status.name,
            "message" to "Dispute initiated successfully"
        ))
    }

    /**
     * Подтверждает ставку и переводит заказ в активную фазу
     * @param bidId ID ставки
     * @return Информация о сделке
     */
    @PostMapping("/deals/award/{bidId}")
    fun awardBid(@PathVariable bidId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Awarding bid: $bidId")
        
        val bid = dealManagementService.awardBid(bidId)
        val dealInfo = dealManagementService.getDealInfo(bidId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "bidId" to bidId,
            "carrierId" to bid.carrierId,
            "amount" to bid.amount.toDouble(),
            "bidStatus" to bid.status.name,
            "dealInfo" to dealInfo,
            "message" to "Bid awarded successfully"
        ))
    }

    /**
     * Отменяет подтверждение ставки
     * @param bidId ID ставки
     * @return Информация о сделке
     */
    @PostMapping("/deals/cancel/{bidId}")
    fun cancelAward(@PathVariable bidId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Cancelling award for bid: $bidId")
        
        val bid = dealManagementService.cancelAward(bidId)
        val dealInfo = dealManagementService.getDealInfo(bidId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "bidId" to bidId,
            "bidStatus" to bid.status.name,
            "dealInfo" to dealInfo,
            "message" to "Award cancelled successfully"
        ))
    }

    /**
     * Отмечает начало транспортировки
     * @param bidId ID ставки
     * @return Информация о сделке
     */
    @PostMapping("/deals/start-transport/{bidId}")
    fun startTransportation(@PathVariable bidId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Starting transportation for bid: $bidId")
        
        val contract = dealManagementService.markTransportationStarted(bidId)
        val dealInfo = dealManagementService.getDealInfo(bidId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "bidId" to bidId,
            "contractId" to contract.id,
            "status" to contract.status.name,
            "dealInfo" to dealInfo,
            "message" to "Transportation started successfully"
        ))
    }

    /**
     * Завершает сделку после доставки
     * @param bidId ID ставки
     * @param evidenceHash Хэш доказательства доставки
     * @return Информация о сделке
     */
    @PostMapping("/deals/complete/{bidId}")
    fun completeDeal(
        @PathVariable bidId: UUID,
        @RequestParam(required = false) evidenceHash: String? = null
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Completing deal for bid: $bidId")
        
        val contract = dealManagementService.completeDeal(bidId, evidenceHash)
        val dealInfo = dealManagementService.getDealInfo(bidId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "bidId" to bidId,
            "contractId" to contract.id,
            "status" to contract.status.name,
            "evidenceHash" to contract.evidenceHash,
            "dealInfo" to dealInfo,
            "message" to "Deal completed successfully"
        ))
    }

    /**
     * Получает информацию о сделке
     * @param bidId ID ставки
     * @return Информация о сделке
     */
    @GetMapping("/deals/info/{bidId}")
    fun getDealInfo(@PathVariable bidId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Getting deal info for bid: $bidId")
        
        val dealInfo = dealManagementService.getDealInfo(bidId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "dealInfo" to dealInfo
        ))
    }

    /**
     * Проверяет, может ли быть подтверждена ставка
     * @param bidId ID ставки
     * @return Возможность подтверждения ставки
     */
    @GetMapping("/deals/can-award/{bidId}")
    fun canAwardBid(@PathVariable bidId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Checking if bid can be awarded: $bidId")
        
        val canAward = dealManagementService.canAwardBid(bidId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "bidId" to bidId,
            "canAward" to canAward
        ))
    }

    /**
     * Получает контракт по ID
     * @param contractId ID контракта
     * @return Информация о контракте
     */
    @GetMapping("/contracts/{contractId}")
    fun getContract(@PathVariable contractId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Getting contract: $contractId")
        
        val contract = escrowService.getContractById(contractId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "contract" to mapOf(
                "id" to contract.id,
                "bidId" to contract.bidId,
                "masterOrderId" to contract.masterOrderId,
                "amount" to contract.amount.toDouble(),
                "status" to contract.status.name,
                "evidenceHash" to contract.evidenceHash,
                "createdAt" to contract.createdAt,
                "updatedAt" to contract.updatedAt
            )
        ))
    }

    /**
     * Получает контракт по ID ставки
     * @param bidId ID ставки
     * @return Информация о контракте
     */
    @GetMapping("/contracts/by-bid/{bidId}")
    fun getContractByBid(@PathVariable bidId: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Getting contract by bid: $bidId")
        
        val contract = escrowService.getContractByBidId(bidId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "contract" to (contract?.let { mapOf(
                "id" to it.id,
                "bidId" to it.bidId,
                "masterOrderId" to it.masterOrderId,
                "amount" to it.amount.toDouble(),
                "status" to it.status.name,
                "evidenceHash" to it.evidenceHash,
                "createdAt" to it.createdAt,
                "updatedAt" to it.updatedAt
            ) } ?: null)
        ))
    }

    /**
     * Получает все контракты по статусу
     * @param status Статус контракта
     * @return Список контрактов
     */
    @GetMapping("/contracts/status/{status}")
    fun getContractsByStatus(@PathVariable status: ContractStatus): ResponseEntity<Map<String, Any>> {
        logger.info("Getting contracts by status: $status")
        
        // TODO: Добавить репозиторий для получения контрактов по статусу
        // val contracts = contractRepository.findByStatus(status)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "status" to status.name,
            "contracts" to emptyList<Map<String, Any>>()
        ))
    }
}