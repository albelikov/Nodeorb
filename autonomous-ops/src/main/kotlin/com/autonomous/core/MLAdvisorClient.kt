package com.autonomous.core

import org.springframework.stereotype.Service

@Service
class MLAdvisorClient {
    fun getAdvice(missionId: String): MLAdvice {
        return MLAdvice(
            adviceId = "",
            missionId = missionId,
            action = "",
            parameters = emptyMap(),
            confidence = 0.0,
            createdAt = 0
        )
    }
}