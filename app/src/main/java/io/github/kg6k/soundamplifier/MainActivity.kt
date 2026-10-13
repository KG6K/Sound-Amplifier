package io.github.kg6k.soundamplifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import io.github.kg6k.soundamplifier.data.BoosterPreferences
import io.github.kg6k.soundamplifier.ui.components.AudioVisualizerWave
import io.github.kg6k.soundamplifier.ui.components.BoostSlider
import io.github.kg6k.soundamplifier.ui.theme.*

class MainActivity : ComponentActivity() {

    private lateinit var prefs: BoosterPreferences
    private lateinit var audioManager: AudioManager

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Trigger recomposition when system hardware volume buttons are pressed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = BoosterPreferences(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        try {
            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            registerReceiver(volumeReceiver, filter)
        } catch (ignored: Throwable) {}

        setContent {
            SoundAmplifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(volumeReceiver)
        } catch (ignored: Throwable) {}
        super.onDestroy()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        var boostPercent by remember { mutableIntStateOf(prefs.getBoostPercent()) }
        var isBoostActive by remember { mutableStateOf(prefs.isBoostActive()) }
        val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
        var systemVolume by remember {
            mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
        }

        Scaffold(
            containerColor = BackgroundDark,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Sound Amplifier",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BackgroundDark
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Live Audio Spectrum Visualizer
                AudioVisualizerWave(
                    boostPercent = boostPercent,
                    isActive = isBoostActive
                )

                // Master Boost Amplification Slider (0% to +100%)
                BoostSlider(
                    value = boostPercent,
                    onValueChange = { newPercent ->
                        boostPercent = newPercent
                        prefs.setBoostPercent(newPercent)
                        if (isBoostActive) {
                            VolumeBoosterService.updatePercent(this@MainActivity, newPercent)
                        }
                    },
                    enabled = isBoostActive
                )

                // System Media Volume Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = CardSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
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
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Standard Android audio stream",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            val volPct = ((systemVolume.toFloat() / maxVolume) * 100).toInt()
                            Text(
                                text = "$volPct%",
                                fontSize = 20.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = PrimaryVioletLight
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Tactile Thick Pill Slider for System Volume
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(TrackBackground)
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                        val newVol = (fraction * maxVolume).toInt()
                                        systemVolume = newVol
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, _ ->
                                        change.consume()
                                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                        val newVol = (fraction * maxVolume).toInt()
                                        systemVolume = newVol
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    }
                                }
                        ) {
                            val fraction = (systemVolume.toFloat() / maxVolume).coerceIn(0f, 1f)
                            val activeWidth = maxWidth * fraction

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(activeWidth)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(PrimaryVioletDark, PrimaryViolet)
                                        )
                                    )
                            )
                        }
                    }
                }

                // Master Toggle Button
                val buttonBrush = if (isBoostActive) {
                    Brush.horizontalGradient(listOf(PrimaryViolet, AccentEmerald))
                } else {
                    Brush.horizontalGradient(listOf(CardSurfaceVariant, CardBorderDark))
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Button(
                        onClick = {
                            isBoostActive = !isBoostActive
                            prefs.setBoostActive(isBoostActive)
                            if (isBoostActive) {
                                VolumeBoosterService.startService(this@MainActivity, boostPercent)
                            } else {
                                VolumeBoosterService.stopService(this@MainActivity)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(buttonBrush),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isBoostActive) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AccentEmerald)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Amplification Active · Tap to Pause",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            } else {
                                Text(
                                    text = "Activate Amplification",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // Quick Settings Tile Hint Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryVioletLight)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Settings Tile",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Swipe down your Android notification panel and edit tiles to add the Sound Amplifier tile for one-tap volume boost from any app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
