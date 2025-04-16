package com.example.brainlog.viewmodel

import retrofit2.http.GET
import retrofit2.http.Query

interface MediaAPI {
    // Suchendpunkte
    @GET("films/search")
    suspend fun searchFilms(@Query("query") query: String): List<FilmDto>

    @GET("series/search")
    suspend fun searchSeries(@Query("query") query: String): List<SerieDto>

    @GET("books/search")
    suspend fun searchBooks(@Query("query") query: String): List<BuchDto>

    // Optional: Alle abrufen
    @GET("films")
    suspend fun getAllFilms(): List<FilmDto>

    @GET("series")
    suspend fun getAllSeries(): List<SerieDto>

    @GET("books")
    suspend fun getAllBooks(): List<BuchDto>

}