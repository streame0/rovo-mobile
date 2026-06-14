package com.rovo.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.components.MetaItemCard
import com.rovo.app.ui.components.SectionHeader
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.ui.library.LibraryUiState
import com.rovo.shared.ui.library.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onItemClick: (String, String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val rovoColors = MaterialTheme.rovo.colors

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = rovoColors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = rovoColors.background.copy(alpha = RovoTokens.Opacity.strong)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp
                ),
                modifier = Modifier.padding(horizontal = RovoTokens.Space.s20, vertical = 8.dp),
                color = rovoColors.textPrimary
            )

            Box(
                modifier = Modifier.weight(1f)
            ) {
                when (val state = uiState) {
                    is LibraryUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = rovoColors.primary,
                            strokeWidth = 3.dp
                        )
                    }
                    is LibraryUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = RovoTokens.Space.s16, bottom = RovoTokens.Space.s32),
                            verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s32)
                        ) {
                            if (state.watchlist.isNotEmpty()) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s16)) {
                                        SectionHeader(
                                            title = "Watchlist",
                                            modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)
                                        )
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = RovoTokens.Space.s20),
                                            horizontalArrangement = Arrangement.spacedBy(RovoTokens.Space.s12)
                                        ) {
                                            items(state.watchlist, key = { it.id }) { item ->
                                                MetaItemCard(
                                                    item = MetaItem(
                                                        id = item.id,
                                                        type = item.type,
                                                        name = item.title,
                                                        poster = item.poster
                                                    ),
                                                    onClick = { onItemClick(item.type, item.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (state.history.isNotEmpty()) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s16)) {
                                        SectionHeader(
                                            title = "Continue Watching",
                                            modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)
                                        )
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = RovoTokens.Space.s20),
                                            horizontalArrangement = Arrangement.spacedBy(RovoTokens.Space.s12)
                                        ) {
                                            items(state.history, key = { it.id }) { item ->
                                                MetaItemCard(
                                                    item = MetaItem(
                                                        id = item.id,
                                                        type = item.type,
                                                        name = item.title,
                                                        poster = item.poster
                                                    ),
                                                    onClick = { onItemClick(item.type, item.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (state.watchlist.isEmpty() && state.history.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillParentMaxSize()
                                            .padding(horizontal = 40.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = rovoColors.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(100.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Bookmark,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(40.dp),
                                                    tint = rovoColors.textMuted
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Text(
                                            "Your library is empty",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = rovoColors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "Movies and shows you watch or save will appear here. Start exploring to fill it up!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = rovoColors.textMuted,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
