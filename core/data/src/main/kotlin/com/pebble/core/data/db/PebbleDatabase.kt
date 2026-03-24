package com.pebble.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PebbleDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
