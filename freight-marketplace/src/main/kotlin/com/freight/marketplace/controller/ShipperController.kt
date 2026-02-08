package com.freight.marketplace.controller

import com.freight.marketplace.dto.*
import com.freight.marketplace.entity.*
import com.freight.marketplace.service.OrderService
import com.freight.marketplace.service.ScmIntegrationService
import com.freight.marketplace.service.ComplianceService
import com.freight.marketplace.repository.MasterOrderRepository
import com.freight.marketplace.repository.PartialOrderRepository
import com.freight.marketplace.repository.UserProfileRepository
import com.freight.marketplace.repository.BidRepository
import com.freight.marketplace.repository.ScmSnapshotRepository
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Контроллер для Shipper UI
 * Обеспечивает функциональность управления заказами и мониторинга прогресса
 */
@RestController
@RequestMapping("/api/v1/shipper")
class ShipperController(
    private val orderService: OrderService,
    private val scmIntegrationService: ScmIntegrationService,
    private val complianceService: ComplianceService,
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository,
    private val userProfileRepository: UserProfileRepository,
    private val bidRepository: BidRepository,
    private val scmSnapshotRepository: ScmSnapshotRepository
) {

    /**
     * Создание нового заказа
     */
    @PostMapping("/orders")
    fun createOrder(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody orderRequest: OrderRequestDto
    ): ResponseEntity<MasterOrderEntity> {
        val shipperId = UUID.fromString(jwt.subject)
        
        val masterOrder = orderService.createMasterOrder(
            shipperId = shipperId,
            title = orderRequest.title,
            description = orderRequest.description,
            cargoType = CargoType.valueOf(orderRequest.cargoType),
            totalWeight = orderRequest.totalWeight,
            totalVolume = orderRequest.totalVolume,
            pickupLocation = createPoint(orderRequest.pickupLocation),
            deliveryLocation = createPoint(orderRequest.deliveryLocation),
            pickupAddress = orderRequest.pickupAddress,
            deliveryAddress = orderRequest.deliveryAddress,
            requiredDeliveryDate = orderRequest.requiredDeliveryDate,
            maxBidAmount = orderRequest.maxBidAmount,
            isLtlEnabled = orderRequest.isLtlEnabled,
            minLoadPercentage = orderRequest.minLoadPercentage,
            partialOrderSize = orderRequest.partialOrderSize
        )

        return ResponseEntity.ok(masterOrder)
    }

    /**
     * Получение заказов грузоотправителя
     */
    @GetMapping("/orders")
    fun getMyOrders(
        @AuthenticationPrincipal jwt: Jwt,
        pageable: Pageable
    ): ResponseEntity<Page<MasterOrderEntity>> {
        val shipperId = UUID.fromString(jwt.subject)
        
        val orders = masterOrderRepository.findByShipperId(shipperId, pageable)
        return ResponseEntity.ok(orders)
    }

    /**
     * Получение прогресса заказа
     */
    @GetMapping("/orders/{orderId}/progress")
    fun getOrderProgress(
        @PathVariable orderId: UUID
    ): ResponseEntity<OrderProgressDto> {
        val progress = orderService.getOrderProgress(orderId)
        return ResponseEntity.ok(progress)
    }

    /**
     * Получение деталей заказа
     */
    @GetMapping("/orders/{orderId}")
    fun getOrderDetails(
        @PathVariable orderId: UUID
    ): ResponseEntity<MasterOrderEntity> {
        val order = masterOrderRepository.findById(orderId)
            .orElseThrow { RuntimeException("Order not found") }
        return ResponseEntity.ok(order)
    }

    /**
     * Получение ставок по заказу
     */
    @GetMapping("/orders/{orderId}/bids")
    fun getBidsForOrder(
        @PathVariable orderId: UUID
    ): ResponseEntity<List<BidEntity>> {
        val bids = bidRepository.findByFreightOrderId(orderId)
        return ResponseEntity.ok(bids)
    }

    /**
     * Получение частичных заказов
     */
    @GetMapping("/orders/{orderId}/partial-orders")
    fun getPartialOrders(
        @PathVariable orderId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<PartialOrderEntity>> {
        val partialOrders = partialOrderRepository.findByMasterOrderId(orderId, pageable)
        return ResponseEntity.ok(partialOrders)
    }

    /**
     * Назначение перевозчика на частичный заказ
     */
    @PostMapping("/orders/{orderId}/partial-orders/{partialOrderId}/assign")
    fun assignCarrier(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orderId: UUID,
        @PathVariable partialOrderId: UUID,
        @RequestBody assignmentRequest: CarrierAssignmentRequest
    ): ResponseEntity<PartialOrderEntity> {
        val shipperId = UUID.fromString(jwt.subject)
        
        // Проверяем, что заказ принадлежит грузоотправителю
        val masterOrder = masterOrderRepository.findById(orderId)
            .orElseThrow { RuntimeException("Order not found") }
        
        if (masterOrder.shipperId != shipperId) {
            throw RuntimeException("Access denied: Order does not belong to shipper")
        }

        val partialOrder = orderService.assignCarrierToPartialOrder(
            partialOrderId = partialOrderId,
            carrierId = assignmentRequest.carrierId,
            bidId = assignmentRequest.bidId
        )

        return ResponseEntity.ok(partialOrder)
    }

    /**
     * Отмена заказа
     */
    @PostMapping("/orders/{orderId}/cancel")
    fun cancelOrder(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable orderId: UUID,
        @RequestBody cancellationRequest: OrderCancellationRequest
    ): ResponseEntity<String> {
        val shipperId = UUID.fromString(jwt.subject)
        
        val masterOrder = masterOrderRepository.findById(orderId)
            .orElseThrow { RuntimeException("Order not found") }
        
        if (masterOrder.shipperId != shipperId) {
            throw RuntimeException("Access denied: Order does not belong to shipper")
        }

        if (masterOrder.status == MasterOrderStatus.IN_PROGRESS || masterOrder.status == MasterOrderStatus.COMPLETED) {
            throw RuntimeException("Cannot cancel order that is in progress or completed")
        }

        masterOrder.status = MasterOrderStatus.CANCELLED
        masterOrderRepository.save(masterOrder)

        return ResponseEntity.ok("Order cancelled successfully")
    }

    /**
     * Получение статистики по заказам
     */
    @GetMapping("/orders/statistics")
    fun getOrderStatistics(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<OrderStatisticsDto> {
        val shipperId = UUID.fromString(jwt.subject)
        
        val totalOrders = masterOrderRepository.countByShipperId(shipperId)
        val activeOrders = masterOrderRepository.findByShipperIdAndStatusIn(
            shipperId,
            listOf(MasterOrderStatus.OPEN, MasterOrderStatus.PARTIALLY_FILLED)
        ).size
        val completedOrders = masterOrderRepository.findByShipperIdAndStatus(
            shipperId,
            MasterOrderStatus.COMPLETED
        ).size
        val cancelledOrders = masterOrderRepository.findByShipperIdAndStatus(
            shipperId,
            MasterOrderStatus.CANCELLED
        ).size

        val statistics = OrderStatisticsDto(
            totalOrders = totalOrders,
            activeOrders = activeOrders,
            completedOrders = completedOrders,
            cancelledOrders = cancelledOrders,
            averageFillRate = calculateAverageFillRate(shipperId)
        )

        return ResponseEntity.ok(statistics)
    }

    /**
     * Получение истории SCM проверок
     */
    @GetMapping("/orders/{orderId}/scm-history")
    fun getScmHistory(
        @PathVariable orderId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<ScmSnapshotEntity>> {
        val snapshots = scmSnapshotRepository.findByMasterOrderId(orderId, pageable)
        return ResponseEntity.ok(snapshots)
    }

    /**
     * Получение перевозчиков по рейтингу
     */
    @GetMapping("/carriers/top")
    fun getTopCarriers(
        pageable: Pageable
    ): ResponseEntity<Page<UserProfileEntity>> {
        val carriers = userProfileRepository.findByTotalOrdersGreaterThanEqual(10, pageable)
        return ResponseEntity.ok(carriers)
    }

    /**
     * Получение аудит-трейла заказа
     */
    @GetMapping("/orders/{orderId}/audit-trail")
    fun getOrderAuditTrail(
        @PathVariable orderId: UUID
    ): ResponseEntity<List<String>> {
        val snapshots = scmSnapshotRepository.findByMasterOrderId(orderId)
        val auditTrail = snapshots.map { it.auditTrail }
        
        return ResponseEntity.ok(auditTrail)
    }

    /**
     * Экспорт данных заказа
     */
    @GetMapping("/orders/{orderId}/export")
    fun exportOrderData(
        @PathVariable orderId: UUID
    ): ResponseEntity<OrderExportDto> {
        val masterOrder = masterOrderRepository.findById(orderId)
            .orElseThrow { RuntimeException("Order not found") }
        
        val partialOrders = partialOrderRepository.findByMasterOrderId(orderId)
        val bids = bidRepository.findByFreightOrderId(orderId)
        val snapshots = scmSnapshotRepository.findByMasterOrderId(orderId)

        val exportData = OrderExportDto(
            orderId = orderId,
            orderDetails = masterOrder,
            partialOrders = partialOrders,
            bids = bids,
            scmSnapshots = snapshots,
            exportDate = LocalDateTime.now()
        )

        return ResponseEntity.ok(exportData)
    }

    // Вспомогательные методы

    private fun calculateAverageFillRate(shipperId: UUID): Double {
        val orders = masterOrderRepository.findByShipperId(shipperId)
        if (orders.isEmpty()) return 0.0

        val totalFillRate = orders.sumOf { order ->
            val partialOrders = partialOrderRepository.findByMasterOrderId(order.id!!)
            val assignedWeight = partialOrders.sumOf { it.weight.toDouble() }
            assignedWeight / order.totalWeight.toDouble()
        }

        return totalFillRate / orders.size
    }

    private fun createPoint(location: LocationInfo): org.locationtech.jts.geom.Point {
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        return geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(location.longitude, location.latitude))
    }
}

/**
 * DTO для запроса на создание заказа
 */
data class OrderRequestDto(
    val title: String,
    val description: String? = null,
    val cargoType: String,
    val totalWeight: BigDecimal,
    val totalVolume: BigDecimal,
    val pickupLocation: LocationInfo,
    val deliveryLocation: LocationInfo,
    val pickupAddress: String,
    val deliveryAddress: String,
    val requiredDeliveryDate: LocalDateTime,
    val maxBidAmount: BigDecimal,
    val isLtlEnabled: Boolean = true,
    val minLoadPercentage: Double = 0.8,
    val partialOrderSize: BigDecimal? = null
)

/**
 * DTO для назначения перевозчика
 */
data class CarrierAssignmentRequest(
    val carrierId: UUID,
    val bidId: UUID
)

/**
 * DTO для отмены заказа
 */
data class OrderCancellationRequest(
    val reason: String
)

/**
 * DTO для статистики заказов
 */
data class OrderStatisticsDto(
    val totalOrders: Long,
    val activeOrders: Int,
    val completedOrders: Int,
    val cancelledOrders: Int,
    val averageFillRate: Double
)

/**
 * DTO для экспорта данных заказа
 */
data class OrderExportDto(
    val orderId: UUID,
    val orderDetails: MasterOrderEntity,
    val partialOrders: List<PartialOrderEntity>,
    val bids: List<BidEntity>,
    val scmSnapshots: List<ScmSnapshotEntity>,
    val exportDate: LocalDateTime
)