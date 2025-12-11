package com.example.futurememailapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNextOrFinish: Button
    private lateinit var tabIndicator: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPagerOnboarding)
        btnNextOrFinish = findViewById(R.id.btnNextOrFinish)
        tabIndicator = findViewById(R.id.tabIndicator)

        val pages = listOf(
            // 頁面 1: 歡迎
            OnboardingData(
                "歡迎來到信運草",
                "這是一個專為您設計的『未來信件』應用程式。\n\n" +
                        "透過它，您可以將此刻的心情、夢想與期許，封存給未來的自己。\n\n" +
                        "準備好，開始寫下第一封信了嗎？",
                0 // 建議使用 App Logo 或一個代表時光膠囊/信件的圖示
            ),

            // 頁面 2: 撰寫信件
            OnboardingData(
                "一、寄出第一封時光信件",
                "1. 設定『寄達日期』：決定您何時收到來自過去的訊息。\n" +
                        "2. 撰寫內容：填寫信件的主旨與內容。\n" +
                        "3. 連結 Email：可選擇是否連結您的 Email 帳號進行備份接收。",
                R.drawable.tutorial_write_step1
            ),
            // 頁面 3: 等待收信
            OnboardingData(
                "二、靜待時光，收取心意",
                "1. 通知提醒：到達設定的日期時，您將會收到通知，提醒您有信件已送達。\n" +
                        "2. 隨時回顧：進入『收信總覽』頁面，在信箱中點擊信件即可完整回顧。",
                0 // 請替換成您的收信圖片 ID
            ),
            // 頁面 4: 收信總覽
            OnboardingData(
                "三、您的時光信件總覽",
                "1. 日曆檢視：在月曆上查看所有信件的寄達日期，清晰掌握未來。\n" +
                        "2. 信件狀態：查看已發出、等待中或已送達的信件清單。",
                0 // 請替換成您的總覽圖片 ID
            )
        )

        val adapter = OnboardingPagerAdapter(this, pages)
        viewPager.adapter = adapter

        // 將 ViewPager2 與 TabLayout 連結作為頁面指示器
        TabLayoutMediator(tabIndicator, viewPager) { tab, position ->
            // 此處不需要設定文字或圖示，TabBackground 會控制外觀
        }.attach()

        // 監聽頁面切換
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 檢查是否為最後一頁
                if (position == pages.size - 1) {
                    btnNextOrFinish.text = "完成"
                } else {
                    btnNextOrFinish.text = "下一步"
                }
            }
        })

        // 處理按鈕點擊
        btnNextOrFinish.setOnClickListener {
            if (viewPager.currentItem == pages.size - 1) {
                // 最後一頁 -> 完成導覽並跳轉到 InstructActivity
                navigateToInstructActivity()
            } else {
                // 非最後一頁 -> 跳到下一頁
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }
    }

    private fun navigateToInstructActivity() {
        // 跳轉到正式的操作教學頁面
        startActivity(Intent(this, InstructActivity::class.java))
        finish() // 關閉 OnboardingActivity，使用者按返回鍵時不會回到引導頁
    }
}

// 修改 OnboardingData 類別，加入圖片資源 ID 欄位
data class OnboardingData(val title: String, val description: String, @DrawableRes val imageResId: Int)

// 修改 OnboardingPagerAdapter 類別的 createFragment 函式
class OnboardingPagerAdapter(activity: FragmentActivity, private val pages: List<OnboardingData>) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = pages.size
    override fun createFragment(position: Int): Fragment {
        val data = pages[position]
        // 傳遞圖片資源 ID
        return OnboardingFragment.newInstance(data.title, data.description, data.imageResId)
    }
}