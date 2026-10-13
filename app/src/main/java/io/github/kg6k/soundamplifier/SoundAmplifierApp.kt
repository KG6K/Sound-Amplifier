package io.github.kg6k.soundamplifier

import android.app.Application
import io.github.kg6k.soundamplifier.data.CrashReporter

/**
 * Main Application class
 * Initializes CrashReporter early so all activities, services, and threads are monitored.
 */
class SoundAmplifierApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize global crash reporting system to Android storage
        CrashReporter.init(this)
    }
}
