package com.rovo.shared.repository

import com.rovo.shared.api.IntroApi
import com.rovo.shared.model.introdb.IntroDbSegmentsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class IntroRepository(
    private val api: IntroApi
) {
    private val cache = mutableMapOf<String, IntroDbSegmentsResponse>()

    suspend fun getSegments(
        imdbId: String,
        season: Int,
        episode: Int
    ): IntroDbSegmentsResponse? = withContext(Dispatchers.IO) {
        val cacheKey = "$imdbId:$season:$episode"
        cache[cacheKey]?.let { return@withContext it }

        try {
            val url = "$BASE_URL/segments?imdb_id=$imdbId&season=$season&episode=$episode"
            val response = withTimeout(TIMEOUT_MS) { api.getSegments(url) }
            cache[cacheKey] = response
            response
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val BASE_URL = "https://api.introdb.app"
        private const val TIMEOUT_MS = 5_000L
    }
}
