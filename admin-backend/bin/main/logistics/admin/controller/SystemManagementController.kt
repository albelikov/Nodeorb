package logistics.admin.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import logistics.admin.service.SystemMonitoringService

@RestController
@RequestMapping("/api/admin/system")
@PreAuthorize("hasRole('ADMIN')")
class SystemManagementController(
    private val systemMonitoringService: SystemMonitoringService
) {
    
    @GetMapping("/health")
    fun getSystemHealth(): Map<String, Any> {
        return systemMonitoringService.getSystemHealth()
    }
    
    @GetMapping("/config")
    fun getSystemConfig(): Map<String, Any> {
        return systemMonitoringService.getSystemConfiguration()
    }
    
    @PutMapping("/config/{key}")
    fun updateConfig(
        @PathVariable key: String,
        @RequestBody value: Any
    ): Map<String, Any> {
        return systemMonitoringService.updateConfiguration(key, value)
    }
}

