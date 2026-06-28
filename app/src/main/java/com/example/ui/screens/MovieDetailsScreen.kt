package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.navigation.Screen
import com.example.ui.components.MoviesScaffold
import com.example.ui.utils.DrawableHelper
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MovieViewModel
import com.example.data.model.MovieEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    movieId: Int,
    navController: NavController,
    movieViewModel: MovieViewModel,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Fetch and select movie details
    LaunchedEffect(movieId, currentUser) {
        movieViewModel.setActiveUser(currentUser?.id)
        movieViewModel.selectMovie(movieId)
    }

    val movie by movieViewModel.selectedMovie.collectAsState()
    val reviews by movieViewModel.selectedMovieReviews.collectAsState()
    val isInWatchlist by movieViewModel.isCurrentInWatchlist.collectAsState()
    val allMovies by movieViewModel.allMovies.collectAsState()
    
    // Download progress simulation tracking
    val downloadingStates by movieViewModel.downloadingStates.collectAsState()
    val downloadedMovieIds by movieViewModel.downloadedMovieIds.collectAsState()

    val movieForRecommendations = movie
    val recommendations = remember(allMovies, movieForRecommendations) {
        if (movieForRecommendations == null) emptyList() else {
            allMovies.filter { other ->
                other.id != movieForRecommendations.id && (
                    other.director == movieForRecommendations.director || 
                    other.castList.any { actor -> movieForRecommendations.castList.contains(actor) } ||
                    other.genre.split(", ").any { genre -> movieForRecommendations.genre.contains(genre) }
                )
            }
        }
    }

    var userRatingInput by remember { mutableStateOf(5) }
    var userCommentInput by remember { mutableStateOf("") }

    var showAuthDialog by remember { mutableStateOf(false) }
    var authDialogPurpose by remember { mutableStateOf("") }

    if (movie == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val currentMovie = movie!!
    var selectedSeason by remember(currentMovie.id) { mutableStateOf(1) }
    var selectedEpisode by remember(currentMovie.id, selectedSeason) { mutableStateOf(1) }

    MoviesScaffold(
        navController = navController,
        currentRoute = Screen.MovieDetails.route
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
        // Banner Video / Poster Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            var isVideoReady by remember { mutableStateOf(false) }
            var isMuted by remember { mutableStateOf(true) }
            var trailerMediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
 
            // Autoplay trailer preview player (loops automatically, muted by default)
            AndroidView(
                factory = { ctx ->
                    android.widget.VideoView(ctx).apply {
                        setVideoURI(android.net.Uri.parse(currentMovie.videoUrl))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            trailerMediaPlayer = mp
                            mp.setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
                            mp.start()
                            isVideoReady = true
                        }
                        setOnErrorListener { _, _, _ ->
                            // Fallback to image
                            true
                        }
                    }
                },
                update = { vv ->
                    try {
                        trailerMediaPlayer?.setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
 
            // Show static image until video prepares or if video fails
            if (!isVideoReady) {
                val drawableId = DrawableHelper.getDrawableIdByName(currentMovie.posterDrawableName)
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = currentMovie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Mute / Unmute Floating Control
            if (isVideoReady) {
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 70.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Trailer Volume",
                        tint = if (isMuted) Color.White else Color(0xFFFFB300),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Premium Floating Trailer Tag
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 44.dp, end = 16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Trailer Loop preview",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "TRAILER ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            // Gradient overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Back button
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 44.dp, start = 16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        // Content layout container with upward overlap offset
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 8.dp)
        ) {
            // Title and Play FAB Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentMovie.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentMovie.year.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentMovie.duration,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = currentMovie.rating.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Play Button leading directly to video player
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Player.createRoute(currentMovie.id)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .size(60.dp)
                        .testTag("play_movie_fab")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", modifier = Modifier.size(36.dp))
                }
            }

            // Quick details summary (Genre, views, subtitles)
            Text(
                text = "Genre: ${currentMovie.genre}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = "Language: ${currentMovie.language} ${if (currentMovie.subtitlesUrl.isNotEmpty()) "• Subs: ${currentMovie.subtitlesUrl}" else ""}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Action toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Watchlist action
                ActionButton(
                    icon = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                    label = if (isInWatchlist) "In Watchlist" else "Watchlist",
                    tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else Color.White,
                    onClick = {
                        if (currentUser != null) {
                            movieViewModel.toggleWatchlist(currentMovie.id)
                        } else {
                            authDialogPurpose = "save this movie to your watchlist"
                            showAuthDialog = true
                        }
                    }
                )

                // Download action (Simulated)
                val isDownloading = downloadingStates.containsKey(currentMovie.id)
                val isDownloaded = downloadedMovieIds.contains(currentMovie.id)
                val progress = downloadingStates[currentMovie.id] ?: 0f

                ActionButton(
                    icon = when {
                        isDownloaded -> Icons.Default.CheckCircle
                        isDownloading -> Icons.Default.RotateRight
                        else -> Icons.Default.Download
                    },
                    label = when {
                        isDownloaded -> "Downloaded"
                        isDownloading -> "${(progress * 100).toInt()}%"
                        else -> "Download"
                    },
                    tint = if (isDownloaded) MaterialTheme.colorScheme.primary else Color.White,
                    onClick = {
                        if (isDownloaded) {
                            movieViewModel.removeDownloadedMovie(currentMovie.id)
                        } else if (!isDownloading) {
                            movieViewModel.startMovieDownload(currentMovie.id)
                        }
                    }
                )

                // Share Action
                ActionButton(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {}
                )
            }

            if (currentMovie.isSeries && currentMovie.seasonsCount > 0) {
                val currentEpisodesCount = when (currentMovie.title) {
                    "Bachelor Point - Season 4" -> 12
                    "Mirzapur - Season 3" -> 9
                    else -> 8
                }
                
                var showSeasonDropdown by remember { mutableStateOf(false) }
                var showEpisodeDropdown by remember { mutableStateOf(false) }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total counts in small, sleek, thin containers (চিকন কনটেইনার)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB300).copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Seasons: ${currentMovie.seasonsCount}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63).copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Total Episodes: ${currentMovie.seasonsCount * currentEpisodesCount}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Selectors side-by-side in sleek, small containers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Season Selector Container (ছোট চিকন কনটেইনার)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .clickable { showSeasonDropdown = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = "Season Select",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Season $selectedSeason",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSeasonDropdown,
                                onDismissRequest = { showSeasonDropdown = false },
                                modifier = Modifier.background(Color(0xFF1E1E24))
                            ) {
                                for (s in 1..currentMovie.seasonsCount) {
                                    DropdownMenuItem(
                                        text = { Text("Season $s", color = if (selectedSeason == s) Color(0xFFFFB300) else Color.White, fontSize = 13.sp) },
                                        onClick = {
                                            selectedSeason = s
                                            selectedEpisode = 1
                                            showSeasonDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Episode Selector Container (ছোট চিকন কনটেইনার)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .clickable { showEpisodeDropdown = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Episode Select",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Episode $selectedEpisode",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showEpisodeDropdown,
                                onDismissRequest = { showEpisodeDropdown = false },
                                modifier = Modifier.background(Color(0xFF1E1E24))
                            ) {
                                for (ep in 1..currentEpisodesCount) {
                                    DropdownMenuItem(
                                        text = { Text("Episode $ep", color = if (selectedEpisode == ep) Color(0xFFFFB300) else Color.White, fontSize = 13.sp) },
                                        onClick = {
                                            selectedEpisode = ep
                                            showEpisodeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Synopsis
            Text(
                text = "Synopsis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = currentMovie.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Director Info
            Text(
                text = "Director",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = "Director Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = currentMovie.director,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Main Director",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cast / Actors Section
            Text(
                text = "Cast & Starring",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                items(currentMovie.castList.size) { index ->
                    val actor = currentMovie.castList[index]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Actor",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = actor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Sleek thin selector for Season and Episode under Cast & Starring
            if (currentMovie.isSeries && currentMovie.seasonsCount > 0) {
                var isSeasonMenuExpanded by remember { mutableStateOf(false) }
                var isEpisodeMenuExpanded by remember { mutableStateOf(false) }
                val episodesCountForSelector = when (currentMovie.title) {
                    "Bachelor Point - Season 4" -> 12
                    "Mirzapur - Season 3" -> 9
                    else -> 8
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                    border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Active Stream",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Running:",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Season $selectedSeason • Episode $selectedEpisode",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Season Quick Dropdown
                            Box {
                                Row(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                        .clickable { isSeasonMenuExpanded = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "S$selectedSeason",
                                        color = Color(0xFFFFB300),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Season",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = isSeasonMenuExpanded,
                                    onDismissRequest = { isSeasonMenuExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E1E24))
                                ) {
                                    List(currentMovie.seasonsCount) { index ->
                                        val seasonNum = index + 1
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Season $seasonNum",
                                                    color = if (selectedSeason == seasonNum) Color(0xFFFFB300) else Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            onClick = {
                                                selectedSeason = seasonNum
                                                selectedEpisode = 1
                                                isSeasonMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Episode Quick Dropdown
                            Box {
                                Row(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                        .clickable { isEpisodeMenuExpanded = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "E$selectedEpisode",
                                        color = Color(0xFFFFB300),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Episode",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = isEpisodeMenuExpanded,
                                    onDismissRequest = { isEpisodeMenuExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E1E24))
                                ) {
                                    List(episodesCountForSelector) { index ->
                                        val episodeNum = index + 1
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Episode $episodeNum",
                                                    color = if (selectedEpisode == episodeNum) Color(0xFFFFB300) else Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            onClick = {
                                                selectedEpisode = episodeNum
                                                isEpisodeMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Series Seasons and Episodes Selection
            if (currentMovie.isSeries && currentMovie.seasonsCount > 0) {
                Text(
                    text = "Seasons & Episodes 🎬",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                // Horizontal chips for seasons selection
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    items(currentMovie.seasonsCount) { index ->
                        val seasonNum = index + 1
                        val isSelected = selectedSeason == seasonNum
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedSeason = seasonNum }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Season $seasonNum",
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // List of episodes for the selected season
                val episodesCount = when (currentMovie.title) {
                    "Bachelor Point - Season 4" -> 12
                    "Mirzapur - Season 3" -> 9
                    else -> 8
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    List(episodesCount) { episodeIdx ->
                        val episodeNum = episodeIdx + 1
                        val epTitle = when (currentMovie.title) {
                            "Bachelor Point - Season 4" -> when (episodeIdx) {
                                0 -> "The Bachelor Jam"
                                1 -> "Dhaka Life Struggle"
                                2 -> "Habu Bhai's Marriage Drama"
                                3 -> "Kabila's Master Plan"
                                4 -> "Shimul's Unseen Love"
                                5 -> "Pasha Bhai's Outrage"
                                6 -> "Borhan's Dilemma"
                                7 -> "Antu's Return"
                                8 -> "The Big Mess"
                                9 -> "Bachelor Picnic"
                                10 -> "New Tenant Threat"
                                else -> "Bachelor Finale Clash"
                            }
                            "Mirzapur - Season 3" -> when (episodeIdx) {
                                0 -> "The Throne Vacant"
                                1 -> "Guddu's Iron Rule"
                                2 -> "Kaleen Bhaiya's Recovery"
                                3 -> "Sharad's Web of Lies"
                                4 -> "The Golu Investigation"
                                5 -> "Blood on the River"
                                6 -> "Chota Tyagi's Decision"
                                7 -> "The Purvanchal Meeting"
                                else -> "The Mirzapur Massacre"
                            }
                            else -> "Episode $episodeNum: The Untold Chapter"
                        }
                        
                        val epDuration = "45 mins"
                        val epOverview = "A gripping and entertaining episode of ${currentMovie.title} full of twists, local humor, and high production quality."
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEpisode = episodeNum
                                    navController.navigate(Screen.Player.createRoute(currentMovie.id))
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail with movie poster/background and a beautiful overlay play button
                                Box(
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    val drawableId = DrawableHelper.getDrawableIdByName(currentMovie.posterDrawableName)
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = "Episode $episodeNum Thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Ambient dark filter
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    )
                                    
                                    // Play icon overlay
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            .align(Alignment.Center),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Episode",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    
                                    // Duration badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                    ) {
                                        Text(
                                            text = "EP $episodeNum",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                
                                // Text metadata details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ep $episodeNum: $epTitle",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = epDuration,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Text(
                                        text = epOverview,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Suggested Movies Section (Director and Cast-based recommendation)
            Text(
                text = "Suggested Movies",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (recommendations.isEmpty()) {
                Text(
                    text = "No direct suggestions based on director or cast available yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    items(recommendations.size) { index ->
                        val recMovie = recommendations[index]
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable {
                                    navController.navigate(Screen.MovieDetails.createRoute(recMovie.id)) {
                                        // Allow nice backstack transition
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    val drawableId = DrawableHelper.getDrawableIdByName(recMovie.posterDrawableName)
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = recMovie.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Soft rating overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                                            Text(recMovie.rating.toString(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = recMovie.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${recMovie.year} • ${recMovie.duration}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 24.dp))

            // Reviews/Comments List
            Text(
                text = "Reviews & Comments (${reviews.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Leave a review block
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Leave your feedback",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    // Star rating Selector
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (star in 1..5) {
                            Icon(
                                imageVector = if (star <= userRatingInput) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star Stars",
                                tint = if (star <= userRatingInput) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { userRatingInput = star }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = userCommentInput,
                        onValueChange = { userCommentInput = it },
                        placeholder = { Text("Write your thoughts here...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("movie_comment_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            if (currentUser == null) {
                                authDialogPurpose = "post reviews and rate movies"
                                showAuthDialog = true
                            } else if (userCommentInput.isNotBlank()) {
                                movieViewModel.submitReview(
                                    currentMovie.id,
                                    currentUser?.username ?: "Viewer",
                                    userRatingInput,
                                    userCommentInput.trim()
                                )
                                userCommentInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 10.dp)
                            .testTag("submit_review_button")
                    ) {
                        Text("Post Review", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // List reviews
            if (reviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Be the first to review this movie!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    reviews.forEach { review ->
                        ReviewItem(review)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showAuthDialog) {
            AlertDialog(
                onDismissRequest = { showAuthDialog = false },
                title = {
                    Text(
                        text = "Sign In Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Please sign in to $authDialogPurpose and sync your entertainment profile.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAuthDialog = false
                            navController.navigate(Screen.Login.route)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Sign In", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAuthDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ReviewItem(review: com.example.data.model.ReviewEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.username,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= review.rating) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

// Extension getters to map beautiful director and cast information based on titles
val MovieEntity.director: String
    get() = when (this.title) {
        "Dune Chronicles" -> "Denis Villeneuve"
        "Cyberpunk Neo" -> "Lana Wachowski"
        "The Ancient Portal" -> "Peter Jackson"
        "Tears of Steel" -> "Ian Hubert"
        else -> "Christopher Nolan"
    }

val MovieEntity.castList: List<String>
    get() = when (this.title) {
        "Dune Chronicles" -> listOf("Timothée Chalamet", "Zendaya", "Rebecca Ferguson", "Oscar Isaac")
        "Cyberpunk Neo" -> listOf("Keanu Reeves", "Carrie-Anne Moss", "Laurence Fishburne", "Hugo Weaving")
        "The Ancient Portal" -> listOf("Elijah Wood", "Ian McKellen", "Orlando Bloom", "Viggo Mortensen")
        "Tears of Steel" -> listOf("Derek de Lint", "Sergio Hasselbaink", "Rogier Schippers")
        else -> listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page", "Tom Hardy")
    }

