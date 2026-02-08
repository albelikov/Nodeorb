package com.tms.integration

import com.tms.dto.VehicleAvailabilityCheckDto
import com.tms.dto.VehicleAvailabilityDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class FmsIntegrationService(
    private val restTemplate: RestTemplate
) {

    companion object {
        private val logger = LoggerFactory.getLogger(FmsIntegrationService::class.java)
        private const val FMS_BASE_URL = "http://localhost:8082/api/v1/vehicles"
    }

    /**
     * Проверяет доступность транспортных средств из FMS
     * @param availabilityCheck Детали проверки доступности
     * @return Список доступных транспортных средств
     */
    fun checkVehicleAvailability(availabilityCheck: VehicleAvailabilityCheckDto): List<VehicleAvailabilityDto> {
        logger.info("Checking vehicle availability from FMS: $availabilityCheck")
        try {
            val url = "$FMS_BASE_URL/availability"
            return restTemplate.postForObject(url, availabilityCheck, Array<VehicleAvailabilityDto>::class.java)?.toList()
                ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error checking vehicle availability from FMS: $availabilityCheck", e)
            throw RuntimeException("Failed to check vehicle availability from FMS", e)
        }
    }
}