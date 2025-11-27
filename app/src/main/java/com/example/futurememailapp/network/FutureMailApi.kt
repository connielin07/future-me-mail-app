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

interface FutureMailService {
    // 「送出」信件
    @POST("api/future-mails")
    suspend fun submitMail(@Body request: FutureMailRequest): Response<FutureMailResponse>

    // 「取得」所有信件 (修正：移除結尾的斜線)
    @GET("api/future-mails")
    suspend fun getMails(): Response<List<MailsResponse>>

    // 「健康檢查」
    @GET("api/health")
    suspend fun healthCheck(): Response<Unit>
}

object FutureMailApi {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // 將日誌等級調整為 BODY，來取得最詳細的請求資訊
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val service: FutureMailService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(FutureMailService::class.java)
    }
}
