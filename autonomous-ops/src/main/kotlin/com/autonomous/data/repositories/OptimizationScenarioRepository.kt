package com.autonomous.data.repositories

import com.autonomous.data.entities.OptimizationScenario
import com.autonomous.data.entities.ScenarioType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OptimizationScenarioRepository : JpaRepository<OptimizationScenario, Long> {
    fun findByScenarioId(scenarioId: String): OptimizationScenario?
    fun findByType(type: ScenarioType): List<OptimizationScenario>
}