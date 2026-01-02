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
 * FutureMessagingService
 *
 * 功能說明：
 * 此類別繼承 FirebaseMessagingService，
 * 用於接收 Firebase Cloud Messaging（FCM）推播訊息，
 * 並在信件送達時顯示系統通知給使用者。
 *
 * 使用情境：
 * - 未來信件到達指定日期
 * - 後端透過 FCM 發送推播通知
 * - App 接收後顯示通知並導向信件總覽頁
 */
class FutureMessagingService : FirebaseMessagingService() {

    companion object {
        // 推播通知頻道 ID（Android 8.0 以上必須）
        private const val CHANNEL_ID = "futureme_delivery_channel"

        // 推播通知頻道名稱（顯示於系統設定）
        private const val CHANNEL_NAME = "FutureMe Delivery"

        // 推播通知頻道描述
        private const val CHANNEL_DESC = "通知信件送達的推播頻道"
    }

    /**
     * onMessageReceived
     *
     * 當裝置收到 FCM 推播訊息時呼叫
     *
     * @param message RemoteMessage
     *        包含 notification payload 或 data payload
     */
    override fun onMessageReceived(message: RemoteMessage) {

        // 優先從 notification payload 取得標題
        // 若不存在，則從 data payload 取得
        // 若仍不存在，使用預設文字
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "信件已送達！"

        // 推播內容文字（同樣具備多層 fallback）
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "點擊查看你的信件"

        // 顯示系統通知
        showDeliveryNotification(title, body)
    }

    /**
     * onNewToken
     *
     * 當 FCM Token 產生或更新時呼叫
     *
     * 使用情境：
     * - App 第一次安裝
     * - 使用者清除資料
     * - Firebase 重新發放 Token
     *
     * @param token 最新的 FCM 裝置 Token
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // 將 Token 輸出到 Logcat，方便除錯
        Log.d("FutureMe-FCM", "Refreshed token: $token")

        // 將最新 Token 儲存至 Application 層
        // 供後續送出信件時一併傳給後端
        MainApplication.currentFcmToken = token
    }

    /**
     * showDeliveryNotification
     *
     * 功能：
     * 建立並顯示「信件已送達」的系統通知
     *
     * @param title 通知標題
     * @param body  通知內容文字
     */
    private fun showDeliveryNotification(title: String, body: String) {

        // 取得系統 NotificationManager
        val notificationManager = getSystemService<NotificationManager>() ?: return

        // Android 8.0（API 26）以上必須先建立 NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 高重要性，確保顯示通知
            ).apply {
                description = CHANNEL_DESC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 點擊通知後導向「收信總覽頁」
        val intent = Intent(this, OverviewActivity::class.java).apply {
            // 確保不重複建立 Activity
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // PendingIntent flags：
        // Android 6.0 以上建議加上 FLAG_IMMUTABLE 以提升安全性
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // 建立 PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            pendingIntentFlags
        )

        // 建立通知內容
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mail_black_24dp) // 通知小圖示
            .setContentTitle(title)                      // 通知標題
            .setContentText(body)                        // 通知內容
            .setAutoCancel(true)                         // 點擊後自動消失
            .setContentIntent(pendingIntent)             // 點擊後執行動作
            .build()

        // 發送通知（使用時間戳記作為通知 ID，避免覆蓋）
        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}
