package com.pebble.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pebble.core.ai.OnDeviceAiClient
import com.pebble.core.domain.model.Message
import com.pebble.core.domain.model.MessageRole
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(private val aiClient: OnDeviceAiClient) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChatUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        checkAvailability()
    }

    fun onIntent(intent: ChatUiIntent) {
        when (intent) {
            is ChatUiIntent.UpdateInput -> _uiState.update { it.copy(inputText = intent.text, error = null) }
            ChatUiIntent.SendMessage    -> sendMessage()
            ChatUiIntent.ClearChat     -> _uiState.update { ChatUiState(aiAvailability = it.aiAvailability) }
            ChatUiIntent.DismissError  -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun checkAvailability() = viewModelScope.launch {
        val availability = aiClient.checkAvailability()
        _uiState.update { it.copy(aiAvailability = availability) }
    }

    private fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isGenerating) return

        val userMsg = Message(content = text, role = MessageRole.USER)
        _uiState.update {
            it.copy(
                messages      = it.messages + userMsg,
                inputText     = "",
                isGenerating  = true,
                streamingText = "",
                error         = null,
            )
        }

        viewModelScope.launch {
            _events.send(ChatUiEvent.ScrollToBottom)

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

            if (fullResponse.isNotEmpty()) {
                val aiMsg = Message(content = fullResponse.trim(), role = MessageRole.AI)
                _uiState.update {
                    it.copy(
                        messages      = it.messages + aiMsg,
                        isGenerating  = false,
                        streamingText = "",
                    )
                }
                _events.send(ChatUiEvent.ScrollToBottom)
            }
        }
    }
}
