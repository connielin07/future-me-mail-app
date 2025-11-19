package com.example.futurememailapp

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

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
    }
}
