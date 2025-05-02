package com.example.brainlog.model

interface Medium {
    val id: Int
    val title: String
    val description: String
    val genres: List<String>
    val releaseDate: String
}
