package com.internal.engine.validation

import com.internal.repository.ManualEntryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Market Oracle - движок для анализа цен и выявления аномалий
 * Сравнивает введенные стоимости материалов и работ с рыночными данными
 */
@Service
class MarketOracle(
    private val manualEntryRepository: ManualEntryRepository
) {

    companion object {
        private const val MAX_DEVIATION_GREEN = 0.15  // до 15% - норма
        private const val MAX_DEVIATION_YELLOW = 0.40 // 15-40% - подозрительно
        // Свыше 40% - явный фрод
    }

    /**
     * Валидация ручного ввода цен
     */
    @Transactional(readOnly = true)
    fun validateManualInput(
        userId: String,
        orderId: String,
        materialsCost: Double,
        laborCost: Double,
        currency: String = "USD"
    ): ValidationVerdict {
        val totalInput = materialsCost + laborCost
        
        // Получаем медианную цену для аналогичных заказов
        val medianPrice = getHistoricalMedian(orderId, currency)
        
        // Рассчитываем отклонение
        val deviation = if (medianPrice > 0) {
            (totalInput - medianPrice) / medianPrice
        } else {
            1.0 // Если нет исторических данных, считаем высоким риском
        }

        return createVerdict(deviation, totalInput, medianPrice)
    }

    /**
     * Получение медианной цены из истории
     */
    private fun getHistoricalMedian(orderId: String, currency: String): Double {
        // В реальной системе здесь будет сложный запрос к ClickHouse
        // с группировкой по типу груза, расстоянию, региону и т.д.
        // Для примера используем упрощенный подход
        
        val basePrice = when {
            orderId.startsWith("ORD-ADR") -> 2000.0  // Опасные грузы дороже
            orderId.startsWith("ORD-REF") -> 1500.0  // Рефрижераторы
            else -> 1000.0  // Обычные грузы
        }
        
        // Добавляем случайное отклонение для реалистичности
        return basePrice * (0.8 + kotlin.random.Random.nextDouble(0.4))
    }

    /**
     * Создание вердикта на основе отклонения
     */
    private fun createVerdict(
        deviation: Double,
        totalInput: Double,
        suggestedMedian: Double
    ): ValidationVerdict {
        return when {
            deviation <= MAX_DEVIATION_GREEN -> {
                ValidationVerdict(
                    status = "GREEN",
                    riskScore = deviation,
                    requiresAppeal = false,
                    requiresBiometrics = false,
                    suggestedMedian = suggestedMedian,
                    comment = "Price within normal range"
                )
            }
            deviation <= MAX_DEVIATION_YELLOW -> {
                ValidationVerdict(
                    status = "YELLOW",
                    riskScore = deviation,
                    requiresAppeal = true,
                    requiresBiometrics = false,
                    suggestedMedian = suggestedMedian,
                    comment = "Price deviation detected, manual review required"
                )
            }
            else -> {
                ValidationVerdict(
                    status = "RED",
                    riskScore = deviation,
                    requiresAppeal = true,
                    requiresBiometrics = true,
                    suggestedMedian = suggestedMedian,
                    comment = "Significant price deviation, biometric verification required"
                )
            }
        }
    }

    /**
     * Сохранение данных о ручном вводе для анализа
     */
    @Transactional
    fun saveManualEntry(
        userId: String,
        orderId: String,
        materialsCost: Double,
        laborCost: Double,
        currency: String,
        verdict: ValidationVerdict
    ) {
        val entry = ManualEntryValidation(
            userId = userId,
            orderId = orderId,
            materialsCost = materialsCost,
            laborCost = laborCost,
            currency = currency,
            riskVerdict = verdict.status,
            aiConfidenceScore = 1.0 - verdict.riskScore,
            requiresAppeal = verdict.requiresAppeal,
            appealStatus = if (verdict.requiresAppeal) "PENDING" else "NONE",
            createdAt = Instant.now()
        )
        
        manualEntryRepository.save(entry)
    }

    /**
     * Обновление статуса апелляции
     */
    @Transactional
    fun updateAppealStatus(validationId: String, status: String, auditorComment: String? = null) {
        manualEntryRepository.updateAppealStatus(validationId, status, auditorComment)
    }
}

/**
 * Результат валидации цен
 */
data class ValidationVerdict(
    val status: String,           // GREEN, YELLOW, RED
    val riskScore: Double,        // 0.0 - 1.0
    val requiresAppeal: Boolean,
    val requiresBiometrics: Boolean,
    val suggestedMedian: Double,
    val comment: String
)

/**
 * Модель записи о ручном вводе
 */
data class ManualEntryValidation(
    val id: String? = null,
    val userId: String,
    val orderId: String,
    val materialsCost: Double,
    val laborCost: Double,
    val currency: String,
    val riskVerdict: String,
    val aiConfidenceScore: Double,
    val requiresAppeal: Boolean,
    val appealStatus: String,
    val createdAt: Instant
)