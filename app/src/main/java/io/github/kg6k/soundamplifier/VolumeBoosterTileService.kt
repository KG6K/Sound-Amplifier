package io.github.kg6k.soundamplifier

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Crash-safe Android Quick Settings TileService (Android 7.0+ / API 24+)
 * - On Android 12+ (API 31+): Toggles Sound Amplifier directly in Quick Settings
 *   (and falls back to opening the app if the device OS restricts service start).
 * - On Android 11 and below (API <= 30): Opens Sound Amplifier (MainActivity)
 *   and collapses the Quick Settings panel when tapped.
 */
@RequiresApi(Build.VERSION_CODES.N)
class VolumeBoosterTileService : TileService() {

    private val TAG = "VolumeBoosterTile"

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        // On Android 11 and below (API <= 30), clicking the Quick Tile opens the app
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            openAppFromTile()
            return
        }

        try {
            val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
            val currentPercent = prefs.getInt("boost_percent", 0)
            val isCurrentlyActive = prefs.getBoolean("is_boost_active", false) && currentPercent > 0

            if (isCurrentlyActive) {
                prefs.edit()
                    .putInt("boost_percent", 0)
                    .putBoolean("is_boost_active", false)
                    .apply()
                VolumeBoosterService.stopService(this)
                updateTileState()
            } else {
                val targetPercent = prefs.getInt("last_boost_percent", 50).coerceIn(1, 100)
                val started = VolumeBoosterService.setBoost(this, targetPercent)
                if (started) {
                    prefs.edit()
                        .putInt("boost_percent", targetPercent)
                        .putBoolean("is_boost_active", true)
                        .apply()
                    updateTileState()
                } else {
                    // Fallback: if the device/OS blocks starting the service from Quick Settings, open the app
                    openAppFromTile()
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error in tile onClick, opening app as fallback: ${t.message}", t)
            openAppFromTile()
        }
    }

    private fun openAppFromTile() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(launchIntent)
            }
        } catch (t: Throwable) {
            try {
                startActivity(launchIntent)
            } catch (ignored: Throwable) {}
        }
    }

    private fun updateTileState() {
        try {
            val tile = qsTile ?: return
            val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
            val boostPercent = prefs.getInt("boost_percent", 0)
            val isActive = prefs.getBoolean("is_boost_active", false) && boostPercent > 0

            if (isActive) {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.tile_label)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val dbGain = (boostPercent * 60) / 100
                    tile.subtitle = "+$boostPercent% (+$dbGain dB)"
                }
            } else {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.tile_label)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                        "Tap to open"
                    } else {
                        "Off"
                    }
                }
            }

            tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_volume_boost)
            tile.updateTile()
        } catch (t: Throwable) {
            Log.e(TAG, "Error updating tile: ${t.message}", t)
        }
    }
}
