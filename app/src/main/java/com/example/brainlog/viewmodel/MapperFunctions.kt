package com.example.brainlog.viewmodel

fun FilmDto.toDomainModel(): Movie = Movie(
    id = id,
    title = title,
    year = release_date,
    duration = duration,
    genre = genre,
    description = overview,
    director = director,
)

fun SerieDto.toDomainModel(): Series = Series(
    id = id,
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
    id = id,
    title = title,
    year = year,
    description = description,
    genre = genre,
    author = author,
    pages = pages
)

fun SearchMovieDto.toSearchDomainModel(): SearchMovie = SearchMovie(
    id = id,
    original_title = original_title,
    release_date = release_date,
    poster_path = poster_path
)