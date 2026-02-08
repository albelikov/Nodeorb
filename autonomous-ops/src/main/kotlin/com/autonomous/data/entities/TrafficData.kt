package com.autonomous.data.entities

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "traffic_data")
data class TrafficData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val location: GeoPoint,
    val timestamp: Instant,

    val trafficDensity: TrafficDensity,
    val averageSpeed: Double,

    @Column(columnDefinition = "jsonb")
    val incidents: List<TrafficIncident>,

    val source: String
)

enum class TrafficDensity {
    FREE_FLOW,
    LIGHT,
    MODERATE,
    HEAVY,
    CONGESTION
}

data class TrafficIncident(
    val type: String,
    val location: GeoPoint,
    val severity: String,
    val description: String
)