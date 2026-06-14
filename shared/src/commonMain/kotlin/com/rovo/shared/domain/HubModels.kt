package com.rovo.shared.domain

import com.rovo.shared.model.stremio.MetaItem

enum class HubShape(val aspectRatio: Float) {
    HORIZONTAL(16f / 9f),
    VERTICAL(2f / 3f),
    SQUARE(1f)
}

data class HubItem(
    val id: String,
    val title: String,
    val categoryId: String,
    val customImageUrl: String? = null
)

interface HomeRowItem {
    val id: String
    val title: String
    val order: Int
}

data class CategoryRow(
    override val id: String,
    override val title: String,
    override val order: Int,
    val items: List<MetaItem>,
    val isInfiniteLoopEnabled: Boolean = false,
    val visibleItemCount: Int = 15,
    val isInfiniteScrollingEnabled: Boolean = true
) : HomeRowItem {
    companion object {
        fun fromHomeRow(homeRow: HomeRow): CategoryRow {
            return CategoryRow(
                id = homeRow.configId,
                title = homeRow.title,
                order = homeRow.order,
                items = homeRow.items,
                isInfiniteLoopEnabled = homeRow.isInfiniteLoopEnabled,
                visibleItemCount = homeRow.visibleItemCount,
                isInfiniteScrollingEnabled = homeRow.isInfiniteScrollingEnabled
            )
        }
    }
}

data class HubGroupRow(
    override val id: String,
    override val title: String,
    override val order: Int,
    val items: List<HubItem>,
    val shape: HubShape = HubShape.HORIZONTAL
) : HomeRowItem
