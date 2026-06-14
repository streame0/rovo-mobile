package com.rovo.shared.domain

import com.rovo.shared.model.stremio.Stream
import com.rovo.shared.model.stremio.StreamSubtitle

val TORRENT_TRACKERS = listOf(
    // HTTP trackers (TCP — work even when UDP is blocked)
    "http://tracker.opentrackr.org:1337/announce",
    "http://tracker.openbittorrent.com:80/announce",
    "http://tracker1.bt.moack.co.kr:80/announce",
    "http://tracker.gbitt.info:80/announce",
    // UDP trackers (fallback)
    "udp://tracker.opentrackr.org:1337/announce",
    "udp://open.stealth.si:80/announce",
    "udp://tracker.openbittorrent.com:6969/announce",
    "udp://exodus.desync.com:6969/announce"
)

fun resolvePlayableSourceUrl(stream: Stream): String? {
    val directUrl = stream.url?.trim()?.takeIf { it.isNotEmpty() }
    if (directUrl != null) return directUrl

    val infoHash = stream.infoHash?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    // Combine hardcoded trackers with addon-provided tracker URLs
    val addonTrackers = stream.sources
        ?.filter { it.startsWith("tracker:") }
        ?.map { it.removePrefix("tracker:") }
        ?: emptyList()
    val allTrackers = (addonTrackers + TORRENT_TRACKERS).distinct()
    
    // In KMP we don't have java.net.URLEncoder. 
    // We'll use a simple replacement for common characters in tracker URLs.
    val trackerParams = allTrackers.joinToString("") {
        "&tr=${it.replace(":", "%3A").replace("/", "%2F")}"
    }
    return "magnet:?xt=urn:btih:${infoHash}&dn=Video${trackerParams}"
}

fun sourceDisplayLabel(stream: Stream): String {
    val primary = stream.description
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: stream.title
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        ?: stream.name
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        ?: "Source"
    return primary.replace('\n', ' ')
}

fun sanitizeSubtitleSourceName(rawName: String?, fallback: String): String {
    val cleaned = rawName
        ?.replace("[", "")
        ?.replace("]", "")
        ?.trim()
        .orEmpty()
    return cleaned.ifEmpty { fallback }
}

fun normalizeSubtitleLanguageTag(rawLang: String?): String? {
    val value = rawLang?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return value.replace('_', '-').lowercase()
}

data class PlayerSubtitlePayload(
    val id: String,
    val url: String,
    val name: String,
    val language: String?,
    val isDefault: Boolean = false
)

data class PlayerAudioTrackPayload(
    val id: String,
    val name: String,
    val language: String?,
    val isDefault: Boolean = false
)

data class PlayerSourceOption(
    val id: String,
    val url: String,
    val label: String,
    val name: String?,
    val title: String?,
    val description: String?,
    val fileIdx: Int,
    val fileName: String
)

fun buildSourcePayload(
    streams: List<Stream>,
    selectedStream: Stream
): List<PlayerSourceOption> {
    val selectedUrl = resolvePlayableSourceUrl(selectedStream)
    return streams.mapNotNull { stream ->
        val url = resolvePlayableSourceUrl(stream) ?: return@mapNotNull null
        PlayerSourceOption(
            id = url,
            url = url,
            label = sourceDisplayLabel(stream),
            name = stream.name,
            title = stream.title,
            description = stream.description,
            fileIdx = stream.fileIdx ?: -1,
            fileName = stream.behaviorHints?.filename ?: ""
        )
    }
        .distinctBy { it.url }
        .sortedByDescending { option -> option.url == selectedUrl }
}

fun buildSubtitlePayload(stream: Stream, addonSubtitles: List<AddonSubtitle>): List<PlayerSubtitlePayload> {
    val embedded = stream.subtitles.orEmpty().mapNotNull { sub ->
        val url = sub.url?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
        val name = sanitizeSubtitleSourceName(sub.name, "Embedded subtitle")
        val lang = normalizeSubtitleLanguageTag(sub.lang)
        PlayerSubtitlePayload(
            id = sub.id ?: url,
            url = url,
            name = name,
            language = lang
        )
    }

    val addon = addonSubtitles.map { sub ->
        val name = sanitizeSubtitleSourceName(sub.addonName, "Addon subtitle")
        val lang = normalizeSubtitleLanguageTag(sub.lang)
        PlayerSubtitlePayload(
            id = sub.id,
            url = sub.url,
            name = name,
            language = lang
        )
    }

    return (embedded + addon).distinctBy { "${it.url}|${it.language}" }
}
