package com.example.brainlog.viewmodel




data class Movie(
    override val id: Int,
    override val title: String,
    override val description: String,
    override val genres: List<String>,
    override val releaseDate: String,
    val runtime: Int?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: Double,
    val votes: Int,
    val tagline: String?
) : Medium



data class Series(
    override val id: Int,
    override val title: String,
    val originalName: String,
    override val description: String,
    val posterUrl: String?,
    override val releaseDate: String,
    val lastAirDate: String?,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    override val genres: List<String>,
    val country: List<String>,
    val originalLanguage: String,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
) : Medium

