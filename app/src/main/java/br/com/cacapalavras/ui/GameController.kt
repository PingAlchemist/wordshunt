package br.com.cacapalavras.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.cacapalavras.data.WordBank
import br.com.cacapalavras.domain.Cell
import br.com.cacapalavras.domain.FoundWord
import br.com.cacapalavras.domain.GameEngine
import br.com.cacapalavras.domain.GameMode
import br.com.cacapalavras.domain.LevelConfig
import br.com.cacapalavras.domain.LevelDesigner
import br.com.cacapalavras.domain.LevelResult
import br.com.cacapalavras.domain.Progression
import br.com.cacapalavras.domain.PuzzleGenerator
import br.com.cacapalavras.domain.Selection
import br.com.cacapalavras.domain.SelectionOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface GameRequest {
    data class Journey(val level: Int, val isReplay: Boolean) : GameRequest
    data class Daily(val date: LocalDate) : GameRequest
    data class TimeAttack(val seed: Long) : GameRequest
}

sealed interface GameEvent {
    data class Found(val word: FoundWord) : GameEvent
    data object Miss : GameEvent
    data object Tick : GameEvent
    data object BoardCleared : GameEvent
    data object Complete : GameEvent
}

/** "+XP" que sobe a partir do centro da palavra encontrada (coordenadas em fração da grade). */
data class Floater(val id: Long, val text: String, val x: Float, val y: Float, val secret: Boolean)

/**
 * Estado observável de uma partida. Fica no [AppViewModel] para sobreviver a recriações da Activity.
 * O relógio de jogo ([elapsedMs]) é a referência de tempo do combo, por isso pausar não quebra combos.
 */
class GameController(
    val request: GameRequest,
    private val scope: CoroutineScope,
    private val spendCoins: (Int) -> Boolean,
    private val onFinish: (LevelResult) -> Unit,
) {
    private val themeIds = WordBank.themes.map { it.id }

    var engine by mutableStateOf(newEngine(board = 0, colorOffset = 0)); private set
    val config: LevelConfig get() = engine.config

    // Snapshot do motor para a UI
    var found by mutableStateOf<List<FoundWord>>(emptyList()); private set
    var secretInitial by mutableStateOf<Char?>(null); private set
    var canHint by mutableStateOf(engine.canHint); private set
    var levelXp by mutableIntStateOf(0); private set

    var selection by mutableStateOf<List<Cell>>(emptyList()); private set
    var anchor by mutableStateOf<Cell?>(null); private set
    private var dragStart: Cell? = null

    var elapsedMs by mutableLongStateOf(0L); private set
    var timeLeftMs by mutableLongStateOf(TA_START_MS); private set
    var paused by mutableStateOf(false)
    var finished by mutableStateOf(false); private set

    val floaters = mutableStateListOf<Floater>()
    private var floaterId = 0L
    val events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 16)

    // Acumulado do Contra o Tempo
    var boardsCleared by mutableIntStateOf(0); private set
    private var taWords = 0
    private var taWordXp = 0
    private var taHints = 0
    private var taMaxCombo = 0
    var taTotalWords by mutableIntStateOf(0); private set

    private var timer: Job? = null

    val isTimeAttack get() = request is GameRequest.TimeAttack
    val title: String
        get() = when (request) {
            is GameRequest.Journey -> "Nível ${request.level}"
            is GameRequest.Daily -> "Desafio Diário"
            is GameRequest.TimeAttack -> "Contra o Tempo"
        }

    init {
        startTimer()
    }

    private fun newEngine(board: Int, colorOffset: Int): GameEngine {
        val cfg = when (request) {
            is GameRequest.Journey -> LevelDesigner.journey(request.level, themeIds)
            is GameRequest.Daily -> LevelDesigner.daily(request.date, themeIds)
            is GameRequest.TimeAttack -> LevelDesigner.timeAttackBoard(request.seed, board, themeIds)
        }
        return GameEngine(PuzzleGenerator.generate(cfg, WordBank.byId(cfg.themeId)), cfg, colorOffset)
    }

    private fun startTimer() {
        timer = scope.launch {
            var last = System.nanoTime()
            while (isActive && !finished) {
                delay(100)
                val now = System.nanoTime()
                val dt = (now - last) / 1_000_000
                last = now
                if (paused) continue
                elapsedMs += dt
                if (isTimeAttack) {
                    timeLeftMs = (timeLeftMs - dt).coerceAtLeast(0)
                    if (timeLeftMs == 0L) finishTimeAttack()
                }
            }
        }
    }

    fun comboNow(): Int = engine.comboAt(elapsedMs)

    /** Fração restante da janela de combo (1 → 0), para a barrinha sob o indicador. */
    fun comboWindowLeft(): Float {
        val at = lastFoundMs ?: return 0f
        return (1f - (elapsedMs - at).toFloat() / Progression.COMBO_WINDOW_MS).coerceIn(0f, 1f)
    }
    private var lastFoundMs: Long? = null

    // ---------- Entrada ----------

    fun onDragStart(cell: Cell) {
        if (finished || paused) return
        dragStart = cell
        selection = listOf(cell)
    }

    fun onDragMove(cell: Cell) {
        val start = dragStart ?: return
        val line = Selection.line(start, cell, engine.puzzle.size)
        if (line != selection) {
            selection = line
            events.tryEmit(GameEvent.Tick)
        }
    }

    fun onDragEnd() {
        val start = dragStart ?: return
        dragStart = null
        val sel = selection
        selection = emptyList()
        if (sel.size <= 1) {
            // Toque simples: modo dois toques (Constituição VI)
            val a = anchor
            anchor = when {
                a == null -> start
                a == start -> null
                else -> { submit(Selection.line(a, start, engine.puzzle.size)); null }
            }
            return
        }
        anchor = null
        submit(sel)
    }

    private fun submit(cells: List<Cell>) {
        when (val r = engine.trySelection(cells, elapsedMs)) {
            is SelectionOutcome.Found -> {
                lastFoundMs = elapsedMs
                if (isTimeAttack) timeLeftMs += TA_WORD_BONUS_MS
                addFloater(r.found)
                sync()
                events.tryEmit(GameEvent.Found(r.found))
                if (r.complete) onBoardComplete()
            }
            is SelectionOutcome.AlreadyFound -> Unit
            SelectionOutcome.Miss -> if (cells.size > 1) events.tryEmit(GameEvent.Miss)
        }
    }

    private fun addFloater(fw: FoundWord) {
        val n = engine.puzzle.size.toFloat()
        val a = fw.cells.first()
        val b = fw.cells.last()
        val x = ((a.col + b.col) / 2f + 0.5f) / n
        val y = ((a.row + b.row) / 2f + 0.5f) / n
        val comboTxt = if (fw.combo >= 2) "  🔥×${fw.combo.coerceAtMost(4)}" else ""
        floaters += Floater(floaterId++, "+${fw.xp} XP$comboTxt", x, y, fw.word.secret)
    }

    fun removeFloater(id: Long) {
        floaters.removeAll { it.id == id }
    }

    // ---------- Dicas ----------

    /** Dica: revela só a inicial da Palavra Secreta na lista (002-US12). */
    fun useHint(cost: Int) {
        if (finished || !engine.canHint) return
        if (!spendCoins(cost)) return
        engine.hintSecretInitial()
        sync()
    }

    private fun sync() {
        found = engine.found.toList()
        secretInitial = engine.secretInitial
        canHint = engine.canHint
        levelXp = if (isTimeAttack) taWordXp + engine.wordXp else engine.wordXp
        if (isTimeAttack) taTotalWords = taWords + engine.foundByPlayer
    }

    // ---------- Fim ----------

    private fun onBoardComplete() {
        if (isTimeAttack) {
            accumulateBoard()
            boardsCleared++
            timeLeftMs += TA_BOARD_BONUS_MS
            events.tryEmit(GameEvent.BoardCleared)
            engine = newEngine(board = boardsCleared, colorOffset = taWords + boardsCleared)
            lastFoundMs = null
            anchor = null
            sync()
            return
        }
        finished = true
        events.tryEmit(GameEvent.Complete)
        val isReplay = (request as? GameRequest.Journey)?.isReplay ?: false
        val result = engine.result(elapsedSeconds = (elapsedMs / 1000).toInt(), isReplay = isReplay)
        scope.launch {
            delay(1100)
            onFinish(result)
        }
    }

    private fun accumulateBoard() {
        taWords += engine.foundByPlayer
        taWordXp += engine.wordXp
        taHints += engine.hintsUsed
        taMaxCombo = maxOf(taMaxCombo, engine.maxCombo)
    }

    private fun finishTimeAttack() {
        if (finished) return
        finished = true
        accumulateBoard()
        events.tryEmit(GameEvent.Complete)
        val result = LevelResult(
            mode = GameMode.TIME_ATTACK,
            levelNumber = 0,
            wordsFound = taWords,
            secretFound = 0,
            hintsUsed = taHints,
            elapsedSeconds = (elapsedMs / 1000).toInt(),
            parSeconds = 0,
            tier = 0,
            wordXp = taWordXp,
            maxCombo = taMaxCombo,
            boardsCleared = boardsCleared,
        )
        scope.launch {
            delay(900)
            onFinish(result)
        }
    }

    fun cancel() {
        finished = true
        timer?.cancel()
    }

    companion object {
        const val TA_START_MS = 90_000L
        const val TA_WORD_BONUS_MS = 5_000L
        const val TA_BOARD_BONUS_MS = 15_000L
    }
}
