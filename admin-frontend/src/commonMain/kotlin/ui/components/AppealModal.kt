package com.logi.admin.kmp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.logi.admin.kmp.model.Appeal
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppealModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    appeal: Appeal?,
    onApprove: (String?) -> Unit,
    onReject: (String?) -> Unit
) {
    if (!isOpen || appeal == null) return

    var notes by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Review Appeal")
                Text(
                    text = appeal.status.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        color = when (appeal.status) {
                            com.logi.admin.kmp.model.AppealStatus.PENDING -> androidx.compose.ui.graphics.Color(0xFFF1C40F)
                            com.logi.admin.kmp.model.AppealStatus.APPROVED -> androidx.compose.ui.graphics.Color(0xFF27AE60)
                            com.logi.admin.kmp.model.AppealStatus.REJECTED -> androidx.compose.ui.graphics.Color(0xFFE74C3C)
                        }
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Appeal Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("User ID: ${appeal.userId}", style = MaterialTheme.typography.bodyMedium)
                        Text("Event ID: ${appeal.eventId}", style = MaterialTheme.typography.bodyMedium)
                        Text("Submitted: ${appeal.submittedAt}", style = MaterialTheme.typography.bodySmall)
                        appeal.reviewedAt?.let {
                            Text("Reviewed: $it", style = MaterialTheme.typography.bodySmall)
                        }
                        appeal.reviewedBy?.let {
                            Text("Reviewed By: $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                // Appeal Reason
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Appeal Reason:", style = MaterialTheme.typography.bodyMedium)
                        Text(appeal.reason, style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                // Evidence Photo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Evidence Photo:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        
                        // Image loading with Landscapist
                        GlideImage(
                            imageModel = { appeal.evidencePhotoUrl },
                            imageOptions = ImageOptions(
                                contentDescription = "Evidence Photo",
                                alignment = Alignment.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                
                // Review Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Review Notes (Optional)") },
                    placeholder = { Text("Enter your review notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isProcessing = true
                        onReject(notes.ifEmpty { null })
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFE74C3C),
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject")
                }
                
                Button(
                    onClick = {
                        isProcessing = true
                        onApprove(notes.ifEmpty { null })
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF27AE60),
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Approve")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onClose,
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
        }
    )
}