package com.rovo.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.border
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.rovo.app.ui.components.MetaItemCard
import com.rovo.app.ui.components.SectionHeader
import com.rovo.shared.ui.search.SearchUiState
import com.rovo.shared.ui.search.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel

import com.rovo.app.ui.theme.rovo
import com.rovo.app.ui.theme.RovoTokens

@Composable
fun SearchScreen(
    onItemClick: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val rovoColors = MaterialTheme.rovo.colors

    Scaffold(
        containerColor = rovoColors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp
                ),
                modifier = Modifier.padding(horizontal = RovoTokens.Space.s20),
                color = rovoColors.textPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Search Input - Modern Cozy Look
            Surface(
                modifier = Modifier
                    .padding(horizontal = RovoTokens.Space.s20)
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(RovoTokens.Radius.lg),
                color = rovoColors.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    rovoColors.textMuted.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = rovoColors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = rovoColors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(rovoColors.primary),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    "Movies, shows, actors...",
                                    color = rovoColors.textMuted,
                                    fontSize = 17.sp,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (query.isEmpty()) {
                SectionHeader(
                    title = "Discover",
                    modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RovoTokens.Space.s20),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterPill("Movies")
                    FilterPill("TV Shows")
                    FilterPill("Genres")
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is SearchUiState.Idle -> {
                        // suggestions could go here
                    }
                    is SearchUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = rovoColors.primary,
                            strokeWidth = 3.dp
                        )
                    }
                    is SearchUiState.Empty -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "No results found",
                                color = rovoColors.textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Try searching for something else",
                                color = rovoColors.textMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    is SearchUiState.Error -> {
                        Text(
                            state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    is SearchUiState.Success -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(horizontal = RovoTokens.Space.s20, vertical = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.results, key = { it.id }) { item ->
                                MetaItemCard(
                                    item = item,
                                    onClick = { onItemClick(item.type, item.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPill(text: String) {
    val rovoColors = MaterialTheme.rovo.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = rovoColors.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.height(44.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = rovoColors.textMuted.copy(alpha = 0.1f)
        ),
        onClick = { /* TODO */ }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text, 
                style = MaterialTheme.typography.labelLarge, 
                color = rovoColors.textPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Icon(
                Icons.Default.ArrowDropDown, 
                contentDescription = null, 
                tint = rovoColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
