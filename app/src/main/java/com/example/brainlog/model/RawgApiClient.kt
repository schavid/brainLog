package com.example.brainlog.model

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.brainlog.BuildConfig

private const val RAWG_API_KEY = BuildConfig.RAWG_GAME_API
private const val RAWG_BASE_URL = "https://api.rawg.io/api/"


object RawgApiClient {

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val originalHttpUrl = originalRequest.url

            // Füge den API-Schlüssel als Query-Parameter hinzu
            val url = originalHttpUrl.newBuilder()
                .addQueryParameter("key", RAWG_API_KEY)
                .build()

            val requestBuilder = originalRequest.newBuilder().url(url)
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(RAWG_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create()) // Du verwendest Moshi, das passt
        .client(client)
        .build()

    val rawgApi: MediaAPI = retrofit.create(MediaAPI::class.java)
}



