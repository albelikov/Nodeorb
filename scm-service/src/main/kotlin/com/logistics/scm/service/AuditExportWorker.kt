package com.logistics.scm.service

import com.logistics.scm.dao.ManualEntryValidationDAO
import com.logistics.scm.dao.AppealDAO
import com.logistics.scm.database.ManualEntryValidation
import com.logistics.scm.database.Appeal
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.KeyPairGenerator
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

class AuditExportWorker(
    private val validationDAO: ManualEntryValidationDAO,
    private val appealDAO: AppealDAO,
    private val clickHouseService: ClickHouseService,
    private val kafkaEnabled: Boolean = true
) {
    
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val keyPair: KeyPair = generateKeyPair()
    private val privateKey: PrivateKey = keyPair.private
    private val publicKey: PublicKey = keyPair.public
    
    data class AuditEvent(
        val eventTime: String,
        val orderId: String,
        val riskVerdict: String,
        val inputData: String,
        val currentHash: String,
        val prevHash: String?,
        val eventType: String // "VALIDATION" or "APPEAL"
    )
    
    data class EvidencePackage(
        val orderId: String,
        val events: List<AuditEvent>,
        val digitalSeal: String,
        val generatedAt: String
    )
    
    fun exportFinalizedRecords() {
        try {
            // Get finalized validation records (not PENDING_REVIEW)
            val finalizedValidations = transaction {
                ManualEntryValidation
                    .select { ManualEntryValidation.verdict.notIn(listOf("PENDING_REVIEW")) }
                    .orderBy(ManualEntryValidation.createdAt to org.jetbrains.exposed.sql.SortOrder.ASC)
                    .map {
                        AuditEvent(
                            eventTime = it[ManualEntryValidation.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            orderId = it[ManualEntryValidation.orderId],
                            riskVerdict = it[ManualEntryValidation.verdict],
                            inputData = it[ManualEntryValidation.inputValue].toString(),
                            currentHash = it[ManualEntryValidation.currentHash],
                            prevHash = it[ManualEntryValidation.previousHash],
                            eventType = "VALIDATION"
                        )
                    }
            }
            
            // Get finalized appeals (not PENDING)
            val finalizedAppeals = transaction {
                Appeal
                    .select { Appeal.status.notIn(listOf("PENDING")) }
                    .orderBy(Appeal.createdAt to org.jetbrains.exposed.sql.SortOrder.ASC)
                    .map {
                        AuditEvent(
                            eventTime = it[Appeal.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            orderId = "", // Appeals are linked by record_hash, not order_id
                            riskVerdict = it[Appeal.status],
                            inputData = "${it[Appeal.justification]} | Evidence: ${it[Appeal.evidenceUrl]}",
                            currentHash = it[Appeal.id].toString(),
                            prevHash = null, // Appeals don't have previous hash in the same chain
                            eventType = "APPEAL"
                        )
                    }
            }
            
            // Export to ClickHouse
            finalizedValidations.forEach { event ->
                clickHouseService.insertAuditEvent(event)
            }
            
            finalizedAppeals.forEach { event ->
                clickHouseService.insertAuditEvent(event)
            }
            
            // Export to Kafka if enabled
            if (kafkaEnabled) {
                exportToKafka(finalizedValidations + finalizedAppeals)
            }
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to export finalized records: ${e.message}", e)
        }
    }
    
    fun generateEvidencePackage(orderId: String): EvidencePackage {
        try {
            // Get all validation events for the order
            val validationEvents = transaction {
                ManualEntryValidation
                    .select { ManualEntryValidation.orderId eq orderId }
                    .orderBy(ManualEntryValidation.createdAt to org.jetbrains.exposed.sql.SortOrder.ASC)
                    .map {
                        AuditEvent(
                            eventTime = it[ManualEntryValidation.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            orderId = it[ManualEntryValidation.orderId],
                            riskVerdict = it[ManualEntryValidation.verdict],
                            inputData = it[ManualEntryValidation.inputValue].toString(),
                            currentHash = it[ManualEntryValidation.currentHash],
                            prevHash = it[ManualEntryValidation.previousHash],
                            eventType = "VALIDATION"
                        )
                    }
            }
            
            // Get all appeal events for the order (linked by record_hash)
            val appealEvents = transaction {
                Appeal
                    .join(ManualEntryValidation, onColumn = Appeal.recordHash, otherColumn = ManualEntryValidation.currentHash)
                    .select { ManualEntryValidation.orderId eq orderId }
                    .orderBy(Appeal.createdAt to org.jetbrains.exposed.sql.SortOrder.ASC)
                    .map {
                        AuditEvent(
                            eventTime = it[Appeal.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            orderId = orderId,
                            riskVerdict = it[Appeal.status],
                            inputData = "${it[Appeal.justification]} | Evidence: ${it[Appeal.evidenceUrl]}",
                            currentHash = it[Appeal.id].toString(),
                            prevHash = null,
                            eventType = "APPEAL"
                        )
                    }
            }
            
            val allEvents = (validationEvents + appealEvents).sortedBy { it.eventTime }
            
            // Create digital seal
            val digitalSeal = createDigitalSeal(allEvents, orderId)
            
            return EvidencePackage(
                orderId = orderId,
                events = allEvents,
                digitalSeal = digitalSeal,
                generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate evidence package for order $orderId: ${e.message}", e)
        }
    }
    
    fun exportEvidencePackageToClickHouse(evidencePackage: EvidencePackage) {
        try {
            val evidenceJson = objectMapper.writeValueAsString(evidencePackage)
            clickHouseService.insertEvidencePackage(evidencePackage.orderId, evidenceJson)
        } catch (e: Exception) {
            throw RuntimeException("Failed to export evidence package to ClickHouse: ${e.message}", e)
        }
    }
    
    private fun exportToKafka(events: List<AuditEvent>) {
        try {
            val producer = createKafkaProducer()
            
            events.forEach { event ->
                val message = objectMapper.writeValueAsString(event)
                val record = ProducerRecord("security.events", event.orderId, message)
                producer.send(record)
            }
            
            producer.flush()
            producer.close()
        } catch (e: Exception) {
            throw RuntimeException("Failed to export events to Kafka: ${e.message}", e)
        }
    }
    
    private fun createKafkaProducer(): KafkaProducer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092"
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["acks"] = "all"
        props["retries"] = 3
        props["linger.ms"] = 100
        
        return KafkaProducer(props)
    }
    
    private fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }
    
    private fun createDigitalSeal(events: List<AuditEvent>, orderId: String): String {
        try {
            val sealData = """
                Order ID: $orderId
                Event Count: ${events.size}
                Generated At: ${LocalDateTime.now()}
                Events: ${events.joinToString("|") { "${it.eventTime}:${it.riskVerdict}:${it.currentHash}" }}
            """.trimIndent()
            
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(privateKey)
            signature.update(sealData.toByteArray())
            val signedBytes = signature.sign()
            
            return Base64.getEncoder().encodeToString(signedBytes)
        } catch (e: Exception) {
            throw RuntimeException("Failed to create digital seal: ${e.message}", e)
        }
    }
    
    fun verifyDigitalSeal(evidencePackage: EvidencePackage): Boolean {
        try {
            val sealData = """
                Order ID: ${evidencePackage.orderId}
                Event Count: ${evidencePackage.events.size}
                Generated At: ${evidencePackage.generatedAt}
                Events: ${evidencePackage.events.joinToString("|") { "${it.eventTime}:${it.riskVerdict}:${it.currentHash}" }}
            """.trimIndent()
            
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(sealData.toByteArray())
            
            val signedBytes = Base64.getDecoder().decode(evidencePackage.digitalSeal)
            return signature.verify(signedBytes)
        } catch (e: Exception) {
            return false
        }
    }
}