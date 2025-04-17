package com.example.brainlog.viewmodel

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory



object ApiClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI2MzdmZTk5Njc5MjQ3OTEzMDRlOGNjODA0OGZiMmRhMiIsIm5iZiI6MTc0NDg4MDUxNC4zNjA5OTk4LCJzdWIiOiI2ODAwYzM4MmYzOWM3MzAxMjVkOTRmZWMiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.VMTfcmkceJ56C30F-k2l6hsxcmg-WVjjri8n8pKnIQo"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $TOKEN")
                .addHeader("accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .build()

    val mediaApi: MediaAPI = retrofit.create(MediaAPI::class.java)
}