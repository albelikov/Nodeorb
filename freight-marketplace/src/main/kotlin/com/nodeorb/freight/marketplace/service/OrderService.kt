package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.*
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.*
import com.nodeorb.freight.marketplace.service.ScmIntegrationService
import com.nodeorb.freight.marketplace.service.OrderExecutionService
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.logging.Logger

/**
 * Сервис управления заказами
 * Реализует логику расчета статусов прогресс-бара и автоматической проверки заполненности
 */
@Service
@Transactional
class OrderService(
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository,
    private val bidRepository: BidRepository,
    private val userProfileRepository: UserProfileRepository,
    private val scmSnapshotRepository: ScmSnapshotRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val scmIntegrationService: ScmIntegrationService,
    private val orderExecutionService: OrderExecutionService
) {

    companion object {
        private val logger = Logger.getLogger(OrderService::class.java.name)
        private const val AUTO_CANCELLATION_HOURS = 2L
        private const val REMINDER_HOURS_BEFORE_CANCELLATION = 1L
    }

    /**
     * Создание мастер-заказа с автоматическим дроблением на частичные заказы
     */
    fun createMasterOrder(
        shipperId: UUID,
        title: String,
        description: String?,
        cargoType: CargoType,
        totalWeight: BigDecimal,
        totalVolume: BigDecimal,
        pickupLocation: org.locationtech.jts.geom.Point,
        deliveryLocation: org.locationtech.jts.geom.Point,
        pickupAddress: String,
        deliveryAddress: String,
        requiredDeliveryDate: LocalDateTime,
        maxBidAmount: BigDecimal,
        isLtlEnabled: Boolean = true,
        minLoadPercentage: Double = 0.8,
        partialOrderSize: BigDecimal? = null
    ): MasterOrderEntity {
        logger.info("Creating master order for shipper: $shipperId")

        val masterOrder = MasterOrderEntity(
            shipperId = shipperId,
            title = title,
            description = description,
            cargoType = cargoType,
            totalWeight = totalWeight,
            totalVolume = totalVolume,
            remainingWeight = totalWeight,
            remainingVolume = totalVolume,
            pickupLocation = pickupLocation,
            deliveryLocation = deliveryLocation,
            pickupAddress = pickupAddress,
            deliveryAddress = deliveryAddress,
            requiredDeliveryDate = requiredDeliveryDate,
            maxBidAmount = maxBidAmount,
            isLtlEnabled = isLtlEnabled,
            minLoadPercentage = minLoadPercentage
        )

        val savedOrder = masterOrderRepository.save(masterOrder)

        // Создаем частичные заказы
        if (isLtlEnabled) {
            createPartialOrders(savedOrder, partialOrderSize)
        }

        // Отправляем событие о создании заказа
        sendOrderCreatedEvent(savedOrder)

        logger.info("Master order created: ${savedOrder.id}")
        return savedOrder
    }

    /**
     * Создание частичного заказа с проверкой Capacity Guard
     */
    fun createPartialOrder(
        masterOrderId: UUID,
        weight: BigDecimal,
        volume: BigDecimal,
        percentage: Double,
        carrierId: UUID? = null
    ): PartialOrderEntity {
        logger.info("Creating partial order for master order: $masterOrderId")

        // Проверка Capacity Guard
        if (!orderExecutionService.validatePartialOrderCapacity(masterOrderId, volume)) {
            throw RuntimeException("Insufficient capacity for partial order creation")
        }

        val masterOrder = masterOrderRepository.findById(masterOrderId)
            .orElseThrow { RuntimeException("Master order not found: $masterOrderId") }

        val partialOrder = PartialOrderEntity(
            masterOrder = masterOrder,
            weight = weight,
            volume = volume,
            percentage = percentage,
            status = if (carrierId != null) PartialOrderStatus.AWARDED else PartialOrderStatus.AVAILABLE
        )

        if (carrierId != null) {
            partialOrder.assignedCarrierId = carrierId
        }

        val savedPartialOrder = partialOrderRepository.save(partialOrder)

        // Обновляем остатки в мастер-заказе
        updateMasterOrderRemainingCapacity(masterOrder, savedPartialOrder)

        logger.info("Partial order created: ${savedPartialOrder.id}")
        return savedPartialOrder
    }

    /**
     * Получение прогресса заказа
     */
    fun getOrderProgress(masterOrderId: UUID): OrderProgressDto {
        val masterOrder = masterOrderRepository.findById(masterOrderId)
            .orElseThrow { RuntimeException("Master order not found: $masterOrderId") }

        val partialOrders = partialOrderRepository.findByMasterOrderId(masterOrderId)
        val progressStatus = calculateProgressStatus(masterOrder, partialOrders)
        val filledPercentage = calculateFilledPercentage(masterOrder, partialOrders)

        val (committedWeight, committedVolume) = calculateCommittedWeightAndVolume(partialOrders)
        val (pendingWeight, pendingVolume) = calculatePendingWeightAndVolume(partialOrders)
        val (openWeight, openVolume) = calculateOpenWeightAndVolume(masterOrder, partialOrders)

        val estimatedCompletionTime = calculateEstimatedCompletionTime(partialOrders)
        val autoCancellationTime = calculateAutoCancellationTime(masterOrder)

        return OrderProgressDto(
            masterOrderId = masterOrder.id!!,
            totalWeight = masterOrder.totalWeight,
            totalVolume = masterOrder.totalVolume,
            remainingWeight = masterOrder.remainingWeight,
            remainingVolume = masterOrder.remainingVolume,
            filledPercentage = filledPercentage,
            status = masterOrder.status.name,
            progressStatus = progressStatus,
            committedWeight = committedWeight,
            committedVolume = committedVolume,
            pendingWeight = pendingWeight,
            pendingVolume = pendingVolume,
            openWeight = openWeight,
            openVolume = openVolume,
            partialOrders = partialOrders.map { mapToPartialOrderProgressDto(it) },
            estimatedCompletionTime = estimatedCompletionTime,
            autoCancellationTime = autoCancellationTime
        )
    }

    /**
     * Назначение перевозчика на частичный заказ
     */
    fun assignCarrierToPartialOrder(
        partialOrderId: UUID,
        carrierId: UUID,
        bidId: UUID
    ): PartialOrderEntity {
        logger.info("Assigning carrier $carrierId to partial order $partialOrderId")

        val partialOrder = partialOrderRepository.findById(partialOrderId)
            .orElseThrow { RuntimeException("Partial order not found: $partialOrderId") }

        if (partialOrder.status != PartialOrderStatus.AVAILABLE) {
            throw RuntimeException("Partial order is not available for assignment")
        }

        // Проверяем соответствие перевозчика
        val complianceResult = checkCarrierCompliance(carrierId, partialOrder.masterOrder)
        if (complianceResult.complianceStatus != "COMPLIANT") {
            throw RuntimeException("Carrier does not meet compliance requirements")
        }

        // Назначаем перевозчика
        partialOrder.assignedCarrierId = carrierId
        partialOrder.assignedBidId = bidId
        partialOrder.status = PartialOrderStatus.AWARDED

        // Обновляем остатки в мастер-заказе
        updateMasterOrderRemainingCapacity(partialOrder.masterOrder, partialOrder)

        // Отправляем событие о назначении
        sendCarrierAssignedEvent(partialOrder, carrierId)

        // Проверяем необходимость завершения заказа
        orderExecutionService.checkOrderCompletion(partialOrder.masterOrder.id!!)

        logger.info("Carrier $carrierId assigned to partial order $partialOrderId")
        return partialOrderRepository.save(partialOrder)
    }

    /**
     * Обновление статуса частичного заказа
     */
    fun updatePartialOrderStatus(partialOrderId: UUID, newStatus: PartialOrderStatus): PartialOrderEntity {
        logger.info("Updating partial order $partialOrderId status to $newStatus")

        val partialOrder = partialOrderRepository.findById(partialOrderId)
            .orElseThrow { RuntimeException("Partial order not found: $partialOrderId") }

        val oldStatus = partialOrder.status
        partialOrder.status = newStatus
        partialOrder.updatedAt = LocalDateTime.now()

        val updatedPartialOrder = partialOrderRepository.save(partialOrder)

        // Отправляем событие об изменении статуса
        sendPartialOrderStatusChangedEvent(updatedPartialOrder, oldStatus, newStatus)

        // Проверяем необходимость завершения заказа
        orderExecutionService.checkOrderCompletion(partialOrder.masterOrder.id!!)

        logger.info("Partial order $partialOrderId status updated from $oldStatus to $newStatus")
        return updatedPartialOrder
    }

    /**
     * Автоматическая проверка заполненности заказа
     */
    fun performAutoCheck(masterOrderId: UUID): AutoCheckResult {
        val masterOrder = masterOrderRepository.findById(masterOrderId)
            .orElseThrow { RuntimeException("Master order not found: $masterOrderId") }

        val partialOrders = partialOrderRepository.findByMasterOrderId(masterOrderId)
        val currentFilledPercentage = calculateFilledPercentage(masterOrder, partialOrders)
        
        val pickupTime = masterOrder.requiredDeliveryDate.minusHours(24) // Предполагаем погрузку за сутки до доставки
        val cancellationTime = pickupTime.minusHours(AUTO_CANCELLATION_HOURS)
        val now = LocalDateTime.now()

        // Проверяем, нужно ли отменять заказ
        if (now.isAfter(cancellationTime)) {
            if (currentFilledPercentage < masterOrder.minLoadPercentage) {
                return cancelOrderDueToLowFillRate(masterOrder, partialOrders)
            }
        }

        // Проверяем частичные заказы на просроченность
        val expiredPartialOrders = partialOrders.filter { isPartialOrderExpired(it, cancellationTime) }
        if (expiredPartialOrders.isNotEmpty()) {
            return cancelExpiredPartialOrders(expiredPartialOrders)
        }

        // Проверяем необходимость напоминания
        val reminderTime = cancellationTime.minusHours(REMINDER_HOURS_BEFORE_CANCELLATION)
        if (now.isAfter(reminderTime) && currentFilledPercentage < masterOrder.minLoadPercentage) {
            return sendReminder(masterOrder, currentFilledPercentage)
        }

        return AutoCheckResult(
            masterOrderId = masterOrder.id!!,
            action = AutoCheckAction.NONE,
            reason = "Order is within acceptable parameters",
            timestamp = now
        )
    }

    /**
     * Запуск автоматической проверки для всех заказов
     */
    fun performAutoCheckForAllOrders(): List<AutoCheckResult> {
        val now = LocalDateTime.now()
        val cutoffTime = now.plusHours(AUTO_CANCELLATION_HOURS + 1)

        val ordersToCheck = masterOrderRepository.findByStatusIn(
            listOf(MasterOrderStatus.OPEN, MasterOrderStatus.PARTIALLY_FILLED),
            org.springframework.data.domain.PageRequest.of(0, 100)
        ).content

        return ordersToCheck.map { order ->
            try {
                performAutoCheck(order.id!!)
            } catch (e: Exception) {
                logger.severe("Error checking order ${order.id}: ${e.message}")
                AutoCheckResult(
                    masterOrderId = order.id!!,
                    action = AutoCheckAction.NONE,
                    reason = "Error during auto check: ${e.message}",
                    timestamp = now
                )
            }
        }
    }

    /**
     * Отмена заказа из-за низкого уровня заполненности
     */
    private fun cancelOrderDueToLowFillRate(
        masterOrder: MasterOrderEntity,
        partialOrders: List<PartialOrderEntity>
    ): AutoCheckResult {
        logger.warning("Cancelling order ${masterOrder.id} due to low fill rate")

        // Отменяем все частичные заказы
        partialOrders.forEach { partialOrder ->
            if (partialOrder.status == PartialOrderStatus.AVAILABLE) {
                partialOrder.status = PartialOrderStatus.CANCELLED
                partialOrderRepository.save(partialOrder)
            }
        }

        // Отменяем мастер-заказ
        masterOrder.status = MasterOrderStatus.CANCELLED
        masterOrderRepository.save(masterOrder)

        // Отправляем событие об отмене
        sendOrderCancelledEvent(masterOrder, "Low fill rate")

        return AutoCheckResult(
            masterOrderId = masterOrder.id!!,
            action = AutoCheckAction.CANCEL_MASTER_ORDER,
            reason = "Order cancelled due to low fill rate: ${calculateFilledPercentage(masterOrder, partialOrders)}%",
            timestamp = LocalDateTime.now()
        )
    }

    /**
     * Отмена просроченных частичных заказов
     */
    private fun cancelExpiredPartialOrders(expiredPartialOrders: List<PartialOrderEntity>): AutoCheckResult {
        logger.warning("Cancelling ${expiredPartialOrders.size} expired partial orders")

        expiredPartialOrders.forEach { partialOrder ->
            partialOrder.status = PartialOrderStatus.CANCELLED
            partialOrderRepository.save(partialOrder)
        }

        // Отправляем события об отмене
        expiredPartialOrders.forEach { partialOrder ->
            sendPartialOrderCancelledEvent(partialOrder, "Expired")
        }

        return AutoCheckResult(
            masterOrderId = expiredPartialOrders.first().masterOrder.id!!,
            action = AutoCheckAction.CANCEL_PARTIAL_ORDER,
            reason = "Cancelled ${expiredPartialOrders.size} expired partial orders",
            affectedPartialOrders = expiredPartialOrders.map { it.id!! },
            timestamp = LocalDateTime.now()
        )
    }

    /**
     * Отправка напоминания
     */
    private fun sendReminder(
        masterOrder: MasterOrderEntity,
        currentFilledPercentage: Double
    ): AutoCheckResult {
        logger.info("Sending reminder for order ${masterOrder.id}")

        val reminderEvent = mapOf(
            "orderId" to masterOrder.id,
            "shipperId" to masterOrder.shipperId,
            "filledPercentage" to currentFilledPercentage,
            "minLoadPercentage" to masterOrder.minLoadPercentage,
            "message" to "Order is approaching auto-cancellation time",
            "timestamp" to LocalDateTime.now()
        )

        kafkaTemplate.send("order.reminders", reminderEvent)

        return AutoCheckResult(
            masterOrderId = masterOrder.id!!,
            action = AutoCheckAction.SEND_REMINDER,
            reason = "Reminder sent due to approaching cancellation time",
            timestamp = LocalDateTime.now()
        )
    }

    // Вспомогательные методы

    private fun createPartialOrders(masterOrder: MasterOrderEntity, partialOrderSize: BigDecimal?) {
        val orderSize = partialOrderSize ?: calculateOptimalPartialOrderSize(masterOrder)
        val numberOfPartialOrders = (masterOrder.totalWeight / orderSize).toInt()
        val remainder = masterOrder.totalWeight % orderSize

        repeat(numberOfPartialOrders) {
            val partialOrder = PartialOrderEntity(
                masterOrder = masterOrder,
                weight = orderSize,
                volume = orderSize * (masterOrder.totalVolume / masterOrder.totalWeight),
                percentage = orderSize.toDouble() / masterOrder.totalWeight.toDouble()
            )
            partialOrderRepository.save(partialOrder)
        }

        // Создаем частичный заказ для остатка
        if (remainder > BigDecimal.ZERO) {
            val partialOrder = PartialOrderEntity(
                masterOrder = masterOrder,
                weight = remainder,
                volume = remainder * (masterOrder.totalVolume / masterOrder.totalWeight),
                percentage = remainder.toDouble() / masterOrder.totalWeight.toDouble()
            )
            partialOrderRepository.save(partialOrder)
        }
    }

    private fun calculateOptimalPartialOrderSize(masterOrder: MasterOrderEntity): BigDecimal {
        // Оптимальный размер частичного заказа - 20% от общего объема
        return masterOrder.totalWeight * BigDecimal("0.2")
    }

    private fun calculateProgressStatus(
        masterOrder: MasterOrderEntity,
        partialOrders: List<PartialOrderEntity>
    ): ProgressStatus {
        val filledPercentage = calculateFilledPercentage(masterOrder, partialOrders)

        return when {
            filledPercentage >= 1.0 -> ProgressStatus.COMMITTED
            filledPercentage > 0.0 -> ProgressStatus.PENDING
            else -> ProgressStatus.OPEN
        }
    }

    private fun calculateFilledPercentage(
        masterOrder: MasterOrderEntity,
        partialOrders: List<PartialOrderEntity>
    ): Double {
        val assignedWeight = partialOrders.sumOf { it.weight.toDouble() }
        return assignedWeight / masterOrder.totalWeight.toDouble()
    }

    private fun calculateCommittedWeightAndVolume(partialOrders: List<PartialOrderEntity>): Pair<BigDecimal, BigDecimal> {
        val committedOrders = partialOrders.filter { it.status == PartialOrderStatus.AWARDED }
        val weight = committedOrders.sumOf { it.weight }
        val volume = committedOrders.sumOf { it.volume }
        return weight to volume
    }

    private fun calculatePendingWeightAndVolume(partialOrders: List<PartialOrderEntity>): Pair<BigDecimal, BigDecimal> {
        val pendingOrders = partialOrders.filter { it.status == PartialOrderStatus.BIDDING }
        val weight = pendingOrders.sumOf { it.weight }
        val volume = pendingOrders.sumOf { it.volume }
        return weight to volume
    }

    private fun calculateOpenWeightAndVolume(
        masterOrder: MasterOrderEntity,
        partialOrders: List<PartialOrderEntity>
    ): Pair<BigDecimal, BigDecimal> {
        val openOrders = partialOrders.filter { it.status == PartialOrderStatus.AVAILABLE }
        val weight = openOrders.sumOf { it.weight }
        val volume = openOrders.sumOf { it.volume }
        return weight to volume
    }

    private fun calculateEstimatedCompletionTime(partialOrders: List<PartialOrderEntity>): LocalDateTime? {
        val awardedOrders = partialOrders.filter { it.status == PartialOrderStatus.AWARDED }
        return awardedOrders.maxOfOrNull { it.updatedAt }
    }

    private fun calculateAutoCancellationTime(masterOrder: MasterOrderEntity): LocalDateTime {
        // Предполагаем погрузку за сутки до доставки
        val pickupTime = masterOrder.requiredDeliveryDate.minusHours(24)
        return pickupTime.minusHours(AUTO_CANCELLATION_HOURS)
    }

    private fun checkCarrierCompliance(carrierId: UUID, masterOrder: MasterOrderEntity): ComplianceCheckResult {
        // Здесь должна быть интеграция с SCM сервисом
        // Для примера возвращаем заглушку
        return ComplianceCheckResult(
            carrierId = carrierId,
            masterOrderId = masterOrder.id!!,
            bidId = UUID.randomUUID(),
            complianceStatus = "COMPLIANT",
            complianceDetails = "Compliance check passed",
            securityClearance = "NONE",
            securityDetails = "No security restrictions",
            riskScore = 0.1,
            riskFactors = emptyList(),
            trustToken = null,
            auditTrail = "Compliance check completed"
        )
    }

    private fun updateMasterOrderRemainingCapacity(masterOrder: MasterOrderEntity, partialOrder: PartialOrderEntity) {
        masterOrder.remainingWeight -= partialOrder.weight
        masterOrder.remainingVolume -= partialOrder.volume

        // Обновляем статус мастер-заказа
        val filledPercentage = calculateFilledPercentage(masterOrder, listOf(partialOrder))
        masterOrder.status = when {
            filledPercentage >= 1.0 -> MasterOrderStatus.FILLED
            filledPercentage > 0.0 -> MasterOrderStatus.PARTIALLY_FILLED
            else -> MasterOrderStatus.OPEN
        }

        masterOrderRepository.save(masterOrder)
    }

    private fun isPartialOrderExpired(partialOrder: PartialOrderEntity, cancellationTime: LocalDateTime): Boolean {
        return partialOrder.status == PartialOrderStatus.AVAILABLE && 
               partialOrder.createdAt != null && 
               partialOrder.createdAt!! < cancellationTime
    }

    private fun mapToPartialOrderProgressDto(partialOrder: PartialOrderEntity): PartialOrderProgressDto {
        return PartialOrderProgressDto(
            partialOrderId = partialOrder.id!!,
            weight = partialOrder.weight,
            volume = partialOrder.volume,
            percentage = partialOrder.percentage,
            status = partialOrder.status.name,
            assignedCarrierId = partialOrder.assignedCarrierId,
            estimatedPickupTime = null, // Можно рассчитать на основе маршрута
            estimatedDeliveryTime = null
        )
    }

    private fun sendOrderCreatedEvent(masterOrder: MasterOrderEntity) {
        val event = mapOf(
            "orderId" to masterOrder.id,
            "shipperId" to masterOrder.shipperId,
            "title" to masterOrder.title,
            "status" to masterOrder.status.name,
            "timestamp" to LocalDateTime.now()
        )
        kafkaTemplate.send("order.created", event)
    }

    private fun sendCarrierAssignedEvent(partialOrder: PartialOrderEntity, carrierId: UUID) {
        val event = mapOf(
            "partialOrderId" to partialOrder.id,
            "carrierId" to carrierId,
            "masterOrderId" to partialOrder.masterOrder.id,
            "status" to partialOrder.status.name,
            "timestamp" to LocalDateTime.now()
        )
        kafkaTemplate.send("carrier.assigned", event)
    }

    private fun sendOrderCancelledEvent(masterOrder: MasterOrderEntity, reason: String) {
        val event = mapOf(
            "orderId" to masterOrder.id,
            "shipperId" to masterOrder.shipperId,
            "reason" to reason,
            "timestamp" to LocalDateTime.now()
        )
        kafkaTemplate.send("order.cancelled", event)
    }

    private fun sendPartialOrderCancelledEvent(partialOrder: PartialOrderEntity, reason: String) {
        val event = mapOf(
            "partialOrderId" to partialOrder.id,
            "masterOrderId" to partialOrder.masterOrder.id,
            "reason" to reason,
            "timestamp" to LocalDateTime.now()
        )
        kafkaTemplate.send("partial.order.cancelled", event)
    }

    private fun sendPartialOrderStatusChangedEvent(
        partialOrder: PartialOrderEntity,
        oldStatus: PartialOrderStatus,
        newStatus: PartialOrderStatus
    ) {
        val event = mapOf(
            "partialOrderId" to partialOrder.id,
            "masterOrderId" to partialOrder.masterOrder.id,
            "oldStatus" to oldStatus.name,
            "newStatus" to newStatus.name,
            "timestamp" to LocalDateTime.now()
        )
        kafkaTemplate.send("partial.order.status.changed", event)
    }
}