package com.pebble.core.data.di

import org.koin.dsl.module

/**
 * Koin module for the data layer.
 *
 * Room DB and DAOs will be registered here in Phase 2 when
 * chat history persistence is added.
 */
val dataModule = module {
    // TODO: Register PebbleDatabase, MessageDao, ChatRepository here (Phase 2)
}
