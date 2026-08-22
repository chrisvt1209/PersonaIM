package dev.compose.messenger

import android.app.Application
import dev.compose.messenger.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MessengerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MessengerApp)
            modules(appModule)
        }
    }
}
