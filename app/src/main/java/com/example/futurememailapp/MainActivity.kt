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
import com.example.futurememailapp.utils.LanguageHelper
import com.example.futurememailapp.utils.ThemeHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    companion object {
        private var hasClickedTutorialSession = false
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 套用目前儲存的日/夜模式
        ThemeHelper.applySavedTheme(this)

        // 套用目前儲存的語言
        LanguageHelper.applySavedLanguage(this)

        setContentView(R.layout.activity_main)
        ensureNotificationPermission()

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // --- 按鈕跳轉寫信頁 ---
        val btnGoWrite = findViewById<Button>(R.id.btnGoWrite)
        btnGoWrite.setOnClickListener {
            startActivity(Intent(this, WriteActivity::class.java))
        }

        // --- 按鈕跳轉操作教學頁 ---
        val btnGoTutorial = findViewById<Button>(R.id.btnGoTutorial)

        if (hasClickedTutorialSession) {
            btnGoTutorial.visibility = View.GONE
        }

        btnGoTutorial.setOnClickListener {
            hasClickedTutorialSession = true
            btnGoTutorial.visibility = View.GONE
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        // --- 底部導覽列 ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav1

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> true
                R.id.nav2 -> {
                    startActivity(Intent(this, InstructActivity::class.java))
                    true
                }
                R.id.nav3 -> {
                    startActivity(Intent(this, WriteActivity::class.java))
                    true
                }
                R.id.nav4 -> {
                    startActivity(Intent(this, OverviewActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // 右上角選單：載入 menu_main
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // 讓下拉選單自動勾選目前狀態
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isDark = ThemeHelper.isDarkMode(this)
        menu.findItem(R.id.action_theme_dark)?.isChecked = isDark
        menu.findItem(R.id.action_theme_light)?.isChecked = !isDark
        return super.onPrepareOptionsMenu(menu)
    }

    // 點選選單項目
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            // --- 主題：淺色 ---
            R.id.action_theme_light -> {
                item.isChecked = true
                ThemeHelper.setDarkMode(this, false)
                recreate() // 保證立刻刷新畫面
                true
            }

            // --- 主題：深色 ---
            R.id.action_theme_dark -> {
                item.isChecked = true
                ThemeHelper.setDarkMode(this, true)
                recreate() // 保證立刻刷新畫面
                true
            }

            // ✅ 新增：系統資訊
            R.id.action_system_info -> {
                startActivity(Intent(this, SystemInfoActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

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
