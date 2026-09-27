package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = KotatsuTeal,
    onPrimary = KotatsuOnTeal,
    primaryContainer = KotatsuTealContainer,
    onPrimaryContainer = KotatsuTeal,
    secondary = KotatsuBlue,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = KotatsuBlue,
    tertiary = KotatsuRose,
    onTertiary = Color.White,
    tertiaryContainer = KotatsuRoseContainer,
    onTertiaryContainer = Color(0xFFFECDD3),
    background = KotatsuDarkBg,
    onBackground = KotatsuTextPrimary,
    surface = KotatsuDarkSurface,
    onSurface = KotatsuTextPrimary,
    surfaceVariant = KotatsuDarkSurfaceVariant,
    onSurfaceVariant = KotatsuTextSecondary,
    surfaceContainerHighest = KotatsuDarkSurfaceHigh,
    outline = KotatsuCardBorder,
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = darkColorScheme(
    // Default to the signature dark theme matching screenshots, since manga reader is primarily dark
    primary = KotatsuTeal,
    onPrimary = KotatsuOnTeal,
    primaryContainer = KotatsuTealContainer,
    onPrimaryContainer = KotatsuTeal,
    secondary = KotatsuBlue,
    onSecondary = Color.Black,
    background = KotatsuDarkBg,
    onBackground = KotatsuTextPrimary,
    surface = KotatsuDarkSurface,
    onSurface = KotatsuTextPrimary,
    surfaceVariant = KotatsuDarkSurfaceVariant,
    onSurfaceVariant = KotatsuTextSecondary,
    outline = KotatsuCardBorder
)

@Composable
fun KotatsuTheme(
    darkTheme: Boolean = true, // Default to true as in reference screenshots
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    KotatsuTheme(content = content)
}

