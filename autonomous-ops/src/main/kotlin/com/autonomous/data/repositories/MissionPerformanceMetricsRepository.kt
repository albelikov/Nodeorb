package com.autonomous.data.repositories

import com.autonomous.data.entities.MissionPerformanceMetrics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MissionPerformanceMetricsRepository : JpaRepository<MissionPerformanceMetrics, Long> {
    fun findByMissionId(missionId: String): MissionPerformanceMetrics?
    fun findByNodeId(nodeId: String): List<MissionPerformanceMetrics>
}