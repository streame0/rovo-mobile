package com.rovo.app.core.storage

import android.content.Context

internal actual object PlatformLocalAccountDataCleaner {
    private val preferenceNames = listOf(
        "rovo_addons",
        "rovo_library",
        "rovo_home_catalog_settings",
        "rovo_player_settings",
        "rovo_profile_cache",
        "rovo_avatar_cache",
        "rovo_profile_pin_cache",
        "rovo_theme_settings",
        "rovo_poster_card_style",
        "rovo_mdblist_settings",
        "rovo_trakt_auth",
        "rovo_trakt_library",
        "rovo_trakt_settings",
        "rovo_watched",
        "rovo_stream_link_cache",
        "rovo_continue_watching_preferences",
        "rovo_episode_release_notifications",
        "rovo_episode_release_notifications_platform",
        "rovo_watch_progress",
        "rovo_collection_mobile_settings",
        "rovo_collections",
        "rovo_plugins",
    )

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun wipe() {
        val context = appContext ?: return
        preferenceNames.forEach { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
    }
}
