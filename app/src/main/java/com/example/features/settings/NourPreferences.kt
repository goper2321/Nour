package com.example.features.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.UserLocation

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

    // ---- Quran reading continuity ----
    var readingMode: String?
        get() = prefs.getString(KEY_QURAN_MODE, null)
        set(value) = prefs.edit().putString(KEY_QURAN_MODE, value).apply()

    var arabicFontSize: Float
        get() = prefs.getFloat(KEY_QURAN_FONT, 24f).coerceIn(16f, 40f)
        set(value) = prefs.edit().putFloat(KEY_QURAN_FONT, value.coerceIn(16f, 40f)).apply()

    var lastReadSurah: Int
        get() = prefs.getInt(KEY_LAST_SURAH, 1).coerceIn(1, 114)
        set(value) = prefs.edit().putInt(KEY_LAST_SURAH, value.coerceIn(1, 114)).apply()

    var lastReadAyah: Int
        get() = prefs.getInt(KEY_LAST_AYAH, 1).coerceAtLeast(1)
        set(value) = prefs.edit().putInt(KEY_LAST_AYAH, value.coerceAtLeast(1)).apply()

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
        const val KEY_LAST_SURAH = "quran_last_read_surah"
        const val KEY_LAST_AYAH = "quran_last_read_ayah"
    }
}
