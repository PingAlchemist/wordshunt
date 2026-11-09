package br.com.cacapalavras.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import br.com.cacapalavras.domain.PlayerProgress
import br.com.cacapalavras.domain.Progression
import br.com.cacapalavras.ui.Floater
import br.com.cacapalavras.ui.GameController
import br.com.cacapalavras.ui.GameEvent
import br.com.cacapalavras.ui.components.Pill
import br.com.cacapalavras.ui.components.ProgressBar
import br.com.cacapalavras.ui.components.WordGrid
import br.com.cacapalavras.ui.components.formatTime
import br.com.cacapalavras.ui.theme.Gold
import br.com.cacapalavras.ui.theme.LocalPalette

@Composable
fun GameScreen(game: GameController, progress: PlayerProgress, onQuit: () -> Unit) {
    val palette = LocalPalette.current
    val haptics = LocalHapticFeedback.current
    var confirmQuit by remember { mutableStateOf(false) }
    var banner by remember { mutableStateOf<String?>(null) }
    val hapticsOn by rememberUpdatedState(progress.hapticsEnabled)

    // Pausa ao sair do app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, game) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_PAUSE) game.paused = true
            if (e == Lifecycle.Event.ON_RESUME && !confirmQuit) game.paused = false
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(game) {
        game.events.collect { e ->
            val on = hapticsOn
            when (e) {
                is GameEvent.Found -> if (on) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                GameEvent.Tick -> if (on) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                GameEvent.Miss -> Unit
                GameEvent.BoardCleared -> {
                    banner = "Tabuleiro limpo! +15s"
                    if (on) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                GameEvent.Complete -> banner = if (game.isTimeAttack) "Tempo esgotado!" else "Completo! 🎉"
            }
        }
    }
    LaunchedEffect(banner) {
        if (banner != null && !game.finished) {
            kotlinx.coroutines.delay(1400)
            banner = null
        }
    }

    BackHandler {
        game.paused = true
        confirmQuit = true
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        // ---------- Topo ----------
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { game.paused = true; confirmQuit = true }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Sair", tint = palette.text)
            }
            Column(Modifier.weight(1f)) {
                Text(game.title, style = MaterialTheme.typography.titleLarge, color = palette.text)
                Text(
                    "${game.engine.puzzle.themeName} · ${game.config.size}×${game.config.size}",
                    style = MaterialTheme.typography.bodyMedium, color = palette.muted,
                )
            }
            if (game.isTimeAttack) {
                val low = game.timeLeftMs < 10_000
                Pill("⏱ ${formatTime(game.timeLeftMs)}", bg = if (low) Color(0xFFE53935) else palette.card, fg = if (low) Color.White else palette.text)
            } else {
                Pill("⏱ ${formatTime(game.elapsedMs)}", bg = palette.card, fg = palette.text)
            }
            Spacer(Modifier.width(6.dp))
            Pill("🪙 ${progress.coins}", bg = palette.card, fg = palette.text)
        }

        // ---------- Faixa de XP / combo ----------
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp).height(34.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("✨ +${game.levelXp} XP", color = palette.accent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            if (game.isTimeAttack) {
                Spacer(Modifier.width(12.dp))
                Text("📝 ${game.taTotalWords}  ·  🧩 ${game.boardsCleared}", color = palette.muted, fontWeight = FontWeight.Bold)
            } else {
                Spacer(Modifier.width(12.dp))
                Text("${game.found.size}/${game.engine.puzzle.words.size} palavras", color = palette.muted, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            val combo = game.comboNow()
            AnimatedVisibility(combo >= 2, enter = scaleIn() + fadeIn(), exit = fadeOut()) {
                Column(horizontalAlignment = Alignment.End) {
                    val mult = Progression.comboMultiplier(combo)
                    Text(
                        "🔥 COMBO ×${"%.1f".format(mult).replace('.', ',')}",
                        color = if (combo >= 4) Color(0xFFFF5722) else Color(0xFFFF9800),
                        fontWeight = FontWeight.ExtraBold, fontSize = 15.sp,
                    )
                    ProgressBar(game.comboWindowLeft(), Modifier.width(110.dp), height = 4.dp, animate = false, color = Color(0xFFFF9800))
                }
            }
        }

        // ---------- Grade ----------
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            BoxWithConstraints(Modifier.aspectRatio(1f)) {
                WordGrid(
                    puzzle = game.engine.puzzle,
                    found = game.found,
                    selection = game.selection,
                    anchor = game.anchor,
                    palette = palette,
                    onStart = game::onDragStart,
                    onMove = game::onDragMove,
                    onEnd = game::onDragEnd,
                    modifier = Modifier.fillMaxSize(),
                )
                game.floaters.forEach { f ->
                    FloatingXp(f, maxWidth.value, maxHeight.value) { game.removeFloater(f.id) }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = banner != null, modifier = Modifier.align(Alignment.Center),
                    enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut(),
                ) {
                    Text(
                        banner ?: "", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp,
                        modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(palette.accent.copy(alpha = 0.92f))
                            .padding(horizontal = 22.dp, vertical = 12.dp),
                    )
                }
            }
        }

        // ---------- Lista de palavras ----------
        WordList(game)

        // ---------- Dica (002-US12): só a inicial da Palavra Secreta, nada no quadro ----------
        val cost = Progression.HINT_COST
        val secretDone = game.engine.secretWord?.let { s -> game.found.any { it.word == s } } ?: true
        Button(
            onClick = { game.useHint(cost) },
            enabled = game.canHint && progress.coins >= cost && !game.finished,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
        ) {
            Text(
                when {
                    secretDone -> "🔓 Palavra secreta encontrada!"
                    game.secretInitial != null -> "💡 Dica usada: começa com ${game.secretInitial}"
                    progress.coins < cost -> "💡 Dica · $cost🪙 (moedas insuficientes)"
                    else -> "💡 Dica · $cost🪙  —  inicial da palavra secreta"
                },
                fontSize = 15.sp,
            )
        }
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false; game.paused = false },
            title = { Text("Sair da partida?") },
            text = { Text("O progresso deste tabuleiro será perdido. Moedas gastas em dicas não voltam.") },
            confirmButton = { TextButton(onClick = { confirmQuit = false; onQuit() }) { Text("Sair") } },
            dismissButton = { TextButton(onClick = { confirmQuit = false; game.paused = false }) { Text("Continuar jogando") } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordList(game: GameController) {
    val palette = LocalPalette.current
    val foundMap = game.found.associateBy { it.word }
    FlowRow(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        game.engine.puzzle.words.forEach { w ->
            val fw = foundMap[w]
            val bg = when {
                fw != null && w.secret -> Gold.copy(alpha = 0.55f)
                fw != null -> palette.highlight(fw.colorIndex).copy(alpha = 0.40f)
                w.secret -> Gold.copy(alpha = 0.18f)
                else -> palette.card
            }
            val label = when {
                !w.secret -> w.display.uppercase()
                fw != null -> "🔓 " + w.display.uppercase()
                // Escondida: só tracinhos com o tamanho; a dica troca o primeiro pela inicial.
                else -> "🔒 " + List(w.text.length) { i -> if (i == 0 && game.secretInitial != null) "${game.secretInitial}" else "_" }
                    .joinToString(" ")
            }
            Text(
                label,
                modifier = Modifier
                    .widthIn(min = 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = if (fw != null) palette.text.copy(alpha = 0.65f) else palette.text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textDecoration = if (fw != null) TextDecoration.LineThrough else null,
            )
        }
    }
}

@Composable
private fun FloatingXp(f: Floater, widthDp: Float, heightDp: Float, onDone: () -> Unit) {
    val anim = remember(f.id) { Animatable(0f) }
    LaunchedEffect(f.id) {
        anim.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
        onDone()
    }
    val t = anim.value
    Text(
        f.text,
        modifier = Modifier
            .offset(x = (f.x * widthDp - 50).dp, y = (f.y * heightDp - 20 - 60 * t).dp)
            .graphicsLayer {
                alpha = (1f - t * t)
                val s = 0.8f + 0.4f * (if (t < 0.2f) t / 0.2f else 1f)
                scaleX = s; scaleY = s
            }
            .clip(RoundedCornerShape(10.dp))
            .background(if (f.secret) Gold else Color(0xFF222844).copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = if (f.secret) Color(0xFF3A2A00) else Color.White,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
    )
}

