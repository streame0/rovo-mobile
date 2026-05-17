package com.rovo.app.features.library

import co.touchlab.kermit.Logger
import com.rovo.app.features.profiles.ProfileRepository
import com.rovo.app.features.trakt.TraktAuthRepository
import com.rovo.app.features.trakt.TraktLibraryRepository
import com.rovo.app.features.trakt.TraktListTab
import com.rovo.app.features.trakt.TraktListType
import com.rovo.app.features.trakt.TraktMembershipChanges
import com.rovo.app.features.trakt.TraktSettingsRepository
import com.rovo.app.features.trakt.effectiveLibrarySourceMode as resolveEffectiveLibrarySourceMode
import com.rovo.app.features.trakt.shouldUseTraktLibrary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class StoredLibraryPayload(
    val items: List<LibraryItem> = emptyList(),
)

object LibraryRepository {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val log = Logger.withTag("LibraryRepository")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var hasLoaded = false
    private var currentProfileId: Int = 1
    private var itemsById: MutableMap<String, LibraryItem> = mutableMapOf()

    init {
        syncScope.launch {
            TraktAuthRepository.isAuthenticated.collectLatest { authenticated ->
                if (authenticated) {
                    TraktLibraryRepository.preloadListTabsAsync()
                    if (shouldUseTraktLibrary(authenticated, selectedLibrarySourceMode())) {
                        runCatching { TraktLibraryRepository.refreshNow() }
                            .onFailure { log.e(it) { "Failed to refresh Trakt library after auth change" } }
                    }
                }
                publish()
            }
        }
        syncScope.launch {
            TraktSettingsRepository.uiState
                .map { it.librarySourceMode }
                .distinctUntilChanged()
                .collectLatest { source ->
                    if (shouldUseTraktLibrary(TraktAuthRepository.isAuthenticated.value, source)) {
                        TraktLibraryRepository.preloadListTabsAsync()
                        publish()
                        refreshTraktLibraryAsync()
                    } else {
                        publish()
                    }
                }
        }
        syncScope.launch {
            TraktLibraryRepository.uiState.collectLatest {
                if (TraktAuthRepository.isAuthenticated.value) {
                    publish()
                }
            }
        }
    }

    fun ensureLoaded() {
        TraktAuthRepository.ensureLoaded()
        TraktSettingsRepository.ensureLoaded()
        TraktLibraryRepository.ensureLoaded()
        if (hasLoaded) return
        loadFromDisk(ProfileRepository.activeProfileId)
        if (TraktAuthRepository.isAuthenticated.value) {
            TraktLibraryRepository.preloadListTabsAsync()
            if (isTraktLibrarySourceActive()) {
                refreshTraktLibraryAsync()
            }
        }
    }

    fun onProfileChanged(profileId: Int) {
        if (profileId == currentProfileId && hasLoaded) return
        TraktSettingsRepository.onProfileChanged()
        loadFromDisk(profileId)
        TraktAuthRepository.onProfileChanged()
        TraktLibraryRepository.onProfileChanged()
        if (TraktAuthRepository.isAuthenticated.value) {
            TraktLibraryRepository.preloadListTabsAsync()
            if (isTraktLibrarySourceActive()) {
                refreshTraktLibraryAsync()
            }
        }
    }

    fun clearLocalState() {
        hasLoaded = false
        currentProfileId = 1
        itemsById.clear()
        TraktAuthRepository.clearLocalState()
        TraktLibraryRepository.clearLocalState()
        _uiState.value = LibraryUiState()
    }

    private fun loadFromDisk(profileId: Int) {
        currentProfileId = profileId
        hasLoaded = true
        itemsById.clear()

        val payload = LibraryStorage.loadPayload(profileId).orEmpty().trim()
        if (payload.isNotEmpty()) {
            val items = runCatching {
                json.decodeFromString<StoredLibraryPayload>(payload).items
            }.getOrDefault(emptyList())
            itemsById = items.associateBy { it.id }.toMutableMap()
        }

        publish()
    }

    fun toggleSaved(item: LibraryItem) {
        ensureLoaded()

        if (isTraktLibrarySourceActive()) {
            syncScope.launch {
                runCatching { TraktLibraryRepository.toggleWatchlist(item) }
                    .onFailure { e -> log.e(e) { "Failed to toggle Trakt watchlist" } }
                publish()
            }
            return
        }

        if (itemsById.containsKey(item.id)) {
            remove(item.id)
        } else {
            save(item)
        }
    }

    fun save(item: LibraryItem) {
        ensureLoaded()
        itemsById[item.id] = item.copy(savedAtEpochMs = LibraryClock.nowEpochMs())
        publish()
        persist()
    }

    fun remove(id: String) {
        ensureLoaded()
        if (itemsById.remove(id) != null) {
            publish()
            persist()
        }
    }

    fun isSaved(id: String, type: String? = null): Boolean {
        ensureLoaded()

        if (isTraktLibrarySourceActive()) {
            if (type != null) {
                return TraktLibraryRepository.isInAnyList(id, type)
            }
            val entry = TraktLibraryRepository.uiState.value.allItems.firstOrNull { it.id == id }
            if (entry != null) {
                return TraktLibraryRepository.isInAnyList(entry.id, entry.type)
            }
            return false
        }

        return itemsById.containsKey(id)
    }

    fun savedItem(id: String): LibraryItem? {
        ensureLoaded()

        if (isTraktLibrarySourceActive()) {
            return TraktLibraryRepository.uiState.value.allItems.firstOrNull { it.id == id }
        }

        return itemsById[id]
    }

    fun libraryListTabs(): List<TraktListTab> {
        val traktTabs = if (TraktAuthRepository.isAuthenticated.value) {
            TraktLibraryRepository.currentListTabs()
        } else {
            emptyList()
        }
        return libraryTabsWithLocal(traktTabs)
    }

    fun traktListTabs(): List<TraktListTab> = libraryListTabs()

    suspend fun getMembershipSnapshot(item: LibraryItem): Map<String, Boolean> {
        ensureLoaded()
        val inLocal = itemsById.containsKey(item.id)
        if (TraktAuthRepository.isAuthenticated.value) {
            val traktMembership = TraktLibraryRepository.getMembershipSnapshot(item).listMembership
            return libraryMembershipWithLocal(
                inLocal = inLocal,
                traktMembership = traktMembership,
            )
        }
        return libraryMembershipWithLocal(inLocal = inLocal)
    }

    suspend fun applyMembershipChanges(item: LibraryItem, desiredMembership: Map<String, Boolean>) {
        ensureLoaded()
        val localDesired = desiredMembership[LOCAL_LIBRARY_LIST_KEY] == true
        val currentlyInLocal = itemsById.containsKey(item.id)
        if (localDesired != currentlyInLocal) {
            if (localDesired) {
                save(item)
            } else {
                remove(item.id)
            }
        }

        if (TraktAuthRepository.isAuthenticated.value) {
            val traktMembership = desiredMembership.filterKeys { it != LOCAL_LIBRARY_LIST_KEY }
            if (traktMembership.isNotEmpty()) {
                TraktLibraryRepository.applyMembershipChanges(
                    item = item,
                    changes = TraktMembershipChanges(desiredMembership = traktMembership),
                )
            }
            publish()
        } else {
            publish()
        }
    }

    suspend fun removeFromList(item: LibraryItem, listKey: String) {
        val desiredMembership = libraryMembershipWithRemovedList(
            currentMembership = getMembershipSnapshot(item),
            listKey = listKey,
        )
        applyMembershipChanges(item, desiredMembership)
    }

    private fun publish() {
        if (isTraktLibrarySourceActive()) {
            val traktState = TraktLibraryRepository.uiState.value
            val sections = traktState.listTabs.mapNotNull { tab ->
                val listItems = traktState.entriesByList[tab.key].orEmpty()
                if (listItems.isEmpty()) {
                    null
                } else {
                    LibrarySection(
                        type = tab.key,
                        displayTitle = tab.title,
                        items = listItems,
                    )
                }
            }

            _uiState.value = LibraryUiState(
                sourceMode = LibrarySourceMode.TRAKT,
                items = traktState.allItems,
                sections = sections,
                isLoaded = traktState.hasLoaded,
                isLoading = traktState.isLoading,
                errorMessage = traktState.errorMessage,
            )
            return
        }

        val items = itemsById.values
            .sortedByDescending { it.savedAtEpochMs }
        val sections = items
            .groupBy { it.type }
            .map { (type, typeItems) ->
                LibrarySection(
                    type = type,
                    displayTitle = type.toLibraryDisplayTitle(),
                    items = typeItems.sortedByDescending { it.savedAtEpochMs },
                )
            }
            .sortedBy { it.displayTitle }

        _uiState.value = LibraryUiState(
            sourceMode = LibrarySourceMode.LOCAL,
            items = items,
            sections = sections,
            isLoaded = true,
            isLoading = false,
            errorMessage = null,
        )
    }

    private fun persist() {
        LibraryStorage.savePayload(
            currentProfileId,
            json.encodeToString(
                StoredLibraryPayload(
                    items = itemsById.values.sortedByDescending { it.savedAtEpochMs },
                ),
            ),
        )
    }

    private fun refreshTraktLibraryAsync() {
        syncScope.launch {
            runCatching { TraktLibraryRepository.refreshNow() }
                .onFailure { e -> log.e(e) { "Failed to refresh Trakt library" } }
            publish()
        }
    }

    private fun selectedLibrarySourceMode(): LibrarySourceMode {
        TraktSettingsRepository.ensureLoaded()
        return TraktSettingsRepository.uiState.value.librarySourceMode
    }

    private fun effectiveLibrarySourceMode(): LibrarySourceMode =
        resolveEffectiveLibrarySourceMode(
            isAuthenticated = TraktAuthRepository.isAuthenticated.value,
            source = selectedLibrarySourceMode(),
        )

    private fun isTraktLibrarySourceActive(): Boolean =
        effectiveLibrarySourceMode() == LibrarySourceMode.TRAKT
}

internal const val LOCAL_LIBRARY_LIST_KEY = "local"
internal const val LOCAL_LIBRARY_LIST_TITLE = "Rovo Library"

internal fun localLibraryListTab(): TraktListTab =
    TraktListTab(
        key = LOCAL_LIBRARY_LIST_KEY,
        title = LOCAL_LIBRARY_LIST_TITLE,
        type = TraktListType.WATCHLIST,
    )

internal fun libraryTabsWithLocal(traktTabs: List<TraktListTab>): List<TraktListTab> =
    listOf(localLibraryListTab()) + traktTabs

internal fun libraryMembershipWithLocal(
    inLocal: Boolean,
    traktMembership: Map<String, Boolean> = emptyMap(),
): Map<String, Boolean> =
    linkedMapOf<String, Boolean>(LOCAL_LIBRARY_LIST_KEY to inLocal).apply {
        putAll(traktMembership)
    }

internal fun libraryMembershipWithRemovedList(
    currentMembership: Map<String, Boolean>,
    listKey: String,
): Map<String, Boolean> =
    currentMembership.toMutableMap().apply {
        this[listKey] = false
    }

internal fun String.toLibraryDisplayTitle(): String {
    val normalized = trim()
    if (normalized.isBlank()) return "Other"

    return normalized
        .split('-', '_', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { char -> char.uppercase() }
        }
        .ifBlank { "Other" }
}
