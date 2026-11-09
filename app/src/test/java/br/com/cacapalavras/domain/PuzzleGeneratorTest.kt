package br.com.cacapalavras.domain

import br.com.cacapalavras.data.WordBank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleGeneratorTest {
    private val ids = WordBank.themes.map { it.id }

    private fun gen(level: Int): Puzzle {
        val cfg = LevelDesigner.journey(level, ids)
        return PuzzleGenerator.generate(cfg, WordBank.byId(cfg.themeId))
    }

    @Test fun deterministico() {
        assertEquals(gen(17), gen(17))
    }

    @Test fun palavrasEstaoNaGradeNasCelulasDeclaradas() {
        for (level in 1..60) {
            val p = gen(level)
            val cfg = LevelDesigner.journey(level, ids)
            assertEquals("nível $level", cfg.wordCount, p.words.size)
            assertEquals(cfg.size, p.grid.size)
            p.grid.forEach { row -> assertTrue(row.all { it in 'A'..'Z' }) }
            for (w in p.words) {
                assertEquals("nível $level ${w.text}", w.text, p.textOf(w.cells))
                val dir = Selection.directionOf(w.cells)!!
                assertTrue("nível $level direção $dir", dir in cfg.directions)
                assertEquals(w.cells, Selection.line(w.cells.first(), w.cells.last(), p.size))
            }
        }
    }

    @Test fun todosOsTemasGeramEmTodosOsTamanhos() {
        for (theme in WordBank.themes) for (size in 6..12) {
            val cfg = LevelDesigner.journey(1, ids).copy(size = size, wordCount = LevelDesigner.wordCountFor((size - 6) * 4 + 1), directions = Direction.HARD)
            val p = PuzzleGenerator.generate(cfg, theme)
            assertEquals("${theme.id} ${size}x$size", cfg.wordCount, p.words.size)
        }
    }

    @Test fun palavraSecretaUnica() {
        for (level in 1..30) {
            val words = gen(level).words
            assertEquals("nível $level", 1, words.count { it.secret })
            assertTrue("secreta por último no nível $level", words.last().secret)
        }
    }

    @Test fun nenhumaPalavraContidaEmOutra() {
        for (level in 1..40) {
            val texts = gen(level).words.map { it.text }
            for (a in texts) for (b in texts) if (a != b) {
                assertTrue("$a ⊂ $b", !b.contains(a) && !b.contains(a.reversed()))
            }
        }
    }

    @Test fun geraRapido() {
        val cfg = LevelDesigner.journey(40, ids)
        val start = System.nanoTime()
        repeat(20) { PuzzleGenerator.generate(cfg.copy(seed = it.toLong()), WordBank.byId(cfg.themeId)) }
        val avgMs = (System.nanoTime() - start) / 20 / 1_000_000.0
        assertTrue("média $avgMs ms", avgMs < 50)
    }
}
