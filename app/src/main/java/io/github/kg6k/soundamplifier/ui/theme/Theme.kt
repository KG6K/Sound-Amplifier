package io.github.kg6k.soundamplifier.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SoundAmplifierColorScheme = darkColorScheme(
    primary = ThemePrimary,
    onPrimary = ThemeOnPrimary,
    primaryContainer = ThemePrimaryContainer,
    background = SurfaceBackground,
    onBackground = TextPrimaryWhite,
    surface = SurfaceContainer,
    onSurface = TextPrimaryWhite,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextSecondaryMuted,
    outline = SurfaceBorder
)

@Composable
fun SoundAmplifierTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SoundAmplifierColorScheme,
        content = content
    )
}
