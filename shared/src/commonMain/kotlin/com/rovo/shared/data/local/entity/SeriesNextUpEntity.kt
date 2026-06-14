package com.rovo.shared.data.local.entity

import androidx.room.Entity

@Entity(tableName = "series_next_up", primaryKeys = ["seriesId", "profileId"])
data class SeriesNextUpEntity(
    val seriesId: String,
    val profileId: Int,
    val seriesTitle: String,
    val seriesPoster: String?,
    val nextEpisodeId: String,
    val nextSeason: Int,
    val nextEpisode: Int,
    val nextEpisodeTitle: String?,
    val nextReleased: String? = null,
    val isComplete: Boolean = false,
    val isNewEpisode: Boolean = false,
    val updatedAt: Long
)
