package io.github.kg6k.soundamplifier.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SoundAmplifierColorScheme = darkColorScheme(
    primary = PrimaryViolet,
    onPrimary = TextPrimary,
    secondary = AccentEmerald,
    onSecondary = TextPrimary,
    tertiary = PrimaryVioletLight,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = CardSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderDark
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
