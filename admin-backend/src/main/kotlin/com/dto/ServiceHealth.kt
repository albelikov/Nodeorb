package com.dto

data class ServiceHealth(
    val serviceName: String,
    val status: String,
    val url: String,
    val responseTime: Long? = null,
    val error: String? = null,
    val details: Map<String, Any>? = null
)

data class ServiceMetrics(
    val serviceName: String,
    val metrics: Map<String, Any>
)

data class ServiceOverview(
    val totalOrders: Long = 0,
    val activeFleets: Int = 0,
    val warehouseUtilization: Double = 0.0,
    val activeShipments: Int = 0,
    val systemHealth: String = "HEALTHY"
)
