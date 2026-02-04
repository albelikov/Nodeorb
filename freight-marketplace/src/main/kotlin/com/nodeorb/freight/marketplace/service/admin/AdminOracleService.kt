package com.nodeorb.freight.marketplace.service.admin

import com.nodeorb.freight.marketplace.entity.OracleProviderEntity
import com.nodeorb.freight.marketplace.entity.ProviderType
import com.nodeorb.freight.marketplace.entity.CreateOracleProviderRequest
import com.nodeorb.freight.marketplace.entity.UpdateOracleProviderRequest
import com.nodeorb.freight.marketplace.entity.OracleProviderResponse
import com.nodeorb.freight.marketplace.service.insight.InsightProviderRegistry
import com.nodeorb.freight.marketplace.service.insight.MarketInsightService
import com.nodeorb.freight.marketplace.service.audit.EvidenceCollectorService
import com.nodeorb.freight.marketplace.service.audit.EvidenceType
import com.nodeorb.freight.marketplace.service.audit.EvidenceAction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Сервис административного управления Oracle
 * Предоставляет методы для управления провайдерами и настройками системы
 */
@Service
class AdminOracleService(
    private val providerRegistry: InsightProviderRegistry,
    private val marketOracleService: MarketInsightService,
    private val evidenceCollectorService: EvidenceCollectorService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AdminOracleService::class.java)
    }

    /**
     * Регистрация нового источника данных
     */
    @Transactional
    fun registerProvider(
        request: CreateOracleProviderRequest,
        adminId: UUID
    ): OracleProviderResponse {
        logger.info("Admin $adminId registering new provider: ${request.name}")
        
        try {
            val provider = providerRegistry.createProvider(request)
            
            // Фиксируем изменение в системе аудита
            evidenceCollectorService.collectEvidence(
                adminId,
                EvidenceType.SYSTEM_CONFIGURATION_CHANGE,
                EvidenceAction.CREATE,
                "Registered new Oracle provider: ${provider.name}",
                mapOf(
                    "providerId" to provider.id,
                    "providerName" to provider.name,
                    "providerType" to provider.providerType.name,
                    "priority" to provider.priority,
                    "weight" to provider.weight,
                    "isEnabled" to provider.isEnabled
                )
            )
            
            logger.info("Successfully registered provider: ${provider.name} (ID: ${provider.id})")
            return provider
            
        } catch (e: Exception) {
            logger.error("Failed to register provider: ${request.name}", e)
            throw RuntimeException("Failed to register Oracle provider", e)
        }
    }

    /**
     * Включение/выключение провайдера
     */
    @Transactional
    fun toggleProvider(
        providerId: UUID,
        enabled: Boolean,
        adminId: UUID
    ): OracleProviderResponse {
        logger.info("Admin $adminId toggling provider $providerId to $enabled")
        
        try {
            val provider = providerRegistry.toggleProvider(providerId, enabled)
            
            // Фиксируем изменение в системе аудита
            evidenceCollectorService.collectEvidence(
                adminId,
                EvidenceType.SYSTEM_CONFIGURATION_CHANGE,
                EvidenceAction.UPDATE,
                "Toggled Oracle provider: ${provider.name} to $enabled",
                mapOf(
                    "providerId" to provider.id,
                    "providerName" to provider.name,
                    "enabled" to enabled,
                    "previousState" to !enabled
                )
            )
            
            logger.info("Successfully toggled provider ${provider.name} to $enabled")
            return provider
            
        } catch (e: Exception) {
            logger.error("Failed to toggle provider $providerId", e)
            throw RuntimeException("Failed to toggle Oracle provider", e)
        }
    }

    /**
     * Обновление настроек провайдера
     */
    @Transactional
    fun updateProvider(
        providerId: UUID,
        request: UpdateOracleProviderRequest,
        adminId: UUID
    ): OracleProviderResponse {
        logger.info("Admin $adminId updating provider $providerId")
        
        try {
            val provider = providerRegistry.updateProvider(providerId, request)
            
            // Фиксируем изменение в системе аудита
            evidenceCollectorService.collectEvidence(
                adminId,
                EvidenceType.SYSTEM_CONFIGURATION_CHANGE,
                EvidenceAction.UPDATE,
                "Updated Oracle provider: ${provider.name}",
                mapOf(
                    "providerId" to provider.id,
                    "providerName" to provider.name,
                    "updates" to request
                )
            )
            
            logger.info("Successfully updated provider ${provider.name}")
            return provider
            
        } catch (e: Exception) {
            logger.error("Failed to update provider $providerId", e)
            throw RuntimeException("Failed to update Oracle provider", e)
        }
    }

    /**
     * Удаление провайдера
     */
    @Transactional
    fun deleteProvider(
        providerId: UUID,
        adminId: UUID
    ) {
        logger.info("Admin $adminId deleting provider $providerId")
        
        try {
            val provider = providerRegistry.getProvider(providerId)
                ?: throw IllegalArgumentException("Provider with ID '$providerId' not found")
            
            providerRegistry.deleteProvider(providerId)
            
            // Фиксируем изменение в системе аудита
            evidenceCollectorService.collectEvidence(
                adminId,
                EvidenceType.SYSTEM_CONFIGURATION_CHANGE,
                EvidenceAction.DELETE,
                "Deleted Oracle provider: ${provider.name}",
                mapOf(
                    "providerId" to providerId,
                    "providerName" to provider.name,
                    "providerType" to provider.providerType.name
                )
            )
            
            logger.info("Successfully deleted provider ${provider.name}")
            
        } catch (e: Exception) {
            logger.error("Failed to delete provider $providerId", e)
            throw RuntimeException("Failed to delete Oracle provider", e)
        }
    }

    /**
     * Установка приоритета провайдера
     */
    @Transactional
    fun setProviderPriority(
        providerId: UUID,
        priority: Int,
        adminId: UUID
    ): OracleProviderResponse {
        logger.info("Admin $adminId setting priority $priority for provider $providerId")
        
        try {
            val provider = providerRegistry.setProviderPriority(providerId, priority)
            
            // Фиксируем изменение в системе аудита
            evidenceCollectorService.collectEvidence(
                adminId,
                EvidenceType.SYSTEM_CONFIGURATION_CHANGE,
                EvidenceAction.UPDATE,
                "Set priority for Oracle provider: ${provider.name}",
                mapOf(
                    "providerId" to provider.id,
                    "providerName" to provider.name,
                    "priority" to priority
                )
            )
            
            logger.info("Successfully set priority $priority for provider ${provider.name}")
            return provider
            
        } catch (e: Exception) {
            logger.error("Failed to set priority for provider $providerId", e)
            throw RuntimeException("Failed to set provider priority", e)
        }
    }

    /**
     * Включение/выключение провайдера в консенсус
     */
    @Transactional
    fun toggleConsensus(
        providerId: UUID,
        consensusEnabled: Boolean,
        adminId: UUID
    ): OracleProviderResponse {
        logger.info("Admin $adminId toggling consensus for provider $providerId to $consensusEnabled")
        
        try {
            val provider = providerRegistry.toggleConsensus(providerId, consensusEnabled)
            
            // Фиксируем изменение в системе аудита
            evidenceCollectorService.collectEvidence(
                adminId,
                EvidenceType.SYSTEM_CONFIGURATION_CHANGE,
                EvidenceAction.UPDATE,
                "Toggled consensus for Oracle provider: ${provider.name}",
                mapOf(
                    "providerId" to provider.id,
                    "providerName" to provider.name,
                    "consensusEnabled" to consensusEnabled
                )
            )
            
            logger.info("Successfully toggled consensus for provider ${provider.name} to $consensusEnabled")
            return provider
            
        } catch (e: Exception) {
            logger.error("Failed to toggle consensus for provider $providerId", e)
            throw RuntimeException("Failed to toggle provider consensus", e)
        }
    }

    /**
     * Настройка весов Oracle (влияние на Match Score)
     */
    @Transactional
    fun updateOracleWeights(
        weights: Map<String, Double>,
        adminId: UUID
    ): Map<String, Any> {
        logger.info("Admin $adminId updating Oracle weights: $weights")
        
        try {
            // Проверяем валидность весов
            validateWeights(weights)
            
            // Сохраняем настройки весов (в реальной системе это может быть в Redis или БД)
            val updatedSettings = saveOracleWeights(weights)
            
            // Фиксируем изменение в системе аудита
            evidenceCollectorService.collectEvidence(
                adminId,
                EvidenceType.SYSTEM_CONFIGURATION_CHANGE,
                EvidenceAction.UPDATE,
                "Updated Oracle weights configuration",
                mapOf(
                    "weights" to weights,
                    "adminId" to adminId
                )
            )
            
            logger.info("Successfully updated Oracle weights")
            return updatedSettings
            
        } catch (e: Exception) {
            logger.error("Failed to update Oracle weights", e)
            throw RuntimeException("Failed to update Oracle weights", e)
        }
    }

    /**
     * Получение текущих настроек Oracle
     */
    fun getOracleConfiguration(): Map<String, Any> {
        logger.info("Getting Oracle configuration")
        
        try {
            val providerStatistics = providerRegistry.getProviderStatistics()
            val currentWeights = getCurrentOracleWeights()
            
            return mapOf(
                "providers" to providerStatistics,
                "weights" to currentWeights,
                "marketOracle" to mapOf(
                    "fuelUpdateIntervalHours" to 6,
                    "priceCacheTTLMinutes" to 30,
                    "riskThresholdPercent" to 15.0
                )
            )
            
        } catch (e: Exception) {
            logger.error("Failed to get Oracle configuration", e)
            throw RuntimeException("Failed to get Oracle configuration", e)
        }
    }

    /**
     * Инициализация базовых провайдеров
     */
    @Transactional
    fun initializeDefaultProviders(adminId: UUID): Map<String, Any> {
        logger.info("Admin $adminId initializing default Oracle providers")
        
        try {
            providerRegistry.initializeDefaultProviders()
            
            // Фиксируем изменение в системе аудита
            evidenceCollectorService.collectEvidence(
                adminId,
                EvidenceType.SYSTEM_CONFIGURATION_CHANGE,
                EvidenceAction.CREATE,
                "Initialized default Oracle providers",
                mapOf(
                    "adminId" to adminId,
                    "defaultProviders" to listOf(
                        "Mock Fuel Provider",
                        "Regional JSON Provider", 
                        "Oil Bulletin API Provider"
                    )
                )
            )
            
            logger.info("Successfully initialized default providers")
            return mapOf(
                "success" to true,
                "message" to "Default Oracle providers initialized successfully"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to initialize default providers", e)
            throw RuntimeException("Failed to initialize default Oracle providers", e)
        }
    }

    /**
     * Проверка валидности весов
     */
    private fun validateWeights(weights: Map<String, Double>) {
        val validKeys = setOf("fuelSurcharge", "marketPrice", "routeDistance", "providerReliability")
        
        weights.forEach { (key, value) ->
            if (!validKeys.contains(key)) {
                throw IllegalArgumentException("Invalid weight key: $key")
            }
            if (value !in 0.0..1.0) {
                throw IllegalArgumentException("Weight value for $key must be between 0.0 and 1.0")
            }
        }
        
        // Проверяем, что сумма весов не превышает 1.0
        val totalWeight = weights.values.sum()
        if (totalWeight > 1.0) {
            throw IllegalArgumentException("Total weight cannot exceed 1.0")
        }
    }

    /**
     * Сохранение весов Oracle (в реальной системе это может быть в Redis или БД)
     */
    private fun saveOracleWeights(weights: Map<String, Double>): Map<String, Any> {
        // Временная реализация - в реальной системе веса будут сохраняться в постоянное хранилище
        logger.info("Saving Oracle weights: $weights")
        
        return mapOf(
            "weights" to weights,
            "totalWeight" to weights.values.sum(),
            "timestamp" to java.time.LocalDateTime.now()
        )
    }

    /**
     * Получение текущих весов Oracle
     */
    private fun getCurrentOracleWeights(): Map<String, Double> {
        // Временная реализация - в реальной системе веса будут загружаться из постоянного хранилища
        return mapOf(
            "fuelSurcharge" to 0.3,
            "marketPrice" to 0.4,
            "routeDistance" to 0.2,
            "providerReliability" to 0.1
        )
    }
}