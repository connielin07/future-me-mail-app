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
        val writeDateTextView: TextView = findViewById(R.id.detailWriteDateTextView) // 新的 TextView
        val deliveryDateTextView: TextView = findViewById(R.id.detailDeliveryDateTextView)

        // 2. 將 Toolbar 設定為此 Activity 的 ActionBar
        setSupportActionBar(toolbar)

        // 3. 啟用返回箭頭
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // 4. 取得從 Intent 傳來的資料
        val subject = intent.getStringExtra(EXTRA_SUBJECT)
        val writeDate = intent.getStringExtra(EXTRA_WRITE_DATE) // 接收新的撰寫日期
        val deliveryDate = intent.getStringExtra(EXTRA_DELIVERY_DATE)
        
        // 5. 將資料設定到畫面上
        subjectTextView.text = subject
        writeDateTextView.text = "寫於: $writeDate" // 設定撰寫日期的文字
        deliveryDateTextView.text = "預計寄送: $deliveryDate"
    }

    // 6. 覆寫這個方法來處理返回箭頭的點擊
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // 7. 定義所有用來傳遞資料的標籤
    companion object {
        const val EXTRA_SUBJECT = "com.example.futurememailapp.EXTRA_SUBJECT"
        const val EXTRA_WRITE_DATE = "com.example.futurememailapp.EXTRA_WRITE_DATE" // 新增的標籤
        const val EXTRA_DELIVERY_DATE = "com.example.futurememailapp.EXTRA_DELIVERY_DATE"
    }
}
