package com.example.futurememailapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.VideoView
import android.widget.MediaController
import android.net.Uri

class InstructActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruct)

        val videoView = findViewById<VideoView>(R.id.videoView)

        val mediaController = MediaController(this).apply {
            setAnchorView(videoView)
        }
        videoView.setMediaController(mediaController)

        val uri = Uri.parse("android.resource://${packageName}/${R.raw.instruct_test}")
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { it.isLooping = true }
        videoView.start()

    }
}