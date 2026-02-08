package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "missions")
data class Mission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val missionId: String,
    val type: MissionType,
    val priority: Priority,
    val status: MissionStatus,

    val origin: GeoPoint,
    val destination: GeoPoint,

    @Column(columnDefinition = "jsonb")
    val waypoints: List<GeoPoint>,

    @Embedded
    val payload: PayloadSpec,

    @Embedded
    val constraints: MissionConstraints,

    val createdAt: Instant,
    val updatedAt: Instant,
    val scheduledStartTime: Instant?,
    val actualStartTime: Instant?,
    val completedAt: Instant?,

    val assignedNodeId: String?,
    val createdBy: String,
    val approvedBy: String?,

    @OneToMany(mappedBy = "mission", cascade = [CascadeType.ALL])
    val tasks: List<Task> = emptyList()
)

enum class MissionType {
    DELIVERY,
    SURVEILLANCE,
    INSPECTION,
    MAPPING,
    EMERGENCY_RESPONSE,
    MAINTENANCE,
    CUSTOM
}

enum class MissionStatus {
    PENDING,
    PLANNING,
    AWAITING_APPROVAL,
    APPROVED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    FAILED,
    ABORTED
}

enum class Priority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val taskId: String,

    @ManyToOne
    @JoinColumn(name = "mission_id")
    val mission: Mission,

    val type: TaskType,
    val sequence: Int,
    val status: TaskStatus,

    val location: GeoPoint,
    val action: String,
    val duration: Long,

    @Column(columnDefinition = "jsonb")
    val dependencies: List<String>,

    val startedAt: Instant?,
    val completedAt: Instant?
)

enum class TaskType {
    NAVIGATION,
    PICKUP,
    DROPOFF,
    SCAN,
    WAIT,
    CHARGE,
    LAND,
    TAKEOFF
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    SKIPPED
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

@Embeddable
data class PayloadSpec(
    val weightKg: Double,
    val dimensions: Dimensions,
    val type: String,
    val handlingInstructions: String?
)

@Embeddable
data class Dimensions(
    val length: Double,
    val width: Double,
    val height: Double
)

@Embeddable
data class MissionConstraints(
    val maxDuration: Long,
    val maxCost: Double,
    val requiredSensors: List<String>,
    val forbiddenAreas: List<GeoPoint>
)