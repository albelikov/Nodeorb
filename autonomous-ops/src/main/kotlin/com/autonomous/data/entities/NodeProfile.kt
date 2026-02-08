package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "node_profiles")
data class NodeProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val nodeId: String,
    val name: String,
    val type: NodeType,
    val model: String,
    val status: NodeStatus,

    @Embedded
    val capabilities: NodeCapabilities,

    @Embedded
    val hardwareSpecs: HardwareSpecs,

    val currentLocation: GeoPoint,
    val batteryLevel: Double,
    val currentMissionId: String?,

    val operationalHours: Long,
    val lastMaintenanceDate: Instant,
    val nextMaintenanceDate: Instant,

    @Embedded
    val costParameters: NodeCostParameters,

    val createdAt: Instant,
    val updatedAt: Instant,
    val isActive: Boolean = true
)

enum class NodeType {
    UAV,
    UGV,
    USV,
    HUMANOID,
    ARM,
    HYBRID
}

enum class NodeStatus {
    IDLE,
    BUSY,
    CHARGING,
    MAINTENANCE,
    OFFLINE,
    ERROR,
    EMERGENCY
}

@Embeddable
data class NodeCapabilities(
    val maxSpeed: Double,
    val maxPayload: Double,
    val maxRange: Double,
    val maxAltitude: Double,

    @Column(columnDefinition = "jsonb")
    val sensors: List<SensorType>,

    @Column(columnDefinition = "jsonb")
    val communicationProtocols: List<String>,

    val autonomyLevel: AutonomyLevel
)

enum class SensorType {
    LIDAR,
    RGB_CAMERA,
    THERMAL_CAMERA,
    MULTISPECTRAL,
    RADAR,
    GPS,
    IMU,
    ULTRASONIC,
    BAROMETER,
    MAGNETOMETER
}

enum class AutonomyLevel {
    LEVEL_0,
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
    LEVEL_4,
    LEVEL_5
}

@Embeddable
data class HardwareSpecs(
    val batteryCapacity: Double,
    val batteryType: String,
    val chargingTime: Long,
    val processorModel: String,
    val ramSize: Int,
    val storageSize: Int
)

@Embeddable
data class NodeCostParameters(
    val purchasePrice: Double,
    val depreciationRate: Double,
    val energyCostPerKm: Double,
    val maintenanceCostPerHour: Double,
    val insuranceCostPerMonth: Double,
    val operatorCostPerHour: Double
)