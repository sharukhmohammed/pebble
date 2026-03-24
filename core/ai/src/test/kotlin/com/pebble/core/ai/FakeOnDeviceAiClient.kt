package com.pebble.core.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Test double for [OnDeviceAiClient].
 *
 * Configure [availability], [streamChunks], and [generateError] before running assertions.
 * All feature tests that touch AI behaviour should use this instead of the real client.
 */
class FakeOnDeviceAiClient(
    var availability: AiAvailability = AiAvailability.Available,
    var streamChunks: List<String> = listOf("Hello ", "from ", "Pebble!"),
    var generateError: Throwable? = null,
) : OnDeviceAiClient {

    override suspend fun checkAvailability(): AiAvailability = availability

    override fun generateStream(prompt: String): Flow<String> = flow {
        generateError?.let { throw it }
        streamChunks.forEach { emit(it) }
    }

    override suspend fun generate(prompt: String): Result<String> =
        generateError?.let { Result.failure(it) }
            ?: Result.success(streamChunks.joinToString(""))
}
