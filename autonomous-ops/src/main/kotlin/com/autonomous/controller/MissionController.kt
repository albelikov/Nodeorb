package com.autonomous.controller

import com.autonomous.data.entities.*
import com.autonomous.services.MissionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/missions")
class MissionController(
    private val missionService: MissionService
) {

    @PostMapping
    fun createMission(@RequestBody mission: Mission): ResponseEntity<Mission> {
        val createdMission = missionService.createMission(mission.copy(
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            status = MissionStatus.PENDING
        ))
        return ResponseEntity.ok(createdMission)
    }

    @GetMapping("/{missionId}")
    fun getMission(@PathVariable missionId: String): ResponseEntity<Mission?> {
        val mission = missionService.getMission(missionId)
        return if (mission != null) {
            ResponseEntity.ok(mission)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{missionId}")
    fun updateMission(@PathVariable missionId: String, @RequestBody mission: Mission): ResponseEntity<Mission?> {
        val existingMission = missionService.getMission(missionId)
        return if (existingMission != null) {
            val updatedMission = missionService.updateMission(mission.copy(
                id = existingMission.id,
                missionId = existingMission.missionId,
                updatedAt = Instant.now()
            ))
            ResponseEntity.ok(updatedMission)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{missionId}")
    fun deleteMission(@PathVariable missionId: String): ResponseEntity<Void> {
        missionService.deleteMission(missionId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun listMissions(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) type: String?
    ): ResponseEntity<List<Mission>> {
        val missions = missionService.listMissions(status, type)
        return ResponseEntity.ok(missions)
    }

    @PostMapping("/{missionId}/start")
    fun startMission(@PathVariable missionId: String): ResponseEntity<Map<String, String>> {
        return if (missionService.startMission(missionId)) {
            ResponseEntity.ok(mapOf("message" to "Mission started successfully"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Mission cannot be started"))
        }
    }

    @PostMapping("/{missionId}/complete")
    fun completeMission(@PathVariable missionId: String): ResponseEntity<Map<String, String>> {
        return if (missionService.completeMission(missionId)) {
            ResponseEntity.ok(mapOf("message" to "Mission completed successfully"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Mission cannot be completed"))
        }
    }

    @GetMapping("/{missionId}/plan")
    fun getMissionPlans(@PathVariable missionId: String): ResponseEntity<List<MissionPlan>> {
        val plans = missionService.getMissionPlans(missionId)
        return ResponseEntity.ok(plans)
    }

    @GetMapping("/{missionId}/status")
    fun getCurrentExecutionStatus(@PathVariable missionId: String): ResponseEntity<MissionExecutionStatus?> {
        val status = missionService.getCurrentExecutionStatus(missionId)
        return if (status != null) {
            ResponseEntity.ok(status)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{missionId}/status")
    fun updateExecutionStatus(
        @PathVariable missionId: String,
        @RequestBody status: MissionExecutionStatus
    ): ResponseEntity<MissionExecutionStatus?> {
        val existingStatus = missionService.getCurrentExecutionStatus(missionId)
        return if (existingStatus != null) {
            val updatedStatus = missionService.updateExecutionStatus(status.copy(
                id = existingStatus.id,
                missionId = existingStatus.missionId,
                lastUpdated = Instant.now()
            ))
            ResponseEntity.ok(updatedStatus)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}