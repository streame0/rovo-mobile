package com.rovo.shared.repository

import com.rovo.shared.api.DebridApi
import com.rovo.shared.data.local.entity.ProfileEntity
import com.rovo.shared.model.stremio.Stream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DebridRepository(
    private val api: DebridApi,
    private val profileRepository: ProfileRepository
) {
    suspend fun resolveStream(stream: Stream): String? = withContext(Dispatchers.Default) {
        val profile = profileRepository.getDefaultProfile()
        
        return@withContext when (profile.debridService) {
            "realdebrid" -> profile.realDebridToken?.let { resolveRealDebrid(it, stream) }
            "alldebrid" -> profile.allDebridToken?.let { resolveAllDebrid(it, stream) }
            else -> stream.url
        } ?: stream.url
    }

    private suspend fun resolveAllDebrid(token: String, stream: Stream): String? {
        val infoHash = stream.infoHash ?: return stream.url
        try {
            val magnet = "magnet:?xt=urn:btih:$infoHash"
            val added = api.adAddMagnet(token, magnet)
            
            if (added.status == "success" && added.data != null) {
                val magnetInfo = added.data.magnets.firstOrNull() ?: return stream.url
                
                if (magnetInfo.ready) {
                    val status = api.adGetMagnetStatus(token, magnetInfo.id)
                    val link = status.data?.magnets?.links?.getOrNull(stream.fileIdx ?: 0)?.link 
                        ?: status.data?.magnets?.links?.firstOrNull()?.link
                    
                    if (link != null) {
                        return api.adUnrestrict(token, link).data?.link
                    }
                }
            }
        } catch (_: Exception) {}
        return stream.url
    }

    private suspend fun resolveRealDebrid(token: String, stream: Stream): String? {
        if (stream.url?.contains("real-debrid.com") == true) return stream.url

        val infoHash = stream.infoHash ?: return stream.url
        
        try {
            // 1. Add magnet
            val magnet = "magnet:?xt=urn:btih:$infoHash"
            val added = api.rdAddMagnet(token, magnet)
            
            // 2. Get info to see files
            val info = api.rdGetTorrentInfo(token, added.id)
            
            // 3. Select files if not already selected (simplification: select all or first)
            if (info.status == "waiting_files_selection") {
                api.rdSelectFiles(token, added.id, "all")
            }
            
            // 4. Wait for it to be ready (simplified, in real app would poll or check status)
            val readyInfo = api.rdGetTorrentInfo(token, added.id)
            if (readyInfo.status == "downloaded" || readyInfo.status == "uploading") {
                val link = readyInfo.links.getOrNull(stream.fileIdx ?: 0) ?: readyInfo.links.firstOrNull()
                if (link != null) {
                    return api.rdUnrestrict(token, link).link
                }
            }
        } catch (e: Exception) {
            // Fallback to original URL
        }
        
        return stream.url
    }
}
