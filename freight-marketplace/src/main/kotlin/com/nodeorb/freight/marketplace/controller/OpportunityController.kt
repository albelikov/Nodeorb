package com.nodeorb.freight.marketplace.controller

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.service.OpportunityService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Контроллер для Carrier Opportunity Feed
 * Предоставляет ленту заказов, отфильтрованную по Trust Token перевозчика
 */
@RestController
@RequestMapping("/api/v1/opportunities")
class OpportunityController(
    private val opportunityService: OpportunityService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(OpportunityController::class.java)
    }

    /**
     * Получает ленту возможностей для перевозчика
     */
    @GetMapping
    fun getOpportunities(
        @RequestHeader("X-Carrier-ID") carrierId: UUID,
        @RequestParam(required = false) cargoTypes: List<String> = emptyList(),
        @RequestParam(required = false) minTrustLevel: String? = null,
        @RequestParam(required = false) maxDistance: Double? = null,
        @RequestParam(required = false) maxBidAmount: Double? = null,
        @RequestParam(required = false) orderStatuses: List<String> = listOf("OPEN", "AUCTION_ACTIVE"),
        @RequestParam(defaultValue = "matching_score") sortBy: String = "matching_score",
        @RequestParam(defaultValue = "desc") sortOrder: String = "desc",
        @RequestParam(defaultValue = "0") page: Int = 0,
        @RequestParam(defaultValue = "20") size: Int = 20
    ): ResponseEntity<List<CarrierOpportunityDto>> {
        logger.info("Getting opportunities for carrier: $carrierId, filters: cargoTypes=$cargoTypes, minTrustLevel=$minTrustLevel, maxBidAmount=$maxBidAmount")
        
        try {
            val filter = OpportunityFilter(
                cargoTypes = cargoTypes,
                minTrustLevel = minTrustLevel,
                maxDistance = maxDistance,
                maxBidAmount = maxBidAmount?.let { java.math.BigDecimal.valueOf(it) },
                orderStatuses = orderStatuses,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            
            val opportunities = opportunityService.getOpportunitiesForCarrier(carrierId, filter)
            
            // Пагинация
            val total = opportunities.size
            val startIndex = page * size
            val endIndex = kotlin.math.min(startIndex + size, total)
            val paginatedOpportunities = opportunities.subList(startIndex, endIndex)
            
            return ResponseEntity.ok(paginatedOpportunities)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid carrier ID or filter parameters: $carrierId", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting opportunities for carrier: $carrierId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает статистику для Carrier Dashboard
     */
    @GetMapping("/stats")
    fun getCarrierStats(
        @RequestHeader("X-Carrier-ID") carrierId: UUID
    ): ResponseEntity<CarrierStatsDto> {
        logger.info("Getting stats for carrier: $carrierId")
        
        try {
            val stats = opportunityService.getCarrierStats(carrierId)
            return ResponseEntity.ok(stats)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid carrier ID: $carrierId", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting stats for carrier: $carrierId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает конкретную возможность по ID
     */
    @GetMapping("/{opportunityId}")
    fun getOpportunity(
        @RequestHeader("X-Carrier-ID") carrierId: UUID,
        @PathVariable opportunityId: UUID
    ): ResponseEntity<CarrierOpportunityDto> {
        logger.info("Getting opportunity: $opportunityId for carrier: $carrierId")
        
        try {
            val opportunities = opportunityService.getOpportunitiesForCarrier(carrierId, OpportunityFilter())
            val opportunity = opportunities.find { it.orderId == opportunityId }
            
            return if (opportunity != null) {
                ResponseEntity.ok(opportunity)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid carrier ID or opportunity ID: $carrierId, $opportunityId", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting opportunity: $opportunityId for carrier: $carrierId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает возможности по типу заказа
     */
    @GetMapping("/by-type/{orderType}")
    fun getOpportunitiesByType(
        @RequestHeader("X-Carrier-ID") carrierId: UUID,
        @PathVariable orderType: String,
        @RequestParam(required = false) cargoTypes: List<String> = emptyList(),
        @RequestParam(defaultValue = "matching_score") sortBy: String = "matching_score",
        @RequestParam(defaultValue = "desc") sortOrder: String = "desc"
    ): ResponseEntity<List<CarrierOpportunityDto>> {
        logger.info("Getting opportunities by type: $orderType for carrier: $carrierId")
        
        try {
            val filter = OpportunityFilter(
                cargoTypes = cargoTypes,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            
            val opportunities = opportunityService.getOpportunitiesForCarrier(carrierId, filter)
            val filteredOpportunities = opportunities.filter { it.orderType.equals(orderType, ignoreCase = true) }
            
            return ResponseEntity.ok(filteredOpportunities)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid carrier ID or order type: $carrierId, $orderType", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting opportunities by type: $orderType for carrier: $carrierId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает возможности для опасных грузов
     */
    @GetMapping("/dangerous-goods")
    fun getDangerousGoodsOpportunities(
        @RequestHeader("X-Carrier-ID") carrierId: UUID,
        @RequestParam(defaultValue = "matching_score") sortBy: String = "matching_score",
        @RequestParam(defaultValue = "desc") sortOrder: String = "desc"
    ): ResponseEntity<List<CarrierOpportunityDto>> {
        logger.info("Getting dangerous goods opportunities for carrier: $carrierId")
        
        try {
            val filter = OpportunityFilter(
                cargoTypes = listOf("DANGEROUS"),
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            
            val opportunities = opportunityService.getOpportunitiesForCarrier(carrierId, filter)
            
            return ResponseEntity.ok(opportunities)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid carrier ID: $carrierId", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting dangerous goods opportunities for carrier: $carrierId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает высокодоходные возможности (высокий trust score)
     */
    @GetMapping("/high-trust")
    fun getHighTrustOpportunities(
        @RequestHeader("X-Carrier-ID") carrierId: UUID,
        @RequestParam(defaultValue = "80.0") minTrustScore: Double = 80.0,
        @RequestParam(defaultValue = "matching_score") sortBy: String = "matching_score",
        @RequestParam(defaultValue = "desc") sortOrder: String = "desc"
    ): ResponseEntity<List<CarrierOpportunityDto>> {
        logger.info("Getting high trust opportunities for carrier: $carrierId, minTrustScore: $minTrustScore")
        
        try {
            val filter = OpportunityFilter(
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            
            val opportunities = opportunityService.getOpportunitiesForCarrier(carrierId, filter)
            val highTrustOpportunities = opportunities.filter { it.trustScore >= minTrustScore }
            
            return ResponseEntity.ok(highTrustOpportunities)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid carrier ID: $carrierId", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting high trust opportunities for carrier: $carrierId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Получает возможности с сортировкой по расстоянию (если будет реализовано)
     */
    @GetMapping("/nearby")
    fun getNearbyOpportunities(
        @RequestHeader("X-Carrier-ID") carrierId: UUID,
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(required = false) maxDistance: Double? = null,
        @RequestParam(defaultValue = "distance") sortBy: String = "distance",
        @RequestParam(defaultValue = "asc") sortOrder: String = "asc"
    ): ResponseEntity<List<CarrierOpportunityDto>> {
        logger.info("Getting nearby opportunities for carrier: $carrierId, lat: $latitude, lon: $longitude, maxDistance: $maxDistance")
        
        try {
            val filter = OpportunityFilter(
                maxDistance = maxDistance,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            
            val opportunities = opportunityService.getOpportunitiesForCarrier(carrierId, filter)
            
            // В реальной реализации нужно добавить расчет расстояния
            // Пока возвращаем все возможности
            return ResponseEntity.ok(opportunities)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid parameters for nearby opportunities: $carrierId", e)
            return ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error getting nearby opportunities for carrier: $carrierId", e)
            return ResponseEntity.internalServerError().build()
        }
    }
}