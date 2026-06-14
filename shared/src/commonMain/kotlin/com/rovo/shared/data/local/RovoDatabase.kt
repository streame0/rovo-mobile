package com.rovo.shared.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.data.local.entity.*

@Database(
    entities = [
        AddonEntity::class,
        ProfileEntity::class,
        WatchHistoryEntity::class,
        CatalogConfigEntity::class,
        ThemeEntity::class,
        HubRowEntity::class,
        HubRowItemEntity::class,
        WatchlistEntity::class,
        SeriesNextUpEntity::class
    ],
    version = 1
)
abstract class RovoDatabase : RoomDatabase() {
    abstract fun addonDao(): AddonDao
}

interface DBBuilder {
    fun build(): RovoDatabase
}
