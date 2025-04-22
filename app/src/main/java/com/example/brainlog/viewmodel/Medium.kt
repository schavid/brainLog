package com.example.brainlog.viewmodel

interface Medium {
    val id: Int
    val title: String
    val description: String
    val genres: List<String>
    val releaseDate: String
}
