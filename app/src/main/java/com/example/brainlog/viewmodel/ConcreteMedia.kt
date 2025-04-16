package com.example.brainlog.viewmodel

data class Book(
    override val title: String,
    override val year: Int,
    override val description: String,
    override val genre: String,
    val pages: Int,
    val author: String
) : Medium


data class Movie(
    override val title: String,
    override val year: Int,
    override val description: String,
    override val genre: String,
    val director: String,
    val duration: Int,
) : Medium


data class Series (
    override val title: String,
    override val year: Int,
    override val description: String,
    override val genre: String,
    val director: String,
    val episode_duration: Int,
    val seasons: Int,
    val episodes: Int
) : Medium