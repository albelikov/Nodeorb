package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "node_analytics")
data class NodeAnalytics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val nodeId: String,
    val periodStart: Instant,
    val periodEnd: Instant,

    val totalMissions: Int,
    val successfulMissions: Int,
    val failedMissions: Int,
    val abortedMissions: Int,

    val totalFlightTime: Long,
    val totalDistance: Double,
    val totalEnergyConsumed: Double,

    val totalRevenue: Double,
    val totalCosts: Double,
    val profitMargin: Double,

    val mtbf: Long,
    val uptime: Double,

    val predictedNextFailure: Instant?,
    val healthScore: Double
)