package io.github.kg6k.soundamplifier.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kg6k.soundamplifier.ui.theme.*

@Composable
fun BoostSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val statusColor by animateColorAsState(
        targetValue = when {
            !enabled || value == 0 -> Color(0xFF64748B)
            value <= 30 -> AccentEmerald
            value <= 70 -> AccentAmber
            else -> AccentRose
        },
        label = "status_color"
    )

    val statusText = when {
        !enabled -> "Disabled"
        value == 0 -> "Off"
        value <= 30 -> "Safe Boost"
        value <= 70 -> "High Volume"
        else -> "Max Boost"
    }

    val dbValue = String.format("+%.1f dB", value / 10f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardSurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Boost Amplification",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Amplifies past normal speaker limits",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Percentage & dB Badge Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (enabled) "+$value%" else "+0%",
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (enabled) statusColor else TextMuted
                )
                Text(
                    text = if (enabled) dbValue else "+0.0 dB",
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expressive Thick Pill Slider
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(TrackBackground)
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { offset ->
                            val newPct = ((offset.x / size.width) * 100).toInt().coerceIn(0, 100)
                            onValueChange(newPct)
                        }
                    }
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newPct = ((change.position.x / size.width) * 100).toInt().coerceIn(0, 100)
                            onValueChange(newPct)
                        }
                    }
            ) {
                val progressFraction = if (enabled) (value / 100f).coerceIn(0f, 1f) else 0f
                val activeWidth = maxWidth * progressFraction

                // Active Gradient Track
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(activeWidth)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PrimaryViolet, statusColor)
                            )
                        )
                )

                // Tactile Thumb Indicator
                if (progressFraction > 0.03f) {
                    Box(
                        modifier = Modifier
                            .offset(x = (maxWidth * progressFraction) - 18.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, statusColor, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(0, 25, 50, 75, 100)
                presets.forEach { preset ->
                    val isSelected = (value == preset && enabled)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = enabled) { onValueChange(preset) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) statusColor.copy(alpha = 0.2f) else CardSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) statusColor else CardBorderDark
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (preset == 0) "0%" else "+$preset%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) statusColor else TextSecondary,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
