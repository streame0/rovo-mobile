package com.rovo.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rovo.app.core.auth.AuthStorage
import com.rovo.app.core.deeplink.handleAppUrl
import com.rovo.app.core.storage.PlatformLocalAccountDataCleaner
import com.rovo.app.features.addons.AddonStorage
import com.rovo.app.features.collection.CollectionMobileSettingsStorage
import com.rovo.app.features.collection.CollectionStorage
import com.rovo.app.features.debrid.DebridSettingsStorage
import com.rovo.app.features.downloads.DownloadsLiveStatusPlatform
import com.rovo.app.features.downloads.DownloadsPlatformDownloader
import com.rovo.app.features.downloads.DownloadsStorage
import com.rovo.app.features.library.LibraryStorage
import com.rovo.app.features.details.MetaScreenSettingsStorage
import com.rovo.app.features.home.HomeCatalogSettingsStorage
import com.rovo.app.features.mdblist.MdbListSettingsStorage
import com.rovo.app.features.notifications.EpisodeReleaseNotificationPlatform
import com.rovo.app.features.notifications.EpisodeReleaseNotificationsStorage
import com.rovo.app.features.player.PlayerSettingsStorage
import com.rovo.app.features.player.ExternalPlayerPlatform
import com.rovo.app.features.player.PlayerPictureInPictureManager
import com.rovo.app.features.plugins.PluginStorage
import com.rovo.app.features.profiles.AvatarStorage
import com.rovo.app.features.profiles.ProfilePinCacheStorage
import com.rovo.app.features.profiles.ProfileStorage
import com.rovo.app.features.details.SeasonViewModeStorage
import com.rovo.app.features.search.SearchHistoryStorage
import com.rovo.app.features.settings.ThemeSettingsStorage
import com.rovo.app.features.trakt.TraktAuthStorage
import com.rovo.app.features.trakt.TraktCommentsStorage
import com.rovo.app.features.trakt.TraktLibraryStorage
import com.rovo.app.features.trakt.TraktSettingsStorage
import com.rovo.app.features.tmdb.TmdbSettingsStorage
import com.rovo.app.features.updater.AndroidAppUpdaterPlatform
import com.rovo.app.core.ui.PosterCardStyleStorage
import com.rovo.app.features.watched.WatchedStorage
import com.rovo.app.features.streams.StreamLinkCacheStorage
import com.rovo.app.features.watchprogress.ContinueWatchingEnrichmentStorage
import com.rovo.app.features.watchprogress.ContinueWatchingPreferencesStorage
import com.rovo.app.features.watchprogress.ResumePromptStorage
import com.rovo.app.features.watchprogress.WatchProgressStorage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFF020404.toInt(),
            ),
        )
        ThemeSettingsStorage.initialize(applicationContext)
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.rovo_background)
        AddonStorage.initialize(applicationContext)
        AuthStorage.initialize(applicationContext)
        LibraryStorage.initialize(applicationContext)
        WatchedStorage.initialize(applicationContext)
        MetaScreenSettingsStorage.initialize(applicationContext)
        HomeCatalogSettingsStorage.initialize(applicationContext)
        PlayerSettingsStorage.initialize(applicationContext)
        ExternalPlayerPlatform.initialize(applicationContext)
        ProfileStorage.initialize(applicationContext)
        AvatarStorage.initialize(applicationContext)
        ProfilePinCacheStorage.initialize(applicationContext)
        SearchHistoryStorage.initialize(applicationContext)
        SeasonViewModeStorage.initialize(applicationContext)
        PosterCardStyleStorage.initialize(applicationContext)
        DebridSettingsStorage.initialize(applicationContext)
        TmdbSettingsStorage.initialize(applicationContext)
        MdbListSettingsStorage.initialize(applicationContext)
        TraktAuthStorage.initialize(applicationContext)
        TraktCommentsStorage.initialize(applicationContext)
        TraktLibraryStorage.initialize(applicationContext)
        TraktSettingsStorage.initialize(applicationContext)
        ContinueWatchingPreferencesStorage.initialize(applicationContext)
        ResumePromptStorage.initialize(applicationContext)
        ContinueWatchingEnrichmentStorage.initialize(applicationContext)
        EpisodeReleaseNotificationsStorage.initialize(applicationContext)
        WatchProgressStorage.initialize(applicationContext)
        StreamLinkCacheStorage.initialize(applicationContext)
        PluginStorage.initialize(applicationContext)
        CollectionMobileSettingsStorage.initialize(applicationContext)
        CollectionStorage.initialize(applicationContext)
        DownloadsStorage.initialize(applicationContext)
        DownloadsPlatformDownloader.initialize(applicationContext)
        DownloadsLiveStatusPlatform.initialize(applicationContext)
        AndroidAppUpdaterPlatform.initialize(applicationContext)
        PlatformLocalAccountDataCleaner.initialize(applicationContext)
        EpisodeReleaseNotificationPlatform.initialize(applicationContext)
        EpisodeReleaseNotificationPlatform.bindActivity(this)
        handleIncomingAppIntent(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingAppIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        PlayerPictureInPictureManager.onUserLeaveHint(this)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        PlayerPictureInPictureManager.onPictureInPictureModeChanged(this, isInPictureInPictureMode)
    }

    override fun onDestroy() {
        EpisodeReleaseNotificationPlatform.unbindActivity(this)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (EpisodeReleaseNotificationPlatform.handlePermissionRequestResult(requestCode, grantResults)) {
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun handleIncomingAppIntent(intent: Intent?) {
        val appUrl = intent?.dataString?.trim().orEmpty()
        if (appUrl.isBlank()) return
        handleAppUrl(appUrl)
    }
}
