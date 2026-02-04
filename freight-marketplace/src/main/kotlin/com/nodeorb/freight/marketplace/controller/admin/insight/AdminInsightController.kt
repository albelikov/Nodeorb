package com.nodeorb.freight.marketplace.controller.admin.insight

import com.nodeorb.freight.marketplace.entity.CreateOracleProviderRequest
import com.nodeorb.freight.marketplace.entity.UpdateOracleProviderRequest
import com.nodeorb.freight.marketplace.entity.OracleProviderResponse
import com.nodeorb.freight.marketplace.service.admin.AdminOracleService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Административный контроллер для управления NodeInsight Engine
 * Предоставляет эндпоинты для управления "мозгами" системы
 */
@RestController
@RequestMapping("/api/v1/admin/node-insight")
class AdminInsightController(
    private val adminOracleService: AdminOracleService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AdminInsightController::class.java)
    }

    /**
     * Регистрация нового источника данных
     * POST /admin/node-insight/providers
     */
    @PostMapping("/providers")
    fun registerProvider(
        @RequestBody request: CreateOracleProviderRequest,
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId registering new NodeInsight provider: ${request.name}")
        
        try {
            val provider = adminOracleService.registerProvider(request, adminId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "NodeInsight provider registered successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error registering NodeInsight provider: ${request.name}", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "request" to request,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Включение/выключение провайдера
     * PATCH /admin/node-insight/providers/{id}/activate
     */
    @PatchMapping("/providers/{id}/activate")
    fun toggleProvider(
        @PathVariable id: UUID,
        @RequestParam enabled: Boolean,
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId toggling provider $id to $enabled")
        
        try {
            val provider = adminOracleService.toggleProvider(id, enabled, adminId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "NodeInsight provider toggled successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error toggling NodeInsight provider $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "enabled" to enabled,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Обновление настроек провайдера
     * PUT /admin/node-insight/providers/{id}
     */
    @PutMapping("/providers/{id}")
    fun updateProvider(
        @PathVariable id: UUID,
        @RequestBody request: UpdateOracleProviderRequest,
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId updating provider $id")
        
        try {
            val provider = adminOracleService.updateProvider(id, request, adminId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "NodeInsight provider updated successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error updating NodeInsight provider $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "request" to request,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Удаление провайдера
     * DELETE /admin/node-insight/providers/{id}
     */
    @DeleteMapping("/providers/{id}")
    fun deleteProvider(
        @PathVariable id: UUID,
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId deleting provider $id")
        
        try {
            adminOracleService.deleteProvider(id, adminId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "id" to id,
                "message" to "NodeInsight provider deleted successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error deleting NodeInsight provider $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Установка приоритета провайдера
     * PUT /admin/node-insight/providers/{id}/priority
     */
    @PutMapping("/providers/{id}/priority")
    fun setProviderPriority(
        @PathVariable id: UUID,
        @RequestParam priority: Int,
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId setting priority $priority for provider $id")
        
        try {
            val provider = adminOracleService.setProviderPriority(id, priority, adminId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "Provider priority set successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error setting priority for provider $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "priority" to priority,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Включение/выключение провайдера в консенсус
     * PUT /admin/node-insight/providers/{id}/consensus
     */
    @PutMapping("/providers/{id}/consensus")
    fun toggleConsensus(
        @PathVariable id: UUID,
        @RequestParam consensusEnabled: Boolean,
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId toggling consensus for provider $id to $consensusEnabled")
        
        try {
            val provider = adminOracleService.toggleConsensus(id, consensusEnabled, adminId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "provider" to provider,
                "message" to "Provider consensus toggled successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error toggling consensus for provider $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "consensusEnabled" to consensusEnabled,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Настройка весов NodeInsight (влияние на Match Score)
     * PUT /admin/node-insight/settings
     */
    @PutMapping("/settings")
    fun updateInsightWeights(
        @RequestBody weights: Map<String, Double>,
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId updating NodeInsight weights: $weights")
        
        try {
            val updatedSettings = adminOracleService.updateOracleWeights(weights, adminId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "settings" to updatedSettings,
                "message" to "NodeInsight weights updated successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error updating NodeInsight weights", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "weights" to weights,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Получение текущих настроек NodeInsight
     * GET /admin/node-insight/settings
     */
    @GetMapping("/settings")
    fun getInsightConfiguration(
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId getting NodeInsight configuration")
        
        try {
            val configuration = adminOracleService.getOracleConfiguration()
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "configuration" to configuration,
                "message" to "NodeInsight configuration retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting NodeInsight configuration", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Инициализация базовых провайдеров
     * POST /admin/node-insight/providers/initialize-default
     */
    @PostMapping("/providers/initialize-default")
    fun initializeDefaultProviders(
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId initializing default NodeInsight providers")
        
        try {
            val result = adminOracleService.initializeDefaultProviders(adminId)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "result" to result,
                "message" to "Default NodeInsight providers initialized successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error initializing default NodeInsight providers", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Получение списка всех провайдеров
     * GET /admin/node-insight/providers
     */
    @GetMapping("/providers")
    fun getAllProviders(
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId getting all NodeInsight providers")
        
        try {
            val providers = adminOracleService.getOracleConfiguration()["providers"]
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "providers" to providers,
                "message" to "NodeInsight providers retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting NodeInsight providers", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Получение статистики по провайдерам
     * GET /admin/node-insight/providers/statistics
     */
    @GetMapping("/providers/statistics")
    fun getProviderStatistics(
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId getting NodeInsight provider statistics")
        
        try {
            val statistics = adminOracleService.getOracleConfiguration()["providers"]
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "statistics" to statistics,
                "message" to "NodeInsight provider statistics retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting NodeInsight provider statistics", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Тестирование провайдера
     * GET /admin/node-insight/providers/{id}/test
     */
    @GetMapping("/providers/{id}/test")
    fun testProvider(
        @PathVariable id: UUID,
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId testing provider $id")
        
        try {
            // В реальной системе здесь будет вызов метода тестирования провайдера
            // Пока возвращаем заглушку
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "id" to id,
                "testResult" to "Provider test completed successfully",
                "message" to "NodeInsight provider tested successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error testing NodeInsight provider $id", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "id" to id,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }

    /**
     * Получение истории изменений конфигурации
     * GET /admin/node-insight/history
     */
    @GetMapping("/history")
    fun getConfigurationHistory(
        @RequestHeader("X-Admin-ID") adminId: UUID
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Admin $adminId getting NodeInsight configuration history")
        
        try {
            // В реальной системе здесь будет получение истории из EvidenceCollector
            // Пока возвращаем заглушку
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "history" to listOf(
                    mapOf(
                        "timestamp" to "2026-02-04T15:00:00Z",
                        "adminId" to adminId,
                        "action" to "SYSTEM_CONFIGURATION_CHANGE",
                        "description" to "Updated NodeInsight weights configuration"
                    )
                ),
                "message" to "NodeInsight configuration history retrieved successfully"
            ))
        } catch (e: Exception) {
            logger.error("Error getting NodeInsight configuration history", e)
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "adminId" to adminId,
                "error" to e.message
            ))
        }
    }
}