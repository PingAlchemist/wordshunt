package br.com.cacapalavras.domain

data class Cell(val row: Int, val col: Int) {
    operator fun plus(d: Direction) = Cell(row + d.dRow, col + d.dCol)
    fun inside(size: Int) = row in 0 until size && col in 0 until size
}

enum class Direction(val dRow: Int, val dCol: Int) {
    E(0, 1), SE(1, 1), S(1, 0), SW(1, -1), W(0, -1), NW(-1, -1), N(-1, 0), NE(-1, 1);

    companion object {
        /** Ordem por octante de ângulo (0 = leste, sentido horário com linhas crescendo para baixo). */
        val byOctant = listOf(E, SE, S, SW, W, NW, N, NE)

        val EASY = setOf(E, S)
        val MEDIUM = setOf(E, S, SE, NE)
        val HARD = entries.toSet()
    }
}

enum class GameMode { JOURNEY, DAILY, TIME_ATTACK }

data class WordTheme(val id: String, val name: String, val emoji: String, val words: List<String>)

data class PlacedWord(
    /** Como aparece na lista (com acentos). */
    val display: String,
    /** Normalizado: A–Z, como aparece na grade. */
    val text: String,
    val cells: List<Cell>,
    val secret: Boolean = false,
)

data class Puzzle(
    val size: Int,
    val grid: List<String>,
    val words: List<PlacedWord>,
    val themeName: String,
    val seed: Long,
) {
    fun letterAt(cell: Cell): Char = grid[cell.row][cell.col]
    fun textOf(cells: List<Cell>): String = cells.joinToString("") { letterAt(it).toString() }
}

data class LevelConfig(
    val mode: GameMode,
    val levelNumber: Int,
    val size: Int,
    val wordCount: Int,
    val directions: Set<Direction>,
    val themeId: String,
    val seed: Long,
    /** Faixa de dificuldade 0..5 usada no XP de conclusão. */
    val tier: Int,
    val withSecretWord: Boolean,
) {
    /** Tempo-alvo em segundos (contrato §2). */
    val parSeconds: Int get() = 20 * wordCount + 5 * size
}
