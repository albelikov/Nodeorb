package com.autonomous.data.repositories

import com.autonomous.data.entities.MissionExecutionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MissionExecutionStatusRepository : JpaRepository<MissionExecutionStatus, Long> {
    fun findByMissionId(missionId: String): MissionExecutionStatus?
    fun findByNodeId(nodeId: String): MissionExecutionStatus?
}