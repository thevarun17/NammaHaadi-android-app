package com.nammahaadi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nammahaadi.app.data.model.User
import com.nammahaadi.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(viewModel: AppViewModel) {
    val users by viewModel.users.collectAsState()
    val sorted = users.sortedByDescending { it.points }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Road Champions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Podium Section for Top 3
            item {
                if (sorted.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // 2nd Place
                            if (sorted.size >= 2) {
                                PodiumItem(sorted[1], rank = 2, height = 90.dp)
                            } else {
                                Spacer(Modifier.width(100.dp))
                            }
                            
                            // 1st Place
                            if (sorted.size >= 1) {
                                PodiumItem(sorted[0], rank = 1, height = 130.dp)
                            }
                            
                            // 3rd Place
                            if (sorted.size >= 3) {
                                PodiumItem(sorted[2], rank = 3, height = 70.dp)
                            } else {
                                Spacer(Modifier.width(100.dp))
                            }
                        }
                    }
                }
            }

            // Top 5 Highlighted List (Ranks 4 and 5, or more if Podium was smaller)
            val othersStart = if (sorted.size >= 3) 3 else sorted.size
            itemsIndexed(sorted.drop(othersStart)) { idx, user ->
                val rank = idx + othersStart + 1
                LeaderboardRow(
                    user = user, 
                    rank = rank, 
                    isTopFive = rank <= 5
                )
            }
            
            if (sorted.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No champions yet. Start reporting!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumItem(user: User, rank: Int, height: androidx.compose.ui.unit.Dp) {
    val medal = when (rank) { 1 -> "🥇"; 2 -> "🥈"; else -> "🥉" }
    val color = when (rank) { 
        1 -> Color(0xFFFFD700) 
        2 -> Color(0xFFC0C0C0) 
        else -> Color(0xFFCD7F32) 
    }
    val avatarSize = if (rank == 1) 72.dp else 56.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(110.dp)) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(3.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!user.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        (user.displayName.takeIf { it.isNotBlank() } ?: user.name.takeIf { it.isNotBlank() } ?: "U").first().toString().uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            Text(
                medal, 
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp),
                fontSize = 20.sp
            )
        }
        
        Spacer(Modifier.height(8.dp))
        Text(
            (user.displayName.takeIf { it.isNotBlank() } ?: user.name).split(" ").first(), 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.ExtraBold, 
            maxLines = 1
        )
        Text("${user.points} pts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        
        Spacer(Modifier.height(8.dp))
        
        // The Podium Base
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.05f))
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                "#$rank", 
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.titleMedium, 
                color = color, 
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun LeaderboardRow(user: User, rank: Int, isTopFive: Boolean) {
    val containerColor = if (isTopFive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.White
    val borderColor = if (isTopFive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isTopFive) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rank Number
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#$rank", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = if (isTopFive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!user.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        (user.displayName.takeIf { it.isNotBlank() } ?: user.name.takeIf { it.isNotBlank() } ?: "U").first().toString().uppercase(), 
                        style = MaterialTheme.typography.titleMedium, 
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Name and Village
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.displayName.takeIf { it.isNotBlank() } ?: user.name, 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold
                )
                if (user.village.isNotBlank()) {
                    Text(user.village, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Points
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${user.points}", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.primary
                )
                Text("PTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
