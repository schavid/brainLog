package com.example.brainlog.viewmodel

data class FilmDto(
    val title: String,
    val year: Int,
    val duration: Int,
    val genre: String,
    val description: String,
    val director: String
)

data class SerieDto(
    val title: String,
    val year: Int,
    val description: String,
    val genre: String,
    val director: String,
    val episode_duration: Int,
    val seasons: Int,
    val episodes: Int
)

data class BuchDto(
    val title: String,
    val year: Int,
    val description: String,
    val genre: String,
    val pages: Int,
    val author: String
)