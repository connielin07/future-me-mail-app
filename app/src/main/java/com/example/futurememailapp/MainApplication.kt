package com.example.futurememailapp

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 在 App 啟動時，初始化新的日期時間庫
        AndroidThreeTen.init(this)
    }
}
