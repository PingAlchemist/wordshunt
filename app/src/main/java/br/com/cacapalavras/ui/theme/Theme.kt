package br.com.cacapalavras.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Tema visual desbloqueável (US9). As cores de destaque são aplicadas com transparência na grade. */
data class BoardPalette(
    val id: String,
    val dark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val card: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val grid: Color,
    val highlights: List<Color>,
) {
    val background: Brush get() = Brush.verticalGradient(listOf(bgTop, bgBottom))
    fun highlight(index: Int) = highlights[index % highlights.size]
}

val Gold = Color(0xFFF5B700)
val GoldDeep = Color(0xFFB98600)

private val vivid = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFB347), Color(0xFF6C8CFF),
    Color(0xFF9B5DE5), Color(0xFF2EC4B6), Color(0xFFF15BB5), Color(0xFF8AC926),
)

object Palettes {
    val all = listOf(
        BoardPalette(
            "classic", false, Color(0xFFF6F1E7), Color(0xFFEADFCB), Color(0xFFFFFFFF), Color(0xFF2B2D42),
            Color(0xFF7A7D92), Color(0xFF3D5AFE), Color.White, Color(0xFFFFFDF8), vivid,
        ),
        BoardPalette(
            "ocean", false, Color(0xFFE3F6FF), Color(0xFFB9E2F5), Color(0xFFF7FCFF), Color(0xFF0B3954),
            Color(0xFF5B7F95), Color(0xFF087E8B), Color.White, Color(0xFFFFFFFF),
            listOf(Color(0xFF00B4D8), Color(0xFFFF7F51), Color(0xFF52B788), Color(0xFF7B2CBF), Color(0xFFFFB703), Color(0xFFEF476F)),
        ),
        BoardPalette(
            "forest", false, Color(0xFFEAF4E4), Color(0xFFCFE4C3), Color(0xFFFBFDF8), Color(0xFF23331F),
            Color(0xFF66765F), Color(0xFF2E7D32), Color.White, Color(0xFFFFFFFB),
            listOf(Color(0xFF6A994E), Color(0xFFBC4749), Color(0xFFF2CC8F), Color(0xFF3D85C6), Color(0xFFE07A5F), Color(0xFF81B29A)),
        ),
        BoardPalette(
            "night", true, Color(0xFF12162B), Color(0xFF1F2550), Color(0xFF242A4D), Color(0xFFE8EAF6),
            Color(0xFF9AA0C3), Color(0xFF8C9EFF), Color(0xFF12162B), Color(0xFF1B2040),
            listOf(Color(0xFF7DF9FF), Color(0xFFFF77E9), Color(0xFFFFD166), Color(0xFF06D6A0), Color(0xFFB388FF), Color(0xFFFF8A65)),
        ),
        BoardPalette(
            "sunset", false, Color(0xFFFFE6D5), Color(0xFFFFBFA3), Color(0xFFFFF8F3), Color(0xFF4A2C2A),
            Color(0xFF8D6B66), Color(0xFFE85D04), Color.White, Color(0xFFFFFCFA),
            listOf(Color(0xFFF48C06), Color(0xFF9D4EDD), Color(0xFFE5383B), Color(0xFF4CC9F0), Color(0xFFF72585), Color(0xFF80B918)),
        ),
        BoardPalette(
            "gold", true, Color(0xFF2A2116), Color(0xFF3F301C), Color(0xFF3A2F20), Color(0xFFFFF3D6),
            Color(0xFFC9B48A), Color(0xFFFFC93C), Color(0xFF2A2116), Color(0xFF30271A),
            listOf(Color(0xFFFFC93C), Color(0xFFE76F51), Color(0xFF2A9D8F), Color(0xFFE9C46A), Color(0xFFF4A261), Color(0xFF8AB17D)),
        ),
    )

    fun byId(id: String) = all.firstOrNull { it.id == id } ?: all.first()
}

val LocalPalette = staticCompositionLocalOf { Palettes.all.first() }

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp),
)

@Composable
fun CacaTheme(palette: BoardPalette, content: @Composable () -> Unit) {
    val scheme = if (palette.dark) {
        darkColorScheme(
            primary = palette.accent, onPrimary = palette.onAccent, background = palette.bgTop,
            surface = palette.card, onSurface = palette.text, onBackground = palette.text,
            surfaceVariant = palette.card, onSurfaceVariant = palette.muted, secondary = Gold,
        )
    } else {
        lightColorScheme(
            primary = palette.accent, onPrimary = palette.onAccent, background = palette.bgTop,
            surface = palette.card, onSurface = palette.text, onBackground = palette.text,
            surfaceVariant = palette.card, onSurfaceVariant = palette.muted, secondary = GoldDeep,
        )
    }
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
    }
}
