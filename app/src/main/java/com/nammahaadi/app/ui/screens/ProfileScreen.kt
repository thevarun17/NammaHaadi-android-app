package com.nammahaadi.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nammahaadi.app.ui.theme.Primary
import com.nammahaadi.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AppViewModel, onLogout: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRank by viewModel.currentRank.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.uploadProfilePicture(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    IconButton(onClick = { currentUser?.externalId?.let { viewModel.loadCurrentUser(it) } }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Primary)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.photoUrl != null) {
                            AsyncImage(
                                model = currentUser?.photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                (currentUser?.displayName ?: currentUser?.name ?: "U").first().toString().uppercase(),
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text(currentUser?.displayName ?: currentUser?.name ?: "User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(currentUser?.phone ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(" ${currentUser?.village.takeIf { !it.isNullOrBlank() } ?: "Set your location"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStat("${currentUser?.points ?: 0}", "Points", Icons.Default.Star, Color(0xFFFFD700))
                        ProfileStat("#${currentRank.takeIf { it != 0 } ?: "-"}", "Rank", Icons.Default.EmojiEvents, Primary)
                        ProfileStat("${currentUser?.reportsCount ?: 0}", "Reports", Icons.Default.Warning, Color(0xFFE53935))
                        ProfileStat("${currentUser?.updatesCount ?: 0}", "Updates", Icons.Default.Update, Color(0xFF1E88E5))
                    }
                }
            }

            // Personal Info Section
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Personal Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { showEditDialog = true }) {
                            Text("Edit")
                        }
                    }
                    InfoRow(Icons.Default.Home, "Address", currentUser?.address.takeIf { !it.isNullOrBlank() } ?: "Not set")
                    InfoRow(Icons.Default.Info, "Bio", currentUser?.personalInfo.takeIf { !it.isNullOrBlank() } ?: "Tell us about yourself")
                }
            }

            // Badge section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Badges", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                    
                    val badgeEmojis = mapOf(
                        "First Report" to "🗺️",
                        "Active Reporter" to "⭐",
                        "Road Guardian" to "🏆",
                        "Update Pro" to "🔄",
                        "50 Points Club" to "💎"
                    )

                    val userBadges = currentUser?.badges ?: emptyList()
                    
                    if (userBadges.isEmpty()) {
                        Text("🌱 Getting Started", style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            userBadges.forEach { badgeName ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(badgeEmojis[badgeName] ?: "🏅", fontSize = 28.sp)
                                    Text(badgeName, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Sign Out
            OutlinedButton(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            isLoading = isLoading,
            onDismiss = { showEditDialog = false },
            onSave = { updates ->
                viewModel.updateUserProfile(updates)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentUser: com.nammahaadi.app.data.model.User?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var village by remember { mutableStateOf(currentUser?.village ?: "") }
    var address by remember { mutableStateOf(currentUser?.address ?: "") }
    var personalInfo by remember { mutableStateOf(currentUser?.personalInfo ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Display Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village/Area") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = personalInfo, onValueChange = { personalInfo = it }, label = { Text("Bio / Personal Info") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updates = mutableMapOf<String, Any>(
                        "displayName" to displayName,
                        "village" to village,
                        "address" to address,
                        "personalInfo" to personalInfo
                    )
                    onSave(updates)
                },
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ProfileStat(value: String, label: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
