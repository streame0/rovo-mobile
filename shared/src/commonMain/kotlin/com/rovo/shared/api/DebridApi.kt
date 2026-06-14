package com.rovo.shared.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class DebridApi(private val client: HttpClient) {

    @Serializable
    data class RealDebridUnrestrictResponse(
        val link: String,
        val filename: String,
        val filesize: Long,
        val streamable: Int? = null
    )

    @Serializable
    data class RealDebridTorrentInfo(
        val id: String,
        val filename: String,
        val hash: String,
        val bytes: Long,
        val progress: Float,
        val status: String,
        val links: List<String> = emptyList()
    )

    @Serializable
    data class RealDebridAddMagnetResponse(
        val id: String,
        val uri: String
    )

    @Serializable
    data class AllDebridMagnetUploadResponse(
        val status: String,
        val data: AllDebridMagnetData? = null
    )

    @Serializable
    data class AllDebridMagnetData(
        val magnets: List<AllDebridMagnetInfo> = emptyList()
    )

    @Serializable
    data class AllDebridMagnetInfo(
        val id: String,
        val hash: String,
        val name: String?,
        val ready: Boolean,
        val status: String? = null
    )

    @Serializable
    data class AllDebridMagnetStatusResponse(
        val status: String,
        val data: AllDebridMagnetStatusData? = null
    )

    @Serializable
    data class AllDebridMagnetStatusData(
        val magnets: AllDebridMagnetStatusInfo
    )

    @Serializable
    data class AllDebridMagnetStatusInfo(
        val id: String,
        val statusCode: Int,
        val status: String,
        val links: List<AllDebridLink> = emptyList()
    )

    @Serializable
    data class AllDebridLink(
        val link: String,
        val filename: String?,
        val size: Long
    )

    @Serializable
    data class AllDebridUnrestrictResponse(
        val status: String,
        val data: AllDebridUnrestrictData? = null
    )

    @Serializable
    data class AllDebridUnrestrictData(
        val link: String
    )

    private val rdBaseUrl = "https://api.real-debrid.com/rest/1.0"
    private val adBaseUrl = "https://api.alldebrid.com/v4"
    private val agent = "rovo-mobile"

    suspend fun rdAddMagnet(token: String, magnet: String): RealDebridAddMagnetResponse {
        return client.post("$rdBaseUrl/torrents/addMagnet") {
            header("Authorization", "Bearer $token")
            setBody(FormDataContent(Parameters.build {
                append("magnet", magnet)
            }))
        }.body()
    }

    suspend fun rdGetTorrentInfo(token: String, id: String): RealDebridTorrentInfo {
        return client.get("$rdBaseUrl/torrents/info/$id") {
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun rdSelectFiles(token: String, id: String, fileIds: String) {
        client.post("$rdBaseUrl/torrents/selectFiles/$id") {
            header("Authorization", "Bearer $token")
            setBody(FormDataContent(Parameters.build {
                append("files", fileIds)
            }))
        }
    }

    suspend fun rdUnrestrict(token: String, link: String): RealDebridUnrestrictResponse {
        return client.post("$rdBaseUrl/unrestrict/link") {
            header("Authorization", "Bearer $token")
            setBody(FormDataContent(Parameters.build {
                append("link", link)
            }))
        }.body()
    }

    suspend fun adAddMagnet(token: String, magnet: String): AllDebridMagnetUploadResponse {
        return client.get("$adBaseUrl/magnet/upload") {
            parameter("apikey", token)
            parameter("agent", agent)
            parameter("magnets[]", magnet)
        }.body()
    }

    suspend fun adGetMagnetStatus(token: String, id: String): AllDebridMagnetStatusResponse {
        return client.get("$adBaseUrl/magnet/status") {
            parameter("apikey", token)
            parameter("agent", agent)
            parameter("id", id)
        }.body()
    }

    suspend fun adUnrestrict(token: String, link: String): AllDebridUnrestrictResponse {
        return client.get("$adBaseUrl/link/unlock") {
            parameter("apikey", token)
            parameter("agent", agent)
            parameter("link", link)
        }.body()
    }
}
