package com.rovo.app.features.watching.sync

import com.rovo.app.features.watched.WatchedItem

interface WatchedSyncAdapter {
    suspend fun pull(
        profileId: Int,
        pageSize: Int,
    ): List<WatchedItem>

    suspend fun push(
        profileId: Int,
        items: Collection<WatchedItem>,
    )

    suspend fun delete(
        profileId: Int,
        items: Collection<WatchedItem>,
    )
}
