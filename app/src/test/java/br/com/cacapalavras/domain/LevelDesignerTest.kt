package br.com.cacapalavras.domain

import br.com.cacapalavras.data.WordBank
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LevelDesignerTest {
    private val ids = WordBank.themes.map { it.id }

    @Test fun curvaDeTamanhoEPalavras() {
        assertEquals(6, LevelDesigner.sizeFor(1))
        assertEquals(7, LevelDesigner.sizeFor(5))
        assertEquals(12, LevelDesigner.sizeFor(25))
        assertEquals(12, LevelDesigner.sizeFor(500))
        assertEquals(4, LevelDesigner.wordCountFor(1))
        assertEquals(12, LevelDesigner.wordCountFor(100))
    }

    @Test fun direcoesPorFaixa() {
        assertEquals(Direction.EASY, LevelDesigner.directionsFor(5))
        assertEquals(Direction.MEDIUM, LevelDesigner.directionsFor(6))
        assertEquals(Direction.MEDIUM, LevelDesigner.directionsFor(14))
        assertEquals(Direction.HARD, LevelDesigner.directionsFor(15))
    }

    @Test fun diarioIgualNoMesmoDiaEDiferenteNoSeguinte() {
        val d = LocalDate.of(2026, 11, 8)
        assertEquals(LevelDesigner.daily(d, ids), LevelDesigner.daily(d, ids))
        assertEquals(20261108L, LevelDesigner.daily(d, ids).seed)
        assert(LevelDesigner.daily(d, ids).themeId != LevelDesigner.daily(d.plusDays(1), ids).themeId)
    }

    @Test fun tempoAlvo() {
        assertEquals(20 * 4 + 5 * 6, LevelDesigner.journey(1, ids).parSeconds)
    }
}
