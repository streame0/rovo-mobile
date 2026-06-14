package com.rovo.shared.model.torrent

import kotlinx.serialization.Serializable

@Serializable
data class TorrentStats(
    val stat: Int = 0,          // 0=Added, 1=GettingInfo, 2=Preload, 3=Working, 4=Closed
    val active_peers: Int = 0,
    val total_peers: Int = 0,
    val connected_seeders: Int = 0,
    val download_speed: Long = 0L,
    val upload_speed: Long = 0L,
    val bytes_read: Long = 0L,
    val torrent_size: Long = 0L,
    val preloaded_bytes: Long = 0L
) {
    fun statusText(): String = when (stat) {
        0 -> "Connecting to peers..."
        1 -> "Fetching metadata..."
        2 -> "Buffering..."
        3 -> "Streaming"
        4 -> "Stopped"
        else -> "Connecting..."
    }
}

@Serializable
data class TorrServerFile(
    val id: Int,
    val path: String,
    val length: Long
)

@Serializable
data class TorrentProgress(
    val status: String,
    val downloadSpeed: Long = 0,
    val peers: Int = 0,
    val seeds: Int = 0,
    val progress: Float? = null  // null = indeterminate, 0.0-1.0 = determinate
)

@Serializable
data class TorrServerTorrentResponse(
    val stat: Int? = null,
    val active_peers: Int? = null,
    val total_peers: Int? = null,
    val connected_seeders: Int? = null,
    val download_speed: Long? = null,
    val upload_speed: Long? = null,
    val bytes_read: Long? = null,
    val torrent_size: Long? = null,
    val preloaded_bytes: Long? = null,
    val file_stats: List<TorrServerFile>? = null
)
