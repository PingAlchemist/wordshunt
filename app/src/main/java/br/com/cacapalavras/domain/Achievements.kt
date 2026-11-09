package br.com.cacapalavras.domain

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    /** Progresso atual e meta, para a barra na tela de conquistas. */
    val progress: (PlayerProgress) -> Pair<Long, Long>,
) {
    fun isMet(p: PlayerProgress): Boolean = progress(p).let { (cur, goal) -> cur >= goal }
}

/** Contrato §10. */
object Achievements {
    const val XP_REWARD = 50
    const val COIN_REWARD = 25

    val all: List<Achievement> = listOf(
        Achievement("first_word", "Primeiros Passos", "Encontre sua primeira palavra", "👣") { it.wordsFound.toLong() to 1 },
        Achievement("words_100", "Vocabulário", "Encontre 100 palavras", "📖") { it.wordsFound.toLong() to 100 },
        Achievement("words_1000", "Dicionário Ambulante", "Encontre 1000 palavras", "📚") { it.wordsFound.toLong() to 1000 },
        Achievement("journey_10", "Viajante", "Complete 10 níveis da Jornada", "🧭") { it.journeyCompleted.toLong() to 10 },
        Achievement("journey_50", "Desbravador", "Complete 50 níveis da Jornada", "🗺️") { it.journeyCompleted.toLong() to 50 },
        Achievement("three_stars_10", "Perfeccionista", "Tenha 10 níveis com 3 estrelas", "🌟") { it.threeStarLevels.toLong() to 10 },
        Achievement("no_hint_10", "Purista", "Conclua 10 níveis sem usar dicas", "🧠") { it.noHintLevels.toLong() to 10 },
        Achievement("combo_max", "Em Chamas", "Atinja o combo máximo ×2,0", "🔥") { it.maxComboReached.coerceAtMost(4).toLong() to 4 },
        Achievement("secret_10", "Detetive", "Encontre 10 palavras secretas", "🕵️") { it.secretFound.toLong() to 10 },
        Achievement("streak_7", "Constância", "Mantenha uma sequência de 7 dias", "📅") { it.streak.best.toLong() to 7 },
        Achievement("speed_45", "Relâmpago", "Conclua um nível difícil (Jornada 11+ ou Diário) em até 45 s", "⚡") {
            (if (it.fastestLevelSeconds in 1..45) 1L else 0L) to 1
        },
        Achievement("level_10", "Veterano", "Alcance o nível de jogador 10", "🎖️") { it.level.toLong() to 10 },
    )

    fun newlyUnlocked(p: PlayerProgress): List<Achievement> =
        all.filter { it.id !in p.achievements && it.isMet(p) }
}
