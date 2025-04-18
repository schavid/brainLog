package com.example.brainlog.viewmodel

data class FilmDto(
    val id: Int,
    val original_language: String,
    val release_date: String,
    val title: String
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