package com.logi.admin.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.logi.admin.kmp.model.PriceReference
import com.logi.admin.kmp.model.PriceReferenceRequest
import com.logi.admin.kmp.model.PriceReferenceResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceReferenceForm(
    isOpen: Boolean,
    onClose: () -> Unit,
    onSave: (PriceReferenceRequest) -> Unit,
    initialData: PriceReference? = null,
    materialType: String,
    onMaterialTypeChange: (String) -> Unit,
    baseCost: String,
    onBaseCostChange: (String) -> Unit,
    laborRate: String,
    onLaborRateChange: (String) -> Unit,
    serviceCategory: String,
    onServiceCategoryChange: (String) -> Unit,
    allowedDeviation: String,
    onAllowedDeviationChange: (String) -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(if (initialData != null) "Edit Price Reference" else "Add Price Reference")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = materialType,
                    onValueChange = onMaterialTypeChange,
                    label = { Text("Material Type") },
                    placeholder = { Text("Enter material type") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = baseCost,
                    onValueChange = onBaseCostChange,
                    label = { Text("Base Cost") },
                    placeholder = { Text("Enter base cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = laborRate,
                    onValueChange = onLaborRateChange,
                    label = { Text("Labor Rate per Hour") },
                    placeholder = { Text("Enter labor rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = serviceCategory,
                    onValueChange = onServiceCategoryChange,
                    label = { Text("Service Category") },
                    placeholder = { Text("Enter service category") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = allowedDeviation,
                    onValueChange = onAllowedDeviationChange,
                    label = { Text("Allowed Deviation (%)") },
                    placeholder = { Text("Enter allowed deviation") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val request = PriceReferenceRequest(
                            materialType = materialType,
                            baseCost = baseCost.toDoubleOrNull() ?: 0.0,
                            laborRatePerHour = laborRate.toDoubleOrNull() ?: 0.0,
                            serviceCategory = serviceCategory,
                            allowedDeviation = allowedDeviation.toDoubleOrNull() ?: 0.0
                        )
                        onSave(request)
                    } catch (e: Exception) {
                        // Handle validation error
                    }
                },
                enabled = materialType.isNotEmpty() && 
                         baseCost.isNotEmpty() && 
                         laborRate.isNotEmpty() && 
                         serviceCategory.isNotEmpty() &&
                         allowedDeviation.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}