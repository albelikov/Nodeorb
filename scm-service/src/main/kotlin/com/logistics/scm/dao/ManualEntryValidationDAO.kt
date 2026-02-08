package com.logistics.scm.dao

import com.logistics.scm.database.ManualEntryValidation
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.LocalDateTime

class ManualEntryValidationDAO {
    
    private fun calculateHash(data: String, previousHash: String?): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = if (previousHash != null) {
            "$data$previousHash"
        } else {
            data
        }
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun getLatestHash(): String? {
        return transaction {
            ManualEntryValidation
                .select(ManualEntryValidation.currentHash)
                .orderBy(ManualEntryValidation.createdAt to SortOrder.DESC)
                .limit(1)
                .map { it[ManualEntryValidation.currentHash] }
                .firstOrNull()
        }
    }
    
    /**
     * Function to search for the last hash in the table before writing a new event.
     * This ensures proper hash chaining for WORM storage integrity.
     */
    fun getLastHashInTable(): String? {
        return transaction {
            ManualEntryValidation
                .select(ManualEntryValidation.currentHash)
                .orderBy(ManualEntryValidation.createdAt to SortOrder.DESC)
                .limit(1)
                .map { it[ManualEntryValidation.currentHash] }
                .firstOrNull()
        }
    }
    
    fun saveValidation(orderId: String, inputValue: BigDecimal, deviation: Double, verdict: String): String {
        return transaction {
            val previousHash = getLatestHash()
            val data = "$orderId$inputValue$deviation$verdict"
            val currentHash = calculateHash(data, previousHash)
            
            val id = ManualEntryValidation.insert {
                it[this.orderId] = orderId
                it[this.inputValue] = inputValue
                it[this.deviation] = deviation
                it[this.verdict] = verdict
                it[this.previousHash] = previousHash
                it[this.currentHash] = currentHash
                it[this.createdAt] = LocalDateTime.now()
            } get ManualEntryValidation.id
            
            currentHash
        }
    }
    
    fun getValidationHistory(orderId: String): List<ValidationRecord> {
        return transaction {
            ManualEntryValidation
                .select { ManualEntryValidation.orderId eq orderId }
                .orderBy(ManualEntryValidation.createdAt to SortOrder.DESC)
                .map {
                    ValidationRecord(
                        orderId = it[ManualEntryValidation.orderId],
                        inputValue = it[ManualEntryValidation.inputValue],
                        deviation = it[ManualEntryValidation.deviation],
                        verdict = it[ManualEntryValidation.verdict],
                        previousHash = it[ManualEntryValidation.previousHash],
                        currentHash = it[ManualEntryValidation.currentHash],
                        createdAt = it[ManualEntryValidation.createdAt]
                    )
                }
        }
    }
    
    fun verifyIntegrity(): Boolean {
        return transaction {
            val records = ManualEntryValidation
                .selectAll()
                .orderBy(ManualEntryValidation.createdAt to SortOrder.ASC)
                .toList()
            
            var previousHash: String? = null
            
            for (record in records) {
                val currentHash = record[ManualEntryValidation.currentHash]
                val previousHashInRecord = record[ManualEntryValidation.previousHash]
                
                if (previousHash != previousHashInRecord) {
                    return@transaction false
                }
                
                val data = "${record[ManualEntryValidation.orderId]}" +
                          "${record[ManualEntryValidation.inputValue]}" +
                          "${record[ManualEntryValidation.deviation]}" +
                          "${record[ManualEntryValidation.verdict]}"
                
                val expectedHash = calculateHash(data, previousHash)
                
                if (currentHash != expectedHash) {
                    return@transaction false
                }
                
                previousHash = currentHash
            }
            
            true
        }
    }
}

data class ValidationRecord(
    val orderId: String,
    val inputValue: BigDecimal,
    val deviation: Double,
    val verdict: String,
    val previousHash: String?,
    val currentHash: String,
    val createdAt: LocalDateTime
)