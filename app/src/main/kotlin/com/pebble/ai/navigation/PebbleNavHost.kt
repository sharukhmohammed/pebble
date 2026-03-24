package com.pebble.ai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pebble.feature.chat.ui.ChatScreen
import com.pebble.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object ChatRoute

@Composable
fun PebbleNavHost() {
    val backStack = rememberNavBackStack(HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is HomeRoute -> NavEntry(key) {
                    HomeScreen(
                        onNavigateToChat = { backStack.add(ChatRoute) },
                    )
                }
                is ChatRoute -> NavEntry(key) {
                    ChatScreen(
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                else -> NavEntry(key) {}
            }
        },
    )
}
