package com.nodeorb.freight.marketplace.service.insight.provider

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Реализация провайдера для получения данных из локальных JSON-файлов
 * Парсит региональные данные о ценах на топливо
 */
@Component
class RegionalJsonProvider(
    private val objectMapper: ObjectMapper
) : InsightPriceProvider {

    companion object {
        private val logger = LoggerFactory.getLogger(RegionalJsonProvider::class.java)
    }

    @Value("\${nodeorb.insight.regional-json.file-path:}")
    private lateinit var dataFilePath: String

    @Value("\${nodeorb.insight.regional-json.region:US}")
    private lateinit var region: String

    @Value("\${nodeorb.insight.regional-json.weight:0.6}")
    private var weight: Double = 0.6

    @Value("\${nodeorb.insight.regional-json.enabled:true}")
    private var enabled: Boolean = true

    override fun fetchCurrentRate(): BigDecimal {
        if (!enabled) {
            throw IllegalStateException("RegionalJsonProvider is disabled")
        }

        try {
            logger.info("Fetching fuel price from regional JSON file: $dataFilePath")

            val file = File(dataFilePath)
            if (!file.exists()) {
                throw IllegalStateException("Data file not found: $dataFilePath")
            }

            val jsonData = objectMapper.readTree(file)
            val regionData = jsonData.get(region)
                ?: throw IllegalStateException("No data found for region: $region")

            val currentDate = LocalDate.now().toString()
            val currentData = regionData.get(currentDate)
                ?: throw IllegalStateException("No data found for current date: $currentDate")

            val fuelPrice = currentData.get("fuelPrice").asDouble()
            val rate = BigDecimal(fuelPrice)

            logger.info("Successfully fetched fuel price from JSON: $rate")
            return rate

        } catch (e: Exception) {
            logger.error("Error fetching fuel price from regional JSON", e)
            throw RuntimeException("Failed to fetch fuel price from regional JSON", e)
        }
    }

    override fun getProviderName(): String = "Regional JSON Provider"

    override fun isAvailable(): Boolean = enabled && dataFilePath.isNotBlank()

    override fun getProviderType(): ProviderType = ProviderType.REGIONAL_JSON

    override fun getWeight(): Double = weight

    /**
     * DTO для данных из JSON-файла
     */
    data class RegionalFuelData(
        val region: String,
        val date: String,
        val fuelPrice: Double,
        val currency: String
    )
}