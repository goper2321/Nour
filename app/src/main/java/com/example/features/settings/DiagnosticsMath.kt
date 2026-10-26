package com.example.features.settings

/**
 * NUR-033 — pure decision helpers behind the Settings "Device diagnostics"
 * card. Everything here is pure Kotlin (no Android or Compose types) so the
 * card's honest-state logic is covered by plain JVM unit tests.
 */
object DiagnosticsMath {

    /**
     * Decides whether an incoming Adhan scheduling should replace the stored
     * "next scheduled Adhan" record for that prayer. Replace when the stored
     * record is already in the past (fired/stale) or when the candidate fires
     * earlier than the stored one.
     */
    fun shouldReplaceStoredNext(storedMillis: Long, candidateMillis: Long, nowMillis: Long): Boolean =
        storedMillis < nowMillis || candidateMillis < storedMillis

    /**
     * Earliest future schedule among the per-prayer records, or null when
     * nothing future is recorded (e.g. alarms were cancelled).
     */
    fun nextSchedule(records: Map<String, Long>, nowMillis: Long): Pair<String, Long>? =
        records.entries
            .filter { it.value >= nowMillis }
            .minByOrNull { it.value }
            ?.let { it.key to it.value }

    /**
     * Honest relative age of the last successful GPS fix, or null when no fix
     * was ever recorded (or the record lies in the future, which is untrusted).
     * Machine-style units ("5 m", "2 h", "3 d") keep this free of a fourth
     * translation for a value the owner reads as support data.
     */
    fun formatFixAge(fixMillis: Long?, nowMillis: Long): String? {
        if (fixMillis == null || fixMillis <= 0L || nowMillis < fixMillis) return null
        val minutes = (nowMillis - fixMillis) / 60_000L
        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "$minutes m"
            minutes < 1440 -> "${minutes / 60} h"
            else -> "${minutes / 1440} d"
        }
    }

    /**
     * Utc-stamped "yyyy-MM-dd HH:mm" for the next scheduled Adhan. Device
     * timezone on purpose — the alarm fires in the user's own clock.
     */
    fun formatScheduleStamp(triggerMillis: Long): String =
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
            .format(java.util.Date(triggerMillis))

    /** True when the actual hash of the bundled Uthmani text matches the pinned constant. */
    fun isCorpusIntact(actualHash: String?, expectedHash: String): Boolean =
        actualHash != null && actualHash == expectedHash

    /** First [chars] of a hash for display; "—" when absent. */
    fun shortHash(hash: String?, chars: Int = 12): String =
        hash?.take(chars)?.plus("…") ?: "—"
}
