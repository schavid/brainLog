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

data class SearchFilmDto(
    val id: Int,
    val original_title: String,
    val release_date: String
)