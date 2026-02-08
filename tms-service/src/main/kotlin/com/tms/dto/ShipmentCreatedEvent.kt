package com.tms.dto

data class ShipmentCreatedEvent(
    val shipmentId: String,
    val orderId: String,
    val origin: String,
    val destination: String,
    val weight: Double,
    val dimensions: DimensionsDto,
    val timestamp: Long = System.currentTimeMillis()
)