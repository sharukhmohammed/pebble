package com.pebble.ai

import android.app.Application
import com.pebble.core.ai.di.aiModule
import com.pebble.core.data.di.dataModule
import com.pebble.feature.chat.di.chatModule
import com.pebble.feature.home.di.homeModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PebbleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@PebbleApplication)
            modules(aiModule, dataModule, chatModule, homeModule)
        }
    }
}
