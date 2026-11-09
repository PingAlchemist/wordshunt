package br.com.cacapalavras.domain

data class FoundWord(
    val word: PlacedWord,
    /** Células a destacar (as que o jogador selecionou). */
    val cells: List<Cell>,
    val colorIndex: Int,
    val xp: Int,
    val combo: Int,
)

sealed interface SelectionOutcome {
    data class Found(val found: FoundWord, val complete: Boolean) : SelectionOutcome
    data class AlreadyFound(val word: PlacedWord) : SelectionOutcome
    data object Miss : SelectionOutcome
}

/** Estado de um tabuleiro em jogo (US1, US2, US4). Sem Android: o tempo vem por parâmetro. */
class GameEngine(val puzzle: Puzzle, val config: LevelConfig, private val colorOffset: Int = 0) {
    private val _found = mutableListOf<FoundWord>()
    val found: List<FoundWord> get() = _found

    var hintsUsed = 0; private set
    var combo = 0; private set
    var maxCombo = 0; private set
    var wordXp = 0; private set
    private var lastFoundAtMs: Long? = null

    val remaining: List<PlacedWord> get() = puzzle.words.filter { w -> _found.none { it.word == w } }
    val isComplete: Boolean get() = _found.size == puzzle.words.size
    val foundByPlayer: Int get() = _found.size
    val secretFound: Int get() = _found.count { it.word.secret }

    fun isFound(word: PlacedWord) = _found.any { it.word == word }

    /** Combo cai se passou da janela desde a última palavra. */
    fun comboAt(nowMs: Long): Int {
        val last = lastFoundAtMs ?: return 0
        return if (nowMs - last <= Progression.COMBO_WINDOW_MS) combo else 0
    }

    fun trySelection(cells: List<Cell>, nowMs: Long): SelectionOutcome {
        if (cells.size < 2) return SelectionOutcome.Miss
        val text = puzzle.textOf(cells)
        val reversed = text.reversed()
        val candidates = puzzle.words.filter { it.text == text || it.text == reversed }
        if (candidates.isEmpty()) return SelectionOutcome.Miss
        val match = candidates.firstOrNull { !isFound(it) }
            ?: return SelectionOutcome.AlreadyFound(candidates.first())

        combo = if (comboAt(nowMs) > 0) combo + 1 else 1
        maxCombo = maxOf(maxCombo, combo)
        lastFoundAtMs = nowMs
        val xp = Progression.wordXp(match.text.length, combo, match.secret)
        wordXp += xp
        val fw = FoundWord(match, cells, colorOffset + _found.size, xp, combo)
        _found += fw
        return SelectionOutcome.Found(fw, isComplete)
    }

    /** A Palavra Secreta do tabuleiro (002-US11), se houver. */
    val secretWord: PlacedWord? = puzzle.words.firstOrNull { it.secret }

    /** Inicial revelada pela dica, ou null se a dica ainda não foi usada. */
    var secretInitial: Char? = null; private set

    /** A dica só vale uma vez e só enquanto a secreta não foi encontrada (002-US12). */
    val canHint: Boolean get() = secretWord != null && secretInitial == null && !isFound(secretWord)

    /** Dica: devolve a inicial da Palavra Secreta. Não altera nada no quadro (FR-011). */
    fun hintSecretInitial(): Char? {
        if (!canHint) return null
        hintsUsed++
        secretInitial = secretWord!!.text.first()
        return secretInitial
    }

    fun result(elapsedSeconds: Int, isReplay: Boolean): LevelResult = LevelResult(
        mode = config.mode,
        levelNumber = config.levelNumber,
        wordsFound = foundByPlayer,
        secretFound = secretFound,
        hintsUsed = hintsUsed,
        elapsedSeconds = elapsedSeconds,
        parSeconds = config.parSeconds,
        tier = config.tier,
        wordXp = wordXp,
        maxCombo = maxCombo,
        isReplay = isReplay,
    )
}
