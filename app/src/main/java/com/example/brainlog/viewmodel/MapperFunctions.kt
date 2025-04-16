package com.example.brainlog.viewmodel

fun FilmDto.toDomainModel(): Movie = Movie(
    title = title,
    year = year,
    duration = duration,
    genre = genre,
    description = description,
    director = director,
)

fun SerieDto.toDomainModel(): Series = Series(
    title = title,
    year = year,
    description = description,
    genre = genre,
    seasons = seasons,
    episodes = episodes,
    director = director,
    episode_duration = episode_duration,
)

fun BuchDto.toDomainModel(): Book = Book(
    title = title,
    year = year,
    description = description,
    genre = genre,
    author = author,
    pages = pages
)