package com.logi.admin.kmp.service

import com.logi.admin.kmp.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ApiService(private val httpClient: HttpClient) {
    
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
    
    suspend fun getSystemOverview(): SystemOverview = withContext(Dispatchers.Default) {
        try {
            httpClient.get("/api/dashboard/overview").body()
        } catch (e: Exception) {
            SystemOverview(
                totalOrders = 0L,
                activeFleets = 0,
                warehouseUtilization = 0.0,
                activeShipments = 0,
                systemHealth = "ERROR",
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    suspend fun getSystemMetrics(): SystemMetrics = withContext(Dispatchers.Default) {
        try {
            httpClient.get("/api/dashboard/metrics").body()
        } catch (e: Exception) {
            SystemMetrics(
                cpuUsage = 0.0,
                memoryUsage = 0.0,
                totalMemory = 0L,
                freeMemory = 0L,
                usedMemory = 0L,
                activeConnections = 0,
                throughput = 0.0,
                servicesMetrics = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    suspend fun getServicesStatus(): Map<String, String> = withContext(Dispatchers.Default) {
        try {
            httpClient.get("/api/dashboard/services/status").body()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    suspend fun getDetailedServicesStatus(): Map<String, Any> = withContext(Dispatchers.Default) {
        try {
            httpClient.get("/api/dashboard/services/status/detailed").body()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    suspend fun checkServiceHealth(serviceName: String, serviceUrl: String): ServiceHealth {
        val startTime = System.currentTimeMillis()
        return try {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(json)
                }
                install(Logging) {
                    level = LogLevel.NONE
                }
                expectSuccess = true
            }
            
            val response = client.get("$serviceUrl/actuator/health")
            val status = response.body<Map<String, Any>>()["status"] as? String ?: "UNKNOWN"
            
            ServiceHealth(
                name = serviceName,
                status = status,
                url = serviceUrl,
                responseTime = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            ServiceHealth(
                name = serviceName,
                status = "DOWN",
                url = serviceUrl,
                responseTime = System.currentTimeMillis() - startTime,
                error = e.message
            )
        }
    }
    
    suspend fun getDashboardData(): DashboardData {
        val overview = getSystemOverview()
        val metrics = getSystemMetrics()
        val servicesStatus = getServicesStatus()
        val detailedServicesStatus = getDetailedServicesStatus()
        
        return DashboardData(
            overview = overview,
            metrics = metrics,
            servicesStatus = servicesStatus,
            detailedServicesStatus = detailedServicesStatus
        )
    }
}

expect fun createHttpClient(): HttpClient

fun createApiService(): ApiService {
    return ApiService(createHttpClient())
}