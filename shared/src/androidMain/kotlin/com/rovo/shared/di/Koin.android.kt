package com.rovo.shared.di

import com.rovo.shared.data.local.AndroidDBBuilder
import com.rovo.shared.data.local.RovoDatabase
import org.koin.dsl.module

actual val platformModule = module {
    single<RovoDatabase> { AndroidDBBuilder(get()).build() }
    single { com.rovo.shared.data.torrent.TorrServerEngine(get(), get()) }
}
