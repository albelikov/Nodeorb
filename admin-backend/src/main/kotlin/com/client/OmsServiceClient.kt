package com.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

@Component
class OmsServiceClient(
    private val webClient: WebClient
) {
    
    fun getTotalOrders(baseUrl: String): Long {
        return try {
            // Предполагаем, что OMS имеет endpoint для получения статистики
            val response = webClient.get()
                .uri("$baseUrl/api/orders/stats")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block()
            
            (response?.get("total") as? Number)?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    fun getActiveOrders(baseUrl: String): Int {
        return try {
            val response = webClient.get()
                .uri("$baseUrl/api/orders/active/count")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block()
            
            (response?.get("count") as? Number)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
