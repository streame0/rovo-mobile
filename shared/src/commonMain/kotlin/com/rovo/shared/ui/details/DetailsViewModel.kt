package com.rovo.shared.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.data.local.entity.WatchHistoryEntity
import com.rovo.shared.data.local.entity.WatchlistEntity
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.MetaVideo
import com.rovo.shared.model.stremio.Stream
import com.rovo.shared.repository.*
import com.rovo.shared.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class DetailsViewModel(
    private val repository: AddonRepository,
    private val syncRepository: com.rovo.shared.repository.SyncRepository,
    private val profileRepository: com.rovo.shared.repository.ProfileRepository,
    private val debridRepository: com.rovo.shared.repository.DebridRepository,
    private val seriesRepository: com.rovo.shared.repository.SeriesRepository,
    private val tmdbRepository: TmdbRepository,
    private val dao: AddonDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val _selectedVideo = MutableStateFlow<MetaVideo?>(null)
    val selectedVideo = _selectedVideo.asStateFlow()

    private val _streams = MutableStateFlow<List<Stream>>(emptyList())
    val streams = _streams.asStateFlow()

    private val _isLoadingStreams = MutableStateFlow(false)
    val isLoadingStreams = _isLoadingStreams.asStateFlow()

    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist = _isInWatchlist.asStateFlow()

    private val _recommendations = MutableStateFlow<List<MetaItem>>(emptyList())
    val recommendations = _recommendations.asStateFlow()

    private val _trailerKey = MutableStateFlow<String?>(null)
    val trailerKey = _trailerKey.asStateFlow()

    private val _historyItem = MutableStateFlow<WatchHistoryEntity?>(null)
    val historyItem = _historyItem.asStateFlow()

    private val _profile = MutableStateFlow<ProfileEntity?>(null)
    val profile = _profile.asStateFlow()

    private var currentProfileId: Int = 1

    fun loadDetails(type: String, id: String, addonBaseUrl: String? = null) {
        // Reset state
        _selectedVideo.value = null
        _streams.value = emptyList()
        _recommendations.value = emptyList()
        _trailerKey.value = null
        _historyItem.value = null

        viewModelScope.launch {
            val profile = profileRepository.getDefaultProfile()
            currentProfileId = profile.id
            _profile.value = profile
            _uiState.value = DetailsUiState.Loading
            
            // Check watchlist status
            dao.getWatchlist(currentProfileId).collectLatest { list ->
                _isInWatchlist.value = list.any { it.id == id }
            }
        }

        viewModelScope.launch {
            try {
                val profile = profileRepository.getDefaultProfile()
                val meta = repository.getDetails(type, id, addonBaseUrl)
                _uiState.value = DetailsUiState.Success(meta)
                
                loadEnrichment(meta)

                if (type == "series") {
                    val nextUp = dao.getSeriesNextUp(id, profile.id)
                    val lastWatchedEpisode = if (nextUp != null) {
                        meta.videos?.find { it.id == nextUp.nextEpisodeId } ?: meta.videos?.firstOrNull()
                    } else {
                        val history = dao.getWatchHistory(profile.id).first()
                        val lastHistory = history.filter { it.id.startsWith(id) }.maxByOrNull { it.lastWatched }
                        meta.videos?.find { it.id == lastHistory?.id } ?: meta.videos?.firstOrNull()
                    }
                    
                    _selectedVideo.value = lastWatchedEpisode
                    lastWatchedEpisode?.let { 
                        loadStreams("series", it.id)
                        _historyItem.value = dao.getHistoryItem(it.id, profile.id)
                    }
                } else {
                    loadStreams(type, id)
                    _historyItem.value = dao.getHistoryItem(id, profile.id)
                }
            } catch (e: Exception) {
                _uiState.value = DetailsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun loadEnrichment(meta: MetaItem) {
        viewModelScope.launch {
            val profile = profileRepository.getDefaultProfile()
            if (!profile.tmdbEnabled) return@launch

            val language = profile.tmdbLanguage.ifBlank { "en-US" }
            
            launch {
                val enriched = tmdbRepository.enrich(meta, language)
                if (_uiState.value is DetailsUiState.Success) {
                    _uiState.value = DetailsUiState.Success(enriched)
                }
            }
            
            launch {
                _recommendations.value = tmdbRepository.getRecommendations(meta.type, meta.id, language)
            }
            
            launch {
                val trailer = tmdbRepository.getTrailer(meta.type, meta.id, language)
                _trailerKey.value = trailer
                if (_uiState.value is DetailsUiState.Success) {
                    val currentMeta = (_uiState.value as DetailsUiState.Success).meta
                    _uiState.value = DetailsUiState.Success(currentMeta.copy(trailerKey = trailer))
                }
            }
        }
    }

    fun selectVideo(video: MetaVideo) {
        if (_selectedVideo.value?.id == video.id) return
        _selectedVideo.value = video
        loadStreams("series", video.id)
        viewModelScope.launch {
            val profile = profileRepository.getDefaultProfile()
            _historyItem.value = dao.getHistoryItem(video.id, profile.id)
        }
    }

    fun onStreamSelected(stream: Stream, onResolved: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val resolvedUrl = debridRepository.resolveStream(stream)
                if (resolvedUrl != null) {
                    onResolved(resolvedUrl)
                } else {
                    stream.url?.let { onResolved(it) }
                }
            } catch (e: Exception) {
                stream.url?.let { onResolved(it) }
            }
        }
    }

    private fun loadStreams(type: String, id: String) {
        viewModelScope.launch {
            _isLoadingStreams.value = true
            try {
                _streams.value = repository.getStreams(type, id)
            } catch (e: Exception) {
                _streams.value = emptyList()
            } finally {
                _isLoadingStreams.value = false
            }
        }
    }

    fun toggleWatchlist() {
        val state = _uiState.value as? DetailsUiState.Success ?: return
        val meta = state.meta
        viewModelScope.launch {
            val profile = profileRepository.getDefaultProfile()
            if (_isInWatchlist.value) {
                dao.removeFromWatchlist(meta.id, profile.id)
                syncRepository.removeFromTraktWatchlist(profile.id, meta.type, meta.id)
            } else {
                dao.addToWatchlist(
                    WatchlistEntity(
                        id = meta.id,
                        profileId = profile.id,
                        type = meta.type,
                        title = meta.name,
                        poster = meta.poster ?: "",
                        addedAt = Clock.System.now().toEpochMilliseconds()
                    )
                )
                syncRepository.addToTraktWatchlist(profile.id, meta.type, meta.id)
            }
        }
    }

    fun clearProgress() {
        val meta = (uiState.value as? DetailsUiState.Success)?.meta ?: return
        val video = selectedVideo.value
        viewModelScope.launch {
            val profile = profileRepository.getDefaultProfile()
            val itemId = video?.id ?: meta.id
            dao.deleteHistory(itemId, profile.id)
            _historyItem.value = null
            
            // Sync with Trakt
            syncRepository.clearTraktProgress(profile.id, meta.type, itemId)
        }
    }

    fun updateHistory(position: Long, duration: Long, isFinished: Boolean = false) {
        val state = _uiState.value as? DetailsUiState.Success ?: return
        val meta = state.meta
        val video = _selectedVideo.value
        
        viewModelScope.launch {
            val profile = profileRepository.getDefaultProfile()
            val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
            val itemId = video?.id ?: meta.id
            
            dao.upsertHistory(
                WatchHistoryEntity(
                    id = itemId,
                    profileId = profile.id,
                    title = if (video != null) "${meta.name} - ${video.title}" else meta.name,
                    poster = video?.thumbnail ?: meta.poster ?: "",
                    type = meta.type,
                    position = position,
                    duration = duration,
                    lastWatched = Clock.System.now().toEpochMilliseconds(),
                    watched = isFinished || progress > 0.9f
                )
            )

            if (isFinished && meta.type == "series" && video != null) {
                seriesRepository.updateNextUp(profile.id, meta, video)
            }

            if (profile.traktToken != null) {
                val action = if (isFinished) "stop" else "pause"
                syncRepository.scrobbleToTrakt(profile, meta.type, itemId, progress, action)
            }
        }
    }

    fun toggleProfileSetting(update: (ProfileEntity) -> ProfileEntity) {
        val currentProfile = _profile.value ?: return
        val newProfile = update(currentProfile)
        _profile.value = newProfile
        viewModelScope.launch {
            profileRepository.saveProfile(newProfile)
        }
    }
}

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val meta: MetaItem) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}
