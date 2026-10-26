package com.example.features.settings

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File

/**
 * NUR-001: pure, Android-free parsing and ordering helpers for the crash logs
 * that `com.example.CrashReporter` writes, powering the offline Settings
 * "Diagnostics" section. Nothing here uploads, modifies, or deletes anything —
 * CrashReporter's collection and storage are untouched.
 */
object CrashLogCatalog {

    data class CrashLogEntry(
        val fileName: String,
        /** Millis encoded in the file name `nour-crash-<millis>.log`. */
        val epochMillis: Long,
        /** `time=` header value inside the log, as written by CrashReporter. */
        val headerTime: String?,
        /** `version=` header value inside the log. */
        val version: String?,
        /** `exception=` header value, e.g. "java.lang.IllegalStateException: boom". */
        val exceptionSummary: String?,
        /** Full log text, ready for Copy/Share. */
        val fullText: String
    )

    /**
     * What [parseHeader] found inside one crash log's header lines.
     */
    data class ParsedHeader(
        val time: String?,
        val version: String?,
        val exceptionSummary: String?
    )

    const val NAME_PREFIX = "nour-crash-"
    const val NAME_SUFFIX = ".log"

    /** Names are exactly the form CrashReporter writes: nour-crash-<millis>.log. */
    fun isCrashLogFileName(name: String): Boolean {
        if (!name.startsWith(NAME_PREFIX) || !name.endsWith(NAME_SUFFIX)) return false
        val core = name.removePrefix(NAME_PREFIX).removeSuffix(NAME_SUFFIX)
        return core.isNotEmpty() && core.all { it.isDigit() }
    }

    /** Epoch millis encoded in a crash-log file name, or null when unusable. */
    fun epochMillisFromName(name: String): Long? {
        if (!isCrashLogFileName(name)) return null
        return name.removePrefix(NAME_PREFIX).removeSuffix(NAME_SUFFIX).toLongOrNull()
    }

    /**
     * Reads the `time=`, `version=`, and `exception=` lines CrashReporter puts
     * before `--- stack ---`. Tolerant to missing or reorder-ed lines so a
     * partially written log still renders something honest.
     */
    fun parseHeader(text: String): ParsedHeader {
        var time: String? = null
        var version: String? = null
        var exception: String? = null
        for (line in text.lineSequence()) {
            if (line == "--- stack ---") break
            when {
                line.startsWith("time=") -> time = line.removePrefix("time=").ifBlank { null }
                line.startsWith("version=") -> version = line.removePrefix("version=").ifBlank { null }
                line.startsWith("exception=") ->
                    exception = line.removePrefix("exception=").ifBlank { null }
            }
        }
        return ParsedHeader(time, version, exception)
    }

    /**
     * Builds the Diagnostics listing from (fileName, fullText) pairs: newest
     * first by the millis in the file name, oldest logs later. Files with an
     * unusable name stay out; a present but unparseable name keeps the entry
     * with epoch 0 so nothing silently disappears from the reader.
     */
    fun buildEntries(files: List<Pair<String, String>>): List<CrashLogEntry> {
        return files
            .filter { isCrashLogFileName(it.first) }
            .map { (fileName, text) ->
                val header = parseHeader(text)
                CrashLogEntry(
                    fileName = fileName,
                    epochMillis = epochMillisFromName(fileName) ?: 0L,
                    headerTime = header.time,
                    version = header.version,
                    exceptionSummary = header.exceptionSummary,
                    fullText = text
                )
            }
            .sortedByDescending { it.epochMillis }
    }
}

/**
 * Android-side loader for the crash directory CrashReporter writes: MediaStore
 * Downloads on API 29+ (`Download/NourCrash`), `filesDir` below it. Read-only;
 * every failure degrades to an empty list so Settings never crashes over a
 * diagnostic convenience.
 */
object CrashLogReader {

    fun loadCrashLogEntries(context: Context): List<CrashLogCatalog.CrashLogEntry> {
        return try {
            CrashLogCatalog.buildEntries(readSourceFiles(context))
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun readSourceFiles(context: Context): List<Pair<String, String>> {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.filesDir.listFiles()
                ?.filter { it.isFile && CrashLogCatalog.isCrashLogFileName(it.name) }
                ?.map { it.name to it.readText() }
                .orEmpty()
        } else {
            readFromMediaStore(context)
        }
    }

    private fun readFromMediaStore(context: Context): List<Pair<String, String>> {
        val files = mutableListOf<Pair<String, String>>()
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        // Environment.DIRECTORY_DOWNLOADS is the literal "Download", which is
        // exactly the RELATIVE_PATH prefix CrashReporter's writer produces.
        val args = arrayOf("Download/NourCrash/%")
        val query = try {
            resolver.query(collection, projection, selection, args, null)
        } catch (_: Throwable) {
            null
        } ?: return files

        query.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                if (name == null || !CrashLogCatalog.isCrashLogFileName(name)) continue
                val uri = Uri.withAppendedPath(
                    collection,
                    cursor.getLong(idColumn).toString()
                )
                val text = try {
                    resolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                } catch (_: Throwable) {
                    null
                }
                if (text != null) files.add(name to text)
            }
        }
        return files
    }
}
