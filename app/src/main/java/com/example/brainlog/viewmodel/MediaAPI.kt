package com.example.brainlog.viewmodel

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query



interface MediaAPI {
    // Suchendpunkte
    @GET("search/movie")
    suspend fun searchFilms(@Query("query") query: String): SearchMovieResponse

    @GET("search/tv")
    suspend fun searchSeries(@Query("query") query: String): SearchSeriesResponse

    @GET("books/search")
    suspend fun searchBooks(@Query("query") query: String): List<BuchDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): MovieDetailDto


}