package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Professional Settings states
    var isAutoplayEnabled by remember { mutableStateOf(true) }
    var isWifiOnlyDownload by remember { mutableStateOf(true) }
    var isBackgroundPlayEnabled by remember { mutableStateOf(false) }
    var isSubtitlesEnabled by remember { mutableStateOf(true) }
    var streamQuality by remember { mutableStateOf("1080p (Full HD)") }
    var audioLanguage by remember { mutableStateOf("Bengali (বাংলা)") }
    
    var cacheSizeMb by remember { mutableStateOf(142) }
    var isClearingCache by remember { mutableStateOf(false) }

    LaunchedEffect(isClearingCache) {
        if (isClearingCache) {
            kotlinx.coroutines.delay(1500)
            cacheSizeMb = 0
            isClearingCache = false
            Toast.makeText(context, "Storage cache cleared completely!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F13))
            )
        },
        containerColor = Color(0xFF0F0F13)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Video Playback & Quality
            SettingsSectionHeader(title = "Video Playback")
            
            SettingsSelectionCard(
                icon = Icons.Default.HighQuality,
                title = "Preferred Stream Quality",
                value = streamQuality,
                options = listOf("Auto (Recommended)", "Data Saver (480p)", "Standard (720p)", "1080p (Full HD)", "4K Ultra HD"),
                onSelect = { streamQuality = it }
            )

            SettingsSelectionCard(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Primary Audio Language",
                value = audioLanguage,
                options = listOf("Bengali (বাংলা)", "English (UK/US)", "Hindi (हिन्दी)", "Spanish (Español)"),
                onSelect = { audioLanguage = it }
            )

            SettingsToggleRow(
                icon = Icons.Default.PlayCircle,
                title = "Autoplay Next Episode",
                subtitle = "Automatically starts playing the next episode in series",
                checked = isAutoplayEnabled,
                onCheckedChange = { isAutoplayEnabled = it }
            )

            SettingsToggleRow(
                icon = Icons.Default.Subtitles,
                title = "Always Enable Subtitles",
                subtitle = "Loads default subtitle tracks when available",
                checked = isSubtitlesEnabled,
                onCheckedChange = { isSubtitlesEnabled = it }
            )

            Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

            // Section 2: Downloads & Sync
            SettingsSectionHeader(title = "Downloads & Storage")

            SettingsToggleRow(
                icon = Icons.Default.Wifi,
                title = "Download Over Wi-Fi Only",
                subtitle = "Saves mobile data usage by delaying video downloads",
                checked = isWifiOnlyDownload,
                onCheckedChange = { isWifiOnlyDownload = it }
            )

            SettingsToggleRow(
                icon = Icons.Default.MusicNote,
                title = "Audio Background Playback",
                subtitle = "Keep listening to the audio stream even when screen is locked",
                checked = isBackgroundPlayEnabled,
                onCheckedChange = { isBackgroundPlayEnabled = it }
            )

            // Cache management card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Clear Cached Storage", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Currently using ${cacheSizeMb} MB", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = { isClearingCache = true },
                        enabled = cacheSizeMb > 0 && !isClearingCache,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300),
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isClearingCache) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Clear", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

            // Section 3: App Information
            SettingsSectionHeader(title = "App Information")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppInfoRow(label = "Application Version", value = "v3.2.5-cinema (Premium)")
                    AppInfoRow(label = "Build Date", value = "June 2026")
                    AppInfoRow(label = "Developer Team", value = "MatMovies Studio")
                    AppInfoRow(label = "Status", value = "Certified Stable (M3)")
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFFFB300),
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Color(0xFFFFB300),
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Composable
fun SettingsSelectionCard(
    icon: ImageVector,
    title: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text(value, color = Color(0xFFFFB300), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1E1E24))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = if (option == value) Color(0xFFFFB300) else Color.White) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AppInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
