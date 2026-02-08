package com.tms.dto

data class OrderDto(
    val id: String,
    val customerId: String,
    val origin: String,
    val destination: String,
    val weight: Double,
    val dimensions: DimensionsDto,
    val status: String,
    val items: List<OrderItemDto>
)

data class OrderItemDto(
    val id: String,
    val productId: String,
    val quantity: Int,
    val weight: Double
)