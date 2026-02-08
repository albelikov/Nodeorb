package com.freight.marketplace.service.insight

import com.freight.marketplace.entity.OracleProviderEntity
import com.freight.marketplace.entity.ProviderType
import com.freight.marketplace.entity.CreateOracleProviderRequest
import com.freight.marketplace.entity.UpdateOracleProviderRequest
import com.freight.marketplace.entity.OracleProviderResponse
import com.freight.marketplace.repository.OracleProviderRepository
import com.freight.marketplace.service.insight.provider.InsightPriceProvider
import com.freight.marketplace.service.insight.provider.MockInsightProvider
import com.freight.marketplace.service.insight.provider.OilBulletinProvider
import com.freight.marketplace.service.insight.provider.RegionalJsonProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Регистратор провайдеров данных для NodeInsight Engine
 * Управляет списком активных провайдеров и их приоритетами
 */
@Service
class InsightProviderRegistry(
    private val oracleProviderRepository: OracleProviderRepository,
    private val mockInsightProvider: MockInsightProvider,
    private val oilBulletinProvider: OilBulletinProvider,
    private val regionalJsonProvider: RegionalJsonProvider
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InsightProviderRegistry::class.java)
    }

    /**
     * Получает список всех провайдеров
     */
    fun getAllProviders(): List<OracleProviderResponse> {
        return oracleProviderRepository.findAll().map { it.toResponse() }
    }

    /**
     * Получает провайдера по ID
     */
    fun getProvider(id: UUID): OracleProviderResponse? {
        return oracleProviderRepository.findById(id)?.toResponse()
    }

    /**
     * Создает новый провайдер
     */
    @Transactional
    fun createProvider(request: CreateOracleProviderRequest): OracleProviderResponse {
        val entity = OracleProviderEntity(
            name = request.name,
            providerType = request.providerType,
            apiUrl = request.apiUrl,
            apiKey = request.apiKey,
            dataFilePath = request.dataFilePath,
            region = request.region,
            weight = request.weight,
            isEnabled = request.isEnabled,
            priority = request.priority,
            consensusEnabled = request.consensusEnabled
        )
        
        val savedEntity = oracleProviderRepository.save(entity)
        logger.info("Created Oracle provider: ${savedEntity.name} (ID: ${savedEntity.id})")
        
        return savedEntity.toResponse()
    }

    /**
     * Обновляет провайдера
     */
    @Transactional
    fun updateProvider(id: UUID, request: UpdateOracleProviderRequest): OracleProviderResponse {
        val entity = oracleProviderRepository.findById(id)
            ?: throw IllegalArgumentException("Provider with ID '$id' not found")
        
        entity.name = request.name ?: entity.name
        entity.providerType = request.providerType ?: entity.providerType
        entity.apiUrl = request.apiUrl ?: entity.apiUrl
        entity.apiKey = request.apiKey ?: entity.apiKey
        entity.dataFilePath = request.dataFilePath ?: entity.dataFilePath
        entity.region = request.region ?: entity.region
        entity.weight = request.weight ?: entity.weight
        entity.isEnabled = request.isEnabled ?: entity.isEnabled
        entity.priority = request.priority ?: entity.priority
        entity.consensusEnabled = request.consensusEnabled ?: entity.consensusEnabled
        
        val updatedEntity = oracleProviderRepository.save(entity)
        logger.info("Updated Oracle provider: ${updatedEntity.name} (ID: ${updatedEntity.id})")
        
        return updatedEntity.toResponse()
    }

    /**
     * Удаляет провайдера
     */
    @Transactional
    fun deleteProvider(id: UUID) {
        val entity = oracleProviderRepository.findById(id)
            ?: throw IllegalArgumentException("Provider with ID '$id' not found")
        
        oracleProviderRepository.delete(entity)
        logger.info("Deleted Oracle provider: ${entity.name} (ID: $id)")
    }

    /**
     * Включает/выключает провайдера
     */
    @Transactional
    fun toggleProvider(id: UUID, enabled: Boolean): OracleProviderResponse {
        val entity = oracleProviderRepository.findById(id)
            ?: throw IllegalArgumentException("Provider with ID '$id' not found")
        
        entity.isEnabled = enabled
        val updatedEntity = oracleProviderRepository.save(entity)
        
        logger.info("Toggled Oracle provider ${updatedEntity.name} to $enabled (ID: $id)")
        return updatedEntity.toResponse()
    }

    /**
     * Устанавливает приоритет провайдера
     */
    @Transactional
    fun setProviderPriority(id: UUID, priority: Int): OracleProviderResponse {
        val entity = oracleProviderRepository.findById(id)
            ?: throw IllegalArgumentException("Provider with ID '$id' not found")
        
        entity.priority = priority
        val updatedEntity = oracleProviderRepository.save(entity)
        
        logger.info("Set priority $priority for Oracle provider ${updatedEntity.name} (ID: $id)")
        return updatedEntity.toResponse()
    }

    /**
     * Включает/выключает провайдер в консенсус
     */
    @Transactional
    fun toggleConsensus(id: UUID, consensusEnabled: Boolean): OracleProviderResponse {
        val entity = oracleProviderRepository.findById(id)
            ?: throw IllegalArgumentException("Provider with ID '$id' not found")
        
        entity.consensusEnabled = consensusEnabled
        val updatedEntity = oracleProviderRepository.save(entity)
        
        logger.info("Toggled consensus for Oracle provider ${updatedEntity.name} to $consensusEnabled (ID: $id)")
        return updatedEntity.toResponse()
    }

    /**
     * Получает активные провайдеры, отсортированные по приоритету
     */
    fun getActiveProviders(): List<InsightPriceProvider> {
        val activeEntities = oracleProviderRepository.findByIsEnabledTrueOrderByPriorityAsc()
        
        return activeEntities.mapNotNull { entity ->
            when (entity.providerType) {
                ProviderType.OIL_BULLETIN -> oilBulletinProvider
                ProviderType.REGIONAL_JSON -> regionalJsonProvider
                ProviderType.MOCK -> mockInsightProvider
            }
        }
    }

    /**
     * Получает основной провайдер (с наивысшим приоритетом)
     */
    fun getPrimaryProvider(): InsightPriceProvider? {
        val primaryEntity = oracleProviderRepository.findFirstByIsEnabledTrueOrderByPriorityAsc()
        return primaryEntity?.let { entity ->
            when (entity.providerType) {
                ProviderType.OIL_BULLETIN -> oilBulletinProvider
                ProviderType.REGIONAL_JSON -> regionalJsonProvider
                ProviderType.MOCK -> mockInsightProvider
            }
        }
    }

    /**
     * Получает провайдеры, включенные в консенсус
     */
    fun getConsensusProviders(): List<InsightPriceProvider> {
        val consensusEntities = oracleProviderRepository.findByConsensusEnabledTrueAndIsEnabledTrue()
        
        return consensusEntities.mapNotNull { entity ->
            when (entity.providerType) {
                ProviderType.OIL_BULLETIN -> oilBulletinProvider
                ProviderType.REGIONAL_JSON -> regionalJsonProvider
                ProviderType.MOCK -> mockInsightProvider
            }
        }
    }

    /**
     * Проверяет, включен ли режим консенсуса
     */
    fun isConsensusModeEnabled(): Boolean {
        return oracleProviderRepository.countByConsensusEnabledTrueAndIsEnabledTrue() > 1
    }

    /**
     * Получает статистику по провайдерам
     */
    fun getProviderStatistics(): Map<String, Any> {
        val totalProviders = oracleProviderRepository.count()
        val activeProviders = oracleProviderRepository.countByIsEnabledTrue()
        val consensusProviders = oracleProviderRepository.countByConsensusEnabledTrueAndIsEnabledTrue()
        
        val providerTypes = oracleProviderRepository.findAll().groupBy { it.providerType }
            .mapValues { it.value.size }
        
        return mapOf(
            "totalProviders" to totalProviders,
            "activeProviders" to activeProviders,
            "consensusProviders" to consensusProviders,
            "providerTypes" to providerTypes,
            "consensusModeEnabled" to isConsensusModeEnabled()
        )
    }

    /**
     * Инициализирует базовые провайдеры
     */
    @Transactional
    fun initializeDefaultProviders() {
        logger.info("Initializing default Oracle providers")
        
        // Mock Fuel Provider
        if (!oracleProviderRepository.existsByName("Mock Fuel Provider")) {
            val mockProvider = OracleProviderEntity(
                name = "Mock Fuel Provider",
                providerType = ProviderType.MOCK,
                weight = 0.3,
                isEnabled = true,
                priority = 3,
                consensusEnabled = true
            )
            oracleProviderRepository.save(mockProvider)
            logger.info("Created default Mock Fuel Provider")
        }

        // Regional JSON Provider
        if (!oracleProviderRepository.existsByName("Regional JSON Provider")) {
            val regionalProvider = OracleProviderEntity(
                name = "Regional JSON Provider",
                providerType = ProviderType.REGIONAL_JSON,
                dataFilePath = "data/fuel_prices.json",
                region = "US",
                weight = 0.6,
                isEnabled = true,
                priority = 2,
                consensusEnabled = true
            )
            oracleProviderRepository.save(regionalProvider)
            logger.info("Created default Regional JSON Provider")
        }

        // Oil Bulletin API Provider
        if (!oracleProviderRepository.existsByName("Oil Bulletin API Provider")) {
            val oilBulletinProvider = OracleProviderEntity(
                name = "Oil Bulletin API Provider",
                providerType = ProviderType.OIL_BULLETIN,
                apiUrl = "https://api.oilbulletin.com/v1/fuel-prices",
                weight = 0.8,
                isEnabled = true,
                priority = 1,
                consensusEnabled = true
            )
            oracleProviderRepository.save(oilBulletinProvider)
            logger.info("Created default Oil Bulletin API Provider")
        }
        
        logger.info("Default Oracle providers initialized successfully")
    }
}