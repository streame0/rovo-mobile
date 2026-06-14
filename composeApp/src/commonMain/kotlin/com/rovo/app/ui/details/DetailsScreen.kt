package com.rovo.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rovo.app.ui.components.SectionHeader
import com.rovo.app.ui.details.components.CastRail
import com.rovo.app.ui.details.components.DetailActionRow
import com.rovo.app.ui.details.components.DetailHero
import com.rovo.app.ui.details.components.RecommendationRail
import com.rovo.app.ui.details.components.SeasonSelector
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.MetaVideo
import com.rovo.shared.model.stremio.Stream
import com.rovo.shared.ui.details.DetailsUiState
import com.rovo.shared.ui.details.DetailsViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(
    type: String,
    id: String,
    onBack: () -> Unit,
    onPlayStream: (Stream, MetaItem, MetaVideo?) -> Unit,
    viewModel: DetailsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedVideo by viewModel.selectedVideo.collectAsState()
    val historyItem by viewModel.historyItem.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val streams by viewModel.streams.collectAsState()
    val isLoadingStreams by viewModel.isLoadingStreams.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showMoreInfo by remember { mutableStateOf(false) }
    var selectedSeason by remember { mutableStateOf(0) }
    var showSourceSelection by remember { mutableStateOf(false) }
    var showMoreActions by remember { mutableStateOf(false) }
    var showClearProgressConfirm by remember { mutableStateOf(false) }

    val rovoColors = MaterialTheme.rovo.colors

    LaunchedEffect(type, id) {
        viewModel.loadDetails(type, id)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = rovoColors.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleWatchlist() },
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watchlist",
                            tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = rovoColors.background.copy(alpha = RovoTokens.Opacity.strong)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is DetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailsUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is DetailsUiState.Success -> {
                    val meta = state.meta
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Hero Backdrop
                        item {
                            DetailHero(meta = meta)
                        }

                        // Action Row
                        item {
                            DetailActionRow(
                                onPlayClick = {
                                    if (streams.isNotEmpty()) {
                                        if (streams.size > 1) {
                                            showSourceSelection = true
                                        } else {
                                            onPlayStream(streams.first(), meta, selectedVideo)
                                        }
                                    } else {
                                        showSourceSelection = true
                                    }
                                },
                                onMoreClick = { showMoreActions = true },
                                onEpisodesClick = { /* Show Episode Selection BottomSheet */ },
                                playLabel = if (meta.type == "series") {
                                    val ep = selectedVideo
                                    val prefix = if (historyItem != null && !historyItem!!.watched) "RESUME" else "PLAY"
                                    if (ep != null) "$prefix S${ep.season}:E${ep.episode}" else "PLAY"
                                } else {
                                    if (historyItem != null && !historyItem!!.watched) "RESUME" else "PLAY"
                                },
                                isLoadingStreams = isLoadingStreams,
                                isSeries = meta.type == "series",
                                modifier = Modifier.padding(vertical = RovoTokens.Space.s8)
                            )
                        }

                        // Info Section
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = RovoTokens.Space.s20, vertical = RovoTokens.Space.s16),
                                verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s12)
                            ) {
                                Text(
                                    text = meta.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = rovoColors.textSecondary,
                                    lineHeight = 22.sp,
                                    maxLines = if (showMoreInfo) Int.MAX_VALUE else 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (showMoreInfo) "Show Less ▴" else "Show More ▾",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = rovoColors.textPrimary,
                                    modifier = Modifier.clickable { showMoreInfo = !showMoreInfo }
                                )
                                
                                meta.genres?.let { genres ->
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(RovoTokens.Space.s8),
                                        verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s8)
                                    ) {
                                        genres.forEach { genre ->
                                            Surface(
                                                color = rovoColors.surfaceVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(RovoTokens.Radius.sm),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    rovoColors.textMuted.copy(alpha = 0.1f)
                                                )
                                            ) {
                                                Text(
                                                    text = genre,
                                                    modifier = Modifier.padding(horizontal = RovoTokens.Space.s8, vertical = RovoTokens.Space.s4),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = rovoColors.textPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (meta.type == "series") {
                            item {
                                SeasonSelector(
                                    seasons = meta.videos?.map { it.season }?.distinct()?.sorted() ?: emptyList(),
                                    selectedSeason = selectedSeason,
                                    onSeasonSelect = { selectedSeason = it },
                                    modifier = Modifier.padding(vertical = RovoTokens.Space.s16)
                                )
                            }
                            
                            items(meta.videos?.filter { it.season == selectedSeason } ?: emptyList()) { video ->
                                EpisodeItem(
                                    video = video,
                                    isSelected = selectedVideo?.id == video.id,
                                    onClick = { viewModel.selectVideo(video) }
                                )
                            }
                        }

                        // Cast
                        if (!meta.cast.isNullOrEmpty()) {
                            item {
                                CastRail(
                                    cast = meta.cast!!,
                                    modifier = Modifier.padding(vertical = RovoTokens.Space.s24)
                                )
                            }
                        }

                        // Recommendations
                        if (recommendations.isNotEmpty()) {
                            item {
                                RecommendationRail(
                                    recommendations = recommendations,
                                    onItemClick = { item -> viewModel.loadDetails(item.type, item.id) },
                                    modifier = Modifier.padding(top = RovoTokens.Space.s24)
                                )
                            }
                        }

                        // Details Table
                        item {
                            Spacer(modifier = Modifier.height(RovoTokens.Space.s24))
                            SectionHeader(
                                title = "Show Details",
                                modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)
                            )
                            Spacer(modifier = Modifier.height(RovoTokens.Space.s12))
                            DetailRow("Status", if (meta.type == "series") "Continuing" else "Released")
                            DetailRow("Release Info", meta.releaseInfo ?: "Unknown")
                            DetailRow("Runtime", meta.runtime ?: "N/A")
                            if (!meta.genres.isNullOrEmpty()) {
                                DetailRow("Genres", meta.genres!!.joinToString(", "))
                            }
                        }

                        // Episodes Section (Alternative for Series)
                        if (meta.type == "series" && !meta.videos.isNullOrEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(RovoTokens.Space.s24))
                                SectionHeader(
                                    title = "Episodes",
                                    modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)
                                )
                                Spacer(modifier = Modifier.height(RovoTokens.Space.s12))
                            }
                            
                            val groupedVideos = meta.videos!!.groupBy { it.season }
                            val seasons = groupedVideos.keys.sorted()
                            
                            item {
                                if (selectedSeason == 0 && seasons.isNotEmpty()) {
                                    SideEffect {
                                        selectedSeason = seasons.first()
                                    }
                                }
                                
                                SeasonSelector(
                                    seasons = seasons,
                                    selectedSeason = selectedSeason,
                                    onSeasonSelect = { selectedSeason = it },
                                    modifier = Modifier.padding(bottom = RovoTokens.Space.s8)
                                )
                            }

                            items(groupedVideos[selectedSeason] ?: emptyList()) { video ->
                                EpisodeItem(
                                    video = video,
                                    isSelected = selectedVideo?.id == video.id,
                                    onClick = { viewModel.selectVideo(video) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(RovoTokens.Space.s32))
                        }
                    }
                }
            }
        }

        if (showMoreActions) {
            ModalBottomSheet(
                onDismissRequest = { showMoreActions = false },
                containerColor = rovoColors.surfaceVariant,
                contentColor = rovoColors.textPrimary,
                shape = RoundedCornerShape(topStart = RovoTokens.Radius.xxl, topEnd = RovoTokens.Radius.xxl)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(rovoColors.textMuted.copy(alpha = 0.3f), CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    ListItem(
                        headlineContent = { Text("Clear Watch Progress") },
                        leadingContent = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showMoreActions = false
                            showClearProgressConfirm = true
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    
                    if ((uiState as? DetailsUiState.Success)?.meta?.trailerKey != null) {
                        ListItem(
                            headlineContent = { Text("Watch Trailer") },
                            leadingContent = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                            modifier = Modifier.clickable {
                                // TODO: Handle trailer playback
                                showMoreActions = false
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        if (showSourceSelection) {
            ModalBottomSheet(
                onDismissRequest = { showSourceSelection = false },
                containerColor = rovoColors.surfaceVariant,
                contentColor = rovoColors.textPrimary,
                shape = RoundedCornerShape(topStart = RovoTokens.Radius.xxl, topEnd = RovoTokens.Radius.xxl)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(rovoColors.textMuted.copy(alpha = 0.3f), CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                    
                    Text(
                        text = "Select Source",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(24.dp),
                        fontWeight = FontWeight.Bold
                    )

                    profile?.let { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Auto-select", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.weight(1f))
                            Switch(
                                checked = p.autoSelectSource,
                                onCheckedChange = { checked ->
                                    viewModel.toggleProfileSetting { it.copy(autoSelectSource = checked) }
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Remember Selection", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.weight(1f))
                            Switch(
                                checked = p.rememberSourceSelection,
                                onCheckedChange = { checked ->
                                    viewModel.toggleProfileSetting { it.copy(rememberSourceSelection = checked) }
                                }
                            )
                        }
                    }
                    
                    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                        if (isLoadingStreams) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = rovoColors.primary,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        } else if (streams.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "No sources found",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = rovoColors.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Try a different episode or check your addons",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = rovoColors.textMuted,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(streams) { stream ->
                                val meta = (uiState as? DetailsUiState.Success)?.meta
                                SourceItem(
                                    stream = stream,
                                    onClick = {
                                        showSourceSelection = false
                                        if (meta != null) {
                                            onPlayStream(stream, meta, selectedVideo)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showClearProgressConfirm) {
            AlertDialog(
                onDismissRequest = { showClearProgressConfirm = false },
                title = { Text("Clear Progress") },
                text = { Text("This will remove all watch progress for this title, including on Trakt. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearProgress()
                            showClearProgressConfirm = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("CLEAR")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearProgressConfirm = false }) {
                        Text("CANCEL")
                    }
                },
                containerColor = rovoColors.surfaceVariant,
                titleContentColor = rovoColors.textPrimary,
                textContentColor = rovoColors.textSecondary,
                shape = MaterialTheme.rovo.shapes.card
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = RovoTokens.Space.s20)) {
        Row(
            modifier = Modifier.padding(vertical = RovoTokens.Space.s8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.rovo.colors.textSecondary, 
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                value, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.rovo.colors.textPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(color = MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f), thickness = 1.dp)
    }
}

@Composable
fun EpisodeItem(
    video: MetaVideo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rovoColors = MaterialTheme.rovo.colors
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RovoTokens.Space.s20, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(RovoTokens.Radius.lg),
        color = if (isSelected) rovoColors.primary.copy(alpha = 0.1f) else rovoColors.surface.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) rovoColors.primary.copy(alpha = 0.3f) else rovoColors.textMuted.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(16/9f)
                    .clip(RoundedCornerShape(RovoTokens.Radius.md))
                    .background(rovoColors.surfaceVariant)
            ) {
                if (video.thumbnail != null) {
                    AsyncImage(
                        model = video.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Play overlay if selected
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(rovoColors.primary.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = rovoColors.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Episode ${video.episode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) rovoColors.primary else rovoColors.textMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = video.title ?: "Untitled",
                    style = MaterialTheme.typography.titleSmall,
                    color = rovoColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                if (video.overview != null) {
                    Text(
                        text = video.overview!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = rovoColors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SourceItem(
    stream: Stream,
    onClick: () -> Unit
) {
    val rovoColors = MaterialTheme.rovo.colors
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = RovoTokens.Space.s20, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(RovoTokens.Radius.lg),
        color = rovoColors.surface.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            rovoColors.textMuted.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val title = stream.title ?: stream.name ?: "Unknown Source"
            val lines = title.split("\n")
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quality indicator
                val quality = remember(title) {
                    when {
                        title.contains("4K", ignoreCase = true) -> "4K"
                        title.contains("1080p", ignoreCase = true) -> "1080P"
                        title.contains("720p", ignoreCase = true) -> "720P"
                        else -> "HD"
                    }
                }
                
                Surface(
                    color = rovoColors.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = quality,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = rovoColors.primary,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = lines.first(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = rovoColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (lines.size > 1 || stream.name != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val subText = buildString {
                    if (lines.size > 1) append(lines.drop(1).joinToString(" ").trim())
                    if (stream.name != null) {
                        if (isNotEmpty()) append(" • ")
                        append(stream.name)
                    }
                }
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = rovoColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
