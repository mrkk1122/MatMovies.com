package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Search : Screen("search")
    object Watchlist : Screen("watchlist")
    object History : Screen("history")
    object Subscription : Screen("subscription")
    object Profile : Screen("profile")
    object AdminDashboard : Screen("admin_dashboard")
    object Downloads : Screen("downloads")
    object Notifications : Screen("notifications")

    object MovieDetails : Screen("movie_details/{movieId}") {
        fun createRoute(movieId: Int) = "movie_details/$movieId"
    }

    object Player : Screen("player/{movieId}") {
        fun createRoute(movieId: Int) = "player/$movieId"
    }

    object CategoryMovies : Screen("category/{categoryName}") {
        fun createRoute(categoryName: String) = "category/$categoryName"
    }
}
