package br.com.cacapalavras.domain

import java.text.Normalizer
import java.util.Locale

object TextNormalizer {
    private val marks = "\\p{M}+".toRegex()
    private val nonLetters = "[^A-Z]".toRegex()

    /** "Maçã-verde" → "MACAVERDE" (FR-003). */
    fun normalize(word: String): String =
        Normalizer.normalize(word, Normalizer.Form.NFD)
            .replace(marks, "")
            .uppercase(Locale.ROOT)
            .replace(nonLetters, "")
}
