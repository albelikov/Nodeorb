package com.autonomous.api.rest

import com.autonomous.api.grpc.MissionServiceGrpcKt
import com.autonomous.api.grpc.CreateMissionRequest
import com.autonomous.api.grpc.GetMissionRequest
import com.autonomous.api.grpc.UpdateMissionStatusRequest
import com.autonomous.api.grpc.CancelMissionRequest
import com.autonomous.api.grpc.ListMissionsRequest
import com.autonomous.api.grpc.MissionInfo
import com.autonomous.api.grpc.MissionParameters
import com.autonomous.api.grpc.Waypoint
import com.autonomous.api.grpc.MissionService
import com.autonomous.api.grpc.MissionServiceGrpc
import io.grpc.ManagedChannelBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api/v1/missions")
class MissionController(
    @Value("\${grpc.server.host:localhost}")
    private val grpcHost: String,
    @Value("\${grpc.server.port:50051}")
    private val grpcPort: Int
) {

    private val channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
        .usePlaintext()
        .build()

    private val missionStub = MissionServiceGrpcKt.MissionServiceCoroutineStub(channel)

    @PostMapping
    suspend fun createMission(@RequestBody request: CreateMissionRequest): ResponseEntity<Any> {
        return try {
            val response = missionStub.createMission(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @GetMapping("/{missionId}")
    suspend fun getMission(@PathVariable missionId: String): ResponseEntity<Any> {
        return try {
            val request = GetMissionRequest.newBuilder().setMissionId(missionId).build()
            val response = missionStub.getMission(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @PutMapping("/{missionId}/status")
    suspend fun updateMissionStatus(@PathVariable missionId: String, @RequestBody request: UpdateMissionStatusRequest): ResponseEntity<Any> {
        return try {
            val response = missionStub.updateMissionStatus(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @PostMapping("/{missionId}/cancel")
    suspend fun cancelMission(@PathVariable missionId: String, @RequestBody request: CancelMissionRequest): ResponseEntity<Any> {
        return try {
            val response = missionStub.cancelMission(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @GetMapping
    suspend fun listMissions(@RequestParam(required = false) missionType: String?,
                             @RequestParam(required = false) status: String?,
                             @RequestParam(defaultValue = "10") pageSize: Int,
                             @RequestParam(required = false) pageToken: String?): ResponseEntity<Any> {
        return try {
            val request = ListMissionsRequest.newBuilder()
                .setPageSize(pageSize)
                .also { if (pageToken != null) it.setPageToken(pageToken) }
                .also { if (missionType != null) it.setMissionType(missionType) }
                .also { if (status != null) it.setStatus(status) }
                .build()

            val response = missionStub.listMissions(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }
}