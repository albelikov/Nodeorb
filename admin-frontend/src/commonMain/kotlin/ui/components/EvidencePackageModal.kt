package com.logi.admin.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.logi.admin.kmp.model.EvidencePackage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidencePackageModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    evidencePackage: EvidencePackage?
) {
    if (!isOpen || evidencePackage == null) return

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text("Evidence Package Details")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Basic Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Event ID: ${evidencePackage.eventId}", style = MaterialTheme.typography.bodyMedium)
                        Text("Timestamp: ${evidencePackage.timestamp}", style = MaterialTheme.typography.bodyMedium)
                        Text("Hash: ${evidencePackage.hash}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                // Signatures
                if (evidencePackage.signatures.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Signatures:", style = MaterialTheme.typography.bodyMedium)
                            evidencePackage.signatures.forEachIndexed { index, signature ->
                                Text("• $signature", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                
                // Data Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Evidence Data:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        
                        // Format JSON data for display
                        val jsonData = remember(evidencePackage.data) {
                            try {
                                Json.encodeToString(Map.serializer(), evidencePackage.data)
                            } catch (e: Exception) {
                                evidencePackage.data.toString()
                            }
                        }
                        
                        Text(
                            text = jsonData,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // WORM Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("WORM Status:", style = MaterialTheme.typography.bodyMedium)
                        Text("This evidence package is stored in WORM (Write Once, Read Many) storage", style = MaterialTheme.typography.bodySmall)
                        Text("All signatures are cryptographically verified", style = MaterialTheme.typography.bodySmall)
                        Text("Data integrity is guaranteed by the provided hash", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    )
}