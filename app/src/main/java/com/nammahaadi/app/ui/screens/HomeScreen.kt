package com.nammahaadi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nammahaadi.app.data.model.*
import com.nammahaadi.app.ui.theme.*
import com.nammahaadi.app.ui.utils.safeParseColor
import com.nammahaadi.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel, onViewOnMap: (String) -> Unit) {
    val reports by viewModel.reports.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    var showUpdateDialog by remember { mutableStateOf<RoadReport?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentUser?.photoUrl != null) {
                                AsyncImage(
                                    model = currentUser?.photoUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    (currentUser?.displayName ?: currentUser?.name ?: "U").first().toString().uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (currentUser != null) "Hey ${currentUser?.displayName ?: currentUser?.name}," else "Namma Haadi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "What do you want to report today?",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip("${summary.total} Total", Icons.Default.Warning, Primary, Modifier.weight(1f))
                    StatChip("${summary.active} Active", Icons.Default.FiberManualRecord, Color(0xFFE53935), Modifier.weight(1f))
                    StatChip("${summary.inProgress} Working", Icons.Default.Build, Color(0xFFF57C00), Modifier.weight(1f))
                    StatChip("${summary.resolved} Fixed", Icons.Default.CheckCircle, Color(0xFF2E7D32), Modifier.weight(1f))
                }
            }

            // Type filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { viewModel.setTypeFilter(null) },
                            label = { Text("All Types") }
                        )
                    }
                    items(IssueType.values().filter { it != IssueType.OTHER }) { type ->
                        FilterChip(
                            selected = selectedType == type.key,
                            onClick = { viewModel.setTypeFilter(if (selectedType == type.key) null else type.key) },
                            label = { Text("${type.emoji} ${type.label}") }
                        )
                    }
                }
            }

            // Status filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { viewModel.setStatusFilter(null) },
                            label = { Text("All Status") }
                        )
                    }
                    items(ReportStatus.values()) { status ->
                        FilterChip(
                            selected = selectedStatus == status.key,
                            onClick = { viewModel.setStatusFilter(if (selectedStatus == status.key) null else status.key) },
                            label = { Text(status.label) }
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (reports.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛣️", style = MaterialTheme.typography.headlineLarge)
                            Text("No issues found", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                            Text("All clear in your area!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(reports, key = { it.id }) { report ->
                    IssueCard(
                        report = report,
                        onUpdateStatus = { showUpdateDialog = report },
                        onViewOnMap = { onViewOnMap(report.id) }
                    )
                }
            }
        }
    }

    showUpdateDialog?.let { report ->
        UpdateStatusDialog(
            report = report,
            viewModel = viewModel,
            onDismiss = { showUpdateDialog = null }
        )
    }
}

@Composable
fun StatChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun IssueCard(
    report: RoadReport,
    onUpdateStatus: () -> Unit,
    onViewOnMap: () -> Unit
) {
    val issueType = IssueType.fromKey(report.type)
    val status = ReportStatus.fromKey(report.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    TypeBadge(issueType)
                    StatusBadge(status)
                }
                SeverityBadge(Severity.fromKey(report.severity))
            }

            Spacer(Modifier.height(8.dp))
            Text(report.title, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(" ${report.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            if (!report.description.isNullOrBlank()) {
                Text(report.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(" ${report.reportedByName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(" · ${report.reportCount} report${if (report.reportCount == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onUpdateStatus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text("🚩 Update Status", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                    onClick = onViewOnMap,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View on Map", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun TypeBadge(type: IssueType) {
    val color = safeParseColor(type.colorHex)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text("${type.emoji} ${type.label}", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusBadge(status: ReportStatus) {
    val (bg, textColor) = when (status) {
        ReportStatus.ACTIVE -> ActiveBg to ActiveText
        ReportStatus.IN_PROGRESS -> InProgressBg to InProgressText
        ReportStatus.RESOLVED -> ResolvedBg to ResolvedText
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(status.label, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SeverityBadge(severity: Severity) {
    val (bg, text) = when (severity) {
        Severity.LOW -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        Severity.MEDIUM -> Color(0xFFFFF9C4) to Color(0xFFF9A825)
        Severity.HIGH -> Color(0xFFFFE0B2) to Color(0xFFE65100)
        Severity.CRITICAL -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(severity.label, style = MaterialTheme.typography.labelSmall, color = text, fontWeight = FontWeight.Bold)
    }
}
