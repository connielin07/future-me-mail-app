package com.example.futurememailapp
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView

class WriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_write)

        // --- 底部導覽列 ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // 預設選中 Write 頁（避免顯示在 Home）
        bottomNavigationView.selectedItemId = R.id.nav3

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> { // Home
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav2 -> { // Instruct (先放著空的，以後再做)
                    true
                }
                R.id.nav3 -> { // Write（當前頁，不跳）
                    true
                }
                R.id.nav4 -> { // Overview（之後要做的頁面）
                    true
                }
                else -> false
            }
        }
    }
}