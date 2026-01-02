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

/**
 * OnboardingActivity
 *
 * 功能說明：
 * 此 Activity 為「新手引導 / 操作教學導覽」頁面，提供使用者分頁式介紹 App 功能流程，
 * 主要包含：
 * 1) ViewPager2：左右滑動切換導覽頁
 * 2) TabLayout：頁面指示器（點點 / indicator）
 * 3) 下一步/完成按鈕：控制切換下一頁或結束導覽
 *
 * 使用情境：
 * - 使用者從首頁點選「操作教學」後進入
 * - 完成導覽後引導使用者直接進入 WriteActivity 開始寫信
 *
 * UI 對應：
 * - activity_onboarding.xml
 *   - viewPagerOnboarding：承載導覽頁的 ViewPager2
 *   - tabIndicator：頁面指示器（TabLayout）
 *   - btnNextOrFinish：下一步/完成按鈕
 */
class OnboardingActivity : AppCompatActivity() {

    // ViewPager2：負責顯示多個導覽頁面（Fragment）
    private lateinit var viewPager: ViewPager2

    // 下一步 / 完成 按鈕
    private lateinit var btnNextOrFinish: Button

    // TabLayout：作為頁面指示器（點點）
    private lateinit var tabIndicator: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 載入 Onboarding 版型（ViewPager2 + 指示器 + 按鈕）
        setContentView(R.layout.activity_onboarding)

        // 取得 XML 元件
        viewPager = findViewById(R.id.viewPagerOnboarding)
        btnNextOrFinish = findViewById(R.id.btnNextOrFinish)
        tabIndicator = findViewById(R.id.tabIndicator)

        // =========================
        // 1) 定義導覽頁資料（每頁的標題、描述、圖片）
        // =========================
        // pages：導覽頁的資料集合
        // - 每一筆 OnboardingData 對應一個 OnboardingFragment 頁面
        val pages = listOf(

            // 頁面 1：歡迎頁（通常可放 App Logo / 概念圖）
            OnboardingData(
                "歡迎來到信運草",
                "這是一個專為您設計的『未來信件』應用程式。\n\n" +
                        "透過它，您可以將此刻的心情、夢想與期許，封存給未來的自己。\n\n" +
                        "準備好，開始寫下第一封信了嗎？",
                0 // 這裡目前使用 0，代表沒有指定圖片或使用預設處理
            ),

            // 頁面 2：撰寫信件流程
            OnboardingData(
                "一、寄出第一封時光信件",
                "1. 設定『寄達日期』：決定您何時收到來自過去的訊息。\n" +
                        "2. 撰寫內容：填寫信件的主旨與內容。\n" +
                        "3. 連結 Email：可選擇是否連結您的 Email 帳號進行備份接收。",
                R.drawable.tutorial_step1
            ),

            // 頁面 3：等待收信（推播通知概念）
            OnboardingData(
                "二、靜待時光，收取心意",
                "1. 通知提醒：到達設定的日期時，您將會收到通知，提醒您有信件已送達。",
                R.drawable.tutorial_step2
            ),

            // 頁面 4：收信總覽（Overview 月曆與信箱）
            OnboardingData(
                "三、您的時光信件總覽",
                "1. 日曆檢視：在月曆上查看所有信件的寄達日期，清晰掌握未來。\n" +
                        "2. 隨時回顧：進入『收信總覽』頁面，在信箱中點擊信件即可完整回顧。",
                R.drawable.tutorial_step3
            )
        )

        // =========================
        // 2) 設定 ViewPager2 Adapter
        // =========================
        // 使用 FragmentStateAdapter：適合多頁 Fragment、可有效回收頁面
        val adapter = OnboardingPagerAdapter(this, pages)
        viewPager.adapter = adapter

        // =========================
        // 3) 將 TabLayout 與 ViewPager2 綁定（頁面指示器）
        // =========================
        // TabLayoutMediator：官方推薦的 ViewPager2 指示器綁定方式
        // 這裡不需要設定 tab 的文字或圖示，
        // 因為你已用 tabBackground / selector 來控制外觀（點點樣式）
        TabLayoutMediator(tabIndicator, viewPager) { tab, position ->
            // no-op：外觀由 TabLayout 的 style/selector 控制
        }.attach()

        // =========================
        // 4) 監聽頁面切換：更新按鈕文字（下一步 / 完成）
        // =========================
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // 若是最後一頁 → 按鈕顯示「完成」
                // 否則顯示「下一步」
                if (position == pages.size - 1) {
                    btnNextOrFinish.text = "完成"
                } else {
                    btnNextOrFinish.text = "下一步"
                }
            }
        })

        // =========================
        // 5) 下一步 / 完成 按鈕點擊事件
        // =========================
        btnNextOrFinish.setOnClickListener {

            // 若目前在最後一頁 → 結束導覽並前往寫信頁
            if (viewPager.currentItem == pages.size - 1) {
                navigateToWriteActivity()
            } else {
                // 否則切換到下一頁
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }
    }

    /**
     * navigateToWriteActivity
     *
     * 功能說明：
     * 完成導覽後，直接導向 WriteActivity，
     * 並 finish() 關閉導覽頁，避免使用者按返回鍵回到 Onboarding。
     */
    private fun navigateToWriteActivity() {
        startActivity(Intent(this, WriteActivity::class.java))
        finish()
    }
}

/**
 * OnboardingData
 *
 * 功能說明：
 * 用來描述每一頁導覽內容的資料類別（Data Model）。
 *
 * @param title       每頁標題
 * @param description 每頁文字描述
 * @param imageResId  每頁圖片資源 ID（Drawable）
 *
 * @DrawableRes：
 * - 編譯期標註，提醒此參數應傳入 drawable resource id
 */
data class OnboardingData(
    val title: String,
    val description: String,
    @DrawableRes val imageResId: Int
)

/**
 * OnboardingPagerAdapter
 *
 * 功能說明：
 * ViewPager2 的 Adapter，負責根據 position 建立對應的 Fragment。
 *
 * 設計重點：
 * - 每頁使用 OnboardingFragment 呈現 UI（標題/文字/圖片）
 * - 透過 newInstance 傳入 title、description、imageResId
 */
class OnboardingPagerAdapter(
    activity: FragmentActivity,
    private val pages: List<OnboardingData>
) : FragmentStateAdapter(activity) {

    // 總頁數
    override fun getItemCount(): Int = pages.size

    // 根據 position 建立對應 Fragment
    override fun createFragment(position: Int): Fragment {
        val data = pages[position]

        // 將頁面資料傳入 Fragment（通常使用 arguments Bundle）
        return OnboardingFragment.newInstance(
            data.title,
            data.description,
            data.imageResId
        )
    }
}
