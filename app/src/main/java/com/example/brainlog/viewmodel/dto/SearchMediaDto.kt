package com.example.brainlog.viewmodel.dto

import com.example.brainlog.viewmodel.MediumType

data class SearchMovieDto(
    val id: Int,
    val original_title: String,
    val title: String,
    val overview: String,
    val release_date: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double,
    val vote_count: Int,
    val popularity: Double,
    val original_language: String,
    val genre_ids: List<Int>,
    val adult: Boolean,
    val video: Boolean,
    val type: MediumType
)


data class SearchSeriesDto(
    val adult: Boolean,
    val backdrop_path: String?,
    val genre_ids: List<Int>,
    val id: Int,
    val origin_country: List<String>,
    val original_language: String,
    val original_name: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val first_air_date: String,
    val name: String,
    val vote_average: Double,
    val vote_count: Int,
    val type: MediumType
)


data class SearchSeriesResponse(
    val page: Int,
    val results: List<SearchSeriesDto>,
    val totalPages: Int,
    val totalResults: Int
)

data class SearchMovieResponse(
    val page: Int,
    val results: List<SearchMovieDto>,
    val totalPages: Int,
    val totalResults: Int
)