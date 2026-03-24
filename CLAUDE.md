# CLAUDE.md — Pebble AI

## Project Overview

**Pebble AI** is an Android application focused exclusively on **on-device AI**.
All intelligence runs locally using **Gemini Nano** via Android AICore / Google AI Edge SDK.
No internet connection is ever required. No data leaves the device.

> _"Your private AI companion. Completely offline. Always on."_

---

## Core Principles

1. **Offline-first, offline-only** — zero network calls, ever. AI inference happens entirely on-device.
2. **Privacy by design** — no accounts, no telemetry, no cloud sync.
3. **KMP-ready** — all non-Android code lives in shared modules, ready to be extracted into a KMP shared module targeting iOS and Desktop.
4. **Material 3 only** — UI uses only the standard Material 3 Compose library. No third-party UI or animation libraries.
5. **Fun, Google-app-quality animations** — motion is meaningful, playful, and never added just for show.
6. **Testability first** — every layer is independently testable with no hidden dependencies.

---

## Technology Stack

### Language & Runtime
| Concern | Choice | Notes |
|---|---|---|
| Language | Kotlin | 100%, no Java |
| Coroutines | `kotlinx.coroutines` | Flows everywhere |
| Serialization | `kotlinx.serialization` | KMP-compatible |

### UI
| Concern | Choice | Notes |
|---|---|---|
| UI toolkit | Jetpack Compose (BOM latest) | No XML Views |
| Design system | Material 3 (`material3`) | Dynamic color enabled |
| Navigation | Navigation 3 (`androidx.navigation3`) | Type-safe, Compose-native |
| Animations | Material 3 built-in motion | `AnimatedContent`, `AnimatedVisibility`, shared element transitions, spring physics |
| Theme | Material You (Dynamic Color) | System dark/light default |

### Architecture
| Concern | Choice |
|---|---|
| Pattern | MVI + Clean Architecture |
| DI | Koin (KMP-compatible; do NOT use Hilt) |
| State management | `StateFlow` + `UiState` sealed classes |
| Side effects | `Channel` → `SharedFlow` for one-shot events |

### AI / On-device Inference
| Concern | Choice | Notes |
|---|---|---|
| Primary model | Gemini Nano via `play-services-generativeai` | `modelName = "gemini-nano"` |
| Inference runtime | Android AICore (system-level, Android 12+) | Managed by Google Play Services |
| Availability check | `GenerativeModel.checkAvailability()` | Always check before inference |
| Fallback | Graceful UI message — no cloud fallback | App is offline-only by design |

### Data & Storage
| Concern | Choice |
|---|---|
| Local persistence | Room (KMP-compatible via `androidx.room`) |
| Preferences | DataStore Preferences |
| No networking | Retrofit/OkHttp must NOT be added |

### Build
| Concern | Choice |
|---|---|
| Build system | Gradle with Kotlin DSL (`.kts`) |
| Dependency management | Version Catalog (`libs.versions.toml`) |
| Min SDK | **31** (Android 12) — required for full AICore support |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 |

---

## Project Structure

```
pebble/
├── app/                          # Android app module (Hilt-free, Koin entry point)
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/pebble/ai/
│   │   │   ├── PebbleApplication.kt   # Koin init
│   │   │   └── MainActivity.kt        # Single activity, nav host
│   └── build.gradle.kts
│
├── core/
│   ├── design/                   # Design system, theme, tokens, animations
│   │   └── src/main/kotlin/com/pebble/core/design/
│   │       ├── theme/
│   │       │   ├── PebbleTheme.kt
│   │       │   ├── Color.kt
│   │       │   ├── Type.kt
│   │       │   └── Shape.kt
│   │       ├── animation/
│   │       │   └── PebbleMotion.kt    # Shared motion specs
│   │       └── components/           # Shared reusable Compose components
│   │
│   ├── ai/                       # On-device AI abstraction (KMP-ready)
│   │   └── src/main/kotlin/com/pebble/core/ai/
│   │       ├── OnDeviceAiClient.kt    # Interface
│   │       ├── GeminiNanoClient.kt    # Android implementation
│   │       └── AiAvailability.kt      # Sealed class: Available, Downloading, Unavailable
│   │
│   ├── data/                     # Room DB, DAOs, DataStore
│   └── domain/                   # UseCases, repository interfaces, pure Kotlin models
│
├── feature/
│   ├── chat/                     # Conversational AI feature
│   │   └── src/main/kotlin/com/pebble/feature/chat/
│   │       ├── ui/
│   │       │   ├── ChatScreen.kt
│   │       │   ├── ChatViewModel.kt
│   │       │   └── components/
│   │       │       ├── MessageBubble.kt
│   │       │       ├── TypingIndicator.kt
│   │       │       └── ChatInputBar.kt
│   │       ├── domain/
│   │       │   ├── SendMessageUseCase.kt
│   │       │   └── GetChatHistoryUseCase.kt
│   │       └── data/
│   │           ├── ChatRepository.kt
│   │           └── ChatRepositoryImpl.kt
│   │
│   └── home/                     # Home / feature hub screen
│
├── gradle/
│   └── libs.versions.toml
└── build.gradle.kts
```

---

## Architecture Deep-Dive

### MVI Flow

```
User Action → Intent → ViewModel → UseCase → Repository → (AI / DB)
                ↑                                               ↓
           UiState ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
```

- **`UiState`** — a single `data class` exposed as `StateFlow<UiState>` from the ViewModel. The Compose screen observes it.
- **`UiIntent`** — sealed interface of all possible user actions (e.g., `SendMessage`, `ClearChat`).
- **`UiEvent`** — one-shot side effects (e.g., `ShowSnackbar`, `ScrollToBottom`) sent via `Channel<UiEvent>`.

### ViewModel Template

```kotlin
class ChatViewModel(
    private val sendMessage: SendMessageUseCase,
    private val getHistory: GetChatHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChatUiEvent>(Channel.BUFFERED)
    val events: Flow<ChatUiEvent> = _events.receiveAsFlow()

    fun onIntent(intent: ChatUiIntent) {
        when (intent) {
            is ChatUiIntent.SendMessage -> handleSend(intent.text)
            ChatUiIntent.ClearChat -> handleClear()
        }
    }

    private fun handleSend(text: String) = viewModelScope.launch {
        _uiState.update { it.copy(isGenerating = true) }
        sendMessage(text)
            .onSuccess { response ->
                _uiState.update { it.copy(isGenerating = false, messages = it.messages + response) }
                _events.send(ChatUiEvent.ScrollToBottom)
            }
            .onFailure { error ->
                _uiState.update { it.copy(isGenerating = false) }
                _events.send(ChatUiEvent.ShowError(error.message ?: "Unknown error"))
            }
    }
}
```

### Clean Architecture Layers

| Layer | Module | Depends on |
|---|---|---|
| UI (Compose + ViewModel) | `feature/*` | `domain` |
| Domain (UseCases, interfaces) | `core/domain` | Nothing (pure Kotlin) |
| Data (Room, AI client) | `core/data`, `core/ai` | `domain` |

The **domain layer has zero Android dependencies** — it is pure Kotlin and will compile for any KMP target.

---

## On-Device AI Integration

### How Gemini Nano Works on Android

- Gemini Nano is a system-level model shipped and managed by **Google Play Services** on Android 12+.
- Your app never bundles the model weights — it accesses inference via the `play-services-generativeai` SDK.
- The model may need to be downloaded by the system on first use; always check availability before calling inference.

### Availability States

```kotlin
sealed interface AiAvailability {
    data object Available : AiAvailability
    data class Downloading(val progress: Float) : AiAvailability
    data object Unavailable : AiAvailability  // Device does not support Gemini Nano
}
```

### OnDeviceAiClient Interface (KMP-ready)

```kotlin
interface OnDeviceAiClient {
    suspend fun checkAvailability(): AiAvailability
    fun generateStream(prompt: String): Flow<String>   // streaming token-by-token
    suspend fun generate(prompt: String): Result<String> // full response
}
```

### Android Implementation

```kotlin
class GeminiNanoClient(
    private val model: GenerativeModel  // com.google.ai.generativelanguage
) : OnDeviceAiClient {

    override suspend fun checkAvailability(): AiAvailability {
        return when (model.checkAvailability()) {
            AvailabilityStatus.AVAILABLE -> AiAvailability.Available
            AvailabilityStatus.DOWNLOADING -> AiAvailability.Downloading(0f)
            else -> AiAvailability.Unavailable
        }
    }

    override fun generateStream(prompt: String): Flow<String> = flow {
        model.generateContentStream(prompt).collect { chunk ->
            emit(chunk.text ?: "")
        }
    }

    override suspend fun generate(prompt: String): Result<String> = runCatching {
        model.generateContent(prompt).text ?: ""
    }
}
```

### Koin DI Setup

```kotlin
val aiModule = module {
    single<GenerativeModel> {
        GenerativeModel(modelName = "gemini-nano")
    }
    single<OnDeviceAiClient> {
        GeminiNanoClient(model = get())
    }
}
```

---

## Material 3 Design System

### Theme

- **Dynamic Color** is enabled by default — the app adapts to the user's wallpaper palette on Android 12+.
- Falls back to a hand-crafted seed color palette on older devices.
- Dark/light follows the **system setting** by default; user can override in app settings.

```kotlin
@Composable
fun PebbleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> PebbleDarkColorScheme
        else -> PebbleLightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PebbleTypography,
        shapes = PebbleShapes,
        content = content
    )
}
```

### Typography
- Use `MaterialTheme.typography.*` tokens only — no hardcoded font sizes.
- Display / Headline styles for AI responses, Body for messages, Label for metadata.

### Shape
- Message bubbles: `MaterialTheme.shapes.large` with asymmetric corners (user vs AI bubble).
- Cards and surfaces: `MaterialTheme.shapes.medium`.
- Input bar: `MaterialTheme.shapes.extraLarge` (pill shape).

---

## Animations

All animations use **only** the standard `androidx.compose.animation` and `androidx.compose.animation.core` libraries. No extra dependencies.

### Principles (Google-app feel)
- Use **spring physics** (`spring()`) instead of tween for natural feel.
- Keep durations short: 200–350ms for transitions, 150ms for micro-interactions.
- Motion should reinforce hierarchy — entering elements come from a logical spatial origin.
- Never block the user; animations run alongside interaction.

### Key Animations to Implement

| Element | Animation | API |
|---|---|---|
| Screen transitions | Slide + fade via `NavHost` | `Navigation 3 transitions` |
| New AI message appears | Slide up + fade in with spring | `AnimatedVisibility` + `slideInVertically` |
| Message bubbles | Staggered entrance on load | `LazyColumn` with index-based delay |
| Typing indicator | 3-dot bouncing animation | `InfiniteTransition` + `spring` |
| Send button | Scale press feedback | `animateFloatAsState` |
| AI thinking state | Shimmer / pulse on response area | `InfiniteTransition` + alpha |
| FAB / action button | Expand/collapse with `AnimatedContent` | `AnimatedContent` + `SizeTransform` |
| Feature card reveal | Shared element transition to detail | `SharedTransitionLayout` |
| Empty state | Subtle floating/breathing animation | `InfiniteTransition` + `translationY` |

### PebbleMotion — Shared Specs

```kotlin
object PebbleMotion {
    val springDefault = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val springGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    val durationShort = 150
    val durationMedium = 250
    val durationLong = 350

    val enterTransition: EnterTransition =
        fadeIn(tween(durationMedium)) + slideInVertically(
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
            initialOffsetY = { it / 4 }
        )

    val exitTransition: ExitTransition =
        fadeOut(tween(durationShort)) + slideOutVertically(
            animationSpec = tween(durationShort),
            targetOffsetY = { -it / 8 }
        )
}
```

---

## Navigation (Navigation 3)

Navigation 3 is the new declarative, type-safe navigation library for Compose.
It replaces the route-string approach with typed destinations.

```kotlin
// Destinations
@Serializable object HomeDestination
@Serializable object ChatDestination
@Serializable data class ChatDetailDestination(val sessionId: String)

// Nav host in MainActivity
@Composable
fun PebbleNavHost() {
    val backStack = rememberNavBackStack(HomeDestination)
    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(rememberSceneSetupNavEntryDecorator()),
        transitionSpec = { PebbleNavTransitionSpec },
    ) { entry ->
        when (val key = entry.key) {
            is HomeDestination -> HomeScreen(onNavigateToChat = { backStack.add(ChatDestination) })
            is ChatDestination -> ChatScreen(onBack = { backStack.removeLastOrNull() })
            is ChatDetailDestination -> ChatDetailScreen(sessionId = key.sessionId)
        }
    }
}
```

---

## Feature Roadmap

### Phase 1 — Foundation (current focus)
- [ ] Project scaffold: modules, theme, DI, navigation
- [ ] On-device AI client with availability check
- [ ] **Chat** — conversational AI with streaming responses
  - Message history (Room)
  - Typing indicator animation
  - Staggered message entrance
  - Streaming token display (text appears word-by-word)

### Phase 2 — Expand
- [ ] **Summarizer** — paste or share text → get a concise summary
- [ ] **Smart Rewrite** — improve tone/grammar of selected text
- [ ] **AI Journal** — private notes with AI reflection prompts
- [ ] Conversation sessions (multiple chat threads)

### Phase 3 — Polish
- [ ] App settings (theme override, clear all data)
- [ ] Onboarding flow (checks Gemini Nano availability, explains offline-only)
- [ ] Widget (quick prompt from home screen)
- [ ] KMP shared module extraction

---

## Dependency Reference

```toml
# gradle/libs.versions.toml

[versions]
agp                     = "8.9.0"
kotlin                   = "2.1.10"
ksp                      = "2.1.10-1.0.31"
compose-bom              = "2025.03.00"
navigation3              = "1.0.0-alpha04"
koin                     = "4.0.3"
room                     = "2.7.0"
datastore                = "1.1.3"
coroutines               = "1.10.1"
serialization            = "1.8.1"
gms-generativeai         = "0.9.0"

[libraries]
# Compose BOM
compose-bom              = { group = "androidx.compose", name = "compose-bom",           version.ref = "compose-bom" }
compose-ui               = { group = "androidx.compose.ui",      name = "ui" }
compose-ui-tooling       = { group = "androidx.compose.ui",      name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui",    name = "ui-tooling-preview" }
compose-material3        = { group = "androidx.compose.material3", name = "material3" }
compose-animation        = { group = "androidx.compose.animation", name = "animation" }
activity-compose         = { group = "androidx.activity",         name = "activity-compose",  version = "1.10.1" }

# Navigation 3
navigation3-compose      = { group = "androidx.navigation3",      name = "navigation3-ui",    version.ref = "navigation3" }
navigation3-runtime      = { group = "androidx.navigation3",      name = "navigation3-runtime", version.ref = "navigation3" }

# Koin
koin-android             = { group = "io.insert-koin",            name = "koin-android",      version.ref = "koin" }
koin-compose             = { group = "io.insert-koin",            name = "koin-compose",      version.ref = "koin" }

# Room
room-runtime             = { group = "androidx.room",             name = "room-runtime",      version.ref = "room" }
room-ktx                 = { group = "androidx.room",             name = "room-ktx",          version.ref = "room" }
room-compiler            = { group = "androidx.room",             name = "room-compiler",     version.ref = "room" }

# DataStore
datastore-preferences    = { group = "androidx.datastore",        name = "datastore-preferences", version.ref = "datastore" }

# Coroutines
coroutines-android       = { group = "org.jetbrains.kotlinx",    name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Serialization
kotlinx-serialization    = { group = "org.jetbrains.kotlinx",    name = "kotlinx-serialization-json",  version.ref = "serialization" }

# Gemini Nano (on-device)
gms-generativeai         = { group = "com.google.android.gms",   name = "play-services-generativeai", version.ref = "gms-generativeai" }

[plugins]
android-application      = { id = "com.android.application",      version.ref = "agp" }
android-library          = { id = "com.android.library",          version.ref = "agp" }
kotlin-android           = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose           = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization     = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp                      = { id = "com.google.devtools.ksp",      version.ref = "ksp" }
```

---

## Testing Strategy

| Layer | Tool | What to test |
|---|---|---|
| Domain (UseCases) | JUnit 5 + `kotlinx-coroutines-test` | Business logic, pure Kotlin |
| ViewModel (MVI) | JUnit 5 + Turbine | State transitions, intent handling |
| Data (Repository) | JUnit 5 + Room in-memory | DB queries, data mapping |
| AI Client | Mockk + fake `OnDeviceAiClient` | Availability states, streaming |
| UI (Compose) | Compose UI Test | Screen rendering, interactions |
| E2E | Espresso (optional) | Critical user flows |

### Test File Conventions
- Mirror the source structure under `src/test/` and `src/androidTest/`.
- One test class per production class.
- Use fakes/stubs over mocks wherever possible (e.g., `FakeOnDeviceAiClient`).
- ViewModels are tested with `TestCoroutineScheduler` + `Turbine` for Flow assertions.

---

## Code Style & Conventions

- **Kotlin idioms only** — extension functions, data classes, sealed interfaces, `when` exhaustiveness.
- **No nullable types at domain boundaries** — use `Result<T>` or sealed classes instead of `null`.
- File names match the primary class/composable they contain.
- Composables: PascalCase. Functions/properties: camelCase.
- Each `@Composable` screen receives only `UiState` and callback lambdas — no ViewModel references inside composables.
- Previews: every composable has at least one `@Preview` annotated with `@PreviewLightDark`.
- No `TODO` in committed code — use GitHub Issues instead.
- Gradle: no `implementation` in `app` module for anything that belongs in a feature module.

---

## Banned Dependencies

Do NOT add these to the project:

| Library | Reason |
|---|---|
| Retrofit / OkHttp | App is offline-only |
| Hilt | Not KMP-compatible; use Koin |
| Glide / Coil | No remote images; use vector assets |
| Any third-party animation lib | Use Material 3 built-ins only |
| Firebase | Requires network; violates offline principle |
| Any crash reporting SDK | No telemetry |

---

## Min SDK & Device Requirements

| Property | Value |
|---|---|
| `minSdk` | **31** (Android 12) |
| `targetSdk` | 35 (Android 15) |
| `compileSdk` | 35 |

**Why API 31?**
- Gemini Nano / Android AICore is fully supported on Android 12+.
- Dynamic Color (`dynamicDarkColorScheme` / `dynamicLightColorScheme`) requires API 31+.
- `SplashScreen` API requires API 31+.
- Predictive back gesture animation requires API 31+.
- This lets us skip virtually all `Build.VERSION.SDK_INT` checks in UI code.

---

## Git Workflow

- Branch naming: `feature/<name>`, `fix/<name>`, `chore/<name>`
- Commits: Conventional Commits format (`feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`)
- Each feature module is developed and reviewed independently
- PRs require passing all unit tests before merge
