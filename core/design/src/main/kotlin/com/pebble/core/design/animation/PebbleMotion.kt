package com.pebble.core.design.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

/**
 * Central motion spec for Pebble AI.
 *
 * All durations are in milliseconds. Spring specs use M3 recommended damping values.
 * No third-party animation library is used — only androidx.compose.animation.
 */
object PebbleMotion {

    const val DurationShort  = 150
    const val DurationMedium = 250
    const val DurationLong   = 350

    /** Bouncy spring — use for buttons, cards, FABs entering the screen. */
    fun <T> springBouncy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium,
    )

    /** Gentle spring — use for large panels and list items. */
    fun <T> springGentle(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessLow,
    )

    /** Snappy spring — use for quick dismiss and collapse actions. */
    fun <T> springSnappy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMediumLow,
    )

    /** Placement spring for LazyList animateItem(). */
    val itemPlacementSpec: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium,
    )

    // ---- Screen-level transitions (NavDisplay) ----

    val screenEnter: EnterTransition =
        fadeIn(tween(DurationMedium)) +
            slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
                initialOffsetY = { it / 6 },
            )

    val screenExit: ExitTransition =
        fadeOut(tween(DurationShort)) +
            slideOutVertically(
                animationSpec = tween(DurationShort),
                targetOffsetY = { -it / 12 },
            )

    // ---- Message-level transitions (chat bubbles) ----

    val messageEnter: EnterTransition =
        fadeIn(tween(DurationMedium)) +
            slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMediumLow,
                ),
                initialOffsetY = { it / 3 },
            )
}
