package io.github.kg6k.soundamplifier

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

/**
 * Foreground Audio Amplification Service
 * Uses LoudnessEnhancer on session 0 for global media stream enhancement
 */
class VolumeBoosterService : Service() {

    private var loudnessEnhancer: LoudnessEnhancer? = null
    private val CHANNEL_ID = "sound_amplifier_channel"
    private val NOTIF_ID = 2001

    companion object {
        const val ACTION_ENABLE_BOOST = "io.github.kg6k.soundamplifier.ENABLE"
        const val ACTION_DISABLE_BOOST = "io.github.kg6k.soundamplifier.DISABLE"
        const val ACTION_SET_PERCENT = "io.github.kg6k.soundamplifier.SET_PERCENT"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_ENABLE_BOOST
        val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
        val boostPercent = prefs.getInt("boost_percent", 50)

        when (action) {
            ACTION_ENABLE_BOOST -> {
                applyLoudnessBoost(boostPercent)
                startServiceWithNotification(boostPercent)
            }
            ACTION_DISABLE_BOOST -> {
                loudnessEnhancer?.enabled = false
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_SET_PERCENT -> {
                val newPct = intent?.getIntExtra("percent", boostPercent) ?: boostPercent
                applyLoudnessBoost(newPct)
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIF_ID, buildNotification(newPct))
            }
        }
        return START_STICKY
    }

    private fun startServiceWithNotification(percent: Int) {
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
    }

    private fun applyLoudnessBoost(percent: Int) {
        try {
            if (loudnessEnhancer == null) {
                loudnessEnhancer = LoudnessEnhancer(0)
            }
            // 100% boost = 1000 mB (+10 dB)
            val millibels = (percent * 10).coerceIn(0, 1000)
            loudnessEnhancer?.setTargetGain(millibels)
            loudnessEnhancer?.enabled = (percent > 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildNotification(percent: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sound Amplifier")
            .setContentText("Running in background · Active Boost: +$percent%")
            .setSmallIcon(R.drawable.ic_qs_volume_boost)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sound Amplifier Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Provides continuous background volume amplification"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        loudnessEnhancer?.release()
        loudnessEnhancer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
