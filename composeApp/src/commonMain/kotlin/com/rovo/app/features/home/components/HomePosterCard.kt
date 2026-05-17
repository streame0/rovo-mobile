package com.rovo.app.features.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rovo.app.core.format.formatReleaseDateForDisplay
import com.rovo.app.core.ui.RovoPosterCard
import com.rovo.app.core.ui.RovoPosterShape
import com.rovo.app.core.ui.rememberPosterCardStyleUiState
import com.rovo.app.features.home.MetaPreview
import com.rovo.app.features.home.PosterShape

@Composable
fun HomePosterCard(
    item: MetaPreview,
    modifier: Modifier = Modifier,
    useLandscapeBackdropMode: Boolean = false,
    isWatched: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val posterCardStyle = rememberPosterCardStyleUiState()
    val isLandscapeMode = useLandscapeBackdropMode || posterCardStyle.catalogLandscapeModeEnabled

    RovoPosterCard(
        title = item.name,
        imageUrl = if (isLandscapeMode) (item.banner ?: item.poster) else item.poster,
        modifier = modifier,
        shape = if (isLandscapeMode) RovoPosterShape.Landscape else item.posterShape.toRovoPosterShape(),
        detailLine = if (isLandscapeMode || posterCardStyle.hideLabelsEnabled) null else item.releaseInfo?.let { formatReleaseDateForDisplay(it) },
        showTitleBelow = !posterCardStyle.hideLabelsEnabled,
        bottomLeftLogoUrl = if (isLandscapeMode) item.logo else null,
        bottomLeftText = if (isLandscapeMode && item.logo.isNullOrBlank() && !posterCardStyle.hideLabelsEnabled) item.name else null,
        isWatched = isWatched,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

private fun PosterShape.toRovoPosterShape(): RovoPosterShape =
    when (this) {
        PosterShape.Poster -> RovoPosterShape.Poster
        PosterShape.Square -> RovoPosterShape.Square
        PosterShape.Landscape -> RovoPosterShape.Landscape
    }
