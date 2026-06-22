package dev.ferrero

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Ferrero Dev brand palette
object Brand {
    val ForestGreen = Color(0xFF1F3D2B)
    val ForestGreenDark = Color(0xFF152A1E)
    val Cream = Color(0xFFF4F1E8)
    val CreamMuted = Color(0xFFE7E2D3)
    val Charcoal = Color(0xFF1A1A18)
    val CharcoalMuted = Color(0xFF4A4A45)
}

private val LightColorScheme = lightColorScheme(
    primary = Brand.ForestGreen,
    onPrimary = Brand.Cream,
    background = Brand.Cream,
    onBackground = Brand.Charcoal,
    surface = Brand.Cream,
    onSurface = Brand.Charcoal,
    surfaceVariant = Brand.CreamMuted,
    onSurfaceVariant = Brand.CharcoalMuted,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4C7B5D), // Lighter green for dark mode contrast
    onPrimary = Brand.Charcoal,
    background = Brand.Charcoal,
    onBackground = Brand.Cream,
    surface = Brand.Charcoal,
    onSurface = Brand.Cream,
    surfaceVariant = Color(0xFF242422), // Subtle charcoal variant
    onSurfaceVariant = Brand.CreamMuted,
)

@Composable
fun FerreroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
