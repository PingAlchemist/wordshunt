package br.com.cacapalavras.domain

import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlin.math.roundToInt

sealed interface Reward {
    data class Coins(val amount: Int) : Reward
    data class Minds(val amount: Int) : Reward
    data class Item(val itemId: String) : Reward
}

data class PassTier(val level: Int, val free: Reward, val premium: Reward)

data class Season(val number: Int, val name: String, val startEpochDay: Long, val endEpochDay: Long) {
    fun daysLeft(todayEpochDay: Long) = (endEpochDay - todayEpochDay).coerceAtLeast(0)
}

/** Estado persistido da temporada corrente. */
@Serializable
data class SeasonState(
    val number: Int = 0,
    val xp: Long = 0,
    val premium: Boolean = false,
    val claimedFree: Set<Int> = emptySet(),
    val claimedPremium: Set<Int> = emptySet(),
)

@Serializable
data class SeasonRecord(val number: Int, val name: String, val finalLevel: Int, val premium: Boolean)

/** Passe de temporada (003a-US17). */
object SeasonPass {
    const val TIERS = 30
    const val XP_PER_TIER = 1_000L
    const val DURATION_DAYS = 35L
    const val PRICE_MINDS = 950
    const val LIFETIME_DISCOUNT = 0.30
    val FIRST_SEASON_START: LocalDate = LocalDate.of(2026, 11, 1)

    private val seasonNames = mapOf(1 to "Exploradores das Letras")

    /** Temporadas contínuas de 35 dias a partir de 01/11/2026. */
    fun current(today: LocalDate): Season {
        val anchor = FIRST_SEASON_START.toEpochDay()
        val idx = Math.floorDiv(today.toEpochDay() - anchor, DURATION_DAYS).coerceAtLeast(0)
        val n = idx.toInt() + 1
        val start = anchor + idx * DURATION_DAYS
        return Season(n, seasonNames[n] ?: "Temporada $n", start, start + DURATION_DAYS)
    }

    fun price(p: PlayerProgress): Int =
        if (p.lifetimePremium) (PRICE_MINDS * (1 - LIFETIME_DISCOUNT)).roundToInt() else PRICE_MINDS

    fun levelFor(xp: Long): Int = (xp / XP_PER_TIER).toInt().coerceAtMost(TIERS)
    fun progressInTier(xp: Long): Float =
        if (levelFor(xp) >= TIERS) 1f else (xp % XP_PER_TIER).toFloat() / XP_PER_TIER

    private fun itemAt(tier: Int, premium: Boolean): Reward.Item =
        Reward.Item(CosmeticCatalog.items.first { it.source == ItemSource.Pass(tier, premium) }.id)

    /** 30 níveis, recompensa em todos, nas duas trilhas. Grátis: 300 Minds; Premium: 1.000 Minds (≥ preço). */
    val tiers: List<PassTier> = (1..TIERS).map { t ->
        val freeItemTiers = setOf(2, 5, 7, 10, 12, 15, 17, 20, 22, 25, 27, 30)
        val freeMindsTiers = setOf(3, 9, 14, 19, 24, 29)
        val premiumItemTiers = setOf(1, 4, 6, 9, 10, 12, 15, 18, 21, 24, 26, 29, 30)
        val premiumMindsTiers = setOf(2, 5, 8, 11, 14, 17, 20, 23, 25, 28)
        val free = when (t) {
            in freeItemTiers -> itemAt(t, premium = false)
            in freeMindsTiers -> Reward.Minds(50)
            else -> Reward.Coins(if (t < 10) 100 else if (t < 20) 150 else 250)
        }
        val premium = when (t) {
            in premiumItemTiers -> itemAt(t, premium = true)
            in premiumMindsTiers -> Reward.Minds(100)
            else -> Reward.Coins(if (t < 15) 300 else 450)
        }
        PassTier(t, free, premium)
    }

    fun totalMinds(premium: Boolean): Int =
        tiers.sumOf { ((if (premium) it.premium else it.free) as? Reward.Minds)?.amount ?: 0 }

    // ---------- Operações ----------

    /** Troca de temporada: resgata automaticamente o que foi alcançado, guarda histórico e zera (US17-7). */
    fun sync(p: PlayerProgress, today: LocalDate): PlayerProgress {
        val season = current(today)
        if (p.season.number == season.number) return p
        var out = p
        if (p.season.number > 0) {
            out = claimAll(out)
            val oldName = seasonNames[p.season.number] ?: "Temporada ${p.season.number}"
            out = out.copy(seasonHistory = out.seasonHistory + SeasonRecord(p.season.number, oldName, levelFor(p.season.xp), p.season.premium))
        }
        return out.copy(season = SeasonState(number = season.number))
    }

    fun addXp(p: PlayerProgress, xp: Int): PlayerProgress =
        if (xp <= 0) p else p.copy(season = p.season.copy(xp = p.season.xp + xp))

    fun canClaim(p: PlayerProgress, tier: Int, premium: Boolean): Boolean {
        if (tier !in 1..TIERS || levelFor(p.season.xp) < tier) return false
        return if (premium) p.season.premium && tier !in p.season.claimedPremium else tier !in p.season.claimedFree
    }

    /** Resgata uma recompensa. Devolve o novo estado e a descrição do que foi ganho, ou null se não pode. */
    fun claim(p: PlayerProgress, tier: Int, premium: Boolean): Pair<PlayerProgress, String>? {
        if (!canClaim(p, tier, premium)) return null
        val t = tiers[tier - 1]
        val (granted, text) = grant(p, if (premium) t.premium else t.free)
        val season = if (premium) granted.season.copy(claimedPremium = granted.season.claimedPremium + tier)
        else granted.season.copy(claimedFree = granted.season.claimedFree + tier)
        return granted.copy(season = season) to text
    }

    fun claimAll(p: PlayerProgress): PlayerProgress {
        var out = p
        for (t in 1..TIERS) {
            claim(out, t, premium = false)?.let { out = it.first }
            claim(out, t, premium = true)?.let { out = it.first }
        }
        return out
    }

    fun claimableCount(p: PlayerProgress): Int =
        (1..TIERS).count { canClaim(p, it, false) } + (1..TIERS).count { canClaim(p, it, true) }

    /** Compra o Premium com Minds. Prêmios premium já alcançados ficam resgatáveis (retroativo). */
    fun buyPremium(p: PlayerProgress): PlayerProgress? {
        val cost = price(p)
        if (p.season.premium || p.minds < cost) return null
        return p.copy(minds = p.minds - cost, season = p.season.copy(premium = true))
    }

    fun describe(r: Reward): String = when (r) {
        is Reward.Coins -> "${r.amount} 🪙"
        is Reward.Minds -> "${r.amount} 🧠 Minds"
        is Reward.Item -> CosmeticCatalog.byId(r.itemId)?.name ?: r.itemId
    }

    private fun grant(p: PlayerProgress, r: Reward): Pair<PlayerProgress, String> = when (r) {
        is Reward.Coins -> p.copy(coins = p.coins + r.amount) to "+${r.amount} 🪙"
        is Reward.Minds -> p.copy(minds = p.minds + r.amount) to "+${r.amount} 🧠"
        is Reward.Item -> {
            val item = CosmeticCatalog.byId(r.itemId)!!
            val (np, dup) = Inventory.grantItem(p, item)
            np to if (dup > 0) "${item.name} (repetido → +$dup 🪙)" else item.name
        }
    }
}

/** Produtos da loja. Por ora todas as compras são SIMULADAS (decisão D4). */
object Shop {
    data class Product(val id: String, val title: String, val description: String, val priceLabel: String, val minds: Int = 0, val lifetime: Boolean = false)

    val products = listOf(
        Product("minds_500", "500 Minds", "Pacote pequeno", "R$ 7,90", minds = 500),
        Product("minds_1000", "1.000 Minds", "Dá para um Passe Premium", "R$ 14,90", minds = 1_000),
        Product("minds_2800", "2.800 Minds", "Pacote grande", "R$ 39,90", minds = 2_800),
        Product(
            "premium_lifetime", "Premium Vitalício",
            "30% de desconto no Passe Premium de todas as temporadas. Não inclui os passes.", "R$ 89,90", lifetime = true,
        ),
    )

    /** Aplica uma compra de teste (nada é cobrado). */
    fun testPurchase(p: PlayerProgress, productId: String): PlayerProgress? {
        val product = products.firstOrNull { it.id == productId } ?: return null
        if (product.lifetime && p.lifetimePremium) return null
        return p.copy(minds = p.minds + product.minds, lifetimePremium = p.lifetimePremium || product.lifetime)
    }
}

/** Resultado completo de uma partida para a tela de resultado. */
data class GameRewards(
    val summary: Progression.RewardSummary,
    val newItems: List<CosmeticItem>,
    val passXp: Int,
    val passLevelBefore: Int,
    val passLevelAfter: Int,
    val streak: StreakUpdate,
)

/** Pipeline único (plan §2): partida → XP/moedas → itens → passe. O XP do passe é o mesmo XP do jogo. */
object RewardPipeline {
    fun applyGame(p0: PlayerProgress, result: LevelResult, today: LocalDate): Pair<PlayerProgress, GameRewards> {
        val p1 = SeasonPass.sync(p0, today)
        val (pS, streak) = DailyStreak.register(p1, today) // dia ativo = concluir 1 fase (004-US20)
        val (p2, summary) = Progression.applyResult(pS, result, today.toEpochDay())
        val (p3, items) = Inventory.grantProgressItems(p2)
        val before = SeasonPass.levelFor(p3.season.xp)
        val p4 = SeasonPass.addXp(p3, summary.totalXp)
        val newItems = items + listOfNotNull(streak.milestoneItem)
        return p4 to GameRewards(summary, newItems, summary.totalXp, before, SeasonPass.levelFor(p4.season.xp), streak)
    }

    /** XP vindo de fora de uma partida (ferramenta de teste): conta para nível do jogador e do passe. */
    fun grantXp(p0: PlayerProgress, xp: Int, today: LocalDate): PlayerProgress {
        val p1 = SeasonPass.sync(p0, today)
        val levelBefore = p1.level
        var p = p1.copy(totalXp = p1.totalXp + xp)
        for (lv in (levelBefore + 1)..p.level) p = p.copy(coins = p.coins + Progression.levelUpCoins(lv))
        p = Inventory.grantProgressItems(p).first
        return SeasonPass.addXp(p, xp)
    }

    /** Ao abrir o app: itens retroativos e troca de temporada. */
    fun onAppStart(p: PlayerProgress, today: LocalDate): PlayerProgress =
        SeasonPass.sync(Inventory.grantProgressItems(DailyStreak.migrateLegacy(p)).first, today)
}
