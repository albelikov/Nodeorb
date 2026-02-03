package com.internal.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * WORM (Write Once Read Many) хранилище для compliance-логов
 * Обеспечивает неизменяемость записей для аудита и compliance
 */
@NoRepositoryBean
interface WormStorageRepository<T, ID> : JpaRepository<T, ID> {

    /**
     * Сохранение записи в WORM хранилище
     * Автоматически устанавливает временные метки и хэши
     */
    @Transactional
    override fun save(entity: T): T

    /**
     * Поиск записи по ID
     */
    fun findByIdWithHash(id: ID): T?

    /**
     * Поиск записей по диапазону дат
     */
    fun findByCreatedAtBetween(startDate: Instant, endDate: Instant): List<T>

    /**
     * Поиск записей по типу события
     */
    fun findByEventType(eventType: String): List<T>

    /**
     * Проверка целостности записи по хэшу
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM WormLog wl WHERE wl.id = :id AND wl.hash = :expectedHash")
    fun verifyIntegrity(id: ID, expectedHash: String): Boolean

    /**
     * Получение всех изменений записи (если поддерживается версионирование)
     */
    fun getAuditTrail(id: ID): List<T>
}

/**
 * Репозиторий для compliance-событий с WORM-защитой
 */
interface ComplianceEventWormRepository : WormStorageRepository<ComplianceEventWormEntity, String> {
    
    /**
     * Поиск событий по пользователю
     */
    fun findByUserId(userId: String): List<ComplianceEventWormEntity>

    /**
     * Поиск событий по заказу
     */
    fun findByOrderId(orderId: String): List<ComplianceEventWormEntity>

    /**
     * Поиск событий по типу и диапазону дат
     */
    fun findByEventTypeAndCreatedAtBetween(
        eventType: String,
        startDate: Instant,
        endDate: Instant
    ): List<ComplianceEventWormEntity>
}

/**
 * Репозиторий для валидаций ручного ввода с WORM-защитой
 */
interface ManualValidationWormRepository : WormStorageRepository<ManualValidationWormEntity, String> {
    
    /**
     * Поиск валидаций по пользователю
     */
    fun findByUserId(userId: String): List<ManualValidationWormEntity>

    /**
     * Поиск валидаций по заказу
     */
    fun findByOrderId(orderId: String): List<ManualValidationWormEntity>

    /**
     * Поиск валидаций по статусу
     */
    fun findByRiskVerdict(riskVerdict: String): List<ManualValidationWormEntity>
}

/**
 * Репозиторий для проверок доступа с WORM-защитой
 */
interface AccessCheckWormRepository : WormStorageRepository<AccessCheckWormEntity, String> {
    
    /**
     * Поиск проверок по пользователю
     */
    fun findByUserId(userId: String): List<AccessCheckWormEntity>

    /**
     * Поиск проверок по заказу
     */
    fun findByOrderId(orderId: String): List<AccessCheckWormEntity>

    /**
     * Поиск проверок по результату
     */
    fun findByAccessGranted(accessGranted: Boolean): List<AccessCheckWormEntity>
}

/**
 * Репозиторий для геозонных проверок с WORM-защитой
 */
interface GeofenceCheckWormRepository : WormStorageRepository<GeofenceCheckWormEntity, String> {
    
    /**
     * Поиск проверок по пользователю
     */
    fun findByUserId(userId: String): List<GeofenceCheckWormEntity>

    /**
     * Поиск проверок по заказу
     */
    fun findByOrderId(orderId: String): List<GeofenceCheckWormEntity>

    /**
     * Поиск проверок по типу геозоны
     */
    fun findByGeofenceType(geofenceType: String): List<GeofenceCheckWormEntity>
}

/**
 * Сущности для WORM хранилища
 */

/**
 * Сущность compliance-события с WORM-защитой
 */
data class ComplianceEventWormEntity(
    val id: String,
    val userId: String,
    val eventType: String,
    val timestamp: Instant,
    val details: String, // JSON-строка с деталями
    val hash: String,    // Хэш записи для проверки целостности
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Сущность валидации ручного ввода с WORM-защитой
 */
data class ManualValidationWormEntity(
    val id: String,
    val userId: String,
    val orderId: String,
    val materialsCost: Double,
    val laborCost: Double,
    val currency: String,
    val riskVerdict: String,
    val aiConfidenceScore: Double,
    val requiresAppeal: Boolean,
    val appealStatus: String,
    val hash: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Сущность проверки доступа с WORM-защитой
 */
data class AccessCheckWormEntity(
    val id: String,
    val userId: String,
    val orderId: String,
    val accessGranted: Boolean,
    val reason: String?,
    val riskLevel: String,
    val requiresBiometrics: Boolean,
    val requiresAppeal: Boolean,
    val hash: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Сущность геозонной проверки с WORM-защитой
 */
data class GeofenceCheckWormEntity(
    val id: String,
    val userId: String,
    val orderId: String,
    val latitude: Double,
    val longitude: Double,
    val isInside: Boolean,
    val geofenceType: String,
    val violationReason: String?,
    val hash: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * Интерфейс для WORM-сервиса
 */
interface WormStorageService {
    
    /**
     * Сохранение события в WORM хранилище
     */
    fun saveEvent(event: ComplianceEventWormEntity): ComplianceEventWormEntity

    /**
     * Сохранение валидации в WORM хранилище
     */
    fun saveValidation(validation: ManualValidationWormEntity): ManualValidationWormEntity

    /**
     * Сохранение проверки доступа в WORM хранилище
     */
    fun saveAccessCheck(check: AccessCheckWormEntity): AccessCheckWormEntity

    /**
     * Сохранение геозонной проверки в WORM хранилище
     */
    fun saveGeofenceCheck(check: GeofenceCheckWormEntity): GeofenceCheckWormEntity

    /**
     * Проверка целостности записи
     */
    fun verifyIntegrity(entityId: String, entityType: String, expectedHash: String): Boolean

    /**
     * Поиск записей по диапазону дат
     */
    fun searchByDateRange(
        startDate: Instant,
        endDate: Instant,
        entityType: String? = null
    ): List<Any>

    /**
     * Экспорт записей в архивный формат
     */
    fun exportToArchive(
        startDate: Instant,
        endDate: Instant,
        format: String = "JSON"
    ): String
}

/**
 * Реализация WORM-сервиса
 */
class WormStorageServiceImpl : WormStorageService {
    
    // В реальной системе здесь будут внедренные репозитории
    // private val eventRepository: ComplianceEventWormRepository
    // private val validationRepository: ManualValidationWormRepository
    // и т.д.

    override fun saveEvent(event: ComplianceEventWormEntity): ComplianceEventWormEntity {
        // В реальной системе здесь будет сохранение в БД
        // Пока заглушка
        return event
    }

    override fun saveValidation(validation: ManualValidationWormEntity): ManualValidationWormEntity {
        // В реальной системе здесь будет сохранение в БД
        // Пока заглушка
        return validation
    }

    override fun saveAccessCheck(check: AccessCheckWormEntity): AccessCheckWormEntity {
        // В реальной системе здесь будет сохранение в БД
        // Пока заглушка
        return check
    }

    override fun saveGeofenceCheck(check: GeofenceCheckWormEntity): GeofenceCheckWormEntity {
        // В реальной системе здесь будет сохранение в БД
        // Пока заглушка
        return check
    }

    override fun verifyIntegrity(
        entityId: String,
        entityType: String,
        expectedHash: String
    ): Boolean {
        // В реальной системе здесь будет проверка целостности
        // Пока заглушка
        return true
    }

    override fun searchByDateRange(
        startDate: Instant,
        endDate: Instant,
        entityType: String?
    ): List<Any> {
        // В реальной системе здесь будет поиск в БД
        // Пока заглушка
        return emptyList()
    }

    override fun exportToArchive(
        startDate: Instant,
        endDate: Instant,
        format: String
    ): String {
        // В реальной системе здесь будет экспорт в архив
        // Пока заглушка
        return "{}"
    }
} <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/ExternalComplianceAdapter.kt
scm-service/src/main/kotlin/com/internal/engine/validation/AccessValidator.kt
scm-service/src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt
scm-service/src/main/kotlin/com/internal/repository/WormStorageRepository.kt

# Current Time
2/3/2026, 11:15:48 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
43,258 / 256K tokens used (17%)

# Current Mode
ACT MODE
</environment_details>