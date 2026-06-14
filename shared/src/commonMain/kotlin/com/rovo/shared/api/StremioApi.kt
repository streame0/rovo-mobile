package com.rovo.shared.api

import com.rovo.shared.model.stremio.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class StremioApi(private val client: HttpClient) {

    suspend fun getManifest(url: String): Manifest {
        println("StremioApi: getManifest from $url")
        return client.get(url).body()
    }

    suspend fun getCatalog(url: String): CatalogResponse {
        println("StremioApi: getCatalog from $url")
        return client.get(url).body()
    }

    suspend fun getMeta(url: String): MetaResponse = client.get(url).body()

    suspend fun getStreams(url: String): StreamResponse = client.get(url).body()

    suspend fun getSubtitles(url: String): SubtitleResponse = client.get(url).body()
}
