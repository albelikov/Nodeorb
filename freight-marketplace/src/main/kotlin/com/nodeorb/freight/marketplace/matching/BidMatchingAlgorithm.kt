package com.nodeorb.freight.marketplace.matching

import com.nodeorb.freight.marketplace.entity.BidEntity
import com.nodeorb.freight.marketplace.entity.UserProfileEntity
import com.nodeorb.freight.marketplace.FreightMarketplaceProperties
import com.nodeorb.freight.marketplace.repository.UserProfileRepository
import com.nodeorb.freight.marketplace.entity.FreightOrderEntity
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Component
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import java.util.UUID

@Component
class BidMatchingAlgorithm(
    private val userProfileRepository: UserProfileRepository,
    private val properties: FreightMarketplaceProperties
) {
    
    fun calculateBidScore(bid: BidEntity, currentLocation: Point? = null): Double {
        val priceScore = calculatePriceScore(bid)
        val reputationScore = calculateReputationScore(bid.carrierId)
        val proximityScore = calculateProximityScore(bid, currentLocation)
        val deliveryTimeScore = calculateDeliveryTimeScore(bid)
        
        return (priceScore * properties.matching.priceWeight +
                reputationScore * properties.matching.reputationWeight +
                proximityScore * properties.matching.proximityWeight +
                deliveryTimeScore * 0.1).coerceIn(0.0, 1.0)
    }
    
    fun rankBids(bids: List<BidEntity>, currentLocation: Point? = null): List<BidEntity> {
        return bids.map { bid ->
            bid.score = calculateBidScore(bid, currentLocation)
            bid
        }.sortedByDescending { it.score }
    }
    
    fun autoAwardOrder(bids: List<BidEntity>): BidEntity? {
        if (bids.isEmpty()) return null
        
        val scoredBids = rankBids(bids)
        val highestBid = scoredBids.first()
        
        return if (highestBid.score!! >= properties.auction.autoAwardThreshold) {
            highestBid
        } else {
            null
        }
    }
    
    private fun calculatePriceScore(bid: BidEntity): Double {
        val order = bid.freightOrder
        val maxBidAmount = order.maxBidAmount.toDouble()
        val requestedAmount = bid.amount.toDouble()
        
        // Лучшая цена = ближе к максимальной, но не превышающая её
        val bidRatio = requestedAmount / maxBidAmount
        return 1.0 - bidRatio
    }
    
    private fun calculateReputationScore(carrierId: UUID): Double {
        val profile = userProfileRepository.findById(carrierId).orElse(null)
        return when {
            profile == null -> 0.5
            profile.totalOrders == 0 -> 0.5
            else -> {
                val rating = profile.rating.coerceIn(1.0, 5.0)
                val completionRate = profile.completedOrders.toDouble() / profile.totalOrders
                
                (rating / 5.0 * 0.6 + completionRate * 0.4).coerceIn(0.0, 1.0)
            }
        }
    }
    
    private fun calculateProximityScore(bid: BidEntity, currentLocation: Point?): Double {
        if (currentLocation == null) return 0.5
        
        val order = bid.freightOrder
        
        val pickupDistance = calculateDistance(currentLocation, order.pickupLocation)
        val deliveryDistance = calculateDistance(currentLocation, order.deliveryLocation)
        
        // Нормализуем расстояния относительно разумных значений
        val maxPickupDistance = 100.0 // км
        val maxDeliveryDistance = 200.0 // км
        
        val pickupScore = max(0.0, 1.0 - pickupDistance / maxPickupDistance)
        val deliveryScore = max(0.0, 1.0 - deliveryDistance / maxDeliveryDistance)
        
        return (pickupScore + deliveryScore) / 2.0
    }
    
    private fun calculateDeliveryTimeScore(bid: BidEntity): Double {
        val order = bid.freightOrder
        val requiredDate = order.requiredDeliveryDate
        val proposedDate = bid.proposedDeliveryDate
        
        val daysDifference = abs(java.time.Duration.between(requiredDate, proposedDate).toDays())
        
        // Нормализуем разницу в днях
        return max(0.0, 1.0 - daysDifference / 7.0)
    }
    
    private fun calculateDistance(point1: Point, point2: Point): Double {
        // Простая евклидова дистанция для примера
        // В реальности использовать гео-библиотеки для точных расстояний
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return Math.sqrt(dx * dx + dy * dy) * 111.0 // Приблизительное преобразование в км
    }
    
    fun optimizeRoute(bids: List<BidEntity>): List<BidEntity> {
        // Простой алгоритм оптимизации маршрута
        return bids.sortedBy { bid ->
            val order = bid.freightOrder
            val distance = calculateDistance(order.pickupLocation, order.deliveryLocation)
            distance
        }
    }
    
    companion object {
        const val EARTH_RADIUS_KM = 6371.0
    }
}