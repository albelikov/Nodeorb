package com.nodeorb.freight.marketplace

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties("freight.marketplace")
@Validated
data class FreightMarketplaceProperties(
    val auction: AuctionProperties = AuctionProperties(),
    val matching: MatchingProperties = MatchingProperties(),
    val notification: NotificationProperties = NotificationProperties()
)

@Validated
data class AuctionProperties(
    val bidExpirationHours: Long = 24,
    val maxBidsPerOrder: Int = 10,
    val autoAwardThreshold: Double = 0.8
)

@Validated
data class MatchingProperties(
    val algorithm: String = "weighted",
    val priceWeight: Double = 0.4,
    val reputationWeight: Double = 0.3,
    val proximityWeight: Double = 0.3,
    val minMatchScore: Double = 0.6
)

@Validated
data class NotificationProperties(
    val emailEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val webSocketEnabled: Boolean = true
)