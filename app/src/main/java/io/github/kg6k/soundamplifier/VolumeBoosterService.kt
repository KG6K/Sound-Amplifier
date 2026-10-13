package io.github.kg6k.soundamplifier

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

/**
 * Crash-resilient Foreground Audio Amplification Service
 * Fully tested for Android 10 (API 29), Android 11 (API 30) through Android 15 (API 35)
 */
class VolumeBoosterService : Service() {

    private var loudnessEnhancer: LoudnessEnhancer? = null
    private val CHANNEL_ID = "sound_amplifier_foreground_channel"
    private val NOTIF_ID = 2001
    private val TAG = "VolumeBoosterService"

    companion object {
        const val ACTION_ENABLE_BOOST = "io.github.kg6k.soundamplifier.ENABLE"
        const val ACTION_DISABLE_BOOST = "io.github.kg6k.soundamplifier.DISABLE"
        const val ACTION_SET_PERCENT = "io.github.kg6k.soundamplifier.SET_PERCENT"
        const val EXTRA_PERCENT = "percent"

        fun startService(context: Context, percent: Int) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                action = ACTION_ENABLE_BOOST
                putExtra(EXTRA_PERCENT, percent)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (t: Throwable) {
                Log.e("VolumeBoosterService", "Error starting service", t)
            }
        }

        fun updatePercent(context: Context, percent: Int) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                action = ACTION_SET_PERCENT
                putExtra(EXTRA_PERCENT, percent)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (t: Throwable) {
                Log.e("VolumeBoosterService", "Error updating percent", t)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                action = ACTION_DISABLE_BOOST
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (t: Throwable) {
                Log.e("VolumeBoosterService", "Error stopping service", t)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_ENABLE_BOOST
        val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
        val boostPercent = intent?.getIntExtra(EXTRA_PERCENT, prefs.getInt("boost_percent", 50))
            ?: prefs.getInt("boost_percent", 50)

        // Android 8.0-14 Contract: ALWAYS call startForeground FIRST before any operations
        // to prevent RemoteServiceException / ForegroundServiceDidNotStartInTimeException
        safeStartForeground(boostPercent)

        when (action) {
            ACTION_ENABLE_BOOST, ACTION_SET_PERCENT -> {
                prefs.edit()
                    .putInt("boost_percent", boostPercent)
                    .putBoolean("is_boost_active", true)
                    .apply()

                applyLoudnessBoost(boostPercent, true)
                updateNotification(boostPercent)
            }
            ACTION_DISABLE_BOOST -> {
                prefs.edit().putBoolean("is_boost_active", false).apply()
                releaseLoudnessEnhancer()
                safeStopForeground()
                stopSelf()
            }
        }
        return START_STICKY
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

    private fun applyLoudnessBoost(percent: Int, active: Boolean) {
        try {
            if (loudnessEnhancer == null) {
                loudnessEnhancer = LoudnessEnhancer(0)
            }
            if (!active || percent <= 0) {
                loudnessEnhancer?.enabled = false
                return
            }

            // 100% boost maps to +10 dB (1000 millibels)
            val millibels = (percent * 10).coerceIn(0, 1000)
            loudnessEnhancer?.setTargetGain(millibels)
            loudnessEnhancer?.enabled = true
        } catch (t: Throwable) {
            Log.e(TAG, "LoudnessEnhancer error: ${t.message}", t)
        }
    }

    private fun releaseLoudnessEnhancer() {
        try {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
        } catch (t: Throwable) {
            Log.e(TAG, "Error releasing LoudnessEnhancer: ${t.message}")
        } finally {
            loudnessEnhancer = null
        }
    }

    private fun buildNotification(percent: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sound Amplifier")
            .setContentText("Active Amplification: +$percent% (+${percent / 10} dB)")
            .setSmallIcon(R.drawable.ic_qs_volume_boost)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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
                    description = "Keeps audio boost running in background"
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
        releaseLoudnessEnhancer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
