package com.freight.marketplace.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.freight.marketplace.dto.ScoreBreakdown
import com.freight.marketplace.dto.PriceScoreDetails
import com.freight.marketplace.dto.TrustScoreDetails
import com.freight.marketplace.dto.SLAScoreDetails
import com.freight.marketplace.dto.GeoScoreDetails
import com.freight.marketplace.entity.BidEntity
import com.freight.marketplace.entity.ScmSnapshotEntity
import com.freight.marketplace.entity.UserProfileEntity
import com.freight.marketplace.repository.BidRepository
import com.freight.marketplace.repository.ScmSnapshotRepository
import com.freight.marketplace.repository.UserProfileRepository
import com.freight.marketplace.service.insight.MarketInsightService
import com.freight.marketplace.service.insight.RouteInfo
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min

/**
 * Сервис оценки и ранжирования ставок
 * Реализует формулу расчета Match Score с Explainable AI
 */
@Service
class ScoringService(
    private val bidRepository: BidRepository,
    private val scmSnapshotRepository: ScmSnapshotRepository,
    private val userProfileRepository: UserProfileRepository,
    private val objectMapper: ObjectMapper,
    private val marketOracleService: MarketInsightService
) {

    companion object {
        // Веса компонент в формуле (сумма = 1.0)
        private const val WEIGHT_PRICE = 0.40
        private const val WEIGHT_TRUST = 0.30
        private const val WEIGHT_SLA = 0.20
        private const val WEIGHT_GEO = 0.10
        
        // Параметры для расчета баллов
        private const val MAX_PRICE_DEVIATION_FOR_FULL_SCORE = 10.0 // 10% отклонения = полный балл
        private const val MAX_GEO_DISTANCE_FOR_FULL_SCORE = 50.0 // 50 км = полный балл
        private const val SLA_MIN_ORDERS_FOR_SCORING = 5 // минимум заказов для учета SLA
    }

    /**
     * Асинхронно вычисляет и обновляет скоринг для ставки
     */
    @Transactional
    fun calculateAndSaveScore(bidId: java.util.UUID) {
        val bid = bidRepository.findById(bidId)
            ?: throw IllegalArgumentException("Bid not found: $bidId")
        
        val scoreBreakdown = calculateScore(bid)
        val totalScore = scoreBreakdown.totalScore
        
        // Обновляем ставку
        bid.matchingScore = totalScore
        bid.scoreBreakdown = objectMapper.writeValueAsString(scoreBreakdown)
        bidRepository.save(bid)
    }

    /**
     * Рассчитывает Match Score для ставки
     */
    fun calculateScore(bid: BidEntity): ScoreBreakdown {
        // 1. Цена (40%)
        val priceScore = calculatePriceScore(bid)
        
        // 2. Trust Score из SCM (30%)
        val trustScore = calculateTrustScore(bid)
        
        // 3. SLA History (20%)
        val slaScore = calculateSLAScore(bid)
        
        // 4. Гео-оптимальность (10%)
        val geoScore = calculateGeoScore(bid)
        
        // Итоговый балл
        val totalScore = (priceScore * WEIGHT_PRICE) + 
                        (trustScore * WEIGHT_TRUST) + 
                        (slaScore * WEIGHT_SLA) + 
                        (geoScore * WEIGHT_GEO)
        
        return ScoreBreakdown(
            priceScore = priceScore,
            priceDetails = priceScore,
            trustScore = trustScore,
            trustDetails = trustScore,
            slaScore = slaScore,
            slaDetails = slaScore,
            geoScore = geoScore,
            geoDetails = geoScore,
            totalScore = totalScore
        )
    }

    /**
     * Цена (40%): Чем ближе к медиане рынка или таргету шиппера, тем выше балл
     * Включает проверку рыночной цены через MarketOracleService
     */
    private fun calculatePriceScore(bid: BidEntity): Double {
        val targetAmount = bid.freightOrder?.maxBidAmount
        val marketMedian = calculateMarketMedian(bid.freightOrder?.id)
        
        val priceDeviationPercent = when {
            targetAmount != null -> {
                val deviation = abs(bid.amount.toDouble() - targetAmount.toDouble()) / targetAmount.toDouble() * 100
                deviation
            }
            marketMedian != null -> {
                val deviation = abs(bid.amount.toDouble() - marketMedian.toDouble()) / marketMedian.toDouble() * 100
                deviation
            }
            else -> 0.0
        }
        
        // Снижение балла за отклонение от оптимальной цены
        var score = if (priceDeviationPercent <= MAX_PRICE_DEVIATION_FOR_FULL_SCORE) {
            100.0
        } else {
            // Экспоненциальное снижение балла за превышение порога
            val excess = priceDeviationPercent - MAX_PRICE_DEVIATION_FOR_FULL_SCORE
            val penalty = min(excess * 2.0, 50.0) // максимум 50% штраф
            maxOf(0.0, 100.0 - penalty)
        }
        
        // Проверка рыночной цены через MarketOracleService
        if (bid.freightOrder != null) {
            val route = RouteInfo(
                pickupLocation = bid.freightOrder.pickupLocation,
                deliveryLocation = bid.freightOrder.deliveryLocation
            )
            
            val validation = marketOracleService.validateMarketPrice(bid.amount, route)
            
            // Если цена ниже рыночной более чем на 15%, снижаем балл как "High Risk of Default"
            if (validation.isHighRisk && validation.priceDifferencePercent < 0) {
                val riskPenalty = min(abs(validation.priceDifferencePercent) * 2.0, 60.0) // максимум 60% штраф
                score = maxOf(0.0, score - riskPenalty)
            }
        }
        
        return score
    }

    /**
     * Trust Score из SCM (30%): Прямая трансляция рейтинга надежности из scm_snapshot
     */
    private fun calculateTrustScore(bid: BidEntity): Double {
        val snapshot = scmSnapshotRepository.findByBidId(bid.id!!)
            ?: return 0.0 // Нет данных SCM - минимальный балл
        
        // Базовый балл из SCM
        var trustScore = snapshot.riskScore
        
        // Понижение за нарушения соответствия
        when (snapshot.complianceStatus) {
            com.freight.marketplace.entity.ComplianceStatus.NON_COMPLIANT -> trustScore *= 0.5
            com.freight.marketplace.entity.ComplianceStatus.EXPIRED -> trustScore *= 0.7
            else -> {} // COMPLIANT и PENDING не влияют
        }
        
        // Понижение за низкий уровень безопасности
        when (snapshot.securityClearance) {
            com.freight.marketplace.entity.SecurityLevel.NONE -> trustScore *= 0.8
            com.freight.marketplace.entity.SecurityLevel.CONFIDENTIAL -> trustScore *= 0.9
            else -> {} // Высокие уровни не влияют
        }
        
        return maxOf(0.0, minOf(100.0, trustScore))
    }

    /**
     * SLA History (20%): Процент вовремя завершенных заказов (из профиля перевозчика)
     */
    private fun calculateSLAScore(bid: BidEntity): Double {
        val profile = userProfileRepository.findById(bid.carrierId)
            ?: return 50.0 // Нет профиля - средний балл
        
        if (profile.totalOrders < SLA_MIN_ORDERS_FOR_SCORING) {
            return 50.0 // Недостаточно данных - средний балл
        }
        
        val onTimeRate = if (profile.totalOrders > 0) {
            (profile.completedOrders.toDouble() / profile.totalOrders.toDouble()) * 100
        } else 0.0
        
        // Бонус за высокую надежность
        val bonus = if (onTimeRate >= 95.0) 10.0 else 0.0
        
        return maxOf(0.0, minOf(100.0, onTimeRate + bonus))
    }

    /**
     * Гео-оптимальность (10%): Насколько машина близко к точке погрузки
     */
    private fun calculateGeoScore(bid: BidEntity): Double {
        val pickupLocation = bid.freightOrder?.pickupLocation
            ?: bid.masterOrder?.pickupLocation
            ?: return 50.0 // Нет данных - средний балл
        
        // В реальной системе здесь будет вызов геосервиса для определения
        // текущего местоположения перевозчика и расчета расстояния
        // Пока используем заглушку - предполагаем, что данные есть в профиле
        val carrierLocation = getCarrierCurrentLocation(bid.carrierId)
            ?: return 50.0
        
        val distanceKm = calculateDistance(pickupLocation, carrierLocation)
        
        return if (distanceKm <= MAX_GEO_DISTANCE_FOR_FULL_SCORE) {
            100.0
        } else {
            // Линейное снижение балла с расстоянием
            val penalty = min((distanceKm - MAX_GEO_DISTANCE_FOR_FULL_SCORE) * 0.5, 50.0)
            maxOf(0.0, 100.0 - penalty)
        }
    }

    /**
     * Рассчитывает медиану цен по рынку для аналогичных заказов
     */
    private fun calculateMarketMedian(orderId: java.util.UUID?): BigDecimal? {
        if (orderId == null) return null
        
        val similarBids = bidRepository.findSimilarBids(orderId)
        if (similarBids.isEmpty()) return null
        
        val sortedAmounts = similarBids.map { it.amount }.sorted()
        val size = sortedAmounts.size
        
        return if (size % 2 == 0) {
            // Среднее двух центральных значений
            (sortedAmounts[size / 2 - 1] + sortedAmounts[size / 2]).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP)
        } else {
            sortedAmounts[size / 2]
        }
    }

    /**
     * Получает текущее местоположение перевозчика
     * В реальной системе это будет интеграция с GPS-трекером или мобильным приложением
     */
    private fun getCarrierCurrentLocation(carrierId: java.util.UUID): Point? {
        // TODO: Интеграция с GPS-трекером или мобильным приложением
        // Пока возвращаем null - в реальной системе здесь будет вызов сервиса геолокации
        return null
    }

    /**
     * Рассчитывает расстояние между двумя точками в километрах
     * Использует формулу гаверсинуса для расчета расстояния на сфере
     */
    private fun calculateDistance(point1: Point, point2: Point): Double {
        val lat1 = point1.y
        val lon1 = point1.x
        val lat2 = point2.y
        val lon2 = point2.x
        
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}