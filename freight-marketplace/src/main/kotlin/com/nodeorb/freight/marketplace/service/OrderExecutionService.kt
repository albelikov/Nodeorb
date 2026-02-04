package com.nodeorb.freight.marketplace.service

import com.nodeorb.freight.marketplace.dto.OrderFillingWarningEvent
import com.nodeorb.freight.marketplace.entity.*
import com.nodeorb.freight.marketplace.repository.MasterOrderRepository
import com.nodeorb.freight.marketplace.repository.PartialOrderRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.logging.Logger

/**
 * Сервис управления жизненным циклом Master Order
 * Реализует фоновые задачи и автоматическое управление заказами
 */
@Service
@Transactional
class OrderExecutionService(
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    companion object {
        private val logger = Logger.getLogger(OrderExecutionService::class.java.name)
        private const val WARNING_HOURS_BEFORE_PICKUP = 2L
        private const val WARNING_THRESHOLD_PERCENTAGE = 0.5
    }

    /**
     * Cron Job: Автоматическая проверка заполненности заказов
     * Запускается каждые 15 минут
     */
    @Scheduled(cron = "0 */15 * * * *")
    fun checkOrderFillingStatus() {
        logger.info("Starting order filling status check")
        
        try {
            val now = LocalDateTime.now()
            val cutoffTime = now.plusHours(WARNING_HOURS_BEFORE_PICKUP)

            // Находим заказы, у которых погрузка через 2 часа
            val ordersToCheck = masterOrderRepository.findByStatusInAndRequiredDeliveryDateBetween(
                listOf(MasterOrderStatus.OPEN, MasterOrderStatus.PARTIALLY_FILLED),
                now,
                cutoffTime
            )

            logger.info("Found ${ordersToCheck.size} orders to check for filling status")

            ordersToCheck.forEach { order ->
                try {
                    checkOrderFillingAndSendWarning(order)
                } catch (e: Exception) {
                    logger.severe("Error checking order ${order.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            logger.severe("Error in order filling status check: ${e.message}")
        }
    }

    /**
     * Проверка заполненности заказа и отправка предупреждения
     */
    private fun checkOrderFillingAndSendWarning(masterOrder: MasterOrderEntity) {
        val partialOrders = partialOrderRepository.findByMasterOrderId(masterOrder.id!!)
        val totalAssignedVolume = partialOrders.sumOf { it.volume.toDouble() }
        val totalVolume = masterOrder.totalVolume.toDouble()
        
        val fillPercentage = if (totalVolume > 0) {
            totalAssignedVolume / totalVolume
        } else 1.0

        val remainingPercentage = 1.0 - fillPercentage

        logger.info("Order ${masterOrder.id}: fill percentage = ${fillPercentage * 100}%, remaining = ${remainingPercentage * 100}%")

        // Если осталось более 50% объема
        if (remainingPercentage > WARNING_THRESHOLD_PERCENTAGE) {
            sendOrderFillingWarning(masterOrder, remainingPercentage)
        }
    }

    /**
     * Отправка предупреждения о низкой заполненности заказа
     */
    private fun sendOrderFillingWarning(masterOrder: MasterOrderEntity, remainingPercentage: Double) {
        val warningEvent = OrderFillingWarningEvent(
            orderId = masterOrder.id!!,
            shipperId = masterOrder.shipperId,
            remainingPercentage = remainingPercentage,
            pickupTime = masterOrder.requiredDeliveryDate.minusHours(24), // Предполагаем погрузку за сутки до доставки
            warningTime = LocalDateTime.now(),
            message = "Order has ${remainingPercentage * 100}% remaining volume with 2 hours until pickup"
        )

        kafkaTemplate.send("order.filling.warnings", warningEvent)
        
        logger.warning("Sent filling warning for order ${masterOrder.id}: ${warningEvent.message}")
    }

    /**
     * e-POD Aggregator: Проверка и обновление статуса Master Order
     * MasterOrder переводится в COMPLETED только когда ВСЕ PartialOrder имеют статус DELIVERED
     */
    fun updateMasterOrderStatus(masterOrderId: UUID): MasterOrderEntity {
        logger.info("Updating status for master order: $masterOrderId")

        val masterOrder = masterOrderRepository.findById(masterOrderId)
            .orElseThrow { RuntimeException("Master order not found: $masterOrderId") }

        val partialOrders = partialOrderRepository.findByMasterOrderId(masterOrderId)
        
        if (partialOrders.isEmpty()) {
            logger.warning("No partial orders found for master order: $masterOrderId")
            return masterOrder
        }

        val allDelivered = partialOrders.all { it.status == PartialOrderStatus.DELIVERED }
        val anyInProgress = partialOrders.any { it.status == PartialOrderStatus.IN_PROGRESS }
        val anyPending = partialOrders.any { it.status in listOf(PartialOrderStatus.AVAILABLE, PartialOrderStatus.BIDDING) }

        val newStatus = when {
            allDelivered -> MasterOrderStatus.COMPLETED
            anyInProgress -> MasterOrderStatus.IN_PROGRESS
            anyPending -> MasterOrderStatus.PARTIALLY_FILLED
            else -> masterOrder.status
        }

        if (newStatus != masterOrder.status) {
            logger.info("Updating master order ${masterOrder.id} status from ${masterOrder.status} to $newStatus")
            masterOrder.status = newStatus
            masterOrder.updatedAt = LocalDateTime.now()
            
            masterOrderRepository.save(masterOrder)
            
            // Отправляем событие об изменении статуса
            sendOrderStatusChangedEvent(masterOrder)
        }

        return masterOrder
    }

    /**
     * Capacity Guard: Проверка возможности создания Partial Order
     * Запрещает создание ставок если остаток объема меньше minQuantum
     */
    fun validatePartialOrderCapacity(masterOrderId: UUID, requestedVolume: BigDecimal): Boolean {
        val masterOrder = masterOrderRepository.findById(masterOrderId)
            .orElseThrow { RuntimeException("Master order not found: $masterOrderId") }

        val minQuantum = calculateMinQuantum(masterOrder)
        val remainingVolume = masterOrder.remainingVolume

        logger.info("Validating capacity for master order ${masterOrder.id}: remaining = $remainingVolume, minQuantum = $minQuantum, requested = $requestedVolume")

        val hasSufficientCapacity = remainingVolume >= minQuantum
        val requestedVolumeValid = requestedVolume >= minQuantum

        if (!hasSufficientCapacity) {
            logger.warning("Insufficient capacity for master order ${masterOrder.id}: remaining $remainingVolume < minQuantum $minQuantum")
        }

        if (!requestedVolumeValid) {
            logger.warning("Requested volume too small for master order ${masterOrder.id}: requested $requestedVolume < minQuantum $minQuantum")
        }

        return hasSufficientCapacity && requestedVolumeValid
    }

    /**
     * Расчет минимального кванта объема для заказа
     */
    private fun calculateMinQuantum(masterOrder: MasterOrderEntity): BigDecimal {
        // Минимальный квант = 10% от общего объема или 1 кубический метр, whichever is greater
        val percentageBasedQuantum = masterOrder.totalVolume * BigDecimal("0.1")
        val minimumQuantum = BigDecimal("1.0")
        
        return if (percentageBasedQuantum > minimumQuantum) percentageBasedQuantum else minimumQuantum
    }

    /**
     * Проверка необходимости завершения заказа
     */
    fun checkOrderCompletion(masterOrderId: UUID): Boolean {
        val masterOrder = masterOrderRepository.findById(masterOrderId)
            .orElseThrow { RuntimeException("Master order not found: $masterOrderId") }

        if (masterOrder.status == MasterOrderStatus.COMPLETED) {
            return true
        }

        val partialOrders = partialOrderRepository.findByMasterOrderId(masterOrderId)
        val allDelivered = partialOrders.all { it.status == PartialOrderStatus.DELIVERED }

        if (allDelivered && masterOrder.status != MasterOrderStatus.COMPLETED) {
            logger.info("All partial orders delivered for master order ${masterOrder.id}, updating status to COMPLETED")
            masterOrder.status = MasterOrderStatus.COMPLETED
            masterOrder.completedAt = LocalDateTime.now()
            masterOrderRepository.save(masterOrder)
            
            sendOrderCompletedEvent(masterOrder)
            return true
        }

        return false
    }

    /**
     * Отправка события об изменении статуса заказа
     */
    private fun sendOrderStatusChangedEvent(masterOrder: MasterOrderEntity) {
        val event = mapOf(
            "orderId" to masterOrder.id,
            "shipperId" to masterOrder.shipperId,
            "status" to masterOrder.status.name,
            "timestamp" to LocalDateTime.now(),
            "type" to "ORDER_STATUS_CHANGED"
        )
        
        kafkaTemplate.send("order.status.changed", event)
    }

    /**
     * Отправка события о завершении заказа
     */
    private fun sendOrderCompletedEvent(masterOrder: MasterOrderEntity) {
        val event = mapOf(
            "orderId" to masterOrder.id,
            "shipperId" to masterOrder.shipperId,
            "completedAt" to masterOrder.completedAt,
            "timestamp" to LocalDateTime.now(),
            "type" to "ORDER_COMPLETED"
        )
        
        kafkaTemplate.send("order.completed", event)
    }

    /**
     * Получение статистики по заказам
     */
    fun getOrderExecutionStatistics(): OrderExecutionStatistics {
        val totalOrders = masterOrderRepository.count()
        val completedOrders = masterOrderRepository.countByStatus(MasterOrderStatus.COMPLETED)
        val inProgressOrders = masterOrderRepository.countByStatus(MasterOrderStatus.IN_PROGRESS)
        val pendingOrders = masterOrderRepository.countByStatus(MasterOrderStatus.PARTIALLY_FILLED)
        val openOrders = masterOrderRepository.countByStatus(MasterOrderStatus.OPEN)
        val cancelledOrders = masterOrderRepository.countByStatus(MasterOrderStatus.CANCELLED)

        return OrderExecutionStatistics(
            totalOrders = totalOrders,
            completedOrders = completedOrders,
            inProgressOrders = inProgressOrders,
            pendingOrders = pendingOrders,
            openOrders = openOrders,
            cancelledOrders = cancelledOrders,
            completionRate = if (totalOrders > 0) completedOrders.toDouble() / totalOrders.toDouble() else 0.0
        )
    }

    /**
     * Принудительное завершение заказа (для экстренных случаев)
     */
    fun forceCompleteOrder(masterOrderId: UUID, reason: String): MasterOrderEntity {
        logger.warning("Force completing order ${masterOrderId} with reason: $reason")

        val masterOrder = masterOrderRepository.findById(masterOrderId)
            .orElseThrow { RuntimeException("Master order not found: $masterOrderId") }

        if (masterOrder.status == MasterOrderStatus.COMPLETED) {
            logger.info("Order ${masterOrderId} is already completed")
            return masterOrder
        }

        // Отменяем все незавершенные частичные заказы
        val partialOrders = partialOrderRepository.findByMasterOrderId(masterOrderId)
        partialOrders.forEach { partialOrder ->
            if (partialOrder.status !in listOf(PartialOrderStatus.DELIVERED, PartialOrderStatus.COMPLETED)) {
                partialOrder.status = PartialOrderStatus.CANCELLED
                partialOrderRepository.save(partialOrder)
            }
        }

        // Завершаем мастер-заказ
        masterOrder.status = MasterOrderStatus.COMPLETED
        masterOrder.completedAt = LocalDateTime.now()
        masterOrderRepository.save(masterOrder)

        // Отправляем событие
        sendOrderForceCompletedEvent(masterOrder, reason)

        return masterOrder
    }

    /**
     * Отправка события о принудительном завершении заказа
     */
    private fun sendOrderForceCompletedEvent(masterOrder: MasterOrderEntity, reason: String) {
        val event = mapOf(
            "orderId" to masterOrder.id,
            "shipperId" to masterOrder.shipperId,
            "reason" to reason,
            "completedAt" to masterOrder.completedAt,
            "timestamp" to LocalDateTime.now(),
            "type" to "ORDER_FORCE_COMPLETED"
        )
        
        kafkaTemplate.send("order.force.completed", event)
    }
}

/**
 * DTO для события предупреждения о заполненности заказа
 */
data class OrderFillingWarningEvent(
    val orderId: UUID,
    val shipperId: UUID,
    val remainingPercentage: Double,
    val pickupTime: LocalDateTime,
    val warningTime: LocalDateTime,
    val message: String
)

/**
 * DTO для статистики выполнения заказов
 */
data class OrderExecutionStatistics(
    val totalOrders: Long,
    val completedOrders: Long,
    val inProgressOrders: Long,
    val pendingOrders: Long,
    val openOrders: Long,
    val cancelledOrders: Long,
    val completionRate: Double
)