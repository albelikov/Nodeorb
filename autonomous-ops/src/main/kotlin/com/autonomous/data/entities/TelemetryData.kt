package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "telemetry_data")
data class TelemetryData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val nodeId: String,
    val timestamp: Instant,

    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double,
    val heading: Double,

    val batteryVoltage: Double,
    val batteryCurrent: Double,
    val batteryPercentage: Double,
    val temperature: Double,
    val vibration: Double?,
    val pressure: Double?,

    val gpsQuality: Int,
    val communicationSignal: Int,

    @Column(columnDefinition = "jsonb")
    val rawSensorData: String?
)