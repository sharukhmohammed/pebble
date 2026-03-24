package com.pebble.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val role: String,       // "USER" | "AI"
    val timestamp: Long,
)
