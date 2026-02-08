package com.freight.marketplace.service.audit

import com.fasterxml.jackson.databind.ObjectMapper
import com.freight.marketplace.entity.BidEntity
import com.freight.marketplace.entity.ScmSnapshotEntity
import com.freight.marketplace.entity.deal.ContractEntity
import com.freight.marketplace.repository.BidRepository
import com.freight.marketplace.repository.ContractRepository
import com.freight.marketplace.repository.ScmSnapshotRepository
import com.freight.marketplace.service.ScoringService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

/**
 * Сервис сбора доказательств для юридической чистоты сделок
 * Реализует WORM (Write Once, Read Many) логику для арбитража
 */
@Service
class EvidenceCollectorService(
    private val bidRepository: BidRepository,
    private val scmSnapshotRepository: ScmSnapshotRepository,
    private val scoringService: ScoringService,
    private val objectMapper: ObjectMapper,
    private val contractRepository: ContractRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(EvidenceCollectorService::class.java)
        private const val HASH_ALGORITHM = "SHA-256"
    }

    /**
     * Собирает снимок сделки для юридической чистоты
     * @param contract Контракт сделки
     * @return SHA-256 хеш от собранного JSON-объекта
     */
    @Transactional
    fun collectDealSnapshot(contract: ContractEntity): String {
        logger.info("Collecting deal snapshot for contract: ${contract.id}")
        
        // 1. Получаем ставку по контракту
        val bid = getBidByContract(contract)
        
        // 2. Получаем снимок SCM перевозчика
        val scmSnapshot = getScmSnapshotByBid(bid)
        
        // 3. Получаем финальный Match Score
        val finalScore = getFinalScore(bid)
        
        // 4. Собираем данные в JSON-объект
        val evidenceData = buildEvidenceData(bid, scmSnapshot, finalScore)
        
        // 5. Генерируем SHA-256 хеш
        val evidenceHash = generateSHA256Hash(evidenceData)
        
        // 6. Обновляем контракт
        contract.evidenceHash = evidenceHash
        contract.updatedAt = java.time.LocalDateTime.now()
        
        logger.info("Deal snapshot collected for contract: ${contract.id}, hash: $evidenceHash")
        return evidenceHash
    }

    /**
     * Получает ставку по контракту
     */
    private fun getBidByContract(contract: ContractEntity): BidEntity {
        return bidRepository.findById(contract.bidId).orElseThrow { 
            IllegalArgumentException("Bid not found for contract: ${contract.id}")
        }
    }

    /**
     * Получает снимок SCM по ставке
     */
    private fun getScmSnapshotByBid(bid: BidEntity): ScmSnapshotEntity {
        return scmSnapshotRepository.findByBidId(bid.id!!).firstOrNull()
            ?: throw IllegalArgumentException("SCM snapshot not found for bid: ${bid.id}")
    }

    /**
     * Получает финальный Match Score
     */
    private fun getFinalScore(bid: BidEntity): Double {
        return bid.matchingScore ?: 0.0
    }

    /**
     * Собирает все данные сделки в JSON-объект
     */
    private fun buildEvidenceData(
        bid: BidEntity,
        scmSnapshot: ScmSnapshotEntity,
        finalScore: Double
    ): String {
        val evidenceData = mapOf(
            "dealInfo" to mapOf(
                "contractId" to bid.id,
                "bidId" to bid.id,
                "carrierId" to bid.carrierId,
                "masterOrderId" to bid.masterOrder?.id,
                "amount" to bid.amount.toDouble(),
                "proposedDeliveryDate" to bid.proposedDeliveryDate,
                "notes" to bid.notes,
                "createdAt" to bid.createdAt
            ),
            "scmSnapshot" to mapOf(
                "id" to scmSnapshot.id,
                "carrierId" to scmSnapshot.carrierId,
                "masterOrderId" to scmSnapshot.masterOrderId,
                "snapshotDate" to scmSnapshot.snapshotDate,
                "complianceStatus" to scmSnapshot.complianceStatus.name,
                "complianceDetails" to scmSnapshot.complianceDetails,
                "securityClearance" to scmSnapshot.securityClearance.name,
                "securityDetails" to scmSnapshot.securityDetails,
                "riskScore" to scmSnapshot.riskScore,
                "riskFactors" to scmSnapshot.riskFactors,
                "auditTrail" to scmSnapshot.auditTrail,
                "createdAt" to scmSnapshot.createdAt
            ),
            "scoringInfo" to mapOf(
                "finalScore" to finalScore,
                "scoreBreakdown" to bid.scoreBreakdown,
                "scoredAt" to java.time.LocalDateTime.now()
            ),
            "metadata" to mapOf(
                "collectedAt" to java.time.LocalDateTime.now(),
                "version" to "1.0",
                "system" to "Nodeorb Freight Marketplace - NodeInsight Engine"
            )
        )
        
        return objectMapper.writeValueAsString(evidenceData)
    }

    /**
     * Генерирует SHA-256 хеш от JSON-объекта
     */
    private fun generateSHA256Hash(data: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(data.toByteArray(StandardCharsets.UTF_8))
        
        // Преобразуем байты в hex-строку
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Проверяет целостность доказательства
     * @param contract Контракт с хешем
     * @return true, если данные не были изменены
     */
    fun verifyEvidenceIntegrity(contract: ContractEntity): Boolean {
        if (contract.evidenceHash == null) {
            logger.warn("No evidence hash found for contract: ${contract.id}")
            return false
        }
        
        try {
            // Получаем текущие данные
            val currentEvidenceData = buildCurrentEvidenceData(contract)
            val currentHash = generateSHA256Hash(currentEvidenceData)
            
            // Сравниваем с сохраненным хешем
            return contract.evidenceHash == currentHash
        } catch (e: Exception) {
            logger.error("Error verifying evidence integrity for contract: ${contract.id}", e)
            return false
        }
    }

    /**
     * Собирает текущие данные для проверки целостности
     */
    private fun buildCurrentEvidenceData(contract: ContractEntity): String {
        val bid = getBidByContract(contract)
        val scmSnapshot = getScmSnapshotByBid(bid)
        val finalScore = getFinalScore(bid)
        
        return buildEvidenceData(bid, scmSnapshot, finalScore)
    }

    /**
     * Получает полные данные доказательства по контракту
     * @param contractId ID контракта
     * @return JSON-объект с данными доказательства
     */
    fun getEvidenceData(contractId: UUID): String {
        val contract = contractRepository.findById(contractId).orElseThrow {
            IllegalArgumentException("Contract not found: $contractId")
        }
        
        return buildCurrentEvidenceData(contract)
    }

    /**
     * Получает хеш доказательства по контракту
     * @param contractId ID контракта
     * @return SHA-256 хеш
     */
    fun getEvidenceHash(contractId: UUID): String? {
        val contract = contractRepository.findById(contractId).orElseThrow {
            IllegalArgumentException("Contract not found: $contractId")
        }
        
        return contract.evidenceHash
    }
}