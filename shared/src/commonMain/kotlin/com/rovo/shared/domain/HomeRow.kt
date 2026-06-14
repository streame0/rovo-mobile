package com.rovo.shared.domain

import com.rovo.shared.model.stremio.MetaItem

data class HomeRow(
    val configId: String,
    val title: String,
    val items: List<MetaItem>,
    val catalogUrl: String = "",
    val isInfiniteLoopEnabled: Boolean = false,
    val visibleItemCount: Int = 15,
    val isInfiniteScrollingEnabled: Boolean = true,
    val order: Int = 999,
    val supportsSkip: Boolean = false
)
