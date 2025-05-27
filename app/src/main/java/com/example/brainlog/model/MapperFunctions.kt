package com.example.brainlog.model
import com.example.brainlog.viewmodel.dto.MovieDetailDto
import com.example.brainlog.viewmodel.dto.SearchMovieDto
import com.example.brainlog.viewmodel.dto.SearchSeriesDto
import com.example.brainlog.viewmodel.dto.TvShowDto

// In deiner Datei mit den DTO-Mapping-Funktionen (z.B. ConcreteMedia.kt oder eine Mapper-Datei)
// Stelle sicher, dass MediumType importiert ist: import com.example.brainlog.model.MediumType

fun MovieDetailDto.getDetailsDomainModel(): Movie { // Movie ist deine Domänenklasse
        val provider = "TMDB"
        val typeEnum = MediumType.MOVIE // Der korrekte Enum-Wert

        return Movie(
                globalID = "${provider}_${typeEnum.name}_${this.id}", // KORREKTES FORMAT: z.B. "TMDB_MOVIE_124905"
                id = this.id,
                apiProvider = provider,
                title = this.title,
                description = this.overview ?: "Keine Beschreibung verfügbar.",
                genres = this.genres.mapNotNull { it.name },
                releaseDate = this.release_date ?: "Unbekannt",
                type = typeEnum, // Enum-Wert hier verwenden
                isFinished = false,

                // Movie-spezifische Felder
                runtime = this.runtime,
                posterUrl = this.poster_path,
                backdropUrl = this.backdrop_path,
                rating = this.vote_average,
                votes = this.vote_count,
                tagline = this.tagline
        )
}

// In deiner Datei mit den DTO-Mapping-Funktionen
// Stelle sicher, dass MediumType importiert ist

fun TvShowDto.getDetailsDomainModel(): Series { // Series ist deine Domänenklasse
        val provider = "TMDB"
        val typeEnum = MediumType.SERIES // Der korrekte Enum-Wert

        return Series(
                globalID = "${provider}_${typeEnum.name}_${this.id}", // KORREKTES FORMAT: z.B. "TMDB_SERIES_67890"
                id = this.id,
                apiProvider = provider,
                title = this.name,
                description = this.overview ?: "Keine Beschreibung verfügbar.",
                genres = this.genres.mapNotNull { it.name },
                releaseDate = this.first_air_date ?: "Unbekannt",
                type = typeEnum, // Enum-Wert hier verwenden
                isFinished = false,

                // Series-spezifische Felder
                originalName = this.original_name,
                posterUrl = this.poster_path,
                lastAirDate = this.last_air_date,
                numberOfSeasons = this.number_of_seasons,
                numberOfEpisodes = this.number_of_episodes,
                country = this.origin_country,
                originalLanguage = this.original_language,
                popularity = this.popularity,
                voteAverage = this.vote_average,
                voteCount = this.vote_count
        )
}


// In der Datei, wo deine Mappings sind (z.B. MappingExtensions.kt oder am Ende von MediaRepository.kt)
// Stelle sicher, dass alle nötigen Klassen importiert sind (Game, MediumType, RawgGameDetailDto etc.)

fun RawgGameDetailDto.toDomainModel(): com.example.brainlog.model.Game {
        val provider = "RAWG"
        val mediaTypeEnum = com.example.brainlog.model.MediumType.GAME // Sicherstellen, dass der Enum-Typ verwendet wird

        return com.example.brainlog.model.Game(
                globalID = "${provider}_${mediaTypeEnum.name}_${this.id}", // KORREKTES FORMAT! z.B. "RAWG_GAME_123"
                id = this.id,
                apiProvider = provider,
                title = this.name,
                description = this.descriptionRaw ?: this.description ?: "Keine Beschreibung verfügbar.",
                genres = this.genres?.mapNotNull { it.name } ?: emptyList(),
                releaseDate = this.released ?: "Unbekannt",
                type = mediaTypeEnum, // Das ist der Enum-Wert
                isFinished = false,
                posterUrl = this.backgroundImage,
                website = this.website,
                metacriticScore = this.metacritic,
                esrbRatingName = this.esrbRating?.name,
                developers = this.developers?.mapNotNull { it.name } ?: emptyList(),
                publishers = this.publishers?.mapNotNull { it.name } ?: emptyList(),
                playtime = this.playtime
        )
}