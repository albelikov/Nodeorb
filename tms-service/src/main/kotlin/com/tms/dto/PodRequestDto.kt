package com.tms.dto

data class PodRequestDto(
    val signature: String?,
    val signedBy: String,
    val signedAt: String,
    val notes: String?
)