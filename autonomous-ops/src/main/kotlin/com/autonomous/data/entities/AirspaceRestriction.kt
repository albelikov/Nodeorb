package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "airspace_restrictions")
data class AirspaceRestriction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val restrictionId: String,
    val type: RestrictionType,

    @Column(columnDefinition = "geometry")
    val geometry: String,

    val minAltitude: Double,
    val maxAltitude: Double,

    val validFrom: Instant,
    val validUntil: Instant?,

    val reason: String,
    val severity: RestrictionSeverity,

    val source: String,
    val isActive: Boolean = true
)

enum class RestrictionType {
    NO_FLY_ZONE,
    RESTRICTED,
    CONTROLLED,
    WARNING,
    TEMPORARY
}

enum class RestrictionSeverity {
    ABSOLUTE,
    HIGH,
    MEDIUM,
    LOW
}