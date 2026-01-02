package com.example.futurememailapp.network

import com.example.futurememailapp.BuildConfig
import com.example.futurememailapp.network.model.FutureMailRequest
import com.example.futurememailapp.network.model.FutureMailResponse
import com.example.futurememailapp.network.model.MailsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * FutureMailService
 *
 * 功能說明：
 * 此介面為 Retrofit API 定義介面，
 * 用來描述 App 與後端 Future Mail 系統之間的 HTTP API 契約（Contract）。
 *
 * 設計重點：
 * - 使用 suspend function，搭配 Kotlin Coroutine 非同步呼叫
 * - 每一個 function 對應一個後端 API Endpoint
 * - 使用 Response<T> 以便取得 HTTP 狀態碼與回應內容
 */
interface FutureMailService {

    /**
     * 送出未來信件
     *
     * HTTP Method：POST
     * API 路徑：api/future-mails
     *
     * @param request FutureMailRequest
     *        包含寫信日期、收信日期、主旨、內容、Email、裝置 Token 等資料
     *
     * @return Response<FutureMailResponse>
     *         後端回傳信件建立結果（id / status / message）
     */
    @POST("api/future-mails")
    suspend fun submitMail(
        @Body request: FutureMailRequest
    ): Response<FutureMailResponse>

    /**
     * 取得所有信件列表
     *
     * HTTP Method：GET
     * API 路徑：api/future-mails
     *
     * 使用情境：
     * - 收信總覽（OverviewActivity）
     * - RecyclerView 顯示信件清單
     *
     * @return Response<List<MailsResponse>>
     *         後端回傳所有信件的列表資料
     */
    @GET("api/future-mails")
    suspend fun getMails(): Response<List<MailsResponse>>

    /**
     * 健康檢查（Health Check）
     *
     * HTTP Method：GET
     * API 路徑：api/health
     *
     * 使用目的：
     * - 確認後端服務是否正常運作
     * - 開發或除錯階段用來測試連線狀態
     *
     * @return Response<Unit>
     *         不需要回傳內容，只需確認 HTTP 狀態碼
     */
    @GET("api/health")
    suspend fun healthCheck(): Response<Unit>
}

/**
 * FutureMailApi
 *
 * 功能說明：
 * 此 object 負責建立 Retrofit 實例與 OkHttpClient，
 * 並對外提供 FutureMailService 供其他層（Repository / ViewModel）使用。
 *
 * 設計方式：
 * - 使用 object + lazy，確保 Retrofit 僅建立一次（Singleton）
 * - 集中管理 Base URL、Converter、Client 設定
 */
object FutureMailApi {

    /**
     * HttpLoggingInterceptor
     *
     * 功能：
     * - 攔截 HTTP 請求與回應
     * - 將請求 URL、Header、Body、Response 印出到 Logcat
     *
     * 使用目的：
     * - 開發與除錯階段方便檢查 API 是否正確呼叫
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // BODY 等級會輸出最完整的請求與回應內容
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttpClient
     *
     * 功能：
     * - 作為 Retrofit 的底層 HTTP Client
     * - 加入 loggingInterceptor 以記錄 API 呼叫過程
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * FutureMailService 實例
     *
     * - 使用 lazy 延遲初始化，避免 App 啟動時立即建立物件
     * - Base URL 由 BuildConfig.BACKEND_BASE_URL 提供
     *   （依 buildType 可切換不同後端環境）
     */
    val service: FutureMailService by lazy {
        Retrofit.Builder()
            // 後端 API Base URL（由 build.gradle.kts 中的 buildConfigField 注入）
            .baseUrl(BuildConfig.BACKEND_BASE_URL)

            // Gson 轉換器：負責 JSON <-> Kotlin 物件轉換
            .addConverterFactory(GsonConverterFactory.create())

            // 使用自訂的 OkHttpClient
            .client(okHttpClient)

            // 建立 Retrofit 實例並產生 API Service
            .build()
            .create(FutureMailService::class.java)
    }
}
