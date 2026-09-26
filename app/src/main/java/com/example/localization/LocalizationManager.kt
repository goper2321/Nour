package com.example.localization

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

object LocalizationManager {
    private const val PREFS_NAME = "nour_app_preferences"
    private const val KEY_LANGUAGE = "selected_app_language"

    fun getSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        return AppLanguage.fromCode(code)
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        applySystemLocale(context, language)
    }

    /**
     * Maps an in-app language to its platform [java.util.Locale].
     *
     * `AppLanguage.code` is already a BCP-47 language tag, so build the Locale
     * from that single source of truth instead of repeating a parallel `when`
     * that has to be kept in sync by hand. (The `Locale(String)` constructor is
     * also deprecated in API 36; `forLanguageTag` is the replacement.)
     */
    fun getLocale(language: AppLanguage): java.util.Locale =
        java.util.Locale.forLanguageTag(language.code)

    /**
     * Syncs system locale for dates, numbers, permission dialogs and
     * Android 13+ per-app language (LocaleManager) without extra deps.
     * In-app Compose strings remain the source of truth via LocalAppStrings.
     */
    fun applySystemLocale(context: Context, language: AppLanguage) {
        try {
            val locale = getLocale(language)
            java.util.Locale.setDefault(locale)
            val res = context.resources
            val config = android.content.res.Configuration(res.configuration)
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
                localeManager?.applicationLocales =
                    android.os.LocaleList.forLanguageTags(language.code)
            }
        } catch (_: Exception) {
            // Best-effort: in-app strings still work even if system sync fails
        }
    }

    fun attachBaseContext(base: Context): Context {
        return try {
            val lang = getSavedLanguage(base)
            val locale = getLocale(lang)
            val config = android.content.res.Configuration(base.resources.configuration)
            config.setLocale(locale)
            base.createConfigurationContext(config)
        } catch (_: Exception) {
            base
        }
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
val LocalAppStrings = compositionLocalOf { AppStrings.forLanguage(AppLanguage.ENGLISH) }

/** true = 24-hour clock (17:30), false = 12-hour clock (5:30 PM). */
val LocalUse24HourFormat = compositionLocalOf { true }
