package com.tms.integration

import com.tms.dto.OrderDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OmsIntegrationService(
    private val restTemplate: RestTemplate
) {

    companion object {
        private val logger = LoggerFactory.getLogger(OmsIntegrationService::class.java)
        private const val OMS_BASE_URL = "http://localhost:8081/api/v1/orders"
    }

    /**
     * Запрашивает детали заказа из OMS
     * @param orderId ID заказа
     * @return Детали заказа
     */
    fun getOrderDetails(orderId: String): OrderDto {
        logger.info("Requesting order details from OMS for orderId: $orderId")
        try {
            val url = "$OMS_BASE_URL/$orderId"
            return restTemplate.getForObject(url, OrderDto::class.java)
                ?: throw RuntimeException("Order details not found for orderId: $orderId")
        } catch (e: Exception) {
            logger.error("Error requesting order details from OMS for orderId: $orderId", e)
            throw RuntimeException("Failed to get order details from OMS", e)
        }
    }
}