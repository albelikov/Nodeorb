package com.nodeorb.freight.marketplace.dto

import java.math.BigDecimal

/**
 * Детализация расчета Match Score для прозрачности и объяснимости
 * Показывает, как был рассчитан итоговый балл по каждой компоненте
 */
data class ScoreBreakdown(
    /**
     * Цена (40%): Чем ближе к медиане рынка или таргету шиппера, тем выше балл
     */
    val priceScore: Double,
    val priceDetails: PriceScoreDetails,
    
    /**
     * Trust Score из SCM (30%): Прямая трансляция рейтинга надежности из scm_snapshot
     */
    val trustScore: Double,
    val trustDetails: TrustScoreDetails,
    
    /**
     * SLA History (20%): Процент вовремя завершенных заказов (из профиля перевозчика)
     */
    val slaScore: Double,
    val slaDetails: SLAScoreDetails,
    
    /**
     * Гео-оптимальность (10%): Насколько машина близко к точке погрузки
     */
    val geoScore: Double,
    val geoDetails: GeoScoreDetails,
    
    /**
     * Итоговый Match Score (0-100)
     */
    val totalScore: Double
)

/**
 * Детали расчета ценового балла
 */
data class PriceScoreDetails(
    val bidAmount: BigDecimal,
    val targetAmount: BigDecimal?,
    val marketMedian: BigDecimal?,
    val priceDeviationPercent: Double,
    val maxDeviationForFullScore: Double = 10.0 // отклонение в 10% дает полный балл
)

/**
 * Детали расчета доверительного балла из SCM
 */
data class TrustScoreDetails(
    val scmTrustScore: Double, // 0-100 из SCM
    val complianceStatus: String,
    val securityClearance: String,
    val riskScore: Double
)

/**
 * Детали расчета SLA балла
 */
data class SLAScoreDetails(
    val onTimeDeliveryRate: Double, // 0-100
    val totalOrders: Int,
    val onTimeOrders: Int,
    val avgDeliveryTime: Int? // в часах
)

/**
 * Детали расчета географического балла
 */
data class GeoScoreDetails(
    val carrierLocation: String?, // координаты или адрес
    val pickupLocation: String,
    val distanceKm: Double?,
    val maxDistanceForFullScore: Double = 50.0 // 50 км и меньше - полный балл
)