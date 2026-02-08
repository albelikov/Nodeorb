package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "ai_decision_logs")
data class AIDecisionLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val decisionId: String,
    val timestamp: Instant,

    val decisionType: DecisionType,
    val context: String,

    val inputData: String,
    val decision: String,
    val confidence: Double,

    val alternatives: String,
    val reasoning: String,
    val influencingFactors: String,

    val actualOutcome: String?,
    val wasCorrect: Boolean?
)

enum class DecisionType {
    ROUTE_SELECTION,
    TASK_PRIORITIZATION,
    RESOURCE_ALLOCATION,
    EMERGENCY_RESPONSE,
    MAINTENANCE_SCHEDULING,
    COST_OPTIMIZATION
}