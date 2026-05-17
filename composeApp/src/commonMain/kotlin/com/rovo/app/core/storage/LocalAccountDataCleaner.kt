package com.rovo.app.core.storage

import com.rovo.app.core.build.AppFeaturePolicy
import com.rovo.app.features.addons.AddonRepository
import com.rovo.app.features.catalog.CatalogRepository
import com.rovo.app.features.collection.CollectionMobileSettingsRepository
import com.rovo.app.features.collection.CollectionRepository
import com.rovo.app.features.details.MetaDetailsRepository
import com.rovo.app.features.details.MetaScreenSettingsRepository
import com.rovo.app.features.home.HomeCatalogSettingsRepository
import com.rovo.app.features.home.HomeRepository
import com.rovo.app.features.library.LibraryRepository
import com.rovo.app.features.notifications.EpisodeReleaseNotificationsRepository
import com.rovo.app.features.player.PlayerLaunchStore
import com.rovo.app.features.player.PlayerSettingsRepository
import com.rovo.app.features.plugins.PluginRepository
import com.rovo.app.features.player.SubtitleRepository
import com.rovo.app.features.profiles.ProfileRepository
import com.rovo.app.features.search.SearchRepository
import com.rovo.app.features.settings.ThemeSettingsRepository
import com.rovo.app.features.streams.StreamContextStore
import com.rovo.app.features.streams.StreamLaunchStore
import com.rovo.app.features.streams.StreamsRepository
import com.rovo.app.features.trakt.TraktAuthRepository
import com.rovo.app.features.trakt.TraktSettingsRepository
import com.rovo.app.core.ui.PosterCardStyleRepository
import com.rovo.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.rovo.app.features.watchprogress.WatchProgressRepository
import com.rovo.app.features.watched.WatchedRepository

internal object LocalAccountDataCleaner {
    fun wipe() {
        PlatformLocalAccountDataCleaner.wipe()

        ProfileRepository.clearInMemory()
        AddonRepository.clearLocalState()
        if (AppFeaturePolicy.pluginsEnabled) {
            PluginRepository.clearLocalState()
        }
        HomeRepository.clear()
        HomeCatalogSettingsRepository.clearLocalState()
        MetaScreenSettingsRepository.clearLocalState()
        LibraryRepository.clearLocalState()
        WatchProgressRepository.clearLocalState()
        WatchedRepository.clearLocalState()
        ContinueWatchingPreferencesRepository.clearLocalState()
        EpisodeReleaseNotificationsRepository.clearLocalState()
        CollectionMobileSettingsRepository.clearLocalState()
        CollectionRepository.clearLocalState()
        ThemeSettingsRepository.clearLocalState()
        PosterCardStyleRepository.clearLocalState()
        TraktAuthRepository.clearLocalState()
        TraktSettingsRepository.clearLocalState()
        PlayerSettingsRepository.clearLocalState()
        CatalogRepository.clear()
        StreamsRepository.clear()
        MetaDetailsRepository.clear()
        SearchRepository.reset()
        SubtitleRepository.clear()
        PlayerLaunchStore.clear()
        StreamLaunchStore.clear()
        StreamContextStore.clear()
    }
}

internal expect object PlatformLocalAccountDataCleaner {
    fun wipe()
}
