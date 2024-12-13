package com.example.composesongspot.ui.theme.network

import com.example.composesongspot.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    const val BASE_URL = "https://api.audd.io/"
    const val API_TOKEN = BuildConfig.API_TOKEN

    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AuddApiService by lazy {
        retrofit.create(AuddApiService::class.java)
    }
}