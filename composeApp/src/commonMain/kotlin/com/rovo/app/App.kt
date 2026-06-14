package com.rovo.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rovo.app.ui.addons.AddonManagementScreen
import com.rovo.app.ui.details.DetailsScreen
import com.rovo.app.ui.home.HomeScreen
import com.rovo.app.ui.library.LibraryScreen
import com.rovo.app.ui.player.VideoPlayer
import com.rovo.app.ui.search.SearchScreen
import com.rovo.app.ui.settings.SettingsScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

import com.rovo.shared.model.stremio.MetaItem
import com.rovo.shared.model.stremio.MetaVideo
import com.rovo.shared.model.stremio.Stream
import com.rovo.shared.ui.player.PlayerViewModel
import com.rovo.shared.ui.settings.SettingsViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

import com.rovo.app.ui.theme.*

sealed interface Screen {
    data object Home : Screen
    data object Search : Screen
    data object Library : Screen
    data object Settings : Screen
    data object AddonManagement : Screen
    data class Details(val type: String, val id: String) : Screen
    data class Player(
        val url: String,
        val meta: MetaItem,
        val video: MetaVideo?,
        val stream: Stream
    ) : Screen
}

@Composable
@Preview
fun App() {
    KoinContext {
        val settingsViewModel: SettingsViewModel = koinViewModel()
        val profile by settingsViewModel.profile.collectAsState()
        val isDarkTheme = profile?.themeId != "light" // Default to dark for premium feel

        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
        val navigationHistory = remember { mutableStateListOf<Screen>(Screen.Home) }

        fun navigateTo(screen: Screen) {
            if (screen is Screen.Home || screen is Screen.Search || screen is Screen.Library || screen is Screen.Settings) {
                navigationHistory.clear()
            }
            navigationHistory.add(screen)
            currentScreen = screen
        }

        fun navigateBack() {
            if (navigationHistory.size > 1) {
                navigationHistory.removeAt(navigationHistory.size - 1)
                currentScreen = navigationHistory.last()
            }
        }

        // Handle System Back Press
        androidx.activity.compose.BackHandler(enabled = navigationHistory.size > 1) {
            navigateBack()
        }

        RovoTheme(darkTheme = isDarkTheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    bottomBar = {
                        if (currentScreen is Screen.Home || currentScreen is Screen.Search || currentScreen is Screen.Library || currentScreen is Screen.Settings) {
                            NavigationBar(
                                containerColor = NavBackground,
                                contentColor = NavSelected,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
                                    selected = currentScreen is Screen.Home,
                                    onClick = { navigateTo(Screen.Home) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NavSelected,
                                        selectedTextColor = NavSelected,
                                        unselectedIconColor = NavUnselected,
                                        unselectedTextColor = NavUnselected,
                                        indicatorColor = Color.Transparent
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    label = { Text("Search", style = MaterialTheme.typography.labelSmall) },
                                    selected = currentScreen is Screen.Search,
                                    onClick = { navigateTo(Screen.Search) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NavSelected,
                                        selectedTextColor = NavSelected,
                                        unselectedIconColor = NavUnselected,
                                        unselectedTextColor = NavUnselected,
                                        indicatorColor = Color.Transparent
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.List, contentDescription = "Library") },
                                    label = { Text("Library", style = MaterialTheme.typography.labelSmall) },
                                    selected = currentScreen is Screen.Library,
                                    onClick = { navigateTo(Screen.Library) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NavSelected,
                                        selectedTextColor = NavSelected,
                                        unselectedIconColor = NavUnselected,
                                        unselectedTextColor = NavUnselected,
                                        indicatorColor = Color.Transparent
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings", style = MaterialTheme.typography.labelSmall) },
                                    selected = currentScreen is Screen.Settings,
                                    onClick = { navigateTo(Screen.Settings) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NavSelected,
                                        selectedTextColor = NavSelected,
                                        unselectedIconColor = NavUnselected,
                                        unselectedTextColor = NavUnselected,
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (val screen = currentScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    onItemClick = { type, id ->
                                        navigateTo(Screen.Details(type, id))
                                    }
                                )
                            }
                            is Screen.Search -> {
                                SearchScreen(
                                    onItemClick = { type, id ->
                                        navigateTo(Screen.Details(type, id))
                                    },
                                    onBack = { navigateBack() }
                                )
                            }
                            is Screen.Library -> {
                                LibraryScreen(
                                    onItemClick = { type, id ->
                                        navigateTo(Screen.Details(type, id))
                                    }
                                )
                            }
                            is Screen.Settings -> {
                                SettingsScreen(
                                    onBack = { navigateBack() },
                                    onManageAddons = { navigateTo(Screen.AddonManagement) }
                                )
                            }
                            is Screen.AddonManagement -> {
                                AddonManagementScreen(
                                    onBack = { navigateBack() }
                                )
                            }
                            is Screen.Details -> {
                                DetailsScreen(
                                    type = screen.type,
                                    id = screen.id,
                                    onBack = { navigateBack() },
                                    onPlayStream = { stream, meta, video ->
                                        stream.url?.let { url ->
                                            navigateTo(Screen.Player(url, meta, video, stream))
                                        }
                                    }
                                )
                            }
                            is Screen.Player -> {
                                VideoPlayerScreen(
                                    url = screen.url,
                                    meta = screen.meta,
                                    video = screen.video,
                                    stream = screen.stream,
                                    onBack = { navigateBack() }
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
fun VideoPlayerScreen(
    url: String,
    meta: MetaItem,
    video: MetaVideo?,
    stream: Stream,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    LaunchedEffect(meta, video, stream) {
        viewModel.setPlaybackInfo(meta, video, stream)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VideoPlayer(
            url = url,
            meta = meta,
            modifier = Modifier.fillMaxSize(),
            onBack = onBack,
            onProgress = { pos, dur ->
                viewModel.updateProgress(pos, dur)
            }
        )
    }
}
