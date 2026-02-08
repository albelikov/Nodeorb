package com.autonomous.core

import org.springframework.stereotype.Service

@Service
class SafetyController {
    fun checkSafety(missionId: String): Boolean {
        return false
    }
}