package com.nodeorb.freight.marketplace.entity

import org.hibernate.annotations.CreationTimestamp
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Снимок состояния SCM на момент подачи заявки
 * Сохраняет состояние проверки соответствия для аудита и прозрачности
 */
@Entity
@Table(name = "scm_snapshots")
data class ScmSnapshotEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,
    
    @Column(nullable = false)
    val bidId: UUID,
    
    @Column(nullable = false)
    val carrierId: UUID,
    
    @Column(nullable = false)
    val masterOrderId: UUID,
    
    @Column(nullable = false)
    val snapshotDate: LocalDateTime,
    
    @Column(nullable = false)
    var complianceStatus: ComplianceStatus,
    
    @Column(columnDefinition = "TEXT")
    var complianceDetails: String? = null,
    
    @Column(nullable = false)
    var securityClearance: SecurityLevel,
    
    @Column(columnDefinition = "TEXT")
    var securityDetails: String? = null,
    
    @Column(nullable = false)
    var riskScore: Double,
    
    @Column(columnDefinition = "TEXT")
    var riskFactors: String? = null,
    
    @Column(nullable = false)
    var auditTrail: String,
    
    @CreationTimestamp
    @Column(nullable = false)
    val createdAt: LocalDateTime? = null
)

enum class ComplianceStatus {
    COMPLIANT,      // Соответствует требованиям
    NON_COMPLIANT,  // Не соответствует требованиям
    PENDING,        // Проверка в процессе
    EXPIRED         // Срок действия проверки истек
}

enum class SecurityLevel {
    NONE,           // Без ограничений
    CONFIDENTIAL,   // Конфиденциально
    SECRET,         // Секретно
    TOP_SECRET,     // Совершенно секретно
    RESTRICTED      // Ограниченный доступ
}