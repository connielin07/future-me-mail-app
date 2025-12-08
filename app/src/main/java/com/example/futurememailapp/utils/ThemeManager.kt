package com.example.futurememailapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val PREF_NAME = "app_settings"
    private const val THEME_KEY = "theme_mode"

    fun applySavedTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isNight = prefs.getBoolean(THEME_KEY, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isNight) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun toggleTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isNight = prefs.getBoolean(THEME_KEY, false)

        prefs.edit().putBoolean(THEME_KEY, !isNight).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (!isNight) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
