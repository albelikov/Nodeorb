package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "mission_performance_metrics")
data class MissionPerformanceMetrics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val missionId: String,
    val nodeId: String,

    val plannedDuration: Long,
    val actualDuration: Long,
    val durationDeviation: Long,

    val plannedCost: Double,
    val actualCost: Double,
    val costDeviation: Double,
    val costSavings: Double,

    val energyEfficiency: Double,
    val routeEfficiency: Double,

    val successRate: Double,
    val accuracyScore: Double,

    val aiDecisionAccuracy: Double,
    val replansCount: Int,

    val completedAt: Instant
)