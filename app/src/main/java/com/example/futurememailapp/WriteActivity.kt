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

/**
 * WriteActivity
 *
 * 功能說明：
 * 「撰寫信件」頁面，提供使用者輸入信件主旨/內容，選擇收信日期，並送出到後端儲存。
 *
 * 核心流程：
 * 1) 預設寫信日期為今天（tvWriteDate）
 * 2) 透過 DatePickerDialog 選擇收信日期（tvReceiveDate）
 *    - 限制收信日期不能早於寫信日期（minDate）
 * 3) 表單驗證：
 *    - 主旨、內容、收信日期必填
 *    - Email 若有填需符合格式
 *    - 收信日期不得早於寫信日期（再次檢查）
 * 4) 使用 Retrofit + Coroutine（lifecycleScope）呼叫 submitMail() 送出到後端
 * 5) 送出期間暫停按鈕避免重複點擊
 * 6) 提供清空功能與確認 Dialog
 * 7) 底部導覽列切換頁面
 *
 * UI 對應：
 * - activity_write.xml
 */
class WriteActivity : AppCompatActivity() {

    // =========================
    // 1) 宣告 View（對應 XML 元件）
    // =========================
    private lateinit var tvWriteDate: TextView           // 寫信日期（預設今天）
    private lateinit var tvReceiveDate: TextView         // 收信日期（使用者選擇）
    private lateinit var btnPickReceiveDate: Button      // 打開 DatePicker 的按鈕
    private lateinit var etTitle: EditText               // 主旨輸入框
    private lateinit var etContent: EditText             // 內容輸入框
    private lateinit var etEmail: EditText               // Email（可選）
    private lateinit var btnSend: Button                 // 送出按鈕
    private lateinit var btnClear: Button                // 清空按鈕

    /**
     * futureMailService
     * Retrofit API 服務（FutureMailApi.service）
     * lazy：首次使用才建立，避免過早初始化
     */
    private val futureMailService by lazy { FutureMailApi.service }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 載入撰寫信件頁面 XML
        setContentView(R.layout.activity_write)

        // =========================
        // 2) Toolbar 設定
        // =========================
        // 將 MaterialToolbar 設定為此 Activity 的 ActionBar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_write)
        setSupportActionBar(toolbar)

        // =========================
        // 3) 初始化 View：將 XML 元件綁到變數
        // =========================
        tvWriteDate = findViewById(R.id.tvWriteDate)
        tvReceiveDate = findViewById(R.id.tvReceiveDate)
        btnPickReceiveDate = findViewById(R.id.btnPickReceiveDate)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etEmail = findViewById(R.id.etEmail)
        btnSend = findViewById(R.id.btnSend)
        btnClear = findViewById(R.id.btnClear)

        // =========================
        // 4) 預設寫信日期為今天（yyyy-MM-dd）
        // =========================
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tvWriteDate.text = sdf.format(today.time)

        // =========================
        // 5) 選擇收信日期（DatePickerDialog）
        // =========================
        btnPickReceiveDate.setOnClickListener {

            // 以今天作為 DatePicker 預設顯示日期
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // DatePickerDialog：選好日期後更新 tvReceiveDate
            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->

                // 格式化成 yyyy-MM-dd（注意 month 要 +1，因為 Calendar 月份從 0 起算）
                val dateStr = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                tvReceiveDate.text = dateStr
            }, year, month, day)

            // ---------- 限制：收信日期不得早於寫信日期 ----------
            // 讀取 tvWriteDate 上的日期，轉成 Calendar，設定給 DatePicker 的 minDate
            val writeCal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            writeCal.time = sdf.parse(tvWriteDate.text.toString())!!

            // DatePicker 允許選擇的最小日期（毫秒）
            dpd.datePicker.minDate = writeCal.timeInMillis

            dpd.show()
        }

        // =========================
        // 6) 按鈕事件：送出 / 清空
        // =========================
        btnSend.setOnClickListener { submitLetter() }
        btnClear.setOnClickListener { showClearConfirmDialog() }

        // =========================
        // 7) 底部導覽列
        // =========================
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // 預設選中 Write（nav3）
        bottomNavigationView.selectedItemId = R.id.nav3

        // 監聽底部導覽列點擊，負責 Activity 切換
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

    /**
     * submitLetter
     *
     * 功能說明：
     * 1) 取得使用者輸入（日期/主旨/內容/email）
     * 2) 進行表單驗證（必填、Email 格式、日期先後）
     * 3) 建立 FutureMailRequest 並透過 Retrofit POST 到後端
     * 4) 送出中暫時禁用按鈕避免重複點擊
     * 5) 成功後清空欄位並顯示 Toast
     *
     * 特別點：
     * - deviceToken：從 MainApplication.currentFcmToken 取得（FCM 推播用）
     */
    private fun submitLetter() {

        // 取得欄位內容（trim 避免前後空白）
        val writeDate = tvWriteDate.text.toString()
        val receiveDate = tvReceiveDate.text.toString()
        val subject = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        // Email 可選：若空字串就轉成 null（後端可用 null 判斷未提供）
        val emailRaw = etEmail.text.toString().trim()
        val email = emailRaw.ifEmpty { null }

        // FCM Token（可能為 null：例如尚未取得 token）
        val deviceToken = MainApplication.currentFcmToken

        // =========================
        // 1) 基本欄位驗證（必填）
        // =========================
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

        // =========================
        // 2) Email 格式驗證（有填才檢查）
        // =========================
        if (!email.isNullOrEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email 格式不正確", Toast.LENGTH_SHORT).show()
            return
        }

        // =========================
        // 3) 收信日期不得早於寫信日期（再次檢查）
        // =========================
        // 注意：雖然 DatePicker 已設 minDate，但此處再做一次防呆檢查較安全
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val write = sdf.parse(writeDate)
            val receive = sdf.parse(receiveDate)

            if (receive.before(write)) {
                Toast.makeText(this, "收信日期不能早於寫信日期", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            // 日期解析失敗（格式錯誤）
            Toast.makeText(this, "日期格式錯誤", Toast.LENGTH_SHORT).show()
            return
        }

        // =========================
        // 4) 非同步送出（Coroutine）
        // =========================
        lifecycleScope.launch {

            // 送出期間禁用按鈕，避免重複送出
            toggleSendEnabled(false)

            // 建立 POST request body
            val request = FutureMailRequest(
                writeDate = writeDate,
                receiveDate = receiveDate,
                subject = subject,
                content = content,
                email = email,
                deviceToken = deviceToken
            )

            // 預設用 try-catch 包網路呼叫，避免崩潰
            val toastMessage = try {
                val response = futureMailService.submitMail(request)

                // response.code() 是 HTTP 狀態碼（200-299 代表成功）
                if (response.code() in 200..299) {

                    // 成功：清空欄位（保留日期）
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

            // 顯示送出結果
            Toast.makeText(this@WriteActivity, toastMessage, Toast.LENGTH_SHORT).show()

            // 恢復按鈕可點擊
            toggleSendEnabled(true)
        }
    }

    /**
     * showClearConfirmDialog
     *
     * 功能說明：
     * 清空前跳出確認視窗，避免使用者誤觸造成資料消失。
     */
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

    /**
     * clearFields
     *
     * 功能：
     * 清空輸入欄位（主旨/內容/Email）
     * 注意：日期欄位不清空，避免使用者需要重新選擇日期
     */
    private fun clearFields() {
        etTitle.text?.clear()
        etContent.text?.clear()
        etEmail.text?.clear()
    }

    /**
     * toggleSendEnabled
     *
     * 功能：
     * 控制「送出」與「選日期」按鈕是否可點擊，用於避免重複送出
     *
     * @param enabled true 表示可點擊；false 表示禁用
     */
    private fun toggleSendEnabled(enabled: Boolean) {
        btnSend.isEnabled = enabled
        btnPickReceiveDate.isEnabled = enabled
    }
}
