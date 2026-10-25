package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Bright and refreshing Linen Cream, Warm Beige, Mushroom Taupe, Olive Sage, and Forest Green palette
private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = OliveSageSecondary,
    onSecondary = Color.White,
    secondaryContainer = OliveSageContainer,
    onSecondaryContainer = OliveSageOnContainer,
    tertiary = MushroomTaupeTertiary,
    onTertiary = Color.White,
    tertiaryContainer = MushroomTaupeContainer,
    onTertiaryContainer = Color(0xFF2A231C),
    background = LinenCreamBackground,
    surface = LinenCreamSurface,
    surfaceVariant = WarmBeigeSurfaceVariant,
    onBackground = CharcoalTextDark,
    onSurface = CharcoalTextDark,
    onSurfaceVariant = TaupeTextMuted,
    outline = TaupeOutline
)

@Composable
fun HillYeahTheme(
    darkTheme: Boolean = false, // App is bright, luminous, and refreshing (never gloomy black)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    HillYeahTheme(darkTheme = false, dynamicColor = false, content = content)
}
