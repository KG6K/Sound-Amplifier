package io.github.kg6k.soundamplifier

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat

/**
 * Android Quick Settings TileService for Sound Amplifier
 * Tested for Android 10 (API 29) through Android 15 (API 35)
 */
class VolumeBoosterTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return

        val isCurrentlyActive = (tile.state == Tile.STATE_ACTIVE)
        val newActiveState = !isCurrentlyActive

        val serviceIntent = Intent(this, VolumeBoosterService::class.java).apply {
            action = if (newActiveState) {
                VolumeBoosterService.ACTION_ENABLE_BOOST
            } else {
                VolumeBoosterService.ACTION_DISABLE_BOOST
            }
        }
        ContextCompat.startForegroundService(this, serviceIntent)

        val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("is_boost_active", newActiveState).apply()

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
        val isActive = prefs.getBoolean("is_boost_active", false)
        val boostPercent = prefs.getInt("boost_percent", 50)

        if (isActive) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = getString(R.string.tile_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "+$boostPercent% Boost"
            }
            tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_volume_boost)
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.tile_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Off"
            }
            tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_volume_boost)
        }

        tile.updateTile()
    }
}
