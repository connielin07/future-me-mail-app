package com.example.futurememailapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * 【組員 C 負責：FCM 雲端訊息服務】
 * 
 * 功能說明：
 * 此類別繼承自 FirebaseMessagingService，是 App 與雲端推播伺服器對接的「橋樑」。
 * 它專門負責在後台監聽訊息，即使 App 處於關閉狀態，也能接收信件送達的通知。
 *
 * 核心邏輯：
 * 1. 監聽伺服器發出的信件送達指令。
 * 2. 負責解析訊息內容（標題、內文）。
 * 3. 建立符合 Android 系統規範的「通知頻道」。
 * 4. 實作「點擊跳轉」功能，讓使用者能從通知直接進入總覽頁。
 */
class FutureMessagingService : FirebaseMessagingService() {

    companion object {
        /**
         * 通知頻道設定 (Android 8.0+ 規定必須要有 Channel 才能顯示通知)
         */
        private const val CHANNEL_ID = "futureme_delivery_channel" // 程式識別用的 ID
        private const val CHANNEL_NAME = "FutureMe Delivery"       // 使用者在手機設定看到的名稱
        private const val CHANNEL_DESC = "通知信件送達的推播頻道"      // 頻道的詳細描述
    }

    /**
     * 當手機接收到 FCM 訊息時，系統會自動觸發此函式。
     * 
     * @param message 包含從雲端傳來的所有資料
     */
    override fun onMessageReceived(message: RemoteMessage) {
        // --- 1. 訊息內容解析 ---
        // 優先從 notification 區塊拿資料，如果沒有，就從自訂的 data 區塊拿。
        // 這是一個「雙重保險」的寫法，確保通知一定有文字。
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "信件已送達！"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "快來看看是誰寄給你的信吧！"

        // --- 2. 執行通知顯示 ---
        showDeliveryNotification(title, body)
    }

    /**
     * 當裝置產生新的 Token 或 Token 更新時觸發。
     * 
     * Token 是這台手機在 Firebase 上的「身分證字號」，
     * 後端組員 E 需要這個 Token 才能準確把信「寄到這台手機」。
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // 輸出到 Logcat 方便開發階段測試
        Log.d("FutureMe-FCM", "裝置新 Token: $token")

        // 【關鍵對接】：將最新 Token 儲存到 MainApplication
        // 這樣在組員 B 寫好信按下傳送時，我們就能把這個 Token 一併傳給後端存檔。
        MainApplication.currentFcmToken = token
    }

    /**
     * 在手機通知列顯示美觀的通知卡片
     * 
     * @param title 通知標題
     * @param body  通知內容
     */
    private fun showDeliveryNotification(title: String, body: String) {

        // 取得系統層級的通知管理員
        val notificationManager = getSystemService<NotificationManager>() ?: return

        // --- 步驟 A：針對 Android 8.0 以上的版本建立「通知頻道」 ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 設定為高重要性，通知會跳出並發出聲音
            ).apply {
                description = CHANNEL_DESC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // --- 步驟 B：設定「點擊通知後要做什麼」 ---
        // 我們希望點擊後能精準跳轉到「總覽頁 (OverviewActivity)」
        val intent = Intent(this, OverviewActivity::class.java).apply {
            // 設定標籤：如果 App 原本就在執行，就把它拉到最前面，而不是開一個新頁面
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // 為了安全性，Android 12 (API 31) 以上規定必須明確指定 FLAG_IMMUTABLE
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // 將 Intent 包裝成 PendingIntent (這是一個延後執行的指令)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            pendingIntentFlags
        )

        // --- 步驟 C：建構通知視覺外觀 ---
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mail_black_24dp) // 設定通知列的小信封圖示
            .setContentTitle(title)                      // 設定標題
            .setContentText(body)                        // 設定內容
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 確保在舊版 Android 也能有高優先度
            .setAutoCancel(true)                         // 設定使用者點擊後，通知自動消失
            .setContentIntent(pendingIntent)             // 綁定剛才設定的跳轉動作
            .build()

        // --- 步驟 D：正式發送！ ---
        // 使用目前時間作為 ID，確保如果有多封信同時送達，通知不會互相覆蓋。
        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}
