package com.example.brainlog.viewmodel

data class Person(
    val id: Int,
    val username: String,
    val email: String,
    val library: MutableList<Medium> = mutableListOf() // Die Bibliothek des Benutzers
)