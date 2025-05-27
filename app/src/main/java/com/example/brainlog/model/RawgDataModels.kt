package com.example.brainlog.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawgSearchResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<RawgGame>
)

@JsonClass(generateAdapter = true)
data class RawgGame(
    val id: Int,
    val slug: String?,
    val name: String,
    val released: String?, // z.B. "2023-05-20"
    @Json(name = "background_image") val backgroundImage: String?,
    val rating: Double?,
    val metacritic: Int?,
    val platforms: List<RawgPlatformContainer>?,
    val genres: List<RawgGenre>?
    // Füge weitere Felder hinzu, falls benötigt
)

@JsonClass(generateAdapter = true)
data class RawgPlatformContainer(
    val platform: RawgPlatformDetails
)

@JsonClass(generateAdapter = true)
data class RawgPlatformDetails(
    val id: Int,
    val name: String,
    val slug: String?
)

@JsonClass(generateAdapter = true)
data class RawgGenre(
    val id: Int,
    val name: String,
    val slug: String?
)
@JsonClass(generateAdapter = true)
data class RawgGameDetailDto(
    val id: Int,
    val slug: String?,
    val name: String,
    @Json(name = "name_original") val nameOriginal: String?,
    val description: String?, // Oft HTML-formatiert
    @Json(name = "description_raw") val descriptionRaw: String?, // Oft reiner Text
    val metacritic: Int?,
    val released: String?, // z.B. "2023-05-20"
    val tba: Boolean?,
    @Json(name = "updated") val lastUpdated: String?,
    @Json(name = "background_image") val backgroundImage: String?,
    @Json(name = "background_image_additional") val backgroundImageAdditional: String?,
    val website: String?,
    val rating: Double?,
    @Json(name = "rating_top") val ratingTop: Int?,
    // ratings: List<Rating>?, // Kann komplex sein, optional hinzufügen
    val playtime: Int?,
    @Json(name = "screenshots_count") val screenshotsCount: Int?,
    @Json(name = "movies_count") val moviesCount: Int?,
    // ... viele weitere Felder wie reddit_url, twitch_count, youtube_count ...
    val platforms: List<RawgPlatformContainer>?, // Kann von Suchergebnissen wiederverwendet werden
    val stores: List<StoreContainer>?,
    val developers: List<DeveloperOrPublisher>?,
    val genres: List<RawgGenre>?, // Kann von Suchergebnissen wiederverwendet werden
    val tags: List<Tag>?,
    val publishers: List<DeveloperOrPublisher>?,
    @Json(name = "esrb_rating") val esrbRating: EsrbRating?
    // Parent platforms, additions_count, etc.
)

@JsonClass(generateAdapter = true)
data class StoreContainer(
    val id: Int?,
    val store: StoreDetails?
)

@JsonClass(generateAdapter = true)
data class StoreDetails(
    val id: Int,
    val name: String,
    val slug: String?,
    val domain: String?
    // image_background: String?
)

@JsonClass(generateAdapter = true)
data class DeveloperOrPublisher( // Kann für Entwickler und Publisher verwendet werden
    val id: Int,
    val name: String,
    val slug: String?
    // games_count: Int?,
    // image_background: String?
)

@JsonClass(generateAdapter = true)
data class Tag(
    val id: Int,
    val name: String,
    val slug: String?,
    val language: String?
    // games_count: Int?,
    // image_background: String?
)

@JsonClass(generateAdapter = true)
data class EsrbRating(
    val id: Int,
    val name: String, // z.B. "Mature", "Everyone"
    val slug: String?
)