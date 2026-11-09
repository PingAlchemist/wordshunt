package br.com.cacapalavras.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {
    @Test fun removeAcentosECedilha() {
        assertEquals("MACA", TextNormalizer.normalize("Maçã"))
        assertEquals("CORACAO", TextNormalizer.normalize("Coração"))
        assertEquals("ONIBUS", TextNormalizer.normalize("Ônibus"))
    }

    @Test fun removeHifenEEspaco() {
        assertEquals("ARCOIRIS", TextNormalizer.normalize("Arco-íris"))
        assertEquals("COPODELEITE", TextNormalizer.normalize("Copo de leite"))
    }
}
