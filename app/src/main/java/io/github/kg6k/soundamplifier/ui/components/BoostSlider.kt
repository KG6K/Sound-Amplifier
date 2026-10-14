package io.github.kg6k.soundamplifier.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BoostSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    MaterialExpressiveSlider(
        value = value,
        onValueChange = onValueChange,
        label = "Boost Amplification",
        sublabel = "Increases volume past normal 100% Android speaker ceiling",
        modifier = modifier,
        isBoostMode = true,
        enabled = enabled
    )
}
