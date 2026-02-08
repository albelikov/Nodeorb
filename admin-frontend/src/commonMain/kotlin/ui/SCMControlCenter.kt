package com.logi.admin.kmp.ui

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
import com.logi.admin.kmp.model.*
import com.logi.admin.kmp.service.ApiService
import com.logi.admin.kmp.ui.components.PriceReferenceForm
import com.logi.admin.kmp.ui.components.EvidencePackageModal
import com.logi.admin.kmp.ui.components.AppealModal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SCMControlCenter(
    apiService: ApiService,
    onNavigateTo: (Screen) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // State for Price Reference Manager
    var priceReferences by remember { mutableStateOf<List<PriceReference>>(emptyList()) }
    var isPriceFormOpen by remember { mutableStateOf(false) }
    var selectedPriceReference by remember { mutableStateOf<PriceReference?>(null) }
    
    // State for Security Events
    var securityEvents by remember { mutableStateOf<List<SecurityEvent>>(emptyList()) }
    var selectedEvent by remember { mutableStateOf<SecurityEvent?>(null) }
    
    // State for Appeals
    var appeals by remember { mutableStateOf<List<Appeal>>(emptyList()) }
    var selectedAppeal by remember { mutableStateOf<Appeal?>(null) }
    var isAppealModalOpen by remember { mutableStateOf(false) }
    
    // State for Evidence Package
    var evidencePackage by remember { mutableStateOf<EvidencePackage?>(null) }
    var isEvidenceModalOpen by remember { mutableStateOf(false) }
    
    // Loading states
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Form state for Price Reference
    var materialType by remember { mutableStateOf("") }
    var baseCost by remember { mutableStateOf("") }
    var laborRate by remember { mutableStateOf("") }
    var serviceCategory by remember { mutableStateOf("") }
    var allowedDeviation by remember { mutableStateOf("") }
    
    // Load initial data
    LaunchedEffect(Unit) {
        loadAllData()
    }
    
    // Load all data function
    suspend fun loadAllData() {
        isLoading = true
        try {
            loadPriceReferences()
            loadSecurityEvents()
            loadAppeals()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }
    
    suspend fun loadPriceReferences() {
        priceReferences = apiService.getPriceReferences()
    }
    
    suspend fun loadSecurityEvents() {
        val response = apiService.getSecurityEvents(page = 0, size = 50)
        securityEvents = response.events
    }
    
    suspend fun loadAppeals() {
        val response = apiService.getAppeals(page = 0, size = 50)
        appeals = response.appeals
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SCM Control Center") },
                actions = {
                    IconButton(onClick = { scope.launch { loadAllData() } }) {
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
            if (isLoading) {
                LoadingIndicator()
            } else {
                SCMContent(
                    priceReferences = priceReferences,
                    securityEvents = securityEvents,
                    appeals = appeals,
                    onAddPriceReference = { isPriceFormOpen = true },
                    onEditPriceReference = { ref ->
                        selectedPriceReference = ref
                        materialType = ref.materialType
                        baseCost = ref.baseCost.toString()
                        laborRate = ref.laborRatePerHour.toString()
                        serviceCategory = ref.serviceCategory
                        allowedDeviation = ref.allowedDeviation.toString()
                        isPriceFormOpen = true
                    },
                    onDeletePriceReference = { id ->
                        scope.launch {
                            if (apiService.deletePriceReference(id)) {
                                loadPriceReferences()
                            } else {
                                error = "Failed to delete price reference"
                            }
                        }
                    },
                    onSecurityEventClick = { event ->
                        selectedEvent = event
                        scope.launch {
                            evidencePackage = apiService.getEvidencePackage(event.id)
                            isEvidenceModalOpen = true
                        }
                    },
                    onAppealClick = { appeal ->
                        selectedAppeal = appeal
                        isAppealModalOpen = true
                    }
                )
            }
            
            if (error != null) {
                ErrorSnackbar(error!!)
            }
        }
        
        // Price Reference Form Modal
        PriceReferenceForm(
            isOpen = isPriceFormOpen,
            onClose = { isPriceFormOpen = false },
            onSave = { request ->
                scope.launch {
                    val response = if (selectedPriceReference != null) {
                        apiService.updatePriceReference(selectedPriceReference!!.id, request)
                    } else {
                        apiService.createPriceReference(request)
                    }
                    
                    if (response.success) {
                        isPriceFormOpen = false
                        selectedPriceReference = null
                        materialType = ""
                        baseCost = ""
                        laborRate = ""
                        serviceCategory = ""
                        allowedDeviation = ""
                        loadPriceReferences()
                    } else {
                        error = response.message
                    }
                }
            },
            initialData = selectedPriceReference,
            materialType = materialType,
            onMaterialTypeChange = { materialType = it },
            baseCost = baseCost,
            onBaseCostChange = { baseCost = it },
            laborRate = laborRate,
            onLaborRateChange = { laborRate = it },
            serviceCategory = serviceCategory,
            onServiceCategoryChange = { serviceCategory = it },
            allowedDeviation = allowedDeviation,
            onAllowedDeviationChange = { allowedDeviation = it }
        )
        
        // Evidence Package Modal
        EvidencePackageModal(
            isOpen = isEvidenceModalOpen,
            onClose = { isEvidenceModalOpen = false },
            evidencePackage = evidencePackage
        )
        
        // Appeal Modal
        AppealModal(
            isOpen = isAppealModalOpen,
            onClose = { isAppealModalOpen = false },
            appeal = selectedAppeal,
            onApprove = { notes ->
                scope.launch {
                    val response = apiService.processAppeal(
                        AppealActionRequest(
                            appealId = selectedAppeal!!.id,
                            action = "approve",
                            notes = notes
                        )
                    )
                    
                    if (response.success) {
                        isAppealModalOpen = false
                        selectedAppeal = null
                        loadAppeals()
                    } else {
                        error = response.message
                    }
                }
            },
            onReject = { notes ->
                scope.launch {
                    val response = apiService.processAppeal(
                        AppealActionRequest(
                            appealId = selectedAppeal!!.id,
                            action = "reject",
                            notes = notes
                        )
                    )
                    
                    if (response.success) {
                        isAppealModalOpen = false
                        selectedAppeal = null
                        loadAppeals()
                    } else {
                        error = response.message
                    }
                }
            }
        )
    }
}

@Composable
private fun SCMContent(
    priceReferences: List<PriceReference>,
    securityEvents: List<SecurityEvent>,
    appeals: List<Appeal>,
    onAddPriceReference: () -> Unit,
    onEditPriceReference: (PriceReference) -> Unit,
    onDeletePriceReference: (String) -> Unit,
    onSecurityEventClick: (SecurityEvent) -> Unit,
    onAppealClick: (Appeal) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Price Reference Manager Section
        item {
            SectionHeader("Price Reference Manager")
            PriceReferenceManager(
                priceReferences = priceReferences,
                onAdd = onAddPriceReference,
                onEdit = onEditPriceReference,
                onDelete = onDeletePriceReference
            )
        }
        
        item { Spacer(Modifier.height(16.dp)) }
        
        // Security Events Section
        item {
            SectionHeader("Real-time Incident Monitor")
            SecurityEventsMonitor(
                events = securityEvents,
                onEventClick = onSecurityEventClick
            )
        }
        
        item { Spacer(Modifier.height(16.dp)) }
        
        // Appeals Section
        item {
            SectionHeader("Appeals Workflow")
            AppealsWorkflow(
                appeals = appeals,
                onAppealClick = onAppealClick
            )
        }
        
        item { Spacer(Modifier.height(32.dp)) }
        
        // Market Reference Table Section
        item {
            SectionHeader("Market Reference Table")
            MarketReferenceTable(
                priceReferences = priceReferences
            )
        }
        
        item { Spacer(Modifier.height(16.dp)) }
        
        // Arbitration Inbox Section
        item {
            SectionHeader("Arbitration Inbox")
            ArbitrationInbox(
                appeals = appeals,
                onAppealClick = onAppealClick
            )
        }
        
        item { Spacer(Modifier.height(16.dp)) }
        
        // Global Risk Feed Section
        item {
            SectionHeader("Global Risk Feed")
            GlobalRiskFeed(
                events = securityEvents
            )
        }
        
        item { Spacer(Modifier.height(16.dp)) }
        
        // WORM Viewer Section
        item {
            SectionHeader("WORM Viewer")
            WormViewer(
                onSecurityEventClick = onSecurityEventClick
            )
        }
        
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun PriceReferenceManager(
    priceReferences: List<PriceReference>,
    onAdd: () -> Unit,
    onEdit: (PriceReference) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Price References", style = MaterialTheme.typography.titleSmall)
                Button(onClick = onAdd) {
                    Text("Add Reference")
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            if (priceReferences.isEmpty()) {
                Text("No price references found", color = Color.Gray)
            } else {
                LazyColumn {
                    items(priceReferences) { reference ->
                        PriceReferenceItem(
                            reference = reference,
                            onEdit = { onEdit(reference) },
                            onDelete = { onDelete(reference.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceReferenceItem(
    reference: PriceReference,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(reference.materialType, style = MaterialTheme.typography.bodyMedium)
                Text("Base Cost: $${reference.baseCost}", style = MaterialTheme.typography.bodySmall)
                Text("Labor Rate: $${reference.laborRatePerHour}/hr", style = MaterialTheme.typography.bodySmall)
                Text("Category: ${reference.serviceCategory}", style = MaterialTheme.typography.bodySmall)
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityEventsMonitor(
    events: List<SecurityEvent>,
    onEventClick: (SecurityEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Security Events", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            
            if (events.isEmpty()) {
                Text("No security events found", color = Color.Gray)
            } else {
                LazyColumn {
                    items(events) { event ->
                        SecurityEventItem(event = event, onClick = { onEventClick(event) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityEventItem(
    event: SecurityEvent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val riskColor = when (event.riskLevel) {
                RiskLevel.LOW -> Color(0xFF27AE60)
                RiskLevel.YELLOW -> Color(0xFFF1C40F)
                RiskLevel.RED -> Color(0xFFE74C3C)
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(riskColor, RoundedCornerShape(50))
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("${event.userId} - ${event.service}", style = MaterialTheme.typography.bodyMedium)
                Text("Risk: ${event.riskLevel.name} | Trigger: ${event.trigger}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(event.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(event.timestamp, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Visibility,
                    contentDescription = "View Details"
                )
            }
        }
    }
}

@Composable
private fun AppealsWorkflow(
    appeals: List<Appeal>,
    onAppealClick: (Appeal) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Active Appeals", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            
            if (appeals.isEmpty()) {
                Text("No active appeals found", color = Color.Gray)
            } else {
                LazyColumn {
                    items(appeals) { appeal ->
                        AppealItem(appeal = appeal, onClick = { onAppealClick(appeal) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AppealItem(
    appeal: Appeal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = when (appeal.status) {
                AppealStatus.PENDING -> Color(0xFFF1C40F)
                AppealStatus.APPROVED -> Color(0xFF27AE60)
                AppealStatus.REJECTED -> Color(0xFFE74C3C)
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, RoundedCornerShape(50))
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("User: ${appeal.userId}", style = MaterialTheme.typography.bodyMedium)
                Text("Event: ${appeal.eventId}", style = MaterialTheme.typography.bodySmall)
                Text("Status: ${appeal.status.name}", style = MaterialTheme.typography.bodySmall)
                Text(appeal.reason, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(appeal.submittedAt, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Visibility,
                    contentDescription = "Review Appeal"
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

@Composable
private fun MarketReferenceTable(
    priceReferences: List<PriceReference>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Market Reference Table", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            
            if (priceReferences.isEmpty()) {
                Text("No market references found", color = Color.Gray)
            } else {
                LazyColumn {
                    items(priceReferences) { reference ->
                        MarketReferenceItem(reference = reference)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketReferenceItem(
    reference: PriceReference
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(reference.materialType, style = MaterialTheme.typography.bodyMedium)
                Text("Base Cost: $${reference.baseCost}", style = MaterialTheme.typography.bodySmall)
                Text("Labor Rate: $${reference.laborRatePerHour}/hr", style = MaterialTheme.typography.bodySmall)
                Text("Category: ${reference.serviceCategory}", style = MaterialTheme.typography.bodySmall)
                Text("Allowed Deviation: ${reference.allowedDeviation}%", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ArbitrationInbox(
    appeals: List<Appeal>,
    onAppealClick: (Appeal) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Arbitration Inbox", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            
            if (appeals.isEmpty()) {
                Text("No appeals pending arbitration", color = Color.Gray)
            } else {
                LazyColumn {
                    items(appeals) { appeal ->
                        ArbitrationItem(appeal = appeal, onClick = { onAppealClick(appeal) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ArbitrationItem(
    appeal: Appeal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = when (appeal.status) {
                AppealStatus.PENDING -> Color(0xFFF1C40F)
                AppealStatus.APPROVED -> Color(0xFF27AE60)
                AppealStatus.REJECTED -> Color(0xFFE74C3C)
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, RoundedCornerShape(50))
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("User: ${appeal.userId}", style = MaterialTheme.typography.bodyMedium)
                Text("Event: ${appeal.eventId}", style = MaterialTheme.typography.bodySmall)
                Text("Status: ${appeal.status.name}", style = MaterialTheme.typography.bodySmall)
                Text(appeal.reason, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(appeal.submittedAt, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Gavel,
                    contentDescription = "Arbitrate"
                )
            }
        }
    }
}

@Composable
private fun GlobalRiskFeed(
    events: List<SecurityEvent>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Global Risk Feed", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            
            if (events.isEmpty()) {
                Text("No security events to display", color = Color.Gray)
            } else {
                LazyColumn {
                    items(events) { event ->
                        RiskFeedItem(event = event)
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskFeedItem(
    event: SecurityEvent
) {
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
            val riskColor = when (event.riskLevel) {
                RiskLevel.LOW -> Color(0xFF27AE60)
                RiskLevel.YELLOW -> Color(0xFFF1C40F)
                RiskLevel.RED -> Color(0xFFE74C3C)
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(riskColor, RoundedCornerShape(50))
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("${event.userId} - ${event.service}", style = MaterialTheme.typography.bodyMedium)
                Text("Risk: ${event.riskLevel.name} | Trigger: ${event.trigger}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(event.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(event.timestamp, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun WormViewer(
    onSecurityEventClick: (SecurityEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("WORM Viewer", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            Text("View immutable audit logs and evidence packages", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            
            Button(onClick = { /* Open WORM viewer */ }) {
                Text("Open WORM Viewer")
            }
        }
    }
}
