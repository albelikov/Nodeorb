package com.internal.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Репозиторий для хранения данных о ручном вводе цен (для Market Oracle)
 */
@Repository
interface ManualEntryRepository : JpaRepository<ManualEntryEntity, String> {

    @Query("SELECT AVG(m.materialsCost + m.laborCost) FROM ManualEntryEntity m WHERE m.orderId = :orderId AND m.currency = :currency AND m.createdAt >= :startDate")
    fun getMedianPrice(orderId: String, currency: String, daysBack: Int): Double

    @Query("SELECT m FROM ManualEntryEntity m WHERE m.userId = :userId ORDER BY m.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<ManualEntryEntity>

    @Query("SELECT m FROM ManualEntryEntity m WHERE m.riskVerdict = :verdict ORDER BY m.createdAt DESC")
    fun findByRiskVerdict(verdict: String): List<ManualEntryEntity>

    @Query("UPDATE ManualEntryEntity m SET m.appealStatus = :status, m.updatedAt = :updatedAt WHERE m.id = :validationId")
    fun updateAppealStatus(validationId: String, status: String, updatedAt: Instant): Int

    @Query("SELECT COUNT(m) FROM ManualEntryEntity m WHERE m.userId = :userId AND m.createdAt >= :startDate")
    fun countByUserIdSince(userId: String, startDate: Instant): Long

    @Query("SELECT AVG(m.aiConfidenceScore) FROM ManualEntryEntity m WHERE m.orderId = :orderId")
    fun getAverageConfidenceScore(orderId: String): Double?
}

/**
 * Сущность для хранения данных о ручном вводе цен
 */
data class ManualEntryEntity(
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
    val createdAt: Instant,
    val updatedAt: Instant? = null
) {
    companion object {
        const val TABLE_NAME = "manual_entry_validation"
    }
}