package com.example.futurememailapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.futurememailapp.databinding.ActivitySystemInfoBinding

class SystemInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySystemInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySystemInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 設定 Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "系統資訊"

        // 返回鍵
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // 顯示版本資訊
        val versionName = packageManager
            .getPackageInfo(packageName, 0).versionName

        binding.tvVersion.text = "App 版本：$versionName"
    }
}
