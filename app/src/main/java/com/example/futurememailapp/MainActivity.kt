package com.example.futurememailapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 找到並啟用我們自己的 Toolbar
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

        // --- 底部導覽列跳轉 ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // 預設選中 Main 頁（亮起）
        bottomNavigationView.selectedItemId = R.id.nav1

        bottomNavigationView.setOnItemSelectedListener { item ->
            // 如果點擊的項目不是當前頁面，才進行跳轉
            if (item.itemId != bottomNavigationView.selectedItemId) {
                val intent = when (item.itemId) {
                    R.id.nav2 -> Intent(this, InstructActivity::class.java)
                    R.id.nav3 -> Intent(this, WriteActivity::class.java)
                    R.id.nav4 -> Intent(this, OverviewActivity::class.java)
                    else -> null
                }
                intent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(it)
                }
            }
            true
        }
    }
}