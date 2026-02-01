package logistics.admin.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
class TmsServiceClient(
    private val webClient: WebClient
) {
    
    fun getActiveShipments(baseUrl: String): Int {
        return try {
            val response = webClient.get()
                .uri("$baseUrl/api/shipments/active/count")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block()
            
            (response?.get("count") as? Number)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    fun getTransportationStatistics(baseUrl: String): Map<String, Any> {
        return try {
            @Suppress("UNCHECKED_CAST")
            webClient.get()
                .uri("$baseUrl/api/transportation/statistics")
                .retrieve()
                .bodyToMono(Map::class.java)
                .timeout(Duration.ofSeconds(5))
                .block() as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

