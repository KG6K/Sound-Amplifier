package io.github.kg6k.soundamplifier.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ThemePrimaryDark,
    onPrimary = ThemeOnPrimaryDark,
    primaryContainer = ThemePrimaryContainerDark,
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    background = SurfaceBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceContainerDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceContainerHighDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = ThemePrimaryLight,
    onPrimary = ThemeOnPrimaryLight,
    primaryContainer = ThemePrimaryContainerLight,
    secondaryContainer = Color(0xFFEDE7F6),
    onSecondaryContainer = Color(0xFF5E35B1),
    background = SurfaceBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceContainerLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceContainerHighLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = SurfaceBorderLight
)

@Composable
fun SoundAmplifierTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
