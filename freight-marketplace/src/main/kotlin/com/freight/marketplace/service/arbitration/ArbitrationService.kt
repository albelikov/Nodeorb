package com.freight.marketplace.service.arbitration

import com.fasterxml.jackson.databind.ObjectMapper
import com.freight.marketplace.entity.deal.ContractEntity
import com.freight.marketplace.entity.deal.ContractStatus
import com.freight.marketplace.exception.FreightMarketplaceException
import com.freight.marketplace.repository.ContractRepository
import com.freight.marketplace.service.audit.EvidenceCollectorService
import com.freight.marketplace.service.finance.EscrowService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Сервис арбитража для управления спорами по сделкам
 */
@Service
class ArbitrationService(
    private val contractRepository: ContractRepository,
    private val escrowService: EscrowService,
    private val evidenceCollectorService: EvidenceCollectorService,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ArbitrationService::class.java)
    }

    /**
     * Открывает спор по контракту
     * @param contractId ID контракта
     * @param reason Причина спора
     * @return Обновленный контракт
     */
    @Transactional
    fun openDispute(contractId: UUID, reason: String): ContractEntity {
        logger.info("Opening dispute for contract: $contractId, reason: $reason")
        
        // 1. Находим контракт
        val contract = contractRepository.findById(contractId)
            ?: throw FreightMarketplaceException("Contract not found: $contractId")
        
        // 2. Проверяем, что контракт не в статусе RELEASED
        if (contract.status == ContractStatus.RELEASED) {
            throw FreightMarketplaceException("Cannot open dispute: funds already released")
        }
        
        // 3. Проверяем, что контракт не в статусе DISPUTED
        if (contract.status == ContractStatus.DISPUTED) {
            throw FreightMarketplaceException("Dispute already opened for contract: $contractId")
        }
        
        // 4. Переводим контракт в статус DISPUTED
        contract.status = ContractStatus.DISPUTED
        contract.evidenceHash = reason // Используем evidenceHash для хранения причины спора
        contract.updatedAt = java.time.LocalDateTime.now()
        
        val updatedContract = contractRepository.save(contract)
        
        logger.info("Successfully opened dispute for contract: $contractId")
        return updatedContract
    }

    /**
     * Разрешает спор по контракту
     * @param contractId ID контракта
     * @param decision Решение арбитра
     * @return Обновленный контракт
     */
    @Transactional
    fun resolveDispute(contractId: UUID, decision: ResolutionType): ContractEntity {
        logger.info("Resolving dispute for contract: $contractId, decision: $decision")
        
        // 1. Находим контракт
        val contract = contractRepository.findById(contractId)
            ?: throw FreightMarketplaceException("Contract not found: $contractId")
        
        // 2. Проверяем, что контракт в статусе DISPUTED
        if (contract.status != ContractStatus.DISPUTED) {
            throw FreightMarketplaceException("Cannot resolve dispute: contract status is ${contract.status}, expected DISPUTED")
        }
        
        // 3. Проверяем целостность данных
        val integrityCheck = verifyEvidenceIntegrity(contract)
        
        // 4. Принимаем решение
        val updatedContract = when (decision) {
            ResolutionType.PAY_CARRIER -> {
                // Переводим в IN_TRANSIT, затем освобождаем средства
                contract.status = ContractStatus.IN_TRANSIT
                contract.updatedAt = java.time.LocalDateTime.now()
                val inTransitContract = contractRepository.save(contract)
                
                // Освобождаем средства перевозчику
                escrowService.releaseFunds(inTransitContract.id!!, "Arbitration decision: PAY_CARRIER")
            }
            ResolutionType.REFUND_SHIPPER -> {
                // Переводим в RELEASED с пометкой о возврате
                contract.status = ContractStatus.RELEASED
                contract.evidenceHash = "Arbitration decision: REFUND_SHIPPER"
                contract.updatedAt = java.time.LocalDateTime.now()
                contractRepository.save(contract)
            }
        }
        
        logger.info("Successfully resolved dispute for contract: $contractId with decision: $decision")
        return updatedContract
    }

    /**
     * Проверяет целостность данных сделки
     * @param contract Контракт
     * @return Результат проверки целостности
     */
    fun verifyEvidenceIntegrity(contract: ContractEntity): IntegrityCheckResult {
        val isIntegrityValid = evidenceCollectorService.verifyEvidenceIntegrity(contract)
        
        val message = if (isIntegrityValid) {
            "Данные сделки верны"
        } else {
            "ВНИМАНИЕ: Хеш не совпадает, данные изменены!"
        }
        
        logger.info("Evidence integrity check for contract ${contract.id}: $message")
        
        return IntegrityCheckResult(
            isValid = isIntegrityValid,
            message = message,
            timestamp = java.time.LocalDateTime.now()
        )
    }

    /**
     * Получает детальную информацию о споре
     * @param contractId ID контракта
     * @return Детали спора
     */
    fun getDisputeDetails(contractId: UUID): DisputeDetails {
        val contract = contractRepository.findById(contractId)
            ?: throw FreightMarketplaceException("Contract not found: $contractId")
        
        if (contract.status != ContractStatus.DISPUTED) {
            throw FreightMarketplaceException("Contract is not in dispute status: $contractId")
        }
        
        // Проверяем целостность данных
        val integrityCheck = verifyEvidenceIntegrity(contract)
        
        // Получаем данные доказательства
        val evidenceData = try {
            evidenceCollectorService.getEvidenceData(contractId)
        } catch (e: Exception) {
            logger.error("Error getting evidence data for contract: $contractId", e)
            null
        }
        
        return DisputeDetails(
            contractId = contractId,
            contract = contract,
            integrityCheck = integrityCheck,
            evidenceData = evidenceData,
            disputeReason = contract.evidenceHash,
            createdAt = contract.updatedAt
        )
    }

    /**
     * Получает список всех контрактов в статусе DISPUTED
     * @return Список контрактов в споре
     */
    fun getAllDisputedContracts(): List<ContractEntity> {
        return contractRepository.findByStatus(ContractStatus.DISPUTED)
    }

    /**
     * Получает контракт по ID
     * @param contractId ID контракта
     * @return Контракт
     */
    fun getContractById(contractId: UUID): ContractEntity {
        return contractRepository.findById(contractId)
            ?: throw FreightMarketplaceException("Contract not found: $contractId")
    }
}

/**
 * Тип решения арбитра
 */
enum class ResolutionType {
    PAY_CARRIER,    // Оплатить перевозчику
    REFUND_SHIPPER  // Вернуть средства шипперу
}

/**
 * Результат проверки целостности данных
 */
data class IntegrityCheckResult(
    val isValid: Boolean,
    val message: String,
    val timestamp: java.time.LocalDateTime
)

/**
 * Детали спора
 */
data class DisputeDetails(
    val contractId: java.util.UUID,
    val contract: ContractEntity,
    val integrityCheck: IntegrityCheckResult,
    val evidenceData: String?,
    val disputeReason: String?,
    val createdAt: java.time.LocalDateTime?
)