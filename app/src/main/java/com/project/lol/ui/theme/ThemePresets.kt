package com.project.lol.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

data class ThemePalette(
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val alt: Color,
    val bg: Color,
    val panel: Color,
    val panelHigh: Color,
    val fg: Color,
    val fgMuted: Color,
    val outline: Color,
    val onAlt: Color = bg,
    val altContainer: Color = panelHigh,
    val onAltContainer: Color = fg,
)

val DEFAULT_PALETTE = ThemePalette(
    accent = Color(0xFF7FC79A),
    onAccent = Color(0xFF121411),
    accentSoft = Color(0xFF2E6B45),
    onAccentSoft = Color(0xFFE3E2DC),
    alt = Color(0xFFB6CCBB),
    bg = Color(0xFF121411),
    panel = Color(0xFF1C1E1A),
    panelHigh = Color(0xFF45473F),
    fg = Color(0xFFE3E2DC),
    fgMuted = Color(0xFFC6C5BE),
    outline = Color(0xFF909189),
)

fun ThemePalette.toColorScheme(): ColorScheme = darkColorScheme().copy(
    primary = accent,
    onPrimary = onAccent,
    primaryContainer = accentSoft,
    onPrimaryContainer = onAccentSoft,
    secondary = alt,
    onSecondary = onAlt,
    secondaryContainer = altContainer,
    onSecondaryContainer = onAltContainer,
    tertiary = accent,
    onTertiary = onAccent,
    tertiaryContainer = accentSoft,
    onTertiaryContainer = onAccentSoft,
    background = bg,
    onBackground = fg,
    surface = bg,
    onSurface = fg,
    surfaceVariant = panelHigh,
    onSurfaceVariant = fgMuted,
    outline = outline,
    outlineVariant = panelHigh,
    surfaceContainerLowest = bg,
    surfaceContainerLow = panel,
    surfaceContainer = panel,
    surfaceContainerHigh = panelHigh,
    surfaceContainerHighest = panelHigh,
    surfaceTint = accent,
)

private val AmoledBlack = Color(0xFF000000)
private val AmoledPanel = Color(0xFF0A0A0A)
private val AmoledPanelHigh = Color(0xFF161616)
private val AmoledPanelHighest = Color(0xFF1C1C1C)

fun ColorScheme.amoledScheme(): ColorScheme = copy(
    background = AmoledBlack,
    surface = AmoledBlack,
    surfaceContainerLowest = AmoledBlack,
    surfaceContainerLow = AmoledPanel,
    surfaceContainer = AmoledPanel,
    surfaceContainerHigh = AmoledPanelHigh,
    surfaceContainerHighest = AmoledPanelHighest,
    surfaceVariant = AmoledPanelHigh,
)
