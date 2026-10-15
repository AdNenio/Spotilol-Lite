package com.project.lol.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.mutableStateListOf
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
    val onAlt: Color = bg,
    val altContainer: Color = panelHigh,
    val onAltContainer: Color = fg,
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

    private val builtIn = listOf(
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
        ThemePreset(
            "spotify_classic", "Spotify", "Classic Green",
            pal(0xFF1DB954, 0xFF0E4A28, 0xFF1ED760, 0xFF121212, 0xFF181818, 0xFF282828, 0xFFFFFFFF, 0xFFB3B3B3, 0xFF404040),
        ),
        ThemePreset(
            "aurora", "Aurora", "Borealis",
            pal(0xFF64E3B4, 0xFF1F4D42, 0xFFA78BFA, 0xFF0A1210, 0xFF111C18, 0xFF1A2923, 0xFFE3F2EB, 0xFF93ADA0, 0xFF27382F),
        ),
        ThemePreset(
            "midnight_jazz", "Jazz Club", "Midnight",
            pal(0xFFE2B857, 0xFF54431C, 0xFF88A9C9, 0xFF12151C, 0xFF191D26, 0xFF252B37, 0xFFF0EBDE, 0xFFA6A598, 0xFF383F4D),
        ),
        ThemePreset(
            "cappuccino", "Cappuccino", "Cream",
            pal(0xFFD8A878, 0xFF503C24, 0xFFA8BBA0, 0xFF16120E, 0xFF1F1915, 0xFF2B2420, 0xFFF3EADF, 0xFFB6A896, 0xFF3F362D),
        ),
        ThemePreset(
            "matcha", "Matcha", "Latte",
            pal(0xFFA6C36F, 0xFF3B4A22, 0xFFD9C08F, 0xFF12150E, 0xFF1A1E15, 0xFF262B1F, 0xFFEDF1E0, 0xFFA8AF94, 0xFF363C2C),
        ),
        ThemePreset(
            "lavender", "Lavender", "Fields",
            pal(0xFFC9B1FF, 0xFF453963, 0xFFA5BFFF, 0xFF14121A, 0xFF1D1B25, 0xFF292733, 0xFFEEEAF6, 0xFFAEA8C0, 0xFF3C3849),
        ),
        ThemePreset(
            "velvet", "Velvet", "Rose",
            pal(0xFFD96A8B, 0xFF4E2434, 0xFFB98BD9, 0xFF151013, 0xFF1E171B, 0xFF2A2025, 0xFFF5E9ED, 0xFFB49FAB, 0xFF3B2D34),
        ),
        ThemePreset(
            "rose_gold", "Rose Gold", "Blush",
            pal(0xFFECAC97, 0xFF56382E, 0xFFF2D8B4, 0xFF181213, 0xFF221919, 0xFF2E2423, 0xFFF7ECE7, 0xFFB9A59E, 0xFF423331),
        ),
        ThemePreset(
            "sunrise", "Sunrise", "Peach",
            pal(0xFFFFB08A, 0xFF5A3A24, 0xFFFFDCA8, 0xFF191210, 0xFF231A17, 0xFF302421, 0xFFF9ECE3, 0xFFC3AAA0, 0xFF44332D),
        ),
        ThemePreset(
            "bonfire", "Bonfire", "Ember",
            pal(0xFFFF6B4A, 0xFF57301F, 0xFFFFC864, 0xFF170F0C, 0xFF221713, 0xFF2F211A, 0xFFF9EDE3, 0xFFB7A094, 0xFF412F24),
        ),
        ThemePreset(
            "cotton_candy", "Cotton Candy", "Pastel",
            pal(0xFFFFA8C8, 0xFF5A3548, 0xFF9FD4FA, 0xFF171218, 0xFF211A20, 0xFF2D252B, 0xFFF8EEF4, 0xFFBBA6B3, 0xFF41343C),
        ),
        ThemePreset(
            "synthwave_84", "Synthwave", "'84",
            pal(0xFFFF7EDB, 0xFF4E2745, 0xFF36F9F6, 0xFF241B2F, 0xFF2D2240, 0xFF372B54, 0xFFF7F7FB, 0xFFB5B3BD, 0xFF495495),
        ),
        ThemePreset(
            "vaporwave", "Vaporwave", "Neon",
            pal(0xFFFF71CE, 0xFF4E2444, 0xFF01CDFE, 0xFF191024, 0xFF231838, 0xFF2F204C, 0xFFF7F0FF, 0xFFB39ECB, 0xFF42296A),
        ),
        ThemePreset(
            "cyberpunk", "Cyberpunk", "Neon Night",
            pal(0xFFFCEE0A, 0xFF514E12, 0xFF00F0FF, 0xFF101014, 0xFF17171D, 0xFF21212B, 0xFFF6F7F8, 0xFF9DA0A8, 0xFF34343F),
        ),
        ThemePreset(
            "neon_purple", "Neon", "Purple",
            pal(0xFFB388FF, 0xFF3B2B5E, 0xFFEA80FC, 0xFF120E1A, 0xFF1A1424, 0xFF251C30, 0xFFEDE7F6, 0xFFA898BC, 0xFF382A4A),
        ),
        ThemePreset(
            "sakura", "Sakura", "Blossom",
            pal(0xFFF5A8C0, 0xFF5A3242, 0xFF9EC9E2, 0xFF1B1216, 0xFF251A20, 0xFF312329, 0xFFF5E8ED, 0xFFBCA0AC, 0xFF45303A),
        ),
        ThemePreset(
            "crimson", "Crimson", "Night",
            pal(0xFFFF5252, 0xFF521F1F, 0xFFFF8A80, 0xFF150D0F, 0xFF1F1315, 0xFF2A1A1D, 0xFFF5E6E8, 0xFFB79CA0, 0xFF412A2E),
        ),
        ThemePreset(
            "emerald", "Emerald", "Forest",
            pal(0xFF34D399, 0xFF144D2E, 0xFF6EE7B7, 0xFF0C1410, 0xFF122019, 0xFF1A2B22, 0xFFD7F5E5, 0xFF87A998, 0xFF26402F),
        ),
        ThemePreset(
            "sunset", "Sunset", "Dusk",
            pal(0xFFFF8A50, 0xFF502F1E, 0xFFFFC46B, 0xFF1A1210, 0xFF251A16, 0xFF312220, 0xFFF8EBE0, 0xFFBCA293, 0xFF453229),
        ),
        ThemePreset(
            "ocean_deep", "Ocean", "Deep",
            pal(0xFF4FC3F7, 0xFF1E4557, 0xFF80DEEA, 0xFF0A1724, 0xFF112231, 0xFF1A3245, 0xFFE3F2FD, 0xFF86A8C4, 0xFF274A63),
        ),
        ThemePreset(
            "matrix", "Matrix", "Terminal",
            pal(0xFF00E676, 0xFF0B4A22, 0xFF69F0AE, 0xFF0A0F0A, 0xFF111811, 0xFF1A231A, 0xFFC8E6C9, 0xFF7FA47F, 0xFF273327),
        ),
        ThemePreset(
            "amber", "Amber", "Retro",
            pal(0xFFFFB74D, 0xFF4E3A18, 0xFFFFE082, 0xFF14120E, 0xFF1D1A14, 0xFF27231B, 0xFFEDDFC5, 0xFFA89880, 0xFF3C362A),
        ),
        ThemePreset(
            "youtube_music", "YouTube Music", "Midnight",
            pal(0xFFFF5252, 0xFF592828, 0xFFFF8A80, 0xFF121212, 0xFF1B1B1B, 0xFF292929, 0xFFFFFFFF, 0xFFB3B3B3, 0xFF414141),
        ),
        ThemePreset(
            "tidal", "Tidal", "Hi-Fi",
            pal(0xFFB8F5D0, 0xFF294D3C, 0xFF8BE9C0, 0xFF101312, 0xFF191D1B, 0xFF252B28, 0xFFF2F7F4, 0xFFA3B0A9, 0xFF414C46),
        ),
        ThemePreset(
            "deezer", "Deezer", "Flow",
            pal(0xFFFF70B5, 0xFF542743, 0xFF9A8CFF, 0xFF15121B, 0xFF211C2B, 0xFF30273D, 0xFFF7F1FF, 0xFFB9ADC9, 0xFF493B58),
        ),
        ThemePreset(
            "apple_music", "Apple Music", "Dark Red",
            pal(0xFFFF375F, 0xFF582233, 0xFFFF8FA3, 0xFF121212, 0xFF1C1C1E, 0xFF2C2C2E, 0xFFF5F5F7, 0xFFAEAEB2, 0xFF48484A),
        ),
        ThemePreset(
            "soundcloud", "SoundCloud", "Orange Wave",
            pal(0xFFFF7A1A, 0xFF60361D, 0xFFFFB36B, 0xFF14110F, 0xFF201A16, 0xFF30231B, 0xFFFFF3E9, 0xFFC1A995, 0xFF4B382B),
        ),
        ThemePreset(
            "pure_black", "AMOLED", "Pure Black",
            pal(0xFFBB86FC, 0xFF392750, 0xFF03DAC6, 0xFF000000, 0xFF080808, 0xFF161616, 0xFFF5F5F5, 0xFFAAAAAA, 0xFF383838),
        ),
        ThemePreset(
            "graphite", "Graphite", "Monochrome",
            pal(0xFFD0D0D0, 0xFF424242, 0xFF999999, 0xFF101010, 0xFF1A1A1A, 0xFF282828, 0xFFF0F0F0, 0xFFAAAAAA, 0xFF484848),
        ),
        ThemePreset(
            "midnight_blue", "Midnight", "Blue Hour",
            pal(0xFF82B1FF, 0xFF263F67, 0xFF80DEEA, 0xFF080F1C, 0xFF101A2A, 0xFF1B2940, 0xFFE8F0FF, 0xFF9EADC7, 0xFF354762),
        ),
        ThemePreset(
            "obsidian", "Obsidian", "Violet Ink",
            pal(0xFFB69CFF, 0xFF3B2E5B, 0xFF85D7D0, 0xFF100E16, 0xFF1B1823, 0xFF292333, 0xFFF0ECF8, 0xFFB0A7BE, 0xFF433A50),
        ),
        ThemePreset(
            "steel_blue", "Steel", "Blue Gray",
            pal(0xFF90CAF9, 0xFF30475D, 0xFFB0BEC5, 0xFF11171C, 0xFF1B242B, 0xFF29353F, 0xFFEAF1F5, 0xFFA2B0BA, 0xFF43525D),
        ),
        ThemePreset(
            "amethyst", "Amethyst", "Dream",
            pal(0xFFD0A5FF, 0xFF50366B, 0xFFFFB4D8, 0xFF17111E, 0xFF23192D, 0xFF32243F, 0xFFF6EDFF, 0xFFC0AFCB, 0xFF4A3957),
        ),
        ThemePreset(
            "ocean_breeze", "Ocean Breeze", "Lagoon",
            pal(0xFF69D8E8, 0xFF214B58, 0xFF8BB8FF, 0xFF0B151B, 0xFF14232B, 0xFF20343F, 0xFFE4F7FA, 0xFF9CBCC4, 0xFF34515C),
        ),
        ThemePreset(
            "arctic", "Arctic", "Frost",
            pal(0xFFA5D8FF, 0xFF31516A, 0xFFB9F0E4, 0xFF101820, 0xFF19242D, 0xFF25333F, 0xFFEAF5FF, 0xFFA8BACB, 0xFF405463),
        ),
        ThemePreset(
            "dusty_rose", "Dusty Rose", "Muted Pink",
            pal(0xFFE5A3B5, 0xFF55313D, 0xFFD8B4E2, 0xFF191216, 0xFF251A20, 0xFF34242C, 0xFFF6EAF0, 0xFFC0A7B2, 0xFF49343E),
        ),
        ThemePreset(
            "lilac_dream", "Lilac", "Dreamscape",
            pal(0xFFC5B5FF, 0xFF453968, 0xFF9FDCEB, 0xFF15131D, 0xFF201D2C, 0xFF2F2A3E, 0xFFF1EDFF, 0xFFB5ADC9, 0xFF494257),
        ),
        ThemePreset(
            "vhs", "VHS", "Retro Tape",
            pal(0xFFFF5FA2, 0xFF552343, 0xFF42E8E0, 0xFF170F20, 0xFF24172F, 0xFF34203F, 0xFFFFF0FA, 0xFFC2A7C4, 0xFF4D355A),
        ),
        ThemePreset(
            "film_noir", "Film Noir", "Silver Screen",
            pal(0xFFD9D9D9, 0xFF414141, 0xFFA8A8A8, 0xFF0D0D0D, 0xFF191919, 0xFF292929, 0xFFF2F2F2, 0xFFAAAAAA, 0xFF444444),
        ),
        ThemePreset(
            "golden_hour", "Golden Hour", "Cinema",
            pal(0xFFFFCB69, 0xFF59431E, 0xFFFF986B, 0xFF17120D, 0xFF241C14, 0xFF35291C, 0xFFFFF3DE, 0xFFC5B293, 0xFF51412C),
        ),
        ThemePreset(
            "retro_wave", "Retro Wave", "Sunset Drive",
            pal(0xFFFF8CBE, 0xFF542844, 0xFFFFC078, 0xFF1A1022, 0xFF29172F, 0xFF3B2140, 0xFFFFF0FA, 0xFFC6A9C9, 0xFF54375D),
        ),
        ThemePreset(
            "copper", "Copper", "Vintage",
            pal(0xFFE6A477, 0xFF573726, 0xFFC9B08A, 0xFF17120F, 0xFF241B16, 0xFF35271F, 0xFFF4E8DD, 0xFFC0A999, 0xFF514034),
        ),
        ThemePreset(
            "electric_blue", "Electric", "Blue",
            pal(0xFF448AFF, 0xFF233E75, 0xFF00E5FF, 0xFF0B0F19, 0xFF141B2B, 0xFF202C43, 0xFFEAF1FF, 0xFFA4B4D4, 0xFF354966),
        ),
        ThemePreset(
            "acid_lime", "Acid", "Lime",
            pal(0xFFD4FF00, 0xFF454F16, 0xFF65FBD2, 0xFF10130C, 0xFF1A2012, 0xFF29321B, 0xFFF2F7E8, 0xFFB4BE9E, 0xFF414B32),
        ),
        ThemePreset(
            "miami_vice", "Miami Vice", "Neon Coast",
            pal(0xFFFF77C8, 0xFF562649, 0xFF52E5FF, 0xFF15101E, 0xFF21182F, 0xFF30223F, 0xFFF9F0FF, 0xFFC0A9D0, 0xFF4D3A61),
        ),
        ThemePreset(
            "laser_green", "Laser", "Green Beam",
            pal(0xFF39FF88, 0xFF174B30, 0xFF00D9FF, 0xFF080F0C, 0xFF111B15, 0xFF1C2B21, 0xFFE5FFEF, 0xFF96BBA4, 0xFF2D4738),
        ),
        ThemePreset(
            "ultraviolet", "Ultraviolet", "Night Club",
            pal(0xFF9D72FF, 0xFF3B2A66, 0xFFFF5EDB, 0xFF100B1B, 0xFF1C132B, 0xFF2B1D40, 0xFFF4ECFF, 0xFFB6A4CE, 0xFF43315D),
        ),
        ThemePreset(
            "sage", "Sage", "Calm",
            pal(0xFFA8C6A0, 0xFF354D36, 0xFFD7C7A1, 0xFF131713, 0xFF1E241D, 0xFF2B3429, 0xFFEAF0E5, 0xFFADB9A5, 0xFF465243),
        ),
        ThemePreset(
            "pine_forest", "Pine Forest", "Evergreen",
            pal(0xFF72C69A, 0xFF244B36, 0xFFB5D69B, 0xFF0B130F, 0xFF142019, 0xFF203127, 0xFFE1F2E7, 0xFF96B3A0, 0xFF30483A),
        ),
        ThemePreset(
            "terracotta", "Terracotta", "Clay",
            pal(0xFFE89576, 0xFF593528, 0xFFE8C49A, 0xFF18110F, 0xFF251B17, 0xFF352720, 0xFFF8E9DF, 0xFFC1A89A, 0xFF514034),
        ),
        ThemePreset(
            "desert_sand", "Desert", "Sandstone",
            pal(0xFFE5BD83, 0xFF57452D, 0xFFD5A88A, 0xFF17130F, 0xFF241E17, 0xFF352B20, 0xFFF5EBDD, 0xFFC1B29C, 0xFF514535),
        ),
        ThemePreset(
            "glacier", "Glacier", "Frozen Lake",
            pal(0xFF8DE1D5, 0xFF28514E, 0xFF9EC7FF, 0xFF0B1519, 0xFF142329, 0xFF20353C, 0xFFE6F7F5, 0xFFA1C1BF, 0xFF35545A),
        )
    )

    private val builtInById = builtIn.associateBy { it.id }

    val custom = mutableStateListOf<ThemePreset>()

    val all: List<ThemePreset> get() = builtIn + custom

    val byId: Map<String, ThemePreset>
        get() = if (custom.isEmpty()) builtInById else builtInById + custom.associateBy { it.id }

    fun resolve(id: String): ThemePalette =
        (byId[id] ?: builtInById.getValue(DEFAULT_ID)).palette

    fun find(id: String): ThemePreset? = byId[id]

    fun isCustom(id: String): Boolean = custom.any { it.id == id }
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