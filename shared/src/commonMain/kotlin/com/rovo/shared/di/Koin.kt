package com.rovo.shared.di

import com.rovo.shared.api.*
import com.rovo.shared.data.player.PlaybackTrackSelectionStore
import com.rovo.shared.data.player.SourceSelectionStore
import com.rovo.shared.repository.*
import com.rovo.shared.ui.home.HomeViewModel
import com.rovo.shared.ui.details.DetailsViewModel
import com.rovo.shared.ui.search.SearchViewModel
import com.rovo.shared.ui.library.LibraryViewModel
import com.rovo.shared.ui.addons.AddonManagementViewModel
import com.rovo.shared.ui.settings.SettingsViewModel
import com.rovo.shared.ui.player.PlayerViewModel
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val sharedModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
            install(HttpRedirect) {
                checkHttpMethod = false
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    single { StremioApi(get()) }
    single { TmdbApi(get()) }
    single { TraktApi(get()) }
    single { DebridApi(get()) }
    single { IntroApi(get()) }
    single { TorrServerApi(get()) }
    
    single { get<com.rovo.shared.data.local.RovoDatabase>().addonDao() }

    single<Settings> { Settings() }
    single { PlaybackTrackSelectionStore(get()) }
    single { SourceSelectionStore(get()) }

    single { AddonRepository(get(), get(), get()) }
    single { ProfileRepository(get()) }
    single { SyncRepository(get(), get(), get()) }
    single { SeriesRepository(get(), get()) }
    single { DebridRepository(get(), get()) }
    single { TmdbRepository(get()) }
    single { com.rovo.shared.domain.StreamSortingService() }
    single { SubtitleRepository(get(), get()) }
    single { IntroRepository(get()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { DetailsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { AddonManagementViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { PlayerViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

expect val platformModule: Module
