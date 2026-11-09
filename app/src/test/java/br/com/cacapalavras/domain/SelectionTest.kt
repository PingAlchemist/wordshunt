package br.com.cacapalavras.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SelectionTest {
    @Test fun horizontal() {
        assertEquals((0..3).map { Cell(2, it) }, Selection.line(Cell(2, 0), Cell(2, 3), 8))
    }

    @Test fun diagonalExata() {
        assertEquals((0..3).map { Cell(it, it) }, Selection.line(Cell(0, 0), Cell(3, 3), 8))
    }

    @Test fun snapParaDiagonalQuandoQuaseDiagonal() {
        // alvo (3,4) está a ~37° → encaixa na diagonal SE com comprimento 4
        assertEquals((0..4).map { Cell(it, it) }, Selection.line(Cell(0, 0), Cell(3, 4), 8))
    }

    @Test fun snapParaHorizontalQuandoQuaseHorizontal() {
        assertEquals((0..5).map { Cell(1, it) }, Selection.line(Cell(1, 0), Cell(2, 5), 8))
    }

    @Test fun cortaNaBorda() {
        val line = Selection.line(Cell(6, 6), Cell(9, 9), 8)
        assertEquals(listOf(Cell(6, 6), Cell(7, 7)), line)
    }

    @Test fun direcaoInvertida() {
        assertEquals(listOf(Cell(3, 3), Cell(2, 2), Cell(1, 1)), Selection.line(Cell(3, 3), Cell(1, 1), 8))
    }

    @Test fun celulaUnica() {
        assertEquals(listOf(Cell(1, 1)), Selection.line(Cell(1, 1), Cell(1, 1), 8))
    }
}
