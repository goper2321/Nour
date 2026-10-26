package com.example.features.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.UserLocation

/**
 * Reader-only presentation theme. SYSTEM keeps the app's scheme (the default,
 * pixel-identical to previous builds); PAPER and NIGHT recolor the reader/page
 * subtree only. Stored by name, unknown values fall back to SYSTEM.
 */
enum class QuranReadingTheme { SYSTEM, PAPER, NIGHT }

/** Scripture typeface choice; SYSTEM keeps the platform font. */
enum class QuranTypeface { SYSTEM, AMIRI }

/**
 * Typed access to the existing `nour_app_prefs` file. Keeps MainActivity's
 * remembered state durable across process restarts without changing the Room
 * schema. All reads validate stored values and fall back to defaults.
 */
class NourPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nour_app_prefs", Context.MODE_PRIVATE)

    var themeMode: String
        get() = prefs.getString("theme_mode", "system")?.takeIf { it in listOf("system", "light", "dark") } ?: "system"
        set(value) = prefs.edit().putString("theme_mode", value.takeIf { it in listOf("system", "light", "dark") } ?: "system").apply()

    var dynamicColors: Boolean
        get() = prefs.getBoolean("dynamic_colors", false)
        set(value) = prefs.edit().putBoolean("dynamic_colors", value).apply()

    /**
     * NUR-008: high-contrast theme toggle (M3 Expressive user-contrast level
     * 1.0). Lives in the same `nour_app_prefs` family as `theme_mode` and
     * `dynamic_colors`; MainActivity threads it into MyApplicationTheme the
     * same way. Default off, so nothing changes for existing installs.
     */
    var highContrast: Boolean
        get() = prefs.getBoolean("theme_high_contrast", false)
        set(value) = prefs.edit().putBoolean("theme_high_contrast", value).apply()

    var numeralEastern: Boolean
        get() = prefs.getBoolean(KEY_NUMERAL_EASTERN, false)
        set(value) = prefs.edit().putBoolean(KEY_NUMERAL_EASTERN, value).apply()

    var time24h: Boolean
        get() = prefs.getBoolean(KEY_TIME_24H, true)
        set(value) = prefs.edit().putBoolean(KEY_TIME_24H, value).apply()

    /** Master switch for prayer alarms. Off = cancel all scheduled alarms and schedule nothing new. */
    var alarmsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALARMS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ALARMS_ENABLED, value).apply()

    var setupCompleted: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETED, value).apply()

    /** Custom M3 seed color ARGB, or null for the stock palette. */
    var seedColorArgb: Int?
        get() = if (prefs.contains(KEY_SEED_COLOR)) prefs.getInt(KEY_SEED_COLOR, 0) else null
        set(value) {
            if (value == null) prefs.edit().remove(KEY_SEED_COLOR).apply()
            else prefs.edit().putInt(KEY_SEED_COLOR, value).apply()
        }

    // ---- Location (mode tracked separately from the last resolved location) ----
    var locationIsAutoDetected: Boolean
        get() = prefs.getBoolean(KEY_LOCATION_AUTO, false)
        set(value) = prefs.edit().putBoolean(KEY_LOCATION_AUTO, value).apply()

    var savedLocation: UserLocation?
        get() {
            if (!prefs.contains(KEY_LOC_LAT)) return null
            val lat = prefs.getFloat(KEY_LOC_LAT, Float.NaN).toDouble()
            val lng = prefs.getFloat(KEY_LOC_LNG, Float.NaN).toDouble()
            if (lat.isNaN() || lng.isNaN() || lat < -90.0 || lat > 90.0 ||
                lng < -180.0 || lng > 180.0
            ) {
                return null
            }
            return UserLocation(
                latitude = lat,
                longitude = lng,
                cityName = prefs.getString(KEY_LOC_CITY, null) ?: "Current location",
                countryName = prefs.getString(KEY_LOC_COUNTRY, null) ?: "",
                timezoneOffsetHours = prefs.getFloat(KEY_LOC_TZ, 0f).toDouble(),
                isAutoDetected = locationIsAutoDetected
            )
        }
        set(value) {
            if (value == null) {
                prefs.edit()
                    .remove(KEY_LOC_LAT).remove(KEY_LOC_LNG).remove(KEY_LOC_CITY)
                    .remove(KEY_LOC_COUNTRY).remove(KEY_LOC_TZ).remove(KEY_LOCATION_AUTO)
                    .apply()
            } else {
                prefs.edit()
                    .putFloat(KEY_LOC_LAT, value.latitude.toFloat())
                    .putFloat(KEY_LOC_LNG, value.longitude.toFloat())
                    .putString(KEY_LOC_CITY, value.cityName)
                    .putString(KEY_LOC_COUNTRY, value.countryName)
                    .putFloat(KEY_LOC_TZ, value.timezoneOffsetHours.toFloat())
                    .putBoolean(KEY_LOCATION_AUTO, value.isAutoDetected)
                    .apply()
            }
        }

    // ---- Prayer calculation preferences ----
    var calculationMethod: CalculationMethod
        get() = enumValue(CalculationMethod.values(), prefs.getString(KEY_METHOD, null),
            CalculationMethod.MUSLIM_WORLD_LEAGUE)
        set(value) = prefs.edit().putString(KEY_METHOD, value.name).apply()

    var madhab: Madhab
        get() = enumValue(Madhab.values(), prefs.getString(KEY_MADHAB, null),
            Madhab.SHAFI_MALIKI_HANBALI)
        set(value) = prefs.edit().putString(KEY_MADHAB, value.name).apply()

    var highLatRule: HighLatitudeRule
        get() = enumValue(HighLatitudeRule.values(), prefs.getString(KEY_HIGH_LAT, null),
            HighLatitudeRule.ANGLE_BASED)
        set(value) = prefs.edit().putString(KEY_HIGH_LAT, value.name).apply()

    var adjustments: PrayerAdjustments
        get() = PrayerAdjustments(
            fajr = clampAdjustment(prefs.getInt(KEY_ADJ_FAJR, 0)),
            sunrise = clampAdjustment(prefs.getInt(KEY_ADJ_SUNRISE, 0)),
            dhuhr = clampAdjustment(prefs.getInt(KEY_ADJ_DHUR, 0)),
            asr = clampAdjustment(prefs.getInt(KEY_ADJ_ASR, 0)),
            maghrib = clampAdjustment(prefs.getInt(KEY_ADJ_MAGHRIB, 0)),
            isha = clampAdjustment(prefs.getInt(KEY_ADJ_ISHA, 0))
        )
        set(value) = prefs.edit()
            .putInt(KEY_ADJ_FAJR, value.fajr).putInt(KEY_ADJ_SUNRISE, value.sunrise)
            .putInt(KEY_ADJ_DHUR, value.dhuhr).putInt(KEY_ADJ_ASR, value.asr)
            .putInt(KEY_ADJ_MAGHRIB, value.maghrib).putInt(KEY_ADJ_ISHA, value.isha)
            .apply()

    // NUR-033: epoch millis of the last successful device-GPS fix, recorded by
    // LocationPermissionCard's detect flow so the Settings diagnostics card
    // can show "time since last successful location fix". New key on the
    // existing nour_app_prefs store — no existing key or value is touched.
    var lastLocationFixMillis: Long?
        get() {
            if (!prefs.contains(KEY_LAST_FIX_MILLIS)) return null
            val value = prefs.getLong(KEY_LAST_FIX_MILLIS, 0L)
            return value.takeIf { it > 0L }
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_LAST_FIX_MILLIS).apply()
            } else {
                prefs.edit().putLong(KEY_LAST_FIX_MILLIS, value).apply()
            }
        }

    /**
     * NUR-033: the main (non-pre-reminder) Adhan triggers we still believe are
     * scheduled, keyed by prayer name. Written by PrayerNotificationScheduler
     * when an exact alarm is set and cleared when its prayer is cancelled.
     */
    fun nextAdhanRecords(): Map<String, Long> {
        val out = LinkedHashMap<String, Long>()
        com.example.features.prayer.models.PrayerName.values().forEach { prayer ->
            val millis = prefs.getLong(KEY_ADHAN_NEXT_PREFIX + prayer.name, 0L)
            if (millis > 0L) out[prayer.name] = millis
        }
        return out
    }

    /** Last recorded exactness of the app's own Adhan scheduling attempts. */
    fun adhanExactOk(): Boolean = prefs.getBoolean(KEY_ADHAN_EXACT_OK, true)

    // ---- Quran reading continuity ----
    var readingMode: String?
        get() = prefs.getString(KEY_QURAN_MODE, null)
        set(value) = prefs.edit().putString(KEY_QURAN_MODE, value).apply()
    var arabicFontSize: Float
        get() = prefs.getFloat(KEY_QURAN_FONT, 24f).coerceIn(16f, 40f)
        set(value) = prefs.edit().putFloat(KEY_QURAN_FONT, value.coerceIn(16f, 40f)).apply()

    var quranReadingTheme: QuranReadingTheme
        get() = enumValue(
            QuranReadingTheme.values(),
            prefs.getString(KEY_QURAN_READING_THEME, null),
            QuranReadingTheme.SYSTEM
        )
        set(value) = prefs.edit().putString(KEY_QURAN_READING_THEME, value.name).apply()

    var quranTypeface: QuranTypeface
        get() = enumValue(
            QuranTypeface.values(),
            prefs.getString(KEY_QURAN_TYPEFACE, null),
            QuranTypeface.SYSTEM
        )
        set(value) = prefs.edit().putString(KEY_QURAN_TYPEFACE, value.name).apply()

    var lastReadSurah: Int
        get() = prefs.getInt(KEY_LAST_SURAH, 1).coerceIn(1, 114)
        // NUR-017: the last-read position must survive an immediate force-kill,
        // so these two writes commit synchronously to disk instead of relying
        // on apply()'s scheduled background flush. The store is a tiny
        // preferences file and the value a single int; the writer runs in a
        // LaunchedEffect coroutine on QuranScreen, not inside a frame.
        set(value) {
            prefs.edit().putInt(KEY_LAST_SURAH, value.coerceIn(1, 114)).commit()
        }

    var lastReadAyah: Int
        get() = prefs.getInt(KEY_LAST_AYAH, 1).coerceAtLeast(1)
        set(value) {
            prefs.edit().putInt(KEY_LAST_AYAH, value.coerceAtLeast(1)).commit()
        }

    // Legacy Mushaf page. The Mushaf UI (and its only writer) was removed on
    // 2026-10-26; the accessor and raw key are kept so upgrades never lose or
    // rename the stored value. No production reader/writer remains.
    var lastReadPage: Int
        get() = prefs.getInt(KEY_LAST_PAGE, 1).coerceIn(1, 604)
        set(value) {
            prefs.edit().putInt(KEY_LAST_PAGE, value.coerceIn(1, 604)).commit()
        }

    val hasReadPage: Boolean
        get() = prefs.contains(KEY_LAST_PAGE)

    private fun <T : Enum<T>> enumValue(values: Array<T>, stored: String?, default: T): T =
        values.firstOrNull { it.name == stored } ?: default

    private fun clampAdjustment(value: Int): Int = value.coerceIn(-30, 30)

    private companion object {
        const val KEY_NUMERAL_EASTERN = "numeral_use_eastern"
        const val KEY_TIME_24H = "time_use_24h"
        const val KEY_ALARMS_ENABLED = "prayer_alarms_enabled"
        const val KEY_SETUP_COMPLETED = "setup_completed"
        const val KEY_SEED_COLOR = "theme_seed_color"
        const val KEY_LOCATION_AUTO = "location_is_auto_detected"
        const val KEY_LOC_LAT = "saved_location_lat"
        const val KEY_LOC_LNG = "saved_location_lng"
        const val KEY_LOC_CITY = "saved_location_city"
        const val KEY_LOC_COUNTRY = "saved_location_country"
        const val KEY_LOC_TZ = "saved_location_tz"
        const val KEY_METHOD = "calculation_method"
        const val KEY_MADHAB = "madhab"
        const val KEY_HIGH_LAT = "high_latitude_rule"
        const val KEY_ADJ_FAJR = "adjustment_fajr"
        const val KEY_ADJ_SUNRISE = "adjustment_sunrise"
        const val KEY_ADJ_DHUR = "adjustment_dhuhr"
        const val KEY_ADJ_ASR = "adjustment_asr"
        const val KEY_ADJ_MAGHRIB = "adjustment_maghrib"
        const val KEY_ADJ_ISHA = "adjustment_isha"
        const val KEY_QURAN_MODE = "quran_reading_mode"
        const val KEY_QURAN_FONT = "quran_arabic_font_size"
        const val KEY_QURAN_READING_THEME = "quran_reading_theme"
        const val KEY_QURAN_TYPEFACE = "quran_typeface"
        const val KEY_LAST_SURAH = "quran_last_read_surah"
        const val KEY_LAST_AYAH = "quran_last_read_ayah"
        const val KEY_LAST_PAGE = "quran_last_read_page"
        // NUR-033: diagnostics-only additions (never written for anything the
        // user toggles; purely reflective state).
        const val KEY_LAST_FIX_MILLIS = "location_last_fix_millis"
        const val KEY_ADHAN_NEXT_PREFIX = "adhan_next_"
        const val KEY_ADHAN_EXACT_OK = "adhan_exact_ok"
    }
}
