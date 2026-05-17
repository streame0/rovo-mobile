package com.rovo.app.features.profiles

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
private data class StoredAvatarCatalogPayload(
    val items: List<AvatarCatalogItem> = emptyList(),
)

object AvatarRepository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _avatars = MutableStateFlow<List<AvatarCatalogItem>>(emptyList())
    val avatars: StateFlow<List<AvatarCatalogItem>> = _avatars.asStateFlow()

    private var cacheHydrated = false

    suspend fun fetchAvatars() {
        hydrateFromCacheIfNeeded()
    }

    suspend fun refreshAvatars() {
        hydrateFromCacheIfNeeded()
    }

    private fun hydrateFromCacheIfNeeded() {
        if (cacheHydrated) return
        cacheHydrated = true

        val payload = AvatarStorage.loadPayload().orEmpty().trim()
        if (payload.isEmpty()) return

        val stored = runCatching {
            json.decodeFromString<StoredAvatarCatalogPayload>(payload)
        }.getOrNull() ?: return

        val items = stored.items
            .filter { it.isActive }
            .sortedWith(compareBy({ it.category }, { it.sortOrder }))
        if (items.isEmpty()) return

        _avatars.value = items
    }
}
