package com.rovo.shared.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

class TmdbApi(private val client: HttpClient) {
    private val baseUrl = "https://api.themoviedb.org/3"
    private val apiKey = "YOUR_TMDB_API_KEY" // This should ideally be provided via DI or config

    @Serializable
    data class TmdbExternalIds(
        val imdb_id: String? = null,
        val tvdb_id: Int? = null
    )

    @Serializable
    data class TmdbFindResult(
        val movie_results: List<TmdbMovie> = emptyList(),
        val tv_results: List<TmdbShow> = emptyList()
    )

    @Serializable
    data class TmdbMovie(
        val id: Int,
        val title: String,
        val overview: String? = null,
        val poster_path: String? = null,
        val backdrop_path: String? = null,
        val release_date: String? = null,
        val vote_average: Float? = null,
        val runtime: Int? = null,
        val genres: List<TmdbGenre>? = null,
        val credits: TmdbCredits? = null
    )

    @Serializable
    data class TmdbShow(
        val id: Int,
        val name: String,
        val overview: String? = null,
        val poster_path: String? = null,
        val backdrop_path: String? = null,
        val first_air_date: String? = null,
        val vote_average: Float? = null,
        val genres: List<TmdbGenre>? = null,
        val credits: TmdbCredits? = null
    )

    @Serializable
    data class TmdbGenre(
        val id: Int,
        val name: String
    )

    @Serializable
    data class TmdbVideoResult(
        val results: List<TmdbVideo> = emptyList()
    )

    @Serializable
    data class TmdbVideo(
        val key: String,
        val site: String,
        val type: String,
        val name: String
    )

    @Serializable
    data class TmdbPageResponse<T>(
        val results: List<T> = emptyList()
    )

    @Serializable
    data class TmdbSeason(
        val episodes: List<TmdbEpisode> = emptyList()
    )

    @Serializable
    data class TmdbEpisode(
        val season_number: Int,
        val episode_number: Int,
        val name: String? = null,
        val overview: String? = null,
        val still_path: String? = null,
        val air_date: String? = null,
        val runtime: Int? = null
    )

    @Serializable
    data class TmdbCredits(
        val cast: List<TmdbCast> = emptyList()
    )

    @Serializable
    data class TmdbCast(
        val name: String,
        val character: String? = null,
        val profile_path: String? = null
    )

    suspend fun findByExternalId(externalId: String, source: String = "imdb_id"): TmdbFindResult {
        return client.get("$baseUrl/find/$externalId") {
            parameter("api_key", apiKey)
            parameter("external_source", source)
        }.body()
    }

    suspend fun getExternalIds(type: String, tmdbId: String): TmdbExternalIds {
        val path = if (type == "movie") "movie" else "tv"
        return client.get("$baseUrl/$path/$tmdbId/external_ids") {
            parameter("api_key", apiKey)
        }.body()
    }

    suspend fun getMovieDetails(id: Int, language: String = "en-US"): TmdbMovie {
        return client.get("$baseUrl/movie/$id") {
            parameter("api_key", apiKey)
            parameter("language", language)
            parameter("append_to_response", "credits")
        }.body()
    }

    suspend fun getTvDetails(id: Int, language: String = "en-US"): TmdbShow {
        return client.get("$baseUrl/tv/$id") {
            parameter("api_key", apiKey)
            parameter("language", language)
            parameter("append_to_response", "credits")
        }.body()
    }

    suspend fun getRecommendations(type: String, id: Int, language: String = "en-US"): TmdbPageResponse<TmdbMovie> {
        val path = if (type == "movie") "movie" else "tv"
        return client.get("$baseUrl/$path/$id/recommendations") {
            parameter("api_key", apiKey)
            parameter("language", language)
        }.body()
    }

    suspend fun getVideos(type: String, id: Int, language: String = "en-US"): TmdbVideoResult {
        val path = if (type == "movie") "movie" else "tv"
        return client.get("$baseUrl/$path/$id/videos") {
            parameter("api_key", apiKey)
            parameter("language", language)
        }.body()
    }

    suspend fun getSeason(id: Int, seasonNumber: Int, language: String = "en-US"): TmdbSeason {
        return client.get("$baseUrl/tv/$id/season/$seasonNumber") {
            parameter("api_key", apiKey)
            parameter("language", language)
        }.body()
    }
}
