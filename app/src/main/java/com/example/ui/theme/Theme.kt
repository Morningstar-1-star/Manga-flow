package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppSettings

@Composable
fun KotatsuTheme(
    appSettings: AppSettings = AppSettings(),
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isAmoled = appSettings.isAmoledBlack || appSettings.colorScheme.equals("AMOLED Black", ignoreCase = true)
    
    val bg = if (isAmoled) AmoledBlackBg else KotatsuDarkBg
    val surface = if (isAmoled) AmoledBlackSurface else KotatsuDarkSurface
    val surfaceVariant = if (isAmoled) AmoledBlackSurfaceVariant else KotatsuDarkSurfaceVariant

    val (primaryColor, primaryContainerColor) = when (appSettings.colorScheme.lowercase()) {
        "black & white", "monochrome" -> ThemeMonochromeWhite to ThemeMonochromeContainer
        "manga yellow", "yellow", "asuka" -> ThemeMangaYellow to ThemeMangaYellowContainer
        "pink", "kawaii pink", "miku" -> ThemeKawaiiPink to ThemeKawaiiPinkContainer
        "silver", "silver slate" -> ThemeSilverSlate to ThemeSilverSlateContainer
        "expressive", "purple" -> ThemeExpressivePurple to ThemeExpressivePurpleContainer
        else -> KotatsuTeal to KotatsuTealContainer
    }

    val dynamicColorScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.Black,
        primaryContainer = primaryContainerColor,
        onPrimaryContainer = primaryColor,
        secondary = KotatsuBlue,
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF0C4A6E),
        onSecondaryContainer = KotatsuBlue,
        tertiary = KotatsuRose,
        onTertiary = Color.White,
        tertiaryContainer = KotatsuRoseContainer,
        onTertiaryContainer = Color(0xFFFECDD3),
        background = bg,
        onBackground = KotatsuTextPrimary,
        surface = surface,
        onSurface = KotatsuTextPrimary,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = KotatsuTextSecondary,
        surfaceContainerHighest = KotatsuDarkSurfaceHigh,
        outline = if (isAmoled) Color(0xFF1F1F1F) else KotatsuCardBorder,
        outlineVariant = Color(0xFF1E293B)
    )

    MaterialTheme(
        colorScheme = dynamicColorScheme,
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


