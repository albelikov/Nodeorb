package com.autonomous.api.dto

import com.autonomous.api.grpc.GetMissionResponse
import com.autonomous.api.grpc.MissionInfo
import java.time.Instant

data class MissionPlan(
    val missionId: String,
    val missionType: String,
    val description: String,
    val parameters: Map<String, Any>,
    val waypoints: List<Waypoint>,
    val startTime: Instant,
    val endTime: Instant,
    val status: String,
    val currentWaypoint: String,
    val progressPercentage: Double
) {
    companion object {
        fun fromGrpcResponse(response: GetMissionResponse): MissionPlan {
            return MissionPlan(
                missionId = response.missionId,
                missionType = response.missionType,
                description = response.description,
                parameters = mapOf(
                    "maxSpeedKmh" to response.parameters.maxSpeedKmh,
                    "maxAltitudeM" to response.parameters.maxAltitudeM,
                    "payloadKg" to response.parameters.payloadKg,
                    "weatherCondition" to response.parameters.weatherCondition,
                    "riskLevel" to response.parameters.riskLevel
                ),
                waypoints = response.waypointsList.map { waypoint ->
                    Waypoint(
                        waypointId = waypoint.waypointId,
                        latitude = waypoint.latitude,
                        longitude = waypoint.longitude,
                        altitude = waypoint.altitude,
                        waypointType = waypoint.waypointType
                    )
                },
                startTime = Instant.ofEpochSecond(response.startTime.seconds, response.startTime.nanos.toLong()),
                endTime = Instant.ofEpochSecond(response.endTime.seconds, response.endTime.nanos.toLong()),
                status = response.status,
                currentWaypoint = response.currentWaypoint,
                progressPercentage = response.progressPercentage
            )
        }
    }
}

data class Waypoint(
    val waypointId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val waypointType: String
)