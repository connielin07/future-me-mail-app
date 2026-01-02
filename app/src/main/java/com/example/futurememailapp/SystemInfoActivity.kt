package com.example.futurememailapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * SystemInfoActivity
 *
 * 功能說明：
 * 此 Activity 為「系統資訊」頁面，用於顯示應用程式的基本資訊，
 * 例如：
 * 1) App 版本號
 * 2) 支援系統版本
 * 3) 使用的技術列表（由 XML 靜態呈現）
 *
 * 使用情境：
 * - 使用者於 MainActivity 右上角選單中點選「系統資訊」
 * - 進入此頁查看應用程式相關資訊
 *
 * UI 對應：
 * - activity_system_info.xml
 *   - toolbar：上方工具列
 *   - tvVersion：顯示 App 版本號
 */
class SystemInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 載入系統資訊頁面的版型
        setContentView(R.layout.activity_system_info)

        // =========================
        // 1) Toolbar 設定
        // =========================
        // 使用自訂的 MaterialToolbar 作為 ActionBar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 啟用返回箭頭（Up Button）
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 設定頁面標題
        supportActionBar?.title = "系統資訊"

        // 點擊 Toolbar 左上角返回鍵時，關閉此 Activity
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // =========================
        // 2) 顯示 App 版本號
        // =========================
        // 透過 PackageManager 讀取目前 App 的 versionName
        val tvVersion = findViewById<TextView>(R.id.tvVersion)

        // 從系統取得 App 版本資訊（versionName 定義於 build.gradle）
        val versionName = packageManager
            .getPackageInfo(packageName, 0)
            .versionName

        // 將版本號顯示在畫面上
        tvVersion.text = "App 版本：$versionName"
    }
}
