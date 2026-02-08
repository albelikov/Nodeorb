package com.freight.marketplace.service.insight

import com.freight.marketplace.entity.FreightOrderEntity
import com.freight.marketplace.entity.PartialOrderEntity
import com.freight.marketplace.repository.FreightOrderRepository
import com.freight.marketplace.repository.PartialOrderRepository
import org.locationtech.jts.geom.Point
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.math.abs

/**
 * Сервис рыночной аналитики для корректировки стоимости и Match Score
 * Интегрируется с внешними API для получения актуальных рыночных данных
 */
@Service
class MarketInsightService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val freightOrderRepository: FreightOrderRepository,
    private val partialOrderRepository: PartialOrderRepository,
    private val insightProviderRegistry: InsightProviderRegistry
) {

    companion object {
        private val logger = LoggerFactory.getLogger(MarketInsightService::class.java)
        private const val FUEL_SURCHARGE_KEY = "market:fuel:surcharge"
        private const val ROUTE_PRICES_KEY_PREFIX = "market:route:prices:"
        private const val FUEL_UPDATE_INTERVAL_HOURS = 6
        private const val PRICE_CACHE_TTL_MINUTES = 30
        private const val DEFAULT_FUEL_SURCHARGE = 1.05 // 5% надбавка
        private const val RISK_THRESHOLD_PERCENT = 15.0
    }

    /**
     * Получает коэффициент изменения цены на топливо
     * @return Коэффициент изменения цены (например, 1.05 для 5% надбавки)
     */
    fun getFuelSurcharge(): Double {
        val cachedSurcharge = redisTemplate.opsForValue().get(FUEL_SURCHARGE_KEY)
        if (cachedSurcharge != null) {
            logger.info("Retrieved cached fuel surcharge: $cachedSurcharge")
            return cachedSurcharge as Double
        }
        
        // Используем провайдеров для получения актуальной цены на топливо
        val fuelSurcharge = fetchFuelSurchargeFromProviders()
        
        // Кэшируем результат
        redisTemplate.opsForValue().set(
            FUEL_SURCHARGE_KEY, 
            fuelSurcharge, 
            Duration.ofHours(FUEL_UPDATE_INTERVAL_HOURS)
        )
        
        logger.info("Fetched and cached fuel surcharge: $fuelSurcharge")
        return fuelSurcharge
    }

    /**
     * Получает коэффициент изменения цены на топливо через провайдеров
     */
    private fun fetchFuelSurchargeFromProviders(): Double {
        try {
            // Проверяем, включен ли режим консенсуса
            if (insightProviderRegistry.isConsensusModeEnabled()) {
                return getConsensusFuelSurcharge()
            }
            
            // Используем основной провайдер
            val primaryProvider = insightProviderRegistry.getPrimaryProvider()
            return if (primaryProvider != null && primaryProvider.isAvailable()) {
                val rate = primaryProvider.fetchCurrentRate()
                rate.toDouble()
            } else {
                // Резервный вариант - Mock провайдер
                logger.warn("Primary provider unavailable, using fallback")
                getFallbackFuelSurcharge()
            }
        } catch (e: Exception) {
            logger.error("Error fetching fuel surcharge from providers, using fallback", e)
            return getFallbackFuelSurcharge()
        }
    }

    /**
     * Получает консенсусную цену на топливо (усреднение по всем активным провайдерам)
     */
    private fun getConsensusFuelSurcharge(): Double {
        val consensusProviders = insightProviderRegistry.getConsensusProviders()
        
        if (consensusProviders.isEmpty()) {
            logger.warn("No consensus providers available, using fallback")
            return getFallbackFuelSurcharge()
        }

        val availableProviders = consensusProviders.filter { it.isAvailable() }
        
        if (availableProviders.isEmpty()) {
            logger.warn("No available providers in consensus mode, using fallback")
            return getFallbackFuelSurcharge()
        }

        // Взвешенное усреднение по весам провайдеров
        var totalWeightedRate = BigDecimal.ZERO
        var totalWeight = 0.0

        availableProviders.forEach { provider ->
            try {
                val rate = provider.fetchCurrentRate()
                val weight = provider.getWeight()
                
                totalWeightedRate += rate * BigDecimal(weight)
                totalWeight += weight
            } catch (e: Exception) {
                logger.warn("Provider ${provider.getProviderName()} failed, skipping", e)
            }
        }

        return if (totalWeight > 0.0) {
            (totalWeightedRate / BigDecimal(totalWeight)).toDouble()
        } else {
            getFallbackFuelSurcharge()
        }
    }

    /**
     * Резервный метод получения коэффициента изменения цены на топливо
     */
    private fun getFallbackFuelSurcharge(): Double {
        // Эмуляция случайных колебаний цен на топливо
        val random = Random()
        val baseSurcharge = DEFAULT_FUEL_SURCHARGE
        val fluctuation = random.nextDouble() * 0.04 - 0.02 // ±2% колебания
        val surcharge = baseSurcharge + fluctuation
        
        return surcharge.coerceIn(0.95, 1.15) // Ограничиваем диапазон 95% - 115%
    }

    /**
     * Валидирует рыночную цену заказа
     * @param orderPrice Цена заказа
     * @param route Маршрут (точки погрузки и разгрузки)
     * @return Результат валидации
     */
    fun validateMarketPrice(orderPrice: BigDecimal, route: RouteInfo): MarketPriceValidation {
        val routeKey = generateRouteKey(route)
        val cachedPrices = getRoutePricesFromCache(routeKey)
        
        val medianPrice = if (cachedPrices.isNotEmpty()) {
            calculateMedian(cachedPrices)
        } else {
            // Если нет кэшированных данных, получаем из базы
            val dbPrices = getRoutePricesFromDatabase(route)
            cacheRoutePrices(routeKey, dbPrices)
            calculateMedian(dbPrices)
        }
        
        val priceDifferencePercent = calculatePriceDifference(orderPrice.toDouble(), medianPrice)
        val isHighRisk = abs(priceDifferencePercent) > RISK_THRESHOLD_PERCENT
        
        return MarketPriceValidation(
            orderPrice = orderPrice.toDouble(),
            medianPrice = medianPrice,
            priceDifferencePercent = priceDifferencePercent,
            isHighRisk = isHighRisk,
            riskReason = if (isHighRisk) "Price deviation exceeds ${RISK_THRESHOLD_PERCENT}%" else null,
            timestamp = LocalDateTime.now()
        )
    }

    /**
     * Получает цены на маршрут из кэша Redis
     */
    private fun getRoutePricesFromCache(routeKey: String): List<Double> {
        val cachedPrices = redisTemplate.opsForList().range(routeKey, 0, -1)
        return cachedPrices?.map { it as Double } ?: emptyList()
    }

    /**
     * Получает цены на маршрут из базы данных
     */
    private fun getRoutePricesFromDatabase(route: RouteInfo): List<Double> {
        val similarOrders = findSimilarOrders(route)
        return similarOrders.map { it.maxBidAmount.toDouble() }
    }

    /**
     * Находит похожие заказы по маршруту
     */
    private fun findSimilarOrders(route: RouteInfo): List<FreightOrderEntity> {
        // В реальной системе здесь будет более сложный запрос с географическим анализом
        // Пока используем упрощенный подход
        return freightOrderRepository.findAll().filter { order ->
            isSimilarRoute(order.pickupLocation, order.deliveryLocation, route)
        }
    }

    /**
     * Проверяет схожесть маршрутов
     */
    private fun isSimilarRoute(orderPickup: Point, orderDelivery: Point, route: RouteInfo): Boolean {
        // Упрощенная проверка - расстояние между точками должно быть меньше 50 км
        val pickupDistance = calculateDistance(orderPickup, route.pickupLocation)
        val deliveryDistance = calculateDistance(orderDelivery, route.deliveryLocation)
        
        return pickupDistance < 50.0 && deliveryDistance < 50.0
    }

    /**
     * Кэширует цены на маршрут в Redis
     */
    private fun cacheRoutePrices(routeKey: String, prices: List<Double>) {
        redisTemplate.opsForList().leftPushAll(routeKey, prices)
        redisTemplate.expire(routeKey, Duration.ofMinutes(PRICE_CACHE_TTL_MINUTES))
        logger.info("Cached ${prices.size} prices for route: $routeKey")
    }

    /**
     * Рассчитывает медиану цен
     */
    private fun calculateMedian(prices: List<Double>): Double {
        if (prices.isEmpty()) return 0.0
        
        val sortedPrices = prices.sorted()
        val size = sortedPrices.size
        
        return if (size % 2 == 0) {
            (sortedPrices[size / 2 - 1] + sortedPrices[size / 2]) / 2.0
        } else {
            sortedPrices[size / 2]
        }
    }

    /**
     * Рассчитывает разницу в ценах в процентах
     */
    private fun calculatePriceDifference(orderPrice: Double, medianPrice: Double): Double {
        if (medianPrice == 0.0) return 0.0
        return ((orderPrice - medianPrice) / medianPrice) * 100
    }

    /**
     * Генерирует ключ для кэширования маршрута
     */
    private fun generateRouteKey(route: RouteInfo): String {
        val pickupHash = route.pickupLocation.x.hashCode() + route.pickupLocation.y.hashCode()
        val deliveryHash = route.deliveryLocation.x.hashCode() + route.deliveryLocation.y.hashCode()
        return "${ROUTE_PRICES_KEY_PREFIX}${pickupHash}_${deliveryHash}"
    }

    /**
     * Рассчитывает расстояние между двумя точками в километрах
     */
    private fun calculateDistance(point1: Point, point2: Point): Double {
        val lat1 = point1.y
        val lon1 = point1.x
        val lat2 = point2.y
        val lon2 = point2.x
        
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        
        val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Планировщик обновления топливных индексов
     * Выполняется каждые 6 часов
     */
    @Scheduled(fixedRate = 21600000) // 6 часов в миллисекундах
    @Transactional
    fun updateFuelIndexes() {
        logger.info("Scheduled fuel index update started")
        
        try {
            val newFuelSurcharge = fetchFuelSurchargeFromProviders()
            
            // Обновляем кэш
            redisTemplate.opsForValue().set(
                FUEL_SURCHARGE_KEY,
                newFuelSurcharge,
                Duration.ofHours(FUEL_UPDATE_INTERVAL_HOURS)
            )
            
            logger.info("Scheduled fuel index update completed: $newFuelSurcharge")
        } catch (e: Exception) {
            logger.error("Error during scheduled fuel index update", e)
        }
    }

    /**
     * Очищает кэш цен на маршруты (для тестирования)
     */
    fun clearRoutePricesCache() {
        val keys = redisTemplate.keys("${ROUTE_PRICES_KEY_PREFIX}*")
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
            logger.info("Cleared ${keys.size} route price cache entries")
        }
    }

    /**
     * Получает статистику по кэшу
     */
    fun getCacheStatistics(): Map<String, Any> {
        val fuelSurcharge = redisTemplate.opsForValue().get(FUEL_SURCHARGE_KEY)
        val routeKeys = redisTemplate.keys("${ROUTE_PRICES_KEY_PREFIX}*")
        val totalRoutes = routeKeys.size
        
        val totalPrices = routeKeys.sumOf { key ->
            redisTemplate.opsForList().size(key)?.toInt() ?: 0
        }
        
        return mapOf(
            "fuelSurcharge" to (fuelSurcharge ?: "Not cached"),
            "totalCachedRoutes" to totalRoutes,
            "totalCachedPrices" to totalPrices,
            "cacheTTLMinutes" to PRICE_CACHE_TTL_MINUTES,
            "fuelUpdateIntervalHours" to FUEL_UPDATE_INTERVAL_HOURS
        )
    }
}

/**
 * Информация о маршруте
 */
data class RouteInfo(
    val pickupLocation: Point,
    val deliveryLocation: Point
)

/**
 * Результат валидации рыночной цены
 */
data class MarketPriceValidation(
    val orderPrice: Double,
    val medianPrice: Double,
    val priceDifferencePercent: Double,
    val isHighRisk: Boolean,
    val riskReason: String?,
    val timestamp: LocalDateTime
)