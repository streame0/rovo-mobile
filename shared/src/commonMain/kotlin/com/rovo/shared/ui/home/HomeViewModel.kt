package com.rovo.shared.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rovo.shared.domain.HomeRow
import com.rovo.shared.repository.AddonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.rovo.shared.model.stremio.MetaItem
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch

class HomeViewModel(
    private val repository: AddonRepository,
    private val profileRepository: com.rovo.shared.repository.ProfileRepository,
    private val seriesRepository: com.rovo.shared.repository.SeriesRepository,
    private val syncRepository: com.rovo.shared.repository.SyncRepository,
    private val dao: com.rovo.shared.data.local.dao.AddonDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        println("HomeViewModel: loadHome started")
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Ensure Cinemeta is installed as a default addon
                val addons = dao.getAllAddons().first()
                println("HomeViewModel: Found ${addons.size} addons")
                if (addons.none { it.transportUrl.contains("cinemeta") }) {
                    println("HomeViewModel: Cinemeta not found, installing...")
                    repository.installAddon("https://v3-cinemeta.strem.io/manifest.json", isTrusted = true)
                    println("HomeViewModel: Cinemeta installation triggered")
                }
                
                val profile = profileRepository.getDefaultProfile()
                println("HomeViewModel: Using profile: ${profile.name} (id=${profile.id})")
                syncRepository.syncWatchlist(profile.id)

                println("HomeViewModel: Starting dashboard flow collection")
                combine(
                    repository.getDashboardRowsFlow("home"),
                    dao.getWatchHistory(profile.id),
                    seriesRepository.getNextUp(profile.id)
                ) { addonRows, history, nextUp ->
                    println("HomeViewModel: Flow emitted: addonRows=${addonRows.size}, history=${history.size}, nextUp=${nextUp.size}")
                    val finalRows = mutableListOf<HomeRow>()

                    if (history.isNotEmpty()) {
                        finalRows.add(
                            HomeRow(
                                configId = "continue_watching",
                                title = "Continue Watching",
                                items = history.filter { !it.watched }.map { 
                                    MetaItem(
                                        id = it.id,
                                        name = it.title,
                                        poster = it.poster,
                                        type = it.type,
                                        progress = it.progress()
                                    )
                                }
                            )
                        )
                    }

                    if (nextUp.isNotEmpty()) {
                        finalRows.add(
                            HomeRow(
                                configId = "next_up",
                                title = "Next Up",
                                items = nextUp.map {
                                    MetaItem(
                                        id = it.seriesId,
                                        name = it.seriesTitle,
                                        poster = it.seriesPoster,
                                        type = "series",
                                        hasNewEpisode = it.isNewEpisode
                                    )
                                }
                            )
                        )
                    }

                    finalRows.addAll(addonRows)
                    HomeUiState.Success(finalRows)
                }.catch { e ->
                    println("HomeViewModel: Dashboard flow error: ${e.message}")
                    _uiState.value = HomeUiState.Error(e.message ?: "Stream error")
                }.collect { state ->
                    val rowCount = if (state is HomeUiState.Success) state.rows.size else 0
                    println("HomeViewModel: Setting state: ${state::class.simpleName} with $rowCount rows")
                    _uiState.value = state
                }
            } catch (e: Exception) {
                println("HomeViewModel: Initialization error: ${e.message}")
                _uiState.value = HomeUiState.Error(e.message ?: "Initialization error")
            }
        }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val rows: List<HomeRow>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
