package com.example.futurememailapp.utils

import android.app.Activity
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
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    /**
     * 切換日/夜模式，並且重啟畫面讓顏色生效
     */
    fun toggleTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)
        val newIsDark = !isDarkMode

        // 存新狀態
        prefs.edit().putBoolean(KEY_DARK_MODE, newIsDark).apply()

        // 套用到 AppCompatDelegate
        AppCompatDelegate.setDefaultNightMode(
            if (newIsDark) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        // 重繪目前 Activity，顏色才會刷新
        activity.recreate()
    }
}
