package com.rovo.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.rovo.app.core.build.AppFeaturePolicy
import com.rovo.app.core.auth.AuthRepository
import com.rovo.app.core.auth.AuthState
import com.rovo.app.core.deeplink.AppDeepLink
import com.rovo.app.core.deeplink.AppDeepLinkRepository
import com.rovo.app.core.ui.RovoNavigationBar
import com.rovo.app.core.ui.RovoContinueWatchingActionSheet
import com.rovo.app.core.ui.RovoPosterActionSheet
import com.rovo.app.core.ui.RovoStatusModal
import com.rovo.app.core.ui.PlatformBackHandler
import com.rovo.app.core.ui.platformExitApp
import com.rovo.app.core.ui.configurePlatformImageLoader
import com.rovo.app.core.ui.RovoToastHost
import com.rovo.app.core.ui.RovoToastController
import com.rovo.app.core.ui.RovoFloatingPrompt
import com.rovo.app.core.ui.TraktListPickerDialog
import com.rovo.app.core.ui.RovoTheme
import com.rovo.app.core.ui.LocalRovoBottomNavigationOverlayPadding
import com.rovo.app.core.ui.NativeNavigationTab
import com.rovo.app.core.ui.NativeTabBridge
import com.rovo.app.core.ui.isLiquidGlassNativeTabBarSupported
import com.rovo.app.core.ui.localizedContinueWatchingSubtitle
import com.rovo.app.features.addons.AddonRepository
import com.rovo.app.features.catalog.CatalogRepository
import com.rovo.app.features.catalog.CatalogScreen
import com.rovo.app.features.catalog.INTERNAL_LIBRARY_MANIFEST_URL
import com.rovo.app.features.debrid.DirectDebridPlayableResult
import com.rovo.app.features.debrid.DirectDebridPlaybackResolver
import com.rovo.app.features.debrid.toastMessage
import com.rovo.app.features.downloads.DownloadsRepository
import com.rovo.app.features.downloads.DownloadsScreen
import com.rovo.app.features.details.MetaDetailsRepository
import com.rovo.app.features.details.MetaDetailsScreen
import com.rovo.app.features.details.MetaPerson
import com.rovo.app.features.details.PersonDetailScreen
import com.rovo.app.features.details.TmdbEntityBrowseScreen
import com.rovo.app.features.tmdb.TmdbEntityKind
import com.rovo.app.features.home.HomeCatalogSection
import com.rovo.app.features.home.HomeScreen
import com.rovo.app.features.home.MetaPreview
import com.rovo.app.features.library.LibraryItem
import com.rovo.app.features.library.LibraryRepository
import com.rovo.app.features.library.LibrarySection
import com.rovo.app.features.library.LibrarySourceMode
import com.rovo.app.features.library.LibraryScreen
import com.rovo.app.features.library.toLibraryItem
import com.rovo.app.features.notifications.EpisodeReleaseNotificationsRepository
import com.rovo.app.features.player.PlayerLaunch
import com.rovo.app.features.player.PlayerLaunchStore
import com.rovo.app.features.player.PlayerRoute
import com.rovo.app.features.player.PlayerScreen
import com.rovo.app.features.player.ExternalPlayerOpenResult
import com.rovo.app.features.player.ExternalPlayerPlatform
import com.rovo.app.features.player.ExternalPlayerPlaybackRequest
import com.rovo.app.features.player.sanitizePlaybackHeaders
import com.rovo.app.features.player.sanitizePlaybackResponseHeaders
import com.rovo.app.features.profiles.AvatarRepository
import com.rovo.app.features.profiles.RovoProfile
import com.rovo.app.features.profiles.ProfileEditScreen
import com.rovo.app.features.profiles.ProfileRepository
import com.rovo.app.features.profiles.ProfileSelectionScreen
import com.rovo.app.features.profiles.ProfileSwitcherTab
import com.rovo.app.features.profiles.profileAvatarImageUrl
import com.rovo.app.features.search.SearchScreen
import com.rovo.app.features.settings.SettingsScreen
import com.rovo.app.features.settings.HomescreenSettingsScreen
import com.rovo.app.features.settings.MetaScreenSettingsScreen
import com.rovo.app.features.settings.ContinueWatchingSettingsScreen
import com.rovo.app.features.settings.AddonsSettingsScreen
import com.rovo.app.features.settings.PluginsSettingsScreen
import com.rovo.app.features.settings.SupportersContributorsSettingsScreen
import com.rovo.app.features.settings.LicensesAttributionsSettingsScreen
import com.rovo.app.features.settings.ThemeSettingsRepository
import com.rovo.app.features.collection.CollectionManagementScreen
import com.rovo.app.features.collection.CollectionEditorScreen
import com.rovo.app.features.collection.CollectionEditorRepository

import com.rovo.app.features.trakt.TraktLibraryRepository
import com.rovo.app.features.collection.FolderDetailScreen
import com.rovo.app.features.collection.FolderDetailRepository
import com.rovo.app.features.streams.StreamAutoPlayPolicy
import com.rovo.app.features.streams.StreamItem
import com.rovo.app.features.streams.StreamLaunch
import com.rovo.app.features.streams.StreamLaunchStore
import com.rovo.app.features.streams.StreamLinkCacheRepository
import com.rovo.app.features.streams.StreamsRepository
import com.rovo.app.features.streams.StreamsScreen
import com.rovo.app.features.tmdb.TmdbService
import com.rovo.app.features.player.PlayerSettingsRepository
import com.rovo.app.features.trakt.TraktListTab
import com.rovo.app.features.updater.AppUpdaterHost
import com.rovo.app.features.updater.rememberAppUpdaterController
import com.rovo.app.features.watched.WatchedRepository
import com.rovo.app.features.watchprogress.ContinueWatchingItem
import com.rovo.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.rovo.app.features.watchprogress.ResumePromptRepository
import com.rovo.app.features.watchprogress.WatchProgressRepository
import com.rovo.app.features.watchprogress.nextUpDismissKey
import com.rovo.app.features.watching.application.WatchingActions
import com.rovo.app.features.watching.application.WatchingState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import rovo.composeapp.generated.resources.*

import rovo.composeapp.generated.resources.compose_catalog_subtitle_library
import rovo.composeapp.generated.resources.compose_catalog_subtitle_trakt_library
import rovo.composeapp.generated.resources.compose_nav_home
import rovo.composeapp.generated.resources.compose_nav_library
import rovo.composeapp.generated.resources.compose_nav_profile
import rovo.composeapp.generated.resources.compose_nav_search
import rovo.composeapp.generated.resources.sidebar_library
import rovo.composeapp.generated.resources.sidebar_search
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Serializable
object TabsRoute

@Serializable
data class DetailRoute(val type: String, val id: String)

@Serializable
data class PersonDetailRoute(
    val personId: Int,
    val personName: String,
    val personPhoto: String? = null,
    val castAvatarTransitionKey: String? = null,
    val preferCrew: Boolean = false,
)

@Serializable
data class EntityBrowseRoute(
    val entityKind: String,
    val entityId: Int,
    val entityName: String,
    val sourceType: String = "tv",
)

@Serializable
object HomescreenSettingsRoute

@Serializable
object MetaScreenSettingsRoute

@Serializable
object ContinueWatchingSettingsRoute

@Serializable
object DownloadsSettingsRoute

@Serializable
object AddonsSettingsRoute

@Serializable
object PluginsSettingsRoute

@Serializable
object SupportersContributorsSettingsRoute

@Serializable
object LicensesAttributionsSettingsRoute

@Serializable
object CollectionsRoute

@Serializable
data class CollectionEditorRoute(val collectionId: String? = null)

@Serializable
data class FolderDetailRoute(val collectionId: String, val folderId: String)

@Serializable
data class StreamRoute(
    val launchId: Long,
)

@Serializable
data class CatalogRoute(
    val title: String,
    val subtitle: String,
    val manifestUrl: String,
    val type: String,
    val catalogId: String,
    val supportsPagination: Boolean = false,
    val genre: String? = null,
)

enum class AppScreenTab {
    Home,
    Search,
    Library,
    Settings,
}

private fun AppScreenTab.toNativeNavigationTab(): NativeNavigationTab = when (this) {
    AppScreenTab.Home -> NativeNavigationTab.Home
    AppScreenTab.Search -> NativeNavigationTab.Search
    AppScreenTab.Library -> NativeNavigationTab.Library
    AppScreenTab.Settings -> NativeNavigationTab.Settings
}

private fun NativeNavigationTab.toAppScreenTab(): AppScreenTab = when (this) {
    NativeNavigationTab.Home -> AppScreenTab.Home
    NativeNavigationTab.Search -> AppScreenTab.Search
    NativeNavigationTab.Library -> AppScreenTab.Library
    NativeNavigationTab.Settings -> AppScreenTab.Settings
}

private fun PlayerLaunch.toExternalPlayerPlaybackRequest(): ExternalPlayerPlaybackRequest =
    ExternalPlayerPlaybackRequest(
        sourceUrl = sourceUrl,
        title = title,
        streamTitle = streamTitle,
        sourceHeaders = sourceHeaders,
    )

private enum class AppGateScreen {
    ProfileSelection,
    ProfileEdit,
    Main,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .components {
                add(SvgDecoder.Factory())
            }
            .configurePlatformImageLoader()
            .build()
    }
    val selectedTheme by remember {
        ThemeSettingsRepository.ensureLoaded()
        ThemeSettingsRepository.selectedTheme
    }.collectAsStateWithLifecycle()
    val amoledEnabled by remember { ThemeSettingsRepository.amoledEnabled }.collectAsStateWithLifecycle()
    RovoTheme(appTheme = selectedTheme, amoled = amoledEnabled) {
        LaunchedEffect(Unit) {
            AuthRepository.initialize()
            val authState = AuthRepository.state.value
            if (authState is AuthState.Unauthenticated) {
                AuthRepository.signInAnonymously()
            }
            ProfileRepository.loadCachedProfiles()
            AvatarRepository.fetchAvatars()
        }

        val profileState by ProfileRepository.state.collectAsStateWithLifecycle()
        val profileAvatars by AvatarRepository.avatars.collectAsStateWithLifecycle()

        LaunchedEffect(
            profileState.activeProfile?.profileIndex,
            profileState.activeProfile?.name,
            profileState.activeProfile?.avatarColorHex,
            profileState.activeProfile?.avatarId,
            profileState.activeProfile?.avatarUrl,
            profileAvatars,
        ) {
            val activeProfile = profileState.activeProfile
            val avatarItem = activeProfile?.avatarId?.let { avatarId ->
                profileAvatars.find { it.id == avatarId }
            }
            NativeTabBridge.publishProfileTabIcon(
                name = activeProfile?.name,
                avatarColorHex = activeProfile?.avatarColorHex,
                avatarImageUrl = activeProfile?.let { profileAvatarImageUrl(it, avatarItem) },
                avatarBackgroundColorHex = avatarItem?.bgColor,
            )
        }

        var gateScreen by rememberSaveable { mutableStateOf(AppGateScreen.ProfileSelection.name) }
        var editingProfile by remember { mutableStateOf<RovoProfile?>(null) }

        AnimatedContent(
            targetState = gateScreen,
            label = "app_gate",
            transitionSpec = {
                (fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.94f))
                    .togetherWith(fadeOut(tween(250)))
            },
        ) { currentGate ->
            when (currentGate) {
                AppGateScreen.ProfileSelection.name -> {
                    PlatformBackHandler(enabled = gateScreen == AppGateScreen.ProfileSelection.name) {
                        gateScreen = AppGateScreen.Main.name
                    }
                    ProfileSelectionScreen(
                        onProfileSelected = { profile ->
                            ProfileRepository.selectProfile(profile.profileIndex)
                            gateScreen = AppGateScreen.Main.name
                        },
                        onEditProfile = { profile ->
                            editingProfile = profile
                            gateScreen = AppGateScreen.ProfileEdit.name
                        },
                        onAddProfile = {
                            editingProfile = null
                            gateScreen = AppGateScreen.ProfileEdit.name
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                AppGateScreen.ProfileEdit.name -> {
                    PlatformBackHandler(enabled = gateScreen == AppGateScreen.ProfileEdit.name) {
                        gateScreen = AppGateScreen.ProfileSelection.name
                    }
                    ProfileEditScreen(
                        profile = editingProfile,
                        onBack = { gateScreen = AppGateScreen.ProfileSelection.name },
                        onSaved = { gateScreen = AppGateScreen.ProfileSelection.name },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                AppGateScreen.Main.name -> {
                    MainAppContent(
                        onSwitchProfile = {
                            gateScreen = AppGateScreen.ProfileSelection.name
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun MainAppContent(
    onSwitchProfile: () -> Unit = {},
) {
        val navController = rememberNavController()
        val appUpdaterController = rememberAppUpdaterController()
        remember {
            EpisodeReleaseNotificationsRepository.ensureLoaded()
        }

        val hapticFeedback = LocalHapticFeedback.current
        val coroutineScope = rememberCoroutineScope()
        var selectedTab by rememberSaveable { mutableStateOf(AppScreenTab.Home) }
        var searchFocusRequestCount by remember { mutableStateOf(0) }
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val nativeRequestedTab by remember { NativeTabBridge.requestedTab }.collectAsStateWithLifecycle()
        val liquidGlassNativeTabBarEnabled by remember {
            ThemeSettingsRepository.liquidGlassNativeTabBarEnabled
        }.collectAsStateWithLifecycle()
        val liquidGlassNativeTabBarSupported = remember { isLiquidGlassNativeTabBarSupported() }
        var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
        var selectedPosterForActions by remember { mutableStateOf<MetaPreview?>(null) }
        var selectedContinueWatchingForActions by remember { mutableStateOf<ContinueWatchingItem?>(null) }
        var showLibraryListPicker by remember { mutableStateOf(false) }
        var pickerItem by remember { mutableStateOf<LibraryItem?>(null) }
        var pickerTitle by remember { mutableStateOf("") }
        var pickerTabs by remember { mutableStateOf<List<TraktListTab>>(emptyList()) }
        var pickerMembership by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
        var pickerPending by remember { mutableStateOf(false) }
        var pickerError by remember { mutableStateOf<String?>(null) }
        val addonsUiState by remember {
            AddonRepository.initialize()
            AddonRepository.uiState
        }.collectAsStateWithLifecycle()
        val libraryUiState by remember {
            LibraryRepository.ensureLoaded()
            LibraryRepository.uiState
        }.collectAsStateWithLifecycle()
        val authState by AuthRepository.state.collectAsStateWithLifecycle()
        val profileState by ProfileRepository.state.collectAsStateWithLifecycle()
    val playerSettingsUiState by remember {
        PlayerSettingsRepository.ensureLoaded()
        PlayerSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val watchedUiState by remember {
        WatchedRepository.ensureLoaded()
        WatchedRepository.uiState
    }.collectAsStateWithLifecycle()
    val downloadsUiState by remember {
        DownloadsRepository.ensureLoaded()
        DownloadsRepository.uiState
    }.collectAsStateWithLifecycle()
    val downloadedProviderLabel = stringResource(Res.string.provider_downloaded)
    val externalPlayerNotConfiguredText = stringResource(Res.string.external_player_not_configured)
    val externalPlayerUnavailableText = stringResource(Res.string.external_player_unavailable)
    val externalPlayerFailedText = stringResource(Res.string.external_player_failed)
    val isTraktLibrarySource = libraryUiState.sourceMode == LibrarySourceMode.TRAKT
    var initialHomeReady by rememberSaveable { mutableStateOf(false) }
    var offlineLaunchRouteHandled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(nativeRequestedTab) {
        if (liquidGlassNativeTabBarSupported && liquidGlassNativeTabBarEnabled) {
            selectedTab = nativeRequestedTab.toAppScreenTab()
        }
    }

    LaunchedEffect(selectedTab) {
        NativeTabBridge.publishSelectedTab(selectedTab.toNativeNavigationTab())
        if (selectedTab != AppScreenTab.Search) {
            searchFocusRequestCount = 0
        }
    }

    DisposableEffect(
        navController,
        liquidGlassNativeTabBarSupported,
        liquidGlassNativeTabBarEnabled,
        initialHomeReady,
    ) {
        fun publishNativeTabVisibilityForCurrentRoute() {
            val visible = liquidGlassNativeTabBarSupported &&
                liquidGlassNativeTabBarEnabled &&
                initialHomeReady &&
                navController.currentDestination?.hasRoute<TabsRoute>() == true
            NativeTabBridge.publishTabBarVisible(visible)
        }

        val destinationChangedListener = NavController.OnDestinationChangedListener { _, _, _ ->
            publishNativeTabVisibilityForCurrentRoute()
        }

        publishNativeTabVisibilityForCurrentRoute()
        navController.addOnDestinationChangedListener(destinationChangedListener)
        onDispose {
            navController.removeOnDestinationChangedListener(destinationChangedListener)
            NativeTabBridge.publishTabBarVisible(false)
        }
    }

    LaunchedEffect(Unit) {
        EpisodeReleaseNotificationsRepository.refreshAsync()
        kotlinx.coroutines.delay(5_000)
        initialHomeReady = true
    }

    LaunchedEffect(initialHomeReady, offlineLaunchRouteHandled) {
        if (!initialHomeReady || offlineLaunchRouteHandled) return@LaunchedEffect
        offlineLaunchRouteHandled = true
    }

    LaunchedEffect(initialHomeReady, offlineLaunchRouteHandled) {
        if (!initialHomeReady || offlineLaunchRouteHandled) return@LaunchedEffect
        offlineLaunchRouteHandled = true
    }

    var profileSwitchLoading by remember { mutableStateOf(false) }
    var resumePromptItem by remember { mutableStateOf<ContinueWatchingItem?>(null) }
    val continueWatchingPreferencesUiState by remember {
        ContinueWatchingPreferencesRepository.ensureLoaded()
        ContinueWatchingPreferencesRepository.uiState
    }.collectAsStateWithLifecycle()

    LaunchedEffect(
        initialHomeReady,
        profileSwitchLoading,
        profileState.activeProfile?.profileIndex,
        continueWatchingPreferencesUiState.showResumePromptOnLaunch,
    ) {
        if (!initialHomeReady || profileSwitchLoading) return@LaunchedEffect
        if (resumePromptItem != null) return@LaunchedEffect
        if (continueWatchingPreferencesUiState.showResumePromptOnLaunch) {
            resumePromptItem = ResumePromptRepository.consumeResumePrompt()
        }
    }

        LaunchedEffect(navController) {
            AppDeepLinkRepository.pendingDeepLink.collectLatest { deepLink ->
                when (deepLink) {
                    is AppDeepLink.Meta -> {
                        selectedTab = AppScreenTab.Home
                        navController.navigate(DetailRoute(type = deepLink.type, id = deepLink.id)) {
                            launchSingleTop = true
                        }
                        AppDeepLinkRepository.markConsumed(deepLink)
                    }

                    AppDeepLink.Downloads -> {
                        selectedTab = AppScreenTab.Settings
                        navController.navigate(DownloadsSettingsRoute) {
                            launchSingleTop = true
                        }
                        AppDeepLinkRepository.markConsumed(deepLink)
                    }

                    null -> Unit
                }
            }
        }

        fun openExternalPlayback(launch: PlayerLaunch): Boolean {
            return when (
                ExternalPlayerPlatform.open(
                    request = launch.toExternalPlayerPlaybackRequest(),
                    playerId = playerSettingsUiState.externalPlayerId,
                )
            ) {
                ExternalPlayerOpenResult.Opened -> true
                ExternalPlayerOpenResult.NotConfigured -> {
                    RovoToastController.show(externalPlayerNotConfiguredText)
                    false
                }
                ExternalPlayerOpenResult.NoPlayerAvailable -> {
                    RovoToastController.show(externalPlayerUnavailableText)
                    false
                }
                ExternalPlayerOpenResult.Failed -> {
                    RovoToastController.show(externalPlayerFailedText)
                    false
                }
            }
        }

        fun launchPlaybackWithDownloadPreference(
            type: String,
            videoId: String,
            parentMetaId: String,
            parentMetaType: String,
            title: String,
            logo: String?,
            poster: String?,
            background: String?,
            seasonNumber: Int?,
            episodeNumber: Int?,
            episodeTitle: String?,
            episodeThumbnail: String?,
            pauseDescription: String?,
            resumePositionMs: Long?,
            resumeProgressFraction: Float?,
            manualSelection: Boolean,
            startFromBeginning: Boolean,
        ) {
            val targetResumePositionMs = if (startFromBeginning) 0L else (resumePositionMs ?: 0L)
            val targetResumeProgressFraction = if (startFromBeginning) null else resumeProgressFraction

            if (!manualSelection) {
                val downloadedItem = DownloadsRepository.findPlayableDownload(
                    parentMetaId = parentMetaId,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    videoId = videoId,
                )
                val localSourceUrl = downloadedItem?.let(DownloadsRepository::playableLocalFileUri)
                if (!localSourceUrl.isNullOrBlank()) {
                    val playerLaunch = PlayerLaunch(
                            title = title,
                            sourceUrl = localSourceUrl,
                            sourceHeaders = emptyMap(),
                            sourceResponseHeaders = emptyMap(),
                            logo = logo,
                            poster = poster,
                            background = background,
                            seasonNumber = seasonNumber,
                            episodeNumber = episodeNumber,
                            episodeTitle = episodeTitle,
                            episodeThumbnail = episodeThumbnail,
                            streamTitle = downloadedItem.streamTitle.ifBlank { title },
                            streamSubtitle = downloadedItem.streamSubtitle,
                            pauseDescription = pauseDescription,
                            providerName = downloadedItem.providerName.ifBlank { downloadedProviderLabel },
                            providerAddonId = downloadedItem.providerAddonId,
                            contentType = type,
                            videoId = videoId,
                            parentMetaId = parentMetaId,
                            parentMetaType = parentMetaType,
                            initialPositionMs = targetResumePositionMs,
                            initialProgressFraction = targetResumeProgressFraction,
                        )
                    if (playerSettingsUiState.externalPlayerEnabled) {
                        openExternalPlayback(playerLaunch)
                        return
                    }
                    val launchId = PlayerLaunchStore.put(playerLaunch)
                    navController.navigate(PlayerRoute(launchId = launchId))
                    return
                }
            }

            val streamLaunchId = StreamLaunchStore.put(
                StreamLaunch(
                    type = type,
                    videoId = videoId,
                    parentMetaId = parentMetaId,
                    parentMetaType = parentMetaType,
                    title = title,
                    logo = logo,
                    poster = poster,
                    background = background,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    episodeTitle = episodeTitle,
                    episodeThumbnail = episodeThumbnail,
                    pauseDescription = pauseDescription,
                    resumePositionMs = if (startFromBeginning) 0L else resumePositionMs,
                    resumeProgressFraction = targetResumeProgressFraction,
                    manualSelection = manualSelection,
                    startFromBeginning = startFromBeginning,
                ),
            )
            navController.navigate(
                StreamRoute(launchId = streamLaunchId),
            )
        }

        val onPlay: (String, String, String, String, String, String?, String?, String?, Int?, Int?, String?, String?, String?, Long?) -> Unit =
            { type, videoId, parentMetaId, parentMetaType, title, logo, poster, background, seasonNumber, episodeNumber, episodeTitle, episodeThumbnail, pauseDescription, resumePositionMs ->
                launchPlaybackWithDownloadPreference(
                    type = type,
                    videoId = videoId,
                    parentMetaId = parentMetaId,
                    parentMetaType = parentMetaType,
                    title = title,
                    logo = logo,
                    poster = poster,
                    background = background,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    episodeTitle = episodeTitle,
                    episodeThumbnail = episodeThumbnail,
                    pauseDescription = pauseDescription,
                    resumePositionMs = resumePositionMs,
                    resumeProgressFraction = null,
                    manualSelection = false,
                    startFromBeginning = false,
                )
            }

        val onPlayManually: (String, String, String, String, String, String?, String?, String?, Int?, Int?, String?, String?, String?, Long?) -> Unit =
            { type, videoId, parentMetaId, parentMetaType, title, logo, poster, background, seasonNumber, episodeNumber, episodeTitle, episodeThumbnail, pauseDescription, resumePositionMs ->
                launchPlaybackWithDownloadPreference(
                    type = type,
                    videoId = videoId,
                    parentMetaId = parentMetaId,
                    parentMetaType = parentMetaType,
                    title = title,
                    logo = logo,
                    poster = poster,
                    background = background,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    episodeTitle = episodeTitle,
                    episodeThumbnail = episodeThumbnail,
                    pauseDescription = pauseDescription,
                    resumePositionMs = resumePositionMs,
                    resumeProgressFraction = null,
                    manualSelection = true,
                    startFromBeginning = false,
                )
            }

        val onCatalogClick: (HomeCatalogSection) -> Unit = { section ->
            navController.navigate(
                CatalogRoute(
                    title = section.title,
                    subtitle = section.subtitle,
                    manifestUrl = section.manifestUrl,
                    type = section.type,
                    catalogId = section.catalogId,
                    supportsPagination = section.supportsPagination,
                ),
            )
        }

        val librarySectionSubtitle = if (libraryUiState.sourceMode == LibrarySourceMode.TRAKT) {
            stringResource(Res.string.compose_catalog_subtitle_trakt_library)
        } else {
            stringResource(Res.string.compose_catalog_subtitle_library)
        }

        val onLibrarySectionViewAllClick: (LibrarySection) -> Unit = { section ->
            navController.navigate(
                CatalogRoute(
                    title = section.displayTitle,
                    subtitle = librarySectionSubtitle,
                    manifestUrl = INTERNAL_LIBRARY_MANIFEST_URL,
                    type = section.items.firstOrNull()?.type ?: "movie",
                    catalogId = section.type,
                    supportsPagination = false,
                ),
            )
        }

        val openContinueWatching: (ContinueWatchingItem, Boolean, Boolean) -> Unit = { item, manualSelection, startFromBeginning ->
            launchPlaybackWithDownloadPreference(
                type = item.parentMetaType,
                videoId = item.videoId,
                parentMetaId = item.parentMetaId,
                parentMetaType = item.parentMetaType,
                title = item.title,
                logo = item.logo,
                poster = item.poster,
                background = item.background,
                seasonNumber = item.seasonNumber,
                episodeNumber = item.episodeNumber,
                episodeTitle = item.episodeTitle,
                episodeThumbnail = item.episodeThumbnail,
                pauseDescription = item.pauseDescription,
                resumePositionMs = item.resumePositionMs,
                resumeProgressFraction = item.resumeProgressFraction,
                manualSelection = manualSelection,
                startFromBeginning = startFromBeginning,
            )
        }

        val onContinueWatchingClick: (ContinueWatchingItem) -> Unit = { item ->
            openContinueWatching(item, false, false)
        }

        val onContinueWatchingStartFromBeginning: (ContinueWatchingItem) -> Unit = { item ->
            openContinueWatching(item, false, true)
        }

        val onContinueWatchingPlayManually: (ContinueWatchingItem) -> Unit = { item ->
            openContinueWatching(item, true, false)
        }

        val onContinueWatchingLongPress: (ContinueWatchingItem) -> Unit = { item ->
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedContinueWatchingForActions = item
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = TabsRoute,
                    modifier = Modifier.fillMaxSize(),
                ) {
                composable<TabsRoute> {
                    PlatformBackHandler(
                        enabled = true,
                        onBack = {
                            if (selectedTab != AppScreenTab.Home) {
                                selectedTab = AppScreenTab.Home
                            } else {
                                showExitConfirmation = !showExitConfirmation
                            }
                        },
                    )

                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isTabletLayout = maxWidth >= 768.dp
                        val useNativeBottomTabs =
                            liquidGlassNativeTabBarSupported && liquidGlassNativeTabBarEnabled && initialHomeReady
                        val tabsRouteActive = currentBackStackEntry?.destination?.hasRoute<TabsRoute>() == true
                        val onProfileSelected: (RovoProfile) -> Unit = { profile ->
                            profileSwitchLoading = true
                            selectedTab = AppScreenTab.Home
                            ProfileRepository.selectProfile(profile.profileIndex)
                            // profile switched
                        }

                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (initialHomeReady) 1f else 0f),
                            containerColor = Color.Transparent,
                            contentWindowInsets = WindowInsets(0),
                            bottomBar = {
                                if (!isTabletLayout && !useNativeBottomTabs) {
                                    RovoNavigationBar {
                                        NavItem(
                                            selected = selectedTab == AppScreenTab.Home,
                                            onClick = { selectedTab = AppScreenTab.Home },
                                            icon = Icons.Filled.Home,
                                            contentDescription = stringResource(Res.string.compose_nav_home),
                                        )
                                        NavItem(
                                            selected = selectedTab == AppScreenTab.Search,
                                            onClick = {
                                                if (selectedTab == AppScreenTab.Search) {
                                                    searchFocusRequestCount++
                                                } else {
                                                    selectedTab = AppScreenTab.Search
                                                }
                                            },
                                            icon = Res.drawable.sidebar_search,
                                            contentDescription = stringResource(Res.string.compose_nav_search),
                                        )
                                        NavItem(
                                            selected = selectedTab == AppScreenTab.Library,
                                            onClick = { selectedTab = AppScreenTab.Library },
                                            icon = Res.drawable.sidebar_library,
                                            contentDescription = stringResource(Res.string.compose_nav_library),
                                        )
                                        NavItem(
                                            selected = selectedTab == AppScreenTab.Settings,
                                            onClick = { selectedTab = AppScreenTab.Settings },
                                        ) {
                                            ProfileSwitcherTab(
                                                selected = selectedTab == AppScreenTab.Settings,
                                                onClick = { selectedTab = AppScreenTab.Settings },
                                                onProfileSelected = onProfileSelected,
                                                onAddProfileRequested = onSwitchProfile,
                                            )
                                        }
                                    }
                                }
                            },
                        ) { innerPadding ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                CompositionLocalProvider(
                                    LocalRovoBottomNavigationOverlayPadding provides if (useNativeBottomTabs) 49.dp else 0.dp,
                                ) {
                                    AppTabHost(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding),
                                        selectedTab = selectedTab,
                                        searchFocusRequestCount = searchFocusRequestCount,
                                        animateHomeCollectionGifs = tabsRouteActive,
                                        onCatalogClick = onCatalogClick,
                                        onPosterClick = { meta ->
                                            navController.navigate(DetailRoute(type = meta.type, id = meta.id))
                                        },
                                        onPosterLongClick = { meta ->
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedPosterForActions = meta
                                        },
                                        onLibraryPosterClick = { item ->
                                            navController.navigate(DetailRoute(type = item.type, id = item.id))
                                        },
                                        onLibrarySectionViewAllClick = onLibrarySectionViewAllClick,
                                        onContinueWatchingClick = onContinueWatchingClick,
                                        onContinueWatchingLongPress = onContinueWatchingLongPress,
                                        onSwitchProfile = onSwitchProfile,
                                        onHomescreenSettingsClick = { navController.navigate(HomescreenSettingsRoute) },
                                        onMetaScreenSettingsClick = { navController.navigate(MetaScreenSettingsRoute) },
                                        onContinueWatchingSettingsClick = { navController.navigate(ContinueWatchingSettingsRoute) },
                                        onDownloadsSettingsClick = { navController.navigate(DownloadsSettingsRoute) },
                                        onAddonsSettingsClick = { navController.navigate(AddonsSettingsRoute) },
                                        onPluginsSettingsClick = {
                                            if (AppFeaturePolicy.pluginsEnabled) {
                                                navController.navigate(PluginsSettingsRoute)
                                            }
                                        },
                                        onSupportersContributorsSettingsClick = {
                                            navController.navigate(SupportersContributorsSettingsRoute)
                                        },
                                        onLicensesAttributionsSettingsClick = {
                                            navController.navigate(LicensesAttributionsSettingsRoute)
                                        },
                                        onCheckForUpdatesClick = if (AppFeaturePolicy.inAppUpdaterEnabled) {
                                            {
                                                appUpdaterController.checkForUpdates(
                                                    force = true,
                                                    showNoUpdateFeedback = true,
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                        onCollectionsSettingsClick = { navController.navigate(CollectionsRoute) },
                                        onFolderClick = { collectionId, folderId ->
                                            navController.navigate(FolderDetailRoute(collectionId = collectionId, folderId = folderId))
                                        },
                                        onInitialHomeContentRendered = { initialHomeReady = true },
                                    )
                                }

                                if (isTabletLayout && !useNativeBottomTabs) {
                                    TabletFloatingTopBar(
                                        selectedTab = selectedTab,
                                        onTabSelected = { tab ->
                                            if (tab == AppScreenTab.Search && selectedTab == AppScreenTab.Search) {
                                                searchFocusRequestCount++
                                            } else {
                                                selectedTab = tab
                                            }
                                        },
                                        onProfileSelected = onProfileSelected,
                                        onAddProfileRequested = onSwitchProfile,
                                    )
                                }
                            }
                        }
                    }
                }
                composable<DetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<DetailRoute>()
                    val directorRole = stringResource(Res.string.person_role_director)
                    val writerRole = stringResource(Res.string.person_role_writer)
                    val creatorRole = stringResource(Res.string.person_role_creator)
                    MetaDetailsScreen(
                        type = route.type,
                        id = route.id,
                        onBack = {
                            navController.popBackStack()
                        },
                        onPlay = onPlay,
                        onPlayManually = onPlayManually,
                        onOpenMeta = { preview ->
                            coroutineScope.launch {
                                val resolvedId = if (preview.id.startsWith("tmdb:")) {
                                    val tmdbId = preview.id.removePrefix("tmdb:").toIntOrNull()
                                    tmdbId?.let {
                                        TmdbService.tmdbToImdb(
                                            tmdbId = it,
                                            mediaType = preview.type,
                                        )
                                    } ?: preview.id
                                } else {
                                    preview.id
                                }
                                navController.navigate(
                                    DetailRoute(
                                        type = preview.type,
                                        id = resolvedId,
                                    ),
                                )
                            }
                        },
                        onCastClick = { person, avatarTransitionKey ->
                            val tmdbId = person.tmdbId
                            if (tmdbId != null && tmdbId > 0) {
                                navController.navigate(
                                    PersonDetailRoute(
                                        personId = tmdbId,
                                        personName = person.name,
                                        personPhoto = person.photo,
                                        castAvatarTransitionKey = avatarTransitionKey,
                                        preferCrew = person.role?.let {
                                            it.equals("Director", ignoreCase = true) ||
                                                it.equals(directorRole, ignoreCase = true) ||
                                                it.equals("Writer", ignoreCase = true) ||
                                                it.equals(writerRole, ignoreCase = true) ||
                                                it.equals("Creator", ignoreCase = true)
                                                || it.equals(creatorRole, ignoreCase = true)
                                        } ?: false,
                                    ),
                                )
                            }
                        },
                        onCompanyClick = { company, entityKind ->
                            val tmdbId = company.tmdbId
                            if (tmdbId != null && tmdbId > 0) {
                                navController.navigate(
                                    EntityBrowseRoute(
                                        entityKind = entityKind,
                                        entityId = tmdbId,
                                        entityName = company.name,
                                        sourceType = route.type,
                                    ),
                                )
                            }
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<PersonDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<PersonDetailRoute>()
                    PersonDetailScreen(
                        personId = route.personId,
                        personName = route.personName,
                        initialProfilePhoto = route.personPhoto,
                        avatarTransitionKey = route.castAvatarTransitionKey,
                        preferCrew = route.preferCrew,
                        onBack = { navController.popBackStack() },
                        onOpenMeta = { preview ->
                            coroutineScope.launch {
                                val resolvedId = if (preview.id.startsWith("tmdb:")) {
                                    val tmdbId = preview.id.removePrefix("tmdb:").toIntOrNull()
                                    tmdbId?.let {
                                        TmdbService.tmdbToImdb(
                                            tmdbId = it,
                                            mediaType = preview.type,
                                        )
                                    } ?: preview.id
                                } else {
                                    preview.id
                                }
                                navController.navigate(
                                    DetailRoute(
                                        type = preview.type,
                                        id = resolvedId,
                                    ),
                                )
                            }
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<EntityBrowseRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<EntityBrowseRoute>()
                    TmdbEntityBrowseScreen(
                        entityKind = TmdbEntityKind.fromRouteValue(route.entityKind),
                        entityId = route.entityId,
                        entityName = route.entityName,
                        sourceType = route.sourceType,
                        onBack = { navController.popBackStack() },
                        onOpenMeta = { preview ->
                            coroutineScope.launch {
                                val resolvedId = if (preview.id.startsWith("tmdb:")) {
                                    val tmdbId = preview.id.removePrefix("tmdb:").toIntOrNull()
                                    tmdbId?.let {
                                        TmdbService.tmdbToImdb(
                                            tmdbId = it,
                                            mediaType = preview.type,
                                        )
                                    } ?: preview.id
                                } else {
                                    preview.id
                                }
                                navController.navigate(
                                    DetailRoute(
                                        type = preview.type,
                                        id = resolvedId,
                                    ),
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<StreamRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<StreamRoute>()
                    val launch = remember(route.launchId) {
                        StreamLaunchStore.get(route.launchId)
                    }
                    if (launch == null) {
                        LaunchedEffect(route.launchId) {
                            StreamsRepository.clear()
                            navController.popBackStack()
                        }
                        return@composable
                    }
                    val pauseDescription = launch.pauseDescription
                    val streamRouteScope = rememberCoroutineScope()
                    var resolvingDebridStream by rememberSaveable(route.launchId) { mutableStateOf(false) }
                    val lifecycleOwner = backStackEntry
                    DisposableEffect(lifecycleOwner, route.launchId) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_DESTROY) {
                                StreamLaunchStore.remove(route.launchId)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }
                    val shouldResolveEpisodeVideoId =
                        launch.parentMetaId != null &&
                            launch.seasonNumber != null &&
                            launch.episodeNumber != null
                    var effectiveVideoId by rememberSaveable(
                        launch.videoId,
                        launch.parentMetaId,
                        launch.seasonNumber,
                        launch.episodeNumber,
                    ) {
                        mutableStateOf(launch.videoId)
                    }
                    var hasResolvedVideoId by rememberSaveable(
                        launch.videoId,
                        launch.parentMetaId,
                        launch.seasonNumber,
                        launch.episodeNumber,
                    ) {
                        mutableStateOf(!shouldResolveEpisodeVideoId)
                    }

                    LaunchedEffect(
                        launch.videoId,
                        launch.parentMetaId,
                        launch.parentMetaType,
                        launch.type,
                        launch.seasonNumber,
                        launch.episodeNumber,
                    ) {
                        effectiveVideoId = launch.videoId
                        if (!shouldResolveEpisodeVideoId) {
                            hasResolvedVideoId = true
                            return@LaunchedEffect
                        }

                        hasResolvedVideoId = false
                        val metaType = launch.parentMetaType ?: launch.type
                        val metaId = launch.parentMetaId ?: return@LaunchedEffect
                        val resolvedVideoId = runCatching {
                            MetaDetailsRepository.fetch(metaType, metaId)
                        }.getOrNull()
                            ?.videos
                            ?.firstOrNull { video ->
                                video.season == launch.seasonNumber &&
                                    video.episode == launch.episodeNumber
                            }
                            ?.id
                            ?.takeIf { it.isNotBlank() }

                        effectiveVideoId = resolvedVideoId ?: launch.videoId
                        hasResolvedVideoId = true
                    }

                    val playerSettings by remember {
                        PlayerSettingsRepository.ensureLoaded()
                        PlayerSettingsRepository.uiState
                    }.collectAsStateWithLifecycle()

                    // Reuse Last Link: auto-play from cache if enabled (only on first entry)
                    var reuseHandled by rememberSaveable(launch.videoId, effectiveVideoId) { mutableStateOf(false) }
                    var reuseNavigated by remember { mutableStateOf(false) }
                    LaunchedEffect(effectiveVideoId, hasResolvedVideoId, playerSettings.streamReuseLastLinkEnabled, launch.manualSelection) {
                        if (!hasResolvedVideoId) return@LaunchedEffect
                        if (reuseHandled) return@LaunchedEffect
                        reuseHandled = true
                        if (launch.manualSelection) return@LaunchedEffect
                        if (!playerSettings.streamReuseLastLinkEnabled) return@LaunchedEffect
                        val cacheKey = StreamLinkCacheRepository.contentKey(
                            type = launch.type,
                            videoId = effectiveVideoId,
                            parentMetaId = launch.parentMetaId,
                            season = launch.seasonNumber,
                            episode = launch.episodeNumber,
                        )
                        val maxAgeMs = playerSettings.streamReuseLastLinkCacheHours * 60L * 60L * 1000L
                        val cached = StreamLinkCacheRepository.getValid(cacheKey, maxAgeMs)
                        if (cached != null) {
                            StreamsRepository.clear()
                            val playerLaunch = PlayerLaunch(
                                    title = launch.title,
                                    sourceUrl = cached.url,
                                    sourceHeaders = sanitizePlaybackHeaders(cached.requestHeaders),
                                    sourceResponseHeaders = sanitizePlaybackResponseHeaders(cached.responseHeaders),
                                    logo = launch.logo,
                                    poster = launch.poster,
                                    background = launch.background,
                                    seasonNumber = launch.seasonNumber,
                                    episodeNumber = launch.episodeNumber,
                                    episodeTitle = launch.episodeTitle,
                                    episodeThumbnail = launch.episodeThumbnail,
                                    streamTitle = cached.streamName,
                                    streamSubtitle = null,
                                    bingeGroup = cached.bingeGroup,
                                    pauseDescription = pauseDescription,
                                    providerName = cached.addonName,
                                    providerAddonId = cached.addonId,
                                    contentType = launch.type,
                                    videoId = effectiveVideoId,
                                    parentMetaId = launch.parentMetaId ?: effectiveVideoId,
                                    parentMetaType = launch.parentMetaType ?: launch.type,
                                    initialPositionMs = launch.resumePositionMs ?: 0L,
                                    initialProgressFraction = launch.resumeProgressFraction,
                                )
                            if (playerSettings.externalPlayerEnabled) {
                                openExternalPlayback(playerLaunch)
                                reuseNavigated = true
                                return@LaunchedEffect
                            }
                            reuseNavigated = true
                            val launchId = PlayerLaunchStore.put(playerLaunch)
                            navController.navigate(PlayerRoute(launchId = launchId)) {
                                popUpTo<StreamRoute> { inclusive = true }
                            }
                        }
                    }

                    val streamsUiState by StreamsRepository.uiState.collectAsStateWithLifecycle()
                    val expectedStreamsRequestToken = StreamsRepository.requestToken(
                        type = launch.type,
                        videoId = effectiveVideoId,
                        season = launch.seasonNumber,
                        episode = launch.episodeNumber,
                        manualSelection = launch.manualSelection,
                    )
                    var autoPlayHandled by rememberSaveable(launch.videoId, effectiveVideoId) { mutableStateOf(false) }
                    LaunchedEffect(
                        streamsUiState.autoPlayStream,
                        streamsUiState.requestToken,
                        expectedStreamsRequestToken,
                        reuseHandled,
                        launch.manualSelection,
                    ) {
                        if (!reuseHandled) return@LaunchedEffect
                        if (launch.manualSelection) return@LaunchedEffect
                        if (reuseNavigated) return@LaunchedEffect
                        if (autoPlayHandled) return@LaunchedEffect
                        if (streamsUiState.requestToken != expectedStreamsRequestToken) return@LaunchedEffect
                        val selectedStream = streamsUiState.autoPlayStream ?: return@LaunchedEffect
                        val stream = when (
                            val resolved = DirectDebridPlaybackResolver.resolveToPlayableStream(
                                stream = selectedStream,
                                season = launch.seasonNumber,
                                episode = launch.episodeNumber,
                            )
                        ) {
                            is DirectDebridPlayableResult.Success -> resolved.stream
                            else -> {
                                resolved.toastMessage()?.let { RovoToastController.show(it) }
                                StreamsRepository.consumeAutoPlay()
                                if (resolved == DirectDebridPlayableResult.Stale) {
                                    StreamsRepository.reload(
                                        type = launch.type,
                                        videoId = effectiveVideoId,
                                        season = launch.seasonNumber,
                                        episode = launch.episodeNumber,
                                        manualSelection = launch.manualSelection,
                                    )
                                }
                                return@LaunchedEffect
                            }
                        }
                        val sourceUrl = stream.directPlaybackUrl ?: return@LaunchedEffect
                        autoPlayHandled = true
                        if (playerSettings.streamReuseLastLinkEnabled) {
                            val cacheKey = StreamLinkCacheRepository.contentKey(
                                type = launch.type,
                                videoId = effectiveVideoId,
                                parentMetaId = launch.parentMetaId,
                                season = launch.seasonNumber,
                                episode = launch.episodeNumber,
                            )
                            StreamLinkCacheRepository.save(
                                contentKey = cacheKey,
                                url = sourceUrl,
                                streamName = stream.streamLabel,
                                addonName = stream.addonName,
                                addonId = stream.addonId,
                                requestHeaders = sanitizePlaybackHeaders(stream.behaviorHints.proxyHeaders?.request),
                                responseHeaders = sanitizePlaybackResponseHeaders(stream.behaviorHints.proxyHeaders?.response),
                                filename = stream.behaviorHints.filename,
                                videoSize = stream.behaviorHints.videoSize,
                                bingeGroup = stream.behaviorHints.bingeGroup,
                            )
                        }
                        val playerLaunch = PlayerLaunch(
                                title = launch.title,
                                sourceUrl = sourceUrl,
                                sourceHeaders = sanitizePlaybackHeaders(stream.behaviorHints.proxyHeaders?.request),
                                sourceResponseHeaders = sanitizePlaybackResponseHeaders(stream.behaviorHints.proxyHeaders?.response),
                                logo = launch.logo,
                                poster = launch.poster,
                                background = launch.background,
                                seasonNumber = launch.seasonNumber,
                                episodeNumber = launch.episodeNumber,
                                episodeTitle = launch.episodeTitle,
                                episodeThumbnail = launch.episodeThumbnail,
                                streamTitle = stream.streamLabel,
                                streamSubtitle = stream.streamSubtitle,
                                bingeGroup = stream.behaviorHints.bingeGroup,
                                pauseDescription = pauseDescription,
                                providerName = stream.addonName,
                                providerAddonId = stream.addonId,
                                contentType = launch.type,
                                videoId = effectiveVideoId,
                                parentMetaId = launch.parentMetaId ?: effectiveVideoId,
                                parentMetaType = launch.parentMetaType ?: launch.type,
                                initialPositionMs = launch.resumePositionMs ?: 0L,
                                initialProgressFraction = launch.resumeProgressFraction,
                            )
                        StreamsRepository.consumeAutoPlay()
                        StreamsRepository.cancelLoading()
                        if (playerSettings.externalPlayerEnabled) {
                            openExternalPlayback(playerLaunch)
                            return@LaunchedEffect
                        }
                        val launchId = PlayerLaunchStore.put(playerLaunch)
                        navController.navigate(PlayerRoute(launchId = launchId)) {
                            popUpTo<StreamRoute> { inclusive = true }
                        }
                    }

                    if (!hasResolvedVideoId) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                        return@composable
                    }

                    fun openSelectedStream(
                        stream: StreamItem,
                        resolvedResumePositionMs: Long?,
                        resolvedResumeProgressFraction: Float?,
                        forceExternal: Boolean,
                        forceInternal: Boolean,
                    ) {
                        if (stream.isDirectDebridStream && stream.directPlaybackUrl == null) {
                            if (resolvingDebridStream) return
                            streamRouteScope.launch {
                                resolvingDebridStream = true
                                val resolved = DirectDebridPlaybackResolver.resolveToPlayableStream(
                                    stream = stream,
                                    season = launch.seasonNumber,
                                    episode = launch.episodeNumber,
                                )
                                resolvingDebridStream = false
                                when (resolved) {
                                    is DirectDebridPlayableResult.Success -> openSelectedStream(
                                        stream = resolved.stream,
                                        resolvedResumePositionMs = resolvedResumePositionMs,
                                        resolvedResumeProgressFraction = resolvedResumeProgressFraction,
                                        forceExternal = forceExternal,
                                        forceInternal = forceInternal,
                                    )
                                    else -> {
                                        resolved.toastMessage()?.let { RovoToastController.show(it) }
                                        if (resolved == DirectDebridPlayableResult.Stale) {
                                            StreamsRepository.reload(
                                                type = launch.type,
                                                videoId = effectiveVideoId,
                                                season = launch.seasonNumber,
                                                episode = launch.episodeNumber,
                                                manualSelection = launch.manualSelection,
                                            )
                                        }
                                    }
                                }
                            }
                            return
                        }
                        val sourceUrl = stream.directPlaybackUrl ?: return
                        if (playerSettings.streamReuseLastLinkEnabled) {
                            val cacheKey = StreamLinkCacheRepository.contentKey(
                                type = launch.type,
                                videoId = effectiveVideoId,
                                parentMetaId = launch.parentMetaId,
                                season = launch.seasonNumber,
                                episode = launch.episodeNumber,
                            )
                            StreamLinkCacheRepository.save(
                                contentKey = cacheKey,
                                url = sourceUrl,
                                streamName = stream.streamLabel,
                                addonName = stream.addonName,
                                addonId = stream.addonId,
                                requestHeaders = sanitizePlaybackHeaders(stream.behaviorHints.proxyHeaders?.request),
                                responseHeaders = sanitizePlaybackResponseHeaders(stream.behaviorHints.proxyHeaders?.response),
                                filename = stream.behaviorHints.filename,
                                videoSize = stream.behaviorHints.videoSize,
                                bingeGroup = stream.behaviorHints.bingeGroup,
                            )
                        }
                        val playerLaunch = PlayerLaunch(
                            title = launch.title,
                            sourceUrl = sourceUrl,
                            sourceHeaders = sanitizePlaybackHeaders(stream.behaviorHints.proxyHeaders?.request),
                            sourceResponseHeaders = sanitizePlaybackResponseHeaders(stream.behaviorHints.proxyHeaders?.response),
                            logo = launch.logo,
                            poster = launch.poster,
                            background = launch.background,
                            seasonNumber = launch.seasonNumber,
                            episodeNumber = launch.episodeNumber,
                            episodeTitle = launch.episodeTitle,
                            episodeThumbnail = launch.episodeThumbnail,
                            streamTitle = stream.streamLabel,
                            streamSubtitle = stream.streamSubtitle,
                            bingeGroup = stream.behaviorHints.bingeGroup,
                            pauseDescription = pauseDescription,
                            providerName = stream.addonName,
                            providerAddonId = stream.addonId,
                            contentType = launch.type,
                            videoId = effectiveVideoId,
                            parentMetaId = launch.parentMetaId ?: effectiveVideoId,
                            parentMetaType = launch.parentMetaType ?: launch.type,
                            initialPositionMs = resolvedResumePositionMs ?: 0L,
                            initialProgressFraction = resolvedResumeProgressFraction,
                        )

                        if (!forceInternal && (forceExternal || playerSettings.externalPlayerEnabled)) {
                            openExternalPlayback(playerLaunch)
                            StreamsRepository.cancelLoading()
                            return
                        }

                        val launchId = PlayerLaunchStore.put(playerLaunch)
                        StreamsRepository.cancelLoading()
                        navController.navigate(
                            PlayerRoute(launchId = launchId)
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        StreamsScreen(
                            type = launch.type,
                            videoId = effectiveVideoId,
                            parentMetaId = launch.parentMetaId ?: effectiveVideoId,
                            parentMetaType = launch.parentMetaType ?: launch.type,
                            title = launch.title,
                            logo = launch.logo,
                            poster = launch.poster,
                            background = launch.background,
                            seasonNumber = launch.seasonNumber,
                            episodeNumber = launch.episodeNumber,
                            episodeTitle = launch.episodeTitle,
                            episodeThumbnail = launch.episodeThumbnail,
                            resumePositionMs = launch.resumePositionMs,
                            resumeProgressFraction = launch.resumeProgressFraction,
                            manualSelection = launch.manualSelection,
                            startFromBeginning = launch.startFromBeginning,
                            onStreamSelected = { stream, resolvedResumePositionMs, resolvedResumeProgressFraction ->
                                openSelectedStream(
                                    stream = stream,
                                    resolvedResumePositionMs = resolvedResumePositionMs,
                                    resolvedResumeProgressFraction = resolvedResumeProgressFraction,
                                    forceExternal = false,
                                    forceInternal = false,
                                )
                            },
                            onStreamActionOpen = { stream, openExternally, resolvedResumePositionMs, resolvedResumeProgressFraction ->
                                openSelectedStream(
                                    stream = stream,
                                    resolvedResumePositionMs = resolvedResumePositionMs,
                                    resolvedResumeProgressFraction = resolvedResumeProgressFraction,
                                    forceExternal = openExternally,
                                    forceInternal = !openExternally,
                                )
                            },
                            onBack = {
                                StreamsRepository.clear()
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                        if (resolvingDebridStream) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.82f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                    Text(
                                        text = stringResource(Res.string.streams_finding_source),
                                        color = Color.White.copy(alpha = 0.82f),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
                composable<PlayerRoute>(
                    enterTransition = {
                        if (isIos) fadeIn(animationSpec = tween(220)) else null
                    },
                    exitTransition = {
                        if (isIos) fadeOut(animationSpec = tween(220)) else null
                    },
                    popEnterTransition = {
                        if (isIos) fadeIn(animationSpec = tween(220)) else null
                    },
                    popExitTransition = {
                        if (isIos) fadeOut(animationSpec = tween(220)) else null
                    },
                ) { backStackEntry ->
                    val route = backStackEntry.toRoute<PlayerRoute>()
                    val launch = remember(route.launchId) { PlayerLaunchStore.get(route.launchId) }
                    if (launch == null) {
                        LaunchedEffect(route.launchId) {
                            navController.popBackStack()
                        }
                        Box(modifier = Modifier.fillMaxSize())
                        return@composable
                    }
                    LaunchedEffect(launch.videoId) {
                        launch.videoId?.let { ResumePromptRepository.markPlayerEntered(it) }
                    }
                    PlayerScreen(
                        title = launch.title,
                        sourceUrl = launch.sourceUrl,
                        sourceAudioUrl = launch.sourceAudioUrl,
                        sourceHeaders = launch.sourceHeaders,
                        sourceResponseHeaders = launch.sourceResponseHeaders,
                        logo = launch.logo,
                        poster = launch.poster,
                        background = launch.background,
                        seasonNumber = launch.seasonNumber,
                        episodeNumber = launch.episodeNumber,
                        episodeTitle = launch.episodeTitle,
                        episodeThumbnail = launch.episodeThumbnail,
                        streamTitle = launch.streamTitle,
                        streamSubtitle = launch.streamSubtitle,
                        initialBingeGroup = launch.bingeGroup,
                        pauseDescription = launch.pauseDescription,
                        providerName = launch.providerName,
                        providerAddonId = launch.providerAddonId,
                        contentType = launch.contentType,
                        videoId = launch.videoId,
                        parentMetaId = launch.parentMetaId,
                        parentMetaType = launch.parentMetaType,
                        initialPositionMs = launch.initialPositionMs,
                        initialProgressFraction = launch.initialProgressFraction,
                        onBack = {
                            ResumePromptRepository.markPlayerExitedNormally()
                            PlayerLaunchStore.remove(route.launchId)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<CatalogRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<CatalogRoute>()
                    CatalogScreen(
                        title = route.title,
                        subtitle = route.subtitle,
                        manifestUrl = route.manifestUrl,
                        type = route.type,
                        catalogId = route.catalogId,
                        supportsPagination = route.supportsPagination,
                        genre = route.genre,
                        onBack = {
                            CatalogRepository.clear()
                            navController.popBackStack()
                        },
                        onPosterClick = { meta ->
                            navController.navigate(DetailRoute(type = meta.type, id = meta.id))
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<HomescreenSettingsRoute> {
                    val onBack = rememberGuardedPopBackStack(
                        navController = navController,
                        backStackEntry = it,
                    )
                    HomescreenSettingsScreen(
                        onBack = onBack,
                    )
                }
                composable<MetaScreenSettingsRoute> { backStackEntry ->
                    val onBack = rememberGuardedPopBackStack(
                        navController = navController,
                        backStackEntry = backStackEntry,
                    )
                    MetaScreenSettingsScreen(
                        onBack = onBack,
                    )
                }
                composable<ContinueWatchingSettingsRoute> { backStackEntry ->
                    val onBack = rememberGuardedPopBackStack(
                        navController = navController,
                        backStackEntry = backStackEntry,
                    )
                    ContinueWatchingSettingsScreen(
                        onBack = onBack,
                    )
                }
                composable<DownloadsSettingsRoute> { backStackEntry ->
                    val onBack = rememberGuardedPopBackStack(
                        navController = navController,
                        backStackEntry = backStackEntry,
                    )
                    DownloadsScreen(
                        onBack = onBack,
                        onOpenDownload = { item ->
                            val sourceUrl = DownloadsRepository.playableLocalFileUri(item) ?: return@DownloadsScreen
                            val resumeEntry = item.videoId
                                .takeIf { it.isNotBlank() }
                                ?.let(WatchProgressRepository::progressForVideo)
                                ?.takeIf { it.isResumable }

                            val playerLaunch = PlayerLaunch(
                                    title = item.title,
                                    sourceUrl = sourceUrl,
                                    sourceHeaders = emptyMap(),
                                    sourceResponseHeaders = emptyMap(),
                                    logo = item.logo,
                                    poster = item.poster,
                                    background = item.background,
                                    seasonNumber = item.seasonNumber,
                                    episodeNumber = item.episodeNumber,
                                    episodeTitle = item.episodeTitle,
                                    episodeThumbnail = item.episodeThumbnail,
                                    streamTitle = item.streamTitle,
                                    streamSubtitle = item.streamSubtitle,
                                    providerName = item.providerName,
                                    providerAddonId = item.providerAddonId,
                                    contentType = item.contentType,
                                    videoId = item.videoId,
                                    parentMetaId = item.parentMetaId,
                                    parentMetaType = item.parentMetaType,
                                    initialPositionMs = resumeEntry?.lastPositionMs?.takeIf { it > 0L } ?: 0L,
                                    initialProgressFraction = resumeEntry?.progressFraction?.takeIf { it > 0f },
                            )
                            if (playerSettingsUiState.externalPlayerEnabled) {
                                openExternalPlayback(playerLaunch)
                                return@DownloadsScreen
                            }
                            val launchId = PlayerLaunchStore.put(playerLaunch)
                            navController.navigate(PlayerRoute(launchId = launchId))
                        },
                    )
                }
                composable<AddonsSettingsRoute> { backStackEntry ->
                    val onBack = rememberGuardedPopBackStack(
                        navController = navController,
                        backStackEntry = backStackEntry,
                    )
                    AddonsSettingsScreen(
                        onBack = onBack,
                    )
                }
                if (AppFeaturePolicy.pluginsEnabled) {
                    composable<PluginsSettingsRoute> { backStackEntry ->
                        val onBack = rememberGuardedPopBackStack(
                            navController = navController,
                            backStackEntry = backStackEntry,
                        )
                        PluginsSettingsScreen(
                            onBack = onBack,
                        )
                    }
                }
                composable<SupportersContributorsSettingsRoute> { backStackEntry ->
                    val onBack = rememberGuardedPopBackStack(
                        navController = navController,
                        backStackEntry = backStackEntry,
                    )
                    SupportersContributorsSettingsScreen(
                        onBack = onBack,
                    )
                }
                composable<LicensesAttributionsSettingsRoute> { backStackEntry ->
                    val onBack = rememberGuardedPopBackStack(
                        navController = navController,
                        backStackEntry = backStackEntry,
                    )
                    LicensesAttributionsSettingsScreen(
                        onBack = onBack,
                    )
                }
                composable<CollectionsRoute> { backStackEntry ->
                    val onBack = rememberGuardedPopBackStack(
                        navController = navController,
                        backStackEntry = backStackEntry,
                    )
                    CollectionManagementScreen(
                        onBack = onBack,
                        onNavigateToEditor = { collectionId ->
                            navController.navigate(CollectionEditorRoute(collectionId = collectionId))
                        },
                    )
                }
                composable<CollectionEditorRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<CollectionEditorRoute>()
                    CollectionEditorScreen(
                        collectionId = route.collectionId,
                        onBack = {
                            CollectionEditorRepository.clear()
                            navController.popBackStack()
                        },
                    )
                }
                composable<FolderDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<FolderDetailRoute>()
                    LaunchedEffect(route.collectionId, route.folderId) {
                        FolderDetailRepository.initialize(route.collectionId, route.folderId)
                    }
                    FolderDetailScreen(
                        onBack = {
                            FolderDetailRepository.clear()
                            navController.popBackStack()
                        },
                        onCatalogClick = onCatalogClick,
                        onPosterClick = { meta ->
                            navController.navigate(DetailRoute(type = meta.type, id = meta.id))
                        },
                    )
                }
                }
            }

            RovoPosterActionSheet(
                item = selectedPosterForActions,
                isSaved = selectedPosterForActions?.let { preview ->
                    LibraryRepository.isSaved(preview.id, preview.type)
                } == true,
                isWatched = selectedPosterForActions?.let { preview ->
                    WatchingState.isPosterWatched(
                        watchedKeys = watchedUiState.watchedKeys,
                        item = preview,
                    )
                } == true,
                onDismiss = { selectedPosterForActions = null },
                onToggleLibrary = {
                    selectedPosterForActions?.let { preview ->
                        val libraryItem = preview.toLibraryItem(savedAtEpochMs = 0L)
                        if (!isTraktLibrarySource) {
                            LibraryRepository.toggleSaved(libraryItem)
                        } else {
                            pickerItem = libraryItem
                            pickerTitle = preview.name
                            pickerTabs = LibraryRepository.libraryListTabs()
                            pickerMembership = pickerTabs.associate { it.key to false }
                            pickerPending = true
                            pickerError = null
                            showLibraryListPicker = true
                            coroutineScope.launch {
                                runCatching {
                                    val snapshot = LibraryRepository.getMembershipSnapshot(libraryItem)
                                    val tabs = LibraryRepository.libraryListTabs()
                                    pickerTabs = tabs
                                    pickerMembership = tabs.associate { tab ->
                                        tab.key to (snapshot[tab.key] == true)
                                    }
                                }.onFailure { error ->
                                    pickerError = error.message ?: getString(Res.string.trakt_lists_load_failed)
                                }
                                pickerPending = false
                            }
                        }
                    }
                },
                onToggleWatched = {
                    selectedPosterForActions?.let { preview ->
                        coroutineScope.launch {
                            WatchingActions.togglePosterWatched(preview)
                        }
                    }
                },
            )

            RovoContinueWatchingActionSheet(
                item = selectedContinueWatchingForActions,
                showManualPlayOption = StreamAutoPlayPolicy.isEffectivelyEnabled(playerSettingsUiState),
                onDismiss = { selectedContinueWatchingForActions = null },
                onOpenDetails = {
                    selectedContinueWatchingForActions?.let { item ->
                        navController.navigate(
                            DetailRoute(
                                type = item.parentMetaType,
                                id = item.parentMetaId,
                            ),
                        )
                    }
                },
                onStartFromBeginning = selectedContinueWatchingForActions
                    ?.takeIf { !it.isNextUp }
                    ?.let { item -> { onContinueWatchingStartFromBeginning(item) } },
                onPlayManually = selectedContinueWatchingForActions
                    ?.let { item -> { onContinueWatchingPlayManually(item) } },
                onRemove = {
                    selectedContinueWatchingForActions?.let { item ->
                        if (item.isNextUp) {
                            ContinueWatchingPreferencesRepository.addDismissedNextUpKey(
                                nextUpDismissKey(
                                    item.parentMetaId,
                                    item.nextUpSeedSeasonNumber,
                                    item.nextUpSeedEpisodeNumber,
                                ),
                            )
                        } else {
                            WatchProgressRepository.removeProgress(contentId = item.parentMetaId)
                        }
                    }
                },
            )

            TraktListPickerDialog(
                visible = showLibraryListPicker,
                title = pickerTitle,
                tabs = pickerTabs,
                membership = pickerMembership,
                isPending = pickerPending,
                errorMessage = pickerError,
                onToggle = { listKey ->
                    pickerMembership = pickerMembership.toMutableMap().apply {
                        this[listKey] = !(this[listKey] == true)
                    }
                },
                onDismiss = {
                    if (!pickerPending) {
                        showLibraryListPicker = false
                        pickerItem = null
                        pickerError = null
                    }
                },
                onSave = {
                    val item = pickerItem ?: return@TraktListPickerDialog
                    coroutineScope.launch {
                        pickerPending = true
                        pickerError = null
                        runCatching {
                            LibraryRepository.applyMembershipChanges(
                                item = item,
                                desiredMembership = pickerMembership,
                            )
                        }.onSuccess {
                            showLibraryListPicker = false
                            pickerItem = null
                            pickerError = null
                        }.onFailure { error ->
                            pickerError = error.message ?: getString(Res.string.trakt_lists_update_failed)
                        }
                        pickerPending = false
                    }
                },
            )

            RovoStatusModal(
                title = stringResource(Res.string.app_exit_title),
                message = stringResource(Res.string.app_exit_message),
                isVisible = showExitConfirmation,
                confirmText = stringResource(Res.string.action_yes),
                dismissText = stringResource(Res.string.action_no),
                onConfirm = {
                    showExitConfirmation = false
                    platformExitApp()
                },
                onDismiss = {
                    showExitConfirmation = false
                },
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = !initialHomeReady || profileSwitchLoading,
                enter = fadeIn(),
                exit = fadeOut(androidx.compose.animation.core.tween(400)),
            ) {
                AppLaunchOverlay(modifier = Modifier.fillMaxSize())
            }

            // Auto-dismiss profile switch overlay
            if (profileSwitchLoading) {
                LaunchedEffect(Unit) {
                    // Brief loading screen while home refreshes for the new profile
                    kotlinx.coroutines.delay(1200)
                    profileSwitchLoading = false
                }
            }

            RovoFloatingPrompt(
                visible = resumePromptItem != null,
                imageUrl = resumePromptItem?.poster ?: resumePromptItem?.imageUrl,
                title = resumePromptItem?.title.orEmpty(),
                subtitle = resumePromptItem?.let { localizedContinueWatchingSubtitle(it) }.orEmpty(),
                progressFraction = resumePromptItem?.progressFraction ?: 0f,
                actionLabel = stringResource(Res.string.resume_prompt_action),
                onAction = {
                    val item = resumePromptItem ?: return@RovoFloatingPrompt
                    resumePromptItem = null
                    openContinueWatching(item, false, false)
                },
                onDismiss = { resumePromptItem = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(15f),
            )

            RovoToastHost(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(20f),
            )

            AppUpdaterHost(
                controller = appUpdaterController,
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(25f),
            )
        }
}

@Composable
private fun rememberGuardedPopBackStack(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
    beforePop: () -> Unit = {},
): () -> Unit {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    var popHandled by remember(backStackEntry) { mutableStateOf(false) }

    return remember(navController, backStackEntry, currentBackStackEntry, popHandled, beforePop) {
        {
            if (!popHandled && currentBackStackEntry == backStackEntry) {
                popHandled = true
                beforePop()
                navController.popBackStack()
            }
        }
    }
}

@Composable
private fun AppTabHost(
    selectedTab: AppScreenTab,
    modifier: Modifier = Modifier,
    searchFocusRequestCount: Int = 0,
    animateHomeCollectionGifs: Boolean = true,
    onCatalogClick: ((HomeCatalogSection) -> Unit)? = null,
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
    onLibraryPosterClick: ((LibraryItem) -> Unit)? = null,
    onLibrarySectionViewAllClick: ((LibrarySection) -> Unit)? = null,
    onContinueWatchingClick: ((ContinueWatchingItem) -> Unit)? = null,
    onContinueWatchingLongPress: ((ContinueWatchingItem) -> Unit)? = null,
    onSwitchProfile: (() -> Unit)? = null,
    onHomescreenSettingsClick: () -> Unit = {},
    onMetaScreenSettingsClick: () -> Unit = {},
    onContinueWatchingSettingsClick: () -> Unit = {},
    onDownloadsSettingsClick: () -> Unit = {},
    onAddonsSettingsClick: () -> Unit = {},
    onPluginsSettingsClick: () -> Unit = {},
    onSupportersContributorsSettingsClick: () -> Unit = {},
    onLicensesAttributionsSettingsClick: () -> Unit = {},
    onCheckForUpdatesClick: (() -> Unit)? = null,
    onCollectionsSettingsClick: () -> Unit = {},
    onFolderClick: ((collectionId: String, folderId: String) -> Unit)? = null,
    onInitialHomeContentRendered: () -> Unit = {},
) {
    val tabStateHolder = rememberSaveableStateHolder()

    Box(modifier = modifier.fillMaxSize()) {
        tabStateHolder.SaveableStateProvider(selectedTab.name) {
            when (selectedTab) {
                AppScreenTab.Home -> {
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        animateCollectionGifs = animateHomeCollectionGifs,
                        onCatalogClick = onCatalogClick,
                        onPosterClick = onPosterClick,
                        onPosterLongClick = onPosterLongClick,
                        onContinueWatchingClick = onContinueWatchingClick,
                        onContinueWatchingLongPress = onContinueWatchingLongPress,
                        onFolderClick = onFolderClick,
                        onFirstCatalogRendered = onInitialHomeContentRendered,
                    )
                }

                AppScreenTab.Search -> {
                    SearchScreen(
                        modifier = Modifier.fillMaxSize(),
                        onPosterClick = onPosterClick,
                        onPosterLongClick = onPosterLongClick,
                        searchFocusRequestCount = searchFocusRequestCount,
                    )
                }

                AppScreenTab.Library -> {
                    LibraryScreen(
                        modifier = Modifier.fillMaxSize(),
                        onPosterClick = onLibraryPosterClick,
                        onSectionViewAllClick = onLibrarySectionViewAllClick,
                    )
                }

                AppScreenTab.Settings -> {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        onSwitchProfile = onSwitchProfile,
                        onHomescreenClick = onHomescreenSettingsClick,
                        onMetaScreenClick = onMetaScreenSettingsClick,
                        onContinueWatchingClick = onContinueWatchingSettingsClick,
                        onDownloadsClick = onDownloadsSettingsClick,
                        onAddonsClick = onAddonsSettingsClick,
                        onPluginsClick = onPluginsSettingsClick,
                        onSupportersContributorsClick = onSupportersContributorsSettingsClick,
                        onLicensesAttributionsClick = onLicensesAttributionsSettingsClick,
                        onCheckForUpdatesClick = onCheckForUpdatesClick,
                        onCollectionsClick = onCollectionsSettingsClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletFloatingTopBar(
    selectedTab: AppScreenTab,
    onTabSelected: (AppScreenTab) -> Unit,
    onProfileSelected: (RovoProfile) -> Unit,
    onAddProfileRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = statusBarPadding + 10.dp, bottom = 8.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(999.dp),
            tonalElevation = 4.dp,
            shadowElevation = 10.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabletTopPillItem(
                    label = stringResource(Res.string.compose_nav_home),
                    selected = selectedTab == AppScreenTab.Home,
                    onClick = { onTabSelected(AppScreenTab.Home) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = stringResource(Res.string.compose_nav_home),
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedTab == AppScreenTab.Home) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                )
                TabletTopPillItem(
                    label = stringResource(Res.string.compose_nav_search),
                    selected = selectedTab == AppScreenTab.Search,
                    onClick = { onTabSelected(AppScreenTab.Search) },
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.sidebar_search),
                            contentDescription = stringResource(Res.string.compose_nav_search),
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedTab == AppScreenTab.Search) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                )
                TabletTopPillItem(
                    label = stringResource(Res.string.compose_nav_library),
                    selected = selectedTab == AppScreenTab.Library,
                    onClick = { onTabSelected(AppScreenTab.Library) },
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.sidebar_library),
                            contentDescription = stringResource(Res.string.compose_nav_library),
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedTab == AppScreenTab.Library) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                )
                Surface(
                    color = if (selectedTab == AppScreenTab.Settings) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileSwitcherTab(
                            selected = selectedTab == AppScreenTab.Settings,
                            onClick = { onTabSelected(AppScreenTab.Settings) },
                            onProfileSelected = onProfileSelected,
                            onAddProfileRequested = onAddProfileRequested,
                        )
                        Text(
                            text = stringResource(Res.string.compose_nav_profile),
                            modifier = Modifier.clickable { onTabSelected(AppScreenTab.Settings) },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == AppScreenTab.Settings) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletTopPillItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun AppLaunchOverlay(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .zIndex(10f),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
