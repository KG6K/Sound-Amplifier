package io.github.kg6k.soundamplifier.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kg6k.soundamplifier.ui.theme.*

@Composable
fun AudioVisualizerWave(
    boostPercent: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val dbGain = String.format("+%.1f dB (%.1fx)", (boostPercent / 100f) * 12f, 1f + (boostPercent / 100f))
    val barColor = when {
        !isActive || boostPercent == 0 -> Color(0xFF475569)
        boostPercent > 70 -> RedDanger
        boostPercent > 30 -> AmberWarning
        else -> ThemePrimary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulse Activity Icon
                Canvas(modifier = Modifier.size(14.dp)) {
                    val path = Path().apply {
                        moveTo(0f, size.height * 0.5f)
                        lineTo(size.width * 0.25f, size.height * 0.5f)
                        lineTo(size.width * 0.4f, size.height * 0.1f)
                        lineTo(size.width * 0.6f, size.height * 0.9f)
                        lineTo(size.width * 0.75f, size.height * 0.5f)
                        lineTo(size.width, size.height * 0.5f)
                    }
                    drawPath(path, color = ThemePrimary, style = Stroke(width = 2.dp.toPx()))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Output Spectrum Power",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
            }

            Text(
                text = if (isActive && boostPercent > 0) "Gain $dbGain" else "0.0 dB (Bypassed)",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Spectrum Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0A0C12))
                .border(1.dp, Color(0xFF1E2433), RoundedCornerShape(18.dp))
        ) {
            // Bars Canvas
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp)) {
                val barCount = 32
                val width = size.width
                val height = size.height
                val barWidth = (width / barCount) * 0.62f
                val gap = (width - barCount * barWidth) / (barCount + 1)

                val effectiveVolume = if (isActive) (1f + boostPercent / 100f) * 0.5f else 0.08f

                for (i in 0 until barCount) {
                    val freqWave = (
                        Math.sin(time.toDouble() * 2.0 + i * 0.35) * 0.4 +
                        Math.sin(time.toDouble() * 3.5 - i * 0.5) * 0.3 +
                        Math.cos(time.toDouble() * 1.2 + i * 0.2) * 0.3
                    ).toFloat()

                    val envelope = Math.sin((i.toFloat() / barCount) * Math.PI).toFloat()
                    val normalized = (0.08f + ((freqWave + 1f) * 0.45f * effectiveVolume * envelope))
                        .coerceIn(0.04f, 0.95f)

                    val barHeight = (normalized * (height - 12f)).coerceAtLeast(4.dp.toPx())
                    val x = gap + i * (barWidth + gap)
                    val y = height / 2f - barHeight / 2f

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            // Top Right Badge Overlay
            Text(
                text = if (isActive && boostPercent > 0) "+$boostPercent% BOOST" else "MUTED",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 12.dp),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isActive && boostPercent > 0) barColor else Color(0xFF64748B)
            )
        }
    }
}
