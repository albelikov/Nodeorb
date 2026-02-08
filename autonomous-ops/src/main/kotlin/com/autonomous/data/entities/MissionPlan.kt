package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "mission_plans")
data class MissionPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val planId: String,

    @ManyToOne
    @JoinColumn(name = "mission_id")
    val mission: Mission,

    val version: Int,
    val status: PlanStatus,

    @Embedded
    val route: Route,

    val totalCost: Double,

    @Embedded
    val costBreakdown: CostBreakdown,

    val estimatedDuration: Long,
    val estimatedStartTime: Instant,
    val estimatedEndTime: Instant,

    @Embedded
    val riskAssessment: RiskAssessment,

    @Column(columnDefinition = "jsonb")
    val alternatives: List<AlternativePlan>,

    @Embedded
    val explanation: PlanExplanation,

    val createdAt: Instant,
    val createdBy: String = "AI-System",
    val approvedAt: Instant?,
    val approvedBy: String?
)

enum class PlanStatus {
    DRAFT,
    PROPOSED,
    APPROVED,
    REJECTED,
    IN_EXECUTION,
    COMPLETED,
    OUTDATED
}

@Embeddable
data class Route(
    @Column(columnDefinition = "jsonb")
    val waypoints: List<Waypoint>,

    val totalDistance: Double,

    @Column(columnDefinition = "jsonb")
    val segments: List<RouteSegment>
)

data class Waypoint(
    val sequence: Int,
    val location: GeoPoint,
    val altitude: Double,
    val action: String?,
    val estimatedArrivalTime: Instant,
    val dwellTime: Long = 0
)

data class RouteSegment(
    val from: GeoPoint,
    val to: GeoPoint,
    val distance: Double,
    val duration: Long,
    val averageSpeed: Double,
    val energyConsumption: Double,
    val riskScore: Double
)

@Embeddable
data class CostBreakdown(
    val energyCost: Double,
    val laborCost: Double,
    val depreciationCost: Double,
    val riskPremium: Double,
    val penaltyCost: Double,
    val otherCosts: Double
)

@Embeddable
data class RiskAssessment(
    val overallRiskScore: Double,

    @Column(columnDefinition = "jsonb")
    val riskFactors: List<RiskFactor>
)

data class RiskFactor(
    val type: RiskType,
    val probability: Double,
    val impact: Double,
    val mitigation: String
)

enum class RiskType {
    WEATHER,
    TRAFFIC,
    EQUIPMENT_FAILURE,
    AIRSPACE_VIOLATION,
    BATTERY_DEPLETION,
    COMMUNICATION_LOSS,
    HUMAN_INTERFERENCE,
    REGULATORY
}

data class AlternativePlan(
    val planId: String,
    val route: Route,
    val totalCost: Double,
    val estimatedDuration: Long,
    val riskScore: Double,
    val reasonRejected: String
)

@Embeddable
data class PlanExplanation(
    val summary: String,

    @Column(columnDefinition = "jsonb")
    val decisionTree: DecisionTree,

    @Column(columnDefinition = "jsonb")
    val comparisons: List<PlanComparison>,

    @Column(columnDefinition = "jsonb")
    val keyFactors: List<KeyFactor>
)

data class DecisionTree(
    val nodes: List<DecisionNode>
)

data class DecisionNode(
    val nodeId: String,
    val question: String,
    val answer: String,
    val children: List<String>
)

data class PlanComparison(
    val planAId: String,
    val planBId: String,
    val metric: String,
    val difference: Double,
    val explanation: String
)

data class KeyFactor(
    val name: String,
    val value: String,
    val impact: String,
    val description: String
)