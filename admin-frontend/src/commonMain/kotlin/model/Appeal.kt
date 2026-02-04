package com.logi.admin.kmp.model

import kotlinx.serialization.Serializable

@Serializable
data class Appeal(
    val id: String,
    val userId: String,
    val eventId: String,
    val reason: String,
    val evidencePhotoUrl: String,
    val status: AppealStatus,
    val submittedAt: String,
    val reviewedAt: String? = null,
    val reviewedBy: String? = null,
    val reviewNotes: String? = null
)

@Serializable
enum class AppealStatus {
    PENDING, APPROVED, REJECTED
}

@Serializable
data class AppealResponse(
    val appeals: List<Appeal>,
    val total: Int,
    val page: Int,
    val size: Int
)

@Serializable
data class AppealActionRequest(
    val appealId: String,
    val action: String, // "approve" or "reject"
    val notes: String? = null
)

@Serializable
data class AppealActionResponse(
    val success: Boolean,
    val message: String,
    val appeal: Appeal? = null
)