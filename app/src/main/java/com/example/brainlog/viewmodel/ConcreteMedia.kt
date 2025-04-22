package com.example.brainlog.viewmodel

/*data class Book(
    override val id: Int,
    override val title: String,
    override val year: String,
    override val description: String,
    override val genre: String,
    val pages: Int,
    val author: String
) : Medium*/


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



/*data class Series (
    override val id: Int,
    override val title: String,
    override val year: String,
    override val description: String,
    override val genre: String,
    val director: String,
    val episode_duration: Int,
    val seasons: Int,
    val episodes: Int
) : Medium*/

