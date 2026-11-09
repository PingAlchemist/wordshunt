package br.com.cacapalavras.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import br.com.cacapalavras.domain.Cell
import br.com.cacapalavras.domain.FoundWord
import br.com.cacapalavras.domain.Puzzle
import br.com.cacapalavras.ui.theme.BoardPalette
import br.com.cacapalavras.ui.theme.Gold

/**
 * Tabuleiro desenhado num único Canvas (NFR-002). Destaques são linhas grossas com ponta redonda.
 * Gesto próprio para capturar a célula exata do primeiro toque.
 */
@Composable
fun WordGrid(
    puzzle: Puzzle,
    found: List<FoundWord>,
    selection: List<Cell>,
    anchor: Cell?,
    palette: BoardPalette,
    onStart: (Cell) -> Unit,
    onMove: (Cell) -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val n = puzzle.size
    val measurer = rememberTextMeasurer(cacheSize = 64)
    val start by rememberUpdatedState(onStart)
    val move by rememberUpdatedState(onMove)
    val end by rememberUpdatedState(onEnd)

    // Anima a palavra encontrada mais recente "crescendo" do início ao fim.
    val grow = remember(puzzle) { Animatable(1f) }
    LaunchedEffect(found.size) {
        if (found.isNotEmpty()) {
            grow.snapTo(0f)
            grow.animateTo(1f, tween(260))
        }
    }
    val foundCells = remember(found) { found.flatMap { it.cells }.toSet() }

    Canvas(
        modifier
            .aspectRatio(1f)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .pointerInput(n) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val cellPx = size.width.toFloat() / n
                    fun cellAt(o: Offset) = Cell(
                        (o.y / cellPx).toInt().coerceIn(0, n - 1),
                        (o.x / cellPx).toInt().coerceIn(0, n - 1),
                    )
                    var last = cellAt(down.position)
                    start(last)
                    down.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        // Processa a posição mesmo no evento de soltar: em gestos rápidos o último
                        // movimento e o "up" chegam juntos e a célula final se perderia.
                        val c = cellAt(change.position)
                        if (c != last) {
                            last = c
                            move(c)
                        }
                        change.consume()
                        if (!change.pressed) break
                    }
                    end()
                }
            },
    ) {
        val cell = size.width / n
        fun center(c: Cell) = Offset((c.col + 0.5f) * cell, (c.row + 0.5f) * cell)
        val stroke = cell * 0.78f

        drawRect(palette.grid)

        // Palavras encontradas
        found.forEachIndexed { i, fw ->
            val color = if (fw.word.secret) Gold else palette.highlight(fw.colorIndex)
            val a = center(fw.cells.first())
            val b = center(fw.cells.last())
            val endPt = if (i == found.lastIndex) lerp(a, b, grow.value) else b
            drawLine(color.copy(alpha = if (palette.dark) 0.55f else 0.45f), a, endPt, stroke, StrokeCap.Round)
        }

        // Âncora do modo dois toques
        anchor?.let {
            drawCircle(palette.accent.copy(alpha = 0.30f), radius = stroke / 2, center = center(it))
        }

        // Seleção atual
        if (selection.isNotEmpty()) {
            drawLine(
                palette.accent.copy(alpha = 0.38f), center(selection.first()), center(selection.last()),
                stroke, StrokeCap.Round,
            )
        }

        // Letras
        val selected = selection.toSet()
        val style = TextStyle(fontSize = (cell * 0.50f).toSp(), fontWeight = FontWeight.Bold)
        for (r in 0 until n) for (c in 0 until n) {
            val here = Cell(r, c)
            val layout = measurer.measure(puzzle.grid[r][c].toString(), style)
            val color = when {
                here in selected -> palette.accent
                here in foundCells -> palette.text
                else -> palette.text.copy(alpha = 0.86f)
            }
            drawText(
                layout, color = color,
                topLeft = Offset(
                    (c + 0.5f) * cell - layout.size.width / 2f,
                    (r + 0.5f) * cell - layout.size.height / 2f,
                ),
            )
        }

        // Moldura suave
        drawRoundRect(
            Color.Black.copy(alpha = 0.05f), style = Stroke(2f),
            cornerRadius = CornerRadius(20.dp.toPx()),
        )
    }
}
