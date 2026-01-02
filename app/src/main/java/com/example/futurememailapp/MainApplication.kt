package com.example.futurememailapp

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * MainApplication
 *
 * 功能說明：
 * 此類別繼承 Application，作為整個 App 的「全域初始化入口」，
 * 會在任何 Activity / Service 建立之前被系統呼叫一次。
 *
 * 使用目的：
 * 1) 初始化第三方套件（Firebase、ThreeTenABP）
 * 2) 提前取得並保存 FCM Token
 * 3) 提供跨 Activity / Service 可共用的全域狀態
 *
 * 注意：
 * - 此類別需在 AndroidManifest.xml 中透過
 *   android:name=".MainApplication" 指定，才會生效
 */
class MainApplication : Application() {

    companion object {
        /**
         * currentFcmToken
         *
         * 功能：
         * 儲存目前裝置的 Firebase Cloud Messaging Token
         *
         * 設計原因：
         * - 讓整個 App（Activity / Service）都能存取最新的 Token
         * - 在送出信件時可一併傳給後端，用於日後推播通知
         *
         * @Volatile：
         * - 確保多執行緒存取時的可見性
         * - 避免背景執行緒取得舊值
         */
        @Volatile
        var currentFcmToken: String? = null
    }

    /**
     * onCreate
     *
     * 功能說明：
     * App 啟動時最早被呼叫的方法，
     * 適合用來做「全 App 只需要做一次」的初始化工作。
     */
    override fun onCreate() {
        super.onCreate()

        // =========================
        // 1) 初始化 ThreeTenABP（日期時間函式庫）
        // =========================
        // 提供 Java 8 java.time API 的相容實作
        // 常用於日期運算、日曆顯示等功能
        AndroidThreeTen.init(this)

        // =========================
        // 2) 初始化 Firebase
        // =========================
        // 讀取 google-services.json，建立 Firebase App 實例
        FirebaseApp.initializeApp(this)

        // =========================
        // 3) 取得並保存 FCM Token
        // =========================
        // App 啟動時主動向 Firebase 取得目前裝置的推播 Token
        fetchFcmToken()
    }

    /**
     * fetchFcmToken
     *
     * 功能說明：
     * 向 Firebase Cloud Messaging 服務請求目前裝置的 Token，
     * 並將結果保存至 currentFcmToken。
     *
     * 使用情境：
     * - App 啟動時初始化
     * - 若 Token 更新（見 FirebaseMessagingService.onNewToken）
     *   也會同步更新此變數
     */
    private fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                // 若請求失敗，輸出警告 Log 並中止
                if (!task.isSuccessful) {
                    Log.w(
                        "FutureMe-FCM",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@addOnCompleteListener
                }

                // 成功取得 Token
                val token = task.result

                // 保存至全域變數，供其他元件使用
                currentFcmToken = token

                // 輸出 Token（僅供開發除錯用）
                Log.d("FutureMe-FCM", "FCM token: $token")
            }
    }
}
