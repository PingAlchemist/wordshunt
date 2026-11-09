package br.com.cacapalavras.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.roundToInt

object Selection {
    /**
     * Linha reta de [start] em direção a [target], "encaixada" na direção mais próxima entre as 8
     * possíveis e cortada nas bordas da grade (FR-004).
     */
    fun line(start: Cell, target: Cell, size: Int): List<Cell> {
        val dr = target.row - start.row
        val dc = target.col - start.col
        if (dr == 0 && dc == 0) return listOf(start)
        val angle = atan2(dr.toDouble(), dc.toDouble())
        val octant = ((angle / (PI / 4)).roundToInt() + 8) % 8
        val dir = Direction.byOctant[octant]
        val length = max(abs(dr), abs(dc))
        val cells = ArrayList<Cell>(length + 1)
        var c = start
        for (i in 0..length) {
            if (!c.inside(size)) break
            cells += c
            c += dir
        }
        return cells
    }

    /** Direção entre duas células já alinhadas, ou null. */
    fun directionOf(cells: List<Cell>): Direction? {
        if (cells.size < 2) return null
        val dr = (cells[1].row - cells[0].row)
        val dc = (cells[1].col - cells[0].col)
        return Direction.entries.firstOrNull { it.dRow == dr && it.dCol == dc }
    }
}
