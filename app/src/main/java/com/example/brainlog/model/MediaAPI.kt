package com.example.brainlog.model

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.brainlog.viewmodel.dto.MovieDetailDto
import com.example.brainlog.viewmodel.dto.SearchMovieResponse
import com.example.brainlog.viewmodel.dto.SearchSeriesResponse
import com.example.brainlog.viewmodel.dto.TvShowDto


interface MediaAPI {
    // Suchendpunkte
    @GET("search/movie")
    suspend fun searchFilms(@Query("query") query: String): SearchMovieResponse

    @GET("search/tv")
    suspend fun searchSeries(@Query("query") query: String): SearchSeriesResponse


    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): MovieDetailDto

    @GET("tv/{series_id}")
    suspend fun getSeriesDetails(
        @Path("series_id") seriesId: Int,
        @Query("language") language: String = "en-US"
    ): TvShowDto


}