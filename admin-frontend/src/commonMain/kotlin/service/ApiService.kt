package com.logi.admin.kmp.service

import com.logi.admin.kmp.model.*
import com.logi.admin.kmp.model.PriceReference
import com.logi.admin.kmp.model.PriceReferenceRequest
import com.logi.admin.kmp.model.PriceReferenceResponse
import com.logi.admin.kmp.model.SecurityEvent
import com.logi.admin.kmp.model.SecurityEventResponse
import com.logi.admin.kmp.model.Appeal
import com.logi.admin.kmp.model.AppealResponse
import com.logi.admin.kmp.model.AppealActionRequest
import com.logi.admin.kmp.model.AppealActionResponse
import com.logi.admin.kmp.model.EvidencePackage
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

    // Price Reference Management
    suspend fun getPriceReferences(): List<PriceReference> = withContext(Dispatchers.Default) {
        try {
            httpClient.get("/api/scm/price-references").body<List<PriceReference>>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createPriceReference(request: PriceReferenceRequest): PriceReferenceResponse = withContext(Dispatchers.Default) {
        try {
            httpClient.post("/api/scm/price-references") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            PriceReferenceResponse(
                success = false,
                message = e.message ?: "Failed to create price reference"
            )
        }
    }

    suspend fun updatePriceReference(id: String, request: PriceReferenceRequest): PriceReferenceResponse = withContext(Dispatchers.Default) {
        try {
            httpClient.put("/api/scm/price-references/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            PriceReferenceResponse(
                success = false,
                message = e.message ?: "Failed to update price reference"
            )
        }
    }

    suspend fun deletePriceReference(id: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val response = httpClient.delete("/api/scm/price-references/$id")
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    // Security Event Monitoring
    suspend fun getSecurityEvents(page: Int = 0, size: Int = 20): SecurityEventResponse = withContext(Dispatchers.Default) {
        try {
            httpClient.get("/api/scm/security-events") {
                parameter("page", page)
                parameter("size", size)
            }.body()
        } catch (e: Exception) {
            SecurityEventResponse(
                events = emptyList(),
                total = 0,
                page = page,
                size = size
            )
        }
    }

    suspend fun getEvidencePackage(eventId: String): EvidencePackage? = withContext(Dispatchers.Default) {
        try {
            httpClient.get("/api/scm/security-events/$eventId/evidence").body()
        } catch (e: Exception) {
            null
        }
    }

    // Appeals Management
    suspend fun getAppeals(page: Int = 0, size: Int = 20): AppealResponse = withContext(Dispatchers.Default) {
        try {
            httpClient.get("/api/scm/appeals") {
                parameter("page", page)
                parameter("size", size)
            }.body()
        } catch (e: Exception) {
            AppealResponse(
                appeals = emptyList(),
                total = 0,
                page = page,
                size = size
            )
        }
    }

    suspend fun processAppeal(request: AppealActionRequest): AppealActionResponse = withContext(Dispatchers.Default) {
        try {
            httpClient.post("/api/scm/appeals/process") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            AppealActionResponse(
                success = false,
                message = e.message ?: "Failed to process appeal"
            )
        }
    }
}

expect fun createHttpClient(): HttpClient

fun createApiService(): ApiService {
    return ApiService(createHttpClient())
}