package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SpacePrimary,
    onPrimary = Color.Black,
    primaryContainer = SpacePrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = SpaceSecondary,
    onSecondary = Color.Black,
    secondaryContainer = SpaceCardBorder,
    onSecondaryContainer = SpaceTextPrimary,
    tertiary = SpaceAccentPurple,
    background = SpaceDarkBg,
    onBackground = SpaceTextPrimary,
    surface = SpaceCardBg,
    onSurface = SpaceTextPrimary,
    surfaceVariant = SpaceCardBorder,
    onSurfaceVariant = SpaceTextSecondary,
    error = SpaceAccentRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SpacePrimaryDark,
    onPrimary = Color.White,
    secondary = SpaceSecondary,
    onSecondary = Color.White,
    background = SpaceLightBg,
    onBackground = SpaceLightTextPrimary,
    surface = SpaceLightCard,
    onSurface = SpaceLightTextPrimary,
    surfaceVariant = SpaceLightCardBorder,
    onSurfaceVariant = SpaceLightTextSecondary,
    error = SpaceAccentRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize ABDULBASIT SPACE custom brand identity
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
