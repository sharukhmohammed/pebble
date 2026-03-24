package com.pebble.core.ai

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Production implementation of [OnDeviceAiClient] backed by Gemini Nano.
 *
 * Gemini Nano runs entirely on-device via Android AICore (Google Play Services).
 * No API key is needed — the system manages the model lifecycle.
 *
 * ## SDK integration
 * When `play-services-generativeai` is set up on the device:
 *
 * ```kotlin
 * private val model = GenerativeModel(modelName = "gemini-nano")
 *
 * override suspend fun checkAvailability(): AiAvailability {
 *     return when (model.checkAvailability()) {
 *         AvailabilityStatus.AVAILABLE   -> AiAvailability.Available
 *         AvailabilityStatus.DOWNLOADING -> AiAvailability.Downloading(0f)
 *         else                           -> AiAvailability.Unavailable
 *     }
 * }
 *
 * override fun generateStream(prompt: String): Flow<String> = flow {
 *     model.generateContentStream(prompt).collect { chunk ->
 *         emit(chunk.text ?: "")
 *     }
 * }
 *
 * override suspend fun generate(prompt: String): Result<String> = runCatching {
 *     model.generateContent(prompt).text ?: ""
 * }
 * ```
 *
 * The stub below simulates streaming so the app is fully functional during development
 * before AICore is available on the test device.
 */
class GeminiNanoClient : OnDeviceAiClient {

    override suspend fun checkAvailability(): AiAvailability {
        // TODO: Replace with model.checkAvailability() once play-services-generativeai
        //       is provisioned on the target device via AICore.
        return AiAvailability.Available
    }

    override fun generateStream(prompt: String): Flow<String> = flow {
        // TODO: Replace with model.generateContentStream(prompt) for real inference.
        // Development stub — simulates token-by-token streaming.
        val stubResponse = buildStubResponse(prompt)
        for (word in stubResponse.split(" ")) {
            emit("$word ")
            delay(60)
        }
    }

    override suspend fun generate(prompt: String): Result<String> = runCatching {
        // TODO: Replace with model.generateContent(prompt).text ?: ""
        buildStubResponse(prompt)
    }

    private fun buildStubResponse(prompt: String): String =
        "I'm Pebble \u2014 your private on-device AI. " +
            "Everything I generate stays on your device. " +
            "You asked: \"$prompt\". " +
            "Once Gemini Nano is connected, real responses will appear here."
}
