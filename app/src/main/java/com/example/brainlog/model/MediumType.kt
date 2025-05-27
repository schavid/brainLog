package com.example.brainlog.model // Oder dein passendes Package

// Dein MediumType Enum (dieser ist okay so)
enum class MediumType {
    MOVIE,
    SERIES, // Ich verwende SERIES, um konsistent zu sein; passe es an, falls du TV_SERIES bevorzugst
    GAME,
    UNKNOWN,
    BOOK
}

// Dies ist die korrigierte SearchMedium-Hierarchie für Suchergebnisse
sealed class SearchMedium {
    abstract val id: Int
    abstract val displayTitle: String
    abstract val displayReleaseDate: String?
    abstract val displayPosterUrl: String?
    abstract val displayMediaType: MediumType
    abstract val apiProvider: String // "TMDB" oder "RAWG"

    data class Movie(
        val tmdbId: Int,          // ID von TMDB
        val title: String,        // Titel von TMDB (z.B. dto.title)
        val posterPath: String?,  // poster_path von TMDB DTO
        val releaseDate: String?  // release_date von TMDB DTO
    ) : SearchMedium() {
        override val id: Int get() = tmdbId
        override val displayTitle: String get() = title
        override val displayReleaseDate: String? get() = releaseDate
        override val displayPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
        override val displayMediaType: MediumType = MediumType.MOVIE
        override val apiProvider: String = "TMDB"
    }

    data class Series(
        val tmdbId: Int,          // ID von TMDB
        val name: String,         // Name der Serie von TMDB (z.B. dto.name)
        val posterPath: String?,  // poster_path von TMDB DTO
        val firstAirDate: String? // first_air_date von TMDB DTO
    ) : SearchMedium() {
        override val id: Int get() = tmdbId
        override val displayTitle: String get() = name
        override val displayReleaseDate: String? get() = firstAirDate
        override val displayPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
        override val displayMediaType: MediumType = MediumType.SERIES
        override val apiProvider: String = "TMDB"
    }

    data class Game(
        val rawgId: Int,
        val name: String,
        val released: String?,
        val backgroundImage: String?,
        val platforms: List<String>? // <--- FÜGE DIESES FELD HINZU
        // Optional: Wenn du auch Genres in der Suchliste anzeigen willst:
        // val gameGenres: List<String>?
    ) : SearchMedium() {
        override val id: Int get() = rawgId
        override val displayTitle: String get() = name
        override val displayReleaseDate: String? get() = released
        override val displayPosterUrl: String? get() = backgroundImage
        override val displayMediaType: MediumType = MediumType.GAME
        override val apiProvider: String = "RAWG"
    }
}