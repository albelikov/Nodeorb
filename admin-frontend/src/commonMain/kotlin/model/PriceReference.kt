package com.logi.admin.kmp.model

import kotlinx.serialization.Serializable

@Serializable
data class PriceReference(
    val id: String,
    val materialType: String,
    val baseCost: Double,
    val laborRatePerHour: Double,
    val serviceCategory: String,
    val allowedDeviation: Double,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class PriceReferenceRequest(
    val materialType: String,
    val baseCost: Double,
    val laborRatePerHour: Double,
    val serviceCategory: String,
    val allowedDeviation: Double
)

@Serializable
data class PriceReferenceResponse(
    val success: Boolean,
    val message: String,
    val data: PriceReference? = null
)