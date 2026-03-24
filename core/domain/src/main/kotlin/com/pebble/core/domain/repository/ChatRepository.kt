package com.pebble.core.domain.repository

import com.pebble.core.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Contract for chat message persistence.
 *
 * Pure Kotlin — no Android / Room imports so this compiles for any KMP target.
 * The Room-backed implementation lives in :core:data.
 */
interface ChatRepository {

    /** Observe all messages in chronological order. Emits on every DB change. */
    fun observeMessages(): Flow<List<Message>>

    /** Persist a single message. */
    suspend fun saveMessage(message: Message)

    /** Remove all messages from storage. */
    suspend fun clearAll()
}
