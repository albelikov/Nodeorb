package com.autonomous.core

import org.springframework.stereotype.Service

@Service
class GovernanceController {
    fun logError(missionId: String, message: String) {
        // Log error
    }

    fun logDecision(missionId: String, decision: Decision, mlAdvice: MLAdvice) {
        // Log decision
    }
}