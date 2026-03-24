package com.pebble.core.data.di

import androidx.room.Room
import com.pebble.core.data.db.PebbleDatabase
import com.pebble.core.data.repository.RoomChatRepository
import com.pebble.core.domain.repository.ChatRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single<PebbleDatabase> {
        Room.databaseBuilder(
            androidContext(),
            PebbleDatabase::class.java,
            "pebble.db",
        ).build()
    }

    single { get<PebbleDatabase>().messageDao() }

    single<ChatRepository> { RoomChatRepository(dao = get()) }
}
