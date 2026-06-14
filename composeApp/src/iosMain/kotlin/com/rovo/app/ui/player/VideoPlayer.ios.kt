package com.rovo.app.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.StreamSubtitle
import com.rovo.shared.ui.player.PlayerViewModel
import com.rovo.shared.domain.PlayerAudioTrackPayload
import com.rovo.shared.domain.PlayerSubtitlePayload
import org.koin.compose.viewmodel.koinViewModel
import platform.AVFoundation.*
import platform.AVKit.AVPlayerViewController
import platform.CoreGraphics.CGRectZero
import platform.Foundation.*
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.*
import platform.CoreMedia.*
import platform.MediaPlayer.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    meta: MetaItem,
    modifier: Modifier,
    subtitles: List<StreamSubtitle>,
    onBack: () -> Unit,
    onProgress: (position: Long, duration: Long) -> Unit
) {
    val viewModel = koinViewModel<PlayerViewModel>()
    val state by viewModel.state.collectAsState()
    
    val player = remember { AVPlayer() }
    val playerViewController = remember {
        AVPlayerViewController().apply {
            this.player = player
            this.showsPlaybackControls = false
            this.allowsPictureInPicturePlayback = true
            this.canStartPictureInPictureAutomaticallyFromInline = true
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Configure Audio Session for background playback
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)

        // Simple orientation lock for iOS
        val windowScene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        windowScene?.let { scene ->
            val geometryUpdate = UIWindowSceneGeometryUpdateInterfaceOrientation(UIInterfaceOrientationMaskLandscape)
            scene.requestGeometryUpdateWithPreferences(geometryUpdate) { error ->
                println("Orientation lock error: ${error?.localizedDescription}")
            }
        }
        // Fallback for older iOS versions
        UIDevice.currentDevice.setValue(UIInterfaceOrientationLandscapeLeft, forKey = "orientation")

        // Setup Remote Command Center (Lock screen controls)
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        commandCenter.playCommand.enabled = true
        commandCenter.playCommand.addTargetWithHandler {
            player.play()
            MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.pauseCommand.enabled = true
        commandCenter.pauseCommand.addTargetWithHandler {
            player.pause()
            MPRemoteCommandHandlerStatusSuccess
        }
        commandCenter.changePlaybackPositionCommand.enabled = true
        commandCenter.changePlaybackPositionCommand.addTargetWithHandler { event ->
            val positionEvent = event as? MPChangePlaybackPositionCommandEvent
            positionEvent?.positionTime?.let {
                player.seekToTime(CMTimeMakeWithSeconds(it, 1000))
            }
            MPRemoteCommandHandlerStatusSuccess
        }
    }

    val currentUrl = state.pendingEpisodeSwitch?.playerCurrentSourceUrl ?: url
    val profile by viewModel.profile.collectAsState()

    // Monitor for track changes and update ViewModel
    LaunchedEffect(player.currentItem) {
        val currentItem = player.currentItem ?: return@LaunchedEffect
        
        // Wait for item to be ready
        while (currentItem.status != AVPlayerItemStatusReadyToPlay) {
            delay(100)
        }

        val asset = currentItem.asset
        val audioTracks = mutableListOf<PlayerAudioTrackPayload>()
        
        asset.tracksWithMediaType(AVMediaTypeAudio).forEachIndexed { index, track ->
            val t = track as AVAssetTrack
            audioTracks.add(
                PlayerAudioTrackPayload(
                    id = t.trackID.toString(),
                    name = "Audio Track ${index + 1}",
                    language = t.languageCode,
                    isDefault = index == 0
                )
            )
        }
        viewModel.setAvailableAudioTracks(audioTracks)
    }

    LaunchedEffect(currentUrl, profile, state.activeSubtitleId) {
        val p = profile ?: return@LaunchedEffect
        errorMessage = null
        
        player.volume = p.lastVolumeLevel
        UIScreen.mainScreen.brightness = p.lastBrightnessLevel.toDouble()

        // Configure Subtitle Styles
        val textStyleRules = mutableListOf<AVTextStyleRule>()
        val fontSize = p.subtitleSize.toDouble()
        
        fun Long.toUIColor(): UIColor {
            val r = (this shr 16 and 0xFF).toDouble() / 255.0
            val g = (this shr 8 and 0xFF).toDouble() / 255.0
            val b = (this and 0xFF).toDouble() / 255.0
            val a = (this shr 24 and 0xFF).toDouble() / 255.0
            return UIColor.colorWithRed(r, green = g, blue = b, alpha = a)
        }

        val attributes = mutableMapOf<Any?, Any?>()
        attributes[kAVCaptionFontScaleKey] = fontSize / 100.0
        
        if (p.subtitleTextColor != 0xFFFFFFFFL) {
            attributes[kAVCaptionForegroundColorKey] = p.subtitleTextColor.toUIColor().CGColor
        }
        
        if (p.subtitleBackgroundColor != 0x00000000L) {
            attributes[kAVCaptionBackgroundColorKey] = p.subtitleBackgroundColor.toUIColor().CGColor
        }

        AVTextStyleRule(textMarkupAttributes = attributes as Map<Any?, *>)?.let { textStyleRules.add(it) }

        val playableUrl = if (currentUrl.startsWith("magnet:") || currentUrl.contains("infoHash=")) {
            if (p.torrServerUrl.isEmpty()) {
                errorMessage = "Remote TorrServer URL is required for magnet streaming on iOS. Please configure it in your profile settings."
                null
            } else {
                viewModel.resolveMagnetUrl(currentUrl, state.pendingEpisodeSwitch?.video?.number ?: 0)
            }
        } else {
            currentUrl
        }

        if (playableUrl != null) {
            val nsUrl = NSURL.URLWithString(playableUrl)
            if (nsUrl != null) {
                val asset = AVURLAsset.assetWithURL(nsUrl)
                val activeSubtitle = viewModel.getActiveSubtitle()
                
                suspend fun applyItem(item: AVPlayerItem) {
                    item.textStyleRules = textStyleRules
                    player.replaceCurrentItemWithPlayerItem(item)
                    player.play()
                    
                    // Update Now Playing Info
                    val info = mutableMapOf<Any?, Any?>()
                    info[MPMediaItemPropertyTitle] = state.pendingEpisodeSwitch?.playbackTitle ?: meta.name
                    info[MPNowPlayingInfoPropertyPlaybackRate] = 1.0
                    MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
                }

                if (activeSubtitle != null && activeSubtitle.url.isNotEmpty()) {
                    val subUrl = NSURL.URLWithString(activeSubtitle.url)
                    if (subUrl != null) {
                        val composition = AVMutableComposition.composition()
                        
                        asset.loadValuesAsynchronouslyForKeys(listOf("tracks")) {
                            val videoTrack = composition.addMutableTrackWithMediaType(AVMediaTypeVideo, kCMPersistentTrackIDInvalid)
                            val audioTrack = composition.addMutableTrackWithMediaType(AVMediaTypeAudio, kCMPersistentTrackIDInvalid)
                            
                            val assetVideoTrack = asset.tracksWithMediaType(AVMediaTypeVideo).firstOrNull() as? AVAssetTrack
                            val assetAudioTrack = asset.tracksWithMediaType(AVMediaTypeAudio).firstOrNull() as? AVAssetTrack
                            
                            if (assetVideoTrack != null) {
                                videoTrack?.insertTimeRange(
                                    CMTimeRangeMake(kCMTimeZero, asset.duration),
                                    ofTrack = assetVideoTrack,
                                    atTime = kCMTimeZero,
                                    error = null
                                )
                                assetAudioTrack?.let {
                                    audioTrack?.insertTimeRange(
                                        CMTimeRangeMake(kCMTimeZero, asset.duration),
                                        ofTrack = it,
                                        atTime = kCMTimeZero,
                                        error = null
                                    )
                                }
                                
                                val subAsset = AVURLAsset.assetWithURL(subUrl)
                                val subTrack = composition.addMutableTrackWithMediaType(AVMediaTypeText, kCMPersistentTrackIDInvalid)
                                subAsset.loadValuesAsynchronouslyForKeys(listOf("tracks")) {
                                    val assetSubTrack = subAsset.tracksWithMediaType(AVMediaTypeText).firstOrNull() as? AVAssetTrack
                                    if (assetSubTrack != null) {
                                        subTrack?.insertTimeRange(
                                            CMTimeRangeMake(kCMTimeZero, asset.duration),
                                            ofTrack = assetSubTrack,
                                            atTime = kCMTimeZero,
                                            error = null
                                        )
                                    }
                                    
                                    val playerItem = AVPlayerItem.playerItemWithAsset(composition)
                                    MainScope().launch {
                                        applyItem(playerItem)
                                    }
                                }
                            } else {
                                MainScope().launch {
                                    applyItem(AVPlayerItem.playerItemWithAsset(asset))
                                }
                            }
                        }
                    } else {
                        applyItem(AVPlayerItem.playerItemWithURL(nsUrl))
                    }
                } else {
                    applyItem(AVPlayerItem.playerItemWithURL(nsUrl))
                }
            }
        }
    }

    // Handle Audio Track Selection
    LaunchedEffect(state.activeAudioTrackId) {
        val trackId = state.activeAudioTrackId ?: return@LaunchedEffect
        val currentItem = player.currentItem ?: return@LaunchedEffect
        val group = currentItem.asset.mediaSelectionGroupForMediaCharacteristic(AVMediaCharacteristicAudible)
        if (group != null) {
            val options = group.options
            val option = options.find { (it as AVMediaSelectionOption).propertyList()?.toString()?.contains(trackId) == true } 
                         ?: options.firstOrNull()
            if (option != null) {
                currentItem.selectMediaOption(option as AVMediaSelectionOption, inMediaSelectionGroup = group)
            }
        }
    }

    LaunchedEffect(player) {
        while (true) {
            val time = player.currentTime()
            val duration = player.currentItem?.duration
            if (time != null && duration != null && CMTIME_IS_VALID(time) && CMTIME_IS_VALID(duration)) {
                currentPosition = (CMTimeGetSeconds(time) * 1000).toLong()
                totalDuration = (CMTimeGetSeconds(duration) * 1000).toLong()
                if (totalDuration > 0) {
                    onProgress(currentPosition, totalDuration)
                    viewModel.updateProgress(currentPosition, totalDuration)
                    
                    // Update Lock Screen Progress
                    val info = MPRemoteCommandCenter.sharedCommandCenter().let {
                        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo?.toMutableMap() ?: mutableMapOf()
                    }
                    info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = CMTimeGetSeconds(time)
                    info[MPMediaItemPropertyPlaybackDuration] = CMTimeGetSeconds(duration)
                    info[MPNowPlayingInfoPropertyPlaybackRate] = player.rate.toDouble()
                    MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
                }
            }
            isPlaying = player.rate > 0
            delay(1000)
        }
    }

    LaunchedEffect(state.pendingSeek) {
        state.pendingSeek?.let {
            val time = CMTimeMake(it, 1000)
            player.seekToTime(time)
            viewModel.onSeekComplete()
        }
    }

    LaunchedEffect(state.playbackSpeed) {
        player.rate = state.playbackSpeed
    }

    LaunchedEffect(state.subtitleOffset) {
        // AVPlayer does not easily support per-item subtitle offset for internal tracks
        // This would require AVMutableComposition timing adjustments if implemented
    }

    Box(modifier = Modifier.fillMaxSize()) {
        UIKitView(
            factory = {
                playerViewController.view
            },
            modifier = Modifier.fillMaxSize()
        )

        errorMessage?.let { msg ->
            Box(
                modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(0.8f)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    modifier = androidx.compose.ui.Modifier.padding(32.dp)
                ) {
                    androidx.compose.material3.Text(
                        msg,
                        color = androidx.compose.ui.graphics.Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.Button(onClick = onBack) {
                        androidx.compose.material3.Text("Go Back")
                    }
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
                if (player.rate > 0) player.pause() else player.play()
            },
            onSeek = { 
                val time = CMTimeMake(it, 1000)
                player.seekToTime(time)
            },
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
                player.volume = it
                viewModel.onVolumeChange(it)
            },
            onBrightnessChange = { 
                UIScreen.mainScreen.brightness = it.toDouble()
                viewModel.onBrightnessChange(it)
            },
            playbackSpeed = state.playbackSpeed,
            onPlaybackSpeedSelected = { viewModel.onPlaybackSpeedChanged(it) },
            subtitleOffset = state.subtitleOffset,
            onSubtitleOffsetChanged = { viewModel.onSubtitleOffsetChanged(it) },
            onDoubleTapSeek = { 
                val newTime = CMTimeAdd(player.currentTime(), CMTimeMake(it, 1000))
                player.seekToTime(newTime)
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            player.pause()
            player.replaceCurrentItemWithPlayerItem(null)
            
            // Reset orientation to portrait on exit
            val windowScene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
            windowScene?.let { scene ->
                val geometryUpdate = UIWindowSceneGeometryUpdateInterfaceOrientation(UIInterfaceOrientationMaskPortrait)
                scene.requestGeometryUpdateWithPreferences(geometryUpdate) { error ->
                    println("Orientation reset error: ${error?.localizedDescription}")
                }
            }
            // Fallback for older iOS versions
            UIDevice.currentDevice.setValue(UIInterfaceOrientationPortrait, forKey = "orientation")
            
            // Clear Lock Screen Info
            MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
            
            // Cleanup Remote Commands
            val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
            commandCenter.playCommand.enabled = false
            commandCenter.pauseCommand.enabled = false
            commandCenter.changePlaybackPositionCommand.enabled = false
        }
    }
}
