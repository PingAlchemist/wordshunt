package br.com.cacapalavras.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private class Piece(val x: Float, val delay: Float, val speed: Float, val drift: Float, val spin: Float, val color: Color, val w: Float)

/** Chuva de confete desenhada em Canvas; dura ~3,5 s. */
@Composable
fun Confetti(modifier: Modifier = Modifier, colors: List<Color>, count: Int = 140) {
    val pieces = remember {
        val r = Random(System.nanoTime())
        List(count) {
            Piece(
                x = r.nextFloat(), delay = r.nextFloat() * 0.9f, speed = 0.35f + r.nextFloat() * 0.45f,
                drift = (r.nextFloat() - 0.5f) * 0.25f, spin = r.nextFloat() * 720f - 360f,
                color = colors[r.nextInt(colors.size)], w = 6f + r.nextFloat() * 8f,
            )
        }
    }
    var t by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (t < 3_500_000_000L) {
            withFrameNanos { t = it - start }
        }
    }
    Canvas(modifier) {
        val sec = t / 1e9f
        pieces.forEach { p ->
            val life = sec - p.delay
            if (life < 0f) return@forEach
            val y = -0.05f + life * p.speed
            if (y > 1.1f) return@forEach
            val x = p.x + p.drift * life + sin(life * 4f + p.x * 10f) * 0.02f
            val center = Offset(x * size.width, y * size.height)
            rotate(p.spin * life, center) {
                drawRect(p.color, topLeft = center - Offset(p.w / 2, p.w / 4), size = Size(p.w, p.w / 2))
            }
        }
    }
}
