package com.pebble.feature.chat.ui

import app.cash.turbine.test
import com.pebble.core.ai.AiAvailability
import com.pebble.core.ai.FakeOnDeviceAiClient
import com.pebble.core.domain.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeClient = FakeOnDeviceAiClient()
        viewModel = ChatViewModel(fakeClient)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no messages and unknown availability`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.messages.isEmpty())
            assertEquals("", state.inputText)
            assertFalse(state.isGenerating)
        }
    }

    @Test
    fun `availability is set to Available after init`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(AiAvailability.Available, viewModel.uiState.value.aiAvailability)
    }

    @Test
    fun `UpdateInput intent updates inputText`() = runTest {
        viewModel.onIntent(ChatUiIntent.UpdateInput("Hello"))
        assertEquals("Hello", viewModel.uiState.value.inputText)
    }

    @Test
    fun `SendMessage adds user message and clears input`() = runTest {
        viewModel.onIntent(ChatUiIntent.UpdateInput("What is Pebble?"))
        viewModel.onIntent(ChatUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.messages.any { it.role == MessageRole.USER && it.content == "What is Pebble?" })
        assertEquals("", state.inputText)
    }

    @Test
    fun `SendMessage appends AI response after streaming`() = runTest {
        fakeClient.streamChunks = listOf("Hello ", "world!")
        viewModel.onIntent(ChatUiIntent.UpdateInput("Hi"))
        viewModel.onIntent(ChatUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = viewModel.uiState.value.messages
        assertEquals(2, messages.size)
        assertEquals(MessageRole.AI, messages.last().role)
        assertEquals("Hello world!", messages.last().content)
    }

    @Test
    fun `ClearChat resets messages but keeps availability`() = runTest {
        viewModel.onIntent(ChatUiIntent.UpdateInput("Hi"))
        viewModel.onIntent(ChatUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(ChatUiIntent.ClearChat)
        val state = viewModel.uiState.value
        assertTrue(state.messages.isEmpty())
        assertEquals(AiAvailability.Available, state.aiAvailability)
    }

    @Test
    fun `SendMessage does nothing when input is blank`() = runTest {
        viewModel.onIntent(ChatUiIntent.UpdateInput("   "))
        viewModel.onIntent(ChatUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.isEmpty())
    }

    @Test
    fun `generation error emits ShowError event`() = runTest {
        fakeClient.generateError = RuntimeException("AICore unavailable")
        viewModel.onIntent(ChatUiIntent.UpdateInput("Hi"))

        viewModel.events.test {
            viewModel.onIntent(ChatUiIntent.SendMessage)
            testDispatcher.scheduler.advanceUntilIdle()

            // ScrollToBottom fires first, then ShowError
            val first = awaitItem()
            assertTrue(first is ChatUiEvent.ScrollToBottom)
            val error = awaitItem()
            assertTrue(error is ChatUiEvent.ShowError)
            assertEquals("AICore unavailable", (error as ChatUiEvent.ShowError).message)
        }
    }
}
