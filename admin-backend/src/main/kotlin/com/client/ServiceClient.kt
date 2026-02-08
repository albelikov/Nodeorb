package com.client

import com.dto.ServiceHealth
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

@Component
class ServiceClient(
    private val webClient: WebClient
) {
    
    fun checkHealth(serviceName: String, baseUrl: String): ServiceHealth {
        return try {
            val startTime = System.currentTimeMillis()
            val response = webClient.get()
                .uri("$baseUrl/actuator/health")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block()
            
            val responseTime = System.currentTimeMillis() - startTime
            val status = response?.get("status")?.toString() ?: "UNKNOWN"
            
            ServiceHealth(
                serviceName = serviceName,
                status = if (status == "UP") "UP" else "DOWN",
                url = baseUrl,
                responseTime = responseTime,
                details = response as? Map<String, Any>
            )
        } catch (e: WebClientResponseException) {
            ServiceHealth(
                serviceName = serviceName,
                status = "DOWN",
                url = baseUrl,
                error = "HTTP ${e.statusCode}: ${e.message}"
            )
        } catch (e: Exception) {
            ServiceHealth(
                serviceName = serviceName,
                status = "DOWN",
                url = baseUrl,
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    fun getMetrics(serviceName: String, baseUrl: String): Map<String, Any>? {
        return try {
            @Suppress("UNCHECKED_CAST")
            webClient.get()
                .uri("$baseUrl/actuator/metrics")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block() as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
    
    fun getInfo(serviceName: String, baseUrl: String): Map<String, Any>? {
        return try {
            @Suppress("UNCHECKED_CAST")
            webClient.get()
                .uri("$baseUrl/actuator/info")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block() as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
}
