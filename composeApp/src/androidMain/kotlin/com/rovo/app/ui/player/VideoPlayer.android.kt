package com.rovo.app.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.view.Window
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.rovo.app.data.torrent.TorrentService
import com.rovo.shared.model.stremio.StreamSubtitle
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.ui.player.PlayerViewModel
import org.koin.compose.viewmodel.koinViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.media3.session.MediaSession
import androidx.media3.common.Tracks
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackGroup
import com.rovo.shared.domain.PlayerAudioTrackPayload

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    meta: MetaItem,
    modifier: Modifier,
    subtitles: List<StreamSubtitle>,
    onBack: () -> Unit,
    onProgress: (position: Long, duration: Long) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val viewModel = koinViewModel<PlayerViewModel>()
    val state by viewModel.state.collectAsState()
    
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            prepare()
            playWhenReady = true
        }
    }

    val mediaSession = remember(exoPlayer) {
        MediaSession.Builder(context, exoPlayer)
            .setId("RovoPlayerSession")
            .build()
    }

    val playerView = remember {
        PlayerView(context).apply {
            player = exoPlayer
            useController = false
        }
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                val audioTracks = mutableListOf<PlayerAudioTrackPayload>()
                tracks.groups.forEach { group ->
                    if (group.type == C.TRACK_TYPE_AUDIO) {
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            audioTracks.add(
                                PlayerAudioTrackPayload(
                                    id = "${group.getTrackFormat(0).id ?: "audio"}-$i",
                                    name = format.label ?: "Audio Track ${audioTracks.size + 1}",
                                    language = format.language,
                                    isDefault = false // ExoPlayer handles defaults initially
                                )
                            )
                        }
                    }
                }
                viewModel.setAvailableAudioTracks(audioTracks)
            }
        })
    }

    // Immersive Mode & Orientation
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            activity.requestedOrientation = originalOrientation
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val profile by viewModel.profile.collectAsState()

    val externalPlayerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val position = data.getLongExtra("extra_position", -1L)
            val duration = data.getLongExtra("extra_duration", -1L)
            if (position != -1L && duration != -1L) {
                viewModel.updateProgress(position, duration, isFinished = position > duration * 0.95)
            }
        }
        onBack()
    }

    fun launchExternalPlayer(url: String, playerName: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(url), "video/*")
            putExtra("position", currentPosition.toInt())
            putExtra("return_result", true)
        }
        
        when (playerName.lowercase()) {
            "vlc" -> {
                intent.`package` = "org.videolan.vlc"
                intent.putExtra("from_start", false)
            }
            "mxplayer" -> {
                intent.`package` = "com.mxtech.videoplayer.ad"
                intent.putExtra("return_result", true)
            }
        }
        
        try {
            externalPlayerLauncher.launch(intent)
        } catch (e: Exception) {
            // Player not installed, fallback to internal
        }
    }

    // Handle URL changes (Initial load and Episode switches)
    val currentUrl = state.pendingEpisodeSwitch?.playerCurrentSourceUrl ?: url
    LaunchedEffect(currentUrl, profile) {
        val p = profile ?: return@LaunchedEffect
        
        if (p.externalPlayer != "none") {
            launchExternalPlayer(currentUrl, p.externalPlayer)
            return@LaunchedEffect
        }

        // Apply persisted volume
        exoPlayer.volume = p.lastVolumeLevel
        
        // Apply persisted brightness
        val activity = context.findActivity()
        activity?.window?.attributes?.let {
            it.screenBrightness = p.lastBrightnessLevel
            activity.window.attributes = it
        }

        if (currentUrl.startsWith("magnet:") || currentUrl.contains("infoHash=")) {
            val remoteUrl = viewModel.resolveMagnetUrl(currentUrl, state.pendingEpisodeSwitch?.videoNumber ?: 0)
            if (remoteUrl != null) {
                val mediaItem = MediaItem.fromUri(remoteUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            } else {
                val intent = Intent(context, TorrentService::class.java).apply {
                    putExtra("MAGNET_LINK", currentUrl)
                    state.pendingEpisodeSwitch?.let {
                        putExtra("FILE_IDX", it.videoNumber ?: 0)
                    }
                }
                
                TorrentService.onStreamReady = { streamUrl ->
                    exoPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
                    exoPlayer.prepare()
                }
                TorrentService.onStreamProgress = { progress ->
                    viewModel.updateTorrentProgress(progress)
                }
                
                context.startService(intent)
            }
        } else {
            val mediaItem = MediaItem.Builder()
                .setUri(currentUrl)
                .setSubtitleConfigurations(
                    state.selectedPlayerSubtitles.map { sub ->
                        MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.url))
                            .setMimeType(MimeTypes.TEXT_VTT)
                            .setLanguage(sub.language)
                            .setLabel(sub.name)
                            .build()
                    }
                )
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    LaunchedEffect(state.playbackSpeed) {
        exoPlayer.setPlaybackSpeed(state.playbackSpeed)
    }

    LaunchedEffect(state.subtitleOffset) {
        playerView.subtitleView?.setFractionalTextSize(0.08f) // Ensure visibility
        // ExoPlayer doesn't have a direct "offset" for all internal tracks easily, 
        // but for sidecar we could adjust. For now, we'll use the UI state.
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            totalDuration = exoPlayer.duration
            if (totalDuration > 0) {
                onProgress(currentPosition, totalDuration)
                viewModel.updateProgress(currentPosition, totalDuration)
            }
            isPlaying = exoPlayer.isPlaying
            kotlinx.coroutines.delay(1000)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaSession.release()
            exoPlayer.release()
        }
    }

    LaunchedEffect(profile) {
        profile?.let { p ->
            playerView.subtitleView?.apply {
                setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, (p.subtitleSize / 100f) * 18f)
                setStyle(
                    androidx.media3.ui.CaptionStyleCompat(
                        p.subtitleTextColor.toInt(),
                        p.subtitleBackgroundColor.toInt(),
                        android.graphics.Color.TRANSPARENT,
                        androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_NONE,
                        android.graphics.Color.WHITE,
                        null
                    )
                )
            }
        }
    }

    LaunchedEffect(state.activeAudioTrackId) {
        val audioTrackId = state.activeAudioTrackId ?: return@LaunchedEffect
        val tracks = exoPlayer.currentTracks
        tracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    val id = "${group.getTrackFormat(0).id ?: "audio"}-$i"
                    if (id == audioTrackId) {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                            .build()
                        return@LaunchedEffect
                    }
                }
            }
        }
    }

    LaunchedEffect(state.activeSubtitleId) {
        val subId = state.activeSubtitleId ?: return@LaunchedEffect
        val tracks = exoPlayer.currentTracks
        tracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    val id = format.id ?: format.label ?: ""
                    if (id == subId || format.label == subId) {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                            .build()
                        return@LaunchedEffect
                    }
                }
            }
        }
    }

    LaunchedEffect(state.pendingSeek) {
        state.pendingSeek?.let {
            exoPlayer.seekTo(it)
            viewModel.onSeekComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { playerView },
            modifier = Modifier.fillMaxSize()
        )

        state.torrentProgress?.let { progress ->
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(progress = { progress.progress ?: 0f })
                    Spacer(Modifier.height(8.dp))
                    Text(progress.status, color = Color.White)
                    Text("Peers: ${progress.peers} | ${progress.downloadSpeed / 1024} KB/s", 
                         color = Color.White.copy(0.7f), 
                         style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
            }
        }

        PlayerOverlay(
            title = state.pendingEpisodeSwitch?.playbackTitle ?: meta.name,
            isPlaying = isPlaying,
            position = currentPosition,
            duration = totalDuration,
            sources = state.selectedPlayerSources,
            subtitles = state.selectedPlayerSubtitles,
            audioTracks = state.selectedPlayerAudioTracks,
            nextEpisodeTitle = state.nextEpisode?.title,
            onPlayPause = {
                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
            },
            onSeek = { exoPlayer.seekTo(it) },
            onBack = onBack,
            onNextEpisode = {
                state.nextEpisode?.let { viewModel.startNextEpisode(it) }
            },
            onSourceSelected = { viewModel.onSourceSelected(it) },
            onSubtitleSelected = { viewModel.onSubtitleSelected(it) },
            onAudioTrackSelected = { viewModel.onAudioTrackSelected(it) },
            onSkipPressed = { viewModel.onSkipPressed() },
            showSkipButton = state.showSkipButton,
            autoplayCountdown = state.autoplayCountdown,
            onCancelAutoplay = { viewModel.cancelAutoplay() },
            initialVolume = profile?.lastVolumeLevel ?: 0.5f,
            initialBrightness = profile?.lastBrightnessLevel ?: 0.5f,
            onVolumeChange = { 
                exoPlayer.volume = it
                viewModel.onVolumeChange(it)
            },
            onBrightnessChange = { 
                val activity = context.findActivity() ?: return@PlayerOverlay
                val layoutParams = activity.window.attributes
                layoutParams.screenBrightness = it
                activity.window.attributes = layoutParams
                viewModel.onBrightnessChange(it)
            },
            playbackSpeed = state.playbackSpeed,
            onPlaybackSpeedSelected = { viewModel.onPlaybackSpeedChanged(it) },
            subtitleOffset = state.subtitleOffset,
            onSubtitleOffsetChanged = { viewModel.onSubtitleOffsetChanged(it) },
            onDoubleTapSeek = { exoPlayer.seekTo(exoPlayer.currentPosition + it) }
        )
        
        if (state.isEpisodeSwitchLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
    }
}
