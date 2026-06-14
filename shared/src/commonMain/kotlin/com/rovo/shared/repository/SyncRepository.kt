package com.rovo.shared.repository

import com.rovo.shared.api.TmdbApi
import com.rovo.shared.api.TraktApi
import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.data.local.entity.ProfileEntity
import com.rovo.shared.data.local.entity.WatchHistoryEntity
import com.rovo.shared.data.local.entity.WatchlistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class SyncRepository(
    private val traktApi: TraktApi,
    private val tmdbApi: TmdbApi,
    private val dao: AddonDao
) {
    suspend fun syncWatchlist(profileId: Int) = withContext(Dispatchers.Default) {
        val profile = dao.getProfileById(profileId) ?: return@withContext
        val token = profile.traktToken ?: return@withContext

        try {
            val remoteWatchlist = traktApi.getWatchlist(token)
            val localWatchlist = dao.getWatchlist(profileId).first()
            
            remoteWatchlist.forEach { remoteItem ->
                val type = remoteItem.type
                val ids = if (type == "movie") remoteItem.movie?.ids else remoteItem.show?.ids
                val imdbId = ids?.imdb ?: ids?.tmdb?.toString() ?: return@forEach
                
                if (localWatchlist.none { it.id == imdbId }) {
                    // Fetch full metadata to get poster and correct details
                    val meta = try {
                        // We use cinemeta as a reliable default for Stremio-compatible IDs
                        val title = if (type == "movie") remoteItem.movie?.title else remoteItem.show?.title
                        WatchlistEntity(
                            id = imdbId,
                            profileId = profileId,
                            type = type,
                            title = title ?: "Unknown",
                            poster = null, 
                            addedAt = Clock.System.now().toEpochMilliseconds()
                        )
                    } catch (e: Exception) { null }
                    
                    meta?.let { entity -> dao.addToWatchlist(entity) }
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun addToTraktWatchlist(profileId: Int, type: String, id: String) = withContext(Dispatchers.Default) {
        val profile = dao.getProfileById(profileId) ?: return@withContext
        val token = profile.traktToken ?: return@withContext
        try {
            val imdbId = if (id.startsWith("tt")) id.split(":").first() else id
            val request = if (type == "movie") {
                TraktApi.TraktSyncRequest(movies = listOf(TraktApi.TraktMovie(title = "", ids = TraktApi.TraktIds(imdb = imdbId))))
            } else {
                TraktApi.TraktSyncRequest(shows = listOf(TraktApi.TraktShow(title = "", ids = TraktApi.TraktIds(imdb = imdbId))))
            }
            traktApi.addToWatchlist(token, request)
        } catch (_: Exception) {}
    }

    suspend fun removeFromTraktWatchlist(profileId: Int, type: String, id: String) = withContext(Dispatchers.Default) {
        val profile = dao.getProfileById(profileId) ?: return@withContext
        val token = profile.traktToken ?: return@withContext
        try {
            val imdbId = if (id.startsWith("tt")) id.split(":").first() else id
            val request = if (type == "movie") {
                TraktApi.TraktSyncRequest(movies = listOf(TraktApi.TraktMovie(title = "", ids = TraktApi.TraktIds(imdb = imdbId))))
            } else {
                TraktApi.TraktSyncRequest(shows = listOf(TraktApi.TraktShow(title = "", ids = TraktApi.TraktIds(imdb = imdbId))))
            }
            traktApi.removeFromWatchlist(token, request)
        } catch (_: Exception) {}
    }

    suspend fun scrobbleToTrakt(
        profile: ProfileEntity,
        type: String,
        id: String,
        progress: Float,
        action: String // "start", "pause", "stop"
    ) = withContext(Dispatchers.Default) {
        val token = profile.traktToken ?: return@withContext

        try {
            val imdbId = if (id.startsWith("tt")) id.split(":").first() else id
            
            val request = if (type == "movie") {
                TraktApi.TraktScrobbleRequest(
                    movie = TraktApi.TraktMovie(title = "", ids = TraktApi.TraktIds(imdb = imdbId)),
                    progress = progress * 100
                )
            } else {
                val parts = id.split(":")
                if (parts.size < 3) return@withContext
                TraktApi.TraktScrobbleRequest(
                    episode = TraktApi.TraktEpisode(
                        season = parts[1].toInt(),
                        number = parts[2].toInt(),
                        title = null,
                        ids = TraktApi.TraktIds(imdb = null)
                    ),
                    show = TraktApi.TraktShow(title = "", ids = TraktApi.TraktIds(imdb = parts[0])),
                    progress = progress * 100
                )
            }
            traktApi.scrobble(token, action, request)
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun clearTraktProgress(profileId: Int, type: String, id: String) = withContext(Dispatchers.Default) {
        val profile = dao.getProfileById(profileId) ?: return@withContext
        val token = profile.traktToken ?: return@withContext
        
        try {
            // 1. Clear paused/playback progress
            val playbackItems = traktApi.getPlaybackProgress(token)
            val imdbId = if (id.startsWith("tt")) id.split(":").first() else id
            
            playbackItems.forEach { item ->
                val itemImdbId = if (item.type == "movie") item.movie?.ids?.imdb else item.show?.ids?.imdb
                if (itemImdbId == imdbId) {
                    traktApi.removePlayback(token, item.id)
                }
            }
            
            // 2. Clear from history
            val request = if (type == "movie") {
                TraktApi.TraktSyncRequest(movies = listOf(TraktApi.TraktMovie(title = "", ids = TraktApi.TraktIds(imdb = imdbId))))
            } else {
                val parts = id.split(":")
                if (parts.size >= 3) {
                    TraktApi.TraktSyncRequest(episodes = listOf(TraktApi.TraktEpisode(
                        season = parts[1].toInt(),
                        number = parts[2].toInt(),
                        title = null,
                        ids = TraktApi.TraktIds(imdb = null)
                    )))
                } else {
                    TraktApi.TraktSyncRequest(shows = listOf(TraktApi.TraktShow(title = "", ids = TraktApi.TraktIds(imdb = imdbId))))
                }
            }
            traktApi.removeHistory(token, request)
        } catch (e: Exception) {
            // Log error
        }
    }
}
