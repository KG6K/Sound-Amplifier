package io.github.kg6k.soundamplifier.data

import android.content.Context
import android.content.SharedPreferences

data class BoosterState(
    val systemVolume: Int = 80,
    val boostPercentage: Int = 50,
    val isBoostActive: Boolean = true,
    val isLimiterActive: Boolean = true
)

class BoosterPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sound_amplifier_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BOOST_PERCENT = "boost_percent"
        private const val KEY_IS_BOOST_ACTIVE = "is_boost_active"
        private const val KEY_IS_LIMITER_ACTIVE = "is_limiter_active"
    }

    fun getBoostPercent(): Int = prefs.getInt(KEY_BOOST_PERCENT, 50).coerceIn(0, 100)

    fun setBoostPercent(percent: Int) {
        prefs.edit().putInt(KEY_BOOST_PERCENT, percent.coerceIn(0, 100)).apply()
    }

    fun isBoostActive(): Boolean = prefs.getBoolean(KEY_IS_BOOST_ACTIVE, true)

    fun setBoostActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_IS_BOOST_ACTIVE, active).apply()
    }

    fun isLimiterActive(): Boolean = prefs.getBoolean(KEY_IS_LIMITER_ACTIVE, true)

    fun setLimiterActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LIMITER_ACTIVE, active).apply()
    }
}
