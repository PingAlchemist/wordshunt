package br.com.cacapalavras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cacapalavras.data.WordBank
import br.com.cacapalavras.domain.Achievements
import br.com.cacapalavras.domain.LevelDesigner
import br.com.cacapalavras.domain.PlayerProgress
import br.com.cacapalavras.domain.Progression
import br.com.cacapalavras.ui.components.GameCard
import br.com.cacapalavras.ui.components.Avatar
import br.com.cacapalavras.domain.Inventory
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import br.com.cacapalavras.ui.components.PlayerXpBlock
import br.com.cacapalavras.ui.components.ProgressBar
import br.com.cacapalavras.ui.components.ScreenHeader
import br.com.cacapalavras.ui.components.Stars
import br.com.cacapalavras.ui.theme.Gold
import br.com.cacapalavras.ui.theme.LocalPalette
import br.com.cacapalavras.ui.theme.Palettes

// ---------------- Seletor de níveis (US3) ----------------

@Composable
fun LevelsScreen(progress: PlayerProgress, onBack: () -> Unit, onPlay: (Int) -> Unit) {
    val palette = LocalPalette.current
    val ids = WordBank.themes.map { it.id }
    val shown = ((progress.journeyUnlocked + 11) / 12 + 1) * 12
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Jornada · ★ ${progress.totalStars}", onBack)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items((1..shown).toList()) { n ->
                val unlocked = n <= progress.journeyUnlocked
                val current = n == progress.journeyUnlocked
                val theme = WordBank.byId(LevelDesigner.journey(n, ids).themeId)
                GameCard(
                    Modifier.aspectRatio(0.82f).then(if (current) Modifier.border(3.dp, palette.accent, RoundedCornerShape(22.dp)) else Modifier),
                    onClick = if (unlocked) ({ onPlay(n) }) else null,
                    color = if (unlocked) palette.card else palette.card.copy(alpha = 0.5f),
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(if (unlocked) theme.emoji else "🔒", fontSize = 18.sp)
                        Text("$n", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = if (unlocked) palette.text else palette.muted)
                        if (unlocked && !current) Stars(progress.starsByLevel[n] ?: 0, size = 12)
                        if (current) Text("JOGAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = palette.accent)
                    }
                }
            }
        }
    }
}

// ---------------- Conquistas (US8) ----------------

@Composable
fun AchievementsScreen(progress: PlayerProgress, onBack: () -> Unit) {
    val palette = LocalPalette.current
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Conquistas · ${progress.achievements.size}/${Achievements.all.size}", onBack)
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(Achievements.all) { a ->
                val done = a.id in progress.achievements
                val (cur, goal) = a.progress(progress)
                GameCard(Modifier.fillMaxWidth(), color = if (done) lerp(palette.card, Gold, 0.25f) else palette.card) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(50.dp).clip(CircleShape).background(if (done) Gold.copy(alpha = 0.35f) else palette.muted.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) { Text(if (done) a.emoji else "🔒", fontSize = 24.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.name, fontWeight = FontWeight.Bold, color = palette.text)
                            Text(a.description, color = palette.muted, fontSize = 13.sp)
                            if (!done) {
                                Spacer(Modifier.height(6.dp))
                                ProgressBar((cur.toFloat() / goal).coerceIn(0f, 1f), Modifier.fillMaxWidth(), height = 6.dp)
                                Text("${cur.coerceAtMost(goal)} / $goal", color = palette.muted, fontSize = 11.sp)
                            }
                        }
                        Text(
                            if (done) "✓" else "+${Achievements.XP_REWARD} XP",
                            color = if (done) Color(0xFF2E7D32) else palette.accent, fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

// ---------------- Temas (US9) ----------------

@Composable
fun ThemesScreen(progress: PlayerProgress, onBack: () -> Unit, onSelect: (String) -> Unit) {
    val palette = LocalPalette.current
    val level = progress.level
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Temas", onBack)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Progression.palettes) { info ->
                val p = Palettes.byId(info.id)
                val unlocked = level >= info.requiredLevel
                val selected = progress.selectedPalette == info.id
                GameCard(
                    Modifier.then(if (selected) Modifier.border(3.dp, palette.accent, RoundedCornerShape(22.dp)) else Modifier),
                    onClick = if (unlocked) ({ onSelect(info.id) }) else null,
                ) {
                    Column {
                        Box(Modifier.fillMaxWidth().height(110.dp).background(p.background), contentAlignment = Alignment.Center) {
                            // Mini tabuleiro de amostra
                            Column(Modifier.clip(RoundedCornerShape(10.dp)).background(p.grid).padding(8.dp)) {
                                listOf("GATO", "AMOR", "SOLE").forEachIndexed { r, row ->
                                    Row {
                                        row.forEachIndexed { c, ch ->
                                            val hl = when {
                                                r == 0 -> p.highlight(0)
                                                c == 3 -> p.highlight(1)
                                                else -> Color.Transparent
                                            }
                                            Box(Modifier.size(22.dp).background(hl.copy(alpha = if (hl == Color.Transparent) 0f else 0.45f)), contentAlignment = Alignment.Center) {
                                                Text("$ch", color = p.text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            if (!unlocked) {
                                Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                                    Text("🔒 Nível ${info.requiredLevel}", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(info.name, fontWeight = FontWeight.Bold, color = palette.text, modifier = Modifier.weight(1f))
                            if (selected) Text("✓", color = palette.accent, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- Perfil (US10) ----------------

@Composable
fun ProfileScreen(
    progress: PlayerProgress,
    onBack: () -> Unit,
    onHaptics: (Boolean) -> Unit,
    onReset: () -> Unit,
    onName: (String) -> Unit,
    onCharacter: () -> Unit,
) {
    val palette = LocalPalette.current
    var confirmReset by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(progress.playerName) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Perfil", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Personagem em destaque (003a-US13)
            GameCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth(0.62f).clip(RoundedCornerShape(24.dp))) {
                        Avatar(Inventory.appearanceOf(progress), Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = name, onValueChange = { name = it.take(18) }, singleLine = true,
                        label = { Text("Nome do jogador") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onName(name) }),
                        trailingIcon = { if (name != progress.playerName) TextButton(onClick = { onName(name) }) { Text("Salvar") } },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(onClick = onCharacter) { Text("🧑‍🎤 Personalizar visual ›") }
                    PlayerXpBlock(progress.totalXp)
                    Spacer(Modifier.height(8.dp))
                    Text("XP total: ${progress.totalXp} · 🪙 ${progress.coins} · 🧠 ${progress.minds}", color = palette.muted)
                }
            }
            GameCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Estatísticas", style = MaterialTheme.typography.titleMedium, color = palette.text)
                    Stat("📝 Palavras encontradas", "${progress.wordsFound}")
                    Stat("🔒 Palavras secretas", "${progress.secretFound}")
                    Stat("🧭 Níveis da Jornada", "${progress.journeyCompleted}")
                    Stat("★ Estrelas", "${progress.totalStars}")
                    Stat("🧠 Níveis sem dica", "${progress.noHintLevels}")
                    Stat("🔥 Sequência diária (atual / recorde)", "${progress.streak.current} / ${progress.streak.best}")
                    Stat("⏱️ Recorde Contra o Tempo", "${progress.bestTimeAttackWords} palavras")
                    Stat("⚡ Nível difícil mais rápido", if (progress.fastestLevelSeconds > 0) "${progress.fastestLevelSeconds} s" else "—")
                    Stat("🏆 Conquistas", "${progress.achievements.size}/${Achievements.all.size}")
                    Stat("🧑‍🎤 Itens de visual", "${progress.inventory.size}/${br.com.cacapalavras.domain.CosmeticCatalog.items.size}")
                    Stat("🎟️ Temporadas jogadas", "${progress.seasonHistory.size + 1}")
                }
            }
            GameCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Vibração", color = palette.text, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Switch(checked = progress.hapticsEnabled, onCheckedChange = onHaptics)
                }
            }
            TextButton(onClick = { confirmReset = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Apagar progresso", color = Color(0xFFD32F2F))
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Apagar todo o progresso?") },
            text = { Text("XP, nível, moedas, estrelas e conquistas voltam ao início. Não dá para desfazer.") },
            confirmButton = { TextButton(onClick = { confirmReset = false; onReset() }) { Text("Apagar", color = Color(0xFFD32F2F)) } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Stat(label: String, value: String) {
    val palette = LocalPalette.current
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = palette.text, modifier = Modifier.weight(1f))
        Text(value, color = palette.text, fontWeight = FontWeight.Bold)
    }
}
