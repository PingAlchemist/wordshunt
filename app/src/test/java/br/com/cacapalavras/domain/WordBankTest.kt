package br.com.cacapalavras.domain

import br.com.cacapalavras.data.WordBank
import org.junit.Assert.assertTrue
import org.junit.Test

class WordBankTest {
    @Test fun temasSuficientes() {
        assertTrue(WordBank.themes.size >= 18)
        assertTrue(WordBank.themes.map { it.id }.toSet().size == WordBank.themes.size)
    }

    @Test fun palavrasValidas() {
        for (t in WordBank.themes) {
            val norm = t.words.map { TextNormalizer.normalize(it) }
            assertTrue("${t.id} tem ${norm.size}", norm.toSet().size >= 20)
            norm.forEachIndexed { i, w -> assertTrue("${t.id}: '${t.words[i]}' → $w", w.length in 3..12 && w.all { it in 'A'..'Z' }) }
        }
    }
}
