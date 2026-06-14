package com.rovo.shared.data.local.entity

import androidx.room.Entity

@Entity(tableName = "watchlist", primaryKeys = ["id", "profileId"])
data class WatchlistEntity(
    val id: String,
    val profileId: Int,
    val type: String,
    val title: String,
    val poster: String?,
    val addedAt: Long
)
