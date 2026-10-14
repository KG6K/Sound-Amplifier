package io.github.kg6k.soundamplifier.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kg6k.soundamplifier.ui.theme.*

@Composable
fun MaterialExpressiveSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    sublabel: String,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 100,
    step: Int = 5,
    isBoostMode: Boolean = true,
    enabled: Boolean = true,
    prefix: String = if (isBoostMode) "+" else "",
    unit: String = "%"
) {
    // Determine color and status badge based on level
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled || value == 0 -> Color(0xFF64748B)
            isBoostMode && value <= 30 -> EmeraldSafe
            isBoostMode && value <= 70 -> AmberWarning
            isBoostMode -> RedDanger
            else -> ThemePrimary
        },
        label = "track_color"
    )

    val (badgeText, badgeBg, badgeTextColor, badgeBorder) = when {
        !enabled -> Quad("Disabled", Color(0xFF1E293B), Color(0xFF94A3B8), Color(0xFF334155))
        value == 0 -> Quad("Off", Color(0xFF1E293B), Color(0xFF94A3B8), Color(0xFF334155))
        isBoostMode && value <= 30 -> Quad("Safe Boost", EmeraldBackground, EmeraldSafe, EmeraldSafe.copy(alpha = 0.4f))
        isBoostMode && value <= 70 -> Quad("High Volume", AmberBackground, AmberWarning, AmberWarning.copy(alpha = 0.4f))
        isBoostMode -> Quad("Max Boost", RedBackground, RedDanger, RedDanger.copy(alpha = 0.4f))
        else -> Quad("Normal", Color(0xFF1E293B), ThemePrimary, ThemePrimary.copy(alpha = 0.4f))
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Label, Status Badge, and Value Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isBoostMode) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = badgeBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder)
                            ) {
                                Text(
                                    text = badgeText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = badgeTextColor
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = sublabel,
                        fontSize = 12.sp,
                        color = TextSecondaryMuted,
                        lineHeight = 16.sp
                    )
                }

                // Big Tabular Value Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = trackColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, trackColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (enabled) "$prefix$value" else "+0",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = trackColor
                        )
                        Text(
                            text = unit,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = trackColor,
                            modifier = Modifier.padding(bottom = 3.dp, start = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expressive Thick 56dp Track Slider
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(TrackBackgroundDark)
                    .border(1.dp, TrackBorder, RoundedCornerShape(18.dp))
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            val rawVal = min + (fraction * (max - min)).toInt()
                            val stepped = Math.round(rawVal.toFloat() / step) * step
                            onValueChange(stepped.coerceIn(min, max))
                        }
                    }
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            val rawVal = min + (fraction * (max - min)).toInt()
                            val stepped = Math.round(rawVal.toFloat() / step) * step
                            onValueChange(stepped.coerceIn(min, max))
                        }
                    }
            ) {
                val fraction = if (enabled) ((value - min).toFloat() / (max - min)).coerceIn(0f, 1f) else 0f
                val activeWidth = (maxWidth - 8.dp) * fraction

                // Active Filled Color Track
                if (activeWidth > 2.dp) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxHeight()
                            .width(activeWidth)
                            .clip(RoundedCornerShape(14.dp))
                            .background(trackColor)
                    )
                }

                // Background Grid Notches at 0%, 25%, 50%, 75%, 100%
                val ticks = listOf(0, 25, 50, 75, 100)
                ticks.forEach { tick ->
                    val tickPct = (tick - min).toFloat() / (max - min)
                    val isPassed = fraction >= tickPct
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (maxWidth * tickPct) - 2.dp)
                            .size(width = 3.dp, height = 14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPassed) Color(0x33000000) else Color(0x4464748B))
                    )
                }

                // Distinctive White Tactile Capsule Thumb ( )
                val thumbWidth = 24.dp
                val thumbOffset = ((maxWidth - thumbWidth - 8.dp) * fraction) + 4.dp

                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset, y = 6.dp)
                        .size(width = thumbWidth, height = 44.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(ThumbWhite)
                ) {
                    // Tactile vertical grip groove in thumb
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 3.dp, height = 16.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFF94A3B8))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Floating Tick Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(0, 25, 50, 75, 100).forEach { tick ->
                    val isSelected = (value == tick && enabled)
                    Text(
                        text = "$prefix$tick$unit",
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = enabled) { onValueChange(tick) }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimaryWhite else TextSecondaryMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = SurfaceBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
            )

            // Fine-Tuning Step Controls Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fine-tune",
                    fontSize = 12.sp,
                    color = TextSecondaryMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Decrement Button
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = enabled) {
                                onValueChange((value - step).coerceAtLeast(min))
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "−",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryWhite
                            )
                        }
                    }

                    // Increment Button
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = enabled) {
                                onValueChange((value + step).coerceAtMost(max))
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "+",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
