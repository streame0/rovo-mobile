package com.rovo.shared.api

import com.rovo.shared.model.introdb.IntroDbSegmentsResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class IntroApi(private val client: HttpClient) {
    suspend fun getSegments(url: String): IntroDbSegmentsResponse = client.get(url).body()
}
