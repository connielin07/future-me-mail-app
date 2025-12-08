package com.example.futurememailapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK = "is_dark"

    /** 在每個 Activity onCreate 一開始呼叫，套用目前的主題 */
    fun applySavedTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean(KEY_IS_DARK, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    /** 切換日 / 夜模式並存起來 */
    fun toggleTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean(KEY_IS_DARK, false)
        val newIsDark = !isDark

        prefs.edit().putBoolean(KEY_IS_DARK, newIsDark).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (newIsDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
