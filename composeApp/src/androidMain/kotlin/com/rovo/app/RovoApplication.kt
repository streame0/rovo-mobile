package com.rovo.app

import android.app.Application
import com.rovo.shared.di.sharedModule
import com.rovo.shared.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RovoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@RovoApplication)
            androidLogger()
            modules(sharedModule + platformModule)
        }
    }
}
