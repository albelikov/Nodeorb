package com.internal.services

import com.internal.engine.validation.MarketOracle
import com.internal.integrations.SecurityEventBus
import com.internal.repository.ManualEntryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Сервис для обработки MANUAL_COST_ENTRY событий
 * Реализует критически важный механизм проверки ручного ввода
 */
@Service
class ManualCostValidationService(
    private val marketOracle: MarketOracle,
    private val manualEntryRepository: ManualEntryRepository,
    private val securityEventBus: SecurityEventBus
) {

    companion object {
        private const val AUDIT_REQUIRED_THRESHOLD = 0.20  // 20%
        private const val REJECTED_THRESHOLD = 0.40        // 40%
    }

    /**
     * Обработка MANUAL_COST_ENTRY события
     * Согласно ТЗ: сначала человек вводит данные, затем ИИ валидирует и выбирает
     */
    @Transactional
    fun processManualCostEntry(
        userId: String,
        orderId: String,
        eventId: String,
        materialsCost: Double,
        laborCost: Double,
        currency: String = "USD"
    ): ManualCostValidationResult {
        // Проверка идемпотентности
        if (isEventAlreadyProcessed(eventId)) {
            return getPreviousResult(eventId)
        }

        // Получаем рыночную медиану из аналитической БД
        val medianPrice = getMarketMedian(orderId, currency)
        
        // Рассчитываем отклонение
        val totalInput = materialsCost + laborCost
        val deviation = calculateDeviation(medianPrice, totalInput)

        // Принятие решений по ТЗ
        val validationResult = when {
            deviation > REJECTED_THRESHOLD -> {
                // Отклонение > 40% -> немедленный возврат статуса REJECTED
                ManualCostValidationResult(
                    eventId = eventId,
                    status = "REJECTED",
                    deviation = deviation,
                    medianPrice = medianPrice,
                    suggestedMedian = medianPrice,
                    auditRequired = false, // Не требуется, так как сразу REJECTED
                    reason = "Significant price deviation > 40%"
                )
            }
            deviation > AUDIT_REQUIRED_THRESHOLD -> {
                // Отклонение > 20% -> пометить событие тегом audit_required
                ManualCostValidationResult(
                    eventId = eventId,
                    status = "AUDIT_REQUIRED",
                    deviation = deviation,
                    medianPrice = medianPrice,
                    suggestedMedian = medianPrice,
                    auditRequired = true,
                    reason = "Price deviation > 20%, requires audit"
                )
            }
            else -> {
                // Нормальное отклонение
                ManualCostValidationResult(
                    eventId = eventId,
                    status = "APPROVED",
                    deviation = deviation,
                    medianPrice = medianPrice,
                    suggestedMedian = medianPrice,
                    auditRequired = false,
                    reason = "Price within acceptable range"
                )
            }
        }

        // Сохраняем результат валидации
        saveValidationResult(
            userId = userId,
            orderId = orderId,
            eventId = eventId,
            materialsCost = materialsCost,
            laborCost = laborCost,
            currency = currency,
            validationResult = validationResult
        )

        // Отправляем событие в Security Event Bus
        sendValidationEvent(userId, orderId, eventId, validationResult)

        return validationResult
    }

    /**
     * Проверка идемпотентности события
     */
    private fun isEventAlreadyProcessed(eventId: String): Boolean {
        return manualEntryRepository.existsByEventId(eventId)
    }

    /**
     * Получение предыдущего результата валидации
     */
    private fun getPreviousResult(eventId: String): ManualCostValidationResult {
        val validation = manualEntryRepository.findByEventId(eventId)
            ?: throw IllegalStateException("Validation not found for event: $eventId")

        return ManualCostValidationResult(
            eventId = eventId,
            status = validation.riskVerdict,
            deviation = calculateDeviation(validation.materialsCost + validation.laborCost, validation.materialsCost + validation.laborCost),
            medianPrice = validation.materialsCost + validation.laborCost, // Упрощенно
            suggestedMedian = validation.materialsCost + validation.laborCost,
            auditRequired = validation.appealStatus == "PENDING",
            reason = "Previously processed"
        )
    }

    /**
     * Получение рыночной медианы из аналитической БД
     */
    private fun getMarketMedian(orderId: String, currency: String): Double {
        // В реальной системе здесь будет запрос к ClickHouse/аналитической БД
        // с группировкой по типу груза, расстоянию, региону и т.д.
        
        return marketOracle.getHistoricalMedian(orderId, currency)
    }

    /**
     * Расчет отклонения от медианы
     */
    private fun calculateDeviation(median: Double, actual: Double): Double {
        return if (median > 0) {
            (actual - median) / median
        } else {
            1.0 // Если нет исторических данных, считаем высоким риском
        }
    }

    /**
     * Сохранение результата валидации
     */
    private fun saveValidationResult(
        userId: String,
        orderId: String,
        eventId: String,
        materialsCost: Double,
        laborCost: Double,
        currency: String,
        validationResult: ManualCostValidationResult
    ) {
        val validation = ManualEntryValidation(
            userId = userId,
            orderId = orderId,
            materialsCost = materialsCost,
            laborCost = laborCost,
            currency = currency,
            riskVerdict = validationResult.status,
            aiConfidenceScore = 1.0 - validationResult.deviation,
            requiresAppeal = validationResult.auditRequired,
            appealStatus = if (validationResult.auditRequired) "PENDING" else "NONE",
            createdAt = Instant.now()
        )

        manualEntryRepository.save(validation)
    }

    /**
     * Отправка события в Security Event Bus
     */
    private fun sendValidationEvent(
        userId: String,
        orderId: String,
        eventId: String,
        validationResult: ManualCostValidationResult
    ) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = eventId,
                eventType = "MANUAL_COST_VALIDATION",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "order_id" to orderId,
                    "event_id" to eventId,
                    "status" to validationResult.status,
                    "deviation" to validationResult.deviation.toString(),
                    "median_price" to validationResult.medianPrice.toString(),
                    "suggested_median" to validationResult.suggestedMedian.toString(),
                    "audit_required" to validationResult.auditRequired.toString(),
                    "reason" to validationResult.reason
                )
            )
        )
    }

    /**
     * Обновление статуса аудита
     */
    @Transactional
    fun updateAuditStatus(
        eventId: String,
        auditStatus: String,
        auditorComment: String? = null
    ): Boolean {
        return try {
            manualEntryRepository.updateAppealStatus(eventId, auditStatus, auditorComment)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Подача апелляции на заблокированную цену (Human-in-the-Loop)
     * Позволяет пользователю прикрепить доказательства (фото счета и т.д.)
     */
    @Transactional
    fun submitAppeal(
        eventId: String,
        userId: String,
        appealEvidence: AppealEvidence
    ): AppealResult {
        // Проверяем, что событие существует и требует аудита
        val validation = manualEntryRepository.findByEventId(eventId)
            ?: throw IllegalStateException("Validation not found for event: $eventId")

        if (validation.appealStatus != "PENDING") {
            throw IllegalStateException("Appeal not allowed for this validation")
        }

        // Сохраняем доказательства апелляции
        val appeal = Appeal(
            eventId = eventId,
            userId = userId,
            evidence = appealEvidence,
            status = "SUBMITTED",
            submittedAt = Instant.now()
        )

        manualEntryRepository.saveAppeal(appeal)

        // Отправляем событие о подаче апелляции
        sendAppealEvent(userId, eventId, appealEvidence)

        return AppealResult(
            eventId = eventId,
            status = "SUBMITTED",
            message = "Appeal submitted successfully",
            evidenceId = appeal.id
        )
    }

    /**
     * Рассмотрение апелляции аудитором
     */
    @Transactional
    fun reviewAppeal(
        eventId: String,
        auditorId: String,
        decision: String, // APPROVED, REJECTED
        auditorComment: String
    ): AppealReviewResult {
        val appeal = manualEntryRepository.findAppealByEventId(eventId)
            ?: throw IllegalStateException("Appeal not found for event: $eventId")

        // Обновляем статус апелляции
        val updatedAppeal = appeal.copy(
            status = decision,
            reviewedAt = Instant.now(),
            reviewedBy = auditorId,
            auditorComment = auditorComment
        )

        manualEntryRepository.updateAppeal(updatedAppeal)

        // Обновляем статус валидации
        manualEntryRepository.updateAppealStatus(eventId, decision, auditorComment)

        // Отправляем событие о рассмотрении апелляции
        sendAppealReviewEvent(eventId, auditorId, decision, auditorComment)

        return AppealReviewResult(
            eventId = eventId,
            decision = decision,
            auditorComment = auditorComment,
            reviewedAt = updatedAppeal.reviewedAt!!
        )
    }

    /**
     * Получение статуса апелляции
     */
    fun getAppealStatus(eventId: String): AppealStatus {
        val appeal = manualEntryRepository.findAppealByEventId(eventId)
        
        return if (appeal != null) {
            AppealStatus(
                eventId = eventId,
                status = appeal.status,
                submittedAt = appeal.submittedAt,
                reviewedAt = appeal.reviewedAt,
                reviewedBy = appeal.reviewedBy,
                auditorComment = appeal.auditorComment
            )
        } else {
            AppealStatus(
                eventId = eventId,
                status = "NONE",
                submittedAt = null,
                reviewedAt = null,
                reviewedBy = null,
                auditorComment = null
            )
        }
    }

    /**
     * Отправка события о подаче апелляции
     */
    private fun sendAppealEvent(userId: String, eventId: String, evidence: AppealEvidence) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "APPEAL_SUBMITTED_${System.currentTimeMillis()}",
                eventType = "APPEAL_SUBMITTED",
                timestamp = Instant.now(),
                userId = userId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "event_id" to eventId,
                    "evidence_type" to evidence.evidenceType,
                    "evidence_count" to evidence.evidenceItems.size.toString(),
                    "has_photos" to evidence.evidenceItems.any { it.type == "PHOTO" }.toString()
                )
            )
        )
    }

    /**
     * Отправка события о рассмотрении апелляции
     */
    private fun sendAppealReviewEvent(
        eventId: String,
        auditorId: String,
        decision: String,
        auditorComment: String
    ) {
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "APPEAL_REVIEWED_${System.currentTimeMillis()}",
                eventType = "APPEAL_REVIEWED",
                timestamp = Instant.now(),
                userId = auditorId,
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "event_id" to eventId,
                    "decision" to decision,
                    "auditor_comment" to auditorComment
                )
            )
        )
    }
}

/**
 * Результат валидации MANUAL_COST_ENTRY
 */
data class ManualCostValidationResult(
    val eventId: String,
    val status: String,           // APPROVED, AUDIT_REQUIRED, REJECTED
    val deviation: Double,        // Отклонение от медианы (0.0 - 1.0)
    val medianPrice: Double,      // Рыночная медиана
    val suggestedMedian: Double,  // Предложенная медиана для пользователя
    val auditRequired: Boolean,   // Требуется аудит (если отклонение > 20%)
    val reason: String            // Причина решения
)

/**
 * Модель для Security Event Bus
 */
data class SecurityEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: String,
    val sourceService: String,
    val details: Map<String, String>
)

/**
 * Модели для апелляций (Human-in-the-Loop)
 */

data class AppealEvidence(
    val evidenceType: String, // DOCUMENT, PHOTO, INVOICE, etc.
    val evidenceItems: List<EvidenceItem>,
    val description: String? = null
)

data class EvidenceItem(
    val id: String,
    val type: String, // PHOTO, PDF, TEXT, etc.
    val url: String, // Ссылка на файл в хранилище
    val description: String? = null,
    val uploadedAt: Instant
)

data class Appeal(
    val id: String? = null,
    val eventId: String,
    val userId: String,
    val evidence: AppealEvidence,
    val status: String, // SUBMITTED, APPROVED, REJECTED, REVIEWED
    val submittedAt: Instant,
    val reviewedAt: Instant? = null,
    val reviewedBy: String? = null,
    val auditorComment: String? = null
)

data class AppealResult(
    val eventId: String,
    val status: String,
    val message: String,
    val evidenceId: String? = null
)

data class AppealReviewResult(
    val eventId: String,
    val decision: String,
    val auditorComment: String,
    val reviewedAt: Instant
)

data class AppealStatus(
    val eventId: String,
    val status: String,
    val submittedAt: Instant?,
    val reviewedAt: Instant?,
    val reviewedBy: String?,
    val auditorComment: String?
) <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/DynamicGeofencingService.kt
scm-service/src/main/kotlin/com/internal/services/ComplianceOraclesService.kt
scm-service/src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt
scm-service/src/main/kotlin/com/internal/services/TrustScoreService.kt
scm-service/src/main/kotlin/com/internal/services/IdempotencyService.kt
scm-service/src/main/resources/db/migration/V1__create_median_prices_table.sql
scm-service/build.gradle.kts
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt

# Current Time
2/3/2026, 11:45:00 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
118,354 / 256K tokens used (46%)

# Current Mode
ACT MODE
</environment_details>