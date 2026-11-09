package br.com.cacapalavras.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {
    private fun result(
        mode: GameMode = GameMode.JOURNEY,
        level: Int = 1,
        words: Int = 4,
        hints: Int = 0,
        elapsed: Int = 50,
        par: Int = 110,
        tier: Int = 0,
        wordXp: Int = 80,
        combo: Int = 1,
        replay: Boolean = false,
        boards: Int = 0,
    ) = LevelResult(mode, level, words, 0, hints, elapsed, par, tier, wordXp, combo, boards, replay)

    // §1
    @Test fun xpPorPalavra() {
        assertEquals(20, Progression.wordXp(5, 1, false))
        assertEquals(24, Progression.wordXp(5, 2, false))
        assertEquals(30, Progression.wordXp(5, 3, false))
        assertEquals(40, Progression.wordXp(5, 4, false))
        assertEquals(40, Progression.wordXp(5, 9, false))
        assertEquals(80, Progression.wordXp(5, 4, true))
    }

    // §2
    @Test fun xpDeConclusao() {
        assertEquals(40, Progression.completionXp(0, false))
        assertEquals(65, Progression.completionXp(5, false))
        assertEquals(65, Progression.completionXp(9, false))
        assertEquals(20, Progression.completionXp(0, true))
    }

    // §5
    @Test fun estrelas() {
        assertEquals(3, Progression.stars(0, 50, 60))
        assertEquals(2, Progression.stars(1, 50, 60))
        assertEquals(2, Progression.stars(0, 61, 60))
        assertEquals(1, Progression.stars(3, 999, 60))
    }

    // §7
    @Test fun curvaDeNivel() {
        assertEquals(100, Progression.xpToNext(1))
        assertEquals(263, Progression.xpToNext(2))
        assertEquals(465, Progression.xpToNext(3))
        assertEquals(696, Progression.xpToNext(4))
        assertEquals(951, Progression.xpToNext(5))
        assertEquals(2511, Progression.xpToNext(10))
        assertEquals(6628, Progression.xpToNext(20))
    }

    @Test fun nivelDerivadoDoXp() {
        assertEquals(1, Progression.levelInfo(0).level)
        assertEquals(1, Progression.levelInfo(99).level)
        assertEquals(2, Progression.levelInfo(100).level)
        assertEquals(0, Progression.levelInfo(100).xpIntoLevel)
        assertEquals(3, Progression.levelInfo(363).level)
    }

    // §8
    @Test fun titulos() {
        assertEquals("Aprendiz", Progression.title(4))
        assertEquals("Explorador", Progression.title(5))
        assertEquals("Caçador de Palavras", Progression.title(10))
        assertEquals("Mestre das Letras", Progression.title(20))
        assertEquals("Grão-Mestre", Progression.title(35))
        assertEquals("Lenda", Progression.title(50))
    }

    // §9
    @Test fun desbloqueios() {
        assertEquals(listOf("Tema Oceano"), Progression.unlocksAt(3))
        assertEquals(listOf("Modo Contra o Tempo"), Progression.unlocksAt(5))
        assertTrue(Progression.unlocksAt(2).isEmpty())
    }

    @Test fun primeiraPartidaJaSobeDeNivel() {
        val (p, s) = Progression.applyResult(PlayerProgress(), result(), todayEpochDay = 100)
        // 80 palavras + 40 conclusão + 30 sem dica + 20 velocidade = 170, +50 da conquista "Primeiros Passos"
        assertEquals(listOf(80, 40, 30, 20, 50), s.lines.map { it.xp })
        assertEquals(220, s.totalXp)
        assertEquals(220L, p.totalXp)
        assertTrue(s.leveledUp)
        assertEquals(2, s.levelAfter)
        assertEquals(3, s.stars)
        // moedas: 100 inicial + 25 (3★) + 25 (conquista) + 30 (nível 2)
        assertEquals(180, p.coins)
        assertEquals(2, p.journeyUnlocked)
        assertEquals(mapOf(1 to 3), p.starsByLevel)
        assertTrue("first_word" in p.achievements)
    }

    @Test fun rejogarNaoAvancaJornadaEMantemMelhorEstrela() {
        val start = PlayerProgress(journeyUnlocked = 5, starsByLevel = mapOf(1 to 3), achievements = setOf("first_word"))
        val (p, s) = Progression.applyResult(start, result(hints = 1, replay = true), 100)
        assertEquals(5, p.journeyUnlocked)
        assertEquals(3, p.starsByLevel[1])
        assertEquals(2, s.stars)
        assertEquals(20, s.lines.first { it.label.startsWith("Conclusão") }.xp)
    }

    // §3
    @Test fun diarioMultiplicaESomaSequencia() {
        // A sequência (004) já foi registrada hoje: 3 dias.
        val start = PlayerProgress(streak = StreakState(current = 3, lastActiveEpochDay = 100), achievements = setOf("first_word"))
        val (p, s) = Progression.applyResult(start, result(mode = GameMode.DAILY, tier = 3), todayEpochDay = 100)
        // base = 80 + 55 + 30 + 20 = 185 → ×1,5 = 278 (arred.) → +93 ; sequência 3 → +30
        assertEquals(185 + 93 + 30, s.totalXp)
        assertEquals(100, p.lastDailyEpochDay)
        assertEquals(3, s.streak)
    }

    @Test fun bonusDeSequenciaLimitadoA7Dias() {
        val start = PlayerProgress(streak = StreakState(current = 25, lastActiveEpochDay = 100), achievements = setOf("first_word"))
        val (_, s) = Progression.applyResult(start, result(mode = GameMode.DAILY, tier = 3), todayEpochDay = 100)
        assertEquals(70, s.lines.last().xp)
    }

    // §4
    @Test fun contraOTempo() {
        val start = PlayerProgress(bestTimeAttackWords = 5, achievements = setOf("first_word"))
        val (p, s) = Progression.applyResult(start, result(mode = GameMode.TIME_ATTACK, words = 9, wordXp = 200, boards = 1), 100)
        assertEquals(225, s.totalXp)
        assertEquals(null, s.stars)
        assertTrue(s.newRecord)
        assertEquals(9, p.bestTimeAttackWords)
        assertEquals(0, p.levelsCompleted)
        assertFalse(Progression.applyResult(p, result(mode = GameMode.TIME_ATTACK, words = 3), 100).second.newRecord)
    }

    @Test fun conquistasEmCascata() {
        // Perto do nível 10: XP da conquista de palavras empurra para o nível 10, que libera "Veterano".
        val xpAtLevel10 = (1..9).sumOf { Progression.xpToNext(it) }
        val start = PlayerProgress(totalXp = xpAtLevel10 - 40, wordsFound = 99, achievements = setOf("first_word"))
        val (p, s) = Progression.applyResult(start, result(mode = GameMode.TIME_ATTACK, words = 1, wordXp = 0), 100)
        assertTrue("words_100" in p.achievements)
        assertTrue("level_10" in p.achievements)
        assertEquals(10, s.levelAfter)
    }
}
