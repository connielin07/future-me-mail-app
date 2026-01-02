package com.example.futurememailapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.futurememailapp.utils.ThemeHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * MainActivity（首頁）
 *
 * 功能說明：
 * 本頁為 App 入口首頁，提供：
 * 1) 顯示首頁文案（tvQuote）與兩個主要操作按鈕（開始寫信 / 操作教學）
 * 2) 右上角選單（menu_main）：主題切換（淺色/深色）+ 系統資訊入口
 * 3) 底部導覽列（BottomNavigationView）：快速切換 Home / Instruct / Write / Overview
 * 4) Android 13+ POST_NOTIFICATIONS 權限請求（確保推播通知可正常顯示）
 *
 * UI 對應：
 * - activity_main.xml
 *   - toolbar_main：上方工具列
 *   - btnGoWrite：開始寫信
 *   - btnGoTutorial：操作教學
 *   - bottomNavigationView：底部導覽列
 *
 * 設計重點：
 * - 透過 ThemeHelper 套用使用者儲存的日/夜模式
 * - 教學按鈕僅在「本次 App 使用 Session」中點一次後隱藏（避免重複打擾）
 */
class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * hasClickedTutorialSession
         * 用途：控制「操作教學」按鈕在同一次 app 執行期間只顯示一次
         * - true 代表本次 session 已點過 → 進入首頁時直接隱藏
         * - 這是「暫存於記憶體」的狀態，App 重開後會恢復預設（false）
         */
        private var hasClickedTutorialSession = false
    }

    /**
     * requestNotificationPermission
     * 使用 Activity Result API 請求通知權限（Android 13+）
     * - Callback 目前不處理結果（no-op），因為 UI 不需要依結果改變流程
     * - 目的：確保推播功能能正常顯示通知
     */
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // =========================
        // 1) 套用儲存的主題（日/夜模式）
        // =========================
        // 需在 setContentView 前呼叫，確保頁面載入時就使用正確 Theme
        ThemeHelper.applySavedTheme(this)

        setContentView(R.layout.activity_main)

        // =========================
        // 2) 請求通知權限（Android 13+）
        // =========================
        // 注意：你這裡呼叫一次 ensureNotificationPermission()
        ensureNotificationPermission()

        // 設定首頁 Toolbar 為 ActionBar（可載入右上角 menu）
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // --- 按鈕跳轉：寫信頁 ---
        // 使用者點擊後進入 WriteActivity，開始撰寫信件
        val btnGoWrite = findViewById<Button>(R.id.btnGoWrite)
        btnGoWrite.setOnClickListener {
            startActivity(Intent(this, WriteActivity::class.java))
        }

        // --- 按鈕跳轉：操作教學（Onboarding） ---
        // 使用 session 旗標控制：本次執行期間點一次就隱藏
        val btnGoTutorial = findViewById<Button>(R.id.btnGoTutorial)

        // 若本 session 已點過教學 → 直接隱藏按鈕
        if (hasClickedTutorialSession) {
            btnGoTutorial.visibility = View.GONE
        }

        // 點擊教學按鈕：
        // 1) 更新 session 狀態
        // 2) 隱藏按鈕（避免回到首頁後仍顯示）
        // 3) 進入 OnboardingActivity
        btnGoTutorial.setOnClickListener {
            hasClickedTutorialSession = true
            btnGoTutorial.visibility = View.GONE
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        // =========================
        // 3) 底部導覽列（BottomNavigation）
        // =========================
        // 目的：在四個核心頁面間快速切換
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // 預設選取 Home（nav1）讓使用者知道目前所在頁面
        bottomNavigationView.selectedItemId = R.id.nav1

        // 依使用者點選項目跳轉至不同 Activity
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> true // Home：當前頁，不跳轉

                R.id.nav2 -> { // Instruct
                    startActivity(Intent(this, InstructActivity::class.java))
                    true
                }

                R.id.nav3 -> { // Write
                    startActivity(Intent(this, WriteActivity::class.java))
                    true
                }

                R.id.nav4 -> { // Overview
                    startActivity(Intent(this, OverviewActivity::class.java))
                    true
                }

                else -> false
            }
        }

        // ⚠️ 注意（僅註解提醒，不修改）：
        // 你在 setContentView 後已呼叫 ensureNotificationPermission()，
        // 但下方（檔案末尾）沒有再呼叫一次，這裡就正常。
        // 若你本機版本曾出現重複呼叫，建議日後統一只保留一次即可。
    }

    /**
     * onCreateOptionsMenu
     * 功能：載入右上角選單（menu_main.xml）
     * - 內容包含：主題切換（子選單）+ 系統資訊
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * onPrepareOptionsMenu
     * 功能：在選單顯示前，根據目前主題狀態「自動勾選」正確選項
     * - 讓 UI 狀態與實際主題一致（避免使用者混淆）
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isDark = ThemeHelper.isDarkMode(this)
        menu.findItem(R.id.action_theme_dark)?.isChecked = isDark
        menu.findItem(R.id.action_theme_light)?.isChecked = !isDark
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * onOptionsItemSelected
     * 功能：處理右上角選單點擊事件
     * - 淺色模式 / 深色模式：寫入偏好並 recreate() 立刻刷新 UI
     * - 系統資訊：導向 SystemInfoActivity
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            // --- 主題：淺色 ---
            R.id.action_theme_light -> {
                item.isChecked = true
                ThemeHelper.setDarkMode(this, false)
                recreate() // 重新建立 Activity，以立即套用新的 Theme
                true
            }

            // --- 主題：深色 ---
            R.id.action_theme_dark -> {
                item.isChecked = true
                ThemeHelper.setDarkMode(this, true)
                recreate() // 重新建立 Activity，以立即套用新的 Theme
                true
            }

            // --- 系統資訊頁 ---
            // 顯示 App 版本、支援系統、技術列表等資訊
            R.id.action_system_info -> {
                startActivity(Intent(this, SystemInfoActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * ensureNotificationPermission
     *
     * 功能說明：
     * Android 13（TIRAMISU / API 33）開始，
     * 通知權限（POST_NOTIFICATIONS）需要使用者明確授權。
     *
     * 這段邏輯會：
     * 1) 檢查目前系統版本是否需要請求通知權限
     * 2) 若尚未授權，則透過 Activity Result API 發出請求
     */
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
