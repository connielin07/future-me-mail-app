package com.example.futurememailapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    private const val PREFS_NAME = "app_theme_prefs"
    private const val KEY_DARK_MODE = "key_dark_mode"

    /**
     * 在每個 Activity 的 onCreate 一開始呼叫：
     * ThemeHelper.applySavedTheme(this)
     * 用來套用上次儲存的日/夜模式
     */
    fun applySavedTheme(context: Context) {
        val isDark = isDarkMode(context)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    /**
     * ✅ 讓 MainActivity.onPrepareOptionsMenu() 可以讀取目前儲存的模式
     */
    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    /**
     * ✅ 讓 MainActivity.onOptionsItemSelected() 可以直接切換並儲存
     */
    fun setDarkMode(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
