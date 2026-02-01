package com.logi.admin.kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.logi.admin.kmp.model.ServiceHealth
import com.logi.admin.kmp.ui.DashboardState
import com.logi.admin.kmp.ui.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onRefresh: () -> Unit,
    onNavigateTo: (Screen) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logistics Ecosystem Dashboard") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (state.isLoading) {
                LoadingIndicator()
            } else {
                DashboardContent(state, onNavigateTo)
            }
            
            if (state.error != null) {
                ErrorSnackbar(state.error)
            }
        }
    }
}

@Composable
private fun DashboardContent(state: DashboardState, onNavigateTo: (Screen) -> Unit) {
    val data = state.dashboardData ?: return
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            // Overview Cards
            OverviewCards(data.overview)
        }
        
        item {
            Spacer(Modifier.height(16.dp))
            Text("System Metrics", style = MaterialTheme.typography.titleMedium)
        }
        
        item {
            SystemMetrics(data.metrics)
        }
        
        item {
            Spacer(Modifier.height(16.dp))
            Text("Services Status", style = MaterialTheme.typography.titleMedium)
        }
        
        items(data.detailedServicesStatus["services"] as? Map<String, Map<String, Any>> ?: emptyMap().toList()) { (serviceName, serviceInfo) ->
            ServiceCard(serviceName, serviceInfo)
        }
        
        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OverviewCards(overview: com.logi.admin.kmp.model.SystemOverview) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OverviewCard(
            title = "📦 Заказы",
            value = overview.totalOrders.toString(),
            color = Color(0xFF3498DB)
        )
        
        OverviewCard(
            title = "🚚 Активный флот",
            value = overview.activeFleets.toString(),
            color = Color(0xFF27AE60)
        )
        
        OverviewCard(
            title = "📊 Загрузка складов",
            value = "${overview.warehouseUtilization.toInt()}%",
            color = Color(0xFFF1C40F)
        )
        
        OverviewCard(
            title = "🚛 Активные перевозки",
            value = overview.activeShipments.toString(),
            color = Color(0xFFE74C3C)
        )
    }
}

@Composable
private fun OverviewCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun SystemMetrics(metrics: com.logi.admin.kmp.model.SystemMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Memory Usage: ${metrics.memoryUsage.toInt()}%", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = metrics.memoryUsage.toFloat() / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(8.dp))
            Text("Total Memory: ${metrics.totalMemory / 1024 / 1024} MB", style = MaterialTheme.typography.bodySmall)
            Text("Used Memory: ${metrics.usedMemory / 1024 / 1024} MB", style = MaterialTheme.typography.bodySmall)
            Text("Free Memory: ${metrics.freeMemory / 1024 / 1024} MB", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ServiceCard(serviceName: String, serviceInfo: Map<String, Any>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val status = serviceInfo["status"] as? String ?: "UNKNOWN"
            val statusColor = when (status) {
                "UP" -> Color(0xFF27AE60)
                "DOWN" -> Color(0xFFE74C3C)
                else -> Color(0xFF95A5A6)
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, RoundedCornerShape(50))
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(serviceName, style = MaterialTheme.typography.bodyMedium)
                Text("Status: $status", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                
                serviceInfo["responseTime"]?.let { responseTime ->
                    Text("Response Time: ${responseTime}ms", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            
            IconButton(onClick = { /* Handle action */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorSnackbar(error: String) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            // Retry action
        }
    ) {
        Text(error)
    }
}