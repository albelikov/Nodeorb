package com.freight.marketplace.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.freight.marketplace.dto.*
import com.freight.marketplace.entity.*
import com.freight.marketplace.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Сервис аналитики для Shipper Dashboard
 * Предоставляет данные о ставках, прогрессе заказов и соответствия правилам
 */
@Service
class AnalyticsService(
    private val bidRepository: BidRepository,
    private val freightOrderRepository: FreightOrderRepository,
    private val masterOrderRepository: MasterOrderRepository,
    private val partialOrderRepository: PartialOrderRepository,
    private val userProfileRepository: UserProfileRepository,
    private val scmSnapshotRepository: ScmSnapshotRepository,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AnalyticsService::class.java)
    }

    /**
     * Получает список ставок для заказа, отсортированных по matching_score
     */
    @Transactional(readOnly = true)
    fun getBidsForOrder(orderId: UUID, orderType: String): List<ShipperBidDto> {
        logger.info("Getting bids for order: $orderId, type: $orderType")
        
        val bids = when (orderType.uppercase()) {
            "FREIGHT" -> bidRepository.findByFreightOrderId(orderId)
            "MASTER" -> bidRepository.findByMasterOrderId(orderId)
            "PARTIAL" -> bidRepository.findByPartialOrderId(orderId)
            else -> throw IllegalArgumentException("Unknown order type: $orderType")
        }
        
        return bids.map { bid ->
            createShipperBidDto(bid)
        }.sortedByDescending { it.matchingScore }
    }

    /**
     * Получает заказ с детализацией ставок для Shipper Dashboard
     */
    @Transactional(readOnly = true)
    fun getOrderWithBids(orderId: UUID, orderType: String): ShipperOrderDto {
        logger.info("Getting order with bids: $orderId, type: $orderType")
        
        val order = when (orderType.uppercase()) {
            "FREIGHT" -> freightOrderRepository.findById(orderId)
                ?: throw IllegalArgumentException("Freight order not found: $orderId")
            "MASTER" -> masterOrderRepository.findById(orderId)
                ?: throw IllegalArgumentException("Master order not found: $orderId")
            "PARTIAL" -> partialOrderRepository.findById(orderId)
                ?: throw IllegalArgumentException("Partial order not found: $orderId")
            else -> throw IllegalArgumentException("Unknown order type: $orderType")
        }
        
        val bids = getBidsForOrder(orderId, orderType)
        
        return when (order) {
            is FreightOrderEntity -> ShipperOrderDto(
                orderId = order.id!!,
                title = order.title,
                cargoType = order.cargoType.name,
                weight = order.weight,
                volume = order.volume,
                pickupAddress = order.pickupAddress,
                deliveryAddress = order.deliveryAddress,
                requiredDeliveryDate = order.requiredDeliveryDate,
                maxBidAmount = order.maxBidAmount,
                status = order.status.name,
                bids = bids,
                createdAt = order.createdAt!!
            )
            is MasterOrderEntity -> ShipperOrderDto(
                orderId = order.id!!,
                title = order.title,
                cargoType = order.cargoType.name,
                weight = order.totalWeight,
                volume = order.totalVolume,
                pickupAddress = order.pickupAddress,
                deliveryAddress = order.deliveryAddress,
                requiredDeliveryDate = order.requiredDeliveryDate,
                maxBidAmount = order.maxBidAmount,
                status = order.status.name,
                bids = bids,
                createdAt = order.createdAt!!
            )
            is PartialOrderEntity -> ShipperOrderDto(
                orderId = order.id!!,
                title = "Partial Order ${order.id}",
                cargoType = order.masterOrder.cargoType.name,
                weight = order.weight,
                volume = order.volume,
                pickupAddress = order.masterOrder.pickupAddress,
                deliveryAddress = order.masterOrder.deliveryAddress,
                requiredDeliveryDate = order.masterOrder.requiredDeliveryDate,
                maxBidAmount = order.masterOrder.maxBidAmount,
                status = order.status.name,
                bids = bids,
                createdAt = order.createdAt!!
            )
            else -> throw IllegalArgumentException("Unknown order type: $orderType")
        }
    }

    /**
     * Получает агрегированный прогресс заказа для трехцветного прогресс-бара
     */
    @Transactional(readOnly = true)
    fun getOrderProgress(orderId: UUID, orderType: String): OrderProgressDto {
        logger.info("Getting progress for order: $orderId, type: $orderType")
        
        return when (orderType.uppercase()) {
            "MASTER" -> getMasterOrderProgress(orderId)
            "FREIGHT" -> getFreightOrderProgress(orderId)
            "PARTIAL" -> getPartialOrderProgress(orderId)
            else -> throw IllegalArgumentException("Unknown order type: $orderType")
        }
    }

    /**
     * Получает прогресс мастер-заказа с агрегацией частичных заказов
     */
    private fun getMasterOrderProgress(masterOrderId: UUID): OrderProgressDto {
        val masterOrder = masterOrderRepository.findById(masterOrderId)
            ?: throw IllegalArgumentException("Master order not found: $masterOrderId")
        
        val partialOrders = partialOrderRepository.findByMasterOrder(masterOrder)
        
        val totalWeight = masterOrder.totalWeight
        val totalVolume = masterOrder.totalVolume
        
        var allocatedWeight = BigDecimal.ZERO
        var allocatedVolume = BigDecimal.ZERO
        var pendingWeight = BigDecimal.ZERO
        var pendingVolume = BigDecimal.ZERO
        
        var blueWeight = BigDecimal.ZERO   // Completed
        var blueVolume = BigDecimal.ZERO
        var yellowWeight = BigDecimal.ZERO // In Progress
        var yellowVolume = BigDecimal.ZERO
        var greyWeight = BigDecimal.ZERO   // Pending
        var greyVolume = BigDecimal.ZERO
        
        partialOrders.forEach { partialOrder ->
            val weight = partialOrder.weight
            val volume = partialOrder.volume
            
            when (partialOrder.status) {
                PartialOrderStatus.COMPLETED -> {
                    blueWeight += weight
                    blueVolume += volume
                    allocatedWeight += weight
                    allocatedVolume += volume
                }
                PartialOrderStatus.IN_PROGRESS -> {
                    yellowWeight += weight
                    yellowVolume += volume
                    allocatedWeight += weight
                    allocatedVolume += volume
                }
                PartialOrderStatus.AVAILABLE, PartialOrderStatus.BIDDING -> {
                    greyWeight += weight
                    greyVolume += volume
                    pendingWeight += weight
                    pendingVolume += volume
                }
                else -> {
                    pendingWeight += weight
                    pendingVolume += volume
                }
            }
        }
        
        val progressPercentage = if (totalWeight > BigDecimal.ZERO) {
            (allocatedWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val blueProgress = if (totalWeight > BigDecimal.ZERO) {
            (blueWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val yellowProgress = if (totalWeight > BigDecimal.ZERO) {
            (yellowWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val greyProgress = if (totalWeight > BigDecimal.ZERO) {
            (greyWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        return OrderProgressDto(
            orderId = masterOrder.id!!,
            orderType = "MASTER",
            totalWeight = totalWeight,
            totalVolume = totalVolume,
            allocatedWeight = allocatedWeight,
            allocatedVolume = allocatedVolume,
            pendingWeight = pendingWeight,
            pendingVolume = pendingVolume,
            progressPercentage = progressPercentage,
            blueProgress = blueProgress,
            yellowProgress = yellowProgress,
            greyProgress = greyProgress,
            status = masterOrder.status.name
        )
    }

    /**
     * Получает прогресс обычного заказа
     */
    private fun getFreightOrderProgress(freightOrderId: UUID): OrderProgressDto {
        val freightOrder = freightOrderRepository.findById(freightOrderId)
            ?: throw IllegalArgumentException("Freight order not found: $freightOrderId")
        
        val totalWeight = freightOrder.weight
        val totalVolume = freightOrder.volume
        
        // Для обычного заказа прогресс определяется по статусу
        val (allocatedWeight, allocatedVolume, blueWeight, blueVolume, yellowWeight, yellowVolume, greyWeight, greyVolume) = when (freightOrder.status) {
            OrderStatus.COMPLETED -> Triple(totalWeight, totalVolume, totalWeight, totalVolume, BigDecimal.ZERO, BigDecimal.ZERO)
            OrderStatus.IN_PROGRESS -> Triple(totalWeight, totalVolume, BigDecimal.ZERO, BigDecimal.ZERO, totalWeight, totalVolume)
            else -> Triple(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
        }
        
        val pendingWeight = totalWeight - allocatedWeight
        val pendingVolume = totalVolume - allocatedVolume
        
        val progressPercentage = if (totalWeight > BigDecimal.ZERO) {
            (allocatedWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val blueProgress = if (totalWeight > BigDecimal.ZERO) {
            (blueWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val yellowProgress = if (totalWeight > BigDecimal.ZERO) {
            (yellowWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val greyProgress = if (totalWeight > BigDecimal.ZERO) {
            (greyWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        return OrderProgressDto(
            orderId = freightOrder.id!!,
            orderType = "FREIGHT",
            totalWeight = totalWeight,
            totalVolume = totalVolume,
            allocatedWeight = allocatedWeight,
            allocatedVolume = allocatedVolume,
            pendingWeight = pendingWeight,
            pendingVolume = pendingVolume,
            progressPercentage = progressPercentage,
            blueProgress = blueProgress,
            yellowProgress = yellowProgress,
            greyProgress = greyProgress,
            status = freightOrder.status.name
        )
    }

    /**
     * Получает прогресс частичного заказа
     */
    private fun getPartialOrderProgress(partialOrderId: UUID): OrderProgressDto {
        val partialOrder = partialOrderRepository.findById(partialOrderId)
            ?: throw IllegalArgumentException("Partial order not found: $partialOrderId")
        
        val totalWeight = partialOrder.weight
        val totalVolume = partialOrder.volume
        
        val (allocatedWeight, allocatedVolume, blueWeight, blueVolume, yellowWeight, yellowVolume, greyWeight, greyVolume) = when (partialOrder.status) {
            PartialOrderStatus.COMPLETED -> Triple(totalWeight, totalVolume, totalWeight, totalVolume, BigDecimal.ZERO, BigDecimal.ZERO)
            PartialOrderStatus.IN_PROGRESS -> Triple(totalWeight, totalVolume, BigDecimal.ZERO, BigDecimal.ZERO, totalWeight, totalVolume)
            else -> Triple(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
        }
        
        val pendingWeight = totalWeight - allocatedWeight
        val pendingVolume = totalVolume - allocatedVolume
        
        val progressPercentage = if (totalWeight > BigDecimal.ZERO) {
            (allocatedWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val blueProgress = if (totalWeight > BigDecimal.ZERO) {
            (blueWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val yellowProgress = if (totalWeight > BigDecimal.ZERO) {
            (yellowWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        val greyProgress = if (totalWeight > BigDecimal.ZERO) {
            (greyWeight / totalWeight * BigDecimal("100")).toDouble()
        } else 0.0
        
        return OrderProgressDto(
            orderId = partialOrder.id!!,
            orderType = "PARTIAL",
            totalWeight = totalWeight,
            totalVolume = totalVolume,
            allocatedWeight = allocatedWeight,
            allocatedVolume = allocatedVolume,
            pendingWeight = pendingWeight,
            pendingVolume = pendingVolume,
            progressPercentage = progressPercentage,
            blueProgress = blueProgress,
            yellowProgress = yellowProgress,
            greyProgress = greyProgress,
            status = partialOrder.status.name
        )
    }

    /**
     * Создает DTO ставки для Shipper Dashboard
     */
    private fun createShipperBidDto(bid: BidEntity): ShipperBidDto {
        val carrierProfile = userProfileRepository.findById(bid.carrierId)
        val scmSnapshot = scmSnapshotRepository.findByBidId(bid.id!!)
        
        val carrierName = carrierProfile?.companyName ?: "Unknown Carrier"
        val carrierRating = carrierProfile?.rating ?: 0.0
        val totalOrders = carrierProfile?.totalOrders ?: 0
        val completedOrders = carrierProfile?.completedOrders ?: 0
        val onTimeDeliveryRate = if (totalOrders > 0) {
            (completedOrders.toDouble() / totalOrders.toDouble() * 100.0)
        } else 0.0
        
        val (passesDangerousGoodsRules, ruleViolations) = checkDangerousGoodsRules(bid, scmSnapshot)
        
        return ShipperBidDto(
            bidId = bid.id!!,
            carrierId = bid.carrierId,
            carrierName = carrierName,
            amount = bid.amount,
            proposedDeliveryDate = bid.proposedDeliveryDate,
            notes = bid.notes,
            matchingScore = bid.matchingScore,
            scoreBreakdown = bid.scoreBreakdown,
            status = bid.status.name,
            createdAt = bid.createdAt!!,
            passesDangerousGoodsRules = passesDangerousGoodsRules,
            ruleViolations = ruleViolations,
            carrierRating = carrierRating,
            totalOrders = totalOrders,
            completedOrders = completedOrders,
            onTimeDeliveryRate = onTimeDeliveryRate
        )
    }

    /**
     * Проверяет соответствие правилам Dangerous Goods
     */
    private fun checkDangerousGoodsRules(bid: BidEntity, scmSnapshot: ScmSnapshotEntity?): Pair<Boolean, List<String>> {
        val violations = mutableListOf<String>()
        var passes = true
        
        // Получаем тип груза
        val cargoType = when {
            bid.freightOrder != null -> bid.freightOrder.cargoType
            bid.masterOrder != null -> bid.masterOrder.cargoType
            bid.partialOrder != null -> bid.partialOrder.masterOrder.cargoType
            else -> null
        }
        
        // Проверяем, является ли груз опасным
        if (cargoType == CargoType.DANGEROUS) {
            // Проверяем соответствие требованиям SCM
            if (scmSnapshot == null) {
                violations.add("No SCM snapshot available")
                passes = false
            } else {
                // Проверяем уровень безопасности
                when (scmSnapshot.securityClearance) {
                    SecurityLevel.NONE -> {
                        violations.add("Insufficient security clearance for dangerous goods")
                        passes = false
                    }
                    SecurityLevel.CONFIDENTIAL -> {
                        // Допустимо для некоторых типов опасных грузов
                    }
                    SecurityLevel.SECRET, SecurityLevel.TOP_SECRET -> {
                        // Полностью соответствует
                    }
                    else -> {
                        violations.add("Unknown security level")
                        passes = false
                    }
                }
                
                // Проверяем уровень доверия
                if (scmSnapshot.riskScore < 70.0) {
                    violations.add("Trust score too low for dangerous goods")
                    passes = false
                }
                
                // Проверяем соответствие
                if (scmSnapshot.complianceStatus != ComplianceStatus.COMPLIANT) {
                    violations.add("Not compliant with dangerous goods regulations")
                    passes = false
                }
            }
        }
        
        return Pair(passes, violations)
    }
}