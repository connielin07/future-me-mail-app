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

        //進入 App 先套用儲存的日夜模式
        ThemeHelper.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ensureNotificationPermission()

        //啟用自訂 Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // ➤ 跳到寫信頁
        val btnGoWrite = findViewById<Button>(R.id.btnGoWrite)
        btnGoWrite.setOnClickListener {
            startActivity(Intent(this, WriteActivity::class.java))
        }

        // ➤ 跳到教學頁
        val btnGoTutorial = findViewById<Button>(R.id.btnGoTutorial)
        btnGoTutorial.setOnClickListener {
            startActivity(Intent(this, InstructActivity::class.java))
        }

        // ─── Bottom Navigation ───
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav1

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> true                          // Home（留在這頁）
                R.id.nav2 -> { startActivity(Intent(this, InstructActivity::class.java)); true }
                R.id.nav3 -> { startActivity(Intent(this, WriteActivity::class.java)); true }
                R.id.nav4 -> { startActivity(Intent(this, OverviewActivity::class.java)); true }
                else -> false
            }
        }
    }

    // 右上齒輪選單 (載入 menu_main.xml)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // 點齒輪 ➜ 進入設定頁（含日夜、語言切換 UI）
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Android 13+ 啟用推播權限
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
