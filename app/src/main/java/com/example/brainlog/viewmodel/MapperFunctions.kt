package com.example.brainlog.viewmodel

fun MovieDetailDto.getDetailsDomainModel(): Movie = Movie(
        id = id,
        title = title,
        description = overview ?: "",
        genres = genres.mapNotNull { it.name },
        releaseDate = release_date ?: "",
        runtime = runtime,
        posterUrl = poster_path,
        backdropUrl = backdrop_path,
        rating = vote_average,
        votes = vote_count,
        tagline = tagline
    )



/*fun SerieDto.toDomainModel(): Series = Series(
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
)*/

fun SearchMovieDto.toSearchDomainModel(): SearchMedium.SearchMovie = SearchMedium.SearchMovie(
    id = id,
    original_title = original_title,
    release_date = release_date,
    poster_path = poster_path
)

fun SearchSeriesDto.toSearchDomainModel(): SearchMedium.SearchSeries = SearchMedium.SearchSeries(
    id = id,
    original_title = original_name,
    release_date = first_air_date,
    poster_path = poster_path
)