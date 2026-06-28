package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import com.example.ui.utils.DrawableHelper
import com.example.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    // List of 20 poster banners for the background collage using real movie/natok banners
    val backgroundPosters = remember {
        listOf(
            "img_natok_banner", "img_action_banner", "img_natok_banner", "img_action_banner",
            "img_action_banner", "img_natok_banner", "img_action_banner", "img_natok_banner",
            "img_natok_banner", "img_action_banner", "img_natok_banner", "img_action_banner",
            "img_action_banner", "img_natok_banner", "img_action_banner", "img_natok_banner",
            "img_natok_banner", "img_action_banner", "img_natok_banner", "img_action_banner"
        )
    }

    LaunchedEffect(key1 = true) {
        // Animate entrance of logo and name
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(1f, animationSpec = tween(1200))
        
        delay(1500) // Elegant splash dwell time

        // Route directly to home screen
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070A))
    ) {
        // 1. Background Collage of 15-20 Photo Banners together (Brighter light background opacity)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.48f), // Increased brightness / light for background posters
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            userScrollEnabled = false
        ) {
            items(backgroundPosters) { posterName ->
                val drawableId = DrawableHelper.getDrawableIdByName(posterName)
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(0.7f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 2. Cinematic Gradient Overlay to darken edges and merge with background (Softer scrim for lighter background)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF07070A).copy(alpha = 0.25f),
                            Color(0xFF07070A).copy(alpha = 0.65f),
                            Color(0xFF07070A)
                        )
                    )
                )
        )

        // 3. App Logo, Name, and Subtitle in foreground
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(24.dp)
        ) {
            // Elegant glowing card for original movie Logo placed strictly ABOVE App Name
            val logoDrawableId = DrawableHelper.getDrawableIdByName("img_app_icon_cinema")
            Box(
                modifier = Modifier
                    .size(125.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFB300).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = logoDrawableId),
                    contentDescription = "MatMovies Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Color(0xFFFFB300), RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Welcome to",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "MatMovies",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFB300),
                            Color(0xFFFF5722),
                            Color(0xFFE91E63)
                        )
                    )
                ),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "STREAM • EXPERIENCE • REPEAT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
