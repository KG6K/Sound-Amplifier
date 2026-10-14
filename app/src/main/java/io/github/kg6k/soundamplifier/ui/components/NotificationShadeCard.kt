package io.github.kg6k.soundamplifier.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kg6k.soundamplifier.ui.theme.*

@Composable
fun NotificationShadeCard(
    boostPercent: Int,
    isActive: Boolean,
    onToggleBoost: () -> Unit,
    onQuickAdd: (Int) -> Unit,
    onStopService: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1E1D24),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2A33))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: ⚡ Sound Amplifier · Running in background
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Circular Purple Icon with Lightning Bolt
                Surface(
                    modifier = Modifier.size(18.dp),
                    shape = CircleShape,
                    color = ThemePrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.55f, 0f)
                                lineTo(size.width * 0.1f, size.height * 0.55f)
                                lineTo(size.width * 0.5f, size.height * 0.55f)
                                lineTo(size.width * 0.45f, size.height)
                                lineTo(size.width * 0.9f, size.height * 0.45f)
                                lineTo(size.width * 0.5f, size.height * 0.45f)
                                close()
                            }
                            drawPath(path, color = ThemeOnPrimary)
                        }
                    }
                }

                Text(
                    text = "Sound Amplifier",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryWhite
                )

                Text(
                    text = if (isActive) "· Running in background" else "· Paused",
                    fontSize = 11.sp,
                    color = TextSecondaryMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Row: Title, Subtitle, and +50% Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = if (isActive) "Amplification Active (+$boostPercent%)" else "Amplification Paused (0%)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isActive) "Loudness enhancement applied to media audio" else "Tap to resume background boost",
                        fontSize = 11.sp,
                        color = TextSecondaryMuted
                    )
                }

                // Chip Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isActive) ThemePrimary.copy(alpha = 0.18f) else Color(0xFF2C2B33)
                ) {
                    Text(
                        text = if (isActive) "+$boostPercent%" else "Muted",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) ThemePrimary else Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = Color(0xFF2E2C38),
                thickness = 1.dp,
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
            )

            // Actions Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Boost Pill
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleBoost() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF26242E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B3847))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isActive) "Pause Boost" else "Resume Boost",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryWhite
                        )
                    }
                }

                // +20% Pill Button
                Surface(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onQuickAdd(20) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF26242E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B3847))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "+ +20%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryWhite
                        )
                    }
                }

                // Power Off Button (Red)
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onStopService() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2D1619),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A1F24))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Power Icon ⏻
                        Canvas(modifier = Modifier.size(13.dp)) {
                            // Circular arc
                            drawArc(
                                color = Color(0xFFEF4444),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                            // Vertical line at top
                            drawLine(
                                color = Color(0xFFEF4444),
                                start = Offset(size.width * 0.5f, 0f),
                                end = Offset(size.width * 0.5f, size.height * 0.5f),
                                strokeWidth = 1.8.dp.toPx()
                            )
                        }
                    }
                }
            }
        }
    }
}
