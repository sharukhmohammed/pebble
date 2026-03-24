package com.pebble.core.data.repository

import com.pebble.core.data.db.MessageDao
import com.pebble.core.data.db.MessageEntity
import com.pebble.core.domain.model.Message
import com.pebble.core.domain.model.MessageRole
import com.pebble.core.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomChatRepository(private val dao: MessageDao) : ChatRepository {

    override fun observeMessages(): Flow<List<Message>> =
        dao.observeAll().map { entities -> entities.map(MessageEntity::toDomain) }

    override suspend fun saveMessage(message: Message) {
        dao.insert(message.toEntity())
    }

    override suspend fun clearAll() {
        dao.deleteAll()
    }
}

// ---- Mappers ----

private fun MessageEntity.toDomain() = Message(
    id        = id,
    content   = content,
    role      = if (role == "USER") MessageRole.USER else MessageRole.AI,
    timestamp = timestamp,
)

private fun Message.toEntity() = MessageEntity(
    id        = id,
    content   = content,
    role      = role.name,
    timestamp = timestamp,
)
