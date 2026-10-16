package com.project.lol.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private const val THEME_ANIMATION_DURATION = 450

@Composable
fun SpotifyTheme(content: @Composable () -> Unit) {
    val target = DEFAULT_PALETTE.toColorScheme()
    val colorScheme = if (ThemeController.amoled) target.amoledScheme() else target

    MaterialTheme(
        colorScheme = colorScheme.animated(),
        typography = RalewayTypography,
        content = content
    )
}

@Composable
private fun animatedThemeColor(color: Color): Color = animateColorAsState(
    targetValue = color,
    animationSpec = tween(durationMillis = THEME_ANIMATION_DURATION, easing = FastOutSlowInEasing),
    label = "themeColor"
).value

@Composable
private fun ColorScheme.animated(): ColorScheme = copy(
    primary = animatedThemeColor(primary),
    onPrimary = animatedThemeColor(onPrimary),
    primaryContainer = animatedThemeColor(primaryContainer),
    onPrimaryContainer = animatedThemeColor(onPrimaryContainer),
    inversePrimary = animatedThemeColor(inversePrimary),
    secondary = animatedThemeColor(secondary),
    onSecondary = animatedThemeColor(onSecondary),
    secondaryContainer = animatedThemeColor(secondaryContainer),
    onSecondaryContainer = animatedThemeColor(onSecondaryContainer),
    tertiary = animatedThemeColor(tertiary),
    onTertiary = animatedThemeColor(onTertiary),
    tertiaryContainer = animatedThemeColor(tertiaryContainer),
    onTertiaryContainer = animatedThemeColor(onTertiaryContainer),
    background = animatedThemeColor(background),
    onBackground = animatedThemeColor(onBackground),
    surface = animatedThemeColor(surface),
    onSurface = animatedThemeColor(onSurface),
    surfaceVariant = animatedThemeColor(surfaceVariant),
    onSurfaceVariant = animatedThemeColor(onSurfaceVariant),
    surfaceTint = animatedThemeColor(surfaceTint),
    inverseSurface = animatedThemeColor(inverseSurface),
    inverseOnSurface = animatedThemeColor(inverseOnSurface),
    error = animatedThemeColor(error),
    onError = animatedThemeColor(onError),
    errorContainer = animatedThemeColor(errorContainer),
    onErrorContainer = animatedThemeColor(onErrorContainer),
    outline = animatedThemeColor(outline),
    outlineVariant = animatedThemeColor(outlineVariant),
    scrim = animatedThemeColor(scrim),
    surfaceBright = animatedThemeColor(surfaceBright),
    surfaceDim = animatedThemeColor(surfaceDim),
    surfaceContainer = animatedThemeColor(surfaceContainer),
    surfaceContainerHigh = animatedThemeColor(surfaceContainerHigh),
    surfaceContainerHighest = animatedThemeColor(surfaceContainerHighest),
    surfaceContainerLow = animatedThemeColor(surfaceContainerLow),
    surfaceContainerLowest = animatedThemeColor(surfaceContainerLowest)
)
