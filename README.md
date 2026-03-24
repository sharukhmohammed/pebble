# Pebble AI

> **Your private AI companion. Completely offline. Always on.**

Pebble AI is an Android app powered entirely by **on-device AI** using Gemini Nano via Android AICore. No internet connection is ever required. No data leaves your device.

---

## Features

| Feature | Status |
|---|---|
| Conversational AI (Chat) | ✅ Phase 1 |
| Text Summarizer | 🔜 Phase 2 |
| Smart Rewrite | 🔜 Phase 2 |
| AI Journal | 🔜 Phase 2 |
| App Settings | 🔜 Phase 3 |

---

## Tech Stack

- **Language** — Kotlin (100%)
- **UI** — Jetpack Compose + Material 3 (Dynamic Color)
- **Navigation** — Navigation 3 (type-safe, Compose-native)
- **Architecture** — MVI + Clean Architecture
- **DI** — Koin (KMP-compatible)
- **On-device AI** — Gemini Nano via `play-services-generativeai`
- **Persistence** — Room
- **Min SDK** — 31 (Android 12)

---

## Project Structure

```
pebble/
├── app/                  # Entry point — Koin init, MainActivity, NavHost
├── core/
│   ├── design/           # M3 theme, colors, PebbleMotion, shared components
│   ├── ai/               # OnDeviceAiClient interface + GeminiNanoClient
│   ├── domain/           # Pure Kotlin models + repository interfaces
│   └── data/             # Room DB, RoomChatRepository
└── feature/
    ├── chat/             # Conversational AI — MVI ViewModel, ChatScreen
    └── home/             # Feature hub — staggered card grid
```

---

## Architecture

```
UI (feature:*)  →  Domain (core:domain)  ←  Data (core:data)
                                              ↑
                                         core:ai
                                    (OnDeviceAiClient)
```

- **MVI**: single `UiState` `StateFlow`, `UiIntent` sealed interface, one-shot `UiEvent` via `Channel`
- **Domain layer** is pure Kotlin — zero Android imports → KMP-ready
- **Koin** for DI instead of Hilt, so the dependency graph works for any KMP target

---

## On-Device AI

Pebble uses **Gemini Nano** managed by Android AICore (Google Play Services). The model weights are never bundled with the app — the system manages them.

```kotlin
// The interface the app talks to — no Android types
interface OnDeviceAiClient {
    suspend fun checkAvailability(): AiAvailability
    fun generateStream(prompt: String): Flow<String>   // token-by-token streaming
    suspend fun generate(prompt: String): Result<String>
}
```

The production `GeminiNanoClient` wires up to `GenerativeModel(modelName = "gemini-nano")`. A streaming stub is active during development so the full UI works before AICore is provisioned on the device.

---

## Animations

All motion uses **only** the standard `androidx.compose.animation` library — no third-party deps.

| Element | Technique |
|---|---|
| Home card stagger entrance | `AnimatedVisibility` + per-card `delay()` + `slideInVertically` spring |
| Card press feedback | `animateFloatAsState` + `DampingRatioMediumBouncy` spring |
| Chat message list | `animateItem()` with `tween` fade + `spring` placement |
| Send button pop | `AnimatedContent` with `scaleIn` / `scaleOut` |
| Empty state icon | `InfiniteTransition` floating loop |
| Loading / typing indicator | `InfiniteTransition` staggered bounce (3 dots) |
| Content state switch | `AnimatedContent` cross-fade |

---

## Getting Started

### Requirements

- Android Studio Ladybug (2024.2) or later
- JDK 17
- Device or emulator running **Android 12+** (API 31)

### Build

```bash
# Clone
git clone https://github.com/sharukhmohammed/pebble.git
cd pebble

# Run unit tests
./gradlew :feature:chat:test

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Connecting Real Gemini Nano

1. Ensure the device runs Android 12+ with Google Play Services.
2. Open `GeminiNanoClient.kt` and replace the stub with the real `GenerativeModel` calls (annotated with `// TODO` comments).
3. The app will automatically check `AiAvailability` on launch and show a download progress screen if AICore needs to prepare the model.

---

## Testing

```bash
# Unit tests (ViewModel, repository, use cases)
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

Test doubles:
- `FakeOnDeviceAiClient` — configurable stream chunks and error injection
- `FakeChatRepository` — in-memory repository with `saved` / `cleared` assertions

---

## Roadmap

- **Phase 1** ✅ — Scaffold, Chat with streaming + persistence, M3 animations
- **Phase 2** — Summarizer, Smart Rewrite, AI Journal, conversation sessions
- **Phase 3** — Settings, onboarding flow, home screen widget, KMP shared module

---

## License

[MIT](LICENSE)
