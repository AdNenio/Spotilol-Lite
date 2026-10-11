package com.project.lol.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

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
)

data class ThemePreset(
    val id: String,
    val family: String,
    val name: String,
    val palette: ThemePalette,
)

fun ThemePalette.toColorScheme(): ColorScheme = darkColorScheme().copy(
    primary = accent,
    onPrimary = onAccent,
    primaryContainer = accentSoft,
    onPrimaryContainer = onAccentSoft,
    secondary = alt,
    onSecondary = bg,
    secondaryContainer = panelHigh,
    onSecondaryContainer = fg,
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

private fun pal(
    accent: Long,
    accentSoft: Long,
    alt: Long,
    bg: Long,
    panel: Long,
    panelHigh: Long,
    fg: Long,
    fgMuted: Long,
    outline: Long,
) = ThemePalette(
    accent = Color(accent),
    onAccent = Color(bg),
    accentSoft = Color(accentSoft),
    onAccentSoft = Color(fg),
    alt = Color(alt),
    bg = Color(bg),
    panel = Color(panel),
    panelHigh = Color(panelHigh),
    fg = Color(fg),
    fgMuted = Color(fgMuted),
    outline = Color(outline),
)

object ThemePresets {

    const val DEFAULT_ID = "spotilol"

    val all = listOf(
        ThemePreset(
            "spotilol", "Spotilol", "Dark",
            pal(0xFF7FC79A, 0xFF2E6B45, 0xFFB6CCBB, 0xFF121411, 0xFF1C1E1A, 0xFF45473F, 0xFFE3E2DC, 0xFFC6C5BE, 0xFF909189),
        ),
        ThemePreset(
            "catppuccin", "Catppuccin", "Mocha",
            pal(0xFFCBA6F7, 0xFF4C4368, 0xFF89B4FA, 0xFF1E1E2E, 0xFF313244, 0xFF45475A, 0xFFCDD6F4, 0xFFA6ADC8, 0xFF6C7086),
        ),
        ThemePreset(
            "catppuccin_frappe", "Catppuccin", "Frappé",
            pal(0xFFCA9EE6, 0xFF514B6B, 0xFF8CAAEE, 0xFF303446, 0xFF414559, 0xFF51576D, 0xFFC6D3F5, 0xFFA5ADCE, 0xFF737994),
        ),
        ThemePreset(
            "catppuccin_macchiato", "Catppuccin", "Macchiato",
            pal(0xFFC6A0F6, 0xFF4E4265, 0xFF8AADF4, 0xFF24273A, 0xFF363A4F, 0xFF494D64, 0xFFCAD3F5, 0xFFA8ADCB, 0xFF6E738D),
        ),
        ThemePreset(
            "tokyo_night_storm", "Tokyo Night", "Storm",
            pal(0xFF7AA2F7, 0xFF344A75, 0xFFBB9AF7, 0xFF24283B, 0xFF2F344E, 0xFF414863, 0xFFA9B1D6, 0xFF8A93C4, 0xFF565F89),
        ),
        ThemePreset(
            "tokyo_night_night", "Tokyo Night", "Night",
            pal(0xFF7AA2F7, 0xFF2D3C66, 0xFFBB9AF7, 0xFF1A1B26, 0xFF21243A, 0xFF343A55, 0xFFC0CAF5, 0xFF9AA5CE, 0xFF565F89),
        ),
        ThemePreset(
            "dracula", "Dracula", "Classic",
            pal(0xFFBD93F9, 0xFF44475A, 0xFF50FA7B, 0xFF282A36, 0xFF333545, 0xFF44475A, 0xFFF8F8F2, 0xFFA6A9C0, 0xFF6272A4),
        ),
        ThemePreset(
            "nord", "Nord", "Polar Night",
            pal(0xFF88C0D0, 0xFF3B4E63, 0xFFA3BE8C, 0xFF2E3440, 0xFF3B4252, 0xFF434C5E, 0xFFECEFF4, 0xFFBBC3CF, 0xFF616E88),
        ),
        ThemePreset(
            "gruvbox", "Gruvbox", "Dark",
            pal(0xFFFE8019, 0xFF504945, 0xFF8EC07C, 0xFF282828, 0xFF3C3836, 0xFF504945, 0xFFEBDBB2, 0xFFBDAE93, 0xFF928374),
        ),
        ThemePreset(
            "solarized", "Solarized", "Dark",
            pal(0xFF268BD2, 0xFF0A4A63, 0xFF859900, 0xFF002B36, 0xFF073642, 0xFF0F4C60, 0xFF93A1A1, 0xFF6F8A94, 0xFF586E75),
        ),
        ThemePreset(
            "one_dark", "One Dark", "Atom",
            pal(0xFF61AFEF, 0xFF383E4C, 0xFF98C379, 0xFF282C34, 0xFF2F333E, 0xFF3E4451, 0xFFABB2BF, 0xFF7F8798, 0xFF4B5263),
        ),
        ThemePreset(
            "everforest", "Everforest", "Dark",
            pal(0xFFA7C080, 0xFF3F4A41, 0xFFE69875, 0xFF2D353B, 0xFF343F44, 0xFF3D484D, 0xFFD3C6AA, 0xFF9DA9A0, 0xFF7A8478),
        ),
        ThemePreset(
            "kanagawa", "Kanagawa", "Wave",
            pal(0xFF7E9CD8, 0xFF2D4F67, 0xFF98BB6C, 0xFF1F1F28, 0xFF2A2A37, 0xFF363646, 0xFFDCD7BA, 0xFFA6A392, 0xFF54546D),
        ),
        ThemePreset(
            "rose_pine", "Rosé Pine", "Pine",
            pal(0xFFEBBCBA, 0xFF403D52, 0xFF9CCFD8, 0xFF191724, 0xFF1F1D2E, 0xFF26233A, 0xFFE0DEF4, 0xFF908CAA, 0xFF6E6A86),
        ),
        ThemePreset(
            "rose_pine_moon", "Rosé Pine", "Moon",
            pal(0xFFEBBCBA, 0xFF403D52, 0xFF9CCFD8, 0xFF232136, 0xFF2A273F, 0xFF393552, 0xFFE0DEF4, 0xFF908CAA, 0xFF6E6A86),
        ),
        ThemePreset(
            "ayu", "Ayu", "Dark",
            pal(0xFFFFB454, 0xFF453322, 0xFF59C2FF, 0xFF0B0E14, 0xFF11151C, 0xFF1C2130, 0xFFBFBDB6, 0xFF8A9098, 0xFF3E4B5B),
        ),
        ThemePreset(
            "ayu_mirage", "Ayu", "Mirage",
            pal(0xFFFF9940, 0xFF3E4859, 0xFF73D0FF, 0xFF1F2430, 0xFF283042, 0xFF333B4F, 0xFFD9D7CE, 0xFFA6ACBB, 0xFF607089),
        ),
        ThemePreset(
            "monokai_pro", "Monokai Pro", "Classic",
            pal(0xFFA9DC76, 0xFF45403E, 0xFFFF6188, 0xFF2D2A2E, 0xFF373338, 0xFF4A4543, 0xFFFCFCFA, 0xFFB7B4AF, 0xFF727072),
        ),
    )

    val byId = all.associateBy { it.id }

    fun resolve(id: String): ThemePalette =
        (byId[id] ?: byId.getValue(DEFAULT_ID)).palette
}

fun schemeFromSeed(seed: Color): ColorScheme {
    fun rotated(degrees: Float): Color {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(seed.toArgb(), hsv)
        val h = (hsv[0] + degrees + 360f) % 360f
        val s = hsv[1].coerceIn(0.3f, 0.85f)
        return Color(android.graphics.Color.HSVToColor(floatArrayOf(h, s, hsv[2])))
    }

    fun onColor(c: Color): Color =
        if (c.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White

    val secondary = rotated(30f)
    val tertiary = rotated(-30f)

    return darkColorScheme().copy(
        primary = seed,
        onPrimary = onColor(seed),
        primaryContainer = lerp(seed, Color.White, 0.22f),
        onPrimaryContainer = Color.White,
        secondary = secondary,
        onSecondary = onColor(secondary),
        secondaryContainer = lerp(secondary, Color.White, 0.22f),
        onSecondaryContainer = Color.White,
        tertiary = tertiary,
        onTertiary = onColor(tertiary),
        tertiaryContainer = lerp(tertiary, Color.White, 0.22f),
        onTertiaryContainer = Color.White
    )
}

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
