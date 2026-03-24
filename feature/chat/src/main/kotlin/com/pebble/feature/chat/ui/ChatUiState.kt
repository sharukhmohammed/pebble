package com.pebble.feature.chat.ui

import com.pebble.core.ai.AiAvailability
import com.pebble.core.domain.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    /** Partial text that is still being streamed from the model. */
    val streamingText: String = "",
    val aiAvailability: AiAvailability = AiAvailability.Unknown,
    val error: String? = null,
)

sealed interface ChatUiIntent {
    data class UpdateInput(val text: String) : ChatUiIntent
    data object SendMessage  : ChatUiIntent
    data object ClearChat    : ChatUiIntent
    data object DismissError : ChatUiIntent
}

sealed interface ChatUiEvent {
    data object ScrollToBottom : ChatUiEvent
    data class  ShowError(val message: String) : ChatUiEvent
}
