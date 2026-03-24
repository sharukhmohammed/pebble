package com.pebble.core.ai

/**
 * Represents the current availability state of the on-device AI model.
 *
 * - [Available]    — Gemini Nano is ready for inference.
 * - [Downloading]  — The model is being downloaded by the system (progress 0..1).
 * - [Unavailable]  — Device does not support on-device inference. Show a friendly message.
 * - [Unknown]      — Availability has not been checked yet (initial state).
 */
sealed interface AiAvailability {
    data object Available   : AiAvailability
    data class  Downloading(val progress: Float) : AiAvailability
    data object Unavailable : AiAvailability
    data object Unknown     : AiAvailability
}
