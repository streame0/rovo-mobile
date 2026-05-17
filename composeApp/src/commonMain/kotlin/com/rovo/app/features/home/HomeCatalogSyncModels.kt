package com.rovo.app.features.home

import kotlinx.serialization.Serializable

@Serializable
data class SyncCatalogItem(
    val addonId: String,
    val type: String,
    val catalogId: String,
    val enabled: Boolean,
    val order: Int,
    val customTitle: String = "",
    val isCollection: Boolean = false,
    val collectionId: String? = null,
)

@Serializable
data class SyncHomeCatalogPayload(
    val hideUnreleasedContent: Boolean,
    val hideCatalogUnderline: Boolean,
    val items: List<SyncCatalogItem>,
)
