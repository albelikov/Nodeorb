package com.freight.marketplace.repository

import com.freight.marketplace.entity.ScmSnapshotEntity
import com.freight.marketplace.entity.ComplianceStatus
import com.freight.marketplace.entity.SecurityLevel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

/**
 * Репозиторий для работы с SCM снимками
 */
interface ScmSnapshotRepository : JpaRepository<ScmSnapshotEntity, UUID> {
    
    /**
     * Поиск снимков по ID заявки
     */
    fun findByBidId(bidId: UUID): List<ScmSnapshotEntity>
    
    /**
     * Поиск снимков по ID перевозчика
     */
    fun findByCarrierId(carrierId: UUID, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков по ID мастер-заказа
     */
    fun findByMasterOrderId(masterOrderId: UUID, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск всех снимков по ID мастер-заказа (без пагинации)
     */
    fun findByMasterOrderId(masterOrderId: UUID): List<ScmSnapshotEntity>
    
    /**
     * Поиск снимков по статусу соответствия
     */
    fun findByComplianceStatus(complianceStatus: ComplianceStatus, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков по уровню безопасности
     */
    fun findBySecurityClearance(securityClearance: SecurityLevel, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков по диапазону рискового балла
     */
    fun findByRiskScoreBetween(minRiskScore: Double, maxRiskScore: Double, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск последнего снимка для заявки
     */
    fun findFirstByBidIdOrderBySnapshotDateDesc(bidId: UUID): ScmSnapshotEntity?
    
    /**
     * Поиск снимков по дате создания
     */
    fun findByCreatedAtAfter(createdAt: LocalDateTime, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков с высоким риском
     */
    fun findByRiskScoreGreaterThan(riskScore: Double, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков с просроченной проверкой
     */
    @Query("SELECT s FROM ScmSnapshotEntity s WHERE s.snapshotDate < :cutoffDate")
    fun findExpiredSnapshots(@Param("cutoffDate") cutoffDate: LocalDateTime, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков по комбинации критериев
     */
    @Query("SELECT s FROM ScmSnapshotEntity s WHERE " +
           "(:carrierId IS NULL OR s.carrierId = :carrierId) AND " +
           "(:masterOrderId IS NULL OR s.masterOrderId = :masterOrderId) AND " +
           "(:complianceStatus IS NULL OR s.complianceStatus = :complianceStatus) AND " +
           "(:securityClearance IS NULL OR s.securityClearance = :securityClearance) AND " +
           "(:minRiskScore IS NULL OR s.riskScore >= :minRiskScore) AND " +
           "(:maxRiskScore IS NULL OR s.riskScore <= :maxRiskScore)")
    fun findSnapshotsByCriteria(
        @Param("carrierId") carrierId: UUID? = null,
        @Param("masterOrderId") masterOrderId: UUID? = null,
        @Param("complianceStatus") complianceStatus: ComplianceStatus? = null,
        @Param("securityClearance") securityClearance: SecurityLevel? = null,
        @Param("minRiskScore") minRiskScore: Double? = null,
        @Param("maxRiskScore") maxRiskScore: Double? = null,
        pageable: Pageable
    ): Page<ScmSnapshotEntity>
    
    /**
     * Подсчет снимков по статусу соответствия
     */
    @Query("SELECT s.complianceStatus, COUNT(s) FROM ScmSnapshotEntity s GROUP BY s.complianceStatus")
    fun countByComplianceStatus(): List<Pair<ComplianceStatus, Long>>
    
    /**
     * Подсчет снимков по уровню безопасности
     */
    @Query("SELECT s.securityClearance, COUNT(s) FROM ScmSnapshotEntity s GROUP BY s.securityClearance")
    fun countBySecurityClearance(): List<Pair<SecurityLevel, Long>>
    
    /**
     * Средний рисковый балл по перевозчику
     */
    @Query("SELECT AVG(s.riskScore) FROM ScmSnapshotEntity s WHERE s.carrierId = :carrierId")
    fun findAverageRiskScoreByCarrierId(@Param("carrierId") carrierId: UUID): Double?
    
    /**
     * Максимальный рисковый балл по перевозчику
     */
    @Query("SELECT MAX(s.riskScore) FROM ScmSnapshotEntity s WHERE s.carrierId = :carrierId")
    fun findMaxRiskScoreByCarrierId(@Param("carrierId") carrierId: UUID): Double?
    
    /**
     * Поиск снимков с определенными факторами риска
     */
    @Query("SELECT s FROM ScmSnapshotEntity s WHERE s.riskFactors LIKE %:riskFactor%")
    fun findByRiskFactorsContaining(@Param("riskFactor") riskFactor: String, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков по аудит-трейлу
     */
    @Query("SELECT s FROM ScmSnapshotEntity s WHERE s.auditTrail LIKE %:auditTrail%")
    fun findByAuditTrailContaining(@Param("auditTrail") auditTrail: String, pageable: Pageable): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков за определенный период
     */
    @Query("SELECT s FROM ScmSnapshotEntity s WHERE s.snapshotDate BETWEEN :startDate AND :endDate")
    fun findBySnapshotDateBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков с определенным статусом и уровнем безопасности
     */
    @Query("SELECT s FROM ScmSnapshotEntity s WHERE s.complianceStatus = :complianceStatus AND s.securityClearance = :securityClearance")
    fun findByComplianceStatusAndSecurityClearance(
        @Param("complianceStatus") complianceStatus: ComplianceStatus,
        @Param("securityClearance") securityClearance: SecurityLevel,
        pageable: Pageable
    ): Page<ScmSnapshotEntity>
    
    /**
     * Поиск последних N снимков для перевозчика
     */
    @Query("SELECT s FROM ScmSnapshotEntity s WHERE s.carrierId = :carrierId ORDER BY s.snapshotDate DESC")
    fun findLatestSnapshotsByCarrierId(
        @Param("carrierId") carrierId: UUID,
        pageable: Pageable
    ): Page<ScmSnapshotEntity>
    
    /**
     * Поиск снимков с низким уровнем соответствия
     */
    @Query("SELECT s FROM ScmSnapshotEntity s WHERE s.complianceStatus IN :nonCompliantStatuses")
    fun findByNonCompliantStatus(
        @Param("nonCompliantStatuses") nonCompliantStatuses: List<ComplianceStatus>,
        pageable: Pageable
    ): Page<ScmSnapshotEntity>
    
    companion object {
        val NON_COMPLIANT_STATUSES = listOf(ComplianceStatus.NON_COMPLIANT, ComplianceStatus.EXPIRED)
    }
}