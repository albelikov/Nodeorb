package com.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
class FmsServiceClient(
    private val webClient: WebClient
) {
    
    fun getActiveFleets(baseUrl: String): Int {
        return try {
            val response = webClient.get()
                .uri("$baseUrl/api/fleets/active/count")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block()
            
            (response?.get("count") as? Number)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    fun getFleetStatistics(baseUrl: String): Map<String, Any> {
        return try {
            @Suppress("UNCHECKED_CAST")
            webClient.get()
                .uri("$baseUrl/api/fleets/statistics")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block() as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
