package com.example.futurememailapp.network.model

import com.google.gson.annotations.SerializedName

/**
 * MailsResponse
 *
 * 功能說明：
 * 此 data class 為「取得信件列表 / 收信總覽」API 的回應資料模型（Response DTO），
 * 用於接收後端回傳的信件資料集合中「單一信件」的結構。
 *
 * 使用情境：
 * - OverviewActivity（信箱 / 收信總覽）
 * - RecyclerView 顯示信件列表
 * - 判斷信件是否已寄送（delivered）
 *
 * 設計重點：
 * - 欄位名稱大多與後端 JSON 相同，僅在必要時使用 @SerializedName
 * - 可選欄位（nullable）避免後端資料不完整時造成解析錯誤
 */
data class MailsResponse(

    // 信件唯一識別 ID（由後端產生）
    val id: String,

    // 信件主旨
    val subject: String,

    // 信件內容
    val content: String,

    // 寫信日期（字串格式，例如 yyyy-MM-dd）
    val writeDate: String,

    // 預計收信 / 寄送日期
    val receiveDate: String,

    // 收信人 Email（可能為 null，代表僅使用裝置推播）
    val email: String? = null,

    // 裝置推播 Token（FCM）
    // 使用 @SerializedName 明確對應後端欄位名稱
    @SerializedName("deviceToken")
    val deviceToken: String? = null,

    // 是否已寄送狀態
    // 通常由後端以數值表示（例如：0 = 未寄送，1 = 已寄送）
    // 設為 nullable 以避免後端尚未提供該欄位時解析失敗
    @SerializedName("delivered")
    val delivered: Int? = null,

    // 實際寄送時間（後端完成寄送時才會有值）
    // 若尚未寄送，可能為 null
    @SerializedName("deliveredAt")
    val deliveredAt: String? = null,

    // 信件建立時間（後端系統時間）
    // 通常用於排序或顯示建立順序
    @SerializedName("createdAt")
    val createdAt: String
)
