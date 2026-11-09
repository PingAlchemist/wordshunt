package br.com.cacapalavras.domain

import kotlin.random.Random

/**
 * Gera tabuleiros determinísticos (Constituição III, FR-001/FR-002).
 */
object PuzzleGenerator {
    private const val MAX_ATTEMPTS = 30

    /** Frequência aproximada das letras no português (por mil), usada no preenchimento. */
    private val letterWeights: List<Pair<Char, Int>> = listOf(
        'A' to 130, 'E' to 115, 'O' to 98, 'S' to 75, 'R' to 63, 'I' to 60, 'N' to 50, 'D' to 48,
        'M' to 46, 'U' to 44, 'T' to 43, 'C' to 39, 'L' to 30, 'P' to 27, 'V' to 18, 'G' to 15,
        'H' to 13, 'Q' to 10, 'B' to 13, 'F' to 12, 'Z' to 7, 'J' to 6, 'X' to 4, 'K' to 1,
        'W' to 1, 'Y' to 1,
    )
    private val totalWeight = letterWeights.sumOf { it.second }

    fun generate(config: LevelConfig, theme: WordTheme): Puzzle {
        for (attempt in 0 until MAX_ATTEMPTS) {
            val rnd = Random(config.seed + attempt * 1_000_003L)
            tryGenerate(config, theme, rnd, minWords = config.wordCount)?.let { return it }
        }
        // Último recurso: aceita o que couber (nunca acontece com o banco atual, mas é seguro).
        return tryGenerate(config, theme, Random(config.seed), minWords = 1)
            ?: error("Tema ${theme.id} não tem palavras que caibam em ${config.size}x${config.size}")
    }

    private fun tryGenerate(config: LevelConfig, theme: WordTheme, rnd: Random, minWords: Int): Puzzle? {
        val size = config.size
        val grid = Array(size) { CharArray(size) { EMPTY } }
        val candidates = theme.words
            .map { it to TextNormalizer.normalize(it) }
            .filter { it.second.length in 3..size }
            .distinctBy { it.second }
            .shuffled(rnd)

        val chosen = mutableListOf<Pair<String, String>>()
        for (c in candidates) {
            if (chosen.size >= config.wordCount) break
            // Evita palavras contidas em outras (ex.: MAR dentro de MARTELO), que gerariam ambiguidade.
            val clash = chosen.any { (_, t) ->
                t.contains(c.second) || c.second.contains(t) ||
                    t.contains(c.second.reversed()) || c.second.contains(t.reversed())
            }
            if (!clash) chosen += c
        }

        val placed = mutableListOf<PlacedWord>()
        val directions = config.directions.toList()
        for ((display, text) in chosen.sortedByDescending { it.second.length }) {
            val options = mutableListOf<List<Cell>>()
            val overlapping = mutableListOf<List<Cell>>()
            for (dir in directions) for (r in 0 until size) for (col in 0 until size) {
                val cells = fit(grid, text, Cell(r, col), dir, size) ?: continue
                options += cells
                if (cells.indices.any { grid[cells[it].row][cells[it].col] == text[it] }) overlapping += cells
            }
            if (options.isEmpty()) continue
            val pool = if (overlapping.isNotEmpty() && rnd.nextInt(100) < 45) overlapping else options
            val cells = pool[rnd.nextInt(pool.size)]
            cells.forEachIndexed { i, cell -> grid[cell.row][cell.col] = text[i] }
            placed += PlacedWord(display = display, text = text, cells = cells)
        }
        if (placed.size < minWords) return null

        for (r in 0 until size) for (c in 0 until size) {
            if (grid[r][c] == EMPTY) grid[r][c] = randomLetter(rnd)
        }

        val secretIndex = if (config.withSecretWord) rnd.nextInt(placed.size) else -1
        val words = placed
            .mapIndexed { i, w -> if (i == secretIndex) w.copy(secret = true) else w }
            // Visíveis em ordem alfabética; a Palavra Secreta sempre por último (FR-010).
            .sortedWith(compareBy({ it.secret }, { it.text }))

        return Puzzle(
            size = size,
            grid = grid.map { String(it) },
            words = words,
            themeName = theme.name,
            seed = config.seed,
        )
    }

    private fun fit(grid: Array<CharArray>, text: String, start: Cell, dir: Direction, size: Int): List<Cell>? {
        val cells = ArrayList<Cell>(text.length)
        var c = start
        for (ch in text) {
            if (!c.inside(size)) return null
            val existing = grid[c.row][c.col]
            if (existing != EMPTY && existing != ch) return null
            cells += c
            c += dir
        }
        // Não permite sobrepor uma palavra inteira já colocada na mesma linha.
        if (cells.all { grid[it.row][it.col] != EMPTY }) return null
        return cells
    }

    private fun randomLetter(rnd: Random): Char {
        var x = rnd.nextInt(totalWeight)
        for ((ch, w) in letterWeights) {
            if (x < w) return ch
            x -= w
        }
        return 'A'
    }

    private const val EMPTY = ' '
}
