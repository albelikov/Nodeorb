package com.service

import com.client.*
import com.config.ServiceConfig
import org.springframework.stereotype.Service

@Service
class DashboardService(
    private val serviceConfig: ServiceConfig,
    private val omsServiceClient: OmsServiceClient,
    private val wmsServiceClient: WmsServiceClient,
    private val fmsServiceClient: FmsServiceClient,
    private val tmsServiceClient: TmsServiceClient,
    private val serviceClient: ServiceClient
) {
    
    fun getSystemOverview(): Map<String, Any> {
        return try {
            val totalOrders = if (serviceConfig.oms.enabled) {
                omsServiceClient.getTotalOrders(serviceConfig.oms.url)
            } else 0L
            
            val activeFleets = if (serviceConfig.fms.enabled) {
                fmsServiceClient.getActiveFleets(serviceConfig.fms.url)
            } else 0
            
            val warehouseUtilization = if (serviceConfig.wms.enabled) {
                wmsServiceClient.getWarehouseUtilization(serviceConfig.wms.url)
            } else 0.0
            
            val activeShipments = if (serviceConfig.tms.enabled) {
                tmsServiceClient.getActiveShipments(serviceConfig.tms.url)
            } else 0
            
            // Определяем общее здоровье системы на основе статусов сервисов
            val servicesHealth = getServicesStatus()
            val allServicesUp = servicesHealth.values.all { it == "UP" }
            val systemHealth = if (allServicesUp) "HEALTHY" else "DEGRADED"
            
            mapOf(
                "totalOrders" to totalOrders,
                "activeFleets" to activeFleets,
                "warehouseUtilization" to warehouseUtilization,
                "activeShipments" to activeShipments,
                "systemHealth" to systemHealth,
                "timestamp" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            mapOf(
                "totalOrders" to 0,
                "activeFleets" to 0,
                "warehouseUtilization" to 0.0,
                "activeShipments" to 0,
                "systemHealth" to "ERROR",
                "error" to (e.message ?: "Unknown error"),
                "timestamp" to System.currentTimeMillis()
            )
        }
    }
    
    fun getSystemMetrics(): Map<String, Any> {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        // Собираем метрики со всех сервисов
        val servicesMetrics = mutableMapOf<String, Map<String, Any>>()
        
        if (serviceConfig.oms.enabled) {
            serviceClient.getMetrics("oms-service", serviceConfig.oms.url)?.let {
                servicesMetrics["oms-service"] = it
            }
        }
        
        if (serviceConfig.wms.enabled) {
            serviceClient.getMetrics("wms-service", serviceConfig.wms.url)?.let {
                servicesMetrics["wms-service"] = it
            }
        }
        
        if (serviceConfig.tms.enabled) {
            serviceClient.getMetrics("tms-service", serviceConfig.tms.url)?.let {
                servicesMetrics["tms-service"] = it
            }
        }
        
        if (serviceConfig.fms.enabled) {
            serviceClient.getMetrics("fms-service", serviceConfig.fms.url)?.let {
                servicesMetrics["fms-service"] = it
            }
        }
        
        return mapOf(
            "cpuUsage" to 0.0, // TODO: Реализовать получение CPU usage
            "memoryUsage" to (usedMemory.toDouble() / totalMemory.toDouble() * 100),
            "totalMemory" to totalMemory,
            "freeMemory" to freeMemory,
            "usedMemory" to usedMemory,
            "activeConnections" to 0, // TODO: Реализовать подсчет соединений
            "throughput" to 0.0, // TODO: Реализовать расчет throughput
            "servicesMetrics" to servicesMetrics,
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    fun getServicesStatus(): Map<String, String> {
        val statuses = mutableMapOf<String, String>()
        
        if (serviceConfig.oms.enabled) {
            val health = serviceClient.checkHealth("oms-service", serviceConfig.oms.url)
            statuses["oms-service"] = health.status
        } else {
            statuses["oms-service"] = "DISABLED"
        }
        
        if (serviceConfig.wms.enabled) {
            val health = serviceClient.checkHealth("wms-service", serviceConfig.wms.url)
            statuses["wms-service"] = health.status
        } else {
            statuses["wms-service"] = "DISABLED"
        }
        
        if (serviceConfig.tms.enabled) {
            val health = serviceClient.checkHealth("tms-service", serviceConfig.tms.url)
            statuses["tms-service"] = health.status
        } else {
            statuses["tms-service"] = "DISABLED"
        }
        
        if (serviceConfig.fms.enabled) {
            val health = serviceClient.checkHealth("fms-service", serviceConfig.fms.url)
            statuses["fms-service"] = health.status
        } else {
            statuses["fms-service"] = "DISABLED"
        }
        
        if (serviceConfig.yms.enabled) {
            val health = serviceClient.checkHealth("yms-service", serviceConfig.yms.url)
            statuses["yms-service"] = health.status
        } else {
            statuses["yms-service"] = "DISABLED"
        }
        
        if (serviceConfig.transportPlatform.enabled) {
            val health = serviceClient.checkHealth("transport-platform", serviceConfig.transportPlatform.url)
            statuses["transport-platform"] = health.status
        } else {
            statuses["transport-platform"] = "DISABLED"
        }
        
        if (serviceConfig.gisSubsystem.enabled) {
            val health = serviceClient.checkHealth("gis-subsystem", serviceConfig.gisSubsystem.url)
            statuses["gis-subsystem"] = health.status
        } else {
            statuses["gis-subsystem"] = "DISABLED"
        }
        
        if (serviceConfig.customs.enabled) {
            val health = serviceClient.checkHealth("customs-service", serviceConfig.customs.url)
            statuses["customs-service"] = health.status
        } else {
            statuses["customs-service"] = "DISABLED"
        }
        
        if (serviceConfig.freightMarketplace.enabled) {
            val health = serviceClient.checkHealth("freight-marketplace", serviceConfig.freightMarketplace.url)
            statuses["freight-marketplace"] = health.status
        } else {
            statuses["freight-marketplace"] = "DISABLED"
        }
        
        if (serviceConfig.autonomousOps.enabled) {
            val health = serviceClient.checkHealth("autonomous-ops", serviceConfig.autonomousOps.url)
            statuses["autonomous-ops"] = health.status
        } else {
            statuses["autonomous-ops"] = "DISABLED"
        }
        
        if (serviceConfig.reverseLogistics.enabled) {
            val health = serviceClient.checkHealth("reverse-logistics", serviceConfig.reverseLogistics.url)
            statuses["reverse-logistics"] = health.status
        } else {
            statuses["reverse-logistics"] = "DISABLED"
        }
        
        if (serviceConfig.scmIam.enabled) {
            val health = serviceClient.checkHealth("scm-iam", serviceConfig.scmIam.url)
            statuses["scm-iam"] = health.status
        } else {
            statuses["scm-iam"] = "DISABLED"
        }
        
        if (serviceConfig.scmDataProtection.enabled) {
            val health = serviceClient.checkHealth("scm-data-protection", serviceConfig.scmDataProtection.url)
            statuses["scm-data-protection"] = health.status
        } else {
            statuses["scm-data-protection"] = "DISABLED"
        }
        
        if (serviceConfig.scmAudit.enabled) {
            val health = serviceClient.checkHealth("scm-audit", serviceConfig.scmAudit.url)
            statuses["scm-audit"] = health.status
        } else {
            statuses["scm-audit"] = "DISABLED"
        }
        
        if (serviceConfig.cyberResilience.enabled) {
            val health = serviceClient.checkHealth("cyber-resilience", serviceConfig.cyberResilience.url)
            statuses["cyber-resilience"] = health.status
        } else {
            statuses["cyber-resilience"] = "DISABLED"
        }
        
        return statuses
    }
    
    fun getDetailedServicesStatus(): Map<String, Any> {
        val services = mutableMapOf<String, Any>()
        
        val serviceConfigs = listOf(
            "oms-service" to serviceConfig.oms,
            "wms-service" to serviceConfig.wms,
            "tms-service" to serviceConfig.tms,
            "fms-service" to serviceConfig.fms,
            "yms-service" to serviceConfig.yms,
            "transport-platform" to serviceConfig.transportPlatform,
            "gis-subsystem" to serviceConfig.gisSubsystem,
            "customs-service" to serviceConfig.customs,
            "freight-marketplace" to serviceConfig.freightMarketplace,
            "autonomous-ops" to serviceConfig.autonomousOps,
            "reverse-logistics" to serviceConfig.reverseLogistics,
            "scm-iam" to serviceConfig.scmIam,
            "scm-data-protection" to serviceConfig.scmDataProtection,
            "scm-audit" to serviceConfig.scmAudit,
            "cyber-resilience" to serviceConfig.cyberResilience
        )
        
        serviceConfigs.forEach { (name, config) ->
            if (config.enabled) {
                val health = serviceClient.checkHealth(name, config.url)
                services[name] = mapOf(
                    "status" to health.status,
                    "url" to health.url,
                    "responseTime" to (health.responseTime ?: 0),
                    "error" to (health.error ?: ""),
                    "details" to (health.details ?: emptyMap())
                )
            } else {
                services[name] = mapOf(
                    "status" to "DISABLED",
                    "url" to config.url,
                    "enabled" to false
                )
            }
        }
        
        return mapOf(
            "services" to services,
            "timestamp" to System.currentTimeMillis()
        )
    }
}
