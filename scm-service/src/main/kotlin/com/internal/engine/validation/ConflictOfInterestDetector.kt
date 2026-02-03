package com.internal.engine.validation

import com.internal.repository.ComplianceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Детектор конфликта интересов
 * Автоматически выявляет связи между участниками сделки
 */
@Service
class ConflictOfInterestDetector(
    private val complianceRepository: ComplianceRepository
) {

    companion object {
        private const val MAX_RELATIONSHIP_SCORE = 0.7
    }

    /**
     * Проверка на конфликт интересов
     */
    @Transactional(readOnly = true)
    fun checkConflictOfInterest(
        shipperUserId: String,
        carrierUserId: String,
        orderId: String
    ): ConflictCheckResult {
        // Проверяем прямые связи
        val directRelationship = checkDirectRelationship(shipperUserId, carrierUserId)
        
        // Проверяем косвенные связи через общие компании
        val indirectRelationship = checkIndirectRelationship(shipperUserId, carrierUserId)
        
        // Проверяем историю сделок
        val transactionHistory = checkTransactionHistory(shipperUserId, carrierUserId)
        
        val totalScore = calculateConflictScore(directRelationship, indirectRelationship, transactionHistory)
        
        return ConflictCheckResult(
            hasConflict = totalScore > MAX_RELATIONSHIP_SCORE,
            conflictScore = totalScore,
            detectedRelationships = buildDetectedRelationships(
                directRelationship,
                indirectRelationship,
                transactionHistory
            ),
            riskLevel = determineRiskLevel(totalScore),
            recommendation = generateRecommendation(totalScore)
        )
    }

    /**
     * Проверка прямых связей (родственники, друзья, коллеги)
     */
    private fun checkDirectRelationship(shipperId: String, carrierId: String): Double {
        // В реальной системе здесь будет проверка через социальные сети, базы данных
        // Пока заглушка - возвращает 0.0 (нет прямых связей)
        return 0.0
    }

    /**
     * Проверка косвенных связей через общие компании
     */
    private fun checkIndirectRelationship(shipperId: String, carrierId: String): Double {
        val shipperPassport = complianceRepository.getCompliancePassport(shipperId)
        val carrierPassport = complianceRepository.getCompliancePassport(carrierId)
        
        if (shipperPassport == null || carrierPassport == null) {
            return 0.0
        }

        // Проверяем общие компании в истории
        val shipperCompanies = getCompaniesFromHistory(shipperPassport)
        val carrierCompanies = getCompaniesFromHistory(carrierPassport)
        
        val commonCompanies = shipperCompanies.intersect(carrierCompanies)
        
        return if (commonCompanies.isNotEmpty()) {
            // Чем больше общих компаний, тем выше риск
            minOf(commonCompanies.size * 0.3, 0.8)
        } else {
            0.0
        }
    }

    /**
     * Проверка истории транзакций
     */
    private fun checkTransactionHistory(shipperId: String, carrierId: String): Double {
        // В реальной системе здесь будет анализ истории сделок
        // Проверка на аномально высокие цены, частые сделки и т.д.
        
        // Заглушка: возвращаем 0.1 если есть история сделок
        return 0.1
    }

    /**
     * Расчет общего балла конфликта интересов
     */
    private fun calculateConflictScore(
        direct: Double,
        indirect: Double,
        history: Double
    ): Double {
        // Взвешенная сумма всех факторов
        return (direct * 0.5) + (indirect * 0.3) + (history * 0.2)
    }

    /**
     * Определение уровня риска
     */
    private fun determineRiskLevel(score: Double): String {
        return when {
            score >= 0.8 -> "CRITICAL"
            score >= 0.6 -> "HIGH"
            score >= 0.4 -> "MEDIUM"
            score >= 0.2 -> "LOW"
            else -> "NONE"
        }
    }

    /**
     * Генерация рекомендаций
     */
    private fun generateRecommendation(score: Double): String {
        return when {
            score >= 0.8 -> "BLOCK_TRANSACTION"
            score >= 0.6 -> "MANUAL_REVIEW_REQUIRED"
            score >= 0.4 -> "ENHANCED_MONITORING"
            else -> "PROCEED_NORMAL"
        }
    }

    /**
     * Сбор информации об обнаруженных связях
     */
    private fun buildDetectedRelationships(
        direct: Double,
        indirect: Double,
        history: Double
    ): List<String> {
        val relationships = mutableListOf<String>()
        
        if (direct > 0) relationships.add("Direct relationship detected")
        if (indirect > 0) relationships.add("Common companies in history")
        if (history > 0) relationships.add("Frequent transactions")
        
        return relationships
    }

    /**
     * Получение списка компаний из истории паспорта
     */
    private fun getCompaniesFromHistory(passport: CompliancePassport): Set<String> {
        // В реальной системе здесь будет извлечение из verificationData
        return setOf()
    }
}

/**
 * Результат проверки на конфликт интересов
 */
data class ConflictCheckResult(
    val hasConflict: Boolean,
    val conflictScore: Double,
    val detectedRelationships: List<String>,
    val riskLevel: String,
    val recommendation: String
)