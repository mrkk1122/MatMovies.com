package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CinemaPrimary,
    secondary = CinemaSecondary,
    tertiary = CinemaTertiary,
    background = CinemaBackground,
    surface = CinemaSurface,
    surfaceVariant = CinemaSurfaceVariant,
    onPrimary = CinemaBackground,
    onSecondary = CinemaTextPrimary,
    onTertiary = CinemaTextPrimary,
    onBackground = CinemaTextPrimary,
    onSurface = CinemaTextPrimary,
    onSurfaceVariant = CinemaTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = LightCinemaPrimary,
    secondary = LightCinemaSecondary,
    background = LightCinemaBackground,
    surface = LightCinemaSurface,
    onPrimary = LightCinemaSurface,
    onSecondary = LightCinemaTextPrimary,
    onBackground = LightCinemaTextPrimary,
    onSurface = LightCinemaTextPrimary
)

@Composable
fun MoviesBoxTheme(
    darkTheme: Boolean = true, // Force cinematic dark theme by default for premium visual polish!
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the beautiful gold/crimson brand look!
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
