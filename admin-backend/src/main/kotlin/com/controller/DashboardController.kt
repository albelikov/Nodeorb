package com.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.service.DashboardService

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {
    
    @GetMapping("/overview")
    fun getOverview(): Map<String, Any> {
        return dashboardService.getSystemOverview()
    }
    
    @GetMapping("/metrics")
    fun getMetrics(): Map<String, Any> {
        return dashboardService.getSystemMetrics()
    }
    
    @GetMapping("/services/status")
    fun getServicesStatus(): Map<String, String> {
        return dashboardService.getServicesStatus()
    }
    
    @GetMapping("/services/status/detailed")
    fun getDetailedServicesStatus(): Map<String, Any> {
        return dashboardService.getDetailedServicesStatus()
    }
}
