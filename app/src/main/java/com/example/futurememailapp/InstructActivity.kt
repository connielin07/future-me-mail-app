package com.example.futurememailapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class InstructActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruct)

        // 對應 activity_instruct.xml 裡的 VideoView（id = videoTutorial）
        val videoView = findViewById<VideoView>(R.id.videoTutorial)

        val mediaController = MediaController(this).apply {
            setAnchorView(videoView)
        }
        videoView.setMediaController(mediaController)

        // raw 資料夾裡要有 instruct_test.mp4 之類的檔案
        val uri = Uri.parse("android.resource://${packageName}/${R.raw.instruct_test}")
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { it.isLooping = true }
        videoView.start()

        // --- 底部導覽列跳轉 ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // 預設選中 Instruct 頁（亮起）
        bottomNavigationView.selectedItemId = R.id.nav2
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> { // Home
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav2 -> { // Instruct（當前頁，不跳）
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
