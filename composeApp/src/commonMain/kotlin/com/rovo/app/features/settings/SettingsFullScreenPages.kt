package com.rovo.app.features.settings

import com.rovo.app.core.build.AppFeaturePolicy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rovo.app.core.ui.RovoScreen
import com.rovo.app.core.ui.RovoScreenHeader
import com.rovo.app.features.addons.AddonRepository
import com.rovo.app.features.collection.CollectionRepository
import com.rovo.app.features.details.MetaScreenSettingsRepository
import com.rovo.app.features.plugins.PluginRepository
import com.rovo.app.features.home.HomeCatalogSettingsRepository
import com.rovo.app.features.watchprogress.ContinueWatchingPreferencesRepository
import rovo.composeapp.generated.resources.Res
import rovo.composeapp.generated.resources.compose_settings_page_addons
import rovo.composeapp.generated.resources.compose_settings_page_continue_watching
import rovo.composeapp.generated.resources.compose_settings_page_homescreen
import rovo.composeapp.generated.resources.compose_settings_page_meta_screen
import rovo.composeapp.generated.resources.compose_settings_page_plugins
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomescreenSettingsScreen(
    onBack: () -> Unit,
) {
    val addonsUiState by AddonRepository.uiState.collectAsStateWithLifecycle()
    val homescreenCatalogRefreshKey = remember(addonsUiState.addons) {
        val allManifestsSettled = addonsUiState.addons.isNotEmpty() &&
            addonsUiState.addons.none { it.isRefreshing }
        if (!allManifestsSettled) return@remember emptyList<String>()
        addonsUiState.addons.mapNotNull { addon ->
            val manifest = addon.manifest ?: return@mapNotNull null
            buildString {
                append(manifest.transportUrl)
                append(':')
                append(manifest.catalogs.joinToString(separator = ",") { catalog ->
                    "${catalog.type}:${catalog.id}:${catalog.extra.count { it.isRequired }}"
                })
            }
        }
    }
    val homescreenSettingsUiState by remember {
        HomeCatalogSettingsRepository.snapshot()
        HomeCatalogSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val collections by CollectionRepository.collections.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        AddonRepository.initialize()
        CollectionRepository.initialize()
    }

    LaunchedEffect(homescreenCatalogRefreshKey) {
        if (homescreenCatalogRefreshKey.isEmpty()) return@LaunchedEffect
        HomeCatalogSettingsRepository.syncCatalogs(addonsUiState.addons)
    }

    LaunchedEffect(collections) {
        HomeCatalogSettingsRepository.syncCollections(collections)
    }

    RovoScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            RovoScreenHeader(
                title = stringResource(Res.string.compose_settings_page_homescreen),
                onBack = onBack,
            )
        }
        homescreenSettingsContent(
            isTablet = false,
            heroEnabled = homescreenSettingsUiState.heroEnabled,
            hideUnreleasedContent = homescreenSettingsUiState.hideUnreleasedContent,
            hideCatalogUnderline = homescreenSettingsUiState.hideCatalogUnderline,
            items = homescreenSettingsUiState.items,
        )
    }
}

@Composable
fun MetaScreenSettingsScreen(
    onBack: () -> Unit,
) {
    val metaScreenSettingsUiState by remember {
        MetaScreenSettingsRepository.ensureLoaded()
        MetaScreenSettingsRepository.uiState
    }.collectAsStateWithLifecycle()

    RovoScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            RovoScreenHeader(
                title = stringResource(Res.string.compose_settings_page_meta_screen),
                onBack = onBack,
            )
        }
        metaScreenSettingsContent(
            isTablet = false,
            uiState = metaScreenSettingsUiState,
        )
    }
}

@Composable
fun ContinueWatchingSettingsScreen(
    onBack: () -> Unit,
) {
    val continueWatchingPreferencesUiState by remember {
        ContinueWatchingPreferencesRepository.ensureLoaded()
        ContinueWatchingPreferencesRepository.uiState
    }.collectAsStateWithLifecycle()

    RovoScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            RovoScreenHeader(
                title = stringResource(Res.string.compose_settings_page_continue_watching),
                onBack = onBack,
            )
        }
        continueWatchingSettingsContent(
            isTablet = false,
            isVisible = continueWatchingPreferencesUiState.isVisible,
            style = continueWatchingPreferencesUiState.style,
            upNextFromFurthestEpisode = continueWatchingPreferencesUiState.upNextFromFurthestEpisode,
            useEpisodeThumbnails = continueWatchingPreferencesUiState.useEpisodeThumbnails,
            showUnairedNextUp = continueWatchingPreferencesUiState.showUnairedNextUp,
            blurNextUp = continueWatchingPreferencesUiState.blurNextUp,
            showResumePromptOnLaunch = continueWatchingPreferencesUiState.showResumePromptOnLaunch,
            sortMode = continueWatchingPreferencesUiState.sortMode,
        )
    }
}

@Composable
fun AddonsSettingsScreen(
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        AddonRepository.initialize()
    }

    RovoScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            RovoScreenHeader(
                title = stringResource(Res.string.compose_settings_page_addons),
                onBack = onBack,
            )
        }
        addonsSettingsContent()
    }
}

@Composable
fun PluginsSettingsScreen(
    onBack: () -> Unit,
) {
    if (!AppFeaturePolicy.pluginsEnabled) {
        AddonsSettingsScreen(onBack = onBack)
        return
    }

    LaunchedEffect(Unit) {
        PluginRepository.initialize()
    }

    RovoScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            RovoScreenHeader(
                title = stringResource(Res.string.compose_settings_page_plugins),
                onBack = onBack,
            )
        }
        pluginsSettingsContent()
    }
}


