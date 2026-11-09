package br.com.cacapalavras.domain

import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlin.math.max

/** Estado persistido da sequência diária (004-US20). */
@Serializable
data class StreakState(
    val current: Int = 0,
    val best: Int = 0,
    val lastActiveEpochDay: Long = -1,
    val shields: Int = 0,
    /** Últimos dias ativos (epoch day), para o calendário. */
    val activeDays: List<Long> = emptyList(),
    /** Marcos já premiados (3, 7, 14…). */
    val milestones: Set<Int> = emptySet(),
)

/** O que aconteceu ao registrar um dia ativo (mostrado na tela de resultado). */
data class StreakUpdate(
    val newDay: Boolean,
    val current: Int,
    val rewards: List<Reward> = emptyList(),
    val rewardText: List<String> = emptyList(),
    val milestoneItem: CosmeticItem? = null,
    val shieldsUsed: Int = 0,
    val shieldEarned: Boolean = false,
    val halvedFrom: Int? = null,
)

enum class StreakStatus { PLAYED_TODAY, AT_RISK, MISSED_DAYS, NOT_STARTED }

object DailyStreak {
    const val MAX_SHIELDS = 2
    val milestoneDays = listOf(3, 7, 14, 30, 60, 100)

    /** Ciclo de 7 dias (US20-3). O 7º é o baú. */
    val cycle: List<List<Reward>> = listOf(
        listOf(Reward.Coins(30)),
        listOf(Reward.Coins(50)),
        listOf(Reward.Coins(75)),
        listOf(Reward.Minds(10)),
        listOf(Reward.Coins(100)),
        listOf(Reward.Coins(150)),
        listOf(Reward.Coins(200), Reward.Minds(25)),
    )

    fun cycleDay(streakDay: Int): Int = ((streakDay - 1).coerceAtLeast(0) % 7) + 1
    fun rewardsFor(streakDay: Int): List<Reward> = cycle[cycleDay(streakDay) - 1]

    fun milestoneItem(days: Int): CosmeticItem? =
        CosmeticCatalog.items.firstOrNull { it.source == ItemSource.Streak(days) }

    fun nextMilestone(current: Int): Int? = milestoneDays.firstOrNull { it > current }

    fun status(s: StreakState, today: LocalDate): StreakStatus {
        val d = today.toEpochDay()
        return when {
            s.lastActiveEpochDay == d -> StreakStatus.PLAYED_TODAY
            s.current == 0 || s.lastActiveEpochDay < 0 -> StreakStatus.NOT_STARTED
            d - s.lastActiveEpochDay == 1L -> StreakStatus.AT_RISK
            else -> StreakStatus.MISSED_DAYS
        }
    }

    fun missedDays(s: StreakState, today: LocalDate): Int =
        if (s.lastActiveEpochDay < 0) 0 else (today.toEpochDay() - s.lastActiveEpochDay - 1).toInt().coerceAtLeast(0)

    /** Quanto a sequência valerá se o jogador jogar hoje (para a UI explicar antes). */
    fun projectedIfPlayToday(s: StreakState, today: LocalDate): Int {
        if (s.lastActiveEpochDay == today.toEpochDay()) return s.current
        if (s.current == 0) return 1
        val missed = missedDays(s, today)
        return when {
            missed == 0 -> s.current + 1
            s.shields >= missed -> s.current + 1
            else -> s.current / 2 + 1
        }
    }

    /**
     * Registra um dia ativo (US20-1). Idempotente no mesmo dia; ignora relógio voltando.
     * Dias perdidos: usa protetores; sem protetores suficientes, a sequência cai pela metade (não zera)
     * e os protetores ficam guardados.
     */
    fun register(p: PlayerProgress, today: LocalDate): Pair<PlayerProgress, StreakUpdate> {
        val s = p.streak
        val d = today.toEpochDay()
        if (s.lastActiveEpochDay >= d) return p to StreakUpdate(newDay = false, current = s.current)

        var shields = s.shields
        var used = 0
        var halvedFrom: Int? = null
        val cur: Int = if (s.current == 0 || s.lastActiveEpochDay < 0) 1 else {
            val missed = (d - s.lastActiveEpochDay - 1).toInt()
            when {
                missed <= 0 -> s.current + 1
                shields >= missed -> { shields -= missed; used = missed; s.current + 1 }
                else -> { halvedFrom = s.current; s.current / 2 + 1 }
            }
        }
        val earned = cur % 7 == 0 && shields < MAX_SHIELDS
        if (earned) shields++

        var out = p
        val rewards = rewardsFor(cur)
        val texts = mutableListOf<String>()
        for (r in rewards) {
            out = when (r) {
                is Reward.Coins -> out.copy(coins = out.coins + r.amount).also { texts += "+${r.amount} 🪙" }
                is Reward.Minds -> out.copy(minds = out.minds + r.amount).also { texts += "+${r.amount} 🧠" }
                is Reward.Item -> out
            }
        }
        var milestones = s.milestones
        var item: CosmeticItem? = null
        if (cur in milestoneDays && cur !in milestones) {
            milestones = milestones + cur
            item = milestoneItem(cur)
            if (item != null) out = Inventory.grantItem(out, item).first
        }
        val state = StreakState(
            current = cur,
            best = max(s.best, cur),
            lastActiveEpochDay = d,
            shields = shields,
            activeDays = (s.activeDays + d).distinct().takeLast(60),
            milestones = milestones,
        )
        return out.copy(streak = state) to StreakUpdate(true, cur, rewards, texts, item, used, earned, halvedFrom)
    }

    /** Migra a sequência antiga do Desafio Diário (v1.0) para o novo sistema (US20-8). */
    fun migrateLegacy(p: PlayerProgress): PlayerProgress {
        if (p.streak.best > 0 || (p.dailyStreak == 0 && p.bestDailyStreak == 0)) return p
        return p.copy(
            streak = StreakState(
                current = p.dailyStreak,
                best = max(p.bestDailyStreak, p.dailyStreak),
                lastActiveEpochDay = p.lastDailyEpochDay,
                activeDays = if (p.lastDailyEpochDay >= 0) listOf(p.lastDailyEpochDay) else emptyList(),
            ),
        )
    }

    // ---------------- Textos dos lembretes (US21-3) ----------------

    enum class ReminderKind { DAILY, LAST_CHANCE }

    /** Título e texto da notificação, ou null se não deve notificar (já jogou hoje / nada em risco). */
    fun reminderText(p: PlayerProgress, today: LocalDate, kind: ReminderKind): Pair<String, String>? {
        val s = p.streak
        val st = status(s, today)
        if (st == StreakStatus.PLAYED_TODAY) return null
        if (kind == ReminderKind.LAST_CHANCE && (st == StreakStatus.NOT_STARTED || s.current < 1)) return null
        val next = projectedIfPlayToday(s, today)
        val reward = rewardsFor(next).joinToString(" + ") { SeasonPass.describe(it) }
        val chest = if (cycleDay(next) == 7) " 🎁 Hoje tem baú!" else ""
        return when {
            st == StreakStatus.NOT_STARTED -> "🧩 Bora começar uma sequência?" to "Uma fase rápida já conta. Hoje vale $reward."
            kind == ReminderKind.LAST_CHANCE -> "⏰ Última chance hoje!" to
                "Sua sequência de 🔥 ${s.current} dia(s) termina à meia-noite. Uma fase basta.$chest"
            st == StreakStatus.MISSED_DAYS && s.shields >= missedDays(s, today) ->
                "🛡️ Seu protetor salvou a sequência!" to "Jogue hoje para continuar com 🔥 $next dias e ganhar $reward."
            st == StreakStatus.MISSED_DAYS -> "🔥 Volte para a sua sequência" to
                "Jogando hoje você mantém 🔥 $next dias (metade, não zera!) e ganha $reward."
            else -> "🔥 Sua sequência de ${s.current} dias está em risco!" to
                "Jogue 1 fase hoje para chegar a $next dias e ganhar $reward.$chest"
        }
    }
}
