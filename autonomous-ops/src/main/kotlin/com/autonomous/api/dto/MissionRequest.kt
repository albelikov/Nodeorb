package com.autonomous.api.dto

import com.autonomous.api.grpc.MissionParameters
import com.autonomous.api.grpc.Waypoint
import com.autonomous.api.grpc.CreateMissionRequest
import java.time.Instant

data class MissionRequest(
    val missionId: String,
    val missionType: String,
    val description: String,
    val parameters: MissionParameters,
    val waypoints: List<Waypoint>,
    val startTime: Instant,
    val endTime: Instant
) {
    fun toGrpcRequest(): CreateMissionRequest {
        return CreateMissionRequest.newBuilder()
            .setMissionId(missionId)
            .setMissionType(missionType)
            .setDescription(description)
            .setParameters(parameters)
            .addAllWaypoints(waypoints)
            .setStartTime(com.google.protobuf.Timestamp.newBuilder().setSeconds(startTime.epochSecond).setNanos(startTime.nano))
            .setEndTime(com.google.protobuf.Timestamp.newBuilder().setSeconds(endTime.epochSecond).setNanos(endTime.nano))
            .build()
    }
}