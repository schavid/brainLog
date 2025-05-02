package com.example.brainlog.viewmodel

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Movie(
    @DocumentId
    var firestoreId: String? = null,

    override val id: Int = 0,
    override val title: String = "",
    override val description: String = "",
    override val genres: List<String> = emptyList(),
    override val releaseDate: String = "",

    val runtime: Int? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val rating: Double = 0.0,
    val votes: Int = 0,
    val tagline: String? = null,
) : Medium


@IgnoreExtraProperties
data class Series(
    @DocumentId
    var firestoreId: String? = null,

    override val id: Int = 0,
    override val title: String = "",
    override val description: String = "",
    override val genres: List<String> = emptyList(),
    override val releaseDate: String = "",

    val originalName: String = "",
    val posterUrl: String? = null,
    val lastAirDate: String? = null,
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val country: List<String> = emptyList(),
    val originalLanguage: String = "",
    val popularity: Double = 0.0,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
) : Medium

