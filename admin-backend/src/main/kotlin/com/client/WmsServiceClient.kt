package com.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
class WmsServiceClient(
    private val webClient: WebClient
) {
    
    fun getWarehouseUtilization(baseUrl: String): Double {
        return try {
            val response = webClient.get()
                .uri("$baseUrl/api/warehouses/utilization")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block()
            
            (response?.get("averageUtilization") as? Number)?.toDouble() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
    
    fun getWarehouseStatistics(baseUrl: String): Map<String, Any> {
        return try {
            @Suppress("UNCHECKED_CAST")
            webClient.get()
                .uri("$baseUrl/api/warehouses/statistics")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block() as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
