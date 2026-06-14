package com.rovo.shared.data.player

import com.rovo.shared.model.stremio.Stream
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class SourceSelectionStore(private val settings: Settings) {
    fun rememberSelection(playbackId: String, stream: Stream) {
        val scopedId = canonicalSourceScopeId(playbackId)
        val streamFingerprint = streamFingerprint(stream) ?: return
        val addonTag = addonTag(stream)

        settings[streamKey(scopedId)] = streamFingerprint
        if (addonTag.isNullOrBlank()) {
            settings.remove(addonPrefKey(scopedId))
        } else {
            settings[addonPrefKey(scopedId)] = addonTag
        }
    }

    fun clearSelection(playbackId: String) {
        val scopedId = canonicalSourceScopeId(playbackId)
        settings.remove(streamKey(scopedId))
        settings.remove(addonPrefKey(scopedId))
    }

    fun hasRememberedSelection(playbackId: String): Boolean {
        val scopedId = canonicalSourceScopeId(playbackId)
        return !settings.getStringOrNull(streamKey(scopedId)).isNullOrBlank()
            || !settings.getStringOrNull(addonPrefKey(scopedId)).isNullOrBlank()
    }

    fun findPreferredStream(playbackId: String, streams: List<Stream>): Stream? {
        if (streams.isEmpty()) return null

        val playableStreams = streams.filter(::isPlayableStream)
        if (playableStreams.isEmpty()) return null

        val scopedId = canonicalSourceScopeId(playbackId)
        val savedFingerprint = settings.getStringOrNull(streamKey(scopedId))
        val savedAddon = settings.getStringOrNull(addonPrefKey(scopedId))

        if (!savedFingerprint.isNullOrBlank()) {
            val exactMatch = playableStreams.firstOrNull { streamFingerprint(it) == savedFingerprint }
            if (exactMatch != null) return exactMatch
        }

        if (!savedAddon.isNullOrBlank()) {
            val addonMatch = playableStreams.firstOrNull { addonTag(it) == savedAddon }
            if (addonMatch != null) return addonMatch
        }

        return null
    }

    private fun streamKey(scopedId: String): String = "${KEY_STREAM_PREFIX}$scopedId"

    private fun addonPrefKey(scopedId: String): String = "${KEY_ADDON_PREFIX}$scopedId"

    private fun streamFingerprint(stream: Stream): String? {
        val normalizedInfoHash = normalize(stream.infoHash)
        val normalizedUrl = normalize(stream.url)
        val normalizedFileIdx = stream.fileIdx?.toString()
        val normalizedName = normalize(stream.name)
        val normalizedTitle = normalize(stream.title)

        if (
            normalizedInfoHash == null &&
            normalizedUrl == null &&
            normalizedFileIdx == null &&
            normalizedName == null &&
            normalizedTitle == null
        ) {
            return null
        }

        return listOf(
            normalizedInfoHash ?: "",
            normalizedUrl ?: "",
            normalizedFileIdx ?: "",
            normalizedName ?: "",
            normalizedTitle ?: ""
        ).joinToString("|")
    }

    private fun addonTag(stream: Stream): String? {
        val sourceName = stream.name ?: return null
        if (!sourceName.contains("[") || !sourceName.contains("]")) return null
        return normalize(sourceName.substringAfter("[").substringBefore("]"))
    }

    private fun canonicalSourceScopeId(playbackId: String): String {
        return playbackId.trim()
    }

    private fun normalize(value: String?): String? {
        if (value == null) return null
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return trimmed.lowercase()
    }

    private fun isPlayableStream(stream: Stream): Boolean {
        return !stream.url.isNullOrBlank() || !stream.infoHash.isNullOrBlank()
    }

    companion object {
        private const val KEY_STREAM_PREFIX = "stream_"
        private const val KEY_ADDON_PREFIX = "addon_"
    }
}
