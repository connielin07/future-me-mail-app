package com.example.futurememailapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Toast
import android.util.Patterns
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.futurememailapp.network.FutureMailApi
import com.example.futurememailapp.network.model.FutureMailRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WriteActivity : AppCompatActivity() {

    // --- 宣告 View ---
    private lateinit var tvWriteDate: TextView
    private lateinit var tvReceiveDate: TextView
    private lateinit var btnPickReceiveDate: Button
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSend: Button
    private lateinit var btnClear: Button

    private val futureMailService by lazy { FutureMailApi.service }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        // 1. 找到並啟用我們自己的 Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_write)
        setSupportActionBar(toolbar)

        // --- 初始化 View ---
        tvWriteDate = findViewById(R.id.tvWriteDate)
        tvReceiveDate = findViewById(R.id.tvReceiveDate)
        btnPickReceiveDate = findViewById(R.id.btnPickReceiveDate)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etEmail = findViewById(R.id.etEmail)
        btnSend = findViewById(R.id.btnSend)
        btnClear = findViewById(R.id.btnClear)

        // --- 預設寫信日期為今天 ---
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvWriteDate.text = sdf.format(today.time)

        // --- 選擇收信日期（DatePicker） ---
        btnPickReceiveDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                tvReceiveDate.text = dateStr
            }, year, month, day)

            // 限制收信日期不能早於寫信日期
            val writeCal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            writeCal.time = sdf.parse(tvWriteDate.text.toString())!!
            dpd.datePicker.minDate = writeCal.timeInMillis

            dpd.show()
        }

        btnSend.setOnClickListener { submitLetter() }
        btnClear.setOnClickListener { showClearConfirmDialog() }

        // --- 底部導覽列 ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // MainActivity
        bottomNavigationView.selectedItemId = R.id.nav3

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
                R.id.nav3 -> { // Write（當前頁，不跳轉）
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

    private fun submitLetter() {
        val writeDate = tvWriteDate.text.toString()
        val receiveDate = tvReceiveDate.text.toString()
        val subject = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val emailRaw = etEmail.text.toString().trim()
        val email = emailRaw.ifEmpty { null }
        val deviceToken = MainApplication.currentFcmToken

        if (subject.isEmpty()) {
            Toast.makeText(this, "請輸入信件主旨", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "請輸入信件內容", Toast.LENGTH_SHORT).show()
            return
        }

        if (receiveDate.isEmpty()) {
            Toast.makeText(this, "請選擇收信日期", Toast.LENGTH_SHORT).show()
            return
        }

        if (!email.isNullOrEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email 格式不正確", Toast.LENGTH_SHORT).show()
            return
        }

        // --- 檢查收信日期是否早於寄信日期 ---
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val write = sdf.parse(writeDate)
            val receive = sdf.parse(receiveDate)

            if (receive.before(write)) {
                Toast.makeText(this, "收信日期不能早於寫信日期", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "日期格式錯誤", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            toggleSendEnabled(false)
            val request = FutureMailRequest(
                writeDate = writeDate,
                receiveDate = receiveDate,
                subject = subject,
                content = content,
                email = email,
                deviceToken = deviceToken
            )

            val toastMessage = try {
                val response = futureMailService.submitMail(request)
                // 修正 Error: 必須使用 .code() 函式
                if (response.code() in 200..299) {
                    etTitle.text?.clear()
                    etContent.text?.clear()
                    etEmail.text?.clear()
                    "信件已儲存至後端"
                } else {
                    "儲存失敗：${response.code()}"
                }
            } catch (e: Exception) {
                "儲存失敗：${e.localizedMessage}"
            }

            Toast.makeText(this@WriteActivity, toastMessage, Toast.LENGTH_SHORT).show()
            toggleSendEnabled(true)
        }
    }

    private fun showClearConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("確認清空？")
            .setMessage("確定要清空所有欄位嗎？")
            .setPositiveButton("確認清空") { _, _ ->
                clearFields()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun clearFields() {
        etTitle.text?.clear()
        etContent.text?.clear()
        etEmail.text?.clear()
    }

    private fun toggleSendEnabled(enabled: Boolean) {
        btnSend.isEnabled = enabled
        btnPickReceiveDate.isEnabled = enabled
    }
}
