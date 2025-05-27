package com.example.brainlog.model // Oder dein passendes Package

import android.util.Log
import com.example.brainlog.viewmodel.dto.SearchMovieDto // Stelle sicher, dass der Import korrekt ist
import com.example.brainlog.viewmodel.dto.SearchSeriesDto // Stelle sicher, dass der Import korrekt ist
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

// MediaAPI ist dein bestehendes Interface für TMDB
// RawgApiService ist dein neues Interface für RAWG

class MediaRepository(
    private val tmdbApi: MediaAPI,         // Dein TMDB API Interface
    private val rawgApi: RawgApiService    // Dein RAWG API Interface
) {
    // In MediaRepository.kt
// import android.util.Log // Sicherstellen, dass dieser Import vorhanden ist

    suspend fun getGameDetails(gameId: Int): Medium { // Oder besser: Game?
        Log.d("MediaRepo_Detail", "GAME_DETAIL: Attempting to fetch details for gameId: $gameId") // LOG X1
        try {
            val gameDetailDto = rawgApi.getGameDetails(gameId)
            Log.i("MediaRepo_Detail", "GAME_DETAIL: DTO received for gameId $gameId. DTO Name: ${gameDetailDto.name}") // LOG X2
            // Logge hier ggf. weitere interessante Felder des DTOs, z.B. gameDetailDto.toString() (kann lang sein)

            val domainModel = gameDetailDto.toDomainModel() // Annahme: toDomainModel() gibt Game zurück
            Log.i("MediaRepo_Detail", "GAME_DETAIL: Mapped to domain model for gameId $gameId. Domain Title: ${domainModel.title}, GlobalID: ${domainModel.globalID}") // LOG X3
            return domainModel
        } catch (e: Exception) {
            Log.e("MediaRepo_Detail", "GAME_DETAIL: Error fetching/mapping details for gameId $gameId: ${e.message}", e) // LOG X4
            throw e // Wichtig: Wirf die Exception weiter, damit sie im ViewModel (LOG J) gefangen werden kann
        }
    }
// Füge ähnliches, detailliertes Logging auch zu getMovieDetails und getSeriesDetails hinzu!

    suspend fun searchAllMedia(query: String): List<SearchMedium> = coroutineScope {
        // Parallele Aufrufe an TMDB (Filme & Serien) und RAWG (Spiele)
        val moviesDeferred = async { searchTmdbMovies(query) }
        val seriesDeferred = async { searchTmdbSeries(query) }
        val gamesDeferred = async { searchRawgGames(query) }

        val movies = moviesDeferred.await()
        val series = seriesDeferred.await()
        val games = gamesDeferred.await()

        // Kombiniere und gib die Ergebnisse zurück (du kannst die Reihenfolge anpassen)
        return@coroutineScope movies + series + games
    }


    // In MediaRepository.kt

    private suspend fun searchTmdbMovies(query: String): List<SearchMedium.Movie> {
        try {
            val response = tmdbApi.searchFilms(query) // tmdbApi.searchFilms gibt SearchMovieResponse (mit List<SearchMovieDto>)
            return response.results.map { dto -> // dto ist hier vom Typ SearchMovieDto
                SearchMedium.Movie( // Erstelle das korrekte SearchMedium.Movie
                    tmdbId = dto.id,
                    title = dto.title, // oder dto.original_title, je nach Präferenz für die Anzeige
                    posterPath = dto.poster_path,
                    releaseDate = dto.release_date
                )
            }
        } catch (e: Exception) {
            println("Fehler beim Laden der Filme für '$query' von TMDB: ${e.message}")
            return emptyList()
        }
    }

    private suspend fun searchTmdbSeries(query: String): List<SearchMedium.Series> {
        try {
            val response = tmdbApi.searchSeries(query) // tmdbApi.searchSeries gibt SearchSeriesResponse (mit List<SearchSeriesDto>)
            return response.results.map { dto -> // dto ist hier vom Typ SearchSeriesDto
                SearchMedium.Series( // Erstelle das korrekte SearchMedium.Series
                    tmdbId = dto.id,
                    name = dto.name, // oder dto.original_name
                    posterPath = dto.poster_path,
                    firstAirDate = dto.first_air_date
                )
            }
        } catch (e: Exception) {
            println("Fehler beim Laden der Serien für '$query' von TMDB: ${e.message}")
            return emptyList()
        }
    }

    // searchRawgGames sollte bereits SearchMedium.Game erstellen und ist wahrscheinlich okay,
// aber stelle sicher, dass es die oben definierte SearchMedium.Game-Struktur verwendet.
    // In MediaRepository.kt

    private suspend fun searchRawgGames(query: String): List<SearchMedium.Game> {
        Log.d("MediaRepo", "RAWG_SEARCH: searchRawgGames called with query: '$query'") // Log 1
        try {
            Log.d("MediaRepo", "RAWG_SEARCH: Calling rawgApi.searchGames...") // Log 2
            val response = rawgApi.searchGames(query = query) // Ruft RawgApiService auf
            Log.d("MediaRepo", "RAWG_SEARCH: API response received. Count: ${response.count}, Results in DTO: ${response.results.size}") // Log 3

            if (response.results.isEmpty() && response.count > 0) {
                Log.w("MediaRepo", "RAWG_SEARCH: API reported ${response.count} items, but results list in DTO is empty. Check RawgSearchResponse and RawgGame DTOs against Postman JSON!")
            }
            if (response.results.isNotEmpty()) {
                Log.d("MediaRepo", "RAWG_SEARCH: First game DTO from API: ${response.results[0]}") // Log 4 (zeigt das erste DTO)
            }

            val mappedGames = response.results.map { rawgGameDto ->
                Log.d("MediaRepo", "RAWG_SEARCH: Mapping DTO: ID=${rawgGameDto.id}, Name='${rawgGameDto.name}'") // Log 5
                SearchMedium.Game(
                    rawgId = rawgGameDto.id,
                    name = rawgGameDto.name,
                    released = rawgGameDto.released,
                    backgroundImage = rawgGameDto.backgroundImage,
                    platforms = rawgGameDto.platforms?.mapNotNull { it.platform?.name }
                ).also {
                    Log.d("MediaRepo", "RAWG_SEARCH: Mapped to SearchMedium.Game: '${it.displayTitle}'") // Log 6
                }
            }
            Log.d("MediaRepo", "RAWG_SEARCH: Successfully mapped ${mappedGames.size} games.") // Log 7
            return mappedGames
        } catch (e: Exception) {
            // Logge den Fehler und den Stack Trace!
            Log.e("MediaRepo", "RAWG_SEARCH: Fehler beim Laden/Mappen der Spiele für '$query': ${e.message}", e) // Log 8 (mit Exception)
            return emptyList()
        }
    }

    // Deine bestehenden Methoden für Detailansichten bleiben vorerst unverändert.
    // Wenn du auch Spieldetails von RAWG abrufen möchtest, können wir das später hinzufügen.
    suspend fun getMovieDetails(movieId: Int): com.example.brainlog.model.Movie { // Annahme: Movie ist dein Domain Model
        val movieDto = tmdbApi.getMovieDetails(movieId)
        return movieDto.getDetailsDomainModel() // Annahme: diese Methode existiert auf MovieDetailDto
    }

    suspend fun getSeriesDetails(seriesId: Int): com.example.brainlog.model.Series { // Annahme: Series ist dein Domain Model
        val seriesDto = tmdbApi.getSeriesDetails(seriesId)
        return seriesDto.getDetailsDomainModel() // Annahme: diese Methode existiert auf TvShowDto
    }
}

