package com.example.brainlog.viewmodel.dto

import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
data class Genre(
    val id: Int,
    val name: String
)
@JsonClass(generateAdapter = true)
data class ProductionCompany(
    val id: Int,
    val logo_path: String?,
    val name: String,
    val origin_country: String
)
@JsonClass(generateAdapter = true)
data class ProductionCountry(
    val iso_3166_1: String,
    val name: String
)
@JsonClass(generateAdapter = true)
data class SpokenLanguage(
    val english_name: String,
    val iso_639_1: String,
    val name: String
)