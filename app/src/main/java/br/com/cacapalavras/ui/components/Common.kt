package br.com.cacapalavras.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cacapalavras.domain.Progression
import br.com.cacapalavras.ui.theme.Gold
import br.com.cacapalavras.ui.theme.LocalPalette

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(22.dp),
        color = color,
        shadowElevation = 3.dp,
        content = content,
    )
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, bg: Color = MaterialTheme.colorScheme.surface, fg: Color = MaterialTheme.colorScheme.onSurface) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, color = fg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        trailing()
        Spacer(Modifier.width(8.dp))
    }
}

/** Barra de progresso arredondada com gradiente do tema. */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, height: Dp = 12.dp, animate: Boolean = true, color: Color? = null) {
    val palette = LocalPalette.current
    val f by animateFloatAsState(if (animate) fraction else fraction, tween(if (animate) 600 else 0), label = "bar")
    val shown = if (animate) f else fraction
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(palette.muted.copy(alpha = 0.22f)),
    ) {
        if (shown > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(shown.coerceIn(0.03f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (color != null) Brush.horizontalGradient(listOf(color, color))
                        else Brush.horizontalGradient(listOf(palette.accent, palette.accent.copy(alpha = 0.7f), Gold)),
                    ),
            )
        }
    }
}

/** Selo circular com o nível do jogador. */
@Composable
fun LevelBadge(level: Int, size: Dp = 56.dp) {
    val palette = LocalPalette.current
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(palette.accent, Gold))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NÍV", fontSize = (size.value * 0.16f).sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = (size.value * 0.18f).sp)
            Text("$level", fontSize = (size.value * 0.38f).sp, color = Color.White, fontWeight = FontWeight.ExtraBold, lineHeight = (size.value * 0.40f).sp)
        }
    }
}

/** Cartão do jogador com nível, título e barra de XP (US2 critério 5). */
@Composable
fun PlayerXpBlock(totalXp: Long, modifier: Modifier = Modifier, animate: Boolean = true) {
    val info = Progression.levelInfo(totalXp)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LevelBadge(info.level)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(Progression.title(info.level), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            ProgressBar(info.fraction, Modifier.fillMaxWidth(), animate = animate)
            Spacer(Modifier.height(4.dp))
            Text(
                "${info.xpIntoLevel} / ${info.xpForNext} XP para o nível ${info.level + 1}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun Stars(count: Int, max: Int = 3, size: Int = 16) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(max) { i ->
            Text(
                if (i < count) "★" else "☆", fontSize = size.sp,
                color = if (i < count) Gold else LocalPalette.current.muted.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

fun formatTime(ms: Long): String {
    val s = (ms / 1000).toInt()
    return "%d:%02d".format(s / 60, s % 60)
}
