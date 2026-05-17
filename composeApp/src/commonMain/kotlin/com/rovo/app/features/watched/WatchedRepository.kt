package com.rovo.app.features.watched

import co.touchlab.kermit.Logger
import com.rovo.app.features.details.MetaDetails
import com.rovo.app.features.profiles.ProfileRepository
import com.rovo.app.features.trakt.TraktAuthRepository
import com.rovo.app.features.trakt.TraktSettingsRepository
import com.rovo.app.features.trakt.WatchProgressSource
import com.rovo.app.features.trakt.shouldUseTraktProgress
import com.rovo.app.features.watching.sync.TraktWatchedSyncAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class StoredWatchedPayload(
    val items: List<WatchedItem> = emptyList(),
)

object WatchedRepository {
    private const val watchedItemsPageSize = 900

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = Logger.withTag("WatchedRepository")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _uiState = MutableStateFlow(WatchedUiState())
    val uiState: StateFlow<WatchedUiState> = _uiState.asStateFlow()

    private var hasLoaded = false
    private var currentProfileId: Int = 1
    private var itemsByKey: MutableMap<String, WatchedItem> = mutableMapOf()

    fun ensureLoaded() {
        if (hasLoaded) return
        loadFromDisk(ProfileRepository.activeProfileId)
    }

    fun onProfileChanged(profileId: Int) {
        if (profileId == currentProfileId && hasLoaded) return
        loadFromDisk(profileId)
    }

    fun clearLocalState() {
        hasLoaded = false
        currentProfileId = 1
        itemsByKey.clear()
        _uiState.value = WatchedUiState()
    }

    private fun loadFromDisk(profileId: Int) {
        currentProfileId = profileId
        hasLoaded = true
        itemsByKey.clear()

        val payload = WatchedStorage.loadPayload(profileId).orEmpty().trim()
        if (payload.isNotEmpty()) {
            val items = runCatching {
                json.decodeFromString<StoredWatchedPayload>(payload).items
            }.getOrDefault(emptyList())
            itemsByKey = items
                .map(WatchedItem::normalizedMarkedAt)
                .associateBy { watchedItemKey(it.type, it.id, it.season, it.episode) }
                .toMutableMap()
        }

        publish()
    }

    suspend fun pullFromServer(profileId: Int) {
        TraktAuthRepository.ensureLoaded()
        TraktSettingsRepository.ensureLoaded()
        currentProfileId = profileId
        if (shouldUseTraktWatchedSync()) {
            runCatching {
                val serverItems = TraktWatchedSyncAdapter.pull(
                    profileId = profileId,
                    pageSize = watchedItemsPageSize,
                )
                itemsByKey = serverItems
                    .map(WatchedItem::normalizedMarkedAt)
                    .associateBy { watchedItemKey(it.type, it.id, it.season, it.episode) }
                    .toMutableMap()
                hasLoaded = true
                publish()
                persist()
            }.onFailure { e ->
                log.e(e) { "Failed to pull watched items from Trakt" }
            }
        }
    }

    fun toggleWatched(item: WatchedItem) {
        ensureLoaded()
        val key = watchedItemKey(item.type, item.id, item.season, item.episode)
        if (itemsByKey.containsKey(key)) {
            unmarkWatched(item)
        } else {
            markWatched(item)
        }
    }

    fun markWatched(item: WatchedItem) {
        markWatched(listOf(item))
    }

    fun markWatched(items: Collection<WatchedItem>) {
        ensureLoaded()
        if (items.isEmpty()) return
        val markedAt = WatchedClock.nowEpochMs()
        val timestampedItems = items.map { watchedItem ->
            watchedItem.copy(markedAtEpochMs = markedAt)
        }
        timestampedItems.forEach { watchedItem ->
            val key = watchedItemKey(watchedItem.type, watchedItem.id, watchedItem.season, watchedItem.episode)
            itemsByKey[key] = watchedItem
        }
        publish()
        persist()
        pushToTrakt(timestampedItems)
    }

    fun unmarkWatched(item: WatchedItem) {
        unmarkWatched(listOf(item))
    }

    fun unmarkWatched(
        id: String,
        type: String,
        season: Int? = null,
        episode: Int? = null,
    ) {
        unmarkWatched(
            listOf(
                WatchedItem(
                    id = id,
                    type = type,
                    name = "",
                    season = season,
                    episode = episode,
                    markedAtEpochMs = 0L,
                ),
            ),
        )
    }

    fun unmarkWatched(items: Collection<WatchedItem>) {
        ensureLoaded()
        if (items.isEmpty()) return
        val removedItems = items.mapNotNull { watchedItem ->
            itemsByKey.remove(watchedItemKey(watchedItem.type, watchedItem.id, watchedItem.season, watchedItem.episode))
        }
        if (removedItems.isNotEmpty()) {
            publish()
            persist()
            deleteFromTrakt(removedItems)
        }
    }

    fun isWatched(
        id: String,
        type: String,
        season: Int? = null,
        episode: Int? = null,
    ): Boolean {
        ensureLoaded()
        return itemsByKey.containsKey(watchedItemKey(type, id, season, episode))
    }

    fun reconcileSeriesWatchedState(
        meta: MetaDetails,
        todayIsoDate: String,
        isEpisodeCompleted: (com.rovo.app.features.details.MetaVideo) -> Boolean = { false },
    ) {
        ensureLoaded()
        val shouldMarkSeriesWatched = meta.hasWatchedAllMainSeasonEpisodes(todayIsoDate) { episode ->
            isWatched(
                id = meta.id,
                type = meta.type,
                season = episode.season,
                episode = episode.episode,
            ) || isEpisodeCompleted(episode)
        }
        val seriesWatchedItem = meta.toSeriesWatchedItem()
        if (shouldMarkSeriesWatched) {
            if (!isWatched(id = meta.id, type = meta.type)) {
                markWatched(seriesWatchedItem)
            }
        } else if (isWatched(id = meta.id, type = meta.type)) {
            unmarkWatched(seriesWatchedItem)
        }
    }

    private fun pushToTrakt(items: Collection<WatchedItem>) {
        if (!TraktAuthRepository.isAuthenticated.value) return
        syncScope.launch {
            runCatching {
                if (items.isEmpty()) return@runCatching
                TraktWatchedSyncAdapter.push(profileId = ProfileRepository.activeProfileId, items = items)
            }.onFailure { e ->
                log.e(e) { "Failed to push watched items to Trakt" }
            }
        }
    }

    private fun deleteFromTrakt(items: Collection<WatchedItem>) {
        if (!TraktAuthRepository.isAuthenticated.value) return
        syncScope.launch {
            runCatching {
                if (items.isEmpty()) return@runCatching
                TraktWatchedSyncAdapter.delete(profileId = ProfileRepository.activeProfileId, items = items)
            }.onFailure { e ->
                log.e(e) { "Failed to delete watched items from Trakt" }
            }
        }
    }

    private fun publish() {
        val items = itemsByKey.values
            .map(WatchedItem::normalizedMarkedAt)
            .sortedByDescending { it.markedAtEpochMs }
        _uiState.value = WatchedUiState(
            items = items,
            watchedKeys = items.mapTo(linkedSetOf()) {
                watchedItemKey(it.type, it.id, it.season, it.episode)
            },
            isLoaded = true,
        )
    }

    private fun persist() {
        WatchedStorage.savePayload(
            currentProfileId,
            json.encodeToString(
                StoredWatchedPayload(
                    items = itemsByKey.values
                        .map(WatchedItem::normalizedMarkedAt)
                        .sortedByDescending { it.markedAtEpochMs },
                ),
            ),
        )
    }

    private fun shouldUseTraktWatchedSync(): Boolean =
        shouldUseTraktWatchedSync(
            isAuthenticated = TraktAuthRepository.isAuthenticated.value,
            source = TraktSettingsRepository.uiState.value.watchProgressSource,
        )
}

internal fun shouldUseTraktWatchedSync(
    isAuthenticated: Boolean,
    source: WatchProgressSource,
): Boolean = shouldUseTraktProgress(
    isAuthenticated = isAuthenticated,
    source = source,
)
