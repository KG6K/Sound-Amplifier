package io.github.kg6k.soundamplifier.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.kg6k.soundamplifier.ui.theme.DangerRed
import io.github.kg6k.soundamplifier.ui.theme.SafeGreen
import io.github.kg6k.soundamplifier.ui.theme.WarningAmber

@Composable
fun BoostSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val statusColor = when {
        value == 0 -> Color.Gray
        value <= 30 -> SafeGreen
        value <= 70 -> WarningAmber
        else -> DangerRed
    }

    val statusText = when {
        value == 0 -> "Off"
        value <= 30 -> "Safe Boost"
        value <= 70 -> "High Volume"
        else -> "Max Boost"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Boost Amplification",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Amplifies past normal speaker limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    value = value.toFloat(),
                    onValueChange = { onValueChange(it.toInt()) },
                    valueRange = 0f..100f,
                    steps = 19, // 5% increments
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = statusColor,
                        activeTrackColor = statusColor
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "+$value%",
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor
                )
            }
        }
    }
}
