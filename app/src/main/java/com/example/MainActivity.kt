package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MoviesBoxTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MovieViewModel
import com.example.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instantiate ViewModels using the ViewModelFactory
        val factory = ViewModelFactory(applicationContext)
        val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        val movieViewModel = ViewModelProvider(this, factory)[MovieViewModel::class.java]

        setContent {
            MoviesBoxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    MoviesAppNavigation(
                        authViewModel = authViewModel,
                        movieViewModel = movieViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MoviesAppNavigation(
    authViewModel: AuthViewModel,
    movieViewModel: MovieViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.fillMaxSize()
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }

        // Auth: Login
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        // Auth: Register
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }

        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                movieViewModel = movieViewModel,
                authViewModel = authViewModel
            )
        }

        // Search Screen
        composable(Screen.Search.route) {
            SearchScreen(navController = navController, movieViewModel = movieViewModel)
        }

        // Watchlist Screen
        composable(Screen.Watchlist.route) {
            WatchlistScreen(
                navController = navController,
                movieViewModel = movieViewModel,
                authViewModel = authViewModel
            )
        }

        // Downloads Screen
        composable(Screen.Downloads.route) {
            DownloadsScreen(navController = navController, movieViewModel = movieViewModel)
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                movieViewModel = movieViewModel,
                authViewModel = authViewModel
            )
        }

        // Notifications Screen
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }

        // Movie Details Screen
        composable(
            route = Screen.MovieDetails.route,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            MovieDetailsScreen(
                movieId = movieId,
                navController = navController,
                movieViewModel = movieViewModel,
                authViewModel = authViewModel
            )
        }

        // Video Player Screen
        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            PlayerScreen(
                movieId = movieId,
                navController = navController,
                movieViewModel = movieViewModel,
                authViewModel = authViewModel
            )
        }

        // Category Movies Filter Screen
        composable(
            route = Screen.CategoryMovies.route,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryMoviesScreen(
                categoryName = categoryName,
                navController = navController,
                movieViewModel = movieViewModel
            )
        }

        // Upcoming Movies Screen
        composable(Screen.Upcoming.route) {
            UpcomingMoviesScreen(
                navController = navController,
                movieViewModel = movieViewModel
            )
        }

        // Admin Console Dashboard Screen
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController, movieViewModel = movieViewModel)
        }

        // Subscription Portal Screen
        composable(Screen.Subscription.route) {
            SubscriptionScreen(navController = navController, authViewModel = authViewModel)
        }

        // App Settings Screen
        composable(Screen.AppSettings.route) {
            AppSettingsScreen(navController = navController)
        }

        // Help & Support Screen
        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(navController = navController)
        }
    }
}
