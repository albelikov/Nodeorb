package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "mission_execution_status")
data class MissionExecutionStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val missionId: String,
    val nodeId: String,

    val currentStatus: MissionStatus,
    val currentTaskId: String?,

    val currentLocation: GeoPoint,
    val currentAltitude: Double,
    val currentSpeed: Double,
    val currentHeading: Double,

    val progressPercentage: Double,
    val completedTasks: Int,
    val totalTasks: Int,

    val batteryLevel: Double,
    val estimatedRemainingRange: Double,

    @Embedded
    val deviationFromPlan: DeviationMetrics,

    @Column(columnDefinition = "jsonb")
    val activeAlerts: List<Alert>,

    val lastUpdated: Instant
)

@Embeddable
data class DeviationMetrics(
    val timeDeviation: Long,
    val distanceDeviation: Double,
    val costDeviation: Double,
    val reasonForDeviation: String?
)

@Entity
@Table(name = "alerts")
data class Alert(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val alertId: String,
    val missionId: String,
    val nodeId: String,

    val type: AlertType,
    val severity: AlertSeverity,

    val message: String,
    val details: String,

    val triggeredAt: Instant,
    val acknowledgedAt: Instant?,
    val acknowledgedBy: String?,
    val resolvedAt: Instant?,

    val recommendedAction: String,
    val autoResolved: Boolean = false
)

enum class AlertType {
    LOW_BATTERY,
    WEATHER_CHANGE,
    OBSTACLE_DETECTED,
    AIRSPACE_VIOLATION,
    COMMUNICATION_LOSS,
    EQUIPMENT_MALFUNCTION,
    ROUTE_DEVIATION,
    SCHEDULE_DELAY,
    EMERGENCY
}

enum class AlertSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}