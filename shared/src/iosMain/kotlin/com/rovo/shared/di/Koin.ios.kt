package com.rovo.shared.di

import com.rovo.shared.data.local.IosDBBuilder
import com.rovo.shared.data.local.RovoDatabase
import org.koin.dsl.module

actual val platformModule = module {
    single<RovoDatabase> { IosDBBuilder().build() }
}
