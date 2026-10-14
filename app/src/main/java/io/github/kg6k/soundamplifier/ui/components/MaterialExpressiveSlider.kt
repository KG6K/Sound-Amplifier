package io.github.kg6k.soundamplifier.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

/**
 * Sleek modern line slider with round elevated thumb,
 * horizontal status pill, and one-tap quick preset chips.
 * Fine-tune strip removed for clean, minimal UI.
 * Adapts automatically to system light/dark theme.
 */
@Composable
fun MaterialExpressiveSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    sublabel: String = "",
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 100,
    step: Int = 1,
    isBoostMode: Boolean = true,
    enabled: Boolean = true,
    prefix: String = if (isBoostMode) "+" else "",
    unit: String = "%",
    presets: List<Int> = emptyList()
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    // Dynamic color coding based on level
    val trackColor by animateColorAsState(
        targetValue = when {
            value == 0 -> if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
            isBoostMode && value <= 30 -> EmeraldSafe
            isBoostMode && value <= 60 -> AmberWarning
            isBoostMode -> RedDanger
            else -> colorScheme.primary
        },
        label = "track_color"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Label and Tabular Value Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                )

                // Fixed-dimension Stable Value Display (Does not jitter or resize)
                Surface(
                    modifier = Modifier.width(56.dp).height(24.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = trackColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, trackColor.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$prefix$value",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = trackColor
                            )
                            Text(
                                text = unit,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = trackColor,
                                modifier = Modifier.padding(bottom = 1.dp, start = 1.dp)
                            )
                        }
                    }
                }
            }

            if (sublabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sublabel,
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Sleek Modern Line Slider Track & Round Thumb
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
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
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                val fraction = ((value - min).toFloat() / (max - min)).coerceIn(0f, 1f)

                // 1. Inactive Background Track Line (6dp height)
                val inactiveTrackColor = if (isDark) Color(0xFF262A36) else Color(0xFFE2E8F0)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(inactiveTrackColor)
                )

                // 2. Active Filled Track Line
                val activeWidth = maxWidth * fraction
                if (activeWidth > 2.dp) {
                    Box(
                        modifier = Modifier
                            .width(activeWidth)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(trackColor)
                    )
                }

                // 3. Smooth Circular Elevated Thumb (18dp diameter)
                val thumbSize = 18.dp
                val thumbOffset = ((maxWidth - thumbSize) * fraction).coerceAtLeast(0.dp)

                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .size(thumbSize)
                        .shadow(elevation = 3.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.5.dp, trackColor, CircleShape)
                )
            }

            // Quick Preset Chips (e.g. 0%, 20%, 40%, 60%, 80%, 100%)
            if (isBoostMode && presets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val chipInactiveBg = if (isDark) Color(0xFF1E212B) else Color(0xFFF1F5F9)
                    val chipInactiveBorder = if (isDark) Color(0xFF2C3242) else Color(0xFFCBD5E1)

                    presets.forEach { preset ->
                        val isSelected = (value == preset)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onValueChange(preset) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) trackColor.copy(alpha = 0.2f) else chipInactiveBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) trackColor else chipInactiveBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$prefix$preset%",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) trackColor else colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
