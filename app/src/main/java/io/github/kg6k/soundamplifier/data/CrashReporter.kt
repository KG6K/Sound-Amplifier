package io.github.kg6k.soundamplifier.data

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Crash Reporting System
 * Catches any unhandled exception across Activities, Services, and background threads,
 * and saves detailed diagnostic logs to Android storage.
 */
object CrashReporter {

    private const val TAG = "CrashReporter"
    private const val DIR_NAME = "crash_reports"
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                saveCrashReport(context, thread, throwable)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed saving crash report", t)
            } finally {
                // Pass to original handler so Android handles process termination gracefully
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun saveCrashReport(context: Context, thread: Thread, throwable: Throwable) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val readableDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(Date())
        val fileName = "crash_report_${dateFormat.format(Date())}.txt"

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        val prefs = context.getSharedPreferences("sound_amplifier_prefs", Context.MODE_PRIVATE)
        val isBoostActive = prefs.getBoolean("is_boost_active", false)
        val boostPercent = prefs.getInt("boost_percent", 50)

        val reportContent = buildString {
            appendLine("==========================================")
            appendLine("SOUND AMPLIFIER - CRASH REPORT")
            appendLine("==========================================")
            appendLine("Timestamp       : $readableDate")
            appendLine("Package         : ${context.packageName}")
            appendLine("App Version     : 1.0.0 (1)")
            appendLine("Device Model    : ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
            appendLine("Android Version : Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Fingerprint     : ${Build.FINGERPRINT}")
            appendLine("Thread          : ${thread.name} (id: ${thread.id})")
            appendLine("Boost Active    : $isBoostActive")
            appendLine("Boost Percent   : $boostPercent%")
            appendLine("==========================================")
            appendLine("EXCEPTION DETAILS:")
            appendLine("Type: ${throwable.javaClass.name}")
            appendLine("Message: ${throwable.message}")
            appendLine("==========================================")
            appendLine("FULL STACK TRACE:")
            appendLine(stackTrace)
            appendLine("==========================================")
        }

        // 1. Write to external app storage: Android/data/io.github.kg6k.soundamplifier/files/crash_reports/
        // Directly accessible via file managers and USB transfer
        try {
            val externalDir = context.getExternalFilesDir(DIR_NAME) ?: File(context.filesDir, DIR_NAME)
            if (!externalDir.exists()) externalDir.mkdirs()

            val externalFile = File(externalDir, fileName)
            externalFile.writeText(reportContent)

            // Save copy as latest_crash.txt for quick access
            val latestFile = File(externalDir, "latest_crash.txt")
            latestFile.writeText(reportContent)

            Log.i(TAG, "Crash report saved to: ${externalFile.absolutePath}")
        } catch (e: Throwable) {
            Log.e(TAG, "Error writing to external storage", e)
        }

        // 2. Also write to internal storage fallback
        try {
            val internalDir = File(context.filesDir, DIR_NAME)
            if (!internalDir.exists()) internalDir.mkdirs()
            File(internalDir, fileName).writeText(reportContent)
            File(internalDir, "latest_crash.txt").writeText(reportContent)
        } catch (e: Throwable) {
            Log.e(TAG, "Error writing to internal storage", e)
        }
    }

    /**
     * Retrieves the latest crash report string if available.
     */
    fun getLatestCrashLog(context: Context): String? {
        val externalDir = context.getExternalFilesDir(DIR_NAME)
        val file = externalDir?.let { File(it, "latest_crash.txt") }
        if (file != null && file.exists()) {
            return try { file.readText() } catch (_: Throwable) { null }
        }

        val internalFile = File(context.filesDir, "$DIR_NAME/latest_crash.txt")
        if (internalFile.exists()) {
            return try { internalFile.readText() } catch (_: Throwable) { null }
        }
        return null
    }

    /**
     * Returns the path of the crash reports directory.
     */
    fun getStorageLocation(context: Context): String {
        val dir = context.getExternalFilesDir(DIR_NAME)
        return dir?.absolutePath ?: File(context.filesDir, DIR_NAME).absolutePath
    }

    /**
     * Clears previous crash logs.
     */
    fun clearLogs(context: Context) {
        try {
            context.getExternalFilesDir(DIR_NAME)?.deleteRecursively()
            File(context.filesDir, DIR_NAME).deleteRecursively()
        } catch (ignored: Throwable) {}
    }

    /**
     * Share crash report as text via Android share sheet.
     */
    fun shareLatestCrashLog(context: Context) {
        val log = getLatestCrashLog(context) ?: return
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Sound Amplifier Crash Report")
            putExtra(Intent.EXTRA_TEXT, log)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Crash Report").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
