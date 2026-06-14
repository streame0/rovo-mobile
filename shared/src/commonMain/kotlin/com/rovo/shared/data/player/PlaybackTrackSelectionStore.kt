package com.rovo.shared.data.player

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class PlaybackTrackSelectionStore(private val settings: Settings) {
    data class Selection(
        val audioTrackId: String?,
        val subtitleTrackId: String?,
        val subtitleDelayMs: Long? = null
    )

    fun getSelection(playbackId: String): Selection? {
        val scopedId = canonicalPlaybackId(playbackId) ?: return null
        val audioTrackId = settings.getStringOrNull(audioKey(scopedId))
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val subtitleTrackId = settings.getStringOrNull(subtitleKey(scopedId))
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        
        val subtitleDelay = if (settings.hasKey(subtitleDelayKey(scopedId))) {
            settings.getLong(subtitleDelayKey(scopedId), 0L)
        } else {
            null
        }

        if (audioTrackId == null && subtitleTrackId == null && subtitleDelay == null) return null
        return Selection(
            audioTrackId = audioTrackId,
            subtitleTrackId = subtitleTrackId,
            subtitleDelayMs = subtitleDelay
        )
    }

    fun updateSelection(
        playbackId: String,
        audioTrackId: String?,
        subtitleTrackId: String?,
        subtitleDelayMs: Long? = null,
        updateAudio: Boolean,
        updateSubtitle: Boolean,
        updateSubtitleDelay: Boolean = false
    ) {
        val scopedId = canonicalPlaybackId(playbackId) ?: return
        if (!updateAudio && !updateSubtitle && !updateSubtitleDelay) return

        if (updateAudio) {
            val normalizedAudio = audioTrackId?.trim()?.takeIf { it.isNotEmpty() }
            if (normalizedAudio == null) {
                settings.remove(audioKey(scopedId))
            } else {
                settings[audioKey(scopedId)] = normalizedAudio
            }
        }
        if (updateSubtitle) {
            val normalizedSubtitle = subtitleTrackId?.trim()?.takeIf { it.isNotEmpty() }
            if (normalizedSubtitle == null) {
                settings.remove(subtitleKey(scopedId))
            } else {
                settings[subtitleKey(scopedId)] = normalizedSubtitle
            }
        }
        if (updateSubtitleDelay) {
            val delay = subtitleDelayMs
            if (delay == null || delay == 0L) {
                settings.remove(subtitleDelayKey(scopedId))
            } else {
                settings[subtitleDelayKey(scopedId)] = delay
            }
        }
    }

    fun clearSelection(playbackId: String) {
        val scopedId = canonicalPlaybackId(playbackId) ?: return
        settings.remove(audioKey(scopedId))
        settings.remove(subtitleKey(scopedId))
        settings.remove(subtitleDelayKey(scopedId))
    }

    private fun canonicalPlaybackId(playbackId: String): String? {
        return playbackId.trim().takeIf { it.isNotEmpty() }
    }

    private fun audioKey(scopedId: String): String = "${KEY_AUDIO_PREFIX}$scopedId"

    private fun subtitleKey(scopedId: String): String = "${KEY_SUBTITLE_PREFIX}$scopedId"

    private fun subtitleDelayKey(scopedId: String): String = "${KEY_SUBTITLE_DELAY_PREFIX}$scopedId"

    companion object {
        private const val KEY_AUDIO_PREFIX = "audio_"
        private const val KEY_SUBTITLE_PREFIX = "subtitle_"
        private const val KEY_SUBTITLE_DELAY_PREFIX = "subtitleDelay_"
    }
}
