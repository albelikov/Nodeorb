package com.nodeorb.scm.application.service.validation

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

@Service
class MarketOracleService(
    private val validationRepository: ValidationRepository,
    private val marketDataClient: MarketDataClient,
    private val cacheManager: CacheManager
) {
    
    @OptIn(ExperimentalStdlibApi::class)
    data class ValidationResult(
        val status: ValidationStatus,
        val riskScore: BigDecimal,
        val requiresAppeal: Boolean,
        val requiresBiometrics: Boolean = false,
        val suggestedMedian: BigDecimal,
        val confidenceInterval: Pair<BigDecimal, BigDecimal>? = null,
        val marketTrend: MarketTrend = MarketTrend.STABLE,
        val timestamp: Instant = Instant.now()
    ) {
        init {
            require(riskScore in BigDecimal.ZERO..BigDecimal.ONE) {
                "Risk score must be between 0 and 1"
            }
        }
    }
    
    enum class ValidationStatus {
        GREEN, YELLOW, RED
    }
    
    enum class MarketTrend {
        UPWARD, DOWNWARD, STABLE, VOLATILE
    }
    
    private val deviationCache = ConcurrentHashMap<String, DeviationMetrics>()
    
    @Transactional(readOnly = true)
    suspend fun validateManualInput(
        materialCost: BigDecimal,
        laborCost: BigDecimal,
        category: String,
        region: String,
        context: Map<String, Any> = emptyMap()
    ): ValidationResult = coroutineScope {
        val totalInput = materialCost + laborCost
        
        // Parallel fetching of market data
        val (medianPrice, confidence, trend) = asyncAll(
            { getHistoricalMedian(category, region) },
            { getConfidenceInterval(category, region) },
            { analyzeMarketTrend(category, region) }
        )
        
        val deviation = calculateDeviation(totalInput, medianPrice)
        val cacheKey = "$category:$region"
        val metrics = deviationCache.computeIfAbsent(cacheKey) {
            DeviationMetrics()
        }
        
        metrics.update(deviation)
        val anomalyScore = metrics.calculateAnomalyScore(deviation)
        
        return@coroutineScope when {
            anomalyScore <= BigDecimal("0.15") && deviation <= BigDecimal("0.15") -> 
                ValidationResult(
                    status = ValidationStatus.GREEN,
                    riskScore = anomalyScore.setScale(4, RoundingMode.HALF_UP),
                    requiresAppeal = false,
                    suggestedMedian = medianPrice,
                    confidenceInterval = confidence,
                    marketTrend = trend
                )
            
            anomalyScore in BigDecimal("0.151")..BigDecimal("0.40") ||
                    deviation in BigDecimal("0.151")..BigDecimal("0.40") ->
                ValidationResult(
                    status = ValidationStatus.YELLOW,
                    riskScore = anomalyScore.setScale(4, RoundingMode.HALF_UP),
                    requiresAppeal = true,
                    requiresBiometrics = context["sensitive"] as? Boolean ?: false,
                    suggestedMedian = medianPrice,
                    confidenceInterval = confidence,
                    marketTrend = trend
                )
            
            else -> ValidationResult(
                status = ValidationStatus.RED,
                riskScore = anomalyScore.setScale(4, RoundingMode.HALF_UP),
                requiresAppeal = true,
                requiresBiometrics = true,
                suggestedMedian = medianPrice,
                confidenceInterval = confidence,
                marketTrend = trend
            )
        }
    }
    
    private suspend fun getHistoricalMedian(category: String, region: String): BigDecimal {
        return cacheManager.getCached("median:$category:$region") {
            marketDataClient.getMedianPrice(category, region)
        } ?: BigDecimal.ZERO
    }
    
    private suspend fun getConfidenceInterval(
        category: String, 
        region: String
    ): Pair<BigDecimal, BigDecimal>? {
        return cacheManager.getCached("confidence:$category:$region") {
            marketDataClient.getConfidenceInterval(category, region)
        }
    }
    
    private suspend fun analyzeMarketTrend(category: String, region: String): MarketTrend {
        return cacheManager.getCached("trend:$category:$region") {
            marketDataClient.analyzeTrend(category, region)
        } ?: MarketTrend.STABLE
    }
    
    private fun calculateDeviation(value: BigDecimal, median: BigDecimal): BigDecimal {
        return if (median > BigDecimal.ZERO) {
            ((value - median).abs() / median).coerceAtMost(BigDecimal.ONE)
        } else {
            BigDecimal.ONE
        }
    }
    
    private class DeviationMetrics {
        private val history = ArrayDeque<BigDecimal>()
        private var mean: BigDecimal = BigDecimal.ZERO
        private var variance: BigDecimal = BigDecimal.ZERO
        
        fun update(newValue: BigDecimal) {
            history.addLast(newValue)
            if (history.size > 100) {
                history.removeFirst()
            }
            recalculateStatistics()
        }
        
        fun calculateAnomalyScore(value: BigDecimal): BigDecimal {
            if (history.size < 10) return value
            
            val zScore = if (variance > BigDecimal.ZERO) {
                ((value - mean).abs() / variance.sqrt()).coerceAtMost(BigDecimal("3.0"))
            } else {
                BigDecimal.ZERO
            }
            
            return (zScore / BigDecimal("3.0")).coerceIn(BigDecimal.ZERO, BigDecimal.ONE)
        }
        
        private fun recalculateStatistics() {
            mean = history.averageOf { it }
            variance = history.fold(BigDecimal.ZERO) { acc, value ->
                acc + (value - mean).pow(2)
            } / history.size.toBigDecimal()
        }
    }
}