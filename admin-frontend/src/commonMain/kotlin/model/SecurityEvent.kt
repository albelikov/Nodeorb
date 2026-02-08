package com.logi.admin.kmp.model

import kotlinx.serialization.Serializable

@Serializable
data class SecurityEvent(
    val id: String,
    val userId: String,
    val service: String,
    val riskLevel: RiskLevel,
    val trigger: String,
    val description: String,
    val timestamp: String,
    val evidencePackage: EvidencePackage? = null
)

@Serializable
enum class RiskLevel {
    LOW, YELLOW, RED
}

@Serializable
data class EvidencePackage(
    val eventId: String,
    val timestamp: String,
    val data: Map<String, Any>,
    val signatures: List<String>,
    val hash: String
)

@Serializable
data class SecurityEventResponse(
    val events: List<SecurityEvent>,
    val total: Int,
    val page: Int,
    val size: Int
)