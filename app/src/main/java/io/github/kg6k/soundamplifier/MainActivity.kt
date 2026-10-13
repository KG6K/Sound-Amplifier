package io.github.kg6k.soundamplifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import io.github.kg6k.soundamplifier.data.BoosterPreferences
import io.github.kg6k.soundamplifier.ui.components.AudioVisualizerWave
import io.github.kg6k.soundamplifier.ui.components.MaterialExpressiveSlider
import io.github.kg6k.soundamplifier.ui.components.NotificationShadeCard
import io.github.kg6k.soundamplifier.ui.theme.*

class MainActivity : ComponentActivity() {

    private lateinit var prefs: BoosterPreferences
    private lateinit var audioManager: AudioManager

    private val boostPercentState = mutableIntStateOf(50)
    private val isBoostActiveState = mutableStateOf(true)

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            syncWithPreferences()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = BoosterPreferences(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Match system status bar precisely to #141218
        val bgColor = Color.parseColor("#141218")
        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        syncWithPreferences()

        try {
            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            registerReceiver(volumeReceiver, filter)
        } catch (ignored: Throwable) {}

        setContent {
            SoundAmplifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceBackground
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
        isBoostActiveState.value = prefs.isBoostActive()
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
        var boostPercent by boostPercentState
        var isBoostActive by isBoostActiveState
        val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
        var systemVolume by remember {
            mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
        }

        Scaffold(
            containerColor = SurfaceBackground,
            topBar = {
                // Header Bar matching Screenshot_20261014-010845.jpg
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceBackground
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Circular purple icon with lightning bolt + Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isBoostActive) ThemePrimary else Color(0xFF2A2830)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Canvas(modifier = Modifier.size(20.dp)) {
                                        val path = Path().apply {
                                            moveTo(size.width * 0.55f, 0f)
                                            lineTo(size.width * 0.1f, size.height * 0.55f)
                                            lineTo(size.width * 0.5f, size.height * 0.55f)
                                            lineTo(size.width * 0.45f, size.height)
                                            lineTo(size.width * 0.9f, size.height * 0.45f)
                                            lineTo(size.width * 0.5f, size.height * 0.45f)
                                            close()
                                        }
                                        drawPath(
                                            path,
                                            color = if (isBoostActive) ThemeOnPrimary else Color(0xFF999999)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sound Amplifier",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryWhite
                            )
                        }

                        // Right: Master Volume/Mute button with purple border
                        Surface(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    isBoostActive = !isBoostActive
                                    prefs.setBoostActive(isBoostActive)
                                    if (isBoostActive) {
                                        VolumeBoosterService.startService(this@MainActivity, boostPercent)
                                    } else {
                                        VolumeBoosterService.stopService(this@MainActivity)
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isBoostActive) Color(0xFF2E2248) else Color(0xFF1E1D24),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isBoostActive) ThemePrimary.copy(alpha = 0.8f) else SurfaceBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Speaker Icon
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val iconColor = if (isBoostActive) ThemePrimary else Color(0xFF64748B)
                                    // Speaker body
                                    val body = Path().apply {
                                        moveTo(size.width * 0.1f, size.height * 0.35f)
                                        lineTo(size.width * 0.35f, size.height * 0.35f)
                                        lineTo(size.width * 0.6f, size.height * 0.15f)
                                        lineTo(size.width * 0.6f, size.height * 0.85f)
                                        lineTo(size.width * 0.35f, size.height * 0.65f)
                                        lineTo(size.width * 0.1f, size.height * 0.65f)
                                        close()
                                    }
                                    drawPath(body, color = iconColor)

                                    if (isBoostActive) {
                                        // Sound wave arc
                                        drawArc(
                                            color = iconColor,
                                            startAngle = -45f,
                                            sweepAngle = 90f,
                                            useCenter = false,
                                            topLeft = Offset(size.width * 0.4f, size.height * 0.25f),
                                            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.5f),
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                }
                            }
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
                // 1. Android Notification Shade Card (Top Card in Screenshot)
                NotificationShadeCard(
                    boostPercent = boostPercent,
                    isActive = isBoostActive,
                    onToggleBoost = {
                        isBoostActive = !isBoostActive
                        prefs.setBoostActive(isBoostActive)
                        if (isBoostActive) {
                            VolumeBoosterService.startService(this@MainActivity, boostPercent)
                        } else {
                            VolumeBoosterService.stopService(this@MainActivity)
                        }
                    },
                    onQuickAdd = { amount ->
                        val next = (boostPercent + amount).coerceIn(0, 100)
                        boostPercent = next
                        prefs.setBoostPercent(next)
                        if (isBoostActive) {
                            VolumeBoosterService.updatePercent(this@MainActivity, next)
                        }
                    },
                    onStopService = {
                        isBoostActive = false
                        prefs.setBoostActive(false)
                        VolumeBoosterService.stopService(this@MainActivity)
                    }
                )

                // 2. Real-time Dynamic Output Spectrum Power (Waveform Box)
                AudioVisualizerWave(
                    boostPercent = boostPercent,
                    isActive = isBoostActive
                )

                // 3. PRIMARY: Expressive Boost Percentage Slider
                MaterialExpressiveSlider(
                    value = if (isBoostActive) boostPercent else 0,
                    onValueChange = { newPercent ->
                        boostPercent = newPercent
                        prefs.setBoostPercent(newPercent)
                        if (newPercent > 0 && !isBoostActive) {
                            isBoostActive = true
                            prefs.setBoostActive(true)
                            VolumeBoosterService.startService(this@MainActivity, newPercent)
                        } else if (isBoostActive) {
                            VolumeBoosterService.updatePercent(this@MainActivity, newPercent)
                        }
                    },
                    label = "Boost Amplification",
                    sublabel = "Increases volume past normal 100% Android speaker ceiling",
                    isBoostMode = true,
                    enabled = isBoostActive
                )

                // 4. Standard System Media Volume Slider
                val systemVolumePercent = ((systemVolume.toFloat() / maxVolume) * 100).toInt()
                MaterialExpressiveSlider(
                    value = systemVolumePercent,
                    onValueChange = { newPct ->
                        val newVol = ((newPct / 100f) * maxVolume).toInt().coerceIn(0, maxVolume)
                        systemVolume = newVol
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                    },
                    label = "System Media Volume",
                    sublabel = "Base device audio stream level (0% to 100%)",
                    isBoostMode = false,
                    enabled = true,
                    prefix = "",
                    unit = "%"
                )

                // 5. Quick Settings Tile Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = ThemePrimary
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Canvas(modifier = Modifier.size(16.dp)) {
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
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Quick Settings Tile",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryWhite
                                    )
                                    Text(
                                        text = "Quick Settings Integration",
                                        fontSize = 11.sp,
                                        color = TextSecondaryMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Tile Preview Button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    isBoostActive = !isBoostActive
                                    prefs.setBoostActive(isBoostActive)
                                    if (isBoostActive) {
                                        VolumeBoosterService.startService(this@MainActivity, boostPercent)
                                    } else {
                                        VolumeBoosterService.stopService(this@MainActivity)
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isBoostActive) ThemePrimary else Color(0xFF211F26),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isBoostActive) androidx.compose.ui.graphics.Color.Transparent else SurfaceBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = if (isBoostActive) Color(0x33000000) else Color(0xFF2A2830)
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
                                            drawPath(
                                                path,
                                                color = if (isBoostActive) ThemeOnPrimary else Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Volume Booster Tile",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBoostActive) ThemeOnPrimary else TextPrimaryWhite
                                    )
                                    Text(
                                        text = if (isBoostActive) "+$boostPercent% Active · Tap to Toggle" else "Off · Tap to Activate",
                                        fontSize = 11.sp,
                                        color = if (isBoostActive) ThemeOnPrimary.copy(alpha = 0.8f) else TextSecondaryMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
