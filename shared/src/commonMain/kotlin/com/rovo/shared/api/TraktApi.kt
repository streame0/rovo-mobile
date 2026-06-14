package com.rovo.shared.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class TraktApi(private val client: HttpClient) {
    private val baseUrl = "https://api.trakt.tv"
    private val clientId = "YOUR_TRAKT_CLIENT_ID"

    @Serializable
    data class TraktIds(
        val trakt: Int? = null,
        val slug: String? = null,
        val tvdb: Int? = null,
        val imdb: String? = null,
        val tmdb: Int? = null
    )

    @Serializable
    data class TraktPlaybackProgress(
        val progress: Float,
        val paused_at: String?,
        val id: Long,
        val type: String,
        val movie: TraktMovie? = null,
        val episode: TraktEpisode? = null,
        val show: TraktShow? = null
    )

    @Serializable
    data class TraktEpisode(
        val season: Int,
        val number: Int,
        val title: String?,
        val ids: TraktIds
    )

    @Serializable
    data class TraktScrobbleRequest(
        val movie: TraktMovie? = null,
        val show: TraktShow? = null,
        val episode: TraktEpisode? = null,
        val progress: Float,
        val app_version: String = "1.0.0",
        val app_date: String = "2024-01-01"
    )

    @Serializable
    data class TraktSyncRequest(
        val movies: List<TraktMovie>? = null,
        val shows: List<TraktShow>? = null,
        val episodes: List<TraktEpisode>? = null
    )

    @Serializable
    data class TraktWatchlistItem(
        val rank: Int,
        val id: Long,
        val type: String,
        val movie: TraktMovie? = null,
        val show: TraktShow? = null
    )

    suspend fun getWatchlist(token: String): List<TraktWatchlistItem> {
        return client.get("$baseUrl/sync/watchlist") {
            header("trakt-api-version", "2")
            header("trakt-api-key", clientId)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun searchByImdbId(imdbId: String): List<TraktSearchResult> {
        return client.get("$baseUrl/search/imdb/$imdbId") {
            header("trakt-api-version", "2")
            header("trakt-api-key", clientId)
        }.body()
    }

    suspend fun getPlaybackProgress(token: String): List<TraktPlaybackProgress> {
        return client.get("$baseUrl/sync/playback") {
            header("trakt-api-version", "2")
            header("trakt-api-key", clientId)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun scrobble(token: String, action: String, request: TraktScrobbleRequest) {
        // action: start, pause, stop
        client.post("$baseUrl/scrobble/$action") {
            header("trakt-api-version", "2")
            header("trakt-api-key", clientId)
            header("Authorization", "Bearer $token")
            setBody(request)
            contentType(ContentType.Application.Json)
        }
    }

    @Serializable
    data class TraktTokenResponse(
        val access_token: String,
        val token_type: String,
        val expires_in: Long,
        val refresh_token: String,
        val scope: String,
        val created_at: Long
    )

    @Serializable
    data class TraktTokenRequest(
        val code: String? = null,
        val refresh_token: String? = null,
        val client_id: String,
        val client_secret: String,
        val redirect_uri: String,
        val grant_type: String
    )

    fun getAuthUrl(redirectUri: String): String {
        return "$baseUrl/oauth/authorize?response_type=code&client_id=$clientId&redirect_uri=$redirectUri"
    }

    suspend fun exchangeCodeForToken(code: String, redirectUri: String, clientSecret: String): TraktTokenResponse {
        return client.post("$baseUrl/oauth/token") {
            setBody(TraktTokenRequest(
                code = code,
                client_id = clientId,
                client_secret = clientSecret,
                redirect_uri = redirectUri,
                grant_type = "authorization_code"
            ))
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun refreshToken(refreshToken: String, redirectUri: String, clientSecret: String): TraktTokenResponse {
        return client.post("$baseUrl/oauth/token") {
            setBody(TraktTokenRequest(
                refresh_token = refreshToken,
                client_id = clientId,
                client_secret = clientSecret,
                redirect_uri = redirectUri,
                grant_type = "refresh_token"
            ))
            contentType(ContentType.Application.Json)
        }.body()
    }

    @Serializable
    data class TraktMovie(
        val title: String,
        val year: Int? = null,
        val ids: TraktIds
    )

    @Serializable
    data class TraktShow(
        val title: String,
        val year: Int? = null,
        val ids: TraktIds
    )

    @Serializable
    data class TraktSearchResult(
        val type: String,
        val score: Double,
        val movie: TraktMovie? = null,
        val show: TraktShow? = null
    )

    suspend fun addToWatchlist(token: String, request: TraktSyncRequest) {
        client.post("$baseUrl/sync/watchlist") {
            header("trakt-api-version", "2")
            header("trakt-api-key", clientId)
            header("Authorization", "Bearer $token")
            setBody(request)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun removeFromWatchlist(token: String, request: TraktSyncRequest) {
        client.post("$baseUrl/sync/watchlist/remove") {
            header("trakt-api-version", "2")
            header("trakt-api-key", clientId)
            header("Authorization", "Bearer $token")
            setBody(request)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun removePlayback(token: String, playbackId: Long) {
        client.delete("$baseUrl/sync/playback/$playbackId") {
            header("trakt-api-version", "2")
            header("trakt-api-key", clientId)
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun removeHistory(token: String, request: TraktSyncRequest) {
        client.post("$baseUrl/sync/history/remove") {
            header("trakt-api-version", "2")
            header("trakt-api-key", clientId)
            header("Authorization", "Bearer $token")
            setBody(request)
            contentType(ContentType.Application.Json)
        }
    }

    // Add more Trakt methods as needed for sync, watchlist, etc.
}
