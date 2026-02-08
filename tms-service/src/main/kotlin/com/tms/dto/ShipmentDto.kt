package com.tms.dto

data class ShipmentDto(
    val id: String? = null,
    val orderId: String,
    val origin: String,
    val destination: String,
    val weight: Double,
    val dimensions: DimensionsDto,
    val status: String = "CREATED"
)

data class DimensionsDto(
    val length: Double,
    val width: Double,
    val height: Double
)

