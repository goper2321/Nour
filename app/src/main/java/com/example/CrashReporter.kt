package com.example

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Last-resort crash recorder. Installed as the very first statement of
 * [NourApp.onCreate], it appends any uncaught exception (with full stack
 * trace) to a log file under Downloads/NourCrash that survives the process
 * death, then forwards to the previous handler for the normal crash dialog.
 * Everything is failure-silent: recording must never break startup itself.
 */
object CrashReporter {
    fun install(context: Context) {
        try {
            val appContext = context.applicationContext
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    writeCrashLog(appContext, thread, throwable)
                } catch (_: Throwable) {
                    // Recording failed; fall through to the system handler.
                }
                try {
                    previous?.uncaughtException(thread, throwable)
                } catch (_: Throwable) {
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun writeCrashLog(context: Context, thread: Thread, throwable: Throwable) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val version = try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Throwable) {
            "unknown"
        }
        val sw = StringWriter().also { throwable.printStackTrace(java.io.PrintWriter(it)) }
        val text = StringBuilder()
            .appendLine("Nour crash report")
            .appendLine("time=$stamp")
            .appendLine("version=$version")
            .appendLine("thread=${thread.name}")
            .appendLine("exception=${throwable::class.java.name}: ${throwable.message}")
            .appendLine("--- stack ---")
            .appendLine(sw.toString())
            .toString()

        // Prefer app-private files on all API levels; MediaStore Downloads needs API 29+.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            val fallback = java.io.File(context.filesDir, "nour-crash-${System.currentTimeMillis()}.log")
            fallback.writeText(text, Charsets.UTF_8)
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "nour-crash-${System.currentTimeMillis()}.log")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/NourCrash"
            )
        }
        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
        ) ?: return
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(text.toByteArray(Charsets.UTF_8))
        }
    }
}
