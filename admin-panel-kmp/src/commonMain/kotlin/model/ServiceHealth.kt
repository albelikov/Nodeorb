package com.logi.admin.kmp.model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceHealth(
    val name: String,
    val status: String,
    val url: String,
    val responseTime: Long? = null,
    val error: String? = null,
    val details: Map<String, Any>? = null
)

@Serializable
data class SystemOverview(
    val totalOrders: Long,
    val activeFleets: Int,
    val warehouseUtilization: Double,
    val activeShipments: Int,
    val systemHealth: String,
    val timestamp: Long
)

@Serializable
data class SystemMetrics(
    val cpuUsage: Double,
    val memoryUsage: Double,
    val totalMemory: Long,
    val freeMemory: Long,
    val usedMemory: Long,
    val activeConnections: Int,
    val throughput: Double,
    val servicesMetrics: Map<String, Map<String, Any>>,
    val timestamp: Long
)

@Serializable
data class ServiceConfig(
    val name: String,
    val url: String,
    val enabled: Boolean
)

@Serializable
data class DashboardData(
    val overview: SystemOverview,
    val metrics: SystemMetrics,
    val servicesStatus: Map<String, String>,
    val detailedServicesStatus: Map<String, Any>
)