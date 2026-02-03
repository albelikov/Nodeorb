package com.internal.services

import com.internal.repository.IdempotencyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Сервис обеспечения идемпотентности для обработки событий
 * Гарантирует, что повторная отправка события с тем же event_id
 * не будет запускать повторную валидацию
 */
@Service
class IdempotencyService(
    private val idempotencyRepository: IdempotencyRepository
) {

    companion object {
        private const val IDEMPOTENCY_TTL_HOURS = 24 // TTL для записей идемпотентности
    }

    /**
     * Проверка, было ли уже обработано событие с данным ID
     */
    @Transactional(readOnly = true)
    fun isEventProcessed(eventId: String): Boolean {
        val record = idempotencyRepository.findById(eventId)
        return record != null && record.processedAt != null
    }

    /**
     * Регистрация начала обработки события
     */
    @Transactional
    fun registerEventProcessing(eventId: String, eventType: String, userId: String): Boolean {
        // Проверяем, не было ли уже обработано
        if (isEventProcessed(eventId)) {
            return false
        }

        // Регистрируем начало обработки
        val processingRecord = IdempotencyRecord(
            eventId = eventId,
            eventType = eventType,
            userId = userId,
            processedAt = null,
            result = null,
            createdAt = Instant.now(),
            expiresAt = Instant.now().plus(IDEMPOTENCY_TTL_HOURS, ChronoUnit.HOURS)
        )

        idempotencyRepository.save(processingRecord)
        return true
    }

    /**
     * Завершение обработки события
     */
    @Transactional
    fun completeEventProcessing(eventId: String, result: String): Boolean {
        val record = idempotencyRepository.findById(eventId)
            ?: return false

        if (record.processedAt != null) {
            return false // Уже обработано
        }

        val updatedRecord = record.copy(
            processedAt = Instant.now(),
            result = result
        )

        idempotencyRepository.save(updatedRecord)
        return true
    }

    /**
     * Получение результата предыдущей обработки события
     */
    @Transactional(readOnly = true)
    fun getPreviousResult(eventId: String): String? {
        val record = idempotencyRepository.findById(eventId)
        return record?.result
    }

    /**
     * Проверка и получение результата обработки события
     * Если событие уже обработано - возвращает результат
     * Если нет - регистрирует начало обработки
     */
    @Transactional
    fun checkAndRegisterProcessing(eventId: String, eventType: String, userId: String): ProcessingResult {
        // Проверяем, было ли уже обработано
        val existingRecord = idempotencyRepository.findById(eventId)
        
        if (existingRecord != null && existingRecord.processedAt != null) {
            return ProcessingResult(
                shouldProcess = false,
                previousResult = existingRecord.result,
                message = "Event already processed"
            )
        }

        // Проверяем, не просрочена ли запись
        if (existingRecord != null && existingRecord.expiresAt != null && 
            existingRecord.expiresAt < Instant.now()) {
            // Удаляем просроченную запись
            idempotencyRepository.deleteById(eventId)
        }

        // Регистрируем начало обработки
        val processingRecord = IdempotencyRecord(
            eventId = eventId,
            eventType = eventType,
            userId = userId,
            processedAt = null,
            result = null,
            createdAt = Instant.now(),
            expiresAt = Instant.now().plus(IDEMPOTENCY_TTL_HOURS, ChronoUnit.HOURS)
        )

        idempotencyRepository.save(processingRecord)

        return ProcessingResult(
            shouldProcess = true,
            previousResult = null,
            message = "Event registered for processing"
        )
    }

    /**
     * Завершение обработки события с результатом
     */
    @Transactional
    fun completeProcessing(eventId: String, result: String): Boolean {
        val record = idempotencyRepository.findById(eventId)
            ?: return false

        if (record.processedAt != null) {
            return false // Уже обработано
        }

        val updatedRecord = record.copy(
            processedAt = Instant.now(),
            result = result
        )

        idempotencyRepository.save(updatedRecord)
        return true
    }

    /**
     * Очистка просроченных записей
     */
    @Transactional
    fun cleanupExpiredRecords(): Int {
        val now = Instant.now()
        val expiredRecords = idempotencyRepository.findExpiredRecords(now)
        
        expiredRecords.forEach { record ->
            idempotencyRepository.deleteById(record.eventId)
        }
        
        return expiredRecords.size
    }

    /**
     * Получение статистики по идемпотентности
     */
    @Transactional(readOnly = true)
    fun getIdempotencyStats(): IdempotencyStats {
        val totalRecords = idempotencyRepository.count()
        val processedRecords = idempotencyRepository.countProcessed()
        val expiredRecords = idempotencyRepository.countExpired(Instant.now())
        
        return IdempotencyStats(
            totalRecords = totalRecords,
            processedRecords = processedRecords,
            expiredRecords = expiredRecords,
            activeRecords = totalRecords - expiredRecords
        )
    }
}

/**
 * Модели для идемпотентности
 */

data class IdempotencyRecord(
    val eventId: String,
    val eventType: String,
    val userId: String,
    val processedAt: Instant?,
    val result: String?,
    val createdAt: Instant,
    val expiresAt: Instant?
)

data class ProcessingResult(
    val shouldProcess: Boolean,
    val previousResult: String?,
    val message: String
)

data class IdempotencyStats(
    val totalRecords: Long,
    val processedRecords: Long,
    val expiredRecords: Long,
    val activeRecords: Long
)

/**
 * Репозиторий для хранения записей идемпотентности
 */
interface IdempotencyRepository {
    fun findById(eventId: String): IdempotencyRecord?
    fun save(record: IdempotencyRecord)
    fun deleteById(eventId: String)
    fun findExpiredRecords(expiresBefore: Instant): List<IdempotencyRecord>
    fun count(): Long
    fun countProcessed(): Long
    fun countExpired(expiresBefore: Instant): Long
} <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/TrustScoreService.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt
scm-service/src/main/kotlin/com/internal/services/DynamicGeofencingService.kt
scm-service/src/main/kotlin/com/internal/services/ComplianceOraclesService.kt
scm-service/src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt
scm-service/src/main/kotlin/com/internal/services/TrustScoreService.kt
scm-service/src/main/kotlin/com/internal/services/IdempotencyService.kt

# Current Time
2/3/2026, 11:31:33 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
118,863 / 256K tokens used (46%)

# Current Mode
ACT MODE
</environment_details>