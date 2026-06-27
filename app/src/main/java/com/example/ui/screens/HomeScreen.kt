package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.model.MovieEntity
import com.example.ui.components.MoviesScaffold
import com.example.ui.navigation.Screen
import com.example.ui.utils.DrawableHelper
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MovieViewModel
import com.example.ui.viewmodel.CarouselUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.CircleShape

@Composable
fun HomeScreen(
    navController: NavController,
    movieViewModel: MovieViewModel,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    // Set active user inside MovieViewModel to track customized lists!
    LaunchedEffect(currentUser) {
        movieViewModel.setActiveUser(currentUser?.id)
    }

    val featuredMovies by movieViewModel.featuredMovies.collectAsStateWithLifecycle()
    val trendingMovies by movieViewModel.trendingMovies.collectAsStateWithLifecycle()
    val latestMovies by movieViewModel.latestMovies.collectAsStateWithLifecycle()
    val categories by movieViewModel.categories.collectAsStateWithLifecycle()
    val watchlist by movieViewModel.watchlist.collectAsStateWithLifecycle()
    val watchHistory by movieViewModel.watchHistory.collectAsStateWithLifecycle()
    val rawWatchHistory by movieViewModel.rawWatchHistory.collectAsStateWithLifecycle()
    val allMovies by movieViewModel.allMovies.collectAsStateWithLifecycle()
    val carouselUiState by movieViewModel.carouselUiState.collectAsStateWithLifecycle()

    var selectedGenreFilter by remember { mutableStateOf("All") }
    var homeSearchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val homeScrollState = rememberScrollState()
    var visibleMoviesLimit by remember { mutableStateOf(4) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val isScrollAtBottom = homeScrollState.value >= homeScrollState.maxValue - 200 && homeScrollState.maxValue > 0

    LaunchedEffect(isScrollAtBottom) {
        if (isScrollAtBottom && visibleMoviesLimit < allMovies.size && !isLoadingMore) {
            isLoadingMore = true
            kotlinx.coroutines.delay(1200) // Beautiful cinematic stream loading delay
            visibleMoviesLimit = (visibleMoviesLimit + 4).coerceAtMost(allMovies.size)
            isLoadingMore = false
        }
    }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            focusRequester.requestFocus()
        }
    }

    // Dynamic Greeting based on Local System Hour
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Late night stream?"
        }
    }

    MoviesScaffold(
        navController = navController,
        currentRoute = Screen.Home.route
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F0F13),
                            Color(0xFF14141F),
                            Color(0xFF0A0A0E)
                        )
                    )
                )
                .padding(innerPadding)
                .verticalScroll(homeScrollState)
        ) {
            // Conditional Smart Header & Search Architecture
            if (isSearchExpanded) {
                // Expanded Elegant Search Bar (Active state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { 
                            isSearchExpanded = false
                            homeSearchQuery = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color.White
                        )
                    }

                    OutlinedTextField(
                        value = homeSearchQuery,
                        onValueChange = { homeSearchQuery = it },
                        placeholder = { 
                            Text(
                                text = "Search movies, genres, cast...", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            ) 
                        },
                        trailingIcon = {
                            if (homeSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { homeSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear, 
                                        contentDescription = "Clear search", 
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("home_search_bar"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // If search is active, show search results
                if (homeSearchQuery.isNotBlank()) {
                    SectionHeader(title = "Instant Search Results")
                    
                    val filteredSearchMovies = remember(homeSearchQuery, allMovies) {
                        allMovies.filter {
                            it.title.contains(homeSearchQuery, ignoreCase = true) ||
                            it.genre.contains(homeSearchQuery, ignoreCase = true) ||
                            it.description.contains(homeSearchQuery, ignoreCase = true)
                        }
                    }
                    
                    if (filteredSearchMovies.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.MovieFilter,
                                    contentDescription = "No results",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No movies match \"$homeSearchQuery\"",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        val chunks = filteredSearchMovies.chunked(2)
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            chunks.forEach { rowMovies ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowMovies.forEach { movie ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            MovieCard(
                                                movie = movie,
                                                onClick = { navController.navigate(Screen.MovieDetails.createRoute(movie.id)) },
                                                isInWatchlist = watchlist.any { it.id == movie.id },
                                                onToggleWatchlist = { movieViewModel.toggleWatchlist(movie.id) }
                                            )
                                        }
                                    }
                                    if (rowMovies.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Search is expanded but query is empty: Show beautiful smart placeholder and quick tags
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search movies",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Search for your next stream",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Explore blockbuster hits, curated categories, and hidden gems.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "POPULAR SUGGESTIONS",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Sci-Fi", "Action", "Inception").forEach { tag ->
                                    SearchSuggestionTag(text = tag, onClick = { homeSearchQuery = tag })
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Interstellar", "Drama", "Fantasy").forEach { tag ->
                                    SearchSuggestionTag(text = tag, onClick = { homeSearchQuery = tag })
                                }
                            }
                        }
                    }
                }
            } else {
                // Collapsed Elegant Header (Standard state - Mobile friendly and beautiful)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, top = 20.dp, end = 14.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(
                                    color = Color(0xFFE50914), // Crimson Red
                                    fontWeight = FontWeight.Black
                                )) {
                                    append("Mat")
                                }
                                withStyle(style = SpanStyle(
                                    color = Color(0xFFFFB300), // Golden Amber
                                    fontWeight = FontWeight.Black
                                )) {
                                    append("Movies")
                                }
                            },
                            fontSize = 32.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Sleek top right Search icon action
                            IconButton(
                                onClick = { isSearchExpanded = true },
                                modifier = Modifier
                                    .testTag("home_search_toggle_btn")
                                    .size(44.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Open Search",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { navController.navigate(Screen.Notifications.route) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.12f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Box(modifier = Modifier.size(20.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                    // Add a small crimson badge to notify the user of updates!
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(Color(0xFFE50914), CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                    }
                }

                // STANDARD SMART HOME DASHBOARD CONTENT
                
                // Interactive Genre Filters Row with ultra modern custom chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val filters = listOf("All", "Sci-Fi", "Action", "Fantasy", "Drama")
                    items(filters) { filter ->
                        CustomFilterChip(
                            text = filter,
                            selected = selectedGenreFilter == filter,
                            onClick = { selectedGenreFilter = filter }
                        )
                    }
                }

                // Immersive Dynamic Hero Banner Carousel Section using HorizontalPager and CarouselUiState
                if (selectedGenreFilter == "All") {
                    when (val state = carouselUiState) {
                        is CarouselUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(450.dp)
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is CarouselUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(450.dp)
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Unable to load trending highlights",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        is CarouselUiState.Success -> {
                            val trendingMoviesList = state.trendingMovies
                            val totalTrending = trendingMoviesList.size
                            
                            if (totalTrending > 0) {
                                val pagerState = rememberPagerState(pageCount = { totalTrending })
                                
                                LaunchedEffect(pagerState.currentPage) {
                                    kotlinx.coroutines.delay(6000)
                                    val nextPage = (pagerState.currentPage + 1) % totalTrending
                                    pagerState.animateScrollToPage(nextPage)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(450.dp)
                                        .padding(horizontal = 8.dp, vertical = 12.dp)
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp))
                                            .border(
                                                width = 1.dp,
                                                color = Color.White.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .testTag("hero_banner_carousel")
                                    ) { page ->
                                        val heroMovie = trendingMoviesList[page]
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { navController.navigate(Screen.MovieDetails.createRoute(heroMovie.id)) }
                                        ) {
                                            val drawableId = DrawableHelper.getDrawableIdByName(heroMovie.posterDrawableName)
                                            Image(
                                                painter = painterResource(id = drawableId),
                                                contentDescription = "Hero Movie Banner - ${heroMovie.title}",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            
                                            // Enhanced dual dynamic overlay gradient for higher cinematic contrast
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color.Transparent,
                                                                Color.Black.copy(alpha = 0.3f),
                                                                Color.Black.copy(alpha = 0.95f)
                                                            )
                                                        )
                                                    )
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(20.dp)
                                            ) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Whatshot,
                                                            contentDescription = "Fire Icon",
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = "TRENDING NOW",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.Black
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = heroMovie.title,
                                                    fontSize = 26.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Text(
                                                    text = "${heroMovie.year} • ${heroMovie.genre} • ${heroMovie.duration}",
                                                    fontSize = 12.sp,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                                                )

                                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Button(
                                                        onClick = { navController.navigate(Screen.Player.createRoute(heroMovie.id)) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Play Now", color = Color.Black, fontWeight = FontWeight.Bold)
                                                    }

                                                    OutlinedButton(
                                                        onClick = { navController.navigate(Screen.MovieDetails.createRoute(heroMovie.id)) },
                                                        shape = RoundedCornerShape(10.dp),
                                                        border = BorderStroke(1.2.dp, Color.White),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                                    ) {
                                                        Icon(Icons.Default.Info, contentDescription = "Info")
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Details")
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    val activeIndex by remember { derivedStateOf { pagerState.currentPage } }
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(totalTrending) { index ->
                                            val isDotSelected = index == activeIndex
                                            Box(
                                                modifier = Modifier
                                                    .size(width = if (isDotSelected) 16.dp else 6.dp, height = 6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(if (isDotSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Smart Recommendation Banner (Brand New CinePick Section)
                if (allMovies.isNotEmpty() && selectedGenreFilter == "All") {
                    // Pick the first featured movie or first movie overall as the daily spotlight
                    val spotlightMovie = featuredMovies.firstOrNull() ?: allMovies.first()
                    CinePickCard(
                        movie = spotlightMovie,
                        onClick = { navController.navigate(Screen.MovieDetails.createRoute(spotlightMovie.id)) },
                        onPlayClick = { navController.navigate(Screen.Player.createRoute(spotlightMovie.id)) }
                    )
                }

                // Smart Continue Watching / Resume Playback Section
                if (watchHistory.isNotEmpty() && selectedGenreFilter == "All") {
                    SectionHeader(title = "Continue Watching")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(watchHistory) { movie ->
                            val progressEntity = rawWatchHistory.find { it.movieId == movie.id }
                            val progressPct = if (progressEntity != null && progressEntity.durationMs > 0) {
                                progressEntity.progressMs.toFloat() / progressEntity.durationMs
                            } else {
                                0.35f
                            }
                            
                            ContinueWatchingCard(
                                movie = movie,
                                progress = progressPct,
                                onClick = {
                                    navController.navigate(Screen.Player.createRoute(movie.id))
                                }
                            )
                        }
                    }
                }

                // Explore Categories Section
                if (categories.isNotEmpty() && selectedGenreFilter == "All") {
                    SectionHeader(title = "Explore Genres")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories) { category ->
                            CategoryCard(
                                category = category,
                                onClick = {
                                    navController.navigate(Screen.CategoryMovies.createRoute(category.name))
                                }
                            )
                        }
                    }
                }

                // Trending Movies Section with Quick Watchlist Action & Netflix-style ranking
                val filteredTrending = if (selectedGenreFilter == "All") trendingMovies else trendingMovies.filter { it.genre.contains(selectedGenreFilter, ignoreCase = true) }
                if (filteredTrending.isNotEmpty()) {
                    SectionHeader(
                        title = "Trending Now",
                        onSeeAllClick = { navController.navigate(Screen.CategoryMovies.createRoute("Trending")) }
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(filteredTrending.size) { index ->
                            val movie = filteredTrending[index]
                            TrendingMovieCard(
                                rank = index + 1,
                                movie = movie,
                                onClick = {
                                    navController.navigate(Screen.MovieDetails.createRoute(movie.id))
                                },
                                isInWatchlist = watchlist.any { it.id == movie.id },
                                onToggleWatchlist = { movieViewModel.toggleWatchlist(movie.id) }
                            )
                        }
                    }
                }

                // Latest Releases Section with Quick Watchlist Action
                val filteredLatest = if (selectedGenreFilter == "All") latestMovies else latestMovies.filter { it.genre.contains(selectedGenreFilter, ignoreCase = true) }
                if (filteredLatest.isNotEmpty()) {
                    SectionHeader(
                        title = "Latest Releases",
                        onSeeAllClick = { navController.navigate(Screen.CategoryMovies.createRoute("Latest")) }
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredLatest) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = {
                                    navController.navigate(Screen.MovieDetails.createRoute(movie.id))
                                },
                                isInWatchlist = watchlist.any { it.id == movie.id },
                                onToggleWatchlist = { movieViewModel.toggleWatchlist(movie.id) }
                            )
                        }
                    }
                }

                // Empty / Not Found Fallback filter state
                if (filteredLatest.isEmpty() && filteredTrending.isEmpty() && featuredMovies.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = "No movies",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No movies match this genre",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Infinite Scroll "Discover More" Section
                if (selectedGenreFilter == "All" && allMovies.isNotEmpty()) {
                    SectionHeader(title = "More to Stream")
                    
                    val infiniteMovies = allMovies.take(visibleMoviesLimit)
                    
                    val chunkedMovies = infiniteMovies.chunked(2)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        chunkedMovies.forEach { rowMovies ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowMovies.forEach { movie ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        DiscoverMovieCard(
                                            movie = movie,
                                            onClick = {
                                                navController.navigate(Screen.MovieDetails.createRoute(movie.id))
                                            }
                                        )
                                    }
                                }
                                if (rowMovies.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    
                    if (isLoadingMore || visibleMoviesLimit < allMovies.size) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.5.dp
                                )
                                Text(
                                    text = "Streaming more movies...",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "You've reached the end of the universe ✨",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CustomFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
fun CinePickCard(
    movie: MovieEntity,
    onClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val drawableId = DrawableHelper.getDrawableIdByName(movie.posterDrawableName)
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "CinePick of the Day",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Soft cinematic gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
            
            // "98% Match" Tag
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trending match",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "98% MATCH",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Top Left Pick tag
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Pick icon",
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "SMART CINEPICK",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Content overlay at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = movie.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = LocalTextStyle.current.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 6f
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${movie.year} • ${movie.genre} • ${movie.duration}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = movie.rating.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            onPlayClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Instant Stream", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                    }
                    
                    Text(
                        text = "Curated based on your preferences",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingMovieCard(
    rank: Int,
    movie: MovieEntity,
    onClick: () -> Unit,
    isInWatchlist: Boolean = false,
    onToggleWatchlist: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(175.dp)
            .clickable { onClick() }
            .testTag("trending_movie_card_${movie.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            val drawableId = DrawableHelper.getDrawableIdByName(movie.posterDrawableName)
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Watchlist toggle
            if (onToggleWatchlist != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable { onToggleWatchlist() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Toggle Watchlist",
                        tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Rating Badge
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = movie.rating.toString(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Bottom Gradient Info Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = movie.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 22.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // High contrast sidebar visual accent line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
        if (onSeeAllClick != null) {
            Text(
                text = "See All",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }
}

@Composable
fun MovieCard(
    movie: MovieEntity,
    onClick: () -> Unit,
    isInWatchlist: Boolean = false,
    onToggleWatchlist: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .width(135.dp)
            .clickable { onClick() }
            .testTag("movie_card_${movie.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.03f)
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                val drawableId = DrawableHelper.getDrawableIdByName(movie.posterDrawableName)
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Subtly darken poster bottoms to make labels easily readable
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                )

                // Quick watchlist bookmark toggle overlay
                if (onToggleWatchlist != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable { onToggleWatchlist() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Toggle Watchlist",
                            tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Rating Badge
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = movie.rating.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Text(
                text = movie.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
            )
            
            Text(
                text = "${movie.year} • ${movie.genre}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: com.example.data.model.CategoryEntity,
    onClick: () -> Unit
) {
    // Beautiful dynamic styling & color mapping for genre categories
    val (icon, tint) = when (category.name) {
        "Action" -> Pair(Icons.Default.Whatshot, Color(0xFFFF3D00))
        "Sci-Fi" -> Pair(Icons.Default.Science, Color(0xFF00E5FF))
        "Fantasy" -> Pair(Icons.Default.AutoAwesome, Color(0xFFFFC400))
        "Drama" -> Pair(Icons.Default.Favorite, Color(0xFFFF007F))
        else -> Pair(Icons.Default.Movie, MaterialTheme.colorScheme.primary)
    }

    Card(
        modifier = Modifier
            .width(115.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    tint.copy(alpha = 0.4f),
                    tint.copy(alpha = 0.05f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1E2A),
                            tint.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(tint.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name,
                        tint = tint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                
                Text(
                    text = category.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    movie: MovieEntity,
    progress: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(225.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("continue_watching_card_${movie.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2A)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            ) {
                val drawableId = DrawableHelper.getDrawableIdByName(movie.posterDrawableName)
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark ambient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Resume",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Progress Bar overlay - with dynamic gradient look
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }

            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = movie.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Resume playing",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SearchSuggestionTag(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun DiscoverMovieCard(
    movie: MovieEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f)
            ) {
                val drawableId = DrawableHelper.getDrawableIdByName(movie.posterDrawableName)
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Rating tag
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = movie.rating.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = movie.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${movie.year} • ${movie.genre.split(",").firstOrNull() ?: movie.genre}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movie.duration,
                        fontSize = 10.sp,
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
