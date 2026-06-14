package com.rovo.shared.data.local.entity

import androidx.room.Entity

@Entity(tableName = "watch_history", primaryKeys = ["id", "profileId"])
data class WatchHistoryEntity(
    val id: String,
    val profileId: Int,
    val title: String,
    val poster: String?,
    val background: String? = null,
    val logo: String? = null,
    val position: Long,
    val duration: Long,
    val lastWatched: Long,
    val type: String,
    val watched: Boolean = false,
    val scrobbled: Boolean = false
) {
    fun progress(): Float {
        if (duration == 0L) return 0f
        return position.toFloat() / duration.toFloat()
    }
}
