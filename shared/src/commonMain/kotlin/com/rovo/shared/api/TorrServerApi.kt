package com.rovo.shared.api

import com.rovo.shared.model.torrent.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class TorrServerApi(
    private val client: HttpClient,
    private val baseUrl: String = "http://127.0.0.1:8090"
) {

    suspend fun addTorrent(magnetLink: String, title: String = ""): TorrServerTorrentResponse {
        val body = buildJsonObject {
            put("action", "add")
            put("link", magnetLink)
            put("title", title)
            put("save_to_db", false)
        }
        return client.post("$baseUrl/torrents") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun getTorrentStats(magnetLink: String): TorrentStats {
        val hash = extractHash(magnetLink)
        val body = buildJsonObject {
            put("action", "get")
            put("hash", hash)
        }
        return try {
            val response: TorrServerTorrentResponse = client.post("$baseUrl/torrents") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
            
            TorrentStats(
                stat = response.stat ?: 0,
                active_peers = response.active_peers ?: 0,
                total_peers = response.total_peers ?: 0,
                connected_seeders = response.connected_seeders ?: 0,
                download_speed = response.download_speed ?: 0L,
                upload_speed = response.upload_speed ?: 0L,
                bytes_read = response.bytes_read ?: 0L,
                torrent_size = response.torrent_size ?: 0L,
                preloaded_bytes = response.preloaded_bytes ?: 0L
            )
        } catch (e: Exception) {
            TorrentStats()
        }
    }

    suspend fun dropTorrent(magnetLink: String) {
        val hash = extractHash(magnetLink)
        val body = buildJsonObject {
            put("action", "drop")
            put("hash", hash)
        }
        try {
            client.post("$baseUrl/torrents") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        } catch (_: Exception) {}
    }

    fun getStreamUrl(magnetLink: String, fileIndex: Int): String {
        val encoded = magnetLink.encodeURLParameter()
        return "$baseUrl/stream?link=$encoded&index=$fileIndex&play"
    }

    suspend fun getFileList(magnetLink: String): List<TorrServerFile> {
        val hash = extractHash(magnetLink)
        val body = buildJsonObject {
            put("action", "get")
            put("hash", hash)
        }
        return try {
            val response: TorrServerTorrentResponse = client.post("$baseUrl/torrents") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
            response.file_stats ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractHash(magnetLink: String): String {
        val regex = Regex("btih:([a-fA-F0-9]{40})")
        return regex.find(magnetLink)?.groupValues?.get(1) ?: magnetLink
    }
}
