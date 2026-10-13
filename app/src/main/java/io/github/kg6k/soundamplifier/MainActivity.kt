package io.github.kg6k.soundamplifier

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.kg6k.soundamplifier.data.BoosterPreferences
import io.github.kg6k.soundamplifier.ui.components.BoostSlider
import io.github.kg6k.soundamplifier.ui.theme.SoundAmplifierTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: BoosterPreferences
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = BoosterPreferences(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        setContent {
            SoundAmplifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        var boostPercent by remember { mutableIntStateOf(prefs.getBoostPercent()) }
        var isBoostActive by remember { mutableStateOf(prefs.isBoostActive()) }
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        var systemVolume by remember {
            mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Sound Amplifier",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Primary Boost Amplification Slider (0% to +100%)
                BoostSlider(
                    value = if (isBoostActive) boostPercent else 0,
                    onValueChange = { newPercent ->
                        boostPercent = newPercent
                        prefs.setBoostPercent(newPercent)
                        if (isBoostActive) {
                            sendBoostIntent(VolumeBoosterService.ACTION_SET_PERCENT, newPercent)
                        }
                    },
                    enabled = isBoostActive
                )

                // Base System Media Volume Slider (0% to 100%)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
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
                                    text = "System Media Volume",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Standard Android audio stream",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${((systemVolume.toFloat() / maxVolume) * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Slider(
                            value = systemVolume.toFloat(),
                            onValueChange = {
                                systemVolume = it.toInt()
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
                            },
                            valueRange = 0f..maxVolume.toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Master Power Button
                Button(
                    onClick = {
                        isBoostActive = !isBoostActive
                        prefs.setBoostActive(isBoostActive)
                        val action = if (isBoostActive) {
                            VolumeBoosterService.ACTION_ENABLE_BOOST
                        } else {
                            VolumeBoosterService.ACTION_DISABLE_BOOST
                        }
                        sendBoostIntent(action, boostPercent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isBoostActive) "Pause Amplification" else "Activate Amplification",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Quick Settings Tile Info Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quick Settings Tile",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pull down your notification shade and edit tiles to add the Sound Amplifier tile for instant one-tap control.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    private fun sendBoostIntent(action: String, percent: Int) {
        val intent = Intent(this, VolumeBoosterService::class.java).apply {
            this.action = action
            putExtra("percent", percent)
        }
        ContextCompat.startForegroundService(this, intent)
    }
}
