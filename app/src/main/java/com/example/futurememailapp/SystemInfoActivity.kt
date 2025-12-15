package com.example.futurememailapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class SystemInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_system_info)

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "系統資訊"

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 版本號
        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        tvVersion.text = "App 版本：$versionName"
    }
}
