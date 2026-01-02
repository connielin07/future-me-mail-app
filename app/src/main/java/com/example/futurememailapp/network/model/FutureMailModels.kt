package com.example.futurememailapp.network.model

import com.google.gson.annotations.SerializedName

/**
 * FutureMailRequest
 *
 * 功能說明：
 * 此 data class 為「寄送未來信件」API 的請求資料模型（Request DTO），
 * 用於將 App 端蒐集到的信件資訊轉換成 JSON，
 * 透過 Retrofit + Gson 傳送至後端伺服器。
 *
 * 設計重點：
 * - 使用 @SerializedName 明確對應後端 API 所需的 JSON 欄位名稱
 * - 欄位命名與後端契約（API Contract）保持一致
 * - 可選欄位（email、deviceToken）使用 nullable 型別，增加彈性
 */
data class FutureMailRequest(

    // 寫信日期（字串格式，通常為 yyyy-MM-dd）
    // 對應後端 JSON 欄位："writeDate"
    @SerializedName("writeDate")
    val writeDate: String,

    // 收信日期（未來送達日期）
    // 對應後端 JSON 欄位："receiveDate"
    @SerializedName("receiveDate")
    val receiveDate: String,

    // 信件主旨
    // 對應後端 JSON 欄位："subject"
    @SerializedName("subject")
    val subject: String,

    // 信件內容
    // 對應後端 JSON 欄位："content"
    @SerializedName("content")
    val content: String,

    // 收信人 Email（可選）
    // 若為 null，代表僅使用裝置推播或其他方式通知
    @SerializedName("email")
    val email: String? = null,

    // 裝置推播 Token（FCM）
    // 用於後端在收信日期觸發推播通知
    // 為可選欄位，避免在尚未取得 Token 時造成送出失敗
    @SerializedName("deviceToken")
    val deviceToken: String? = null
)

/**
 * FutureMailResponse
 *
 * 功能說明：
 * 此 data class 為「寄送未來信件」API 的回應資料模型（Response DTO），
 * 用於接收後端回傳的結果資訊。
 *
 * 設計考量：
 * - 所有欄位皆設為 nullable，避免因後端回傳不完整資料導致解析錯誤
 * - 可依 status 或 message 判斷請求是否成功
 */
data class FutureMailResponse(

    // 後端產生的信件 ID（成功建立時回傳）
    val id: String? = null,

    // 請求狀態（例如 success / error）
    val status: String? = null,

    // 後端回傳的訊息（成功或錯誤說明）
    val message: String? = null
)
