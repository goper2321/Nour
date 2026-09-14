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
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
val LocalAppStrings = compositionLocalOf { AppStrings.forLanguage(AppLanguage.ENGLISH) }
