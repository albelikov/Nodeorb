package com.logi.admin.kmp.ui

import androidx.compose.runtime.mutableStateOf
import com.logi.admin.kmp.model.DashboardData
import com.logi.admin.kmp.model.ServiceHealth
import com.logi.admin.kmp.service.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val apiService: ApiService) {
    
    private val _state = mutableStateOf(AppState())
    val state = _state
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    private val _servicesState = MutableStateFlow(ServicesState())
    val servicesState: StateFlow<ServicesState> = _servicesState.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var refreshJob: Job? = null
    
    init {
        loadDashboardData()
        startAutoRefresh()
    }
    
    fun onRefresh() {
        loadDashboardData()
    }
    
    fun onNavigateTo(screen: Screen) {
        _state.value = _state.value.copy(currentScreen = screen)
        
        when (screen) {
            Screen.Services -> loadServicesData()
            else -> {}
        }
    }
    
    private fun loadDashboardData() {
        _dashboardState.value = _dashboardState.value.copy(isLoading = true, error = null)
        
        coroutineScope.launch {
            try {
                val dashboardData = apiService.getDashboardData()
                _dashboardState.value = DashboardState(
                    isLoading = false,
                    dashboardData = dashboardData,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _dashboardState.value = DashboardState(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }
    
    private fun loadServicesData() {
        _servicesState.value = ServicesState(isLoading = true, error = null)
        
        coroutineScope.launch {
            try {
                val servicesStatus = apiService.getServicesStatus()
                val services = servicesStatus.map { (name, status) ->
                    ServiceHealth(
                        name = name,
                        status = status,
                        url = getServiceUrl(name)
                    )
                }
                
                _servicesState.value = ServicesState(
                    isLoading = false,
                    services = services
                )
            } catch (e: Exception) {
                _servicesState.value = ServicesState(
                    isLoading = false,
                    error = e.message ?: "Failed to load services data"
                )
            }
        }
    }
    
    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = coroutineScope.launch {
            while (true) {
                delay(30000) // Refresh every 30 seconds
                if (_state.value.currentScreen == Screen.Dashboard) {
                    loadDashboardData()
                }
            }
        }
    }
    
    private fun getServiceUrl(serviceName: String): String {
        return when (serviceName) {
            "oms-service" -> "http://localhost:8081"
            "wms-service" -> "http://localhost:8082"
            "tms-service" -> "http://localhost:8083"
            "fms-service" -> "http://localhost:8084"
            "yms-service" -> "http://localhost:8085"
            "transport-platform" -> "http://localhost:8086"
            "gis-subsystem" -> "http://localhost:8087"
            "customs-service" -> "http://localhost:8088"
            "freight-marketplace" -> "http://localhost:8089"
            "autonomous-ops" -> "http://localhost:8090"
            "reverse-logistics" -> "http://localhost:8091"
            "scm-iam" -> "http://localhost:8092"
            "scm-data-protection" -> "http://localhost:8093"
            "scm-audit" -> "http://localhost:8094"
            "cyber-resilience" -> "http://localhost:8095"
            else -> "http://localhost:8080"
        }
    }
    
    fun updateCurrentScreen(screen: Screen) {
        _state.value = _state.value.copy(currentScreen = screen)
    }
}