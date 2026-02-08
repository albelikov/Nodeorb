package com.autonomous.core

import com.autonomous.data.entities.*
import com.autonomous.data.repositories.MissionRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MissionService(
    private val missionRepository: MissionRepository
) {

    fun createMission(missionId: String, missionType: String, description: String): Mission {
        val mission = Mission(
            missionId = missionId,
            type = MissionType.DELIVERY,
            priority = Priority.MEDIUM,
            status = MissionStatus.PENDING,
            origin = GeoPoint(0.0, 0.0),
            destination = GeoPoint(0.0, 0.0),
            waypoints = emptyList(),
            payload = PayloadSpec(
                weightKg = 0.0,
                dimensions = Dimensions(0.0, 0.0, 0.0),
                type = "",
                handlingInstructions = null
            ),
            constraints = MissionConstraints(
                maxDuration = 0,
                maxCost = 0.0,
                requiredSensors = emptyList(),
                forbiddenAreas = emptyList()
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            scheduledStartTime = null,
            actualStartTime = null,
            completedAt = null,
            assignedNodeId = null,
            createdBy = "system",
            approvedBy = null,
            tasks = emptyList()
        )

        return missionRepository.save(mission)
    }

    fun getMission(missionId: String): Mission? {
        return missionRepository.findByMissionId(missionId)
    }

    fun updateMissionStatus(missionId: String, status: String): Mission? {
        val mission = missionRepository.findByMissionId(missionId)
        return mission?.copy(status = MissionStatus.valueOf(status))?.let { missionRepository.save(it) }
    }

    fun cancelMission(missionId: String): Mission? {
        return updateMissionStatus(missionId, "ABORTED")
    }

    fun listMissions(status: String? = null, missionType: String? = null): List<Mission> {
        return when {
            status != null && missionType != null -> missionRepository.findByStatus(MissionStatus.valueOf(status))
                .filter { it.type.toString() == missionType }
            status != null -> missionRepository.findByStatus(MissionStatus.valueOf(status))
            missionType != null -> missionRepository.findByType(missionType)
            else -> missionRepository.findAll()
        }
    }
}