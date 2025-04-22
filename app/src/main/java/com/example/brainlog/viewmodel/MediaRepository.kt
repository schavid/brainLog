package com.example.brainlog.viewmodel
import com.example.brainlog.viewmodel.toSearchDomainModel


class MediaRepository(private val api: MediaAPI) {
    /*// 🔍 Suche nach festgelegtem Typ (z. B. Film)
    suspend fun searchByType(type: MediumType, query: String): List<Medium> {
        return when (type) {
            MediumType.MOVIE -> api.searchFilms(query).map { it.toDomainModel() }
            MediumType.SERIES -> api.searchSeries(query).map { it.toDomainModel() }
            MediumType.BOOK -> api.searchBooks(query).map { it.toDomainModel() }
        }
    }

    // 🌐 Optionale Gesamtsuche über alle Typen
    suspend fun searchAllMedia(query: String): List<Medium> {
        val films = api.searchFilms(query).map { it.toDomainModel() }
        val series = api.searchSeries(query).map { it.toDomainModel() }
        val books = api.searchBooks(query).map { it.toDomainModel() }
        return films + series + books
    }*/

    //nur mal zum testen
    suspend fun searchAllFilms(query: String): List<SearchMedium> {
        val films = api.searchFilms(query).results.map { it.toSearchDomainModel() }
        val series = api.searchSeries(query).results.map { it.toSearchDomainModel() }
        return films + series
    }

}