package com.rovo.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_configs")
data class CatalogConfigEntity(
    @PrimaryKey val uniqueId: String, // transportUrl + type + id
    val transportUrl: String,
    val addonName: String,
    val catalogType: String,
    val catalogId: String,
    val catalogName: String? = null,
    val customTitle: String? = null,
    val showInHome: Boolean = false,
    val showInMovies: Boolean = false,
    val showInSeries: Boolean = false,
    val homeOrder: Int = 999,
    val moviesOrder: Int = 999,
    val seriesOrder: Int = 999,
    val isInfiniteLoopEnabled: Boolean = false,
    val visibleItemCount: Int = 15,
    val isInfiniteScrollingEnabled: Boolean = true
)
