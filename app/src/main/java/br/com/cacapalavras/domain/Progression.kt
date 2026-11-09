package br.com.cacapalavras.domain

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/** Implementação de `specs/001-caca-palavras/contracts/progressao-xp.md`. */
object Progression {
    const val COMBO_WINDOW_MS = 6_000L
    const val NO_HINT_BONUS = 30
    const val SPEED_BONUS = 20
    const val DAILY_MULTIPLIER = 1.5
    const val DAILY_COINS = 30
    const val BOARD_CLEAR_XP = 25
    const val TIME_ATTACK_UNLOCK_LEVEL = 5
    /** Dica de inicial da Palavra Secreta (002-US12). */
    const val HINT_COST = 25
    /** Só níveis com esta faixa ou maior contam para o recorde de velocidade (conquista Relâmpago). */
    const val FAST_LEVEL_MIN_TIER = 2

    // §1
    fun comboMultiplier(combo: Int): Double = when {
        combo >= 4 -> 2.0
        combo == 3 -> 1.5
        combo == 2 -> 1.2
        else -> 1.0
    }

    fun wordXp(letters: Int, combo: Int, secret: Boolean): Int =
        ((10 + 2 * letters) * comboMultiplier(combo)).roundToInt() * (if (secret) 2 else 1)

    // §2
    fun completionXp(tier: Int, isReplay: Boolean): Int {
        val base = 40 + 5 * tier.coerceIn(0, 5)
        return if (isReplay) base / 2 else base
    }

    // §5
    fun stars(hintsUsed: Int, elapsedSeconds: Int, parSeconds: Int): Int =
        max(1, 3 - (if (hintsUsed > 0) 1 else 0) - (if (elapsedSeconds > parSeconds) 1 else 0))

    // §6
    fun completionCoins(stars: Int, mode: GameMode): Int =
        10 + 5 * stars + if (mode == GameMode.DAILY) DAILY_COINS else 0

    // §7
    fun xpToNext(level: Int): Long = floor(100.0 * level.toDouble().pow(1.4)).toLong()

    fun levelUpCoins(level: Int): Int = 20 + 5 * level

    data class LevelInfo(val level: Int, val xpIntoLevel: Long, val xpForNext: Long) {
        val fraction: Float get() = (xpIntoLevel.toFloat() / xpForNext).coerceIn(0f, 1f)
    }

    fun levelInfo(totalXp: Long): LevelInfo {
        var level = 1
        var remaining = totalXp
        while (remaining >= xpToNext(level)) {
            remaining -= xpToNext(level)
            level++
        }
        return LevelInfo(level, remaining, xpToNext(level))
    }

    // §8
    fun title(level: Int): String = when {
        level >= 50 -> "Lenda"
        level >= 35 -> "Grão-Mestre"
        level >= 20 -> "Mestre das Letras"
        level >= 10 -> "Caçador de Palavras"
        level >= 5 -> "Explorador"
        else -> "Aprendiz"
    }

    // §9
    data class Palette(val id: String, val name: String, val requiredLevel: Int)

    val palettes = listOf(
        Palette("classic", "Clássico", 1),
        Palette("ocean", "Oceano", 3),
        Palette("forest", "Floresta", 8),
        Palette("night", "Noite", 12),
        Palette("sunset", "Pôr do Sol", 15),
        Palette("gold", "Dourado", 20),
    )

    fun unlocksAt(level: Int): List<String> = buildList {
        palettes.filter { it.requiredLevel == level && level > 1 }.forEach { add("Tema ${it.name}") }
        if (level == TIME_ATTACK_UNLOCK_LEVEL) add("Modo Contra o Tempo")
    }

    // ---------- Aplicação de um resultado ----------

    data class XpLine(val label: String, val xp: Int)

    data class RewardSummary(
        val lines: List<XpLine>,
        val totalXp: Int,
        val coins: Int,
        val stars: Int?,
        val levelBefore: Int,
        val levelAfter: Int,
        val totalXpBefore: Long,
        val totalXpAfter: Long,
        val unlocks: List<String>,
        val achievements: List<Achievement>,
        val newRecord: Boolean,
        val streak: Int,
    ) {
        val leveledUp: Boolean get() = levelAfter > levelBefore
    }

    /** Linhas de XP do resultado (§2, §3, §4). */
    fun breakdown(result: LevelResult, streak: Int): List<XpLine> {
        val lines = mutableListOf(XpLine("Palavras", result.wordXp))
        when (result.mode) {
            GameMode.TIME_ATTACK -> {
                if (result.boardsCleared > 0) {
                    lines += XpLine("Tabuleiros limpos ×${result.boardsCleared}", BOARD_CLEAR_XP * result.boardsCleared)
                }
            }
            GameMode.JOURNEY, GameMode.DAILY -> {
                lines += XpLine(
                    if (result.isReplay) "Conclusão (rejogada)" else "Conclusão",
                    completionXp(result.tier, result.isReplay),
                )
                if (result.hintsUsed == 0) lines += XpLine("Sem dicas", NO_HINT_BONUS)
                if (result.elapsedSeconds <= result.parSeconds) lines += XpLine("Velocidade", SPEED_BONUS)
                if (result.mode == GameMode.DAILY) {
                    val sum = lines.sumOf { it.xp }
                    lines += XpLine("Desafio diário ×1,5", (sum * DAILY_MULTIPLIER).roundToInt() - sum)
                    lines += XpLine("Sequência de ${min(streak, 7)} dia(s)", 10 * min(streak, 7))
                }
            }
        }
        return lines
    }

    fun applyResult(progress: PlayerProgress, result: LevelResult, todayEpochDay: Long): Pair<PlayerProgress, RewardSummary> {
        val before = levelInfo(progress.totalXp).level
        var p = progress

        // Sequência diária (004): já registrada por DailyStreak.register antes desta chamada.
        // Aqui só marca o Desafio Diário como feito e usa a sequência no bônus (§3).
        val streak = max(1, p.streak.current)
        if (result.mode == GameMode.DAILY) p = p.copy(lastDailyEpochDay = todayEpochDay)

        val lines = breakdown(result, streak)
        val levelXp = lines.sumOf { it.xp }

        val stars: Int?
        var coins: Int
        var newRecord = false
        if (result.mode == GameMode.TIME_ATTACK) {
            stars = null
            coins = result.wordsFound
            if (result.wordsFound > p.bestTimeAttackWords) {
                newRecord = true
                p = p.copy(bestTimeAttackWords = result.wordsFound)
            }
        } else {
            stars = stars(result.hintsUsed, result.elapsedSeconds, result.parSeconds)
            coins = completionCoins(stars, result.mode)
            p = p.copy(
                levelsCompleted = p.levelsCompleted + 1,
                noHintLevels = p.noHintLevels + if (result.hintsUsed == 0) 1 else 0,
                fastestLevelSeconds = when {
                    result.tier < FAST_LEVEL_MIN_TIER -> p.fastestLevelSeconds
                    p.fastestLevelSeconds == 0 -> result.elapsedSeconds
                    else -> min(p.fastestLevelSeconds, result.elapsedSeconds)
                },
            )
            if (result.mode == GameMode.JOURNEY) {
                val best = max(p.starsByLevel[result.levelNumber] ?: 0, stars)
                p = p.copy(
                    starsByLevel = p.starsByLevel + (result.levelNumber to best),
                    journeyUnlocked = if (result.levelNumber == p.journeyUnlocked) p.journeyUnlocked + 1 else p.journeyUnlocked,
                )
            }
        }

        p = p.copy(
            totalXp = p.totalXp + levelXp,
            wordsFound = p.wordsFound + result.wordsFound,
            secretFound = p.secretFound + result.secretFound,
            maxComboReached = max(p.maxComboReached, result.maxCombo),
        )

        // Conquistas (podem gerar XP que gera nível que gera conquista…): laço até estabilizar.
        val unlockedAchievements = mutableListOf<Achievement>()
        var achievementXp = 0
        while (true) {
            val newOnes = Achievements.newlyUnlocked(p)
            if (newOnes.isEmpty()) break
            unlockedAchievements += newOnes
            achievementXp += newOnes.size * Achievements.XP_REWARD
            coins += newOnes.size * Achievements.COIN_REWARD
            p = p.copy(
                totalXp = p.totalXp + newOnes.size * Achievements.XP_REWARD,
                achievements = p.achievements + newOnes.map { it.id },
            )
        }

        val after = levelInfo(p.totalXp).level
        val unlocks = mutableListOf<String>()
        for (lv in (before + 1)..after) {
            coins += levelUpCoins(lv)
            unlocks += unlocksAt(lv)
        }
        p = p.copy(coins = p.coins + coins)

        val allLines = if (achievementXp > 0) lines + XpLine("Conquistas", achievementXp) else lines
        return p to RewardSummary(
            lines = allLines,
            totalXp = levelXp + achievementXp,
            coins = coins,
            stars = stars,
            levelBefore = before,
            levelAfter = after,
            totalXpBefore = progress.totalXp,
            totalXpAfter = p.totalXp,
            unlocks = unlocks,
            achievements = unlockedAchievements,
            newRecord = newRecord,
            streak = streak,
        )
    }
}

/** Resultado bruto de uma partida, entrada de [Progression.applyResult]. */
data class LevelResult(
    val mode: GameMode,
    val levelNumber: Int,
    /** Palavras encontradas pelo jogador (não conta as reveladas por dica). */
    val wordsFound: Int,
    val secretFound: Int,
    val hintsUsed: Int,
    val elapsedSeconds: Int,
    val parSeconds: Int,
    val tier: Int,
    val wordXp: Int,
    val maxCombo: Int,
    val boardsCleared: Int = 0,
    val isReplay: Boolean = false,
)
