package com.autonomous.api.dto

import java.time.Instant

data class XAIResponse(
    val missionId: String,
    val xaiId: String,
    val explanation: String,
    val decisionFactors: List<DecisionFactor>,
    val confidence: Double,
    val createdAt: Instant
)

data class DecisionFactor(
    val factor: String,
    val weight: Double,
    val value: String,
    val explanation: String
)