package com.nammahaadi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.nammahaadi.app.data.model.*
import com.nammahaadi.app.ui.utils.safeParseColor
import com.nammahaadi.app.viewmodel.AppViewModel
import com.nammahaadi.app.viewmodel.SubmitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: AppViewModel, onSuccess: () -> Unit) {
    val submitState by viewModel.reportSubmitState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedType by remember { mutableStateOf<IssueType?>(null) }
    var selectedSeverity by remember { mutableStateOf(Severity.MEDIUM) }
    var title by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var latLng by remember { mutableStateOf<LatLng?>(null) }
    var coordText by remember { mutableStateOf("") }
    var coordError by remember { mutableStateOf("") }

    val defaultLatLng = LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 12f)
    }

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            viewModel.resetReportSubmitState()
            onSuccess()
        }
    }

    // Animate map when coordinate text is parsed
    LaunchedEffect(latLng) {
        latLng?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report an Issue") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Map pin picker ───────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📍 Pin Location",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    // Google Map — tap to drop a pin
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = true,
                                mapToolbarEnabled = false
                            ),
                            onMapClick = { tapped ->
                                latLng = tapped
                                coordText = "%.6f, %.6f".format(tapped.latitude, tapped.longitude)
                                coordError = ""
                            }
                        ) {
                            latLng?.let { pos ->
                                Marker(
                                    state = MarkerState(position = pos),
                                    title = "Selected Location"
                                )
                            }
                        }

                        // Tap hint overlay
                        if (latLng == null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Tap map to drop a pin",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Coordinate text field (alternative to tapping)
                    OutlinedTextField(
                        value = coordText,
                        onValueChange = { text ->
                            coordText = text
                            val parsed = if (text.isBlank()) null else parseCoordText(text)
                            when {
                                text.isBlank() -> {
                                    coordError = ""; latLng = null
                                }
                                parsed != null -> {
                                    coordError = ""; latLng = parsed
                                }
                                else -> coordError = "Enter: 12.9716, 77.5946"
                            }
                        },
                        label = { Text("Paste or type coordinates") },
                        placeholder = { Text("12.9716, 77.5946") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn, null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        isError = coordError.isNotEmpty(),
                        supportingText = when {
                            coordError.isNotEmpty() -> ({ Text(coordError) })
                            latLng != null -> ({ Text("✓ Location set", color = Color(0xFF2E7D32)) })
                            else -> ({
                                Text("Tap the map above, or long-press in Google Maps → copy coords")
                            })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // ── Issue details form ───────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Issue Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Issue type grid
                    Column {
                        Text(
                            "Issue Type *",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val types = IssueType.values().filter { it != IssueType.OTHER }
                        for (row in types.chunked(3)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { type ->
                                    val selected = selectedType == type
                                    val color = safeParseColor(type.colorHex)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (selected) color.copy(0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) color else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedType = type }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                type.emoji,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                type.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                repeat(3 - row.size) { Box(modifier = Modifier.weight(1f)) }
                            }
                            if (row != types.chunked(3).last()) Spacer(Modifier.height(8.dp))
                        }
                    }

                    // Severity chips
                    Column {
                        Text(
                            "Severity *",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Severity.values().forEach { sev ->
                                FilterChip(
                                    selected = selectedSeverity == sev,
                                    onClick = { selectedSeverity = sev },
                                    label = {
                                        Text(sev.label, style = MaterialTheme.typography.labelSmall)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title *") },
                        placeholder = { Text("e.g. Deep pothole near BMTC stop") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address *") },
                        placeholder = { Text("e.g. MG Road, near Trinity Metro, Bengaluru") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Describe the issue in detail...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    if (submitState is SubmitState.Error) {
                        Text(
                            (submitState as SubmitState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    val canSubmit = selectedType != null
                            && title.isNotBlank()
                            && address.isNotBlank()
                            && latLng != null
                            && submitState !is SubmitState.Loading

                    Button(
                        onClick = {
                            viewModel.submitReport(
                                CreateReportRequest(
                                    type = selectedType!!.key,
                                    severity = selectedSeverity.key,
                                    title = title.trim(),
                                    address = address.trim(),
                                    description = description.trim().takeIf { it.isNotBlank() },
                                    lat = latLng!!.latitude,
                                    lng = latLng!!.longitude,
                                    reportedBy = currentUser?.externalId ?: "unknown",
                                    reportedByName = currentUser?.displayName ?: currentUser?.name ?: "User",
                                    photoUrl = null
                                )
                            )
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (submitState is SubmitState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Submit Report", style = MaterialTheme.typography.titleSmall)
                        }
                    }

                    if (!canSubmit && submitState !is SubmitState.Loading) {
                        val missing = buildList {
                            if (selectedType == null) add("issue type")
                            if (title.isBlank()) add("title")
                            if (address.isBlank()) add("address")
                            if (latLng == null) add("location (tap map or paste coords)")
                        }
                        if (missing.isNotEmpty()) {
                            Text(
                                "Fill in: ${missing.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun parseCoordText(text: String): LatLng? {
    val parts = text.trim()
        .replace(Regex("[()]"), "")
        .split(Regex("[,\\s]+"))
        .filter { it.isNotEmpty() }
    if (parts.size < 2) return null
    val lat = parts[0].toDoubleOrNull() ?: return null
    val lng = parts[1].toDoubleOrNull() ?: return null
    if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return null
    return LatLng(lat, lng)
}
