package com.nodeorb.freight.marketplace.service.insight.provider

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

/**
 * Тестовый провайдер для генерации случайных данных о ценах на топливо
 * Используется как fallback при недоступности основных провайдеров
 */
@Component
class MockInsightProvider : InsightPriceProvider {

    companion object {
        private val logger = LoggerFactory.getLogger(MockInsightProvider::class.java)
    }

    @Value("\${nodeorb.insight.mock.base-surcharge:1.05}")
    private var baseSurcharge: Double = 1.05

    @Value("\${nodeorb.insight.mock.variation:0.02}")
    private var variation: Double = 0.02

    @Value("\${nodeorb.insight.mock.weight:0.3}")
    private var weight: Double = 0.3

    @Value("\${nodeorb.insight.mock.enabled:true}")
    private var enabled: Boolean = true

    private val random = Random()

    override fun fetchCurrentRate(): BigDecimal {
        if (!enabled) {
            throw IllegalStateException("MockInsightProvider is disabled")
        }

        try {
            // Генерация случайных колебаний цен на топливо
            val fluctuation = random.nextDouble() * variation * 2 - variation
            val surcharge = baseSurcharge + fluctuation
            
            val rate = BigDecimal(surcharge.coerceIn(0.95, 1.15))
            
            logger.info("Generated mock fuel surcharge: $rate")
            return rate

        } catch (e: Exception) {
            logger.error("Error generating mock fuel surcharge", e)
            throw RuntimeException("Failed to generate mock fuel surcharge", e)
        }
    }

    override fun getProviderName(): String = "Mock Fuel Provider"

    override fun isAvailable(): Boolean = enabled

    override fun getProviderType(): ProviderType = ProviderType.MOCK

    override fun getWeight(): Double = weight
}