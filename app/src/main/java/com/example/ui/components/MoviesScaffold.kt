package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen

@Composable
fun MoviesScaffold(
    navController: NavController,
    currentRoute: String,
    showBottomBar: Boolean = true,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = Color.Transparent,
                    tonalElevation = 16.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1E1A24).copy(alpha = 0.95f),
                                        Color(0xFF121216).copy(alpha = 0.95f),
                                        Color(0xFF1C1318).copy(alpha = 0.95f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFB300).copy(alpha = 0.6f),
                                        Color(0xFFE50914).copy(alpha = 0.6f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val items = listOf(
                                NavigationItem(Screen.Home.route, "Home", Icons.Default.Home),
                                NavigationItem(Screen.Search.route, "Search", Icons.Default.Search),
                                NavigationItem(Screen.Watchlist.route, "Watchlist", Icons.Default.Bookmark),
                                NavigationItem(Screen.Downloads.route, "Downloads", Icons.Default.Download),
                                NavigationItem(Screen.Profile.route, "Profile", Icons.Default.Person)
                            )

                            items.forEach { item ->
                                val selected = currentRoute == item.route
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            if (item.route == Screen.Home.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(Screen.Home.route) { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            } else if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(Screen.Home.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 12.dp)
                                ) {
                                    val iconTint = if (selected) {
                                        when (item.label) {
                                            "Home" -> Color(0xFFFFB300)      // Gold
                                            "Search" -> Color(0xFFE50914)    // Red
                                            "Watchlist" -> Color(0xFF1E88E5) // Blue
                                            "Downloads" -> Color(0xFF00C853) // Green
                                            else -> Color(0xFFFF4081)        // Pink/Magenta
                                        }
                                    } else {
                                        Color.White.copy(alpha = 0.65f)
                                    }

                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = iconTint
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(iconTint, androidx.compose.foundation.shape.CircleShape)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = floatingActionButton,
        content = content
    )
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
