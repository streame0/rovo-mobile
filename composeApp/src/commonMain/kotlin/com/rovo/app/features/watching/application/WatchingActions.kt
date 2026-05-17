package com.rovo.app.features.watching.application

import com.rovo.app.features.details.MetaDetails
import com.rovo.app.features.details.MetaDetailsRepository
import com.rovo.app.features.details.MetaVideo
import com.rovo.app.features.home.MetaPreview
import com.rovo.app.features.watched.WatchedItem
import com.rovo.app.features.watched.WatchedRepository
import com.rovo.app.features.watched.episodePlaybackId
import com.rovo.app.features.watched.releasedPlayableEpisodes
import com.rovo.app.features.watched.toEpisodeWatchedItem
import com.rovo.app.features.watched.toSeriesWatchedItem
import com.rovo.app.features.watched.toWatchedItem
import com.rovo.app.features.watchprogress.CurrentDateProvider
import com.rovo.app.features.watchprogress.WatchProgressEntry
import com.rovo.app.features.watchprogress.WatchProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object WatchingActions {
    private val actionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    suspend fun togglePosterWatched(preview: MetaPreview) {
        if (preview.type != "series") {
            WatchedRepository.toggleWatched(preview.toWatchedItem(markedAtEpochMs = 0L))
            return
        }

        val isCurrentlyWatched = WatchedRepository.isWatched(
            id = preview.id,
            type = preview.type,
        )
        val meta = MetaDetailsRepository.fetch(type = preview.type, id = preview.id)
        if (meta == null) {
            WatchedRepository.toggleWatched(preview.toWatchedItem(markedAtEpochMs = 0L))
            return
        }

        val todayIsoDate = CurrentDateProvider.todayIsoDate()
        val seriesItems = buildList {
            add(meta.toSeriesWatchedItem())
            addAll(meta.releasedPlayableEpisodes(todayIsoDate).map(meta::toEpisodeWatchedItem))
        }

        if (isCurrentlyWatched) {
            WatchedRepository.unmarkWatched(seriesItems)
        } else {
            WatchedRepository.markWatched(seriesItems)
            WatchProgressRepository.clearProgress(
                meta.releasedPlayableEpisodes(todayIsoDate).map(meta::episodePlaybackId),
            )
        }
    }

    fun toggleEpisodeWatched(
        meta: MetaDetails,
        episode: MetaVideo,
        isCurrentlyWatched: Boolean,
    ) {
        val watchedItem = meta.toEpisodeWatchedItem(episode)
        if (isCurrentlyWatched) {
            WatchedRepository.unmarkWatched(watchedItem)
        } else {
            WatchedRepository.markWatched(watchedItem)
            WatchProgressRepository.clearProgress(meta.episodePlaybackId(episode))
        }
        reconcileSeriesWatchedState(meta)
    }

    fun togglePreviousEpisodesWatched(
        meta: MetaDetails,
        episodes: Collection<MetaVideo>,
        areCurrentlyWatched: Boolean,
    ) {
        toggleEpisodesWatched(
            meta = meta,
            episodes = episodes,
            areCurrentlyWatched = areCurrentlyWatched,
        )
    }

    fun toggleSeasonWatched(
        meta: MetaDetails,
        episodes: Collection<MetaVideo>,
        areCurrentlyWatched: Boolean,
    ) {
        toggleEpisodesWatched(
            meta = meta,
            episodes = episodes,
            areCurrentlyWatched = areCurrentlyWatched,
        )
    }

    fun reconcileSeriesWatchedState(
        meta: MetaDetails,
        todayIsoDate: String = CurrentDateProvider.todayIsoDate(),
    ) {
        WatchedRepository.reconcileSeriesWatchedState(
            meta = meta,
            todayIsoDate = todayIsoDate,
            isEpisodeCompleted = { episode ->
                WatchProgressRepository.progressForVideo(meta.episodePlaybackId(episode))?.isCompleted == true
            },
        )
    }

    fun onProgressEntryUpdated(entry: WatchProgressEntry) {
        if (!entry.isCompleted) return

        val watchedItem = WatchedItem(
            id = entry.parentMetaId,
            type = entry.parentMetaType,
            name = entry.title,
            poster = entry.poster,
            season = entry.seasonNumber,
            episode = entry.episodeNumber,
            markedAtEpochMs = entry.lastUpdatedEpochMs,
        )
        WatchedRepository.markWatched(watchedItem)

        if (!entry.isEpisode) return
        actionScope.launch {
            val meta = runCatching {
                MetaDetailsRepository.fetch(
                    type = entry.parentMetaType,
                    id = entry.parentMetaId,
                )
            }.getOrNull() ?: return@launch

            reconcileSeriesWatchedState(meta = meta)
        }
    }

    private fun toggleEpisodesWatched(
        meta: MetaDetails,
        episodes: Collection<MetaVideo>,
        areCurrentlyWatched: Boolean,
    ) {
        if (episodes.isEmpty()) return
        val watchedItems = episodes.map(meta::toEpisodeWatchedItem)
        if (areCurrentlyWatched) {
            WatchedRepository.unmarkWatched(watchedItems)
        } else {
            WatchedRepository.markWatched(watchedItems)
            WatchProgressRepository.clearProgress(episodes.map(meta::episodePlaybackId))
        }
        reconcileSeriesWatchedState(meta)
    }
}
