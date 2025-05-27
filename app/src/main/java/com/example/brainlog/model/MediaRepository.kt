package com.example.brainlog.model // Oder dein passendes Package

import android.util.Log
import com.example.brainlog.viewmodel.dto.SearchMovieDto // Stelle sicher, dass der Import korrekt ist
import com.example.brainlog.viewmodel.dto.SearchSeriesDto // Stelle sicher, dass der Import korrekt ist
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

// MediaAPI ist dein bestehendes Interface für TMDB
// RawgApiService ist dein neues Interface für RAWG

class MediaRepository(
    private val tmdbApi: MediaAPI,
    private val rawgApi: RawgApiService
) {
    // Die getGameDetails, getMovieDetails, getSeriesDetails Methoden bleiben wie sie sind...
    suspend fun getGameDetails(gameId: Int): Medium {
        Log.d("MediaRepo_Detail", "GAME_DETAIL: Attempting to fetch details for gameId: $gameId")
        try {
            val gameDetailDto = rawgApi.getGameDetails(gameId)
            Log.i("MediaRepo_Detail", "GAME_DETAIL: DTO received for gameId $gameId. DTO Name: ${gameDetailDto.name}")
            val domainModel = gameDetailDto.toDomainModel()
            Log.i("MediaRepo_Detail", "GAME_DETAIL: Mapped to domain model for gameId $gameId. Domain Title: ${domainModel.title}, GlobalID: ${domainModel.globalID}")
            return domainModel
        } catch (e: Exception) {
            Log.e("MediaRepo_Detail", "GAME_DETAIL: Error fetching/mapping details for gameId $gameId: ${e.message}", e)
            throw e
        }
    }
    suspend fun getMovieDetails(movieId: Int): com.example.brainlog.model.Movie {
        Log.d("MediaRepo_Detail", "MOVIE_DETAIL: Fetching details for movieId: $movieId")
        try {
            val movieDto = tmdbApi.getMovieDetails(movieId)
            Log.i("MediaRepo_Detail", "MOVIE_DETAIL: DTO received for movieId $movieId. DTO Title: ${movieDto.title}")
            val domainModel = movieDto.getDetailsDomainModel()
            Log.i("MediaRepo_Detail", "MOVIE_DETAIL: Mapped to domain model for movieId $movieId. Domain Title: ${domainModel.title}, GlobalID: ${domainModel.globalID}")
            return domainModel
        } catch (e: Exception) {
            Log.e("MediaRepo_Detail", "MOVIE_DETAIL: Error fetching/mapping details for movieId $movieId: ${e.message}", e)
            throw e
        }
    }

    suspend fun getSeriesDetails(seriesId: Int): com.example.brainlog.model.Series {
        Log.d("MediaRepo_Detail", "SERIES_DETAIL: Fetching details for seriesId: $seriesId")
        try {
            val seriesDto = tmdbApi.getSeriesDetails(seriesId)
            Log.i("MediaRepo_Detail", "SERIES_DETAIL: DTO received for seriesId $seriesId. DTO Name: ${seriesDto.name}")
            val domainModel = seriesDto.getDetailsDomainModel()
            Log.i("MediaRepo_Detail", "SERIES_DETAIL: Mapped to domain model for seriesId $seriesId. Domain Title: ${domainModel.title}, GlobalID: ${domainModel.globalID}")
            return domainModel
        } catch (e: Exception) {
            Log.e("MediaRepo_Detail", "SERIES_DETAIL: Error fetching/mapping details for seriesId $seriesId: ${e.message}", e)
            throw e
        }
    }


    // ERSETZE deine searchAllMedia-Funktion mit dieser searchMediaByType-Funktion:
    suspend fun searchMediaByType(query: String, searchType: MediumType): List<SearchMedium> {
        Log.d("MediaRepository", "searchMediaByType called with query: '$query', type: $searchType")

        if (searchType == MediumType.BOOK) { // Direkt am Anfang abfangen
            Log.d("MediaRepository", "Search for BOOK type, returning empty list.")
            return emptyList()
        }

        return coroutineScope {
            when (searchType) {
                MediumType.MOVIE -> {
                    Log.d("MediaRepository", "Searching only for MOVIEs on TMDB.")
                    searchTmdbMovies(query) // Gibt List<SearchMedium.Movie>
                }
                MediumType.SERIES -> {
                    Log.d("MediaRepository", "Searching only for SERIES on TMDB.")
                    searchTmdbSeries(query) // Gibt List<SearchMedium.Series>
                }
                MediumType.GAME -> {
                    Log.d("MediaRepository", "Searching only for GAMEs on RAWG.")
                    searchRawgGames(query) // Gibt List<SearchMedium.Game>
                }
                // Optional: Ein "ALL" Fall, wenn du einen "Alles"-Filterchip hinzufügst
                // MediumType.ALL (oder ein neuer Enum-Wert) -> {
                //     val moviesDeferred = async { searchTmdbMovies(query) }
                //     val seriesDeferred = async { searchTmdbSeries(query) }
                //     val gamesDeferred = async { searchRawgGames(query) }
                //     moviesDeferred.await() + seriesDeferred.await() + gamesDeferred.await()
                // }
                MediumType.UNKNOWN -> { // Fallback für UNKNOWN
                    Log.w("MediaRepository", "searchMediaByType called with UNKNOWN type for query '$query', attempting all sources.")
                    // Hier könntest du entscheiden, alle Quellen zu durchsuchen oder leer zurückzugeben
                    val moviesDeferred = async { searchTmdbMovies(query) }
                    val seriesDeferred = async { searchTmdbSeries(query) }
                    val gamesDeferred = async { searchRawgGames(query) }
                    moviesDeferred.await() + seriesDeferred.await() + gamesDeferred.await()
                }
                else -> { // Sollte nicht passieren, wenn alle Typen oben abgedeckt sind
                    Log.w("MediaRepository", "searchMediaByType called with unhandled type: $searchType for query '$query', returning empty list.")
                    emptyList()
                }
            }
        }
    }

    // Deine privaten Hilfsfunktionen searchTmdbMovies, searchTmdbSeries, searchRawgGames
    // bleiben exakt so, wie sie sind (mit dem Logging, das wir hinzugefügt haben).
    // Sie geben ja bereits List<SearchMedium.Movie>, List<SearchMedium.Series> und List<SearchMedium.Game> zurück.

    private suspend fun searchTmdbMovies(query: String): List<SearchMedium.Movie> {
        Log.d("MediaRepository", "searchTmdbMovies: Searching movies for '$query'")
        try {
            val response = tmdbApi.searchFilms(query)
            val movies = response.results.map { dto ->
                SearchMedium.Movie(
                    tmdbId = dto.id,
                    title = dto.title,
                    posterPath = dto.poster_path,
                    releaseDate = dto.release_date
                )
            }
            Log.d("MediaRepository", "searchTmdbMovies: Found ${movies.size} movies for '$query'")
            return movies
        } catch (e: Exception) {
            Log.e("MediaRepository", "searchTmdbMovies: Error for '$query': ${e.message}", e)
            return emptyList()
        }
    }

    private suspend fun searchTmdbSeries(query: String): List<SearchMedium.Series> {
        Log.d("MediaRepository", "searchTmdbSeries: Searching series for '$query'")
        try {
            val response = tmdbApi.searchSeries(query)
            val series = response.results.map { dto ->
                SearchMedium.Series(
                    tmdbId = dto.id,
                    name = dto.name,
                    posterPath = dto.poster_path,
                    firstAirDate = dto.first_air_date
                )
            }
            Log.d("MediaRepository", "searchTmdbSeries: Found ${series.size} series for '$query'")
            return series
        } catch (e: Exception) {
            Log.e("MediaRepository", "searchTmdbSeries: Error for '$query': ${e.message}", e)
            return emptyList()
        }
    }

    private suspend fun searchRawgGames(query: String): List<SearchMedium.Game> {
        Log.d("MediaRepo", "RAWG_SEARCH: searchRawgGames called with query: '$query'")
        try {
            Log.d("MediaRepo", "RAWG_SEARCH: Calling rawgApi.searchGames...")
            val response = rawgApi.searchGames(query = query)
            Log.d("MediaRepo", "RAWG_SEARCH: API response received. Count: ${response.count}, Results in DTO: ${response.results.size}")
            if (response.results.isEmpty() && response.count > 0) {
                Log.w("MediaRepo", "RAWG_SEARCH: API reported ${response.count} items, but results list in DTO is empty.")
            }
            if (response.results.isNotEmpty()){
                Log.d("MediaRepo", "RAWG_SEARCH: First game DTO from API: ${response.results[0]}")
            }
            val mappedGames = response.results.map { rawgGameDto ->
                Log.d("MediaRepo", "RAWG_SEARCH: Mapping DTO: ID=${rawgGameDto.id}, Name='${rawgGameDto.name}'")
                SearchMedium.Game(
                    rawgId = rawgGameDto.id,
                    name = rawgGameDto.name,
                    released = rawgGameDto.released,
                    backgroundImage = rawgGameDto.backgroundImage,
                    platforms = rawgGameDto.platforms?.mapNotNull { it.platform?.name }
                ).also { Log.d("MediaRepo", "RAWG_SEARCH: Mapped to SearchMedium.Game: '${it.displayTitle}'") }
            }
            Log.d("MediaRepo", "RAWG_SEARCH: Successfully mapped ${mappedGames.size} games.")
            return mappedGames
        } catch (e: Exception) {
            Log.e("MediaRepo", "RAWG_SEARCH: Fehler beim Laden/Mappen der Spiele für '$query': ${e.message}", e)
            return emptyList()
        }
    }
}
