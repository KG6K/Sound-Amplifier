package io.github.kg6k.soundamplifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import io.github.kg6k.soundamplifier.data.BoosterPreferences
import io.github.kg6k.soundamplifier.data.CrashReporter
import io.github.kg6k.soundamplifier.ui.components.MaterialExpressiveSlider
import io.github.kg6k.soundamplifier.ui.theme.*

class MainActivity : ComponentActivity() {

    private lateinit var prefs: BoosterPreferences
    private lateinit var audioManager: AudioManager

    private val boostPercentState = mutableIntStateOf(0)

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            syncWithPreferences()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = BoosterPreferences(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        syncWithPreferences()

        try {
            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            registerReceiver(volumeReceiver, filter)
        } catch (ignored: Throwable) {}

        setContent {
            SoundAmplifierTheme {
                val isDark = isSystemInDarkTheme()
                SideEffect {
                    val bgColor = if (isDark) {
                        android.graphics.Color.parseColor("#141218")
                    } else {
                        android.graphics.Color.parseColor("#F8F9FA")
                    }
                    window.statusBarColor = bgColor
                    window.navigationBarColor = bgColor

                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !isDark
                    insetsController.isAppearanceLightNavigationBars = !isDark
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        syncWithPreferences()
    }

    private fun syncWithPreferences() {
        boostPercentState.intValue = prefs.getBoostPercent()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(volumeReceiver)
        } catch (ignored: Throwable) {}
        super.onDestroy()
    }

    @Composable
    fun MainScreen() {
        var boostPercent by boostPercentState
        val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
        var systemVolume by remember {
            mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
        }

        // Automatic Crash Recovery Dialog (no permanent UI clutter)
        var showCrashDialog by remember {
            mutableStateOf(CrashReporter.getLatestCrashLog(this@MainActivity) != null)
        }

        if (showCrashDialog) {
            AlertDialog(
                onDismissRequest = {
                    CrashReporter.clearLogs(this@MainActivity)
                    showCrashDialog = false
                },
                title = {
                    Text(
                        text = "Crash Report Saved",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "Sound Amplifier detected an unexpected error and saved the diagnostic report to storage.\n\nPath: ${CrashReporter.getStorageLocation(this@MainActivity)}\n\nWould you like to share or export this report?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            CrashReporter.shareLatestCrashLog(this@MainActivity)
                            CrashReporter.clearLogs(this@MainActivity)
                            showCrashDialog = false
                        }
                    ) {
                        Text("Share / Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            CrashReporter.clearLogs(this@MainActivity)
                            showCrashDialog = false
                        }
                    ) {
                        Text("Dismiss")
                    }
                }
            )
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                // Header Bar: Sleek, Adaptive, Zero switches
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val path = Path().apply {
                                        moveTo(size.width * 0.55f, 0f)
                                        lineTo(size.width * 0.1f, size.height * 0.55f)
                                        lineTo(size.width * 0.5f, size.height * 0.55f)
                                        lineTo(size.width * 0.45f, size.height)
                                        lineTo(size.width * 0.9f, size.height * 0.45f)
                                        lineTo(size.width * 0.5f, size.height * 0.45f)
                                        close()
                                    }
                                    drawPath(path, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sound Amplifier",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (boostPercent > 0) {
                                    "Amplification active (+${(boostPercent * 40) / 100} dB)"
                                } else {
                                    "Amplification idle (0%)"
                                },
                                fontSize = 11.sp,
                                color = if (boostPercent > 0) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. PRIMARY: Boost Amplification Modern Line Slider
                // Zero switches: Slider directly drives the audio booster (0% = off, >0% = active)
                MaterialExpressiveSlider(
                    value = boostPercent,
                    onValueChange = { newPercent ->
                        boostPercent = newPercent
                        prefs.setBoostPercent(newPercent)
                        if (newPercent > 0) {
                            VolumeBoosterService.setBoost(this@MainActivity, newPercent)
                        } else {
                            VolumeBoosterService.stopService(this@MainActivity)
                        }
                    },
                    label = "Boost Amplification",
                    sublabel = "Amplifies audio volume past standard 100% Android speaker ceiling",
                    isBoostMode = true,
                    enabled = true,
                    presets = listOf(0, 20, 40, 60, 80, 100)
                )

                // 2. Standard System Media Volume Line Slider
                val systemVolumePercent = ((systemVolume.toFloat() / maxVolume) * 100).toInt()
                MaterialExpressiveSlider(
                    value = systemVolumePercent,
                    onValueChange = { newPct ->
                        val newVol = ((newPct / 100f) * maxVolume).toInt().coerceIn(0, maxVolume)
                        systemVolume = newVol
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                    },
                    label = "System Media Volume",
                    sublabel = "Standard device audio stream level (0% to 100%)",
                    isBoostMode = false,
                    enabled = true,
                    prefix = "",
                    unit = "%"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
