package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CosmoLabDarkColorScheme = darkColorScheme(
    primary = CosmicPurplePrimary,
    onPrimary = CosmicPurpleOnPrimary,
    primaryContainer = CosmicPurpleContainer,
    onPrimaryContainer = CosmicPurpleOnContainer,
    secondary = CosmicPurplePrimary,
    onSecondary = CosmicPurpleOnPrimary,
    background = SpaceBackground,
    onBackground = TextPrimary,
    surface = SpaceCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SpaceCardSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SpaceBorder,
    outlineVariant = SpaceBorderSubtle
)

@Composable
fun CosmoLabSkyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmoLabDarkColorScheme,
        typography = Typography,
        content = content
    )
}
