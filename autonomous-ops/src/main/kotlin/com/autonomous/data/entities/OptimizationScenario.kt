package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "optimization_scenarios")
data class OptimizationScenario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val scenarioId: String,
    val type: ScenarioType,

    @Embedded
    val parameters: ScenarioParameters,

    val iterations: Int = 1000,

    @Column(columnDefinition = "jsonb")
    val results: List<ScenarioResult>,

    val createdAt: Instant
)

enum class ScenarioType {
    PESSIMISTIC,
    REALISTIC,
    OPTIMISTIC
}

@Embeddable
data class ScenarioParameters(
    val weatherVariability: Double,
    val trafficVariability: Double,
    val equipmentReliability: Double
)

data class ScenarioResult(
    val cost: Double,
    val duration: Long,
    val successProbability: Double
)