package com.freight.marketplace.controller

import com.freight.marketplace.dto.OrderProgressDto
import com.freight.marketplace.dto.ShipperOrderDto
import com.freight.marketplace.dto.ShipperBidDto
import com.freight.marketplace.service.AnalyticsService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Контроллер для Shipper Dashboard
 * Предоставляет данные о ставках, прогрессе заказов и аналитике
 */
@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AnalyticsController::class.java)
    }

    /**
     * Получает список ставок для заказа, отсортированных по matching_score
     */
    @GetMapping("/orders/{orderId}/bids")
    fun getBidsForOrder(
        @PathVariable orderId: UUID,
        @RequestParam orderType: String
    ): ResponseEntity<List<ShipperBidDto>> {
        logger.info("Getting bids for order: $orderId, type: $orderType")
        
        try {
            val bids = analyticsService.getBidsForOrder(orderId, orderType)
return ResponseEntity.ok(bids)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid order type or order not found: $orderType", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting bids for order: $orderId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает заказ с детализацией ставок для Shipper Dashboard
     */
    @GetMapping("/orders/{orderId}")
    fun getOrderWithBids(
        @PathVariable orderId: UUID,
        @RequestParam orderType: String
    ): ResponseEntity<ShipperOrderDto> {
        logger.info("Getting order with bids: $orderId, type: $orderType")
        
        try {
            val order = analyticsService.getOrderWithBids(orderId, orderType)
return ResponseEntity.ok(order)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid order type or order not found: $orderType", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting order with bids: $orderId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает агрегированный прогресс заказа для трехцветного прогресс-бара
     */
    @GetMapping("/orders/{orderId}/progress")
    fun getOrderProgress(
        @PathVariable orderId: UUID,
        @RequestParam orderType: String
    ): ResponseEntity<OrderProgressDto> {
        logger.info("Getting progress for order: $orderId, type: $orderType")
        
        try {
            val progress = analyticsService.getOrderProgress(orderId, orderType)
return ResponseEntity.ok(progress)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid order type or order not found: $orderType", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting progress for order: $orderId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает прогресс для всех частичных заказов мастер-заказа
     */
    @GetMapping("/master-orders/{masterOrderId}/partial-progress")
    fun getPartialOrdersProgress(
        @PathVariable masterOrderId: UUID
    ): ResponseEntity<List<OrderProgressDto>> {
        logger.info("Getting partial orders progress for master order: $masterOrderId")
        
        try {
            // Для каждого частичного заказа получаем его прогресс
            // В реальной реализации нужно добавить метод в AnalyticsService
            // Пока возвращаем пустой список
return ResponseEntity.ok(emptyList())
        } catch (e: Exception) {
            logger.error("Error getting partial orders progress for master order: $masterOrderId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает статистику по ставкам для заказа
     */
    @GetMapping("/orders/{orderId}/stats")
    fun getOrderStats(
        @PathVariable orderId: UUID,
        @RequestParam orderType: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Getting stats for order: $orderId, type: $orderType")
        
        try {
            val bids = analyticsService.getBidsForOrder(orderId, orderType)
            
val stats = mapOf<String, Any>(
                "totalBids" to bids.size as Any,
                "avgMatchingScore" to (bids.mapNotNull { it.matchingScore }.average().takeIf { it.isFinite() } ?: 0.0) as Any,
                "avgAmount" to (bids.map { it.amount.toDouble() }.average().takeIf { it.isFinite() } ?: 0.0) as Any,
                "minAmount" to bids.minByOrNull { it.amount }?.amount as Any,
                "maxAmount" to bids.maxByOrNull { it.amount }?.amount as Any,
                "dangerousGoodsQualified" to bids.count { it.passesDangerousGoodsRules } as Any,
                "avgCarrierRating" to (bids.map { it.carrierRating }.average().takeIf { it.isFinite() } ?: 0.0) as Any,
                "avgOnTimeDeliveryRate" to (bids.map { it.onTimeDeliveryRate }.average().takeIf { it.isFinite() } ?: 0.0) as Any
            )
            
return ResponseEntity.ok(stats)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid order type or order not found: $orderType", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting stats for order: $orderId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает топ перевозчиков по matching_score для заказа
     */
    @GetMapping("/orders/{orderId}/top-carriers")
    fun getTopCarriers(
        @PathVariable orderId: UUID,
        @RequestParam orderType: String,
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<List<ShipperBidDto>> {
        logger.info("Getting top carriers for order: $orderId, type: $orderType, limit: $limit")
        
        try {
            val bids = analyticsService.getBidsForOrder(orderId, orderType)
                .sortedByDescending { it.matchingScore }
                .take(limit)
            
return ResponseEntity.ok(bids)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid order type or order not found: $orderType", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting top carriers for order: $orderId", e)
            return ResponseEntity.internalServerError().build()
        }
    }
}