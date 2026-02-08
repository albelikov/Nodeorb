package com.freight.marketplace.service.insight.provider

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.Duration

/**
 * Реализация провайдера для получения данных из Weekly Oil Bulletin API
 * Работает с внешним API для получения актуальных цен на топливо
 */
@Component
class OilBulletinProvider(
    private val restTemplate: RestTemplate
) : InsightPriceProvider {

    companion object {
        private val logger = LoggerFactory.getLogger(OilBulletinProvider::class.java)
    }

    @Value("\${nodeorb.insight.oil-bulletin.url:https://api.oilbulletin.com/v1/fuel-prices}")
    private lateinit var apiUrl: String

    @Value("\${nodeorb.insight.oil-bulletin.api-key:}")
    private lateinit var apiKey: String

    @Value("\${nodeorb.insight.oil-bulletin.timeout:30000}")
    private var timeout: Int = 30000

    @Value("\${nodeorb.insight.oil-bulletin.weight:0.8}")
    private var weight: Double = 0.8

    @Value("\${nodeorb.insight.oil-bulletin.enabled:true}")
    private var enabled: Boolean = true

    override fun fetchCurrentRate(): BigDecimal {
        if (!enabled) {
            throw IllegalStateException("OilBulletinProvider is disabled")
        }

        try {
            logger.info("Fetching fuel price from Oil Bulletin API: $apiUrl")

            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer $apiKey")
                set("Accept", "application/json")
            }

            val entity = HttpEntity<String>(headers)
            
            val response: ResponseEntity<OilBulletinResponse> = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                OilBulletinResponse::class.java
            )

            val fuelPrice = response.body?.fuelPrice
                ?: throw IllegalStateException("No fuel price data in response")

            logger.info("Successfully fetched fuel price: $fuelPrice")
            return fuelPrice

        } catch (e: Exception) {
            logger.error("Error fetching fuel price from Oil Bulletin API", e)
            throw RuntimeException("Failed to fetch fuel price from Oil Bulletin API", e)
        }
    }

    override fun getProviderName(): String = "Oil Bulletin API Provider"

    override fun isAvailable(): Boolean = enabled

    override fun getProviderType(): ProviderType = ProviderType.OIL_BULLETIN

    override fun getWeight(): Double = weight

    /**
     * DTO для ответа от Oil Bulletin API
     */
    data class OilBulletinResponse(
        val fuelPrice: BigDecimal,
        val currency: String,
        val timestamp: String,
        val region: String
    )
}