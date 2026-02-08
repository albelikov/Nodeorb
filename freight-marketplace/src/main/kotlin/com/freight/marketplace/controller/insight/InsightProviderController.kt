package com.freight.marketplace.controller.insight

import com.freight.marketplace.entity.CreateOracleProviderRequest
import com.freight.marketplace.entity.UpdateOracleProviderRequest
import com.freight.marketplace.entity.OracleProviderResponse
import com.freight.marketplace.service.insight.InsightProviderRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST контроллер для управления провайдерами данных NodeInsight Engine
 */
@RestController
@RequestMapping("/api/v1/node-insight/providers")
class InsightProviderController(
    private val insightProviderRegistry: InsightProviderRegistry
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InsightProviderController::class.java)
    }

    /**
     * Получает список всех провайдеров
     */
    @GetMapping
    fun getAllProviders(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting all insight providers")
        
        try {
            val providers = insightProviderRegistry.getAllProviders()
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "providers" to providers,
                "count" to providers.size,
                "message" to "Providers retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting all providers", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    /**
     * Получает провайдера по ID
     */
    @GetMapping("/{id}")
    fun getProvider(@PathVariable id: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Getting provider with ID: $id")
        
        try {
            val provider = insightProviderRegistry.getProvider(id)
            
            return if (provider != null) {
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "provider" to provider,
                    "message" to "Provider retrieved successfully"
                ))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error getting provider with ID: $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "error" to e.message
            ))
        }
    }

    /**
     * Создает новый провайдер
     */
    @PostMapping
    fun createProvider(@RequestBody request: CreateOracleProviderRequest): ResponseEntity<Map<String, Any>> {
        logger.info("Creating new insight provider: ${request.name}")
        
        try {
            val provider = insightProviderRegistry.createProvider(request)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "Provider created successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error creating provider: ${request.name}", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "request" to request,
                "error" to e.message
            ))
        }
    }

    /**
     * Обновляет провайдера
     */
    @PutMapping("/{id}")
    fun updateProvider(
        @PathVariable id: UUID,
        @RequestBody request: UpdateOracleProviderRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Updating provider with ID: $id")
        
        try {
            val provider = insightProviderRegistry.updateProvider(id, request)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "Provider updated successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error updating provider with ID: $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "request" to request,
                "error" to e.message
            ))
        }
    }

    /**
     * Удаляет провайдера
     */
    @DeleteMapping("/{id}")
    fun deleteProvider(@PathVariable id: UUID): ResponseEntity<Map<String, Any>> {
        logger.info("Deleting provider with ID: $id")
        
        try {
            insightProviderRegistry.deleteProvider(id)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "id" to id,
                "message" to "Provider deleted successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error deleting provider with ID: $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "error" to e.message
            ))
        }
    }

    /**
     * Включает/выключает провайдера
     */
    @PatchMapping("/{id}/toggle")
    fun toggleProvider(
        @PathVariable id: UUID,
        @RequestParam enabled: Boolean
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Toggling provider with ID: $id to $enabled")
        
        try {
            val provider = insightProviderRegistry.toggleProvider(id, enabled)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "Provider toggled successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error toggling provider with ID: $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "enabled" to enabled,
                "error" to e.message
            ))
        }
    }

    /**
     * Устанавливает приоритет провайдера
     */
    @PatchMapping("/{id}/priority")
    fun setProviderPriority(
        @PathVariable id: UUID,
        @RequestParam priority: Int
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Setting priority for provider with ID: $id to $priority")
        
        try {
            val provider = insightProviderRegistry.setProviderPriority(id, priority)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "Provider priority set successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error setting priority for provider with ID: $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "priority" to priority,
                "error" to e.message
            ))
        }
    }

    /**
     * Включает/выключает провайдер в консенсус
     */
    @PatchMapping("/{id}/consensus")
    fun toggleConsensus(
        @PathVariable id: UUID,
        @RequestParam consensusEnabled: Boolean
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Toggling consensus for provider with ID: $id to $consensusEnabled")
        
        try {
            val provider = insightProviderRegistry.toggleConsensus(id, consensusEnabled)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "Provider consensus toggled successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error toggling consensus for provider with ID: $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "consensusEnabled" to consensusEnabled,
                "error" to e.message
            ))
        }
    }

    /**
     * Получает статистику по провайдерам
     */
    @GetMapping("/statistics")
    fun getProviderStatistics(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting provider statistics")
        
        try {
            val statistics = insightProviderRegistry.getProviderStatistics()
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "statistics" to statistics,
                "message" to "Provider statistics retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting provider statistics", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    /**
     * Инициализирует базовые провайдеры
     */
    @PostMapping("/initialize-default")
    fun initializeDefaultProviders(): ResponseEntity<Map<String, Any>> {
        logger.info("Initializing default providers")
        
        try {
            insightProviderRegistry.initializeDefaultProviders()
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Default providers initialized successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error initializing default providers", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    /**
     * Получает активные провайдеры
     */
    @GetMapping("/active")
    fun getActiveProviders(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting active providers")
        
        try {
            val activeProviders = insightProviderRegistry.getActiveProviders()
            val providerNames = activeProviders.map { it.getProviderName() }
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "activeProviders" to providerNames,
                "count" to activeProviders.size,
                "message" to "Active providers retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting active providers", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    /**
     * Получает основной провайдер
     */
    @GetMapping("/primary")
    fun getPrimaryProvider(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting primary provider")
        
        try {
            val primaryProvider = insightProviderRegistry.getPrimaryProvider()
            
            return if (primaryProvider != null) {
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "primaryProvider" to primaryProvider.getProviderName(),
                    "type" to primaryProvider.getProviderType().name,
                    "available" to primaryProvider.isAvailable(),
                    "message" to "Primary provider retrieved successfully"
                ))
            } else {
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "primaryProvider" to null,
                    "message" to "No primary provider found"
                ))
            }
        } catch (e: Exception) {
            logger.error("Error getting primary provider", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }

    /**
     * Получает провайдеры в консенсусе
     */
    @GetMapping("/consensus")
    fun getConsensusProviders(): ResponseEntity<Map<String, Any>> {
        logger.info("Getting consensus providers")
        
        try {
            val consensusProviders = insightProviderRegistry.getConsensusProviders()
            val providerNames = consensusProviders.map { it.getProviderName() }
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "consensusProviders" to providerNames,
                "count" to consensusProviders.size,
                "consensusModeEnabled" to insightProviderRegistry.isConsensusModeEnabled(),
                "message" to "Consensus providers retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting consensus providers", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
}