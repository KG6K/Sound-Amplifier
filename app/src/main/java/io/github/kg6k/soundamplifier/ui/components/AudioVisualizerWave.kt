package io.github.kg6k.soundamplifier.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.kg6k.soundamplifier.ui.theme.AccentEmerald
import io.github.kg6k.soundamplifier.ui.theme.CardSurfaceDark
import io.github.kg6k.soundamplifier.ui.theme.PrimaryViolet
import io.github.kg6k.soundamplifier.ui.theme.PrimaryVioletLight

@Composable
fun AudioVisualizerWave(
    boostPercent: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardSurfaceDark
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            val barCount = 28
            val totalWidth = size.width
            val barSpacing = 4.dp.toPx()
            val barWidth = (totalWidth - (barCount - 1) * barSpacing) / barCount
            val maxBarHeight = size.height
            val gainFactor = if (isActive) (boostPercent / 100f).coerceIn(0.15f, 1f) else 0.08f

            val brush = Brush.verticalGradient(
                colors = if (isActive) {
                    listOf(PrimaryVioletLight, PrimaryViolet, AccentEmerald)
                } else {
                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                }
            )

            for (i in 0 until barCount) {
                val normalizedIndex = i.toFloat() / barCount
                // Symmetric bell curve envelope centered in the middle
                val envelope = Math.sin(normalizedIndex * Math.PI).toFloat()

                val wave = Math.sin(phase.toDouble() * 2.0 + i * 0.45).toFloat()
                val wave2 = Math.cos(phase.toDouble() * 1.5 - i * 0.3).toFloat()
                val combinedWave = ((wave + wave2 + 2f) / 4f)

                val barHeight = (maxBarHeight * (0.15f + 0.85f * combinedWave * envelope * gainFactor))
                    .coerceIn(4.dp.toPx(), maxBarHeight)

                val left = i * (barWidth + barSpacing)
                val top = size.height - barHeight

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
}
