package com.pebble.feature.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.pebble.feature.home.ui.components.FeatureCard
import kotlinx.coroutines.delay

data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isEnabled: Boolean = true,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Drive stagger animation after first composition
    var staggerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        staggerVisible = true
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pebble AI",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = "Offline. Private. Always on.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(features) { index, feature ->
                // Each card enters with a staggered delay based on its grid position.
                var cardVisible by remember { mutableStateOf(false) }
                LaunchedEffect(staggerVisible) {
                    if (staggerVisible) {
                        delay(index * 80L)
                        cardVisible = true
                    }
                }

                AnimatedVisibility(
                    visible = cardVisible,
                    enter = fadeIn(tween(250)) + slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMedium,
                        ),
                        initialOffsetY = { it / 2 },
                    ),
                ) {
                    FeatureCard(
                        feature = feature,
                        onClick = {
                            if (feature.isEnabled) {
                                when (feature.title) {
                                    "Chat" -> onNavigateToChat()
                                    else   -> Unit // Coming soon
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

private val features = listOf(
    Feature(
        title       = "Chat",
        description = "Conversational AI, fully on-device",
        icon        = Icons.Rounded.Chat,
        isEnabled   = true,
    ),
    Feature(
        title       = "Summarize",
        description = "Condense any text instantly",
        icon        = Icons.Rounded.Description,
        isEnabled   = false,
    ),
    Feature(
        title       = "Rewrite",
        description = "Improve tone and clarity",
        icon        = Icons.Rounded.EditNote,
        isEnabled   = false,
    ),
    Feature(
        title       = "Settings",
        description = "Customize your experience",
        icon        = Icons.Rounded.Tune,
        isEnabled   = false,
    ),
)
