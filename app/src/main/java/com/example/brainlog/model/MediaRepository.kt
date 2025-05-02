package com.example.brainlog.model

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class MediaRepository(private val api: MediaAPI) {

    suspend fun searchAllFilms(query: String): List<SearchMedium> {

        return coroutineScope {
            val filmsDeferred = async {
                try {
                    api.searchFilms(query).results.map { it.toSearchDomainModel() }
                } catch (e: Exception) {
                    println("Fehler beim Laden der Filme für '$query': ${e.message}")
                    emptyList<SearchMedium>()
                }
            }

            val seriesDeferred = async {
                try {
                    api.searchSeries(query).results.map { it.toSearchDomainModel() }
                } catch (e: Exception) {
                    println("Fehler beim Laden der Serien für '$query': ${e.message}")
                    emptyList<SearchMedium>()
                }
            }

            val films = filmsDeferred.await()
            val series = seriesDeferred.await()

            films + series
        }
    }

    suspend fun getMovieDetails(movieId: Int): Movie {
        val movieDto = api.getMovieDetails(movieId)  // API-Call für Film
        return movieDto.getDetailsDomainModel()  // Mapping auf das Domain-Modell
    }

    suspend fun getSeriesDetails(seriesId: Int): Series {
        val seriesDto = api.getSeriesDetails(seriesId)  // API-Call für Serie
        return seriesDto.getDetailsDomainModel()  // Mapping auf das Domain-Modell
    }


}