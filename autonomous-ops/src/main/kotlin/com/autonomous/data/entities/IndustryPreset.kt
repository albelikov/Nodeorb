package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "industry_presets")
data class IndustryPreset(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,
    val description: String,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "cost_parameters_id")
    val defaultCostParameters: CostParameters,

    @Column(columnDefinition = "jsonb")
    val recommendedSensors: List<String>,

    @Column(columnDefinition = "jsonb")
    val typicalMissionTypes: List<String>,

    val safetyLevel: SafetyLevel,

    val isActive: Boolean = true
)

enum class SafetyLevel {
    MINIMAL,
    STANDARD,
    HIGH,
    CRITICAL
}