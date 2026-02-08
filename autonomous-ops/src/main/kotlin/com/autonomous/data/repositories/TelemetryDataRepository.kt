package com.autonomous.data.repositories

import com.autonomous.data.entities.TelemetryData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TelemetryDataRepository : JpaRepository<TelemetryData, Long> {
    fun findByNodeId(nodeId: String): List<TelemetryData>
}