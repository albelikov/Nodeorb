package com.freight.marketplace.service.insight.provider

import java.math.BigDecimal

/**
 * Интерфейс для провайдеров данных о ценах на топливо
 * Позволяет системе получать актуальные рыночные данные из различных источников
 */
interface InsightPriceProvider {

    /**
     * Получает текущий коэффициент изменения цены на топливо
     * @return Коэффициент изменения цены (например, 1.05 для 5% надбавки)
     */
    fun fetchCurrentRate(): BigDecimal

    /**
     * Возвращает имя провайдера
     * @return Уникальное имя провайдера
     */
    fun getProviderName(): String

    /**
     * Проверяет доступность провайдера
     * @return true, если провайдер доступен для запросов
     */
    fun isAvailable(): Boolean

    /**
     * Возвращает тип провайдера
     * @return Тип провайдера (OIL_BULLETIN, REGIONAL_JSON, MOCK)
     */
    fun getProviderType(): ProviderType

    /**
     * Возвращает вес провайдера в расчетах
     * @return Вес провайдера (0.0 - 1.0)
     */
    fun getWeight(): Double
}

/**
 * Типы провайдеров данных
 */
enum class ProviderType {
    OIL_BULLETIN,    // Внешний API (Weekly Oil Bulletin)
    REGIONAL_JSON,   // Локальные JSON-данные
    MOCK             // Тестовые данные
}