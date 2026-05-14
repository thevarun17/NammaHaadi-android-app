package com.nammahaadi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nammahaadi.app.data.model.*
import com.nammahaadi.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: AppViewModel) {
    val alerts by viewModel.alerts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Road Alerts") },
                actions = {
                    IconButton(onClick = { viewModel.loadAlerts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alert", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (alerts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔔", style = MaterialTheme.typography.headlineLarge)
                            Text("No active alerts", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                            Text("Roads are clear!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(alerts, key = { it.id }) { alert ->
                    AlertCard(alert = alert)
                }
            }
        }
    }

    if (showAddDialog) {
        AddAlertDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@Composable
fun AlertCard(alert: Alert) {
    val (bg, border, icon, iconTint) = when (alert.severity) {
        "DANGER" -> listOf(Color(0xFFFFEBEE), Color(0xFFEF9A9A), "🚨", Color(0xFFC62828))
        "WARNING" -> listOf(Color(0xFFFFF8E1), Color(0xFFFFCC80), "⚠️", Color(0xFFF57C00))
        else -> listOf(Color(0xFFE3F2FD), Color(0xFF90CAF9), "ℹ️", Color(0xFF1565C0))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg as Color),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(icon as String, style = MaterialTheme.typography.headlineSmall)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(alert.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background((iconTint as Color).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(alert.severity, style = MaterialTheme.typography.labelSmall, color = iconTint, fontWeight = FontWeight.Bold)
                    }
                }
                Text(alert.message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (alert.area.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(" ${alert.area}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text("· ${alert.reportedByName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("INFO") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Broadcast Alert") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("INFO", "WARNING", "DANGER").forEach { sev ->
                        FilterChip(
                            selected = severity == sev,
                            onClick = { severity = sev },
                            label = { Text(sev, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message *") }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(10.dp), maxLines = 3)
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area") }, placeholder = { Text("e.g. Koramangala") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        viewModel.submitAlert(
                            CreateAlertRequest(
                                title.trim(),
                                message.trim(),
                                severity,
                                area.trim(),
                                currentUser?.externalId ?: "unknown",
                                currentUser?.displayName ?: currentUser?.name ?: "User"
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && message.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Broadcast") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
