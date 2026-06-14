package com.rovo.app.ui.addons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.ui.addons.AddonManagementViewModel
import com.rovo.shared.ui.addons.InstallState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonManagementScreen(
    onBack: () -> Unit,
    viewModel: AddonManagementViewModel = koinViewModel()
) {
    val addons by viewModel.addons.collectAsState()
    val installState by viewModel.installState.collectAsState()
    var showInstallDialog by remember { mutableStateOf(false) }
    var addonUrl by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.rovo.colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Addons", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.rovo.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showInstallDialog = true },
                containerColor = MaterialTheme.rovo.colors.primary,
                contentColor = MaterialTheme.rovo.colors.onPrimary,
                shape = MaterialTheme.rovo.shapes.button,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Install Addon") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (addons.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "No Addons Installed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.rovo.colors.textPrimary
                    )
                    Text(
                        "Install addons like Cinemeta or Torrentio to see content.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.rovo.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(addons, key = { it.transportUrl }) { addon ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.rovo.colors.surface,
                        shape = MaterialTheme.rovo.shapes.card,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.rovo.colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (addon.iconUrl != null) {
                                    coil3.compose.AsyncImage(
                                        model = addon.iconUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Extension, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.rovo.colors.primary
                                    )
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = addon.name, 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.rovo.colors.textPrimary
                                )
                                Text(
                                    text = addon.transportUrl.removePrefix("https://").removePrefix("http://"), 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.rovo.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            
                            IconButton(
                                onClick = { viewModel.removeAddon(addon.transportUrl) },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }

            if (installState is InstallState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.rovo.colors.primary)
                }
            }
        }
    }

    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { 
                showInstallDialog = false
                viewModel.resetInstallState()
            },
            title = { Text("Install Addon", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter the manifest.json URL of the Stremio addon.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.rovo.colors.textSecondary
                    )
                    OutlinedTextField(
                        value = addonUrl,
                        onValueChange = { addonUrl = it },
                        label = { Text("Manifest URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.rovo.shapes.card,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.rovo.colors.primary,
                            unfocusedBorderColor = MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.3f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.installAddon(addonUrl) },
                    shape = MaterialTheme.rovo.shapes.button,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.rovo.colors.primary)
                ) {
                    Text("Install", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDialog = false }) {
                    Text("Cancel", color = MaterialTheme.rovo.colors.textSecondary)
                }
            },
            containerColor = MaterialTheme.rovo.colors.surface,
            shape = MaterialTheme.rovo.shapes.card
        )
    }

    LaunchedEffect(installState) {
        if (installState is InstallState.Success) {
            showInstallDialog = false
            addonUrl = ""
            viewModel.resetInstallState()
        }
    }
}
