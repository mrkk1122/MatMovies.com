package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.MovieViewModel
import com.example.ui.components.MoviesScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryMoviesScreen(
    categoryName: String,
    navController: NavController,
    movieViewModel: MovieViewModel
) {
    val allMovies by movieViewModel.allMovies.collectAsState()
    
    val categoryMovies = remember(allMovies, categoryName) {
        when (categoryName.lowercase()) {
            "trending" -> allMovies.filter { it.isTrending }
            "latest" -> allMovies.filter { it.isLatest }
            else -> allMovies.filter { it.genre.contains(categoryName, ignoreCase = true) }
        }
    }

    MoviesScaffold(
        navController = navController,
        currentRoute = Screen.CategoryMovies.route,
        topBar = {
            TopAppBar(
                title = { Text("$categoryName Movies", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (categoryMovies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Empty Category",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No movies in this category yet",
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
                    items(categoryMovies) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = {
                                navController.navigate(Screen.MovieDetails.createRoute(movie.id))
                            }
                        )
                    }
                }
            }
        }
    }
}
