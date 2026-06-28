package com.example.ui.screens

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MovieViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    movieId: Int,
    navController: NavController,
    movieViewModel: MovieViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val currentUser by authViewModel.currentUser.collectAsState()
    val movie by movieViewModel.selectedMovie.collectAsState()

    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerInstance by remember { mutableStateOf<MediaPlayer?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentProgressMs by remember { mutableStateOf(0L) }
    var totalDurationMs by remember { mutableStateOf(0L) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var isControlsVisible by remember { mutableStateOf(true) }

    var selectedSubtitles by remember { mutableStateOf("None") }
    var selectedQuality by remember { mutableStateOf("1080p") }

    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    var savedProgressMs by remember { mutableStateOf(-1L) }

    LaunchedEffect(movieId, currentUser) {
        val historyItem = movieViewModel.getWatchHistoryItem(movieId)
        if (historyItem != null) {
            savedProgressMs = historyItem.progressMs
            currentProgressMs = historyItem.progressMs
        } else {
            savedProgressMs = 0L
        }
    }

    val latestProgress = rememberUpdatedState(currentProgressMs)
    val latestDuration = rememberUpdatedState(totalDurationMs)

    // Lock orientation to Landscape for maximum cinematic streaming experience!
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            // Save final watch progress!
            if (latestProgress.value > 0 && latestDuration.value > 0) {
                movieViewModel.saveWatchProgress(movieId, latestProgress.value, latestDuration.value)
            }
        }
    }

    LaunchedEffect(movieId) {
        movieViewModel.setActiveUser(currentUser?.id)
        movieViewModel.selectMovie(movieId)
    }

    // Auto-save watch progress to local database every 4 seconds!
    LaunchedEffect(isPlaying, currentProgressMs) {
        if (isPlaying && totalDurationMs > 0) {
            delay(4000)
            movieViewModel.saveWatchProgress(movieId, currentProgressMs, totalDurationMs)
        }
    }

    // Progress poller
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewInstance?.let { vv ->
                currentProgressMs = vv.currentPosition.toLong()
                totalDurationMs = vv.duration.toLong()
            }
            delay(500)
        }
    }

    // Auto-hide controls after 5 seconds
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(5000)
            isControlsVisible = false
        }
    }

    if (movie == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val currentMovie = movie!!

    // Back button handler to restore unspecified orientation
    BackHandler {
        videoViewInstance?.stopPlayback()
        navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { isControlsVisible = !isControlsVisible }
    ) {
        // Native VideoView Wrap for highly performant and stable video streaming
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(currentMovie.videoUrl))
                    setOnPreparedListener { mp ->
                        mediaPlayerInstance = mp
                        totalDurationMs = duration.toLong()
                        
                        // Seek to the previously saved progress if any
                        if (savedProgressMs > 0) {
                            seekTo(savedProgressMs.toInt())
                        }
                        
                        mp.start()
                        isPlaying = true
                        
                        // Set playback speed
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                        }
                    }
                    setOnCompletionListener {
                        isPlaying = false
                    }
                }
            },
            update = { view ->
                videoViewInstance = view
            },
            modifier = Modifier.fillMaxSize()
        )

        // Subtitle Overlay text (Simulated caption syncing)
        if (selectedSubtitles != "None" && isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = getSubtitleTextForProgress(currentProgressMs, currentMovie.title),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Overlay Playback Controls (Animated visibility)
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top Toolbar (Back, Movie Title, Subtitle, Quality)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                videoViewInstance?.stopPlayback()
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = currentMovie.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Streaming • ${selectedQuality} Premium UHD",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Subtitle trigger
                        IconButton(
                            onClick = { showSubtitleDialog = true },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                        }

                        // Quality trigger
                        IconButton(
                            onClick = { showQualityDialog = true },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Quality", tint = Color.White)
                        }
                    }
                }

                // Middle Buttons: Rewind, Play/Pause, Fast Forward
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            videoViewInstance?.let { vv ->
                                val target = (vv.currentPosition - 10000).coerceAtLeast(0)
                                vv.seekTo(target)
                                currentProgressMs = target.toLong()
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    // Gorgeous Gold/Yellow Floating Action Button for Play/Pause
                    FloatingActionButton(
                        onClick = {
                            videoViewInstance?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                    // re-apply playback speed
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        mediaPlayerInstance?.playbackParams = mediaPlayerInstance?.playbackParams?.setSpeed(playbackSpeed) ?: mpParamsWithSpeed(playbackSpeed)
                                    }
                                }
                            }
                        },
                        containerColor = Color(0xFFFFB300),
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(68.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            videoViewInstance?.let { vv ->
                                val target = (vv.currentPosition + 10000).coerceAtMost(vv.duration)
                                vv.seekTo(target)
                                currentProgressMs = target.toLong()
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                // Bottom Panel (Progress bar, Speed rate, Fullscreen toggle)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentProgressMs),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Slider progress control with super thin custom line (2.dp) and yellow color
                        Slider(
                            value = if (totalDurationMs > 0) currentProgressMs.toFloat() / totalDurationMs else 0f,
                            onValueChange = { percent ->
                                videoViewInstance?.let { vv ->
                                    val target = (percent * totalDurationMs).toInt()
                                    vv.seekTo(target)
                                    currentProgressMs = target.toLong()
                                }
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFB300),
                                activeTrackColor = Color(0xFFFFB300),
                                inactiveTrackColor = Color.White.copy(alpha = 0.24f)
                            ),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFFFFB300), CircleShape)
                                )
                            },
                            track = { sliderPositions ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp) // Extremely thin watch line!
                                        .background(Color.White.copy(alpha = 0.24f), RoundedCornerShape(1.dp))
                                ) {
                                    val fraction = if (totalDurationMs > 0) currentProgressMs.toFloat() / totalDurationMs else 0f
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction)
                                            .fillMaxHeight()
                                            .background(Color(0xFFFFB300), RoundedCornerShape(1.dp))
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                                .testTag("player_progress_slider")
                        )

                        Text(
                            text = formatTime(totalDurationMs),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Speed controller selector and PiP
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Playback Speed Toggle styled as a beautiful modern chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .clickable {
                                    playbackSpeed = when (playbackSpeed) {
                                        0.5f -> 1.0f
                                        1.0f -> 1.5f
                                        1.5f -> 2.0f
                                        else -> 0.5f
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        mediaPlayerInstance?.let { mp ->
                                            try {
                                                mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Speed: ${playbackSpeed}x", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Picture in picture option
                            IconButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        activity?.enterPictureInPictureMode()
                                    }
                                },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.PictureInPicture, contentDescription = "PiP Mode", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Subtitle selection dialog
    if (showSubtitleDialog) {
        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("Select Subtitles") },
            text = {
                Column {
                    listOf("None", "English", "Spanish", "French").forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSubtitles = sub
                                    showSubtitleDialog = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(sub, color = Color.White)
                            if (selectedSubtitles == sub) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSubtitleDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Quality selection dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Playback Quality") },
            text = {
                Column {
                    listOf("Auto (1080p)", "1080p HD", "720p SD", "480p").forEach { quality ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedQuality = quality
                                    showQualityDialog = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(quality, color = Color.White)
                            if (selectedQuality == quality) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQualityDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// Subtitle generator simulation
private fun getSubtitleTextForProgress(progressMs: Long, movieTitle: String): String {
    val sec = progressMs / 1000
    return when {
        sec in 2..6 -> "Welcome to the cinematic masterpiece: $movieTitle"
        sec in 10..15 -> "In a world governed by choice, one destiny stands tall."
        sec in 20..26 -> "We must defend our future, whatever the cost!"
        sec in 30..36 -> "[Dramatic sci-fi synthesizer music playing]"
        else -> "... [dialogue ongoing] ..."
    }
}

// Time formater
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// Fallback api wrapper for speed
@androidx.annotation.RequiresApi(Build.VERSION_CODES.M)
private fun mpParamsWithSpeed(speed: Float): android.media.PlaybackParams {
    return android.media.PlaybackParams().setSpeed(speed)
}
