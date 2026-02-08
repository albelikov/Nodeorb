package com.tms.dto

data class DealClosedEvent(
    val bidId: String,
    val orderId: String,
    val carrierId: String,
    val amount: Double,
    val origin: String,
    val destination: String,
    val weight: Double,
    val dimensions: DimensionsDto,
    val timestamp: Long = System.currentTimeMillis()
)