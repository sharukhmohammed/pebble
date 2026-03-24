package com.pebble.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pebble.core.ai.OnDeviceAiClient
import com.pebble.core.domain.model.Message
import com.pebble.core.domain.model.MessageRole
import com.pebble.core.domain.repository.ChatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val aiClient: OnDeviceAiClient,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChatUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        checkAvailability()
        observeMessages()
    }

    fun onIntent(intent: ChatUiIntent) {
        when (intent) {
            is ChatUiIntent.UpdateInput -> _uiState.update { it.copy(inputText = intent.text, error = null) }
            ChatUiIntent.SendMessage    -> sendMessage()
            ChatUiIntent.ClearChat     -> clearChat()
            ChatUiIntent.DismissError  -> _uiState.update { it.copy(error = null) }
        }
    }

    // ---- Private helpers ----

    private fun checkAvailability() = viewModelScope.launch {
        val availability = aiClient.checkAvailability()
        _uiState.update { it.copy(aiAvailability = availability) }
    }

    /** Keep UI in sync with the DB at all times. */
    private fun observeMessages() = viewModelScope.launch {
        chatRepository.observeMessages().collect { messages ->
            _uiState.update { it.copy(messages = messages) }
        }
    }

    private fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isGenerating) return

        val userMsg = Message(content = text, role = MessageRole.USER)

        viewModelScope.launch {
            // Persist user message and clear input immediately
            chatRepository.saveMessage(userMsg)
            _uiState.update {
                it.copy(
                    inputText     = "",
                    isGenerating  = true,
                    streamingText = "",
                    error         = null,
                )
            }
            _events.send(ChatUiEvent.ScrollToBottom)

            // Stream the AI response token by token
            var fullResponse = ""

            aiClient.generateStream(text)
                .catch { error ->
                    _uiState.update { it.copy(isGenerating = false, streamingText = "") }
                    _events.send(ChatUiEvent.ShowError(error.message ?: "Generation failed"))
                }
                .collect { chunk ->
                    fullResponse += chunk
                    _uiState.update { it.copy(streamingText = fullResponse) }
                    _events.send(ChatUiEvent.ScrollToBottom)
                }

            // Persist completed AI message
            if (fullResponse.isNotEmpty()) {
                val aiMsg = Message(content = fullResponse.trim(), role = MessageRole.AI)
                chatRepository.saveMessage(aiMsg)
                _uiState.update { it.copy(isGenerating = false, streamingText = "") }
                _events.send(ChatUiEvent.ScrollToBottom)
            }
        }
    }

    private fun clearChat() = viewModelScope.launch {
        chatRepository.clearAll()
        _uiState.update {
            ChatUiState(aiAvailability = it.aiAvailability)
        }
    }
}
