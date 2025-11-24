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

class InstructActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruct)

        // 1. 找到並啟用我們自己的 Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar_instruct)
        setSupportActionBar(toolbar)

        // --- 影片播放邏輯 ---
        val videoView = findViewById<VideoView>(R.id.videoTutorial)
        val mediaController = MediaController(this).apply {
            setAnchorView(videoView)
        }
        videoView.setMediaController(mediaController)

        // 使用 try-catch 保護，避免因為找不到影片檔而崩潰
        try {
            val uri = Uri.parse("android.resource://${packageName}/${R.raw.instruct_test}")
            videoView.setVideoURI(uri)
            videoView.setOnPreparedListener { it.isLooping = true }
            videoView.start()
        } catch (e: Exception) {
            // 如果找不到影片，就把它藏起來
            videoView.visibility = View.GONE
        }

        // --- 底部導覽列跳轉 ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav2

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId != bottomNavigationView.selectedItemId) {
                val intent = when (item.itemId) {
                    R.id.nav1 -> Intent(this, MainActivity::class.java)
                    R.id.nav3 -> Intent(this, WriteActivity::class.java)
                    R.id.nav4 -> Intent(this, OverviewActivity::class.java)
                    else -> null
                }
                intent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(it)
                }
            }
            true
        }
    }
}