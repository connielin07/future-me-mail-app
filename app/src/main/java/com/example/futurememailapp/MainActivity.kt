package com.example.futurememailapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.futurememailapp.utils.ThemeHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 套用目前儲存的日/夜模式
        ThemeHelper.applySavedTheme(this)

        setContentView(R.layout.activity_main)
        ensureNotificationPermission()

        // 啟用自訂 Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // --- 按鈕跳轉寫信頁 ---
        val btnGoWrite = findViewById<Button>(R.id.btnGoWrite)
        btnGoWrite.setOnClickListener {
            startActivity(Intent(this, WriteActivity::class.java))
        }

        // --- 按鈕跳轉操作教學頁 ---
        val btnGoTutorial = findViewById<Button>(R.id.btnGoTutorial)
        btnGoTutorial.setOnClickListener {
            startActivity(Intent(this, InstructActivity::class.java))
        }

        // --- 底部導覽列 ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // 預設選中首頁
        bottomNavigationView.selectedItemId = R.id.nav1

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> { // Home（當前頁，不跳）
                    true
                }
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
    }

    // 右上角齒輪選單：載入 menu_main
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // 點選齒輪裡的項目
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 這裡要跟 menu_main.xml 的 id 對到：action_settings
            R.id.action_settings -> {
                // 開啟我們剛做好的設定頁（SettingsActivity）
                startActivity(Intent(this, SettingsActivity::class.java))
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
