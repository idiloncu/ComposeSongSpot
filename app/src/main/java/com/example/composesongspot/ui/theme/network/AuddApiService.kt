package com.example.composesongspot.ui.theme.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuddApiService {
    @GET("https://api.audd.io/")
    fun searchSong(
        @Query("api_token") apiToken: String,
        @Query("url") url: String
    ): Call<SongResponse>

    @POST("somePostMethod")
    fun somePostMethod(@Body requestBody: RequestBody): Call<SongResponse>
}