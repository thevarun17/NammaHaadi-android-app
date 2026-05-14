package com.nammahaadi.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nammahaadi.app.data.model.*
import com.nammahaadi.app.viewmodel.AppViewModel
import com.nammahaadi.app.viewmodel.SubmitState

@Composable
fun UpdateStatusDialog(
    report: RoadReport,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val submitState by viewModel.updateSubmitState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedStatus by remember { mutableStateOf(ReportStatus.fromKey(report.status)) }
    var note by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf("") }

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            viewModel.resetUpdateSubmitState()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text("Update Issue Status", style = MaterialTheme.typography.titleLarge)
                Text(report.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Status selector
                Text("New Status", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportStatus.values().forEach { status ->
                        val selected = selectedStatus == status
                        val (bg, textColor) = when (status) {
                            ReportStatus.ACTIVE -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                            ReportStatus.IN_PROGRESS -> Color(0xFFFFF8E1) to Color(0xFFF57C00)
                            ReportStatus.RESOLVED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { selectedStatus = status },
                            label = { Text(status.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Location
                OutlinedTextField(
                    value = locationText,
                    onValueChange = {
                        locationText = it
                        locationError = if (it.isNotBlank() && parseCoords(it) == null)
                            "e.g. 12.9716, 77.5946" else ""
                    },
                    label = { Text("Location (optional)") },
                    placeholder = { Text("12.9716, 77.5946") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    isError = locationError.isNotEmpty(),
                    supportingText = if (locationError.isNotEmpty()) ({ Text(locationError) }) else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("Describe the current situation...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                if (submitState is SubmitState.Error) {
                    Text((submitState as SubmitState.Error).message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val coords = if (locationText.isBlank()) null else parseCoords(locationText)
                    if (locationText.isNotBlank() && coords == null) {
                        locationError = "Enter valid coordinates"
                        return@Button
                    }
                    viewModel.submitUpdate(
                        CreateUpdateRequest(
                            reportId = report.id,
                            reportedBy = currentUser?.externalId ?: "unknown",
                            reportedByName = currentUser?.displayName ?: currentUser?.name ?: "User",
                            newStatus = selectedStatus.key,
                            note = note.takeIf { it.isNotBlank() },
                            lat = coords?.first,
                            lng = coords?.second,
                            photoUrl = null
                        )
                    )
                },
                enabled = submitState !is SubmitState.Loading,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (submitState is SubmitState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Submit Update")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun parseCoords(text: String): Pair<Double, Double>? {
    val parts = text.trim().replace(Regex("[()]"), "").split(Regex("[,\\s]+")).filter { it.isNotEmpty() }
    if (parts.size < 2) return null
    val lat = parts[0].toDoubleOrNull() ?: return null
    val lng = parts[1].toDoubleOrNull() ?: return null
    if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return null
    return lat to lng
}
