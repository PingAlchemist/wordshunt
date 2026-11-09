package br.com.cacapalavras.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.cacapalavras.data.ProgressRepository
import br.com.cacapalavras.domain.GameRewards
import br.com.cacapalavras.domain.Inventory
import br.com.cacapalavras.domain.LevelResult
import br.com.cacapalavras.domain.PlayerProgress
import br.com.cacapalavras.domain.RewardPipeline
import br.com.cacapalavras.domain.SeasonPass
import br.com.cacapalavras.domain.SeasonState
import br.com.cacapalavras.domain.Shop
import br.com.cacapalavras.domain.Slot
import br.com.cacapalavras.notifications.StreakReminders
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface Screen {
    data object Home : Screen
    data object Levels : Screen
    data object Game : Screen
    data class Result(val rewards: GameRewards, val request: GameRequest, val wordsFound: Int) : Screen
    data object Achievements : Screen
    data object Profile : Screen
    data object Themes : Screen
    data object Character : Screen
    data object Pass : Screen
    data object Shop : Screen
    data object Streak : Screen
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ProgressRepository(app)

    /** null enquanto o DataStore ainda não carregou. */
    val progress: StateFlow<PlayerProgress?> = repo.progress.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val current get() = progress.value ?: PlayerProgress()

    val backStack = mutableStateListOf<Screen>(Screen.Home)
    val screen: Screen get() = backStack.last()

    var game by mutableStateOf<GameController?>(null); private set

    /** Aviso curto exibido na parte de baixo da tela. */
    var toast by mutableStateOf<String?>(null)

    init {
        // Itens retroativos (quem já jogava) e troca de temporada ao abrir o app.
        viewModelScope.launch {
            progress.filterNotNull().first()
            val p = repo.update { RewardPipeline.onAppStart(it, LocalDate.now()) }
            StreakReminders.createChannel(getApplication())
            StreakReminders.schedule(getApplication(), p)
        }
    }

    // ---------- Sequência e lembretes (004) ----------

    private fun updateReminders(transform: (PlayerProgress) -> PlayerProgress) = viewModelScope.launch {
        val p = repo.update(transform)
        StreakReminders.schedule(getApplication(), p)
    }

    fun setReminderEnabled(enabled: Boolean) = updateReminders { it.copy(reminderEnabled = enabled) }
    fun setReminderHour(hour: Int) = updateReminders { it.copy(reminderHour = hour, reminderMinute = 0) }
    fun setLastChance(enabled: Boolean) = updateReminders { it.copy(lastChanceEnabled = enabled) }
    fun dismissNotificationPrompt() = viewModelScope.launch { repo.update { it.copy(notificationPromptDismissed = true) } }
    fun testNotification() {
        StreakReminders.showTestNow(getApplication(), current)
        toast = "🧪 Notificação enviada"
    }

    fun navigate(to: Screen) {
        backStack += to
    }

    fun back() {
        if (screen == Screen.Game) quitGame()
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun home() {
        quitGame()
        backStack.clear()
        backStack += Screen.Home
    }

    // ---------- Partidas ----------

    fun startJourney(level: Int = current.journeyUnlocked) =
        startGame(GameRequest.Journey(level, isReplay = level < current.journeyUnlocked))

    fun startDaily() = startGame(GameRequest.Daily(LocalDate.now()))

    fun startTimeAttack() = startGame(GameRequest.TimeAttack(System.currentTimeMillis()))

    fun isDailyDone(p: PlayerProgress) = p.lastDailyEpochDay == LocalDate.now().toEpochDay()

    private fun startGame(request: GameRequest) {
        game?.cancel()
        game = GameController(request, viewModelScope, ::spendCoins, ::finishGame)
        // Sai de Result/Game anteriores para não empilhar partidas.
        while (backStack.last() is Screen.Result || backStack.last() == Screen.Game) backStack.removeAt(backStack.lastIndex)
        backStack += Screen.Game
    }

    private fun finishGame(result: LevelResult) {
        val request = game?.request ?: return
        viewModelScope.launch {
            var rewards: GameRewards? = null
            repo.update { p ->
                val (np, r) = RewardPipeline.applyGame(p, result, LocalDate.now())
                rewards = r
                np
            }
            game = null
            if (backStack.last() == Screen.Game) backStack.removeAt(backStack.lastIndex)
            backStack += Screen.Result(rewards!!, request, result.wordsFound)
        }
    }

    private fun quitGame() {
        game?.cancel()
        game = null
    }

    /** Desconta moedas se houver saldo. Atualiza o estado em memória imediatamente para evitar gasto duplo. */
    private var pendingSpend = 0

    private fun spendCoins(amount: Int): Boolean {
        if (current.coins - pendingSpend < amount) return false
        pendingSpend += amount
        viewModelScope.launch {
            repo.update { if (it.coins >= amount) it.copy(coins = it.coins - amount) else it }
            pendingSpend -= amount
        }
        return true
    }

    // ---------- Personagem (003a) ----------

    fun equip(itemId: String) = viewModelScope.launch { repo.update { Inventory.equip(it, itemId) } }

    fun unequip(slot: Slot) = viewModelScope.launch { repo.update { Inventory.unequip(it, slot) } }

    fun setPlayerName(name: String) = viewModelScope.launch {
        val clean = name.trim().take(18).ifEmpty { "Jogador" }
        repo.update { it.copy(playerName = clean) }
    }

    // ---------- Passe e Minds (003a) ----------

    fun claim(tier: Int, premium: Boolean) = viewModelScope.launch {
        var msg: String? = null
        repo.update { p -> SeasonPass.claim(p, tier, premium)?.let { (np, text) -> msg = "🎁 $text"; np } ?: p }
        msg?.let { toast = it }
    }

    fun claimAll() = viewModelScope.launch {
        var n = 0
        repo.update { p -> n = SeasonPass.claimableCount(p); SeasonPass.claimAll(p) }
        if (n > 0) toast = "🎁 $n recompensas resgatadas!"
    }

    fun buyPremium() = viewModelScope.launch {
        var ok = false
        repo.update { p -> SeasonPass.buyPremium(p)?.also { ok = true } ?: p }
        toast = if (ok) "⭐ Passe Premium liberado!" else "Minds insuficientes"
    }

    fun testPurchase(productId: String) = viewModelScope.launch {
        var ok = false
        repo.update { p -> Shop.testPurchase(p, productId)?.also { ok = true } ?: p }
        if (ok) toast = "🧪 Compra de teste concluída: ${Shop.products.first { it.id == productId }.title}"
    }

    fun testAddXp() = viewModelScope.launch {
        repo.update { RewardPipeline.grantXp(it, 1_000, LocalDate.now()) }
        toast = "🧪 +1.000 XP"
    }

    fun testAddMinds() = viewModelScope.launch {
        repo.update { it.copy(minds = it.minds + 500) }
        toast = "🧪 +500 🧠"
    }

    fun testResetPass() = viewModelScope.launch {
        repo.update { it.copy(season = SeasonState(number = it.season.number)) }
        toast = "🧪 Passe reiniciado"
    }

    // ---------- Preferências ----------

    fun selectPalette(id: String) = viewModelScope.launch { repo.update { it.copy(selectedPalette = id) } }

    fun setHaptics(enabled: Boolean) = viewModelScope.launch { repo.update { it.copy(hapticsEnabled = enabled) } }

    fun resetProgress() = viewModelScope.launch { repo.update { RewardPipeline.onAppStart(PlayerProgress(), LocalDate.now()) } }
}
