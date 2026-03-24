package com.pebble.core.domain.model

import java.util.UUID

/**
 * A single turn in a conversation.
 *
 * This is a pure Kotlin domain model — no Android or framework dependencies.
 * Ready to be moved to a KMP shared module when iOS / Desktop targets are added.
 */
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class MessageRole { USER, AI }
