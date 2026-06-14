package com.rovo.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rovo.app.ui.components.HomeRowSection
import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.ui.home.HomeUiState
import com.rovo.shared.ui.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

import com.rovo.app.ui.theme.rovo
import com.rovo.app.ui.theme.RovoTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (String, String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.rovo.colors.background.copy(alpha = RovoTokens.Opacity.strong)
                )
            )
        },
        containerColor = MaterialTheme.rovo.colors.background
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.rovo.colors.primary,
                        strokeWidth = 3.dp
                    )
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(RovoTokens.Space.s32),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s16)
                    ) {
                        Text(
                            text = state.message, 
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.loadHome() },
                            shape = MaterialTheme.rovo.shapes.button,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.rovo.colors.surfaceVariant)
                        ) {
                            Text("Retry", color = MaterialTheme.rovo.colors.textPrimary)
                        }
                    }
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s24), // Increased spacing for "coziness"
                        contentPadding = PaddingValues(bottom = RovoTokens.Space.s32)
                    ) {
                        // Hero Section
                        state.rows.firstOrNull()?.items?.firstOrNull()?.let { hero ->
                            item {
                                HeroSection(hero, onItemClick)
                            }
                        }

                        items(state.rows, key = { it.configId }) { row ->
                            HomeRowSection(
                                homeRow = row,
                                onItemClick = { item ->
                                    onItemClick(item.type, item.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSection(
    item: MetaItem,
    onItemClick: (String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(620.dp) // Taller for more impact
            .clickable { onItemClick(item.type, item.id) }
    ) {
        AsyncImage(
            model = item.background ?: item.poster,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Advanced multi-layer gradient for deep cinematic feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.6f),
                        0.2f to Color.Transparent,
                        0.6f to Color.Transparent,
                        0.85f to MaterialTheme.rovo.colors.background.copy(alpha = RovoTokens.Opacity.strong),
                        1.0f to MaterialTheme.rovo.colors.background
                    )
                )
        )

        // Bottom horizontal vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                        center = androidx.compose.ui.geometry.Offset(0.5f, 0.8f),
                        radius = 1000f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .padding(horizontal = RovoTokens.Space.s24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (item.logo != null) {
                AsyncImage(
                    model = item.logo,
                    contentDescription = item.name,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(0.8f),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        lineHeight = 38.sp,
                        fontSize = RovoTokens.Type.displaySm
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(RovoTokens.Space.s16))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RovoTokens.Space.s8)
            ) {
                Surface(
                    color = MaterialTheme.rovo.colors.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(RovoTokens.Radius.sm),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.rovo.colors.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = item.type.uppercase(),
                        modifier = Modifier.padding(horizontal = RovoTokens.Space.s8, vertical = RovoTokens.Space.s4),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Text(
                    text = item.releaseInfo ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(RovoTokens.Space.s32))
            
            Button(
                onClick = { onItemClick(item.type, item.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(RovoTokens.Radius.lg),
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(0.7f),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = RovoTokens.Space.s8)
            ) {
                Icon(
                    Icons.Default.PlayArrow, 
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(RovoTokens.Space.s12))
                Text(
                    "WATCH NOW",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
