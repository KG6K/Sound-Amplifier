package io.github.kg6k.soundamplifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        try {
            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(volumeReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(volumeReceiver, filter)
            }
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

        var showDonateDialog by remember { mutableStateOf(false) }

        if (showDonateDialog) {
            AlertDialog(
                onDismissRequest = { showDonateDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "❤️", fontSize = 18.sp)
                        Text(
                            text = "Support Development",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "An open-source, copylefted Sound Amplifier for Android.",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Direct UPI App Payment Action
                        Button(
                            onClick = {
                                val upiUri = Uri.parse("upi://pay?pa=ansariusman@ptyes&pn=MOHAMAD%20U%20ANSHARI")
                                val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                try {
                                    startActivity(upiIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "No UPI app found on this device",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text(
                                text = "⚡ Donate by UPI",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                        }

                        // GitHub Source Code Repo Button
                        OutlinedButton(
                            onClick = {
                                val repoIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/KG6K/Sound-Amplifier")
                                )
                                startActivity(repoIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text(
                                text = "GitHub Repository",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDonateDialog = false }) {
                        Text("Close")
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
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0C0A14),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6834CD).copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(34.dp)) {
                                    val w = size.width
                                    val h = size.height

                                    // Outer purple ring
                                    drawCircle(
                                        color = Color(0xFF6834CD),
                                        radius = w * 0.44f,
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )

                                    // Left lavender pill arc accent
                                    drawArc(
                                        color = Color(0xFFAA80F9),
                                        startAngle = 142f,
                                        sweepAngle = 76f,
                                        useCenter = false,
                                        topLeft = Offset(w * 0.06f, h * 0.06f),
                                        size = Size(w * 0.88f, h * 0.88f),
                                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Right lavender pill arc accent
                                    drawArc(
                                        color = Color(0xFFAA80F9),
                                        startAngle = -38f,
                                        sweepAngle = 76f,
                                        useCenter = false,
                                        topLeft = Offset(w * 0.06f, h * 0.06f),
                                        size = Size(w * 0.88f, h * 0.88f),
                                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Inner purple disc
                                    drawCircle(
                                        color = Color(0xFF4A18B7),
                                        radius = w * 0.32f
                                    )
                                    drawCircle(
                                        color = Color(0xFF6C33E2),
                                        radius = w * 0.32f,
                                        style = Stroke(width = 2.dp.toPx())
                                    )

                                    // Yellow speaker horn outline
                                    val speakerPath = Path().apply {
                                        moveTo(w * 0.28f, h * 0.44f)
                                        lineTo(w * 0.38f, h * 0.44f)
                                        lineTo(w * 0.50f, h * 0.33f)
                                        lineTo(w * 0.50f, h * 0.67f)
                                        lineTo(w * 0.38f, h * 0.56f)
                                        lineTo(w * 0.28f, h * 0.56f)
                                        close()
                                    }
                                    drawPath(
                                        path = speakerPath,
                                        color = Color(0xFFFFE600),
                                        style = Stroke(
                                            width = 1.8.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                                        )
                                    )

                                    // Inner yellow sound wave arc
                                    drawArc(
                                        color = Color(0xFFFFE600),
                                        startAngle = -42f,
                                        sweepAngle = 84f,
                                        useCenter = false,
                                        topLeft = Offset(w * 0.42f, h * 0.39f),
                                        size = Size(w * 0.22f, h * 0.22f),
                                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Outer yellow sound wave arc
                                    drawArc(
                                        color = Color(0xFFFFE600),
                                        startAngle = -45f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        topLeft = Offset(w * 0.38f, h * 0.31f),
                                        size = Size(w * 0.38f, h * 0.38f),
                                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                    )
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
                                    "Amplification active (+${(boostPercent * 60) / 100} dB)"
                                } else {
                                    "Amplification idle (0%)"
                                },
                                fontSize = 11.sp,
                                color = when {
                                    boostPercent > 60 -> RedDanger
                                    boostPercent > 0 -> EmeraldSafe
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.height(32.dp),
                            onClick = {
                                showDonateDialog = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "❤️",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Donate",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    sublabel = "",
                    isBoostMode = true,
                    enabled = true
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
                    sublabel = "",
                    isBoostMode = false,
                    enabled = true,
                    prefix = "",
                    unit = "%"
                )

                // Warning Notice below System Volume Slider (Text & Symbol only, no box)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "⚠️", fontSize = 14.sp)
                    Text(
                        text = "Warning: Playing audio at high volumes may damage speakers/hearing. ALL USE IS AT YOUR OWN RISK. Particularly beware of boosts above 60%, especially with music.",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = if (isSystemInDarkTheme()) Color(0xFFFCD34D) else Color(0xFF92400E)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
