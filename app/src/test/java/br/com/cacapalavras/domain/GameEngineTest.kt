package br.com.cacapalavras.domain

import br.com.cacapalavras.data.WordBank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    private val ids = WordBank.themes.map { it.id }

    private fun engine(level: Int = 4): GameEngine {
        val cfg = LevelDesigner.journey(level, ids)
        return GameEngine(PuzzleGenerator.generate(cfg, WordBank.byId(cfg.themeId)), cfg)
    }

    @Test fun encontraPalavraNosDoisSentidos() {
        val e = engine()
        val w = e.puzzle.words[0]
        val r = e.trySelection(w.cells.reversed(), 0)
        assertTrue(r is SelectionOutcome.Found)
        assertEquals(SelectionOutcome.AlreadyFound(w), e.trySelection(w.cells, 100))
    }

    @Test fun erroNaoPenaliza() {
        val e = engine()
        assertEquals(SelectionOutcome.Miss, e.trySelection(listOf(Cell(0, 0)), 0))
        assertEquals(0, e.found.size)
    }

    @Test fun comboSobeDentroDaJanelaEZeraFora() {
        val e = engine(level = 7)
        val ws = e.puzzle.words
        e.trySelection(ws[0].cells, 0)
        e.trySelection(ws[1].cells, 5_000)
        assertEquals(2, e.combo)
        e.trySelection(ws[2].cells, 10_000)
        assertEquals(3, e.combo)
        e.trySelection(ws[3].cells, 30_000)
        assertEquals(1, e.combo)
        assertEquals(3, e.maxCombo)
    }

    @Test fun xpDePalavraSegueContrato() {
        val e = engine(level = 1)
        val w = e.puzzle.words.first { !it.secret }
        val r = e.trySelection(w.cells, 0) as SelectionOutcome.Found
        assertEquals(10 + 2 * w.text.length, r.found.xp)
    }

    @Test fun palavraSecretaValeODobro() {
        val e = engine(level = 1)
        val s = e.secretWord!!
        val r = e.trySelection(s.cells, 0) as SelectionOutcome.Found
        assertEquals(2 * (10 + 2 * s.text.length), r.found.xp)
        assertEquals(1, e.secretFound)
    }

    // 002-US12
    @Test fun dicaDaSoAInicialDaSecretaEUmaVez() {
        val e = engine()
        assertTrue(e.canHint)
        assertEquals(e.secretWord!!.text.first(), e.hintSecretInitial())
        assertEquals(e.secretWord!!.text.first(), e.secretInitial)
        assertEquals(1, e.hintsUsed)
        assertTrue(e.found.isEmpty()) // nada revelado no quadro
        assertFalse(e.canHint)
        assertNull(e.hintSecretInitial()) // segunda vez não faz nada
        assertEquals(1, e.hintsUsed)
    }

    @Test fun dicaIndisponivelDepoisDeAcharASecreta() {
        val e = engine()
        e.trySelection(e.secretWord!!.cells, 0)
        assertFalse(e.canHint)
        assertNull(e.hintSecretInitial())
        assertEquals(0, e.hintsUsed)
    }

    @Test fun completaEGeraResultado() {
        val e = engine()
        var t = 0L
        e.puzzle.words.forEach { e.trySelection(it.cells, t); t += 1000 }
        assertTrue(e.isComplete)
        val r = e.result(elapsedSeconds = 42, isReplay = false)
        assertEquals(e.puzzle.words.size, r.wordsFound)
        assertEquals(0, r.hintsUsed)
        assertEquals(e.wordXp, r.wordXp)
    }
}
