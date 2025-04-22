package com.example.brainlog.viewmodel

data class FilmDto(
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val release_date: String,
    val title: String,
    val duration: Int,
    val genre: String,
    val director: String

)

data class SerieDto(
    val id: Int,
    val title: String,
    val year: String,
    val description: String,
    val genre: String,
    val director: String,
    val episode_duration: Int,
    val seasons: Int,
    val episodes: Int
)

data class BuchDto(
    val id: Int,
    val title: String,
    val year: String,
    val description: String,
    val genre: String,
    val pages: Int,
    val author: String
)

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
val video: Boolean
)

data class SearchResultDto(
    val page: Int,
    val results: List<SearchMovieDto>,
    val totalPages: Int,
    val totalResults: Int
)

