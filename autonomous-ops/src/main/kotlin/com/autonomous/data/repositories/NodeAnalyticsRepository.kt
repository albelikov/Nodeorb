package com.autonomous.data.repositories

import com.autonomous.data.entities.NodeAnalytics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NodeAnalyticsRepository : JpaRepository<NodeAnalytics, Long> {
    fun findByNodeId(nodeId: String): List<NodeAnalytics>
}