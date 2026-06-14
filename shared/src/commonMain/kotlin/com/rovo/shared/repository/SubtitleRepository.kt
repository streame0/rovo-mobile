package com.rovo.shared.repository

import com.rovo.shared.api.StremioApi
import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.domain.AddonSubtitle
import com.rovo.shared.model.stremio.SubtitleResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class SubtitleRepository(
    private val api: StremioApi,
    private val dao: AddonDao
) {
    private val SUBTITLE_TIMEOUT_MS = 10_000L

    suspend fun getSubtitles(type: String, id: String): List<AddonSubtitle> = withContext(Dispatchers.Default) {
        val addons = dao.getAllAddons().firstOrNull()
            ?.filter { it.isEnabled } // Most addons might support subtitles even if not explicitly marked
            ?: emptyList()

        val jobs = addons.map { addon ->
            async {
                try {
                    val url = "${addon.transportUrl}/subtitles/$type/$id.json"
                    val response = withTimeout(SUBTITLE_TIMEOUT_MS) { api.getSubtitles(url) }
                    response.subtitles.map { sub ->
                        AddonSubtitle(
                            id = sub.id ?: "",
                            url = sub.url ?: "",
                            lang = sub.lang,
                            addonName = addon.nickname ?: addon.name
                        )
                    }
                } catch (e: Exception) {
                    emptyList<AddonSubtitle>()
                }
            }
        }

        jobs.awaitAll().flatten()
    }
}
