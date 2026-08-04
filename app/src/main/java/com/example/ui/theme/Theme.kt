package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TailorTealSec,
    secondary = TailorTealPrim,
    tertiary = TailorGoldAccent,
    background = TailorDarkBg,
    surface = TailorDarkSurf,
    onPrimary = TailorDarkBg,
    onSecondary = TailorLightSurf,
    onTertiary = TailorLightSurf,
    onBackground = TailorLightBg,
    onSurface = TailorLightBg
)

private val LightColorScheme = lightColorScheme(
    primary = TailorTealPrim,
    secondary = TailorTealSec,
    tertiary = TailorGoldAccent,
    background = TailorLightBg,
    surface = TailorLightSurf,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF141E1D), // Deep high-contrast charcoal-teal
    onSurface = Color(0xFF141E1D),     // Deep high-contrast charcoal-teal
    primaryContainer = Color(0xFFE0F2F1), // Soft premium sage-teal container
    onPrimaryContainer = Color(0xFF00201F), // Dark rich teal for primary headings
    secondaryContainer = Color(0xFFE6F5F4), // Muted light teal container
    onSecondaryContainer = Color(0xFF042120),
    tertiaryContainer = Color(0xFFFFF2D1), // Soft stitching cream-gold accent container
    onTertiaryContainer = Color(0xFF2C2100),
    surfaceVariant = Color(0xFFF0F5F5), // crisp custom field container background
    onSurfaceVariant = Color(0xFF3F4948), // easily readable medium charcoal
    outline = Color(0xFF707978), // crisp defined borders for fields
    outlineVariant = Color(0xFFD3DFDE) // soft clean dividers
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to false to guarantee eye-catching premium Light Theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
