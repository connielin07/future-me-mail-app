package com.example.futurememailapp.network

import com.example.futurememailapp.BuildConfig
import com.example.futurememailapp.network.model.FutureMailRequest
import com.example.futurememailapp.network.model.FutureMailResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface FutureMailService {
    @POST("api/future-mails")
    suspend fun submitMail(@Body request: FutureMailRequest): Response<FutureMailResponse>
}

object FutureMailApi {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
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
