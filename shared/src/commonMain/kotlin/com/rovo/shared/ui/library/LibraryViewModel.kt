package com.rovo.shared.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.data.local.entity.WatchHistoryEntity
import com.rovo.shared.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val dao: AddonDao,
    private val syncRepository: com.rovo.shared.repository.SyncRepository
) : ViewModel() {

    // For now using default profile ID 1
    private val profileId = 1

    val uiState: StateFlow<LibraryUiState> = combine(
        dao.getWatchlist(profileId),
        dao.getWatchHistory(profileId)
    ) { watchlist, history ->
        LibraryUiState.Success(watchlist, history)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState.Loading
    )

    fun syncWithTrakt() {
        viewModelScope.launch {
            syncRepository.syncWatchlist(profileId)
        }
    }
}

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Success(
        val watchlist: List<WatchlistEntity>,
        val history: List<WatchHistoryEntity>
    ) : LibraryUiState
}
