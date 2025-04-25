package com.example.brainlog.viewmodel


sealed class SearchMedium {
    abstract val id: Int
    abstract val original_title: String
    abstract val release_date: String
    abstract val type: MediumType

    data class SearchMovie(
        override val id: Int,
        override val original_title: String,
        override val release_date: String,
        val poster_path: String?,
        override val type: MediumType
    ) : SearchMedium()

    data class SearchSeries(
        override val id: Int,
        override val original_title: String,
        override val release_date: String,
        val poster_path: String?,
        override val type: MediumType
    ) : SearchMedium()
}
