package com.logi.admin.kmp.ui

import com.logi.admin.kmp.model.DashboardData
import com.logi.admin.kmp.model.ServiceHealth

data class DashboardState(
    val isLoading: Boolean = false,
    val dashboardData: DashboardData? = null,
    val error: String? = null,
    val lastUpdated: Long = 0L
)

data class ServicesState(
    val isLoading: Boolean = false,
    val services: List<ServiceHealth> = emptyList(),
    val error: String? = null
)

data class AppState(
    val currentScreen: Screen = Screen.Dashboard,
    val dashboardState: DashboardState = DashboardState(),
    val servicesState: ServicesState = ServicesState()
)

enum class Screen {
    Dashboard,
    Services,
    Orders,
    Fleet,
    Warehouses
}