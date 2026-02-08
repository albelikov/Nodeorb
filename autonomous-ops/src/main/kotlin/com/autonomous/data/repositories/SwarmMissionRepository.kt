package com.autonomous.data.repositories

import com.autonomous.data.entities.SwarmMission
import com.autonomous.data.entities.SwarmMissionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SwarmMissionRepository : JpaRepository<SwarmMission, Long> {
    fun findBySwarmMissionId(swarmMissionId: String): SwarmMission?
    fun findByStatus(status: SwarmMissionStatus): List<SwarmMission>
}