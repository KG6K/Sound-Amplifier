package io.github.kg6k.soundamplifier.audio

import android.media.audiofx.LoudnessEnhancer
import android.util.Log

/**
 * Manages native Android LoudnessEnhancer audio effect on session 0
 * Supports Android 10 (API 29) to Android 15 (API 35)
 */
class AudioEffectManager {

    private var loudnessEnhancer: LoudnessEnhancer? = null
    private val TAG = "AudioEffectManager"

    fun applyBoost(percent: Int, active: Boolean) {
        try {
            if (loudnessEnhancer == null) {
                // Audio session 0 targets the global media output mix
                loudnessEnhancer = LoudnessEnhancer(0)
            }

            if (!active || percent <= 0) {
                loudnessEnhancer?.enabled = false
                return
            }

            // LoudnessEnhancer uses millibels (mB)
            // 100% boost = 1000 mB (+10 dB gain)
            val millibels = (percent * 10).coerceIn(0, 1000)
            loudnessEnhancer?.setTargetGain(millibels)
            loudnessEnhancer?.enabled = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply LoudnessEnhancer: ${e.message}", e)
        }
    }

    fun release() {
        try {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing LoudnessEnhancer: ${e.message}", e)
        } finally {
            loudnessEnhancer = null
        }
    }
}
