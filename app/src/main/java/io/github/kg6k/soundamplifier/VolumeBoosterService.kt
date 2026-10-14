package io.github.kg6k.soundamplifier

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance Foreground Audio Amplification Service
 * Delivers real audible volume enhancement (up to +40 dB / 4000 mB)
 * with dynamic session hooking across third-party media players (YouTube, Spotify, etc.)
 */
class VolumeBoosterService : Service() {

    private val CHANNEL_ID = "sound_amplifier_foreground_channel"
    private val NOTIF_ID = 2001
    private val TAG = "VolumeBoosterService"

    // Primary global session enhancer (session 0)
    private var globalEnhancer: LoudnessEnhancer? = null

    // Pool of active media player sessions (e.g. YouTube, Spotify, Chrome)
    private val sessionEnhancers = ConcurrentHashMap<Int, LoudnessEnhancer>()

    // Current boost percentage (0 to 100)
    private var currentBoostPercent = 0

    // Receiver to catch new audio sessions from any running music or video app
    private val audioSessionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
            if (sessionId == AudioEffect.ERROR || sessionId <= 0) return

            when (action) {
                AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                    Log.d(TAG, "Audio session opened: $sessionId")
                    attachToSession(sessionId, currentBoostPercent)
                }
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                    Log.d(TAG, "Audio session closed: $sessionId")
                    detachFromSession(sessionId)
                }
            }
        }
    }

    companion object {
        const val ACTION_SET_PERCENT = "io.github.kg6k.soundamplifier.SET_PERCENT"
        const val ACTION_STEP_BOOST = "io.github.kg6k.soundamplifier.STEP_BOOST"
        const val ACTION_STOP = "io.github.kg6k.soundamplifier.STOP"
        const val EXTRA_PERCENT = "percent"
        const val EXTRA_STEP = "step"

        fun setBoost(context: Context, percent: Int) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                action = ACTION_SET_PERCENT
                putExtra(EXTRA_PERCENT, percent)
            }
            dispatchServiceIntent(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                action = ACTION_STOP
            }
            dispatchServiceIntent(context, intent)
        }

        private fun dispatchServiceIntent(context: Context, intent: Intent) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (t: Throwable) {
                Log.e("VolumeBoosterService", "Error dispatching service intent", t)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Register listener for media players opening audio sessions
        try {
            val filter = IntentFilter().apply {
                addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
                addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(audioSessionReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(audioSessionReceiver, filter)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error registering session receiver", t)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
        val savedPercent = prefs.getInt("boost_percent", 0)
        val action = intent?.action ?: ACTION_SET_PERCENT

        // Always satisfy Android foreground service contract immediately
        safeStartForeground(savedPercent)

        when (action) {
            ACTION_SET_PERCENT -> {
                val newPct = intent?.getIntExtra(EXTRA_PERCENT, savedPercent) ?: savedPercent
                applyAndSaveBoost(newPct)
            }
            ACTION_STEP_BOOST -> {
                val step = intent?.getIntExtra(EXTRA_STEP, 0) ?: 0
                val newPct = (currentBoostPercent + step).coerceIn(0, 100)
                applyAndSaveBoost(newPct)
            }
            ACTION_STOP -> {
                applyAndSaveBoost(0)
                releaseAllEnhancers()
                safeStopForeground()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun applyAndSaveBoost(percent: Int) {
        currentBoostPercent = percent
        val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
        prefs.edit()
            .putInt("boost_percent", percent)
            .putBoolean("is_boost_active", percent > 0)
            .apply()

        if (percent > 0) {
            applyLoudnessGain(percent)
            updateNotification(percent)
        } else {
            // At 0% boost, silence effect and gracefully remove foreground notification
            releaseAllEnhancers()
            safeStopForeground()
            stopSelf()
        }
    }

    /**
     * Real Audible Gain Mapping:
     * 100% boost -> 4000 millibels (+40 dB)
     * 50% boost  -> 2000 millibels (+20 dB)
     * 25% boost  -> 1000 millibels (+10 dB)
     */
    private fun applyLoudnessGain(percent: Int) {
        val millibels = (percent * 40).coerceIn(0, 4000)

        // 1. Apply to global session 0
        try {
            if (globalEnhancer == null) {
                globalEnhancer = LoudnessEnhancer(0)
            }
            globalEnhancer?.setTargetGain(millibels)
            globalEnhancer?.enabled = true
        } catch (t: Throwable) {
            Log.e(TAG, "Global LoudnessEnhancer error: ${t.message}")
        }

        // 2. Broadcast effect session announcement so system audio policy routes to enhancer
        try {
            val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            }
            sendBroadcast(intent)
        } catch (ignored: Throwable) {}

        // 3. Apply to all actively tracked third-party player sessions
        sessionEnhancers.forEach { (sessionId, enhancer) ->
            try {
                enhancer.setTargetGain(millibels)
                enhancer.enabled = true
            } catch (t: Throwable) {
                Log.e(TAG, "Session $sessionId gain error: ${t.message}")
            }
        }
    }

    private fun attachToSession(sessionId: Int, percent: Int) {
        if (percent <= 0) return
        try {
            val millibels = (percent * 40).coerceIn(0, 4000)
            val enhancer = sessionEnhancers.getOrPut(sessionId) {
                LoudnessEnhancer(sessionId)
            }
            enhancer.setTargetGain(millibels)
            enhancer.enabled = true
            Log.d(TAG, "Attached LoudnessEnhancer to session $sessionId at $millibels mB")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed attaching enhancer to session $sessionId: ${t.message}")
        }
    }

    private fun detachFromSession(sessionId: Int) {
        try {
            val enhancer = sessionEnhancers.remove(sessionId)
            enhancer?.enabled = false
            enhancer?.release()
        } catch (ignored: Throwable) {}
    }

    private fun releaseAllEnhancers() {
        try {
            globalEnhancer?.enabled = false
            globalEnhancer?.release()
        } catch (ignored: Throwable) {} finally {
            globalEnhancer = null
        }

        sessionEnhancers.forEach { (_, enhancer) ->
            try {
                enhancer.enabled = false
                enhancer.release()
            } catch (ignored: Throwable) {}
        }
        sessionEnhancers.clear()
    }

    private fun safeStartForeground(percent: Int) {
        try {
            val notification = buildNotification(percent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIF_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIF_ID, notification)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "safeStartForeground failed: ${t.message}", t)
        }
    }

    private fun safeStopForeground() {
        try {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        } catch (t: Throwable) {
            try {
                @Suppress("DEPRECATION")
                stopForeground(true)
            } catch (ignored: Throwable) {}
        }
    }

    private fun updateNotification(percent: Int) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(NOTIF_ID, buildNotification(percent))
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to update notification: ${t.message}")
        }
    }

    private fun buildNotification(percent: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Step down -20%
        val stepDownIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, VolumeBoosterService::class.java).apply {
                action = ACTION_STEP_BOOST
                putExtra(EXTRA_STEP, -20)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Step up +20%
        val stepUpIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, VolumeBoosterService::class.java).apply {
                action = ACTION_STEP_BOOST
                putExtra(EXTRA_STEP, 20)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Turn off / Reset to 0%
        val stopIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, VolumeBoosterService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dbGain = String.format("+%.1f dB", (percent * 40) / 100f)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sound Amplifier: +$percent% ($dbGain)")
            .setContentText("Active background volume boost • Tap to adjust")
            .setSmallIcon(R.drawable.ic_qs_volume_boost)
            .setColor(Color.parseColor("#6750A4"))
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0, "Turn Off", stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Sound Amplifier Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Active audio amplification status"
                    setShowBadge(false)
                }
                val nm = getSystemService(NotificationManager::class.java)
                nm?.createNotificationChannel(channel)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed creating notification channel", t)
            }
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(audioSessionReceiver)
        } catch (ignored: Throwable) {}
        releaseAllEnhancers()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
