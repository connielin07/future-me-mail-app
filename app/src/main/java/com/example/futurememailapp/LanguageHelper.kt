package com.example.futurememailapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageHelper {
    private const val PREFS = "app_settings"
    private const val KEY_LANG = "app_lang"

    fun applySavedLanguage(context: Context) {
        val tag = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "zh-TW") ?: "zh-TW"
        apply(tag)
    }

    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "zh-TW") ?: "zh-TW"
    }

    fun setLanguage(context: Context, languageTag: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, languageTag)
            .apply()
        apply(languageTag)
    }

    private fun apply(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }
}
