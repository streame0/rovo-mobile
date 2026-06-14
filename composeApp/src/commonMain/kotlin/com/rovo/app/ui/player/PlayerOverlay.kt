package com.rovo.app.ui.player

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.domain.PlayerSourceOption
import com.rovo.shared.domain.PlayerSubtitlePayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.windowsizeclass.WindowSizeClass


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PlayerOverlay(
    modifier: Modifier = Modifier,
    title: String,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    sources: List<PlayerSourceOption> = emptyList(),
    subtitles: List<PlayerSubtitlePayload> = emptyList(),
    audioTracks: List<com.rovo.shared.domain.PlayerAudioTrackPayload> = emptyList(),
    nextEpisodeTitle: String? = null,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onBack: () -> Unit,
    onNextEpisode: () -> Unit = {},
    onSourceSelected: (PlayerSourceOption) -> Unit = {},
    onSubtitleSelected: (PlayerSubtitlePayload) -> Unit = {},
    onAudioTrackSelected: (com.rovo.shared.domain.PlayerAudioTrackPayload) -> Unit = {},
    onSkipPressed: () -> Unit = {},
    showSkipButton: Boolean = false,
    autoplayCountdown: Int? = null,
    onCancelAutoplay: () -> Unit = {},
    initialVolume: Float = 0.5f,
    initialBrightness: Float = 0.5f,
    onVolumeChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    playbackSpeed: Float = 1.0f,
    onPlaybackSpeedSelected: (Float) -> Unit = {},
    subtitleOffset: Int = 0,
    onSubtitleOffsetChanged: (Int) -> Unit = {},
    onDoubleTapSeek: (Long) -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableStateOf(initialVolume) }
    var brightnessLevel by remember { mutableStateOf(initialBrightness) }
    
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var indicatorJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val scope = rememberCoroutineScope()
    
    var showSources by remember { mutableStateOf(false) }
    var showSubtitles by remember { mutableStateOf(false) }
    var showAudioTracks by remember { mutableStateOf(false) }
    var showPlaybackSpeed by remember { mutableStateOf(false) }
    var showSubtitleOffset by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    // Auto-hide controls
    LaunchedEffect(isVisible, isPlaying, showSources, showSubtitles, isLocked) {
        if (isVisible && isPlaying && !showSources && !showSubtitles && !showPlaybackSpeed && !showSubtitleOffset) {
            delay(5000)
            isVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isLocked) {
                if (isLocked) {
                    detectTapGestures(
                        onTap = { isVisible = !isVisible }
                    )
                    return@pointerInput
                }
                detectVerticalDragGestures { change, dragAmount ->
                    val isLeft = change.position.x < size.width / 2
                    if (isLeft) {
                        brightnessLevel = (brightnessLevel - dragAmount / 500f).coerceIn(0f, 1f)
                        onBrightnessChange(brightnessLevel)
                        showBrightnessIndicator = true
                        showVolumeIndicator = false
                    } else {
                        volumeLevel = (volumeLevel - dragAmount / 500f).coerceIn(0f, 1f)
                        onVolumeChange(volumeLevel)
                        showVolumeIndicator = true
                        showBrightnessIndicator = false
                    }
                    
                    indicatorJob?.cancel()
                    indicatorJob = scope.launch {
                        delay(2000)
                        showVolumeIndicator = false
                        showBrightnessIndicator = false
                    }
                }
            }
            .pointerInput(isLocked) {
                detectTapGestures(
                    onTap = { isVisible = !isVisible },
                    onDoubleTap = { offset: Offset ->
                        if (!isLocked) {
                            if (offset.x < size.width / 2) {
                                onDoubleTapSeek(-10000)
                            } else {
                                onDoubleTapSeek(10000)
                            }
                        }
                    }
                )
            }
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                if (!isLocked) {
                    // Top Bar
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = RovoTokens.Space.s20, vertical = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1.5).sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                                maxLines = 1
                            )
                            
                            Row(
                                modifier = Modifier
                                    .clip(MaterialTheme.rovo.shapes.card)
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), MaterialTheme.rovo.shapes.card)
                            ) {
                                IconButton(onClick = { showSubtitleOffset = true }) {
                                    Icon(Icons.Default.Timer, contentDescription = "Subtitle Offset", tint = Color.White)
                                }
                                IconButton(onClick = { showPlaybackSpeed = true }) {
                                    Icon(Icons.Default.Speed, contentDescription = "Playback Speed", tint = Color.White)
                                }
                                IconButton(onClick = { showSubtitles = true }) {
                                    Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                                }
                                IconButton(onClick = { showAudioTracks = true }) {
                                    Icon(Icons.Default.AudioFile, contentDescription = "Audio Tracks", tint = Color.White)
                                }
                                IconButton(onClick = { showSources = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Sources", tint = Color.White)
                                }
                            }
                        }
                    }

                    // Center Controls
                    Box(
                        modifier = Modifier.align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 80.dp else 48.dp)
                        ) {
                            IconButton(
                                onClick = { onSeek((position - 15000).coerceAtLeast(0)) },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.Replay10, contentDescription = "-15s", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            
                            Surface(
                                onClick = onPlayPause,
                                modifier = Modifier.size(96.dp),
                                shape = CircleShape,
                                color = Color.White,
                                contentColor = Color.Black,
                                shadowElevation = 8.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AnimatedContent(
                                        targetState = isPlaying,
                                        transitionSpec = {
                                            scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) togetherWith 
                                            scaleOut()
                                        }
                                    ) { playing ->
                                        Icon(
                                            if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play/Pause",
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onSeek((position + 15000).coerceAtMost(duration)) },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.Forward10, contentDescription = "+15s", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }

                // Lock/Unlock Button
                IconButton(
                    onClick = { isLocked = !isLocked },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (!isLocked) {
                    // Skip Intro/Outro Button
                    if (showSkipButton) {
                        Button(
                            onClick = onSkipPressed,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 140.dp, end = 24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = MaterialTheme.rovo.shapes.button
                        ) {
                            Text("Skip Intro", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Autoplay Countdown
                    if (autoplayCountdown != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 140.dp)
                                .clip(MaterialTheme.rovo.shapes.card)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Next episode starting in $autoplayCountdown...",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onCancelAutoplay,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                        }
                    }

                    // Next Episode Button
                    if (nextEpisodeTitle != null && autoplayCountdown == null) {
                        Button(
                            onClick = onNextEpisode,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 100.dp, end = 24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = MaterialTheme.rovo.shapes.button
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("Next Episode", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Bottom Controls
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = RovoTokens.Space.s20, vertical = 32.dp)
                        ) {
                            Slider(
                                value = if (duration > 0) position.toFloat() / duration else 0f,
                                onValueChange = { onSeek((it * duration).toLong()) },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    formatTime(position), 
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    formatTime(duration), 
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Volume/Brightness Indicators
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = showVolumeIndicator,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IndicatorOverlay(
                    icon = if (volumeLevel > 0.5f) Icons.AutoMirrored.Filled.VolumeUp else if (volumeLevel > 0f) Icons.AutoMirrored.Filled.VolumeDown else Icons.AutoMirrored.Filled.VolumeOff,
                    value = volumeLevel,
                    label = "Volume"
                )
            }
            
            AnimatedVisibility(
                visible = showBrightnessIndicator,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IndicatorOverlay(
                    icon = Icons.Default.Brightness6,
                    value = brightnessLevel,
                    label = "Brightness"
                )
            }
        }

        // Selection Dialogs
        if (showSources) {
            SelectionDialog(
                title = "Select Source",
                items = sources,
                labelProvider = { it.label },
                onSelect = { onSourceSelected(it); showSources = false },
                onDismiss = { showSources = false }
            )
        }
        
        if (showSubtitles) {
            SelectionDialog(
                title = "Select Subtitles",
                items = subtitles,
                labelProvider = { "${it.name} (${it.language ?: "Unknown"})" },
                onSelect = { onSubtitleSelected(it); showSubtitles = false },
                onDismiss = { showSubtitles = false }
            )
        }

        if (showAudioTracks) {
            SelectionDialog(
                title = "Select Audio Track",
                items = audioTracks,
                labelProvider = { "${it.name} (${it.language ?: "Unknown"})" },
                onSelect = { onAudioTrackSelected(it); showAudioTracks = false },
                onDismiss = { showAudioTracks = false }
            )
        }

        if (showPlaybackSpeed) {
            SelectionDialog(
                title = "Playback Speed",
                items = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f),
                labelProvider = { "${it}x" },
                onSelect = { onPlaybackSpeedSelected(it); showPlaybackSpeed = false },
                onDismiss = { showPlaybackSpeed = false }
            )
        }

        if (showSubtitleOffset) {
            AlertDialog(
                onDismissRequest = { showSubtitleOffset = false },
                containerColor = MaterialTheme.rovo.colors.surface,
                shape = MaterialTheme.rovo.shapes.card,
                title = { 
                    Text(
                        "Subtitle Offset", 
                        color = MaterialTheme.rovo.colors.textPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp
                    ) 
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${subtitleOffset}ms", 
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.rovo.colors.textPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onSubtitleOffsetChanged(subtitleOffset - 100) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.rovo.colors.surfaceVariant)
                            ) { Text("-100ms", color = Color.White) }
                            Button(
                                onClick = { onSubtitleOffsetChanged(subtitleOffset + 100) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.rovo.colors.surfaceVariant)
                            ) { Text("+100ms", color = Color.White) }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onSubtitleOffsetChanged(subtitleOffset - 1000) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.rovo.colors.surfaceVariant)
                            ) { Text("-1s", color = Color.White) }
                            Button(
                                onClick = { onSubtitleOffsetChanged(subtitleOffset + 1000) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.rovo.colors.surfaceVariant)
                            ) { Text("+1s", color = Color.White) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSubtitleOffset = false }) { 
                        Text("Done", color = MaterialTheme.rovo.colors.primary, fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp)
                    }
                }
            )
        }
    }
}

@Composable
fun <T> SelectionDialog(
    title: String,
    items: List<T>,
    labelProvider: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.rovo.colors.surface,
        shape = MaterialTheme.rovo.shapes.card,
        title = { 
            Text(
                title, 
                color = MaterialTheme.rovo.colors.textPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp
            ) 
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    Surface(
                        onClick = { onSelect(item) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.rovo.shapes.card,
                        color = MaterialTheme.rovo.colors.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            labelProvider(item), 
                            textAlign = TextAlign.Start, 
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.rovo.colors.textPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel", color = MaterialTheme.rovo.colors.textSecondary) 
            }
        }
    )
}

@Composable
private fun IndicatorOverlay(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    label: String
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.rovo.shapes.card)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { value },
            modifier = Modifier.width(100.dp).height(4.dp).clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}
