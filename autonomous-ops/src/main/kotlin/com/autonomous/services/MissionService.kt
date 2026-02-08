package com.autonomous.services

import com.autonomous.data.entities.*
import com.autonomous.data.repositories.MissionRepository
import com.autonomous.data.repositories.MissionPlanRepository
import com.autonomous.data.repositories.MissionExecutionStatusRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MissionService(
    private val missionRepository: MissionRepository,
    private val missionPlanRepository: MissionPlanRepository,
    private val missionExecutionStatusRepository: MissionExecutionStatusRepository
) {

    fun createMission(mission: Mission): Mission {
        return missionRepository.save(mission)
    }

    fun getMission(missionId: String): Mission? {
        return missionRepository.findByMissionId(missionId)
    }

    fun updateMission(mission: Mission): Mission {
        return missionRepository.save(mission)
    }

    fun deleteMission(missionId: String) {
        missionRepository.findByMissionId(missionId)?.let {
            missionRepository.delete(it)
        }
    }

    fun listMissions(status: String? = null, type: String? = null): List<Mission> {
        return when {
            status != null && type != null -> missionRepository.findByStatus(MissionStatus.valueOf(status))
                .filter { it.type.toString() == type }
            status != null -> missionRepository.findByStatus(MissionStatus.valueOf(status))
            type != null -> missionRepository.findByType(type)
            else -> missionRepository.findAll()
        }
    }

    fun createMissionPlan(plan: MissionPlan): MissionPlan {
        return missionPlanRepository.save(plan)
    }

    fun getMissionPlan(planId: String): MissionPlan? {
        return missionPlanRepository.findByPlanId(planId)
    }

    fun updateMissionPlan(plan: MissionPlan): MissionPlan {
        return missionPlanRepository.save(plan)
    }

    fun getMissionPlans(missionId: String): List<MissionPlan> {
        val mission = missionRepository.findByMissionId(missionId)
        return mission?.let { missionPlanRepository.findByMission(it) } ?: emptyList()
    }

    fun getCurrentExecutionStatus(missionId: String): MissionExecutionStatus? {
        return missionExecutionStatusRepository.findByMissionId(missionId)
    }

    fun updateExecutionStatus(status: MissionExecutionStatus): MissionExecutionStatus {
        return missionExecutionStatusRepository.save(status)
    }

    fun startMission(missionId: String): Boolean {
        val mission = missionRepository.findByMissionId(missionId)
        if (mission != null && mission.status == MissionStatus.PENDING) {
            missionRepository.save(mission.copy(
                status = MissionStatus.IN_PROGRESS,
                actualStartTime = Instant.now(),
                updatedAt = Instant.now()
            ))

            val executionStatus = MissionExecutionStatus(
                missionId = missionId,
                nodeId = mission.assignedNodeId ?: "",
                currentStatus = MissionStatus.IN_PROGRESS,
                currentTaskId = null,
                currentLocation = mission.origin,
                currentAltitude = 0.0,
                currentSpeed = 0.0,
                currentHeading = 0.0,
                progressPercentage = 0.0,
                completedTasks = 0,
                totalTasks = mission.tasks.size,
                batteryLevel = 1.0,
                estimatedRemainingRange = 100.0,
                deviationFromPlan = DeviationMetrics(0, 0.0, 0.0, null),
                activeAlerts = emptyList(),
                lastUpdated = Instant.now()
            )
            missionExecutionStatusRepository.save(executionStatus)
            return true
        }
        return false
    }

    fun completeMission(missionId: String): Boolean {
        val mission = missionRepository.findByMissionId(missionId)
        if (mission != null && mission.status == MissionStatus.IN_PROGRESS) {
            missionRepository.save(mission.copy(
                status = MissionStatus.COMPLETED,
                completedAt = Instant.now(),
                updatedAt = Instant.now()
            ))

            missionExecutionStatusRepository.findByMissionId(missionId)?.let {
                missionExecutionStatusRepository.save(it.copy(
                    currentStatus = MissionStatus.COMPLETED,
                    progressPercentage = 100.0,
                    completedTasks = it.totalTasks,
                    lastUpdated = Instant.now()
                ))
            }
            return true
        }
        return false
    }
}