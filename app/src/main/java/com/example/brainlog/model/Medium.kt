package com.example.brainlog.model

interface Medium {
    val globalID: String
    val id: Int
    val apiProvider: String
    val title: String
    val description: String
    val genres: List<String>
    val releaseDate: String
    val type: MediumType
    var isFinished: Boolean
}
