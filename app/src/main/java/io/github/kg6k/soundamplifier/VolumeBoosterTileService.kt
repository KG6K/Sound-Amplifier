package io.github.kg6k.soundamplifier

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

/**
 * Crash-safe Android Quick Settings TileService
 */
class VolumeBoosterTileService : TileService() {

    private val TAG = "VolumeBoosterTile"

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        try {
            val prefs = getSharedPreferences("sound_amplifier_prefs", MODE_PRIVATE)
            val isCurrentlyActive = prefs.getBoolean("is_boost_active", false)
            val boostPercent = prefs.getInt("boost_percent", 50)
            val newActiveState = !isCurrentlyActive

            prefs.edit().putBoolean("is_boost_active", newActiveState).apply()

            if (newActiveState) {
                VolumeBoosterService.setBoost(this, boostPercent)
            } else {
                VolumeBoosterService.stopService(this)
            }

            updateTileState()
        } catch (t: Throwable) {
            Log.e(TAG, "Error in tile onClick: ${t.message}", t)
        }
    }

    private fun updateTileState() {
        try {
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
            } else {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.tile_label)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Off"
                }
            }

            tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_volume_boost)
            tile.updateTile()
        } catch (t: Throwable) {
            Log.e(TAG, "Error updating tile: ${t.message}", t)
        }
    }
}
