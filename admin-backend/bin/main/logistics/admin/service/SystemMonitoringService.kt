package logistics.admin.service

import logistics.admin.client.ServiceClient
import logistics.admin.config.ServiceConfig
import org.springframework.stereotype.Service
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import java.lang.management.ManagementFactory

@Service
class SystemMonitoringService(
    private val serviceConfig: ServiceConfig,
    private val serviceClient: ServiceClient,
    private val dashboardService: DashboardService
) : HealthIndicator {
    
    private val startTime = System.currentTimeMillis()
    
    override fun health(): Health {
        val servicesStatus = dashboardService.getServicesStatus()
        val allServicesUp = servicesStatus.values.all { it == "UP" || it == "DISABLED" }
        val anyServiceDown = servicesStatus.values.any { it == "DOWN" }
        
        return if (allServicesUp && !anyServiceDown) {
            Health.up()
                .withDetail("status", "Operational")
                .withDetail("uptime", getUptime())
                .withDetail("servicesCount", servicesStatus.size)
                .withDetail("timestamp", System.currentTimeMillis())
                .build()
        } else {
            Health.down()
                .withDetail("status", "Degraded")
                .withDetail("uptime", getUptime())
                .withDetail("servicesStatus", servicesStatus)
                .withDetail("timestamp", System.currentTimeMillis())
                .build()
        }
    }
    
    fun getSystemHealth(): Map<String, Any> {
        val servicesStatus = dashboardService.getServicesStatus()
        val allServicesUp = servicesStatus.values.all { it == "UP" || it == "DISABLED" }
        val anyServiceDown = servicesStatus.values.any { it == "DOWN" }
        val status = when {
            anyServiceDown -> "DEGRADED"
            allServicesUp -> "HEALTHY"
            else -> "WARNING"
        }
        
        return mapOf(
            "status" to status,
            "uptime" to getUptime(),
            "uptimeFormatted" to formatUptime(getUptime()),
            "services" to getServicesHealth(),
            "servicesCount" to servicesStatus.size,
            "servicesUp" to servicesStatus.values.count { it == "UP" },
            "servicesDown" to servicesStatus.values.count { it == "DOWN" },
            "servicesDisabled" to servicesStatus.values.count { it == "DISABLED" },
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    fun getSystemConfiguration(): Map<String, Any> {
        return mapOf(
            "kafka.enabled" to false,
            "database.type" to "PostgreSQL",
            "security.enabled" to true,
            "services" to mapOf(
                "oms" to mapOf("url" to serviceConfig.oms.url, "enabled" to serviceConfig.oms.enabled),
                "wms" to mapOf("url" to serviceConfig.wms.url, "enabled" to serviceConfig.wms.enabled),
                "tms" to mapOf("url" to serviceConfig.tms.url, "enabled" to serviceConfig.tms.enabled),
                "fms" to mapOf("url" to serviceConfig.fms.url, "enabled" to serviceConfig.fms.enabled),
                "yms" to mapOf("url" to serviceConfig.yms.url, "enabled" to serviceConfig.yms.enabled),
                "transportPlatform" to mapOf("url" to serviceConfig.transportPlatform.url, "enabled" to serviceConfig.transportPlatform.enabled),
                "gisSubsystem" to mapOf("url" to serviceConfig.gisSubsystem.url, "enabled" to serviceConfig.gisSubsystem.enabled),
                "customs" to mapOf("url" to serviceConfig.customs.url, "enabled" to serviceConfig.customs.enabled),
                "freightMarketplace" to mapOf("url" to serviceConfig.freightMarketplace.url, "enabled" to serviceConfig.freightMarketplace.enabled),
                "autonomousOps" to mapOf("url" to serviceConfig.autonomousOps.url, "enabled" to serviceConfig.autonomousOps.enabled),
                "reverseLogistics" to mapOf("url" to serviceConfig.reverseLogistics.url, "enabled" to serviceConfig.reverseLogistics.enabled),
                "scmIam" to mapOf("url" to serviceConfig.scmIam.url, "enabled" to serviceConfig.scmIam.enabled),
                "scmDataProtection" to mapOf("url" to serviceConfig.scmDataProtection.url, "enabled" to serviceConfig.scmDataProtection.enabled),
                "scmAudit" to mapOf("url" to serviceConfig.scmAudit.url, "enabled" to serviceConfig.scmAudit.enabled),
                "cyberResilience" to mapOf("url" to serviceConfig.cyberResilience.url, "enabled" to serviceConfig.cyberResilience.enabled)
            ),
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    fun updateConfiguration(key: String, value: Any): Map<String, Any> {
        // TODO: Реализовать обновление конфигурации через Spring Cloud Config или другой механизм
        return mapOf(
            "key" to key,
            "value" to value,
            "status" to "updated",
            "message" to "Configuration update not yet implemented",
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    private fun getUptime(): Long {
        return System.currentTimeMillis() - startTime
    }
    
    private fun formatUptime(ms: Long): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
            hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    private fun getServicesHealth(): Map<String, String> {
        return dashboardService.getServicesStatus()
    }
}

