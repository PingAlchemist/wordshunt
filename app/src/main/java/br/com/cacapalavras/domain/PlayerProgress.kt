package br.com.cacapalavras.domain

import kotlinx.serialization.Serializable

/** Estado persistido do jogador (data-model.md). Nível e título são sempre derivados de [totalXp]. */
@Serializable
data class PlayerProgress(
    val totalXp: Long = 0,
    val coins: Int = 100,
    val journeyUnlocked: Int = 1,
    val starsByLevel: Map<Int, Int> = emptyMap(),
    val wordsFound: Int = 0,
    val secretFound: Int = 0,
    val levelsCompleted: Int = 0,
    val noHintLevels: Int = 0,
    /** Legado (v1.0): sequência do Desafio Diário. Migrado para [streak] por DailyStreak.migrateLegacy. */
    val dailyStreak: Int = 0,
    val bestDailyStreak: Int = 0,
    val lastDailyEpochDay: Long = -1,
    val bestTimeAttackWords: Int = 0,
    val maxComboReached: Int = 0,
    val fastestLevelSeconds: Int = 0,
    val achievements: Set<String> = emptySet(),
    val selectedPalette: String = "classic",
    val hapticsEnabled: Boolean = true,
    // ---------- 003a: personagem, Minds e passe ----------
    val playerName: String = "Jogador",
    val minds: Int = 0,
    val inventory: Set<String> = emptySet(),
    /** Slot.name → itemId */
    val appearance: Map<String, String> = emptyMap(),
    val season: SeasonState = SeasonState(),
    val seasonHistory: List<SeasonRecord> = emptyList(),
    /** Desconto permanente no passe (D3). Hoje vem de compra de teste; no futuro, do servidor. */
    val lifetimePremium: Boolean = false,
    // ---------- 004: sequência diária e lembretes ----------
    val streak: StreakState = StreakState(),
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val lastChanceEnabled: Boolean = true,
    val notificationPromptDismissed: Boolean = false,
) {
    val level: Int get() = Progression.levelInfo(totalXp).level
    val threeStarLevels: Int get() = starsByLevel.values.count { it == 3 }
    val totalStars: Int get() = starsByLevel.values.sum()
    val journeyCompleted: Int get() = journeyUnlocked - 1
}
