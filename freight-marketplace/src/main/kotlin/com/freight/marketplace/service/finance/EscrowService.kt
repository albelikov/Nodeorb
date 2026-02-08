package com.freight.marketplace.service.finance

import com.fasterxml.jackson.databind.ObjectMapper
import com.freight.marketplace.entity.BidEntity
import com.freight.marketplace.entity.deal.ContractEntity
import com.freight.marketplace.entity.deal.ContractStatus
import com.freight.marketplace.exception.FreightMarketplaceException
import com.freight.marketplace.repository.BidRepository
import com.freight.marketplace.repository.ContractRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * Сервис управления эскроу-счетами для финансовых операций
 */
@Service
class EscrowService(
    private val bidRepository: BidRepository,
    private val contractRepository: ContractRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(EscrowService::class.java)
        private const val ESCROW_LOCKED_TOPIC = "finance.escrow.locked"
        private const val ESCROW_RELEASED_TOPIC = "finance.escrow.released"
    }

    /**
     * Блокирует средства на эскроу-счете
     * @param bidId ID ставки
     * @return Созданный контракт
     */
    @Transactional
    fun lockFunds(bidId: UUID): ContractEntity {
        logger.info("Locking funds for bid: $bidId")
        
        // 1. Находим ставку
        val bid = bidRepository.findById(bidId)
            ?: throw FreightMarketplaceException("Bid not found: $bidId")
        
        // 2. Проверяем, что ставка еще не заблокирована
        val existingContract = contractRepository.findByBidId(bidId)
        if (existingContract != null) {
            throw FreightMarketplaceException("Funds already locked for bid: $bidId")
        }
        
        // 3. Создаем контракт с начальным статусом PENDING_FUNDS
        val contract = ContractEntity(
            masterOrderId = bid.masterOrder?.id ?: throw FreightMarketplaceException("Master order not found for bid: $bidId"),
            bidId = bidId,
            amount = bid.amount,
            status = ContractStatus.PENDING_FUNDS,
            evidenceHash = null
        )
        
        // 4. Сохраняем контракт
        val savedContract = contractRepository.save(contract)
        
        // 5. Отправляем событие в Kafka
        publishEscrowLockedEvent(savedContract)
        
        logger.info("Successfully locked funds for bid: $bidId, contract: ${savedContract.id}")
        return savedContract
    }

    /**
     * Освобождает средства после проверки e-POD
     * @param contractId ID контракта
     * @param evidenceHash Хэш доказательства доставки
     * @return Обновленный контракт
     */
    @Transactional
    fun releaseFunds(contractId: UUID, evidenceHash: String? = null): ContractEntity {
        logger.info("Releasing funds for contract: $contractId")
        
        // 1. Находим контракт
        val contract = contractRepository.findById(contractId)
            ?: throw FreightMarketplaceException("Contract not found: $contractId")
        
        // 2. Проверяем текущий статус
        if (contract.status != ContractStatus.IN_TRANSIT) {
            throw FreightMarketplaceException("Cannot release funds: contract status is ${contract.status}, expected IN_TRANSIT")
        }
        
        // 3. Обновляем статус и хэш доказательства
        contract.status = ContractStatus.RELEASED
        contract.evidenceHash = evidenceHash
        contract.updatedAt = java.time.LocalDateTime.now()
        
        // 4. Сохраняем обновленный контракт
        val updatedContract = contractRepository.save(contract)
        
        // 5. Отправляем событие в Kafka
        publishEscrowReleasedEvent(updatedContract)
        
        logger.info("Successfully released funds for contract: $contractId")
        return updatedContract
    }

    /**
     * Переводит контракт в статус FUNDED (средства заблокированы и готовы)
     * @param contractId ID контракта
     * @return Обновленный контракт
     */
    @Transactional
    fun confirmFunding(contractId: UUID): ContractEntity {
        logger.info("Confirming funding for contract: $contractId")
        
        val contract = contractRepository.findById(contractId)
            ?: throw FreightMarketplaceException("Contract not found: $contractId")
        
        if (contract.status != ContractStatus.PENDING_FUNDS) {
            throw FreightMarketplaceException("Cannot confirm funding: contract status is ${contract.status}, expected PENDING_FUNDS")
        }
        
        contract.status = ContractStatus.FUNDED
        contract.updatedAt = java.time.LocalDateTime.now()
        
        val updatedContract = contractRepository.save(contract)
        
        logger.info("Successfully confirmed funding for contract: $contractId")
        return updatedContract
    }

    /**
     * Переводит контракт в статус IN_TRANSIT (груз в пути)
     * @param contractId ID контракта
     * @return Обновленный контракт
     */
    @Transactional
    fun markInTransit(contractId: UUID): ContractEntity {
        logger.info("Marking contract as in transit: $contractId")
        
        val contract = contractRepository.findById(contractId)
            ?: throw FreightMarketplaceException("Contract not found: $contractId")
        
        if (contract.status != ContractStatus.FUNDED) {
            throw FreightMarketplaceException("Cannot mark as in transit: contract status is ${contract.status}, expected FUNDED")
        }
        
        contract.status = ContractStatus.IN_TRANSIT
        contract.updatedAt = java.time.LocalDateTime.now()
        
        val updatedContract = contractRepository.save(contract)
        
        logger.info("Successfully marked contract as in transit: $contractId")
        return updatedContract
    }

    /**
     * Переводит контракт в статус DISPUTED (спор)
     * @param contractId ID контракта
     * @param reason Причина спора
     * @return Обновленный контракт
     */
    @Transactional
    fun markAsDisputed(contractId: UUID, reason: String): ContractEntity {
        logger.info("Marking contract as disputed: $contractId, reason: $reason")
        
        val contract = contractRepository.findById(contractId)
            ?: throw FreightMarketplaceException("Contract not found: $contractId")
        
        if (contract.status == ContractStatus.RELEASED) {
            throw FreightMarketplaceException("Cannot mark as disputed: funds already released")
        }
        
        contract.status = ContractStatus.DISPUTED
        contract.evidenceHash = reason // Используем evidenceHash для хранения причины спора
        contract.updatedAt = java.time.LocalDateTime.now()
        
        val updatedContract = contractRepository.save(contract)
        
        logger.info("Successfully marked contract as disputed: $contractId")
        return updatedContract
    }

    /**
     * Получает контракт по ID ставки
     * @param bidId ID ставки
     * @return Контракт или null
     */
    fun getContractByBidId(bidId: UUID): ContractEntity? {
        return contractRepository.findByBidId(bidId)
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

    /**
     * Публикует событие о блокировке средств
     */
    private fun publishEscrowLockedEvent(contract: ContractEntity) {
        val event = mapOf(
            "contractId" to contract.id,
            "bidId" to contract.bidId,
            "masterOrderId" to contract.masterOrderId,
            "amount" to contract.amount.toDouble(),
            "status" to contract.status.name,
            "timestamp" to System.currentTimeMillis()
        )
        
        kafkaTemplate.send(ESCROW_LOCKED_TOPIC, contract.id.toString(), event)
        logger.info("Published escrow locked event for contract: ${contract.id}")
    }

    /**
     * Публикует событие об освобождении средств
     */
    private fun publishEscrowReleasedEvent(contract: ContractEntity) {
        val event = mapOf(
            "contractId" to contract.id,
            "bidId" to contract.bidId,
            "masterOrderId" to contract.masterOrderId,
            "amount" to contract.amount.toDouble(),
            "status" to contract.status.name,
            "evidenceHash" to contract.evidenceHash,
            "timestamp" to System.currentTimeMillis()
        )
        
        kafkaTemplate.send(ESCROW_RELEASED_TOPIC, contract.id.toString(), event)
        logger.info("Published escrow released event for contract: ${contract.id}")
    }
}