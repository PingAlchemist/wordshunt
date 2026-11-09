package br.com.cacapalavras.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cacapalavras.domain.Progression
import br.com.cacapalavras.domain.PlayerProgress
import br.com.cacapalavras.domain.Inventory
import br.com.cacapalavras.ui.GameRequest
import br.com.cacapalavras.ui.Screen
import br.com.cacapalavras.ui.components.Confetti
import br.com.cacapalavras.ui.components.GameCard
import br.com.cacapalavras.ui.components.LevelBadge
import br.com.cacapalavras.ui.components.ProgressBar
import br.com.cacapalavras.ui.theme.Gold
import br.com.cacapalavras.ui.theme.LocalPalette
import kotlinx.coroutines.delay

@Composable
fun ResultScreen(
    result: Screen.Result,
    progress: PlayerProgress,
    hapticsOn: Boolean,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit,
) {
    val rw = result.rewards
    val s = rw.summary
    val look = Inventory.appearanceOf(progress)
    val palette = LocalPalette.current
    val haptics = LocalHapticFeedback.current
    val isTA = result.request is GameRequest.TimeAttack

    var revealedLines by remember { mutableIntStateOf(0) }
    var revealedStars by remember { mutableIntStateOf(0) }
    val xpAnim = remember { Animatable(s.totalXpBefore.toFloat()) }
    var showLevelUp by remember { mutableStateOf(false) }
    var levelUpSeen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(250)
        repeat(s.stars ?: 0) {
            revealedStars = it + 1
            if (hapticsOn) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(320)
        }
        repeat(s.lines.size) {
            revealedLines = it + 1
            delay(170)
        }
        xpAnim.animateTo(s.totalXpAfter.toFloat(), tween(1600, easing = FastOutSlowInEasing))
        if (s.leveledUp) {
            delay(200)
            if (hapticsOn) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            showLevelUp = true
        }
    }

    val shownInfo = Progression.levelInfo(xpAnim.value.toLong())

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(18.dp))
            Text(
                when (result.request) {
                    is GameRequest.Journey -> "Nível ${result.request.level} concluído!"
                    is GameRequest.Daily -> "Desafio diário concluído!"
                    is GameRequest.TimeAttack -> "Tempo esgotado!"
                },
                style = MaterialTheme.typography.headlineMedium, color = palette.text, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))

            if (s.stars != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { i ->
                        val on = i < revealedStars
                        val scale = remember { Animatable(0.6f) }
                        LaunchedEffect(on) { if (on) scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy)) }
                        Text(
                            if (on) "★" else "☆", fontSize = if (i == 1) 64.sp else 50.sp,
                            color = if (on) Gold else palette.muted.copy(alpha = 0.4f),
                            modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
                        )
                    }
                }
            } else {
                Text("📝 ${result.wordsFound} palavras", style = MaterialTheme.typography.headlineLarge, color = palette.accent)
                if (s.newRecord) {
                    Text("🏆 Novo recorde!", style = MaterialTheme.typography.titleLarge, color = Gold)
                }
            }
            // Sequência diária (004-US20): aparece na 1ª fase do dia
            val su = rw.streak
            if (su.newDay) {
                Spacer(Modifier.height(10.dp))
                GameCard(Modifier.fillMaxWidth(), color = lerp(palette.card, FlameOrange, 0.16f)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        StreakFlame(su.current, true, size = 46)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (su.current == 1) "Sequência iniciada! 🔥" else "Sequência: ${su.current} dias! 🔥",
                                fontWeight = FontWeight.Black, color = palette.text, fontSize = 17.sp,
                            )
                            Text("Recompensa do dia: ${su.rewardText.joinToString(" ")}", color = palette.text, fontSize = 13.sp)
                            if (su.shieldsUsed > 0) Text("🛡️ ${su.shieldsUsed} protetor(es) salvaram sua sequência", color = palette.muted, fontSize = 12.sp)
                            if (su.shieldEarned) Text("🛡️ Você ganhou um protetor!", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            su.halvedFrom?.let { Text("Você tinha $it dias — a sequência continuou pela metade, não zerou.", color = palette.muted, fontSize = 12.sp) }
                            su.milestoneItem?.let { Text("🏅 Marco alcançado: ${it.name}!", color = FlameOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            GameCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    s.lines.take(revealedLines).forEach { line ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(line.label, color = palette.text, modifier = Modifier.weight(1f))
                            Text("+${line.xp} XP", color = palette.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(palette.muted.copy(alpha = 0.25f)))
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, color = palette.text, modifier = Modifier.weight(1f))
                        Text("+${s.totalXp} XP", style = MaterialTheme.typography.titleLarge, color = palette.accent)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text("Moedas", color = palette.text, modifier = Modifier.weight(1f))
                        Text("+${s.coins} 🪙", fontWeight = FontWeight.Bold, color = palette.text)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            GameCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(shownInfo.level, 52.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(Progression.title(shownInfo.level), style = MaterialTheme.typography.titleMedium, color = palette.text)
                        Spacer(Modifier.height(6.dp))
                        ProgressBar(shownInfo.fraction, Modifier.fillMaxWidth(), animate = false)
                        Text("${shownInfo.xpIntoLevel} / ${shownInfo.xpForNext} XP", color = palette.muted, fontSize = 13.sp)
                    }
                }
            }

            s.achievements.forEach { a ->
                Spacer(Modifier.height(10.dp))
                AnimatedVisibility(true, enter = slideInVertically() + fadeIn()) {
                    GameCard(Modifier.fillMaxWidth(), color = lerp(palette.card, Gold, 0.25f)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(a.emoji, fontSize = 30.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Conquista: ${a.name}", fontWeight = FontWeight.Bold, color = palette.text)
                                Text(a.description, color = palette.muted, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Passe de temporada (mesmo XP do jogo)
            if (rw.passXp > 0) {
                Spacer(Modifier.height(10.dp))
                GameCard(Modifier.fillMaxWidth(), color = lerp(palette.card, Gold, 0.14f)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🎟️", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Passe +${rw.passXp} XP", fontWeight = FontWeight.Bold, color = palette.text)
                            Text(
                                if (rw.passLevelAfter > rw.passLevelBefore) "Subiu para o nível ${rw.passLevelAfter}! Recompensas esperando 🎁"
                                else "Nível ${rw.passLevelAfter} do passe",
                                color = palette.muted, fontSize = 13.sp,
                            )
                        }
                    }
                }
            }

            // Itens de visual desbloqueados (003a-US15)
            if (rw.newItems.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("Novos itens de visual", style = MaterialTheme.typography.titleMedium, color = palette.text)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rw.newItems.take(4).forEach { item ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            ItemThumb(item, look)
                            Text(item.name, fontSize = 11.sp, color = palette.text, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                    repeat((4 - rw.newItems.size).coerceAtLeast(0)) { Spacer(Modifier.weight(1f)) }
                }
            }

            Spacer(Modifier.height(22.dp))
            when (result.request) {
                is GameRequest.Journey -> Button(
                    onClick = onNext, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                ) { Text("Próximo nível ▶", fontSize = 18.sp) }
                is GameRequest.TimeAttack -> Button(
                    onClick = onReplay, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                ) { Text("Jogar de novo ↻", fontSize = 18.sp) }
                is GameRequest.Daily -> Unit
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Início")
            }
            Spacer(Modifier.height(24.dp))
        }

        if (s.stars == 3 || s.newRecord) {
            Confetti(Modifier.fillMaxSize(), palette.highlights + Gold, count = 80)
        }

        if (showLevelUp && !levelUpSeen) {
            LevelUpOverlay(s, rw.newItems.map { "${it.slot.emoji} ${it.name}" }) { levelUpSeen = true }
        }
    }
}

@Composable
private fun LevelUpOverlay(s: Progression.RewardSummary, newItems: List<String>, onClose: () -> Unit) {
    val palette = LocalPalette.current
    val scale = remember { Animatable(0.3f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
    val levelCoins = ((s.levelBefore + 1)..s.levelAfter).sumOf { Progression.levelUpCoins(it) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(remember { MutableInteractionSource() }, null) { },
        contentAlignment = Alignment.Center,
    ) {
        Confetti(Modifier.fillMaxSize(), palette.highlights + Gold)
        Column(
            Modifier
                .padding(28.dp)
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                .clip(RoundedCornerShape(28.dp))
                .background(palette.card)
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("SUBIU DE NÍVEL!", color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(14.dp))
            LevelBadge(s.levelAfter, 110.dp)
            Spacer(Modifier.height(12.dp))
            Text(Progression.title(s.levelAfter), style = MaterialTheme.typography.headlineMedium, color = palette.text)
            if (Progression.title(s.levelAfter) != Progression.title(s.levelBefore)) {
                Text("Novo título desbloqueado!", color = palette.accent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text("Recompensas", color = palette.muted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("+$levelCoins 🪙", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = palette.text)
            (s.unlocks + newItems).forEach {
                Text("🔓 $it", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = palette.accent, modifier = Modifier.padding(top = 6.dp))
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onClose, shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Continuar", fontSize = 17.sp) }
        }
    }
}
