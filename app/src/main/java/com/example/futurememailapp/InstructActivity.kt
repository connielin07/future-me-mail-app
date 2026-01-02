package com.example.futurememailapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * InstructActivity
 *
 * 功能說明：
 * 這是「操作教學」頁面，提供使用者：
 * 1) 文字教學內容（在 activity_instruct.xml 的 ScrollView 中）
 * 2) 教學影片預覽縮圖 → 點擊後開始播放（VideoView）
 * 3) 底部導覽列切換到其他核心頁面（Home / Write / Overview）
 *
 * UI 對應：
 * - activity_instruct.xml
 *   - toolbar_instruct：頁面上方工具列
 *   - videoPreviewContainer：影片縮圖 + 播放圖示的容器（FrameLayout）
 *   - videoTutorial：真正播放影片的 VideoView（預設 hidden）
 *   - bottomNavigationView：底部導覽列
 */
class InstructActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 載入此頁面的 UI 版型（文字教學 + 影片區 + 底部導覽列）
        setContentView(R.layout.activity_instruct)

        // =========================
        // 1) Toolbar 設定
        // =========================
        // 找到 XML 中的 MaterialToolbar 並設為此 Activity 的 ActionBar
        // 目的：統一整個 App 的上方導覽風格（標題、選單等）
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_instruct)
        setSupportActionBar(toolbar)

        // =========================
        // 2) 教學影片播放邏輯
        // =========================
        // videoTutorial：真正播放影片的 VideoView（XML 預設為 gone）
        val videoView = findViewById<VideoView>(R.id.videoTutorial)

        // videoPreviewContainer：縮圖 + 播放 icon 的預覽容器（使用者一開始看到的）
        val previewContainer = findViewById<View>(R.id.videoPreviewContainer)

        // MediaController：提供播放控制（播放/暫停/進度）
        // setAnchorView(videoView)：把控制器「掛在」VideoView 上
        val mediaController = MediaController(this).apply {
            setAnchorView(videoView)
        }
        videoView.setMediaController(mediaController)

        // 使用 try-catch 保護：
        // - 若 raw/instruct_demo 檔案不存在或讀取失敗，避免 App 直接崩潰
        try {
            // 影片來源：res/raw/instruct_demo
            // android.resource://<package>/<resId> 為 Android 內建資源 URI 格式
            val uri = Uri.parse("android.resource://${packageName}/${R.raw.instruct_demo}")
            videoView.setVideoURI(uri)

            // setOnPreparedListener：
            // 影片準備完成時觸發，可在這裡設定播放器參數
            videoView.setOnPreparedListener { mediaPlayer ->
                // 設定循環播放：讓教學影片重複播放，使用者可隨時觀看
                mediaPlayer.isLooping = true
            }

            // 點擊預覽縮圖後：
            // - 隱藏縮圖容器
            // - 顯示 VideoView
            // - 開始播放影片
            previewContainer.setOnClickListener {
                previewContainer.visibility = View.GONE
                videoView.visibility = View.VISIBLE
                videoView.start()
            }
        } catch (e: Exception) {
            // 若影片資源不存在 / 解析錯誤：
            // 直接把影片相關 UI 隱藏，避免畫面出現破版或空白區塊
            videoView.visibility = View.GONE
            previewContainer.visibility = View.GONE
        }

        // =========================
        // 3) 底部導覽列跳轉（BottomNavigationView）
        // =========================
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // 預設選中「Instruct」頁籤（nav2 亮起）
        // 目的：讓使用者知道自己目前在哪一頁
        bottomNavigationView.selectedItemId = R.id.nav2

        // 設定點擊事件：依選到的 itemId 跳轉到對應 Activity
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> { // Home
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.nav2 -> { // Instruct（當前頁，不跳轉）
                    true
                }

                R.id.nav3 -> { // Write
                    startActivity(Intent(this, WriteActivity::class.java))
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
}
