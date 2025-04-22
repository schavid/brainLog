package com.example.brainlog.viewmodel

import retrofit2.http.GET
import retrofit2.http.Query



interface MediaAPI {
    // Suchendpunkte
    @GET("search/movie")
    suspend fun searchFilms(@Query("query") query: String): SearchMovieResponse

    @GET("search/tv")
    suspend fun searchSeries(@Query("query") query: String): SearchSeriesResponse

    @GET("books/search")
    suspend fun searchBooks(@Query("query") query: String): List<BuchDto>


}