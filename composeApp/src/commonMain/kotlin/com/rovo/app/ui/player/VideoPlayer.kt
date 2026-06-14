package com.rovo.app.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.StreamSubtitle

@Composable
expect fun VideoPlayer(
    url: String,
    meta: MetaItem,
    modifier: Modifier = Modifier,
    subtitles: List<StreamSubtitle> = emptyList(),
    onBack: () -> Unit,
    onProgress: (position: Long, duration: Long) -> Unit
)
