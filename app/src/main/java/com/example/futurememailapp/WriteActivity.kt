package com.example.futurememailapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class WriteActivity : AppCompatActivity() {

    // --- 宣告 View ---
    private lateinit var tvWriteDate: TextView
    private lateinit var tvReceiveDate: TextView
    private lateinit var btnPickReceiveDate: Button
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_write)

        // --- 初始化 View ---
        tvWriteDate = findViewById(R.id.tvWriteDate)
        tvReceiveDate = findViewById(R.id.tvReceiveDate)
        btnPickReceiveDate = findViewById(R.id.btnPickReceiveDate)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)

        // --- 1. 預設寫信日期為今天 ---
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvWriteDate.text = sdf.format(today.time)

        // --- 2. 選擇收信日期（DatePicker） ---
        btnPickReceiveDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // month 從 0 開始，所以要 +1
                val dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                tvReceiveDate.text = dateStr
            }, year, month, day)
            dpd.show()
        }

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