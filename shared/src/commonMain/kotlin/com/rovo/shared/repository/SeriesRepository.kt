package com.rovo.shared.repository

import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.data.local.entity.SeriesNextUpEntity
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.MetaVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class SeriesRepository(
    private val dao: AddonDao,
    private val addonRepository: AddonRepository
) {
    fun getNextUp(profileId: Int): Flow<List<SeriesNextUpEntity>> = dao.getActiveSeriesNextUp(profileId)

    suspend fun updateNextUp(
        profileId: Int,
        seriesMeta: MetaItem,
        completedEpisode: MetaVideo
    ) {
        val nextEpisode = seriesMeta.videos?.find { 
            it.season == completedEpisode.season && it.episode == completedEpisode.episode + 1
        } ?: seriesMeta.videos?.find {
            it.season == completedEpisode.season + 1 && it.episode == 1
        }

        if (nextEpisode != null) {
            dao.upsertSeriesNextUp(
                SeriesNextUpEntity(
                    seriesId = seriesMeta.id,
                    profileId = profileId,
                    seriesTitle = seriesMeta.name,
                    seriesPoster = seriesMeta.poster,
                    nextEpisodeId = nextEpisode.id,
                    nextEpisodeTitle = nextEpisode.title,
                    nextSeason = nextEpisode.season,
                    nextEpisode = nextEpisode.episode,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                    isComplete = false
                )
            )
        } else {
            // Mark as complete if no more episodes
            val current = dao.getSeriesNextUp(seriesMeta.id, profileId)
            if (current != null) {
                dao.upsertSeriesNextUp(current.copy(isComplete = true, updatedAt = Clock.System.now().toEpochMilliseconds()))
            }
        }
    }
}
