package com.pebble.feature.chat.ui

import app.cash.turbine.test
import com.pebble.core.ai.AiAvailability
import com.pebble.core.ai.FakeOnDeviceAiClient
import com.pebble.core.domain.model.Message
import com.pebble.core.domain.model.MessageRole
import com.pebble.core.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeClient: FakeOnDeviceAiClient
    private lateinit var fakeChatRepo: FakeChatRepository
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeClient    = FakeOnDeviceAiClient()
        fakeChatRepo  = FakeChatRepository()
        viewModel     = ChatViewModel(fakeClient, fakeChatRepo)
    }

    @After
    fun teardown() = Dispatchers.resetMain()

    @Test
    fun `initial state has empty messages`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.messages.isEmpty())
            assertFalse(state.isGenerating)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `availability is Available after init`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(AiAvailability.Available, viewModel.uiState.value.aiAvailability)
    }

    @Test
    fun `UpdateInput changes inputText`() = runTest {
        viewModel.onIntent(ChatUiIntent.UpdateInput("Hello"))
        assertEquals("Hello", viewModel.uiState.value.inputText)
    }

    @Test
    fun `SendMessage persists user message and clears input`() = runTest {
        viewModel.onIntent(ChatUiIntent.UpdateInput("What is Pebble?"))
        viewModel.onIntent(ChatUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeChatRepo.saved.any { it.role == MessageRole.USER && it.content == "What is Pebble?" })
        assertEquals("", viewModel.uiState.value.inputText)
    }

    @Test
    fun `SendMessage persists AI response after streaming`() = runTest {
        fakeClient.streamChunks = listOf("Hello ", "world!")
        viewModel.onIntent(ChatUiIntent.UpdateInput("Hi"))
        viewModel.onIntent(ChatUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        val aiMessages = fakeChatRepo.saved.filter { it.role == MessageRole.AI }
        assertEquals(1, aiMessages.size)
        assertEquals("Hello world!", aiMessages.first().content)
    }

    @Test
    fun `ClearChat calls repository clearAll`() = runTest {
        viewModel.onIntent(ChatUiIntent.UpdateInput("Hi"))
        viewModel.onIntent(ChatUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(ChatUiIntent.ClearChat)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeChatRepo.cleared)
    }

    @Test
    fun `blank input does not send message`() = runTest {
        viewModel.onIntent(ChatUiIntent.UpdateInput("   "))
        viewModel.onIntent(ChatUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeChatRepo.saved.isEmpty())
    }

    @Test
    fun `AI error emits ShowError event`() = runTest {
        fakeClient.generateError = RuntimeException("AICore unavailable")
        viewModel.onIntent(ChatUiIntent.UpdateInput("Hi"))

        viewModel.events.test {
            viewModel.onIntent(ChatUiIntent.SendMessage)
            testDispatcher.scheduler.advanceUntilIdle()

            val first = awaitItem()
            assertTrue(first is ChatUiEvent.ScrollToBottom)
            val error = awaitItem() as ChatUiEvent.ShowError
            assertEquals("AICore unavailable", error.message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

// ---- Test doubles ----

class FakeChatRepository : ChatRepository {
    val saved  = mutableListOf<Message>()
    var cleared = false
    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    override fun observeMessages(): Flow<List<Message>> = _messages.asStateFlow()

    override suspend fun saveMessage(message: Message) {
        saved += message
        _messages.value = _messages.value + message
    }

    override suspend fun clearAll() {
        cleared = true
        saved.clear()
        _messages.value = emptyList()
    }
}
