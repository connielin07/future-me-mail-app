package com.example.futurememailapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * LetterDetailActivity
 *
 * 功能說明：
 * 此 Activity 為「信件內容頁」，負責顯示單一封信的完整資訊，
 * 包含：
 * 1) 信件主旨
 * 2) 寫信日期
 * 3) 預計寄送日期
 * 4) 信件內文
 *
 * 使用情境：
 * - 使用者於「收信總覽（Overview）」點擊某一封信件
 * - 透過 Intent 傳遞資料，開啟此頁顯示完整內容
 *
 * UI 對應：
 * - activity_letter_detail.xml
 */
class LetterDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 載入信件內容頁的版型
        setContentView(R.layout.activity_letter_detail)

        // =========================
        // 1) 取得畫面上的 UI 元件
        // =========================
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val subjectTextView: TextView = findViewById(R.id.detailSubjectTextView)
        val writeDateTextView: TextView = findViewById(R.id.detailWriteDateTextView)
        val deliveryDateTextView: TextView = findViewById(R.id.detailDeliveryDateTextView)
        val contentTextView: TextView =
            findViewById(R.id.detailContentTextView) // 信件內文顯示區

        // =========================
        // 2) Toolbar 設定
        // =========================
        // 將自訂的 MaterialToolbar 設為此 Activity 的 ActionBar
        setSupportActionBar(toolbar)

        // =========================
        // 3) 啟用返回箭頭（Up Button）
        // =========================
        // 讓使用者可從信件內容頁返回上一頁（收信總覽）
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // =========================
        // 4) 接收從 Intent 傳來的資料
        // =========================
        // 這些資料通常由 OverviewActivity / RecyclerView Item 點擊時傳入
        val subject = intent.getStringExtra(EXTRA_SUBJECT)
        val writeDate = intent.getStringExtra(EXTRA_WRITE_DATE)
        val deliveryDate = intent.getStringExtra(EXTRA_DELIVERY_DATE)
        val content = intent.getStringExtra(EXTRA_CONTENT)

        // =========================
        // 5) 將資料顯示到畫面上
        // =========================
        subjectTextView.text = subject
        writeDateTextView.text = "寫於: $writeDate"
        deliveryDateTextView.text = "預計寄送: $deliveryDate"
        contentTextView.text = content
    }

    /**
     * onOptionsItemSelected
     *
     * 功能說明：
     * 攔截 Toolbar 返回箭頭（Home / Up）的點擊事件，
     * 點擊後結束此 Activity，回到上一頁。
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // 結束此頁，回到前一個 Activity
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Companion Object
     *
     * 功能說明：
     * 集中定義 Intent 傳遞資料所使用的 key，
     * 避免在不同 Activity 中硬編碼字串造成錯誤。
     */
    companion object {

        // 傳遞信件主旨
        const val EXTRA_SUBJECT =
            "com.example.futurememailapp.EXTRA_SUBJECT"

        // 傳遞寫信日期
        const val EXTRA_WRITE_DATE =
            "com.example.futurememailapp.EXTRA_WRITE_DATE"

        // 傳遞預計寄送日期
        const val EXTRA_DELIVERY_DATE =
            "com.example.futurememailapp.EXTRA_DELIVERY_DATE"

        // 傳遞信件內文
        const val EXTRA_CONTENT =
            "com.example.futurememailapp.EXTRA_CONTENT"
    }
}
