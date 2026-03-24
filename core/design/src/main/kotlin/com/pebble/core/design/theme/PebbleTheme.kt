package com.pebble.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary             = PebblePrimary,
    onPrimary           = PebbleOnPrimary,
    primaryContainer    = PebblePrimaryContainer,
    onPrimaryContainer  = PebbleOnPrimaryContainer,
    secondary           = PebbleSecondary,
    onSecondary         = PebbleOnSecondary,
    secondaryContainer  = PebbleSecondaryContainer,
    onSecondaryContainer= PebbleOnSecondaryContainer,
    tertiary            = PebbleTertiary,
    onTertiary          = PebbleOnTertiary,
    tertiaryContainer   = PebbleTertiaryContainer,
    onTertiaryContainer = PebbleOnTertiaryContainer,
    background          = PebbleBackground,
    onBackground        = PebbleOnBackground,
    surface             = PebbleSurface,
    onSurface           = PebbleOnSurface,
    surfaceVariant      = PebbleSurfaceVariant,
    onSurfaceVariant    = PebbleOnSurfaceVariant,
    outline             = PebbleOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary             = PebblePrimaryDark,
    onPrimary           = PebbleOnPrimaryDark,
    primaryContainer    = PebblePrimaryContainerDark,
    onPrimaryContainer  = PebbleOnPrimaryContainerDark,
    secondary           = PebbleSecondaryDark,
    onSecondary         = PebbleOnSecondaryDark,
    secondaryContainer  = PebbleSecondaryContainerDark,
    onSecondaryContainer= PebbleOnSecondaryContainerDark,
    tertiary            = PebbleTertiaryDark,
    onTertiary          = PebbleOnTertiaryDark,
    tertiaryContainer   = PebbleTertiaryContainerDark,
    onTertiaryContainer = PebbleOnTertiaryContainerDark,
    background          = PebbleBackgroundDark,
    onBackground        = PebbleOnBackgroundDark,
    surface             = PebbleSurfaceDark,
    onSurface           = PebbleOnSurfaceDark,
    surfaceVariant      = PebbleSurfaceVariantDark,
    onSurfaceVariant    = PebbleOnSurfaceVariantDark,
    outline             = PebbleOutlineDark,
)

/**
 * Root theme for Pebble AI.
 *
 * - Dynamic Color is on by default: adapts to the user's wallpaper on Android 12+.
 * - Dark/light follows the system setting; users can override via app settings.
 */
@Composable
fun PebbleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && darkTheme  -> dynamicDarkColorScheme(context)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme                  -> DarkColorScheme
        else                       -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = PebbleTypography,
        shapes      = PebbleShapes,
        content     = content,
    )
}
