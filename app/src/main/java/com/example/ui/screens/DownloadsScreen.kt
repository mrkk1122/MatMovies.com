package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.AnimatedAppName
import com.example.ui.components.MoviesScaffold
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun DownloadsScreen(
    navController: NavController,
    movieViewModel: MovieViewModel
) {
    val downloadedMovieIds by movieViewModel.downloadedMovieIds.collectAsState()
    val allMovies by movieViewModel.allMovies.collectAsState()

    val downloadedMovies = remember(allMovies, downloadedMovieIds) {
        allMovies.filter { downloadedMovieIds.contains(it.id) }
    }

    MoviesScaffold(
        navController = navController,
        currentRoute = Screen.Downloads.route
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedAppName(fontSize = 26.sp)
                Text(
                    text = "Downloads",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Text(
                text = "Fully playable in Offline Simulation Mode",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            )

            if (downloadedMovies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.OfflineBolt,
                            contentDescription = "No Downloads",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No offline movies. Download some from Details!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(downloadedMovies) { movie ->
                        HorizontalMovieCard(
                            movie = movie,
                            onClick = {
                                navController.navigate(Screen.Player.createRoute(movie.id))
                            },
                            onRemoveClick = {
                                movieViewModel.removeDownloadedMovie(movie.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
