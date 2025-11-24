package com.example.futurememailapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class LetterDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_letter_detail)

        // 1. 找到畫面上的元件
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val subjectTextView: TextView = findViewById(R.id.detailSubjectTextView)
        val dateTextView: TextView = findViewById(R.id.detailDeliveryDateTextView)

        // 2. 將 Toolbar 設定為此 Activity 的 ActionBar (最關鍵的步驟)
        setSupportActionBar(toolbar)

        // 3. 啟用返回箭頭，並讓 ActionBar 處理它
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // 4. 取得從 Intent 傳來的資料
        val subject = intent.getStringExtra(EXTRA_SUBJECT)
        val deliveryDate = intent.getStringExtra(EXTRA_DELIVERY_DATE)
        
        // 5. 將資料設定到畫面上
        subjectTextView.text = subject
        dateTextView.text = "預計寄送: $deliveryDate"
    }

    // 6. 覆寫這個方法來處理 ActionBar 上的所有按鈕點擊，包括返回箭頭
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // android.R.id.home 是返回箭頭的標準 ID
        if (item.itemId == android.R.id.home) {
            finish() // 結束目前的 Activity，回到上一頁
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_SUBJECT = "com.example.futurememailapp.EXTRA_SUBJECT"
        const val EXTRA_DELIVERY_DATE = "com.example.futurememailapp.EXTRA_DELIVERY_DATE"
    }
}
