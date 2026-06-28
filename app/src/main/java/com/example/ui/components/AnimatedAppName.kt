package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AnimatedAppName(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp
) {
    var animationType by remember { mutableStateOf(0) }
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            // Keep "Movies" text fully fixed and visible for 3 seconds
            isVisible = true
            delay(3000)
            
            // Initiate exit animation
            isVisible = false
            delay(800) // wait for exit animation to complete
            
            // Cycle to the next animation style
            animationType = (animationType + 1) % 3
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Mat" text remains perfectly fixed
        Text(
            text = "Mat",
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color(0xFFE50914), // Crimson Red
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
        
        // "Movies" text animates dynamically based on the active animation type
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 400)) + 
                    slideInVertically(animationSpec = tween(durationMillis = 400)) { -it / 2 },
            exit = when (animationType) {
                0 -> {
                    // Style 1: River Wave Style (Slides down and fades out)
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
                    ) { it } + fadeOut(animationSpec = tween(durationMillis = 300))
                }
                1 -> {
                    // Style 2: Film Reel Style (Rolls/slides upwards and fades out)
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 500, easing = FastOutLinearInEasing)
                    ) { -it } + fadeOut(animationSpec = tween(durationMillis = 300))
                }
                else -> {
                    // Style 3: New Custom Style (Scale-out zoom shrink and fade)
                    scaleOut(
                        targetScale = 0.2f,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 400))
                }
            }
        ) {
            Text(
                text = "Movies",
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFB300), // Golden Amber
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
