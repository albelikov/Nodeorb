import androidx.compose.ui.window.ComposeViewport
import com.logi.admin.kmp.ui.MainViewModel
import com.logi.admin.kmp.ui.Screen
import com.logi.admin.kmp.ui.components.DashboardScreen
import com.logi.admin.kmp.service.createApiService
import kotlinx.browser.document
import kotlinx.browser.window

fun main() {
    val apiService = createApiService()
    val viewModel = MainViewModel(apiService)
    
    ComposeViewport(document.body!!) {
        DashboardScreen(
            state = viewModel.dashboardState.value,
            onRefresh = { viewModel.onRefresh() },
            onNavigateTo = { screen -> viewModel.onNavigateTo(screen) }
        )
    }
    
    // Auto-refresh every 30 seconds
    window.setInterval({
        viewModel.onRefresh()
    }, 30000)
}