package br.com.cacapalavras.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import br.com.cacapalavras.BuildConfig
import br.com.cacapalavras.domain.DailyStreak
import br.com.cacapalavras.domain.Inventory
import br.com.cacapalavras.domain.PlayerProgress
import br.com.cacapalavras.domain.SeasonPass
import br.com.cacapalavras.domain.StreakStatus
import br.com.cacapalavras.notifications.StreakReminders
import br.com.cacapalavras.ui.components.GameCard
import br.com.cacapalavras.ui.components.ProgressBar
import br.com.cacapalavras.ui.components.ScreenHeader
import br.com.cacapalavras.ui.theme.LocalPalette
import java.time.LocalDate

val FlameOrange = Color(0xFFFF6D00)
val FlameYellow = Color(0xFFFFC400)

/** Estado da permissão de notificação + ação para pedir (Android 13+) ou abrir as configurações. */
@Composable
fun rememberNotificationPermission(): Pair<Boolean, () -> Unit> {
    val ctx = LocalContext.current
    var granted by remember { mutableStateOf(StreakReminders.hasPermission(ctx)) }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.repeatOnLifecycle(Lifecycle.State.RESUMED) { granted = StreakReminders.hasPermission(ctx) }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = StreakReminders.hasPermission(ctx)
    }
    val request: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !granted) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ctx.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
    return granted to request
}

/** Chama animada com o número de dias. */
@Composable
fun StreakFlame(days: Int, active: Boolean, size: Int = 120) {
    val pulse by rememberInfiniteTransition(label = "flame").animateFloat(
        0.94f, 1.06f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "p",
    )
    Box(contentAlignment = Alignment.Center) {
        Text(
            "🔥", fontSize = size.sp,
            modifier = Modifier.graphicsLayer {
                val k = if (active) pulse else 1f
                scaleX = k; scaleY = k; alpha = if (active) 1f else 0.35f
            },
        )
        Text(
            "$days", fontSize = (size * 0.32f).sp, fontWeight = FontWeight.Black, color = Color.White,
            modifier = Modifier.padding(top = (size * 0.38f).dp),
        )
    }
}

@Composable
fun StreakScreen(
    progress: PlayerProgress,
    onBack: () -> Unit,
    onReminder: (Boolean) -> Unit,
    onHour: (Int) -> Unit,
    onLastChance: (Boolean) -> Unit,
    onTest: () -> Unit,
) {
    val palette = LocalPalette.current
    val today = LocalDate.now()
    val s = progress.streak
    val status = DailyStreak.status(s, today)
    val projected = DailyStreak.projectedIfPlayToday(s, today)
    val (granted, requestPermission) = rememberNotificationPermission()
    val look = Inventory.appearanceOf(progress)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Sequência diária", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ---- Chama e status
            GameCard(Modifier.fillMaxWidth(), color = Color.Transparent) {
                Column(
                    Modifier.background(Brush.verticalGradient(listOf(Color(0xFF2B1055), Color(0xFF7A1F3D), FlameOrange))).padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StreakFlame(s.current, status == StreakStatus.PLAYED_TODAY)
                    Text(
                        if (s.current == 1) "1 dia seguido" else "${s.current} dias seguidos",
                        color = Color.White, style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when (status) {
                            StreakStatus.PLAYED_TODAY -> "✅ Sequência garantida hoje! Volte amanhã para o dia ${s.current + 1}."
                            StreakStatus.AT_RISK -> "⚠️ Jogue 1 fase hoje para chegar a $projected dias."
                            StreakStatus.MISSED_DAYS ->
                                if (s.shields >= DailyStreak.missedDays(s, today)) "🛡️ Seu protetor cobre o(s) dia(s) perdido(s). Jogue hoje: $projected dias!"
                                else "Você perdeu ${DailyStreak.missedDays(s, today)} dia(s). Jogando hoje fica com $projected dias (metade, nunca zera)."
                            StreakStatus.NOT_STARTED -> "Conclua 1 fase hoje para acender a chama!"
                        },
                        color = Color.White.copy(alpha = 0.95f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("🏆 Recorde: ${s.best}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("🛡️ ${s.shields}/${DailyStreak.MAX_SHIELDS}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ---- Semana
            GameCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Últimos 7 dias", style = MaterialTheme.typography.titleMedium, color = palette.text)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val letters = listOf("S", "T", "Q", "Q", "S", "S", "D")
                        for (i in 6 downTo 0) {
                            val day = today.minusDays(i.toLong())
                            val active = day.toEpochDay() in s.activeDays
                            val isToday = i == 0
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(letters[day.dayOfWeek.value - 1], color = palette.muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    Modifier.size(38.dp).clip(CircleShape)
                                        .background(if (active) FlameOrange else palette.muted.copy(alpha = 0.15f))
                                        .border(if (isToday) 2.5.dp else 0.dp, if (isToday) FlameOrange else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) { Text(if (active) "🔥" else "${day.dayOfMonth}", fontSize = if (active) 18.sp else 13.sp, color = palette.muted) }
                            }
                        }
                    }
                }
            }

            // ---- Ciclo de recompensas
            GameCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Recompensa de cada dia", style = MaterialTheme.typography.titleMedium, color = palette.text)
                    Text("Ganha automaticamente ao concluir a 1ª fase do dia.", color = palette.muted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    val doneUpTo = if (status == StreakStatus.PLAYED_TODAY) DailyStreak.cycleDay(s.current) else DailyStreak.cycleDay(projected) - 1
                    val target = if (status == StreakStatus.PLAYED_TODAY) DailyStreak.cycleDay(s.current + 1) else DailyStreak.cycleDay(projected)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DailyStreak.cycle.forEachIndexed { i, rewards ->
                            val day = i + 1
                            val done = day <= doneUpTo
                            val next = day == target
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                    .background(if (done) lerp(palette.card, FlameOrange, 0.25f) else palette.muted.copy(alpha = 0.08f))
                                    .border(if (next) 2.dp else 0.dp, if (next) FlameOrange else Color.Transparent, RoundedCornerShape(10.dp))
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("D$day", fontSize = 11.sp, color = palette.muted, fontWeight = FontWeight.Bold)
                                Text(if (day == 7) "🎁" else if (rewards[0] is br.com.cacapalavras.domain.Reward.Minds) "🧠" else "🪙", fontSize = 20.sp)
                                Text(
                                    rewards.joinToString("+") { r ->
                                        when (r) {
                                            is br.com.cacapalavras.domain.Reward.Coins -> "${r.amount}"
                                            is br.com.cacapalavras.domain.Reward.Minds -> "${r.amount}🧠"
                                            is br.com.cacapalavras.domain.Reward.Item -> "item"
                                        }
                                    },
                                    fontSize = 10.sp, color = palette.text, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 11.sp,
                                )
                                if (done) Text("✓", fontSize = 11.sp, color = FlameOrange, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "🛡️ A cada 7 dias você ganha 1 protetor (máx. ${DailyStreak.MAX_SHIELDS}). Cada protetor salva 1 dia perdido. " +
                            "Sem protetor, a sequência cai pela metade — nunca zera.",
                        color = palette.muted, fontSize = 12.sp,
                    )
                }
            }

            // ---- Marcos
            GameCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Marcos com itens exclusivos", style = MaterialTheme.typography.titleMedium, color = palette.text)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DailyStreak.milestoneDays.forEach { days ->
                            val item = DailyStreak.milestoneItem(days)!!
                            val got = days in s.milestones
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box {
                                    ItemThumb(item, look, dim = !got)
                                    if (got) Text("✓", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp,
                                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).clip(CircleShape).background(FlameOrange).padding(horizontal = 5.dp))
                                }
                                Text("$days d", fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (got) FlameOrange else palette.text)
                            }
                        }
                    }
                    DailyStreak.nextMilestone(s.current)?.let { nm ->
                        Spacer(Modifier.height(8.dp))
                        Text("Próximo: ${DailyStreak.milestoneItem(nm)!!.name} em $nm dias", color = palette.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        ProgressBar(s.current.toFloat() / nm, Modifier.fillMaxWidth(), height = 8.dp, color = FlameOrange)
                    }
                }
            }

            // ---- Lembretes (US21)
            GameCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("🔔 Lembretes", style = MaterialTheme.typography.titleMedium, color = palette.text)
                            Text("Só avisamos se você ainda não jogou no dia.", color = palette.muted, fontSize = 12.sp)
                        }
                        Switch(checked = progress.reminderEnabled, onCheckedChange = {
                            onReminder(it)
                            if (it && !granted) requestPermission()
                        })
                    }
                    if (progress.reminderEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Text("Horário", color = palette.muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                            listOf(8, 12, 19, 21).forEach { h ->
                                val sel = progress.reminderHour == h
                                Text(
                                    "%02d:00".format(h),
                                    modifier = Modifier.clip(RoundedCornerShape(50)).background(if (sel) FlameOrange else palette.muted.copy(alpha = 0.12f))
                                        .clickable { onHour(h) }.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (sel) Color.White else palette.text, fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Aviso de última chance (22h)", color = palette.text, fontWeight = FontWeight.Bold)
                                Text("Se sua sequência ainda estiver em risco.", color = palette.muted, fontSize = 12.sp)
                            }
                            Switch(checked = progress.lastChanceEnabled, onCheckedChange = onLastChance)
                        }
                        if (!granted) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = requestPermission, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                            ) { Text("Permitir notificações") }
                            Text("Sem a permissão, os lembretes não aparecem.", color = palette.muted, fontSize = 12.sp)
                        }
                    }
                    if (BuildConfig.TEST_TOOLS) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { if (!granted) requestPermission() else onTest() }, shape = RoundedCornerShape(12.dp)) {
                            Text("🧪 Testar notificação agora")
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Card compacto da sequência para a Home. */
@Composable
fun StreakHomeCard(progress: PlayerProgress, onClick: () -> Unit) {
    val palette = LocalPalette.current
    val today = LocalDate.now()
    val s = progress.streak
    val status = DailyStreak.status(s, today)
    val atRisk = status == StreakStatus.AT_RISK || status == StreakStatus.MISSED_DAYS
    GameCard(Modifier.fillMaxWidth(), onClick = onClick, color = if (atRisk) lerp(palette.card, FlameOrange, 0.14f) else palette.card) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            StreakFlame(s.current, status == StreakStatus.PLAYED_TODAY, size = 44)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (s.current == 0) "Sequência diária" else "${s.current} dia(s) de sequência",
                    style = MaterialTheme.typography.titleMedium, color = palette.text,
                )
                val next = DailyStreak.projectedIfPlayToday(s, today)
                val reward = DailyStreak.rewardsFor(next).joinToString(" + ") { SeasonPass.describe(it) }
                Text(
                    when (status) {
                        StreakStatus.PLAYED_TODAY -> "✅ Garantida hoje · amanhã: ${DailyStreak.rewardsFor(s.current + 1).joinToString(" + ") { SeasonPass.describe(it) }}"
                        StreakStatus.NOT_STARTED -> "Conclua 1 fase hoje · ganhe $reward"
                        else -> "⚠️ Jogue hoje para manter · ganhe $reward"
                    },
                    color = if (atRisk) Color(0xFFD84315) else palette.muted, fontSize = 13.sp, fontWeight = if (atRisk) FontWeight.Bold else FontWeight.Normal,
                )
            }
            if (s.shields > 0) Text("🛡️${s.shields}", fontWeight = FontWeight.Bold, color = palette.text)
            Text("  ›", fontSize = 26.sp, color = palette.muted)
        }
    }
}

/** Convite para ativar notificações, mostrado na Home com contexto (US21-5). */
@Composable
fun NotificationPromptCard(onDismiss: () -> Unit) {
    val palette = LocalPalette.current
    val (granted, request) = rememberNotificationPermission()
    if (granted) return
    GameCard(Modifier.fillMaxWidth(), color = lerp(palette.card, FlameYellow, 0.18f)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🔔", fontSize = 28.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Não perca sua sequência", fontWeight = FontWeight.Bold, color = palette.text)
                Text("Avisamos só nos dias em que você ainda não jogou.", color = palette.muted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Button(onClick = request, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FlameOrange)) { Text("Ativar") }
                TextButton(onClick = onDismiss) { Text("Agora não", fontSize = 12.sp) }
            }
        }
    }
}
