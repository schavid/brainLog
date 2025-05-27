package com.example.brainlog.viewmodel.dto

import com.example.brainlog.model.MediumType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass // << IMPORT HINZUFÜGEN

@JsonClass(generateAdapter = true) // << HIER HINZUFÜGEN
data class SearchMovieDto(
    val id: Int,
    val original_title: String,
    val title: String,
    val overview: String, // Sollte overview: String? sein, da es fehlen kann
    val release_date: String, // Sollte release_date: String? sein
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double,
    val vote_count: Int,
    val popularity: Double,
    val original_language: String,
    val genre_ids: List<Int>,
    val adult: Boolean,
    val video: Boolean
    // Das Feld "type: MediumType" ist hier wahrscheinlich nicht nötig,
    // da TMDB diesen Typ nicht direkt so liefert. Der Typ wird später beim Mapping
    // zu SearchMedium.Movie zugewiesen. Überlege, ob du es hier wirklich brauchst.
    // Wenn du es behältst und TMDB es nicht liefert, könnte es Parsing-Probleme geben,
    // es sei denn, du definierst einen Default-Wert oder machst es nullable.
    // Für die reine DTO-Struktur von TMDB würde ich es eher weglassen.
    // val type: MediumType // Vorerst auskommentiert als Denkanstoß
)

@JsonClass(generateAdapter = true) // << HIER HINZUFÜGEN
data class SearchSeriesDto(
    val adult: Boolean,
    val backdrop_path: String?,
    val genre_ids: List<Int>,
    val id: Int,
    val origin_country: List<String>,
    val original_language: String,
    val original_name: String,
    val overview: String, // Sollte overview: String? sein
    val popularity: Double,
    val poster_path: String?,
    val first_air_date: String, // Sollte first_air_date: String? sein
    val name: String,
    val vote_average: Double,
    val vote_count: Int
    // Das Feld "type: MediumType" ist hier wahrscheinlich auch nicht direkt von TMDB.
    // val type: MediumType // Vorerst auskommentiert als Denkanstoß
)

@JsonClass(generateAdapter = true) // << HIER HINZUFÜGEN
data class SearchSeriesResponse(
    val page: Int,
    val results: List<SearchSeriesDto>, // Wichtig: SearchSeriesDto muss auch annotiert sein (haben wir gemacht)
    @Json(name = "total_pages") val totalPages: Int, // TMDB verwendet oft snake_case für JSON-Felder
    @Json(name = "total_results") val totalResults: Int // @Json-Annotation für abweichende Namen
)

@JsonClass(generateAdapter = true) // << HIER HINZUFÜGEN
data class SearchMovieResponse(
    val page: Int,
    val results: List<SearchMovieDto>, // Wichtig: SearchMovieDto muss auch annotiert sein (haben wir gemacht)
    @Json(name = "total_pages") val totalPages: Int, // TMDB verwendet oft snake_case für JSON-Felder
    @Json(name = "total_results") val totalResults: Int // @Json-Annotation für abweichende Namen
)