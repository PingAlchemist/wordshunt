package br.com.cacapalavras.domain

import java.time.LocalDate
import kotlin.math.min

/** Curva de dificuldade (US3) e configuração dos modos Diário (US5) e Contra o Tempo (US6). */
object LevelDesigner {
    const val MIN_SIZE = 6
    const val MAX_SIZE = 12

    fun sizeFor(level: Int) = min(MIN_SIZE + (level - 1) / 4, MAX_SIZE)
    fun wordCountFor(level: Int) = min(4 + (level - 1) / 3, 12)
    fun directionsFor(level: Int): Set<Direction> = when {
        level <= 5 -> Direction.EASY
        level <= 14 -> Direction.MEDIUM
        else -> Direction.HARD
    }
    fun tierFor(level: Int) = min(5, (level - 1) / 5)

    fun journey(level: Int, themeIds: List<String>): LevelConfig {
        require(level >= 1)
        return LevelConfig(
            mode = GameMode.JOURNEY,
            levelNumber = level,
            size = sizeFor(level),
            wordCount = wordCountFor(level),
            directions = directionsFor(level),
            themeId = themeIds[(level - 1) % themeIds.size],
            seed = 7919L * level,
            tier = tierFor(level),
            withSecretWord = true,
        )
    }

    /** Mesmo tabuleiro para todos no mesmo dia (seed = yyyyMMdd). */
    fun daily(date: LocalDate, themeIds: List<String>): LevelConfig {
        val seed = date.year * 10_000L + date.monthValue * 100 + date.dayOfMonth
        return LevelConfig(
            mode = GameMode.DAILY,
            levelNumber = date.toEpochDay().toInt(),
            size = 10,
            wordCount = 10,
            directions = Direction.MEDIUM,
            themeId = themeIds[Math.floorMod(date.toEpochDay() * 7, themeIds.size.toLong()).toInt()],
            seed = seed,
            tier = 3,
            withSecretWord = true,
        )
    }

    fun timeAttackBoard(seed: Long, board: Int, themeIds: List<String>): LevelConfig = LevelConfig(
        mode = GameMode.TIME_ATTACK,
        levelNumber = board,
        size = 8,
        wordCount = 6,
        directions = Direction.MEDIUM,
        themeId = themeIds[Math.floorMod(seed + board, themeIds.size.toLong()).toInt()],
        seed = seed * 31 + board,
        tier = 0,
        withSecretWord = true,
    )
}
