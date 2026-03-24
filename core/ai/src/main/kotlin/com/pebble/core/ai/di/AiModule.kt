package com.pebble.core.ai.di

import com.pebble.core.ai.GeminiNanoClient
import com.pebble.core.ai.OnDeviceAiClient
import org.koin.dsl.module

val aiModule = module {
    single<OnDeviceAiClient> { GeminiNanoClient() }
}
