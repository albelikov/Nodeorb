package com.autonomous.data.repositories

import com.autonomous.data.entities.Mission
import com.autonomous.data.entities.MissionPlan
import com.autonomous.data.entities.PlanStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MissionPlanRepository : JpaRepository<MissionPlan, Long> {
    fun findByPlanId(planId: String): MissionPlan?
    fun findByMission(mission: Mission): List<MissionPlan>
    fun findByStatus(status: PlanStatus): List<MissionPlan>
    fun findByMissionAndVersion(mission: Mission, version: Int): MissionPlan?
}