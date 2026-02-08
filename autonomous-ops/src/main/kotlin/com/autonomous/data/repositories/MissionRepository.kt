package com.autonomous.data.repositories

import com.autonomous.data.entities.Mission
import com.autonomous.data.entities.MissionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MissionRepository : JpaRepository<Mission, Long> {
    fun findByMissionId(missionId: String): Mission?
    fun findByStatus(status: MissionStatus): List<Mission>
    fun findByType(type: String): List<Mission>
}