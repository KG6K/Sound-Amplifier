package io.github.kg6k.soundamplifier

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media.app.NotificationCompat.MediaStyle

/**
 * Crash-resilient Foreground Audio Amplification Service
 * With interactive notification panel slider and step buttons
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
        const val ACTION_STEP_BOOST = "io.github.kg6k.soundamplifier.STEP_BOOST"
        const val EXTRA_PERCENT = "percent"
        const val EXTRA_STEP = "step"

        fun startService(context: Context, percent: Int) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                action = ACTION_ENABLE_BOOST
                putExtra(EXTRA_PERCENT, percent)
            }
            dispatchServiceIntent(context, intent)
        }

        fun updatePercent(context: Context, percent: Int) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                action = ACTION_SET_PERCENT
                putExtra(EXTRA_PERCENT, percent)
            }
            dispatchServiceIntent(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                action = ACTION_DISABLE_BOOST
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_ENABLE_BOOST
        val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
        var currentPercent = prefs.getInt("boost_percent", 50)

        // Always satisfy Android foreground service contract immediately
        safeStartForeground(currentPercent)

        when (action) {
            ACTION_ENABLE_BOOST -> {
                val newPct = intent?.getIntExtra(EXTRA_PERCENT, currentPercent) ?: currentPercent
                applyAndSaveBoost(newPct, true)
            }
            ACTION_SET_PERCENT -> {
                val newPct = intent?.getIntExtra(EXTRA_PERCENT, currentPercent) ?: currentPercent
                applyAndSaveBoost(newPct, true)
            }
            ACTION_STEP_BOOST -> {
                val step = intent?.getIntExtra(EXTRA_STEP, 0) ?: 0
                val newPct = (currentPercent + step).coerceIn(0, 100)
                applyAndSaveBoost(newPct, true)
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

    private fun applyAndSaveBoost(percent: Int, active: Boolean) {
        val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
        prefs.edit()
            .putInt("boost_percent", percent)
            .putBoolean("is_boost_active", active)
            .apply()

        applyLoudnessBoost(percent, active)
        updateNotification(percent)
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
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Step Controls: -10%, +10%, Max (100%), and Pause
        val stepDownIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, VolumeBoosterService::class.java).apply {
                action = ACTION_STEP_BOOST
                putExtra(EXTRA_STEP, -10)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stepUpIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, VolumeBoosterService::class.java).apply {
                action = ACTION_STEP_BOOST
                putExtra(EXTRA_STEP, 10)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val maxIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, VolumeBoosterService::class.java).apply {
                action = ACTION_SET_PERCENT
                putExtra(EXTRA_PERCENT, 100)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, VolumeBoosterService::class.java).apply {
                action = ACTION_DISABLE_BOOST
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dbText = String.format("+%.1f dB", percent / 10f)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sound Amplifier: +$percent% ($dbText)")
            .setContentText("Active Amplification · Tap actions below to adjust")
            .setSmallIcon(R.drawable.ic_qs_volume_boost)
            .setColor(Color.parseColor("#8B5CF6")) // Vibrant primary violet accent
            .setColorized(true)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Visual Progress Slider in the notification panel (0 to 100)
            .setProgress(100, percent, false)
            // Interactive quick actions right in the notification shade
            .addAction(0, "−10%", stepDownIntent)
            .addAction(0, "+10%", stepUpIntent)
            .addAction(0, "Max", maxIntent)
            .addAction(0, "Pause", pauseIntent)
            .setStyle(
                MediaStyle()
                    .setShowActionsInCompactView(0, 1, 3) // -10%, +10%, and Pause in compact view
            )
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
                    description = "Interactive audio amplification controls"
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
