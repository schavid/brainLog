package com.example.brainlog.model

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.example.brainlog.BuildConfig


object ApiClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val TOKEN = BuildConfig.TMDB_API_KEY


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