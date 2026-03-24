package com.pebble.core.design.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pebble.core.design.theme.PebbleTheme

/**
 * Three bouncing dots used as a typing / loading indicator.
 * Each dot is staggered by [staggerMs] milliseconds for a wave effect.
 */
@Composable
fun PebbleLoadingDots(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    staggerMs: Int = 140,
    bounceHeight: Float = 8f,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue  = 0f,
                targetValue   = -bounceHeight,
                animationSpec = infiniteRepeatable(
                    animation    = tween(400, easing = FastOutSlowInEasing),
                    repeatMode   = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * staggerMs),
                ),
                label = "dot_offset_$index",
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(y = offsetY.dp)
                    .background(dotColor, CircleShape),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PebbleLoadingDotsPreview() {
    PebbleTheme { PebbleLoadingDots(modifier = Modifier) }
}
