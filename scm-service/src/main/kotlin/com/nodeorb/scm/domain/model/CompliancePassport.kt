package com.nodeorb.scm.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.*
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Geometry
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.*

@Entity
@Table(name = "compliance_passports", schema = "scm")
@DynamicUpdate
@SelectBeforeUpdate
@BatchSize(size = 50)
class CompliancePassport private constructor(
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID,
    
    @Column(name = "keycloak_user_id", nullable = false, unique = true, updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    val keycloakUserId: UUID,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    val entityType: EntityType,
    
    @Column(name = "trust_score", precision = 5, scale = 2)
    @ColumnDefault("50.00")
    var trustScore: BigDecimal = BigDecimal("50.00"),
    
    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false, length = 20)
    @ColumnDefault("'PENDING'")
    var complianceStatus: ComplianceStatus = ComplianceStatus.PENDING,
    
    @Column(name = "is_biometrics_enabled", nullable = false)
    @ColumnDefault("false")
    var isBiometricsEnabled: Boolean = false,
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verification_data", columnDefinition = "jsonb")
    @ColumnDefault("'{}'::jsonb")
    var verificationData: Map<String, Any> = mutableMapOf(),
    
    @Column(name = "geo_fencing_zone")
    @JdbcTypeCode(SqlTypes.OTHER)
    var geoFencingZone: Geometry? = null,
    
    @Column(name = "expires_at")
    var expiresAt: Instant? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    
    @Version
    @Column(name = "version", nullable = false)
    @ColumnDefault("0")
    val version: Long = 0
) {
    constructor(
        keycloakUserId: UUID,
        entityType: EntityType
    ) : this(
        id = UUID.randomUUID(),
        keycloakUserId = keycloakUserId,
        entityType = entityType
    )
    
    @Transient
    val isActive: Boolean
        get() = complianceStatus == ComplianceStatus.VERIFIED && !isExpired()
    
    @Transient
    val isExpired: Boolean
        get() = expiresAt?.isBefore(Instant.now()) ?: false
    
    @Transient
    val riskLevel: RiskLevel
        get() = when {
            trustScore >= BigDecimal("80") -> RiskLevel.LOW
            trustScore >= BigDecimal("50") -> RiskLevel.MEDIUM
            trustScore >= BigDecimal("30") -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
    
    fun updateTrustScore(newScore: BigDecimal) {
        this.trustScore = newScore.coerceIn(
            BigDecimal.ZERO,
            BigDecimal("100.00")
        ).setScale(2, RoundingMode.HALF_UP)
        this.updatedAt = Instant.now()
    }
    
    fun enableBiometrics(): CompliancePassport {
        this.isBiometricsEnabled = true
        this.trustScore = (this.trustScore + BigDecimal("5.00"))
            .coerceAtMost(BigDecimal("100.00"))
        this.updatedAt = Instant.now()
        return this
    }
    
    fun addVerificationData(key: String, value: Any): CompliancePassport {
        this.verificationData = this.verificationData.toMutableMap().apply {
            put(key, value)
        }
        this.updatedAt = Instant.now()
        return this
    }
    
    companion object {
        fun createForCarrier(
            keycloakUserId: UUID,
            companyName: String,
            licenseNumber: String
        ): CompliancePassport {
            return CompliancePassport(
                keycloakUserId = keycloakUserId,
                entityType = EntityType.CARRIER
            ).apply {
                verificationData = mapOf(
                    "companyName" to companyName,
                    "licenseNumber" to licenseNumber,
                    "verifiedAt" to Instant.now().toString()
                )
                complianceStatus = ComplianceStatus.VERIFIED
                expiresAt = Instant.now().plusSeconds(365 * 24 * 60 * 60L) // 1 year
            }
        }
    }
}

enum class EntityType {
    CARRIER, SHIPPER, WAREHOUSE, DRIVER, DISPATCHER,
    CUSTOMS_AGENT, INSURANCE_PROVIDER, AUDITOR
}

enum class ComplianceStatus {
    PENDING, VERIFIED, SUSPENDED, BLACKLISTED, EXPIRED,
    UNDER_REVIEW, RESTRICTED
}

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}