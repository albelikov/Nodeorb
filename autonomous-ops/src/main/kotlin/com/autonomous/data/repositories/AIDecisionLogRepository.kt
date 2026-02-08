package com.autonomous.data.repositories

import com.autonomous.data.entities.AIDecisionLog
import com.autonomous.data.entities.DecisionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AIDecisionLogRepository : JpaRepository<AIDecisionLog, Long> {
    fun findByDecisionId(decisionId: String): AIDecisionLog?
    fun findByDecisionType(decisionType: DecisionType): List<AIDecisionLog>
}