package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
            Text(
                text = "Simulated Downloads",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 4.dp)
            )
            
            Text(
                text = "Fully playable in Offline Simulation Mode",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(downloadedMovies) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = {
                                navController.navigate(Screen.Player.createRoute(movie.id))
                            }
                        )
                    }
                }
            }
        }
    }
}
