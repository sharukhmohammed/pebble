package com.pebble.core.ai

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the on-device AI runtime.
 *
 * Keeping this interface KMP-ready: no Android-specific types in the signature.
 * The Android implementation ([GeminiNanoClient]) lives in the same module;
 * a KMP expect/actual pair can replace it when targeting iOS or Desktop.
 */
interface OnDeviceAiClient {

    /** Check whether the on-device model is ready to use. Call this before [generate]. */
    suspend fun checkAvailability(): AiAvailability

    /**
     * Stream a response token-by-token.
     * Collect the flow to receive incremental text chunks as they are generated.
     */
    fun generateStream(prompt: String): Flow<String>

    /** Generate a full response in one shot. Returns [Result] so callers handle errors cleanly. */
    suspend fun generate(prompt: String): Result<String>
}
