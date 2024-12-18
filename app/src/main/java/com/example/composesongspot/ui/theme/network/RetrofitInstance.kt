package com.example.composesongspot.ui.theme.network

import com.example.composesongspot.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "https://api.audd.io/"
    const val API_TOKEN = BuildConfig.API_TOKEN

    private val retrofit by lazy {
        val interceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AuddApiService by lazy {
        retrofit.create(AuddApiService::class.java)
    }
}