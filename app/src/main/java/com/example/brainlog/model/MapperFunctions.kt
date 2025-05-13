package com.example.brainlog.model
import com.example.brainlog.viewmodel.dto.MovieDetailDto
import com.example.brainlog.viewmodel.dto.SearchMovieDto
import com.example.brainlog.viewmodel.dto.SearchSeriesDto
import com.example.brainlog.viewmodel.dto.TvShowDto

fun MovieDetailDto.getDetailsDomainModel(): Movie = Movie(
        globalID = "$apiProvider/_$type/_$id",
        apiProvider = "TMDB",
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
        tagline = tagline,
        type = "Movie"
    )



fun TvShowDto.getDetailsDomainModel(): Series = Series(
        id = id,
        title = name,
        originalName = original_name,
        description = overview,
        posterUrl = poster_path,
        releaseDate = first_air_date.orEmpty(),
        lastAirDate = last_air_date,
        numberOfSeasons = number_of_seasons,
        numberOfEpisodes = number_of_episodes,
        genres = genres.mapNotNull { it.name },
        country = origin_country,
        originalLanguage = original_language,
        popularity = popularity,
        voteAverage = vote_average,
        voteCount = vote_count,
        type = "Series"
    )




fun SearchMovieDto.toSearchDomainModel(): SearchMedium.SearchMovie = SearchMedium.SearchMovie(
    id = id,
    original_title = original_title,
    release_date = release_date,
    poster_path = poster_path,
    type = MediumType.MOVIE
)

fun SearchSeriesDto.toSearchDomainModel(): SearchMedium.SearchSeries = SearchMedium.SearchSeries(
    id = id,
    original_title = original_name,
    release_date = first_air_date,
    poster_path = poster_path,
    type = MediumType.SERIES
)