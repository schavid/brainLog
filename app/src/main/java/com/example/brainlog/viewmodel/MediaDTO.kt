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

/*SuchresultsDTO's*/

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
    val vote_count: Int
)

data class SearchMovieResponse(
    val page: Int,
    val results: List<SearchMovieDto>,
    val totalPages: Int,
    val totalResults: Int
)

data class SearchSeriesResponse(
    val page: Int,
    val results: List<SearchSeriesDto>,
    val totalPages: Int,
    val totalResults: Int
)

/*MovieDetailDto's*/

data class MovieDetailDto(
    val adult: Boolean,
    val backdrop_path: String?,
    val belongs_to_collection: BelongsToCollection?, // falls später gebraucht
    val budget: Int,
    val genres: List<Genre>,
    val homepage: String?,
    val id: Int,
    val imdb_id: String?,
    val origin_country: List<String>?, // Achtung: war in deinem JSON kein offizielles Feld!
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val production_companies: List<ProductionCompany>,
    val production_countries: List<ProductionCountry>,
    val release_date: String,
    val revenue: Int,
    val runtime: Int,
    val spoken_languages: List<SpokenLanguage>,
    val status: String,
    val tagline: String?,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)

data class Genre(
    val id: Int,
    val name: String
)

data class ProductionCompany(
    val id: Int,
    val logo_path: String?,
    val name: String,
    val origin_country: String
)

data class ProductionCountry(
    val iso_3166_1: String,
    val name: String
)

data class SpokenLanguage(
    val english_name: String,
    val iso_639_1: String,
    val name: String
)

data class BelongsToCollection(
    val id: Int,
    val name: String,
    val poster_path: String?,
    val backdrop_path: String?
)
