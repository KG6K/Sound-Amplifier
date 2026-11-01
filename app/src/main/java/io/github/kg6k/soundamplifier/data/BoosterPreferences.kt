package io.github.kg6k.soundamplifier.data

import android.content.Context
import android.content.SharedPreferences

class BoosterPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sound_amplifier_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BOOST_PERCENT = "boost_percent"
        private const val KEY_LAST_BOOST_PERCENT = "last_boost_percent"
        private const val KEY_IS_BOOST_ACTIVE = "is_boost_active"
    }

    fun getBoostPercent(): Int = prefs.getInt(KEY_BOOST_PERCENT, 0).coerceIn(0, 100)

    fun getLastBoostPercent(): Int = prefs.getInt(KEY_LAST_BOOST_PERCENT, 50).coerceIn(1, 100)

    fun setBoostPercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        val editor = prefs.edit()
            .putInt(KEY_BOOST_PERCENT, clamped)
            .putBoolean(KEY_IS_BOOST_ACTIVE, clamped > 0)
        if (clamped > 0) {
            editor.putInt(KEY_LAST_BOOST_PERCENT, clamped)
        }
        editor.apply()
    }

    fun isBoostActive(): Boolean = prefs.getBoolean(KEY_IS_BOOST_ACTIVE, false)

    fun setBoostActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_IS_BOOST_ACTIVE, active).apply()
    }
}
