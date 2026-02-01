import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.logi.admin.kmp.ui.MainViewModel
import com.logi.admin.kmp.ui.Screen
import com.logi.admin.kmp.ui.components.DashboardScreen
import com.logi.admin.kmp.service.createApiService

fun main() = application {
    val apiService = createApiService()
    val viewModel = MainViewModel(apiService)
    
    Window(
        onCloseRequest = { exitApplication() },
        title = "Logistics Admin KMP",
        resizable = true
    ) {
        DashboardScreen(
            state = viewModel.dashboardState.value,
            onRefresh = { viewModel.onRefresh() },
            onNavigateTo = { screen -> viewModel.onNavigateTo(screen) }
        )
    }
}