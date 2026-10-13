package io.github.kg6k.soundamplifier

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SoundAmplifierScreen(audioManager)
                }
            }
        }
    }

    @Composable
    fun SoundAmplifierScreen(audioManager: AudioManager) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        var mediaVolume by remember { mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }
        var boostPercent by remember { mutableFloatStateOf(50f) }
        var isBoostActive by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sound Amplifier",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Boost Amplification: +${boostPercent.toInt()}%",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = boostPercent,
                onValueChange = {
                    boostPercent = it
                    updateServiceBoost(it.toInt())
                },
                valueRange = 0f..100f,
                steps = 19
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Media Volume: ${(mediaVolume / maxVolume * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = mediaVolume,
                onValueChange = {
                    mediaVolume = it
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
                },
                valueRange = 0f..maxVolume.toFloat()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isBoostActive = !isBoostActive
                    toggleBoostService(isBoostActive, boostPercent.toInt())
                }
            ) {
                Text(if (isBoostActive) "Pause Amplification" else "Activate Amplification")
            }
        }
    }

    private fun updateServiceBoost(percent: Int) {
        val intent = Intent(this, VolumeBoosterService::class.java).apply {
            action = VolumeBoosterService.ACTION_SET_PERCENT
            putExtra("percent", percent)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun toggleBoostService(active: Boolean, percent: Int) {
        val intent = Intent(this, VolumeBoosterService::class.java).apply {
            action = if (active) VolumeBoosterService.ACTION_ENABLE_BOOST else VolumeBoosterService.ACTION_DISABLE_BOOST
            putExtra("percent", percent)
        }
        ContextCompat.startForegroundService(this, intent)
    }
}
