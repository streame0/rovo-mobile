package com.rovo.shared.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rovo.shared.api.TorrServerApi
import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.data.local.entity.ProfileEntity
import com.rovo.shared.data.local.entity.WatchHistoryEntity
import com.rovo.shared.data.player.PlaybackTrackSelectionStore
import com.rovo.shared.data.player.SourceSelectionStore
import com.rovo.shared.domain.*
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.MetaVideo
import com.rovo.shared.model.StreamQuality
import com.rovo.shared.model.stremio.Stream
import com.rovo.shared.model.torrent.TorrentProgress
import com.rovo.shared.repository.*
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class PendingSourceSelection(
    val playbackId: String,
    val launchedStream: Stream,
    val candidateStreams: List<Stream>
)

data class PendingEpisodeSwitch(
    val playbackId: String,
    val playbackTitle: String,
    val streams: List<Stream>?,
    val addonSubs: List<AddonSubtitle>,
    val playerCurrentSourceUrl: String?,
    val videoNumber: Int? = null
)

data class PlayerState(
    val selectedPlayerSubtitles: List<PlayerSubtitlePayload> = emptyList(),
    val selectedPlayerAudioTracks: List<PlayerAudioTrackPayload> = emptyList(),
    val selectedPlayerSources: List<PlayerSourceOption> = emptyList(),
    val pendingSourceSelection: PendingSourceSelection? = null,
    val currentStream: Stream? = null,
    val pendingEpisodeSwitch: PendingEpisodeSwitch? = null,
    val isEpisodeSwitchLoading: Boolean = false,
    val nextEpisode: MetaVideo? = null,
    val activeSubtitleId: String? = null,
    val activeAudioTrackId: String? = null,
    val currentPosition: Long = 0,
    val pendingSeek: Long? = null,
    val showSkipButton: Boolean = false,
    val autoplayCountdown: Int? = null,
    val torrentProgress: TorrentProgress? = null,
    val playbackSpeed: Float = 1.0f,
    val subtitleOffset: Int = 0 // in milliseconds
)

class PlayerViewModel(
    private val dao: AddonDao,
    private val syncRepository: SyncRepository,
    private val profileRepository: ProfileRepository,
    private val addonRepository: AddonRepository,
    private val debridRepository: DebridRepository,
    private val subtitleRepository: SubtitleRepository,
    private val streamSortingService: StreamSortingService,
    private val trackSelectionStore: PlaybackTrackSelectionStore,
    private val sourceSelectionStore: SourceSelectionStore,
    private val introRepository: IntroRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state = _state.asStateFlow()

    private var meta: MetaItem? = null
    private var video: MetaVideo? = null
    private var introStart: Long? = null
    private var introEnd: Long? = null
    private var outroStart: Long? = null
    private var lastSelectedAddonUrl: String? = null
    private var autoplayJob: Job? = null

    fun onSkipPressed() {
        val currentProgress = _state.value.currentPosition
        if (introEnd != null && currentProgress in (introStart ?: 0)..(introEnd ?: 0)) {
            _state.update { it.copy(pendingSeek = introEnd) }
        } else if (outroStart != null && currentProgress >= outroStart!!) {
            val next = _state.value.nextEpisode
            if (next != null) {
                startNextEpisode(next)
            }
        }
    }
    private var lastSelectedQuality: StreamQuality = StreamQuality.UNKNOWN
    private val profileId = 1 // Default profile

    private val _profile = MutableStateFlow<ProfileEntity?>(null)
    val profile = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            _profile.value = profileRepository.getProfile(profileId)
        }
    }

    fun setPlaybackInfo(meta: MetaItem, video: MetaVideo?, stream: Stream) {
        this.meta = meta
        this.video = video
        this.lastSelectedAddonUrl = stream.addonTransportUrl
        this.lastSelectedQuality = StreamParser.parse(stream).quality
        
        _state.update { it.copy(currentStream = stream) }
        
        loadMetadata(meta, video, stream)
    }

    private fun loadMetadata(meta: MetaItem, video: MetaVideo?, stream: Stream) {
        val playbackId = video?.id ?: meta.id
        
        viewModelScope.launch {
            val segments = introRepository.getSegments(
                imdbId = meta.id.split(":").first(),
                season = video?.season ?: 0,
                episode = video?.episode ?: 0
            )
            introStart = segments?.intro?.start_ms
            introEnd = segments?.intro?.end_ms
            outroStart = segments?.outro?.start_ms

            val addonSubs = subtitleRepository.getSubtitles(meta.type, playbackId)
            val allStreams = addonRepository.getStreams(meta.type, playbackId)
            
            val subtitlePayload = buildSubtitlePayload(stream, addonSubs)
            val sourcePayload = buildSourcePayload(allStreams, stream)
            
            val savedSelection = trackSelectionStore.getSelection(playbackId)
            val activeSubId = savedSelection?.subtitleTrackId ?: subtitlePayload.firstOrNull { it.isDefault }?.id
            val activeAudioId = savedSelection?.audioTrackId

            _state.update { 
                it.copy(
                    selectedPlayerSubtitles = subtitlePayload,
                    selectedPlayerSources = sourcePayload,
                    activeSubtitleId = activeSubId,
                    activeAudioTrackId = activeAudioId,
                    nextEpisode = if (meta.type == "series") findNextEpisode(meta.id, playbackId, meta.videos ?: emptyList()) else null
                )
            }
        }
    }

    fun updateTorrentProgress(progress: TorrentProgress?) {
        _state.update { it.copy(torrentProgress = progress) }
    }

    fun onSourceSelected(option: PlayerSourceOption) {
        viewModelScope.launch {
            val playbackId = video?.id ?: meta?.id ?: return@launch
            val allStreams = addonRepository.getStreams(meta?.type ?: "", playbackId)
            val selectedStream = allStreams.find { resolvePlayableSourceUrl(it) == option.url }
            
            if (selectedStream != null) {
                lastSelectedAddonUrl = selectedStream.addonTransportUrl
                lastSelectedQuality = StreamParser.parse(selectedStream).quality
                sourceSelectionStore.rememberSelection(playbackId, selectedStream)
                
                val resolvedUrl = debridRepository.resolveStream(selectedStream)
                if (resolvedUrl != null) {
                    _state.update { 
                        it.copy(
                            currentStream = selectedStream,
                            pendingEpisodeSwitch = PendingEpisodeSwitch(
                                playbackId = playbackId,
                                playbackTitle = video?.let { episodeDisplayTitle(it) } ?: meta?.name ?: "",
                                streams = allStreams,
                                addonSubs = emptyList(),
                                playerCurrentSourceUrl = resolvedUrl,
                                videoNumber = video?.episode
                            )
                        )
                    }
                    loadMetadata(meta!!, video, selectedStream)
                }
            }
        }
    }

    fun onSubtitleSelected(subtitle: PlayerSubtitlePayload) {
        val playbackId = video?.id ?: meta?.id ?: return
        viewModelScope.launch {
            trackSelectionStore.updateSelection(
                playbackId = playbackId,
                audioTrackId = null,
                subtitleTrackId = subtitle.id,
                updateAudio = false,
                updateSubtitle = true
            )
        }
        _state.update { it.copy(activeSubtitleId = subtitle.id) }
    }

    fun onAudioTrackSelected(audioTrack: PlayerAudioTrackPayload) {
        val playbackId = video?.id ?: meta?.id ?: return
        viewModelScope.launch {
            trackSelectionStore.updateSelection(
                playbackId = playbackId,
                audioTrackId = audioTrack.id,
                subtitleTrackId = null,
                updateAudio = true,
                updateSubtitle = false
            )
        }
        _state.update { it.copy(activeAudioTrackId = audioTrack.id) }
    }

    fun onPlaybackSpeedChanged(speed: Float) {
        _state.update { it.copy(playbackSpeed = speed) }
    }

    fun onSubtitleOffsetChanged(offset: Int) {
        _state.update { it.copy(subtitleOffset = offset) }
        viewModelScope.launch {
            _profile.value?.let { profile ->
                profileRepository.saveProfile(profile.copy(subtitleOffset = offset))
            }
        }
    }

    fun setAvailableAudioTracks(tracks: List<PlayerAudioTrackPayload>) {
        val playbackId = video?.id ?: meta?.id ?: return
        viewModelScope.launch {
            val savedSelection = trackSelectionStore.getSelection(playbackId)
            val activeAudioId = savedSelection?.audioTrackId ?: tracks.find { it.isDefault }?.id ?: tracks.firstOrNull()?.id
            
            _state.update { 
                it.copy(
                    selectedPlayerAudioTracks = tracks,
                    activeAudioTrackId = activeAudioId
                )
            }
        }
    }

    fun onSeekComplete() {
        _state.update { it.copy(pendingSeek = null) }
    }

    fun updateProgress(position: Long, duration: Long, isFinished: Boolean = false) {
        val currentMeta = meta ?: return
        
        val showSkip = (introEnd != null && position in (introStart ?: 0)..(introEnd ?: 0)) ||
                       (outroStart != null && position >= outroStart!!)
                       
        _state.update { it.copy(currentPosition = position, showSkipButton = showSkip) }

        viewModelScope.launch {
            val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
            val itemId = video?.id ?: currentMeta.id
            val currentVideo = video
            
            dao.upsertHistory(
                WatchHistoryEntity(
                    id = itemId,
                    profileId = profileId,
                    title = if (currentVideo != null) "${currentMeta.name} - ${currentVideo.title}" else currentMeta.name,
                    poster = currentVideo?.thumbnail ?: currentMeta.poster,
                    type = currentMeta.type,
                    position = position,
                    duration = duration,
                    lastWatched = Clock.System.now().toEpochMilliseconds(),
                    watched = isFinished || progress > 0.9f
                )
            )

            val profile = profileRepository.getProfile(profileId)
            if (profile?.traktToken != null) {
                val action = if (isFinished) "stop" else "pause"
                syncRepository.scrobbleToTrakt(profile, currentMeta.type, itemId, progress, action)
            }
            
            if (isFinished && currentMeta.type == "series") {
                checkAutoplay()
            }
        }
    }

    private fun checkAutoplay() {
        val next = _state.value.nextEpisode ?: return
        val profile = _profile.value ?: return
        
        if (profile.autoplayNextEpisode) {
            startAutoplayCountdown(next)
        }
    }

    private fun startAutoplayCountdown(next: MetaVideo) {
        autoplayJob?.cancel()
        autoplayJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                _state.update { it.copy(autoplayCountdown = i) }
                delay(1000)
            }
            _state.update { it.copy(autoplayCountdown = null) }
            startNextEpisodeInternal(next)
        }
    }

    fun cancelAutoplay() {
        autoplayJob?.cancel()
        _state.update { it.copy(autoplayCountdown = null) }
    }

    fun getActiveSubtitle(): PlayerSubtitlePayload? {
        val id = _state.value.activeSubtitleId ?: return null
        return _state.value.selectedPlayerSubtitles.find { it.id == id }
    }

    suspend fun resolveMagnetUrl(magnetLink: String, fileIdx: Int): String? {
        val profile = _profile.value ?: return null
        if (profile.torrServerUrl.isNotEmpty()) {
            val api = TorrServerApi(HttpClient(), profile.torrServerUrl)
            return api.getStreamUrl(magnetLink, fileIdx)
        }
        return null
    }

    fun onVolumeChange(level: Float) {
        viewModelScope.launch {
            _profile.value?.let { profile ->
                profileRepository.saveProfile(profile.copy(lastVolumeLevel = level))
            }
        }
    }

    fun onBrightnessChange(level: Float) {
        viewModelScope.launch {
            _profile.value?.let { profile ->
                profileRepository.saveProfile(profile.copy(lastBrightnessLevel = level))
            }
        }
    }

    private fun startNextEpisodeInternal(next: MetaVideo) {
        val currentMeta = meta ?: return
        _state.update { it.copy(isEpisodeSwitchLoading = true) }
        
        viewModelScope.launch {
            val streams = addonRepository.getStreams(currentMeta.type, next.id)
            val profile = _profile.value
            
            val selectedStream = if (profile?.autoSelectSource == true) {
                val enabledQualities = StreamSortingService.parseEnabledQualities(profile.sourceEnabledQualities)
                val excludePhrases = StreamSortingService.parseExcludePhrases(profile.sourceExcludePhrases)
                val excludedFormats = StreamSortingService.parseExcludedFormats(profile.sourceExcludedFormats)
                
                val sorted = streamSortingService.sortAndFilter(
                    streams = streams,
                    enabledQualities = enabledQualities,
                    excludePhrases = excludePhrases,
                    addonSortOrders = emptyMap(), 
                    sortBy = profile.sourceSortPrimary,
                    maxSizeGb = profile.sourceMaxSizeGb,
                    excludedFormats = excludedFormats
                )
                
                // Smart matching: Try to find same addon and quality
                val matchingStream = sorted.find { 
                    it.addonTransportUrl == lastSelectedAddonUrl && 
                    StreamParser.parse(it).quality == lastSelectedQuality 
                } ?: sorted.firstOrNull()
                
                matchingStream ?: streams.firstOrNull()
            } else {
                streams.firstOrNull()
            }

            if (selectedStream != null) {
                lastSelectedAddonUrl = selectedStream.addonTransportUrl
                lastSelectedQuality = StreamParser.parse(selectedStream).quality
                
                val resolvedUrl = debridRepository.resolveStream(selectedStream)
                if (resolvedUrl != null) {
                    video = next
                    
                    _state.update { 
                        it.copy(
                            currentStream = selectedStream,
                            pendingEpisodeSwitch = PendingEpisodeSwitch(
                                playbackId = next.id,
                                playbackTitle = episodeDisplayTitle(next),
                                streams = streams,
                                addonSubs = emptyList(),
                                playerCurrentSourceUrl = resolvedUrl,
                                videoNumber = next.episode
                            ),
                            isEpisodeSwitchLoading = false
                        )
                    }
                    loadMetadata(currentMeta, next, selectedStream)
                }
            } else {
                 _state.update { it.copy(isEpisodeSwitchLoading = false) }
            }
        }
    }

    fun startNextEpisode(next: MetaVideo) {
        startNextEpisodeInternal(next)
    }
}
