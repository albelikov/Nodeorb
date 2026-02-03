package com.internal.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.internal.integrations.SecurityEventBus
import com.internal.repository.ComplianceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.*

/**
 * Сервис генерации пакетов доказательств для страховых случаев
 * Агрегирует все связанные события по order_id и формирует подписанный JSON-объект
 * с использованием Hash Chaining для подтверждения неизменяемости логов в WORM-хранилище
 */
@Service
class EvidencePackageGenerator(
    private val complianceRepository: ComplianceRepository,
    private val securityEventBus: SecurityEventBus,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val HASH_ALGORITHM = "SHA-256"
        private const val SIGNATURE_ALGORITHM = "SHA256withRSA"
    }

    /**
     * Генерация пакета доказательств по order_id
     */
    @Transactional(readOnly = true)
    fun generateEvidencePackage(orderId: String): EvidencePackage {
        // Собираем все события по заказу
        val events = complianceRepository.getEventsByOrderId(orderId)
        
        // Собираем все валидации ручного ввода
        val validations = complianceRepository.getValidationsByOrderId(orderId)
        
        // Собираем все проверки доступа
        val accessChecks = complianceRepository.getAccessChecksByOrderId(orderId)
        
        // Собираем все геозонные проверки
        val geofenceChecks = complianceRepository.getGeofenceChecksByOrderId(orderId)

        // Создаем цепочку хэшей (Hash Chaining)
        val hashChain = buildHashChain(events, validations, accessChecks, geofenceChecks)
        
        // Формируем пакет доказательств
        val evidencePackage = EvidencePackage(
            orderId = orderId,
            generatedAt = Instant.now(),
            events = events,
            validations = validations,
            accessChecks = accessChecks,
            geofenceChecks = geofenceChecks,
            hashChain = hashChain,
            rootHash = hashChain.lastOrNull()?.hash ?: "",
            signature = generateSignature(hashChain.lastOrNull()?.hash ?: "")
        )

        // Сохраняем пакет доказательств в WORM хранилище
        complianceRepository.saveEvidencePackage(evidencePackage)

        // Отправляем событие о создании пакета
        securityEventBus.sendSecurityEvent(
            SecurityEvent(
                eventId = "EVIDENCE_PACKAGE_GENERATED_${System.currentTimeMillis()}",
                eventType = "EVIDENCE_PACKAGE_GENERATED",
                timestamp = evidencePackage.generatedAt,
                userId = "SYSTEM",
                sourceService = "SCM_SERVICE",
                details = mapOf(
                    "order_id" to orderId,
                    "events_count" to events.size.toString(),
                    "validations_count" to validations.size.toString(),
                    "access_checks_count" to accessChecks.size.toString(),
                    "geofence_checks_count" to geofenceChecks.size.toString(),
                    "root_hash" to evidencePackage.rootHash,
                    "hash_chain_length" to hashChain.size.toString()
                )
            )
        )

        return evidencePackage
    }

    /**
     * Проверка целостности пакета доказательств
     */
    fun verifyEvidencePackage(evidencePackage: EvidencePackage): EvidenceVerificationResult {
        // Проверяем подпись
        val signatureValid = verifySignature(evidencePackage.rootHash, evidencePackage.signature)
        
        // Проверяем целостность цепочки хэшей
        val hashChainValid = verifyHashChain(evidencePackage.hashChain)
        
        // Проверяем соответствие root hash
        val rootHashValid = evidencePackage.hashChain.lastOrNull()?.hash == evidencePackage.rootHash

        val isVerified = signatureValid && hashChainValid && rootHashValid

        return EvidenceVerificationResult(
            isVerified = isVerified,
            signatureValid = signatureValid,
            hashChainValid = hashChainValid,
            rootHashValid = rootHashValid,
            verificationTime = Instant.now()
        )
    }

    /**
     * Получение пакета доказательств по ID
     */
    @Transactional(readOnly = true)
    fun getEvidencePackage(packageId: String): EvidencePackage? {
        return complianceRepository.getEvidencePackage(packageId)
    }

    /**
     * Поиск пакетов доказательств по диапазону дат
     */
    @Transactional(readOnly = true)
    fun searchEvidencePackages(
        startDate: Instant,
        endDate: Instant,
        orderId: String? = null
    ): List<EvidencePackage> {
        return complianceRepository.searchEvidencePackages(startDate, endDate, orderId)
    }

    /**
     * Экспорт пакета доказательств в JSON
     */
    fun exportEvidencePackage(evidencePackage: EvidencePackage): String {
        return objectMapper.writeValueAsString(evidencePackage)
    }

    /**
     * Импорт пакета доказательств из JSON
     */
    fun importEvidencePackage(json: String): EvidencePackage {
        return objectMapper.readValue(json)
    }

    /**
     * Создание цифрового доказательства для конкретного события
     */
    @Transactional(readOnly = true)
    fun createDigitalEvidence(
        orderId: String,
        eventId: String,
        eventType: String,
        eventData: Map<String, Any>
    ): DigitalEvidence {
        // Получаем все связанные события
        val relatedEvents = complianceRepository.getRelatedEvents(orderId, eventId)
        
        // Создаем цепочку хэшей для этого события
        val hashChain = buildEventHashChain(eventId, eventType, eventData, relatedEvents)
        
        // Формируем цифровое доказательство
        val digitalEvidence = DigitalEvidence(
            eventId = eventId,
            orderId = orderId,
            eventType = eventType,
            eventData = eventData,
            hashChain = hashChain,
            rootHash = hashChain.lastOrNull()?.hash ?: "",
            signature = generateSignature(hashChain.lastOrNull()?.hash ?: ""),
            createdAt = Instant.now()
        )

        // Сохраняем в WORM хранилище
        complianceRepository.saveDigitalEvidence(digitalEvidence)

        return digitalEvidence
    }

    /**
     * Проверка цифрового доказательства
     */
    fun verifyDigitalEvidence(digitalEvidence: DigitalEvidence): DigitalEvidenceVerificationResult {
        // Проверяем подпись
        val signatureValid = verifySignature(digitalEvidence.rootHash, digitalEvidence.signature)
        
        // Проверяем целостность цепочки хэшей
        val hashChainValid = verifyHashChain(digitalEvidence.hashChain)
        
        // Проверяем соответствие root hash
        val rootHashValid = digitalEvidence.hashChain.lastOrNull()?.hash == digitalEvidence.rootHash

        val isVerified = signatureValid && hashChainValid && rootHashValid

        return DigitalEvidenceVerificationResult(
            isVerified = isVerified,
            signatureValid = signatureValid,
            hashChainValid = hashChainValid,
            rootHashValid = rootHashValid,
            verificationTime = Instant.now()
        )
    }

    // Вспомогательные методы

    /**
     * Построение цепочки хэшей для всех событий
     */
    private fun buildHashChain(
        events: List<ComplianceEvent>,
        validations: List<ManualEntryValidation>,
        accessChecks: List<AccessCheck>,
        geofenceChecks: List<GeofenceCheck>
    ): List<HashChainNode> {
        val hashChain = mutableListOf<HashChainNode>()
        var previousHash = ""

        // Хэшируем события
        events.forEach { event ->
            val hashInput = "$previousHash${event.eventId}${event.timestamp}${event.eventType}"
            val hash = calculateHash(hashInput)
            hashChain.add(
                HashChainNode(
                    id = event.eventId,
                    type = "EVENT",
                    hash = hash,
                    previousHash = previousHash,
                    timestamp = event.timestamp
                )
            )
            previousHash = hash
        }

        // Хэшируем валидации
        validations.forEach { validation ->
            val hashInput = "$previousHash${validation.id}${validation.createdAt}${validation.riskVerdict}"
            val hash = calculateHash(hashInput)
            hashChain.add(
                HashChainNode(
                    id = validation.id ?: UUID.randomUUID().toString(),
                    type = "VALIDATION",
                    hash = hash,
                    previousHash = previousHash,
                    timestamp = validation.createdAt
                )
            )
            previousHash = hash
        }

        // Хэшируем проверки доступа
        accessChecks.forEach { check ->
            val hashInput = "$previousHash${check.id}${check.timestamp}${check.accessGranted}"
            val hash = calculateHash(hashInput)
            hashChain.add(
                HashChainNode(
                    id = check.id,
                    type = "ACCESS_CHECK",
                    hash = hash,
                    previousHash = previousHash,
                    timestamp = check.timestamp
                )
            )
            previousHash = hash
        }

        // Хэшируем геозонные проверки
        geofenceChecks.forEach { check ->
            val hashInput = "$previousHash${check.id}${check.timestamp}${check.isInside}"
            val hash = calculateHash(hashInput)
            hashChain.add(
                HashChainNode(
                    id = check.id,
                    type = "GEOFENCE_CHECK",
                    hash = hash,
                    previousHash = previousHash,
                    timestamp = check.timestamp
                )
            )
            previousHash = hash
        }

        return hashChain
    }

    /**
     * Построение цепочки хэшей для конкретного события
     */
    private fun buildEventHashChain(
        eventId: String,
        eventType: String,
        eventData: Map<String, Any>,
        relatedEvents: List<ComplianceEvent>
    ): List<HashChainNode> {
        val hashChain = mutableListOf<HashChainNode>()
        var previousHash = ""

        // Добавляем основное событие
        val eventHashInput = "$previousHash$eventId${Instant.now()}$eventType${eventData.toString()}"
        val eventHash = calculateHash(eventHashInput)
        hashChain.add(
            HashChainNode(
                id = eventId,
                type = eventType,
                hash = eventHash,
                previousHash = previousHash,
                timestamp = Instant.now()
            )
        )
        previousHash = eventHash

        // Добавляем связанные события
        relatedEvents.forEach { event ->
            val relatedHashInput = "$previousHash${event.eventId}${event.timestamp}${event.eventType}"
            val relatedHash = calculateHash(relatedHashInput)
            hashChain.add(
                HashChainNode(
                    id = event.eventId,
                    type = "RELATED_EVENT",
                    hash = relatedHash,
                    previousHash = previousHash,
                    timestamp = event.timestamp
                )
            )
            previousHash = relatedHash
        }

        return hashChain
    }

    /**
     * Расчет хэша
     */
    private fun calculateHash(input: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Генерация цифровой подписи
     */
    private fun generateSignature(data: String): String {
        // В реальной системе здесь будет использование приватного ключа
        // Пока заглушка
        return "SIGNATURE_${calculateHash(data)}"
    }

    /**
     * Проверка цифровой подписи
     */
    private fun verifySignature(data: String, signature: String): Boolean {
        // В реальной системе здесь будет проверка с использованием публичного ключа
        // Пока заглушка
        return signature.startsWith("SIGNATURE_")
    }

    /**
     * Проверка целостности цепочки хэшей
     */
    private fun verifyHashChain(hashChain: List<HashChainNode>): Boolean {
        var expectedHash = ""
        
        for (node in hashChain) {
            if (node.previousHash != expectedHash) {
                return false
            }
            
            val hashInput = "${node.previousHash}${node.id}${node.type}${node.timestamp}"
            val calculatedHash = calculateHash(hashInput)
            
            if (calculatedHash != node.hash) {
                return false
            }
            
            expectedHash = node.hash
        }
        
        return true
    }
}

/**
 * Модели для пакетов доказательств
 */

data class EvidencePackage(
    val id: String? = null,
    val orderId: String,
    val generatedAt: Instant,
    val events: List<ComplianceEvent>,
    val validations: List<ManualEntryValidation>,
    val accessChecks: List<AccessCheck>,
    val geofenceChecks: List<GeofenceCheck>,
    val hashChain: List<HashChainNode>,
    val rootHash: String,
    val signature: String
)

data class EvidenceVerificationResult(
    val isVerified: Boolean,
    val signatureValid: Boolean,
    val hashChainValid: Boolean,
    val rootHashValid: Boolean,
    val verificationTime: Instant
)

data class HashChainNode(
    val id: String,
    val type: String,
    val hash: String,
    val previousHash: String,
    val timestamp: Instant
)

data class DigitalEvidence(
    val id: String? = null,
    val eventId: String,
    val orderId: String,
    val eventType: String,
    val eventData: Map<String, Any>,
    val hashChain: List<HashChainNode>,
    val rootHash: String,
    val signature: String,
    val createdAt: Instant
)

data class DigitalEvidenceVerificationResult(
    val isVerified: Boolean,
    val signatureValid: Boolean,
    val hashChainValid: Boolean,
    val rootHashValid: Boolean,
    val verificationTime: Instant
)

/**
 * Модели для хранения в репозитории
 */

data class ComplianceEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: String,
    val details: Map<String, Any>
)

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

data class AccessCheck(
    val id: String,
    val userId: String,
    val orderId: String,
    val accessGranted: Boolean,
    val timestamp: Instant,
    val reason: String? = null
)

data class GeofenceCheck(
    val id: String,
    val userId: String,
    val orderId: String,
    val isInside: Boolean,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val violationReason: String? = null
)

data class SecurityEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: String,
    val sourceService: String,
    val details: Map<String, String>
) <environment_details>
# Visual Studio Code - Insiders Visible Files
scm-service/src/main/kotlin/com/internal/services/ComplianceOraclesService.kt

# Visual Studio Code - Insiders Open Tabs
scm-service/src/main/kotlin/com/internal/services/ManualCostValidationService.kt
scm-service/src/main/kotlin/com/internal/services/DynamicGeofencingService.kt
scm-service/src/main/kotlin/com/internal/services/ComplianceOraclesService.kt
scm-service/src/main/kotlin/com/internal/services/EvidencePackageGenerator.kt

# Current Time
2/3/2026, 11:28:50 PM (Europe/Kiev, UTC+2:00)

# Context Window Usage
103,452 / 256K tokens used (40%)

# Current Mode
ACT MODE
</environment_details>