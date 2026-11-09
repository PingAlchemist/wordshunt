package br.com.cacapalavras.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementsTest {
    @Test fun dozeConquistasComIdsUnicos() {
        assertEquals(12, Achievements.all.size)
        assertEquals(12, Achievements.all.map { it.id }.toSet().size)
    }

    @Test fun nadaDesbloqueadoNoInicio() {
        assertTrue(Achievements.newlyUnlocked(PlayerProgress()).isEmpty())
    }

    @Test fun naoRepeteConquistaJaObtida() {
        val p = PlayerProgress(wordsFound = 1)
        assertEquals(listOf("first_word"), Achievements.newlyUnlocked(p).map { it.id })
        assertTrue(Achievements.newlyUnlocked(p.copy(achievements = setOf("first_word"))).isEmpty())
    }

    @Test fun relampagoSoComTempoValido() {
        val a = Achievements.all.first { it.id == "speed_45" }
        assertTrue(!a.isMet(PlayerProgress(fastestLevelSeconds = 0)))
        assertTrue(a.isMet(PlayerProgress(fastestLevelSeconds = 45)))
        assertTrue(!a.isMet(PlayerProgress(fastestLevelSeconds = 46)))
    }

    @Test fun nivelFacilRapidoNaoContaParaRelampago() {
        val easy = LevelResult(GameMode.JOURNEY, 1, 4, 0, 0, 8, 110, tier = 0, wordXp = 80, maxCombo = 1)
        val (p1, s1) = Progression.applyResult(PlayerProgress(), easy, 100)
        assertEquals(0, p1.fastestLevelSeconds)
        assertTrue(s1.achievements.none { it.id == "speed_45" })
        val hard = easy.copy(levelNumber = 12, tier = 2, elapsedSeconds = 40)
        val (p2, s2) = Progression.applyResult(p1, hard, 100)
        assertEquals(40, p2.fastestLevelSeconds)
        assertTrue(s2.achievements.any { it.id == "speed_45" })
    }
}
