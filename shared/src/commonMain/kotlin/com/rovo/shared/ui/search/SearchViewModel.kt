package com.rovo.shared.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.repository.AddonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class SearchViewModel(
    private val repository: AddonRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> = _query
        .debounce(500L)
        .flatMapLatest { q ->
            flow {
                if (q.length < 3) {
                    emit(SearchUiState.Idle)
                    return@flow
                }
                emit(SearchUiState.Loading)
                try {
                    val results = repository.searchMovies(q)
                    if (results.isEmpty()) {
                        emit(SearchUiState.Empty)
                    } else {
                        emit(SearchUiState.Success(results))
                    }
                } catch (e: Exception) {
                    emit(SearchUiState.Error(e.message ?: "Unknown error"))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState.Idle)

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data object Empty : SearchUiState
    data class Success(val results: List<MetaItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
