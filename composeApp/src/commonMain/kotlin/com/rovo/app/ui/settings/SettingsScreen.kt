package com.rovo.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.ui.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManageAddons: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    var showDebridDialog by remember { mutableStateOf(false) }
    var showPlaybackDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showTraktDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.rovo.colors.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.rovo.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back", 
                            tint = MaterialTheme.rovo.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.rovo.colors.background.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            item {
                SettingsGroup(title = "Account & Services") {
                    SettingsItem(
                        icon = Icons.Default.AccountCircle,
                        title = "Profile",
                        subtitle = profile?.name ?: "Default Profile",
                        onClick = { showAccountDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.Cloud,
                        title = "Debrid Services",
                        subtitle = profile?.debridService?.uppercase() ?: "None",
                        onClick = { showDebridDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.Sync,
                        title = "Trakt.tv",
                        subtitle = "Sync history and watchlist",
                        onClick = { showTraktDialog = true }
                    )
                }
            }

            item {
                SettingsGroup(title = "Experience") {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Appearance",
                        subtitle = "Theme, layout, and poster styles",
                        onClick = { showAppearanceDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.PlayCircle,
                        title = "Playback",
                        subtitle = "Player settings and auto-play",
                        onClick = { showPlaybackDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Default.Extension,
                        title = "Addons",
                        subtitle = "Manage discovery and stream sources",
                        onClick = onManageAddons
                    )
                }
            }

            item {
                SettingsGroup(title = "System") {
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        title = "Cache & Storage",
                        subtitle = "Manage local data and downloads",
                        onClick = { }
                    )
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About Rovo",
                        subtitle = "Version 1.2.0-beta",
                        onClick = { }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Made with ❤️ for the community",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.rovo.colors.textMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showDebridDialog) {
        DebridSettingsDialog(
            profile = profile,
            onDismiss = { showDebridDialog = false },
            onUpdate = { update -> viewModel.updateProfile(update) }
        )
    }

    if (showPlaybackDialog) {
        PlaybackSettingsDialog(
            profile = profile,
            onDismiss = { showPlaybackDialog = false },
            onUpdate = { update -> viewModel.updateProfile(update) }
        )
    }

    if (showAppearanceDialog) {
        AppearanceSettingsDialog(
            profile = profile,
            onDismiss = { showAppearanceDialog = false },
            onUpdate = { update -> viewModel.updateProfile(update) }
        )
    }

    if (showTraktDialog) {
        TraktSettingsDialog(
            profile = profile,
            onDismiss = { showTraktDialog = false },
            onLogin = { 
                // In a real app, this would open a browser
                // For this demo, we'll just simulate success if needed or show a message
                // viewModel.getTraktAuthUrl("rovo://trakt")
            },
            onLogout = {
                viewModel.updateProfile { it.copy(traktToken = null) }
            }
        )
    }

    if (showAccountDialog) {
        AccountSettingsDialog(
            profile = profile,
            onDismiss = { showAccountDialog = false },
            onUpdate = { update -> viewModel.updateProfile(update) }
        )
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.rovo.colors.primary,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
            letterSpacing = 1.sp
        )
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.rovo.colors.surface.copy(alpha = 0.5f)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.rovo.colors.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(24.dp), 
                tint = MaterialTheme.rovo.colors.primary
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.rovo.colors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.rovo.colors.textSecondary
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
