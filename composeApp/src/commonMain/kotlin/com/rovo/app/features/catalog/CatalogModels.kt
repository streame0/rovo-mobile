package com.rovo.app.features.catalog

import com.rovo.app.features.home.MetaPreview

data class CatalogUiState(
    val items: List<MetaPreview> = emptyList(),
    val isLoading: Boolean = false,
    val nextSkip: Int? = null,
    val errorMessage: String? = null,
) {
    val canLoadMore: Boolean
        get() = nextSkip != null
}
