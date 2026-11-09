package br.com.cacapalavras.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import br.com.cacapalavras.domain.CosmeticItem
import br.com.cacapalavras.domain.Slot
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Espaço virtual do personagem: 200 × 270 unidades. */
const val AVATAR_RATIO = 200f / 270f

/**
 * Personagem do jogador em estilo comic/cartoon (004-US19): proporções de herói, contorno de tinta,
 * sombreado em dois tons, olhos com íris e brilho, piscar periódico.
 * Cada slot é uma função isolada → dá para trocar por arte em camadas (PNG) no futuro sem mexer no resto.
 */
@Composable
fun Avatar(
    look: Map<Slot, CosmeticItem>,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    showBackground: Boolean = true,
) {
    val measurer = rememberTextMeasurer(cacheSize = 16)
    if (animate) {
        val t by rememberInfiniteTransition(label = "avatar").animateFloat(
            0f, 1f, infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "t",
        )
        AvatarCanvas(look, modifier, { t }, true, showBackground, measurer)
    } else {
        AvatarCanvas(look, modifier, { 0.3f }, false, showBackground, measurer)
    }
}

@Composable
private fun AvatarCanvas(
    look: Map<Slot, CosmeticItem>,
    modifier: Modifier,
    time: () -> Float,
    animated: Boolean,
    showBackground: Boolean,
    measurer: TextMeasurer,
) {
    Canvas(modifier.aspectRatio(AVATAR_RATIO)) {
        val s = size.width / 200f
        val t = time()
        withTransform({ scale(s, s, Offset.Zero) }) {
            ComicPainter(this, look, t, animated, measurer).draw(showBackground)
        }
    }
}

// ============================================================================================

private val INK = Color(0xFF221D2B)
private fun c(v: Long) = Color(v)
private fun Color.darker(f: Float = 0.78f) = Color(red * f, green * f, blue * f, alpha)
private fun Color.lighter(f: Float = 0.3f) = lerp(this, Color.White, f)

private class ComicPainter(
    val d: DrawScope,
    val look: Map<Slot, CosmeticItem>,
    val t: Float,
    val animated: Boolean,
    val measurer: TextMeasurer,
) {
    val skin = c(look[Slot.SKIN]?.colors?.firstOrNull() ?: 0xFFF1C27D)
    val skinShade = skin.darker(0.84f)
    val hairItem = look[Slot.HAIR]
    val hairColor = c(hairItem?.colors?.firstOrNull() ?: 0xFF6B3E26)
    val outfit = look[Slot.OUTFIT]
    val ov = outfit?.variant ?: "TSHIRT"
    val o0 = c(outfit?.colors?.getOrNull(0) ?: 0xFF3D7BFD)
    val o1 = c(outfit?.colors?.getOrNull(1) ?: 0xFF2A3A5C)
    val anim = look[Slot.ANIMATION]?.variant ?: "BREATHE"
    val wave = 2 * PI.toFloat() * t
    val blink = animated && (t % 0.5f) in 0.455f..0.485f

    // Esqueleto
    val shL = Offset(70f, 116f); val shR = Offset(130f, 116f)  // cotovelos levemente abertos = pose mais natural
    val elL = Offset(60f, 146f); val wrL = Offset(65f, 175f)
    val elR: Offset; val wrR: Offset
    val hipL = Offset(89f, 170f); val kneeL = Offset(87f, 206f); val ankL = Offset(86f, 236f)
    val hipR = Offset(111f, 170f); val kneeR = Offset(113f, 206f); val ankR = Offset(114f, 236f)
    val eyeL = Offset(89f, 67f); val eyeR = Offset(111f, 67f)

    init {
        if (anim == "WAVE") {
            val a = sin(wave * 3) * 0.35f
            elR = Offset(150f, 100f)
            wrR = Offset(156f + 14f * sin(a), 76f + 4f * cos(a))
        } else {
            elR = Offset(140f, 146f); wrR = Offset(135f, 175f)
        }
    }

    fun draw(showBackground: Boolean) = with(d) {
        if (showBackground) background(look[Slot.BACKGROUND]?.variant ?: "SKY")
        if (anim == "STAR_RAIN") starRain()

        val lift = when (anim) {
            "BOUNCE" -> abs(sin(wave * 2)) * 16f
            "FLOAT" -> 10f + 7f * sin(wave)
            else -> 0f
        }
        val sw = 112f - lift * 2.2f
        drawOval(
            Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.32f), Color.Transparent), Offset(100f, 253f), sw / 2),
            Offset(100f - sw / 2, 246f), Size(sw, 14f),
        )

        withTransform({
            translate(0f, -lift)
            if (anim == "BREATHE") scale(1f, 1f + 0.012f * sin(wave * 2), Offset(100f, 252f))
        }) {
            backLayers()
            legs()
            if (ov == "ROBE") robeSkirt()
            shoes()
            torso()
            straps()
            arms()
            neckAndHead()
            face()
            hairFront()
            glasses()
            hat()
        }
        if (anim == "MAGNIFIER") magnifier()
        if (anim == "AURA") aura()
    }

    // ------------------------------------------------------------------ utilitários de traço

    fun DrawScope.inked(path: Path, fill: Brush, w: Float = 2.2f) {
        drawPath(path, fill)
        drawPath(path, INK, style = Stroke(w, join = StrokeJoin.Round, cap = StrokeCap.Round))
    }

    fun DrawScope.inked(path: Path, fill: Color, w: Float = 2.2f) = inked(path, SolidColor(fill), w)

    fun DrawScope.inkOval(topLeft: Offset, size: Size, fill: Brush, w: Float = 2.2f) {
        drawOval(fill, topLeft, size)
        drawOval(INK, topLeft, size, style = Stroke(w))
    }

    fun DrawScope.inkCircle(center: Offset, r: Float, fill: Brush, w: Float = 2.2f) {
        drawCircle(fill, r, center)
        drawCircle(INK, r, center, style = Stroke(w))
    }

    /** Gradiente de volume: luz no alto à esquerda, sombra embaixo à direita. */
    fun vol(base: Color, from: Offset, to: Offset) =
        Brush.linearGradient(listOf(base.lighter(0.28f), base, base.darker(0.8f)), from, to)

    /** Membro em dois segmentos com contorno contínuo e faixa de sombra. */
    fun DrawScope.limb(a: Offset, b: Offset, c2: Offset, w1: Float, w2: Float, col1: Color, col2: Color) {
        drawLine(INK, a, b, w1 + 4.4f, StrokeCap.Round)
        drawLine(INK, b, c2, w2 + 4.4f, StrokeCap.Round)
        drawLine(col1, a, b, w1, StrokeCap.Round)
        drawLine(col2, b, c2, w2, StrokeCap.Round)
        val off = Offset(w1 * 0.24f, 0f)
        drawLine(col1.darker(0.82f), a + off, b + off, w1 * 0.42f, StrokeCap.Round)
        drawLine(col2.darker(0.82f), b + Offset(w2 * 0.24f, 0f), c2 + Offset(w2 * 0.24f, 0f), w2 * 0.42f, StrokeCap.Round)
        drawLine(col1.lighter(0.35f).copy(alpha = 0.7f), a - Offset(w1 * 0.22f, 0f), b - Offset(w1 * 0.22f, 0f), w1 * 0.16f, StrokeCap.Round)
    }

    fun lock(b1: Offset, b2: Offset, tip: Offset, bend: Float = 0f): Path = Path().apply {
        val mid = Offset((b1.x + b2.x) / 2, (b1.y + b2.y) / 2)
        val ctl1 = Offset((b1.x + tip.x) / 2 + bend, (b1.y + tip.y) / 2)
        val ctl2 = Offset((b2.x + tip.x) / 2 + bend, (b2.y + tip.y) / 2)
        moveTo(b1.x, b1.y); quadraticTo(ctl1.x, ctl1.y, tip.x, tip.y); quadraticTo(ctl2.x, ctl2.y, b2.x, b2.y)
        quadraticTo(mid.x, mid.y - 2f, b1.x, b1.y); close()
    }

    fun DrawScope.star(center: Offset, r: Float, color: Color, outline: Boolean = false) {
        val p = Path()
        for (k in 0 until 10) {
            val rad = if (k % 2 == 0) r else r * 0.45f
            val a = -PI.toFloat() / 2 + k * PI.toFloat() / 5
            val pt = Offset(center.x + rad * cos(a), center.y + rad * sin(a))
            if (k == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
        }
        p.close()
        if (outline) inked(p, color, 1.4f) else drawPath(p, color)
    }

    // ------------------------------------------------------------------ cenários

    fun DrawScope.background(v: String) {
        val full = Size(200f, 270f)
        when (v) {
            "FOREST" -> {
                drawRect(Brush.verticalGradient(listOf(c(0xFF9ED8B8), c(0xFFE3F4E3))), size = full)
                listOf(Triple(20f, 0.8f, 0xFF4E8F5B), Triple(178f, 1f, 0xFF3D7A4A), Triple(150f, 0.65f, 0xFF5FA36C), Triple(46f, 0.6f, 0xFF5FA36C)).forEach { (x, k, col) ->
                    pine(x, 228f, k, c(col))
                }
                drawRect(Brush.verticalGradient(listOf(c(0xFF7CB342), c(0xFF558B2F)), 226f, 270f), Offset(0f, 226f), Size(200f, 44f))
            }
            "NIGHT" -> {
                drawRect(Brush.verticalGradient(listOf(c(0xFF0B1636), c(0xFF2B3F7E))), size = full)
                twinkles(22, Color.White)
                drawCircle(Brush.radialGradient(listOf(c(0xFFFFF6D5).copy(alpha = 0.55f), Color.Transparent), Offset(158f, 46f), 40f), 40f, Offset(158f, 46f))
                drawCircle(c(0xFFFFF3C4), 15f, Offset(158f, 46f))
                drawCircle(c(0xFFE8D9A0), 3f, Offset(153f, 42f)); drawCircle(c(0xFFE8D9A0), 2f, Offset(162f, 52f))
                drawRect(Brush.verticalGradient(listOf(c(0xFF22325E), c(0xFF141F3D)), 228f, 270f), Offset(0f, 228f), Size(200f, 42f))
            }
            "BEACH" -> {
                drawRect(Brush.verticalGradient(listOf(c(0xFF6EC6F5), c(0xFFDDF4FF))), size = full)
                drawCircle(Brush.radialGradient(listOf(c(0xFFFFF59D), c(0xFFFFD54F).copy(alpha = 0f)), Offset(42f, 48f), 34f), 34f, Offset(42f, 48f))
                drawCircle(c(0xFFFFE082), 14f, Offset(42f, 48f))
                drawRect(Brush.verticalGradient(listOf(c(0xFF29B6F6), c(0xFF0288D1)), 176f, 214f), Offset(0f, 176f), Size(200f, 38f))
                repeat(5) { i -> drawLine(Color.White.copy(alpha = 0.6f), Offset(10f + i * 42f, 186f + (i % 2) * 10f), Offset(30f + i * 42f, 186f + (i % 2) * 10f), 2f, StrokeCap.Round) }
                drawRect(Brush.verticalGradient(listOf(c(0xFFF8DFB0), c(0xFFE9C489)), 212f, 270f), Offset(0f, 212f), Size(200f, 58f))
            }
            "SUNSET" -> {
                drawRect(Brush.verticalGradient(listOf(c(0xFF3B1E5A), c(0xFFE0436A), c(0xFFFF9A4D), c(0xFFFFD58A))), size = full)
                drawCircle(Brush.radialGradient(listOf(c(0xFFFFF1B8), c(0xFFFFB74D).copy(alpha = 0f)), Offset(100f, 196f), 80f), 80f, Offset(100f, 196f))
                val hills = Path().apply {
                    moveTo(0f, 214f); quadraticTo(52f, 182f, 104f, 216f); quadraticTo(152f, 190f, 200f, 210f)
                    lineTo(200f, 270f); lineTo(0f, 270f); close()
                }
                drawPath(hills, c(0xFF4A1F4F))
            }
            "LIBRARY" -> {
                drawRect(Brush.verticalGradient(listOf(c(0xFF6D4C41), c(0xFF8D6E63))), size = full)
                val books = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFDD835, 0xFF8E24AA, 0xFFFF7043, 0xFF00897B)
                listOf(70f, 146f).forEach { y ->
                    var x = 4f; var i = (y / 7).toInt()
                    while (x < 196f) {
                        val w = 8f + (i * 7 % 5) * 2f
                        val h = 34f + (i * 13 % 4) * 5f
                        val col = c(books[i % books.size])
                        drawRect(col, Offset(x, y - h), Size(w, h))
                        drawRect(col.darker(0.7f), Offset(x, y - h + 6f), Size(w, 2f))
                        drawLine(INK.copy(alpha = 0.5f), Offset(x, y - h), Offset(x, y), 0.8f)
                        x += w + 1.2f; i++
                    }
                    drawRect(c(0xFF3E2723), Offset(0f, y), Size(200f, 8f))
                }
                drawRect(Brush.verticalGradient(listOf(c(0xFF5D4037), c(0xFF3E2723)), 226f, 270f), Offset(0f, 226f), Size(200f, 44f))
            }
            "GALAXY" -> {
                drawRect(Brush.radialGradient(listOf(c(0xFF5B2A9E), c(0xFF1A0B3D), c(0xFF07061A)), Offset(110f, 110f), 200f), size = full)
                drawOval(Brush.radialGradient(listOf(c(0xFFFF6FD8).copy(alpha = 0.35f), Color.Transparent), Offset(60f, 150f), 70f), Offset(-10f, 110f), Size(140f, 80f))
                twinkles(30, Color.White)
                inkCircle(Offset(40f, 52f), 15f, vol(c(0xFFFF8A65), Offset(28f, 40f), Offset(52f, 64f)), 1.6f)
                drawOval(c(0xFFFFE0B2), Offset(16f, 47f), Size(48f, 10f), style = Stroke(2.5f))
                inkCircle(Offset(168f, 32f), 7f, vol(c(0xFF80DEEA), Offset(161f, 25f), Offset(175f, 39f)), 1.2f)
            }
            else -> { // SKY
                drawRect(Brush.verticalGradient(listOf(c(0xFF5BB6F5), c(0xFFBEE6FF), c(0xFFEAF7FF))), size = full)
                cloud(40f, 46f, 1f); cloud(156f, 30f, 0.8f); cloud(170f, 120f, 0.6f)
                drawOval(Brush.verticalGradient(listOf(c(0xFF9CCC65), c(0xFF7CB342)), 214f, 280f), Offset(-40f, 214f), Size(280f, 90f))
                drawOval(c(0xFFAED581).copy(alpha = 0.8f), Offset(120f, 206f), Size(130f, 40f))
            }
        }
        // vinheta suave
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)), Offset(100f, 130f), 190f), size = full)
    }

    fun DrawScope.pine(x: Float, base: Float, k: Float, col: Color) {
        drawRect(c(0xFF5D4037), Offset(x - 3f * k, base - 22f * k), Size(6f * k, 22f * k))
        for (i in 0..2) {
            val top = base - (100f - i * 26f) * k
            val w = (24f + i * 8f) * k
            val p = Path().apply { moveTo(x, top); lineTo(x - w, top + 46f * k); lineTo(x + w, top + 46f * k); close() }
            drawPath(p, col.darker(1f - i * 0.06f))
        }
    }

    fun DrawScope.cloud(x: Float, y: Float, k: Float) {
        val base = Color.White
        val shade = c(0xFFD6E9F8)
        drawCircle(shade, 13f * k, Offset(x - 14f * k, y + 6f * k)); drawCircle(shade, 13f * k, Offset(x + 15f * k, y + 6f * k))
        drawCircle(base, 15f * k, Offset(x, y)); drawCircle(base, 11f * k, Offset(x - 15f * k, y + 3f * k)); drawCircle(base, 11f * k, Offset(x + 16f * k, y + 3f * k))
    }

    fun DrawScope.twinkles(n: Int, col: Color) {
        repeat(n) { i ->
            val x = (i * 73 % 197).toFloat() + 2f
            val y = (i * 37 % 170).toFloat() + 6f
            val tw = 0.4f + 0.6f * abs(sin(wave + i))
            if (i % 5 == 0) star(Offset(x, y), 3f, col.copy(alpha = tw)) else drawCircle(col.copy(alpha = tw), 1.1f, Offset(x, y))
        }
    }

    // ------------------------------------------------------------------ camadas de trás

    fun DrawScope.backLayers() {
        if (ov == "CAPE") {
            val sway = 4f * sin(wave)
            val cape = Path().apply {
                moveTo(72f, 112f); lineTo(128f, 112f)
                cubicTo(144f, 150f, 150f, 200f, 158f + sway, 234f)
                quadraticTo(130f, 228f, 112f, 238f); quadraticTo(96f, 228f, 82f, 238f); quadraticTo(62f, 228f, 42f + sway, 234f)
                cubicTo(50f, 200f, 56f, 150f, 72f, 112f); close()
            }
            inked(cape, vol(o0, Offset(60f, 110f), Offset(150f, 230f)))
            drawLine(o0.darker(0.65f), Offset(100f, 130f), Offset(100f, 226f), 2f)
        }
        if (hairItem?.variant == "LONG") {
            val back = Path().apply {
                moveTo(70f, 50f); cubicTo(58f, 90f, 60f, 120f, 62f, 140f)
                quadraticTo(100f, 150f, 138f, 140f); cubicTo(140f, 120f, 142f, 90f, 130f, 50f); close()
            }
            inked(back, hairBrush())
        }
        if (ov == "HOODIE") {
            val hood = Path().apply {
                moveTo(74f, 112f); cubicTo(70f, 96f, 80f, 92f, 100f, 94f); cubicTo(120f, 92f, 130f, 96f, 126f, 112f); close()
            }
            inked(hood, o0.darker(0.82f))
        }
        val bp = look[Slot.BACKPACK] ?: return
        val b0 = c(bp.colors[0]); val b1 = c(bp.colors.getOrElse(1) { bp.colors[0] })
        when (bp.variant) {
            "JETPACK" -> listOf(52f, 132f).forEach { x ->
                val cyl = Path().apply { addRoundRect(RoundRect(Rect(x, 102f, x + 16f, 162f), CornerRadius(8f))) }
                inked(cyl, Brush.horizontalGradient(listOf(b0.lighter(0.4f), b0, b0.darker(0.6f)), x, x + 16f))
                drawLine(INK.copy(alpha = 0.4f), Offset(x + 2f, 118f), Offset(x + 14f, 118f), 1.2f)
                val noz = Path().apply { moveTo(x + 3f, 162f); lineTo(x + 13f, 162f); lineTo(x + 15f, 170f); lineTo(x + 1f, 170f); close() }
                inked(noz, c(0xFF546E7A), 1.6f)
                flame(x + 8f, 172f, b1)
            }
            "ROCKET" -> {
                val body = Path().apply { addRoundRect(RoundRect(Rect(66f, 98f, 134f, 170f), CornerRadius(24f))) }
                inked(body, vol(b0, Offset(66f, 98f), Offset(134f, 170f)))
                listOf(66f to -12f, 134f to 12f).forEach { (x, dx) ->
                    val fin = Path().apply { moveTo(x, 146f); lineTo(x + dx, 172f); lineTo(x, 168f); close() }
                    inked(fin, b1, 1.6f)
                }
                flame(78f, 172f, c(0xFFFF7043)); flame(122f, 172f, c(0xFFFF7043))
            }
            "BOOKS" -> {
                val bag = Path().apply { addRoundRect(RoundRect(Rect(68f, 104f, 132f, 168f), CornerRadius(16f))) }
                inked(bag, vol(b0, Offset(68f, 104f), Offset(132f, 168f)))
                listOf(Triple(120f, 84f, b1), Triple(132f, 90f, c(0xFFFDD835)), Triple(108f, 88f, c(0xFF43A047))).forEach { (x, y, col) ->
                    val book = Path().apply { addRect(Rect(x, y, x + 10f, y + 30f)) }
                    inked(book, col, 1.6f)
                    drawLine(Color.White.copy(alpha = 0.7f), Offset(x + 2f, y + 6f), Offset(x + 8f, y + 6f), 1.5f)
                }
            }
            else -> {
                val bag = Path().apply { addRoundRect(RoundRect(Rect(68f, 102f, 132f, 168f), CornerRadius(18f))) }
                inked(bag, vol(b0, Offset(68f, 102f), Offset(132f, 168f)))
                val handle = Path().apply { moveTo(90f, 102f); quadraticTo(100f, 88f, 110f, 102f) }
                drawPath(handle, INK, style = Stroke(5f, cap = StrokeCap.Round)); drawPath(handle, b1, style = Stroke(2.6f, cap = StrokeCap.Round))
            }
        }
    }

    fun DrawScope.flame(x: Float, y: Float, color: Color) {
        val h = 14f + 6f * abs(sin(wave * 4))
        val p = Path().apply { moveTo(x - 7f, y); quadraticTo(x - 5f, y + h, x, y + h * 1.5f); quadraticTo(x + 5f, y + h, x + 7f, y); close() }
        drawPath(p, color)
        val p2 = Path().apply { moveTo(x - 3.5f, y); quadraticTo(x, y + h * 1.1f, x + 3.5f, y); close() }
        drawPath(p2, c(0xFFFFF176))
    }

    // ------------------------------------------------------------------ corpo

    fun pants(): Color = when (ov) {
        "SPACE" -> c(0xFFE8ECF1)
        "EXPLORER" -> o1
        "ROBE" -> c(0xFF3A2F5B)
        else -> c(0xFF2E3B5E)
    }

    fun DrawScope.legs() {
        val p = pants()
        // quadril
        val hips = Path().apply { moveTo(74f, 160f); lineTo(126f, 160f); lineTo(124f, 180f); lineTo(76f, 180f); close() }
        inked(hips, p)
        limb(hipL, kneeL, ankL, 19f, 16f, p, p)
        limb(hipR, kneeR, ankR, 19f, 16f, p, p)
        // costura e joelho
        drawLine(p.darker(0.7f), Offset(100f, 166f), Offset(100f, 182f), 1.4f)
        drawCircle(p.lighter(0.15f), 4f, kneeL - Offset(2f, 0f)); drawCircle(p.lighter(0.15f), 4f, kneeR - Offset(2f, 0f))
        if (ov == "SPACE") {
            listOf(kneeL, kneeR).forEach {
                val pad = Path().apply { addRoundRect(RoundRect(Rect(it.x - 9f, it.y - 7f, it.x + 9f, it.y + 7f), CornerRadius(5f))) }
                inked(pad, c(0xFFB0BEC5), 1.6f)
            }
            drawLine(o1, Offset(81f, 214f), Offset(80f, 230f), 3f); drawLine(o1, Offset(119f, 214f), Offset(120f, 230f), 3f)
        }
    }

    fun DrawScope.shoes() {
        val sh = look[Slot.SHOES]
        val s0 = c(sh?.colors?.getOrNull(0) ?: 0xFFF5F5F5)
        val s1 = c(sh?.colors?.getOrNull(1) ?: 0xFF3D7BFD)
        listOf(ankL to -1f, ankR to 1f).forEach { (a, dir) ->
            if (sh?.variant == "BOOTS") {
                val shaft = Path().apply { addRoundRect(RoundRect(Rect(a.x - 11f, 212f, a.x + 11f, 240f), CornerRadius(4f))) }
                inked(shaft, vol(s0, Offset(a.x - 11f, 212f), Offset(a.x + 11f, 240f)))
                val cuff = Path().apply { addRoundRect(RoundRect(Rect(a.x - 12.5f, 210f, a.x + 12.5f, 217f), CornerRadius(3f))) }
                inked(cuff, s0.darker(0.85f), 1.8f)
                for (i in 0..2) {
                    val y = 222f + i * 6f
                    drawLine(INK.copy(alpha = 0.7f), Offset(a.x - 4f, y), Offset(a.x + 4f, y + 3f), 1.2f)
                    drawLine(INK.copy(alpha = 0.7f), Offset(a.x + 4f, y), Offset(a.x - 4f, y + 3f), 1.2f)
                }
            }
            val toe = a.x + dir * 8f
            val shoe = Path().apply {
                moveTo(a.x - 11f, 236f); lineTo(a.x + 11f, 236f)
                cubicTo(toe + dir * 10f + 4f, 238f, toe + dir * 12f, 246f, toe + dir * 8f, 250f)
                lineTo(a.x - dir * 12f, 250f)
                cubicTo(a.x - dir * 14f, 244f, a.x - 13f, 238f, a.x - 11f, 236f); close()
            }
            inked(shoe, vol(s0, Offset(a.x - 12f, 234f), Offset(a.x + 14f, 252f)))
            val sole = if (s0.red + s0.green + s0.blue > 2.4f) c(0xFFB0BEC5) else Color.White
            drawLine(INK, Offset(minOf(a.x - dir * 12f, toe + dir * 8f) + 1f, 249f), Offset(maxOf(a.x - dir * 12f, toe + dir * 8f) - 1f, 249f), 4.6f, StrokeCap.Round)
            drawLine(if (sh?.variant == "BOOTS") s1 else sole, Offset(minOf(a.x - dir * 12f, toe + dir * 8f) + 1f, 249f), Offset(maxOf(a.x - dir * 12f, toe + dir * 8f) - 1f, 249f), 2.6f, StrokeCap.Round)
            if (sh?.variant != "BOOTS") {
                drawLine(s1, Offset(a.x - 4f * dir, 244f), Offset(toe + dir * 2f, 240f), 2.4f, StrokeCap.Round)
                drawLine(INK.copy(alpha = 0.7f), Offset(a.x - 3f, 238.5f), Offset(a.x + 3f, 238.5f), 1.2f)
                drawLine(INK.copy(alpha = 0.7f), Offset(a.x - 3f, 241.5f), Offset(a.x + 3f, 241.5f), 1.2f)
            }
        }
    }

    fun DrawScope.robeSkirt() {
        val skirt = Path().apply {
            moveTo(76f, 150f); lineTo(124f, 150f); cubicTo(132f, 190f, 140f, 220f, 146f, 238f)
            quadraticTo(100f, 246f, 54f, 238f); cubicTo(60f, 220f, 68f, 190f, 76f, 150f); close()
        }
        inked(skirt, vol(o0, Offset(60f, 150f), Offset(146f, 240f)))
        val trim = Path().apply { moveTo(56f, 234f); quadraticTo(100f, 242f, 144f, 234f) }
        drawPath(trim, o1, style = Stroke(4f, cap = StrokeCap.Round))
        drawLine(o1, Offset(100f, 160f), Offset(100f, 240f), 3f)
        listOf(Offset(78f, 196f), Offset(122f, 210f), Offset(88f, 224f), Offset(118f, 178f)).forEach { star(it, 5f, o1) }
    }

    fun torsoPath() = Path().apply {
        moveTo(82f, 105f); lineTo(118f, 105f)
        cubicTo(128f, 106f, 135f, 110f, 135f, 120f)
        lineTo(131f, 150f); lineTo(127f, 168f); lineTo(73f, 168f); lineTo(69f, 150f); lineTo(65f, 120f)
        cubicTo(65f, 110f, 72f, 106f, 82f, 105f); close()
    }

    fun DrawScope.torso() {
        val base = when (ov) { "JACKET", "CAPE" -> o1; "EXPLORER" -> o0; else -> o0 }
        val tp = torsoPath()
        inked(tp, vol(base, Offset(66f, 104f), Offset(134f, 170f)))
        clipPath(tp) {
            drawRect(base.darker(0.8f).copy(alpha = 0.55f), Offset(116f, 100f), Size(30f, 80f))
            drawOval(base.lighter(0.4f).copy(alpha = 0.35f), Offset(72f, 110f), Size(24f, 30f))
        }
        when (ov) {
            "STRIPES" -> clipPath(tp) {
                for (y in 116..168 step 11) {
                    val p = Path().apply { moveTo(60f, y.toFloat()); quadraticTo(100f, y + 4f, 140f, y.toFloat()) }
                    drawPath(p, o1, style = Stroke(4.5f))
                }
                drawRect(Color.Black.copy(alpha = 0.12f), Offset(116f, 100f), Size(30f, 80f))
            }
            "HOODIE" -> {
                val pocket = Path().apply { moveTo(82f, 148f); lineTo(118f, 148f); lineTo(122f, 166f); lineTo(78f, 166f); close() }
                inked(pocket, o0.darker(0.9f), 1.8f)
                listOf(94f to 92f, 106f to 108f).forEach { (x1, x2) ->
                    drawLine(Color.White, Offset(x1, 108f), Offset(x2, 128f), 2f, StrokeCap.Round)
                    drawCircle(c(0xFFB0BEC5), 2f, Offset(x2, 129f))
                }
                drawRect(o0.darker(0.82f), Offset(73f, 163f), Size(54f, 5f))
            }
            "JACKET" -> {
                listOf(true, false).forEach { left ->
                    val panel = Path().apply {
                        if (left) { moveTo(82f, 105f); lineTo(96f, 105f); lineTo(92f, 130f); lineTo(97f, 168f); lineTo(73f, 168f); lineTo(69f, 150f); lineTo(65f, 120f); cubicTo(65f, 110f, 72f, 106f, 82f, 105f) }
                        else { moveTo(118f, 105f); lineTo(104f, 105f); lineTo(108f, 130f); lineTo(103f, 168f); lineTo(127f, 168f); lineTo(131f, 150f); lineTo(135f, 120f); cubicTo(135f, 110f, 128f, 106f, 118f, 105f) }
                        close()
                    }
                    inked(panel, vol(o0, Offset(66f, 104f), Offset(134f, 170f)))
                }
                val lapelL = Path().apply { moveTo(84f, 105f); lineTo(96f, 105f); lineTo(90f, 124f); close() }
                val lapelR = Path().apply { moveTo(116f, 105f); lineTo(104f, 105f); lineTo(110f, 124f); close() }
                inked(lapelL, o0.lighter(0.15f), 1.6f); inked(lapelR, o0.lighter(0.15f), 1.6f)
                drawLine(c(0xFFFFC93C), Offset(97f, 130f), Offset(97f, 166f), 1.4f)
                drawLine(c(0xFFFFC93C), Offset(103f, 130f), Offset(103f, 166f), 1.4f)
            }
            "EXPLORER" -> {
                val v = Path().apply { moveTo(92f, 105f); lineTo(108f, 105f); lineTo(100f, 126f); close() }
                inked(v, skin, 1.6f)
                for (i in 0..2) {
                    val y = 110f + i * 5f
                    drawLine(INK, Offset(95f + i, y), Offset(105f - i, y + 3f), 1.2f)
                    drawLine(INK, Offset(105f - i, y), Offset(95f + i, y + 3f), 1.2f)
                }
                beltAndBuckle()
                val pouch = Path().apply { addRoundRect(RoundRect(Rect(108f, 162f, 122f, 176f), CornerRadius(3f))) }
                inked(pouch, c(0xFF6D4C41), 1.6f)
            }
            "SPACE" -> {
                val ring = Path().apply { addOval(Rect(80f, 98f, 120f, 112f)) }
                inked(ring, Brush.horizontalGradient(listOf(c(0xFFECEFF1), c(0xFF90A4AE)), 80f, 120f), 1.8f)
                val box = Path().apply { addRoundRect(RoundRect(Rect(86f, 124f, 114f, 146f), CornerRadius(4f))) }
                inked(box, c(0xFFCFD8DC), 1.8f)
                drawCircle(o1, 3.2f, Offset(93f, 135f)); drawCircle(c(0xFF42A5F5), 3.2f, Offset(102f, 135f)); drawCircle(c(0xFF66BB6A), 2.4f, Offset(109f, 130f))
                drawRect(o1, Offset(70f, 154f), Size(60f, 5f))
                drawLine(INK.copy(alpha = 0.35f), Offset(100f, 146f), Offset(100f, 166f), 1.2f)
            }
            "ROBE" -> {
                val sash = Path().apply { moveTo(72f, 148f); lineTo(128f, 148f); lineTo(127f, 156f); lineTo(73f, 156f); close() }
                inked(sash, o1, 1.6f)
                val v = Path().apply { moveTo(90f, 105f); lineTo(110f, 105f); lineTo(100f, 130f); close() }
                inked(v, o1, 1.6f)
            }
            "CAPE" -> {
                star(Offset(100f, 134f), 11f, o0, outline = true)
                listOf(Offset(76f, 110f), Offset(124f, 110f)).forEach { inkCircle(it, 4.5f, SolidColor(c(0xFFFFC93C)), 1.4f) }
                beltAndBuckle()
            }
            else -> Unit
        }
        if (ov in setOf("TSHIRT", "STRIPES", "CAPE")) {
            // gola careca
            val collar = Path().apply { moveTo(88f, 105f); quadraticTo(100f, 116f, 112f, 105f) }
            drawPath(collar, INK, style = Stroke(5f, cap = StrokeCap.Round))
            drawPath(collar, base.darker(0.85f), style = Stroke(2.6f, cap = StrokeCap.Round))
            // dobras
            drawLine(base.darker(0.7f), Offset(76f, 128f), Offset(82f, 136f), 1.2f, StrokeCap.Round)
            drawLine(base.darker(0.7f), Offset(124f, 128f), Offset(118f, 136f), 1.2f, StrokeCap.Round)
        }
    }

    fun DrawScope.beltAndBuckle() {
        val belt = Path().apply { addRect(Rect(70f, 156f, 130f, 164f)) }
        inked(belt, c(0xFF5D4037), 1.6f)
        val buckle = Path().apply { addRoundRect(RoundRect(Rect(94f, 154f, 106f, 166f), CornerRadius(2f))) }
        inked(buckle, Brush.linearGradient(listOf(c(0xFFECEFF1), c(0xFF90A4AE)), Offset(94f, 154f), Offset(106f, 166f)), 1.6f)
        drawRect(c(0xFF5D4037), Offset(97.5f, 157.5f), Size(5f, 5f))
    }

    fun DrawScope.straps() {
        val bp = look[Slot.BACKPACK] ?: return
        if (bp.variant == "JETPACK") {
            drawLine(INK, Offset(76f, 108f), Offset(78f, 150f), 7.4f, StrokeCap.Round); drawLine(c(0xFF455A64), Offset(76f, 108f), Offset(78f, 150f), 4f, StrokeCap.Round)
            drawLine(INK, Offset(124f, 108f), Offset(122f, 150f), 7.4f, StrokeCap.Round); drawLine(c(0xFF455A64), Offset(124f, 108f), Offset(122f, 150f), 4f, StrokeCap.Round)
            return
        }
        val col = c(bp.colors.getOrElse(1) { bp.colors[0] }).darker(0.9f)
        listOf(Offset(80f, 106f) to Offset(80f, 154f), Offset(120f, 106f) to Offset(120f, 154f)).forEach { (a, b) ->
            drawLine(INK, a, b, 8.4f, StrokeCap.Round); drawLine(col, a, b, 5f, StrokeCap.Round)
            val bk = Path().apply { addRect(Rect(b.x - 4f, b.y - 14f, b.x + 4f, b.y - 9f)) }
            inked(bk, c(0xFFB0BEC5), 1.2f)
        }
    }

    fun DrawScope.arms() {
        val fullSleeve = ov in setOf("HOODIE", "JACKET", "SPACE", "ROBE")
        val sleeve = when (ov) { "CAPE" -> o1; "JACKET" -> o0; else -> o0 }
        val forearm = when {
            fullSleeve -> sleeve
            ov == "EXPLORER" -> c(0xFFE8DCC2) // manga longa clara por baixo da túnica
            else -> skin
        }
        val w1 = if (ov == "ROBE") 20f else 14f
        val w2 = if (ov == "ROBE") 17f else 12f
        if (!fullSleeve) {
            // Braço de pele (ou manga longa clara) + manga curta mais larga por cima, terminando antes do cotovelo.
            val upper = if (ov == "EXPLORER") forearm else skin
            limb(shL, elL, wrL, w1 - 1f, w2, upper, forearm)
            limb(shR, elR, wrR, w1 - 1f, w2, upper, forearm)
            listOf(shL to elL, shR to elR).forEach { (s, e) ->
                val hem = Offset(s.x + (e.x - s.x) * 0.5f, s.y + (e.y - s.y) * 0.5f)
                drawLine(INK, s, hem, w1 + 6.4f, StrokeCap.Round)
                drawLine(sleeve, s, hem, w1 + 2f, StrokeCap.Round)
                drawLine(sleeve.darker(0.82f), s + Offset(w1 * 0.3f, 0f), hem + Offset(w1 * 0.3f, 0f), w1 * 0.5f, StrokeCap.Round)
            }
        } else {
            limb(shL, elL, wrL, w1, w2, sleeve, forearm)
            limb(shR, elR, wrR, w1, w2, sleeve, forearm)
            listOf(elL to wrL, elR to wrR).forEach { (e, w) ->
                val m = Offset(e.x + (w.x - e.x) * 0.85f, e.y + (w.y - e.y) * 0.85f)
                drawCircle(INK, w2 / 2 + 2.6f, m); drawCircle(if (ov == "ROBE") o1 else sleeve.darker(0.8f), w2 / 2 + 0.6f, m)
            }
        }
        val glove = if (ov == "SPACE") c(0xFFECEFF1) else skin
        hand(wrL + Offset(-1f, 8f), glove, -1f)
        hand(wrR + Offset(1f, if (anim == "WAVE") -8f else 8f), glove, 1f)
    }

    fun DrawScope.hand(center: Offset, col: Color, dir: Float) {
        val palm = Path().apply { addOval(Rect(center.x - 7f, center.y - 8f, center.x + 7f, center.y + 8f)) }
        inked(palm, vol(col, center - Offset(7f, 8f), center + Offset(7f, 8f)), 2f)
        val thumb = Path().apply { addOval(Rect(center.x - dir * 9f - 3.5f, center.y - 6f, center.x - dir * 9f + 3.5f, center.y + 2f)) }
        inked(thumb, col, 1.6f)
        drawLine(col.darker(0.75f), Offset(center.x - 3f, center.y + 3f), Offset(center.x + 3f, center.y + 3f), 1f)
    }

    // ------------------------------------------------------------------ cabeça e rosto

    fun facePath() = Path().apply {
        moveTo(100f, 33f)
        cubicTo(116f, 33f, 128f, 44f, 128f, 62f)
        cubicTo(128f, 76f, 124f, 86f, 116f, 93f)
        cubicTo(110f, 98f, 105f, 101f, 100f, 101f)
        cubicTo(95f, 101f, 90f, 98f, 84f, 93f)
        cubicTo(76f, 86f, 72f, 76f, 72f, 62f)
        cubicTo(72f, 44f, 84f, 33f, 100f, 33f); close()
    }

    fun DrawScope.neckAndHead() {
        if (ov != "SPACE") {
            val neck = Path().apply { moveTo(92f, 90f); lineTo(108f, 90f); lineTo(109f, 108f); quadraticTo(100f, 112f, 91f, 108f); close() }
            inked(neck, skin)
            drawRect(skinShade, Offset(92f, 92f), Size(16f, 6f))
        }
        // orelhas
        listOf(72f, 128f).forEach { x ->
            val ear = Path().apply { addOval(Rect(x - 5f, 60f, x + 5f, 76f)) }
            inked(ear, skin, 2f)
            drawArc(skinShade, if (x < 100) 60f else -120f, 180f, false, Offset(x - 2.5f, 64f), Size(5f, 8f), style = Stroke(1.4f))
        }
        val face = facePath()
        inked(face, Brush.linearGradient(listOf(skin.lighter(0.12f), skin, skin), Offset(76f, 36f), Offset(124f, 100f)), 2.4f)
        clipPath(face) {
            // sombra do lado direito (cel-shading) e sob a franja
            val shade = Path().apply { moveTo(116f, 30f); cubicTo(132f, 50f, 128f, 90f, 104f, 104f); lineTo(140f, 104f); lineTo(140f, 30f); close() }
            drawPath(shade, skinShade.copy(alpha = 0.55f))
            drawRect(skinShade.copy(alpha = 0.45f), Offset(70f, 44f), Size(60f, 10f))
        }
        // bochechas
        drawCircle(Brush.radialGradient(listOf(c(0xFFFF7B7B).copy(alpha = 0.38f), Color.Transparent), Offset(82f, 80f), 8f), 8f, Offset(82f, 80f))
        drawCircle(Brush.radialGradient(listOf(c(0xFFFF7B7B).copy(alpha = 0.38f), Color.Transparent), Offset(118f, 80f), 8f), 8f, Offset(118f, 80f))
        // nariz
        val nose = Path().apply { moveTo(101f, 69f); quadraticTo(104f, 78f, 99f, 80f) }
        drawPath(nose, skin.darker(0.66f), style = Stroke(1.8f, cap = StrokeCap.Round))
        drawCircle(Color.White.copy(alpha = 0.5f), 1.2f, Offset(100f, 74f))
    }

    fun DrawScope.eye(o: Offset, k: Float = 1f, iris: Color = c(0xFF6B4A2B), starIris: Boolean = false, lidCut: Float = 0f) {
        if (blink && !starIris) {
            drawArc(INK, 10f, 160f, false, Offset(o.x - 7f * k, o.y - 4f), Size(14f * k, 7f), style = Stroke(2.4f, cap = StrokeCap.Round))
            return
        }
        val white = Path().apply { addOval(Rect(o.x - 7f * k, o.y - 6f * k, o.x + 7f * k, o.y + 6f * k)) }
        drawPath(white, Color.White)
        clipPath(white) {
            if (starIris) star(o, 6.5f * k, c(0xFFFFC93C), true)
            else {
                drawCircle(Brush.radialGradient(listOf(iris.lighter(0.35f), iris, iris.darker(0.6f)), o + Offset(0f, 1f), 5f * k), 5f * k, o + Offset(0.5f, 0.5f))
                drawCircle(INK, 2.3f * k, o + Offset(0.5f, 0.8f))
            }
            drawCircle(Color.White, 1.7f * k, o + Offset(2f, -2f) * k)
            drawCircle(Color.White.copy(alpha = 0.8f), 0.8f * k, o + Offset(-1.8f, 2f) * k)
            if (lidCut > 0f) drawRect(skin, Offset(o.x - 8f * k, o.y - 7f * k), Size(16f * k, 12f * k * lidCut))
        }
        // pálpebra superior grossa (traço de quadrinho)
        drawArc(INK, 195f, 150f, false, Offset(o.x - 7.5f * k, o.y - 6.5f * k + 12f * k * lidCut), Size(15f * k, 13f * k), style = Stroke(2.6f, cap = StrokeCap.Round))
        drawArc(INK.copy(alpha = 0.5f), 30f, 120f, false, Offset(o.x - 6f * k, o.y - 5f * k), Size(12f * k, 11f * k), style = Stroke(1f))
    }

    fun DrawScope.brow(a: Offset, b: Offset, lift: Float) {
        val p = Path().apply { moveTo(a.x, a.y); quadraticTo((a.x + b.x) / 2, (a.y + b.y) / 2 - lift, b.x, b.y) }
        drawPath(p, hairColor.darker(0.6f), style = Stroke(3.4f, cap = StrokeCap.Round))
    }

    fun DrawScope.openMouth(top: Float, w: Float, h: Float) {
        val m = Path().apply {
            moveTo(100f - w / 2, top); quadraticTo(100f, top + 1.5f, 100f + w / 2, top)
            quadraticTo(100f + w / 2 - 1f, top + h, 100f, top + h); quadraticTo(100f - w / 2 + 1f, top + h, 100f - w / 2, top); close()
        }
        drawPath(m, c(0xFF6E1B24))
        clipPath(m) {
            drawRect(Color.White, Offset(100f - w / 2, top - 1f), Size(w, h * 0.3f))
            drawOval(c(0xFFFF7A8A), Offset(100f - w * 0.3f, top + h * 0.55f), Size(w * 0.6f, h * 0.7f))
        }
        drawPath(m, INK, style = Stroke(2.2f, join = StrokeJoin.Round))
    }

    fun DrawScope.face() {
        val mouthInk = Stroke(2.4f, cap = StrokeCap.Round)
        when (look[Slot.FACE]?.variant ?: "SMILE") {
            "BIG_SMILE" -> {
                listOf(eyeL, eyeR).forEach { drawArc(INK, 200f, 140f, false, Offset(it.x - 7f, it.y - 3f), Size(14f, 10f), style = Stroke(3f, cap = StrokeCap.Round)) }
                brow(Offset(82f, 54f), Offset(95f, 53f), 3f); brow(Offset(105f, 53f), Offset(118f, 54f), 3f)
                openMouth(83f, 20f, 12f)
            }
            "WINK" -> {
                eye(eyeL)
                drawArc(INK, 200f, 140f, false, Offset(eyeR.x - 7f, eyeR.y - 3f), Size(14f, 10f), style = Stroke(3f, cap = StrokeCap.Round))
                brow(Offset(82f, 56f), Offset(95f, 55f), 2f); brow(Offset(105f, 57f), Offset(118f, 58f), -1f)
                val m = Path().apply { moveTo(92f, 86f); quadraticTo(101f, 92f, 110f, 84f) }
                drawPath(m, INK, style = mouthInk)
            }
            "SURPRISED" -> {
                eye(eyeL, 1.2f); eye(eyeR, 1.2f)
                brow(Offset(82f, 51f), Offset(95f, 50f), 4f); brow(Offset(105f, 50f), Offset(118f, 51f), 4f)
                val m = Path().apply { addOval(Rect(95f, 84f, 105f, 96f)) }
                drawPath(m, c(0xFF6E1B24)); drawPath(m, INK, style = Stroke(2.2f))
            }
            "COOL" -> {
                eye(eyeL, lidCut = 0.42f); eye(eyeR, lidCut = 0.42f)
                brow(Offset(82f, 58f), Offset(95f, 58f), 0f); brow(Offset(105f, 57f), Offset(118f, 55f), 1f)
                val m = Path().apply { moveTo(93f, 88f); quadraticTo(102f, 89f, 109f, 84f) }
                drawPath(m, INK, style = mouthInk)
            }
            "STARS" -> {
                eye(eyeL, 1.15f, starIris = true); eye(eyeR, 1.15f, starIris = true)
                brow(Offset(82f, 53f), Offset(95f, 52f), 4f); brow(Offset(105f, 52f), Offset(118f, 53f), 4f)
                openMouth(82f, 22f, 14f)
            }
            "DETERMINED" -> {
                eye(eyeL, lidCut = 0.25f); eye(eyeR, lidCut = 0.25f)
                brow(Offset(81f, 54f), Offset(96f, 60f), -1f); brow(Offset(104f, 60f), Offset(119f, 54f), -1f)
                val teeth = Path().apply { addRoundRect(RoundRect(Rect(91f, 84f, 109f, 92f), CornerRadius(3f))) }
                inked(teeth, Color.White, 2f)
                drawLine(INK.copy(alpha = 0.6f), Offset(91f, 88f), Offset(109f, 88f), 1f)
                for (x in listOf(95.5f, 100f, 104.5f)) drawLine(INK.copy(alpha = 0.5f), Offset(x, 84f), Offset(x, 92f), 0.8f)
            }
            else -> { // SMILE
                eye(eyeL); eye(eyeR)
                brow(Offset(82f, 56f), Offset(95f, 55f), 2f); brow(Offset(105f, 55f), Offset(118f, 56f), 2f)
                val m = Path().apply { moveTo(91f, 85f); quadraticTo(100f, 93f, 109f, 85f) }
                drawPath(m, INK, style = mouthInk)
                drawLine(INK.copy(alpha = 0.6f), Offset(89f, 84f), Offset(91f, 86f), 1.4f, StrokeCap.Round)
                drawLine(INK.copy(alpha = 0.6f), Offset(111f, 84f), Offset(109f, 86f), 1.4f, StrokeCap.Round)
            }
        }
    }

    // ------------------------------------------------------------------ cabelo

    fun hairBrush(): Brush {
        val cols = hairItem?.colors ?: listOf(0xFF6B3E26)
        return if (cols.size > 1) Brush.verticalGradient(listOf(c(cols[0]).lighter(0.2f), c(cols[0]), c(cols[1])), 10f, 140f)
        else Brush.verticalGradient(listOf(hairColor.lighter(0.3f), hairColor, hairColor.darker(0.7f)), 10f, 70f)
    }

    fun capPath() = Path().apply {
        moveTo(69f, 70f); cubicTo(64f, 38f, 82f, 22f, 100f, 22f); cubicTo(118f, 22f, 136f, 38f, 131f, 70f)
        cubicTo(128f, 60f, 124f, 52f, 116f, 49f); quadraticTo(100f, 45f, 84f, 49f); cubicTo(76f, 52f, 72f, 60f, 69f, 70f); close()
    }

    fun DrawScope.shine(p1: Offset, p2: Offset, p3: Offset) {
        val hp = Path().apply { moveTo(p1.x, p1.y); quadraticTo(p2.x, p2.y, p3.x, p3.y) }
        drawPath(hp, Color.White.copy(alpha = 0.45f), style = Stroke(2.4f, cap = StrokeCap.Round))
    }

    fun DrawScope.hairFront() {
        val h = hairItem ?: return
        val brush = hairBrush()
        when (h.variant) {
            "SPIKY" -> {
                inked(capPath(), brush)
                val center = Offset(100f, 46f)
                // Mechas grossas e curtas (estilo herói de quadrinho), não raios finos.
                val spikes = listOf(-188f to 12f, -162f to 18f, -136f to 24f, -110f to 27f, -84f to 28f, -58f to 25f, -32f to 20f, -6f to 13f)
                spikes.forEachIndexed { i, (deg, len) ->
                    val a = deg * PI.toFloat() / 180f
                    val baseR = 20f
                    val b1a = a - 0.36f; val b2a = a + 0.36f
                    val b1 = Offset(center.x + baseR * cos(b1a), center.y + baseR * sin(b1a))
                    val b2 = Offset(center.x + baseR * cos(b2a), center.y + baseR * sin(b2a))
                    val tip = Offset(center.x + (baseR + len) * cos(a), center.y + (baseR + len) * sin(a))
                    inked(lock(b1, b2, tip, if (i % 2 == 0) 5f else -4f), brush, 2f)
                }
                // franja em pontas
                listOf(Triple(78f, 90f, Offset(80f, 60f)), Triple(88f, 102f, Offset(92f, 62f)), Triple(98f, 112f, Offset(106f, 60f)), Triple(108f, 122f, Offset(120f, 58f))).forEach { (x1, x2, tip) ->
                    inked(lock(Offset(x1, 46f), Offset(x2, 46f), tip, 2f), brush, 2f)
                }
                shine(Offset(84f, 30f), Offset(96f, 24f), Offset(108f, 28f))
            }
            "CURLY" -> {
                val cap = Path().apply { addOval(Rect(64f, 20f, 136f, 74f)) }
                inked(cap, brush)
                val pts = listOf(-195, -170, -148, -126, -104, -82, -60, -38, -16, 8)
                pts.forEach { deg ->
                    val a = deg * PI.toFloat() / 180f
                    val o = Offset(100f + 36f * cos(a), 52f + 30f * sin(a))
                    inkCircle(o, 11f, brush, 2f)
                    drawArc(Color.White.copy(alpha = 0.4f), 200f, 70f, false, o - Offset(7f, 7f), Size(14f, 14f), style = Stroke(1.8f, cap = StrokeCap.Round))
                }
                listOf(Offset(84f, 50f), Offset(98f, 46f), Offset(112f, 49f)).forEach { inkCircle(it, 9f, brush, 2f) }
            }
            "MOHAWK" -> {
                val stubble = Path().apply { addOval(Rect(72f, 30f, 128f, 64f)) }
                clipPath(facePath()) { drawPath(stubble, hairColor.copy(alpha = 0.28f)) }
                for (i in 0..5) {
                    val y = 48f - i * 1f
                    val x = 100f
                    inked(lock(Offset(x - 7f, y + 4f - i * 2f), Offset(x + 7f, y + 4f - i * 2f), Offset(x + (i - 2.5f) * 4f, 2f + i * 4f), 0f), brush, 2f)
                }
                shine(Offset(98f, 14f), Offset(101f, 26f), Offset(100f, 40f))
            }
            "BUN" -> {
                inkCircle(Offset(100f, 18f), 13f, brush)
                drawArc(hairColor.darker(0.6f), 200f, 140f, false, Offset(90f, 10f), Size(20f, 18f), style = Stroke(1.4f))
                val band = Path().apply { addRoundRect(RoundRect(Rect(92f, 27f, 108f, 32f), CornerRadius(2f))) }
                inked(band, c(0xFFE53935), 1.4f)
                inked(capPath(), brush)
                drawLine(hairColor.darker(0.55f), Offset(88f, 26f), Offset(84f, 48f), 1.6f, StrokeCap.Round)
                inked(lock(Offset(76f, 50f), Offset(92f, 46f), Offset(74f, 64f), -2f), brush, 2f)
                inked(lock(Offset(108f, 46f), Offset(124f, 50f), Offset(127f, 64f), 2f), brush, 2f)
                shine(Offset(92f, 28f), Offset(104f, 25f), Offset(116f, 30f))
            }
            "LONG" -> {
                inked(capPath(), brush)
                listOf(true, false).forEach { left ->
                    val side = Path().apply {
                        if (left) { moveTo(70f, 50f); cubicTo(62f, 80f, 64f, 110f, 70f, 130f); quadraticTo(76f, 120f, 78f, 100f); cubicTo(76f, 80f, 78f, 64f, 84f, 50f) }
                        else { moveTo(130f, 50f); cubicTo(138f, 80f, 136f, 110f, 130f, 130f); quadraticTo(124f, 120f, 122f, 100f); cubicTo(124f, 80f, 122f, 64f, 116f, 50f) }
                        close()
                    }
                    inked(side, brush, 2f)
                }
                listOf(Triple(78f, 96f, Offset(84f, 64f)), Triple(92f, 112f, Offset(104f, 62f)), Triple(106f, 124f, Offset(122f, 60f))).forEach { (x1, x2, tip) ->
                    inked(lock(Offset(x1, 44f), Offset(x2, 44f), tip, 3f), brush, 2f)
                }
                shine(Offset(80f, 34f), Offset(96f, 26f), Offset(112f, 30f))
            }
            else -> { // SHORT
                inked(capPath(), brush)
                listOf(
                    Triple(72f, 88f, Offset(74f, 62f)), Triple(84f, 100f, Offset(90f, 60f)),
                    Triple(96f, 112f, Offset(106f, 59f)), Triple(108f, 126f, Offset(124f, 60f)),
                ).forEach { (x1, x2, tip) -> inked(lock(Offset(x1, 46f), Offset(x2, 44f), tip, 3f), brush, 2f) }
                shine(Offset(82f, 32f), Offset(96f, 25f), Offset(110f, 29f))
            }
        }
    }

    // ------------------------------------------------------------------ óculos e chapéus

    fun DrawScope.glasses() {
        val g = look[Slot.GLASSES] ?: return
        val col = c(g.colors[0])
        fun reflex(o: Offset) = drawLine(Color.White.copy(alpha = 0.7f), o + Offset(-4f, 2f), o + Offset(1f, -3f), 1.6f, StrokeCap.Round)
        when (g.variant) {
            "SQUARE" -> {
                listOf(eyeL, eyeR).forEach {
                    drawRoundRect(Color.White.copy(alpha = 0.15f), Offset(it.x - 10f, it.y - 7.5f), Size(20f, 15f), CornerRadius(3f))
                    drawRoundRect(INK, Offset(it.x - 10f, it.y - 7.5f), Size(20f, 15f), CornerRadius(3f), style = Stroke(4.2f))
                    drawRoundRect(col, Offset(it.x - 10f, it.y - 7.5f), Size(20f, 15f), CornerRadius(3f), style = Stroke(2.2f))
                    reflex(it)
                }
            }
            "SUN" -> listOf(eyeL, eyeR).forEach {
                val lens = Path().apply { addRoundRect(RoundRect(Rect(it.x - 11f, it.y - 7f, it.x + 11f, it.y + 8f), CornerRadius(7f))) }
                inked(lens, Brush.verticalGradient(listOf(col.lighter(0.25f), col), it.y - 7f, it.y + 8f), 2.2f)
                drawLine(Color.White.copy(alpha = 0.55f), Offset(it.x - 7f, it.y - 2f), Offset(it.x - 1f, it.y - 5f), 2f, StrokeCap.Round)
            }
            "STAR" -> listOf(eyeL, eyeR).forEach { star(it, 13f, col.copy(alpha = 0.9f), outline = true); reflex(it) }
            "VISOR" -> {
                val v = Path().apply { addRoundRect(RoundRect(Rect(70f, 58f, 130f, 76f), CornerRadius(9f))) }
                inked(v, Brush.horizontalGradient(listOf(col.copy(alpha = 0.85f), col.lighter(0.4f).copy(alpha = 0.75f), col.copy(alpha = 0.85f)), 70f, 130f), 2.2f)
                drawLine(Color.White.copy(alpha = 0.7f), Offset(78f, 63f), Offset(120f, 63f), 2f, StrokeCap.Round)
                return
            }
            else -> listOf(eyeL, eyeR).forEach { // ROUND
                drawCircle(Color.White.copy(alpha = 0.15f), 9.5f, it)
                drawCircle(INK, 9.5f, it, style = Stroke(4f)); drawCircle(col, 9.5f, it, style = Stroke(2f))
                reflex(it)
            }
        }
        drawLine(INK, Offset(96.5f, 66f), Offset(103.5f, 66f), 3.4f); drawLine(col, Offset(96.5f, 66f), Offset(103.5f, 66f), 1.6f)
        drawLine(col, Offset(78f, 64f), Offset(72f, 63f), 2f); drawLine(col, Offset(122f, 64f), Offset(128f, 63f), 2f)
    }

    fun DrawScope.hat() {
        val h = look[Slot.HAT] ?: return
        val h0 = c(h.colors[0]); val h1 = c(h.colors.getOrElse(1) { h.colors[0] })
        when (h.variant) {
            "CAP" -> {
                val dome = Path().apply { moveTo(68f, 52f); cubicTo(64f, 26f, 82f, 14f, 100f, 14f); cubicTo(118f, 14f, 136f, 26f, 132f, 52f); close() }
                inked(dome, vol(h0, Offset(70f, 14f), Offset(130f, 52f)))
                drawLine(h0.darker(0.65f), Offset(100f, 15f), Offset(100f, 50f), 1.3f)
                drawLine(h0.darker(0.65f), Offset(86f, 18f), Offset(80f, 50f), 1.1f); drawLine(h0.darker(0.65f), Offset(114f, 18f), Offset(120f, 50f), 1.1f)
                inkCircle(Offset(100f, 15f), 3f, SolidColor(h1), 1.2f)
                inkCircle(Offset(100f, 36f), 6.5f, SolidColor(h1), 1.6f)
                val brim = Path().apply { moveTo(66f, 50f); quadraticTo(100f, 42f, 134f, 50f); quadraticTo(132f, 62f, 100f, 62f); quadraticTo(68f, 62f, 66f, 50f); close() }
                inked(brim, Brush.verticalGradient(listOf(h0.darker(0.9f), h0.darker(0.6f)), 44f, 62f))
            }
            "BEANIE" -> {
                val dome = Path().apply { moveTo(66f, 54f); cubicTo(62f, 22f, 82f, 8f, 100f, 8f); cubicTo(118f, 8f, 138f, 22f, 134f, 54f); close() }
                inked(dome, vol(h0, Offset(66f, 8f), Offset(134f, 54f)))
                clipPath(dome) { for (x in 70..130 step 7) drawLine(h0.darker(0.8f), Offset(x.toFloat(), 8f), Offset(x.toFloat(), 54f), 1.4f) }
                val cuff = Path().apply { addRoundRect(RoundRect(Rect(64f, 44f, 136f, 60f), CornerRadius(7f))) }
                inked(cuff, vol(h1, Offset(64f, 44f), Offset(136f, 60f)))
                for (x in 68..132 step 5) drawLine(h1.darker(0.82f), Offset(x.toFloat(), 46f), Offset(x.toFloat(), 58f), 1.1f)
                inkCircle(Offset(100f, 8f), 9f, vol(h1, Offset(91f, -1f), Offset(109f, 17f)))
                repeat(6) { i -> drawCircle(h1.darker(0.85f), 1.3f, Offset(96f + (i % 3) * 4f, 5f + (i / 3) * 5f)) }
            }
            "EXPLORER", "DETECTIVE" -> {
                val wide = h.variant == "EXPLORER"
                val bw = if (wide) 58f else 46f
                val brim = Path().apply { addOval(Rect(100f - bw, 36f, 100f + bw, 58f)) }
                inked(brim, Brush.verticalGradient(listOf(h0.lighter(0.1f), h0.darker(0.65f)), 36f, 58f))
                val crown = Path().apply {
                    moveTo(74f, 46f); cubicTo(72f, 22f, 80f, 10f, 92f, 12f); quadraticTo(100f, 18f, 108f, 12f)
                    cubicTo(120f, 10f, 128f, 22f, 126f, 46f); close()
                }
                inked(crown, vol(h0, Offset(74f, 10f), Offset(126f, 46f)))
                val band = Path().apply { addRect(Rect(74.5f, 36f, 125.5f, 44f)) }
                inked(band, h1, 1.6f)
                if (!wide) clipPath(crown) {
                    for (x in 78..124 step 9) drawLine(h1.copy(alpha = 0.35f), Offset(x.toFloat(), 10f), Offset(x.toFloat(), 46f), 1.2f)
                    for (y in 16..34 step 8) drawLine(h1.copy(alpha = 0.35f), Offset(72f, y.toFloat()), Offset(128f, y.toFloat()), 1.2f)
                }
                drawLine(h0.darker(0.55f), Offset(100f, 16f), Offset(100f, 30f), 1.6f, StrokeCap.Round)
            }
            "CROWN" -> {
                val p = Path().apply {
                    moveTo(68f, 52f); lineTo(66f, 22f); lineTo(80f, 36f); lineTo(90f, 14f); lineTo(100f, 32f); lineTo(110f, 14f); lineTo(120f, 36f); lineTo(134f, 22f); lineTo(132f, 52f); close()
                }
                inked(p, Brush.linearGradient(listOf(c(0xFFFFF59D), h0, h0.darker(0.7f)), Offset(70f, 14f), Offset(130f, 52f)))
                val band = Path().apply { addRect(Rect(68f, 44f, 132f, 52f)) }
                inked(band, h0.darker(0.82f), 1.6f)
                listOf(Offset(66f, 22f), Offset(90f, 14f), Offset(110f, 14f), Offset(134f, 22f)).forEach { inkCircle(it, 3.6f, SolidColor(h1), 1.2f) }
                inkCircle(Offset(100f, 48f), 3.4f, SolidColor(c(0xFF42A5F5)), 1.2f)
                drawLine(Color.White.copy(alpha = 0.6f), Offset(76f, 40f), Offset(84f, 30f), 1.8f, StrokeCap.Round)
            }
            "WIZARD" -> {
                val brim = Path().apply { addOval(Rect(46f, 38f, 154f, 60f)) }
                inked(brim, Brush.verticalGradient(listOf(h0.lighter(0.1f), h0.darker(0.6f)), 38f, 60f))
                val cone = Path().apply {
                    moveTo(72f, 48f); cubicTo(80f, 20f, 100f, 2f, 128f, -6f); cubicTo(118f, 8f, 120f, 26f, 128f, 48f); close()
                }
                inked(cone, vol(h0, Offset(72f, -6f), Offset(128f, 48f)))
                val band = Path().apply { moveTo(74f, 40f); quadraticTo(100f, 36f, 126f, 40f); lineTo(127f, 47f); quadraticTo(100f, 44f, 73f, 47f); close() }
                inked(band, h1, 1.6f)
                star(Offset(98f, 26f), 5f, h1, true); star(Offset(114f, 12f), 3.5f, h1, true)
            }
            "HELMET" -> {
                drawCircle(Brush.radialGradient(listOf(h0.copy(alpha = 0.08f), h0.copy(alpha = 0.32f)), Offset(100f, 62f), 50f), 50f, Offset(100f, 62f))
                drawCircle(INK, 50f, Offset(100f, 62f), style = Stroke(4.4f)); drawCircle(h1, 50f, Offset(100f, 62f), style = Stroke(2.2f))
                drawArc(Color.White.copy(alpha = 0.8f), 200f, 55f, false, Offset(58f, 20f), Size(84f, 84f), style = Stroke(4f, cap = StrokeCap.Round))
                drawCircle(Color.White.copy(alpha = 0.7f), 3f, Offset(132f, 40f))
                val collar = Path().apply { addOval(Rect(72f, 100f, 128f, 114f)) }
                inked(collar, Brush.horizontalGradient(listOf(c(0xFFECEFF1), c(0xFF90A4AE)), 72f, 128f), 2f)
            }
            "ANTENNA" -> {
                val bob = 3f * sin(wave * 3)
                listOf(Offset(82f, 30f) to Offset(66f, 4f + bob), Offset(118f, 30f) to Offset(134f, 4f - bob)).forEach { (a, b) ->
                    val spring = Path().apply {
                        moveTo(a.x, a.y)
                        for (i in 1..6) {
                            val f = i / 6f
                            val x = a.x + (b.x - a.x) * f + (if (i % 2 == 0) 3f else -3f)
                            val y = a.y + (b.y - a.y) * f
                            lineTo(x, y)
                        }
                    }
                    drawPath(spring, INK, style = Stroke(3.6f, join = StrokeJoin.Round)); drawPath(spring, h0, style = Stroke(1.8f, join = StrokeJoin.Round))
                    drawCircle(Brush.radialGradient(listOf(h1.copy(alpha = 0.6f), Color.Transparent), b, 12f), 12f, b)
                    inkCircle(b, 6f, vol(h1, b - Offset(6f, 6f), b + Offset(6f, 6f)), 1.6f)
                }
                val band = Path().apply { moveTo(70f, 56f); cubicTo(66f, 22f, 134f, 22f, 130f, 56f) }
                drawPath(band, INK, style = Stroke(8f, cap = StrokeCap.Round)); drawPath(band, h0, style = Stroke(4.6f, cap = StrokeCap.Round))
            }
        }
    }

    // ------------------------------------------------------------------ efeitos

    fun DrawScope.magnifier() {
        val a = wave
        val pos = Offset(100f + 82f * cos(a), 140f + 30f * sin(a))
        rotate(30f + 15f * sin(a), pos) {
            drawLine(INK, pos + Offset(8f, 8f), pos + Offset(22f, 22f), 8f, StrokeCap.Round)
            drawLine(c(0xFF8D6E63), pos + Offset(8f, 8f), pos + Offset(22f, 22f), 4.6f, StrokeCap.Round)
            drawCircle(Brush.radialGradient(listOf(c(0xFFE1F5FE).copy(alpha = 0.7f), c(0xFF81D4FA).copy(alpha = 0.5f)), pos, 12f), 12f, pos)
            drawCircle(INK, 12f, pos, style = Stroke(5.4f)); drawCircle(c(0xFFFFC93C), 12f, pos, style = Stroke(3f))
            drawArc(Color.White.copy(alpha = 0.8f), 200f, 60f, false, pos - Offset(8f, 8f), Size(16f, 16f), style = Stroke(2f, cap = StrokeCap.Round))
        }
    }

    fun DrawScope.aura() {
        val letters = "ABCMPSR"
        letters.forEachIndexed { i, ch ->
            val ph = (t * 1.5f + i / letters.length.toFloat()) % 1f
            val x = 100f + (if (i % 2 == 0) -1 else 1) * (60f + 16f * sin(ph * 6f + i))
            val y = 240f - ph * 230f
            val alpha = sin(ph * PI.toFloat()).coerceIn(0f, 1f)
            drawCircle(Brush.radialGradient(listOf(c(0xFFFFE082).copy(alpha = 0.5f * alpha), Color.Transparent), Offset(x, y), 14f), 14f, Offset(x, y))
            val layout = measurer.measure(ch.toString(), TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black))
            val k = 16f / layout.size.height
            withTransform({ scale(k, k, Offset(x, y)) }) {
                drawText(layout, c(0xFFFFC93C).copy(alpha = alpha), Offset(x - layout.size.width / 2f, y - layout.size.height / 2f))
            }
        }
    }

    fun DrawScope.starRain() {
        repeat(16) { i ->
            val x = (i * 53 % 190).toFloat() + 5f
            val y = ((t * 1.5f + i / 16f) % 1f) * 280f - 10f
            drawCircle(Brush.radialGradient(listOf(c(0xFFFFF59D).copy(alpha = 0.5f), Color.Transparent), Offset(x, y), 9f), 9f, Offset(x, y))
            star(Offset(x, y), 3.5f + (i % 3), c(0xFFFFF59D))
        }
    }
}
