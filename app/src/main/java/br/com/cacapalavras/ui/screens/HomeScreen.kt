package br.com.cacapalavras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cacapalavras.data.WordBank
import br.com.cacapalavras.domain.Achievements
import br.com.cacapalavras.domain.CosmeticCatalog
import br.com.cacapalavras.domain.Inventory
import br.com.cacapalavras.domain.SeasonPass
import br.com.cacapalavras.ui.components.Avatar
import br.com.cacapalavras.ui.components.ProgressBar
import androidx.compose.foundation.clickable
import br.com.cacapalavras.domain.LevelDesigner
import br.com.cacapalavras.domain.PlayerProgress
import br.com.cacapalavras.domain.Progression
import br.com.cacapalavras.ui.components.GameCard
import br.com.cacapalavras.ui.components.Pill
import br.com.cacapalavras.ui.theme.Gold
import br.com.cacapalavras.ui.theme.LocalPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    progress: PlayerProgress,
    dailyDone: Boolean,
    onJourney: () -> Unit,
    onLevels: () -> Unit,
    onDaily: () -> Unit,
    onTimeAttack: () -> Unit,
    onAchievements: () -> Unit,
    onThemes: () -> Unit,
    onProfile: () -> Unit,
    onCharacter: () -> Unit,
    onPass: () -> Unit,
    onShop: () -> Unit,
    onStreak: () -> Unit,
    onDismissNotif: () -> Unit,
) {
    val palette = LocalPalette.current
    val themeIds = WordBank.themes.map { it.id }
    val next = progress.journeyUnlocked
    val nextCfg = LevelDesigner.journey(next, themeIds)
    val nextTheme = WordBank.byId(nextCfg.themeId)
    val playerLevel = progress.level
    val taUnlocked = playerLevel >= Progression.TIME_ATTACK_UNLOCK_LEVEL

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Cabeçalho
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Caça-Palavras", style = MaterialTheme.typography.headlineMedium, color = palette.text)
                Text("Encontre. Evolua. Repita.", color = palette.muted)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Pill("🪙 ${progress.coins}")
                Box(Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onShop)) { Pill("🧠 ${progress.minds}") }
            }
        }

        // Cartão do jogador com o personagem (003a-US13)
        GameCard(Modifier.fillMaxWidth(), onClick = onCharacter) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(92.dp).clip(RoundedCornerShape(18.dp))) {
                    Avatar(Inventory.appearanceOf(progress), Modifier.fillMaxWidth())
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(progress.playerName, style = MaterialTheme.typography.titleLarge, color = palette.text)
                    val info = Progression.levelInfo(progress.totalXp)
                    Text("Nível ${info.level} · ${Progression.title(info.level)}", color = palette.accent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    ProgressBar(info.fraction, Modifier.fillMaxWidth())
                    Text("${info.xpIntoLevel} / ${info.xpForNext} XP", color = palette.muted, fontSize = 13.sp)
                    Text("Toque para personalizar ›", color = palette.muted, fontSize = 12.sp)
                }
            }
        }

        // Sequência diária (004-US20) e convite para lembretes (US21-5)
        StreakHomeCard(progress, onStreak)
        if (progress.reminderEnabled && progress.streak.current >= 1 && !progress.notificationPromptDismissed) {
            NotificationPromptCard(onDismissNotif)
        }

        // Jornada
        GameCard(Modifier.fillMaxWidth(), color = palette.accent) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(palette.accent, lerp(palette.accent, Color.White, 0.22f))))
                    .padding(20.dp),
            ) {
                Column {
                    Text("JORNADA", color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Nível $next", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "${nextTheme.emoji} ${nextTheme.name} · ${nextCfg.size}×${nextCfg.size} · ${nextCfg.wordCount} palavras",
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onJourney,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = palette.accent),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(50.dp),
                        ) { Text("▶  Jogar", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = onLevels,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f), contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(50.dp),
                        ) { Text("Níveis") }
                        Spacer(Modifier.weight(1f))
                        Text("★ ${progress.totalStars}", color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                }
            }
        }

        // Passe de temporada (003a-US17)
        val season = SeasonPass.current(LocalDate.now())
        val passLevel = SeasonPass.levelFor(progress.season.xp)
        val claimable = SeasonPass.claimableCount(progress)
        GameCard(Modifier.fillMaxWidth(), onClick = onPass, color = lerp(palette.card, Gold, 0.16f)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎟️", fontSize = 28.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Passe · ${season.name}", style = MaterialTheme.typography.titleMedium, color = palette.text)
                        Text(
                            "Nível $passLevel/${SeasonPass.TIERS} · ${season.daysLeft(LocalDate.now().toEpochDay())} dias" +
                                if (progress.season.premium) " · ⭐ Premium" else "",
                            color = palette.muted, fontSize = 13.sp,
                        )
                    }
                    if (claimable > 0) Pill("🎁 $claimable", bg = Gold, fg = Color(0xFF3A2A00))
                    else Text("  ›", fontSize = 28.sp, color = palette.muted)
                }
                Spacer(Modifier.height(8.dp))
                ProgressBar(SeasonPass.progressInTier(progress.season.xp), Modifier.fillMaxWidth(), height = 8.dp, color = Gold)
            }
        }

        // Diário
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM"))
        ModeCard(
            emoji = "📅",
            title = "Desafio Diário · $today",
            subtitle = if (dailyDone) "Concluído ✓ — volte amanhã" else "XP ×1,5 + bônus de sequência + 30 🪙",
            badge = if (progress.streak.current > 0) "🔥 ${progress.streak.current}" else null,
            enabled = !dailyDone,
            onClick = onDaily,
        )

        // Contra o tempo
        ModeCard(
            emoji = if (taUnlocked) "⏱️" else "🔒",
            title = "Contra o Tempo",
            subtitle = if (taUnlocked) {
                if (progress.bestTimeAttackWords > 0) "Recorde: ${progress.bestTimeAttackWords} palavras" else "90 s · +5 s por palavra"
            } else {
                "Desbloqueia no nível ${Progression.TIME_ATTACK_UNLOCK_LEVEL}"
            },
            badge = null,
            enabled = taUnlocked,
            onClick = onTimeAttack,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SmallTile("🧑‍🎤", "Visual", "${progress.inventory.size}/${CosmeticCatalog.items.size}", Modifier.weight(1f), onCharacter)
            SmallTile("🏆", "Conquistas", "${progress.achievements.size}/${Achievements.all.size}", Modifier.weight(1f), onAchievements)
            SmallTile("🎨", "Temas", Progression.palettes.count { playerLevel >= it.requiredLevel }.let { "$it/${Progression.palettes.size}" }, Modifier.weight(1f), onThemes)
            SmallTile("📊", "Perfil", "Stats", Modifier.weight(1f), onProfile)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ModeCard(emoji: String, title: String, subtitle: String, badge: String?, enabled: Boolean, onClick: () -> Unit) {
    val palette = LocalPalette.current
    GameCard(Modifier.fillMaxWidth(), onClick = if (enabled) onClick else null) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(palette.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 26.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = if (enabled) palette.text else palette.muted)
                Text(subtitle, color = palette.muted, fontSize = 14.sp)
            }
            if (badge != null) Pill(badge, bg = Color(0xFFFFE0B2), fg = Color(0xFFB34700))
            if (enabled) Text("  ›", fontSize = 28.sp, color = palette.muted)
        }
    }
}

@Composable
private fun SmallTile(emoji: String, title: String, sub: String, modifier: Modifier, onClick: () -> Unit) {
    val palette = LocalPalette.current
    GameCard(modifier, onClick = onClick) {
        Column(Modifier.padding(vertical = 14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 26.sp)
            Text(title, fontWeight = FontWeight.Bold, color = palette.text, fontSize = 14.sp)
            Text(sub, color = palette.muted, fontSize = 12.sp)
        }
    }
}
