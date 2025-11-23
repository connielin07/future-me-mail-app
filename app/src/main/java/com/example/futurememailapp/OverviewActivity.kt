package com.example.futurememailapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class OverviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        val header1: MaterialButton = findViewById(R.id.header1)
        val content1: NestedScrollView = findViewById(R.id.content1)

        header1.setOnClickListener {
            val expanded = content1.isVisible
            content1.isVisible = !expanded
            header1.icon = ContextCompat.getDrawable(
                this,
                if (expanded) R.drawable.ic_expand_more_24dp else R.drawable.ic_expand_less_24dp
            )
        }

        // --- 底部導覽列跳轉 ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // 預設選中 Overview 頁（亮起）
        bottomNavigationView.selectedItemId = R.id.nav4

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> { // Home
                    startActivity(Intent(this, MainActivity::class.java))
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
                R.id.nav4 -> { // Overview（當前頁，不跳）
                    true
                }
                else -> false
            }
        }
    }
}
