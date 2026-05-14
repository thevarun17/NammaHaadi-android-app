package com.nammahaadi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.nammahaadi.app.data.model.*
import com.nammahaadi.app.ui.theme.Primary
import com.nammahaadi.app.ui.utils.safeParseColor
import com.nammahaadi.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: AppViewModel, selectedReportId: String?) {
    val reports by viewModel.reports.collectAsState()

    val selectedReport = selectedReportId?.let { id -> reports.find { it.id == id } }
    val displayReports = if (selectedReport != null) listOf(selectedReport) else reports

    var showUpdateDialog by remember { mutableStateOf<RoadReport?>(null) }
    var showLegend by remember { mutableStateOf(true) }

    // Default camera position — Bengaluru centre
    val defaultPosition = LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 11f)
    }

    // Animate camera when a specific report is selected
    LaunchedEffect(selectedReport) {
        if (selectedReport != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(selectedReport.lat, selectedReport.lng), 16f
                )
            )
        } else if (displayReports.isNotEmpty()) {
            val avgLat = displayReports.map { it.lat }.average()
            val avgLng = displayReports.map { it.lng }.average()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(avgLat, avgLng), 11f)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Google Map ───────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true,
                mapToolbarEnabled = true
            ),
            properties = MapProperties(
                isMyLocationEnabled = false // set true after location permission granted
            )
        ) {
            displayReports.forEach { report ->
                val issueType = IssueType.fromKey(report.type)
                // Colour-code markers by severity
                val hue = when (report.severity) {
                    "CRITICAL" -> BitmapDescriptorFactory.HUE_RED
                    "HIGH" -> BitmapDescriptorFactory.HUE_ORANGE
                    "MEDIUM" -> BitmapDescriptorFactory.HUE_YELLOW
                    else -> BitmapDescriptorFactory.HUE_GREEN
                }
                Marker(
                    state = MarkerState(position = LatLng(report.lat, report.lng)),
                    title = "${issueType.emoji} ${report.title}",
                    snippet = "${report.address} · ${ReportStatus.fromKey(report.status).label}",
                    icon = BitmapDescriptorFactory.defaultMarker(hue),
                    onClick = {
                        showUpdateDialog = report
                        false // return false to show default info window too
                    }
                )
            }
        }

        // ── Selected report pill ─────────────────────────────────────────────
        if (selectedReport != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Primary)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(IssueType.fromKey(selectedReport.type).emoji)
                    Text(
                        selectedReport.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        // ── Legend ───────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .widthIn(min = 160.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Issue Types",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { showLegend = !showLegend },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                if (showLegend) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (showLegend) {
                        Spacer(Modifier.height(6.dp))
                        IssueType.values().forEach { type ->
                            val count = reports.count { it.type == type.key }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(safeParseColor(type.colorHex))
                                )
                                Text(
                                    "${type.emoji} ${type.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "$count",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Realtime badge ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Text(
                    "Live · ${displayReports.size} issues",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
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
