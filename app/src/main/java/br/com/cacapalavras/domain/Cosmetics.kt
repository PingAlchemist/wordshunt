package br.com.cacapalavras.domain

/** Partes do personagem (003a-US13). [required] = sempre tem um item equipado. */
enum class Slot(val label: String, val emoji: String, val required: Boolean) {
    SKIN("Pele", "✋", true),
    HAIR("Cabelo", "💇", true),
    FACE("Expressão", "😊", true),
    OUTFIT("Roupa", "👕", true),
    SHOES("Calçado", "👟", true),
    HAT("Chapéu", "🎩", false),
    GLASSES("Óculos", "👓", false),
    BACKPACK("Mochila", "🎒", false),
    BACKGROUND("Cenário", "🏞️", true),
    ANIMATION("Animação", "✨", true),
}

/** Raridade é só visual (Constituição V-b). [duplicateCoins] = compensação por item repetido. */
enum class Rarity(val label: String, val argb: Long, val duplicateCoins: Int) {
    COMMON("Comum", 0xFF9AA5B1, 50),
    UNCOMMON("Incomum", 0xFF3FB950, 100),
    RARE("Raro", 0xFF3D8BFD, 200),
    EPIC("Épico", 0xFFA855F7, 400),
    LEGENDARY("Lendário", 0xFFF59E0B, 800),
}

sealed interface ItemSource {
    data object Starter : ItemSource
    data class Level(val level: Int) : ItemSource
    data class FromAchievement(val achievementId: String) : ItemSource
    data class Pass(val tier: Int, val premium: Boolean) : ItemSource
    data class Streak(val days: Int) : ItemSource
}

/**
 * Item cosmético. [variant] e [colors] descrevem o desenho (o renderizador da UI interpreta);
 * o domínio não depende de Android.
 */
data class CosmeticItem(
    val id: String,
    val name: String,
    val slot: Slot,
    val rarity: Rarity,
    val variant: String,
    val colors: List<Long>,
    val source: ItemSource,
) {
    fun howToGet(): String = when (val s = source) {
        ItemSource.Starter -> "Item inicial"
        is ItemSource.Level -> "Alcance o nível ${s.level}"
        is ItemSource.FromAchievement -> "Conquista: ${Achievements.all.firstOrNull { it.id == s.achievementId }?.name ?: s.achievementId}"
        is ItemSource.Pass -> "Passe — nível ${s.tier}${if (s.premium) " (Premium)" else " (grátis)"}"
        is ItemSource.Streak -> "Sequência de ${s.days} dias 🔥"
    }
}

object CosmeticCatalog {
    private fun item(id: String, name: String, slot: Slot, rarity: Rarity, variant: String, colors: List<Long>, source: ItemSource) =
        CosmeticItem(id, name, slot, rarity, variant, colors, source)

    private val S = ItemSource.Starter
    private fun lv(n: Int) = ItemSource.Level(n)
    private fun ach(id: String) = ItemSource.FromAchievement(id)
    private fun free(t: Int) = ItemSource.Pass(t, premium = false)
    private fun prem(t: Int) = ItemSource.Pass(t, premium = true)

    val items: List<CosmeticItem> = listOf(
        // ---------- Iniciais ----------
        item("skin_1", "Pele Clara", Slot.SKIN, Rarity.COMMON, "SKIN", listOf(0xFFFFE0C7), S),
        item("skin_2", "Pele Média Clara", Slot.SKIN, Rarity.COMMON, "SKIN", listOf(0xFFF1C27D), S),
        item("skin_3", "Pele Média", Slot.SKIN, Rarity.COMMON, "SKIN", listOf(0xFFD39B6A), S),
        item("skin_4", "Pele Morena", Slot.SKIN, Rarity.COMMON, "SKIN", listOf(0xFFA86B3C), S),
        item("skin_5", "Pele Escura", Slot.SKIN, Rarity.COMMON, "SKIN", listOf(0xFF6B4423), S),
        item("hair_short_brown", "Curto Castanho", Slot.HAIR, Rarity.COMMON, "SHORT", listOf(0xFF6B3E26), S),
        item("hair_curly_black", "Cacheado Preto", Slot.HAIR, Rarity.COMMON, "CURLY", listOf(0xFF2B2B2B), S),
        item("face_smile", "Sorriso", Slot.FACE, Rarity.COMMON, "SMILE", listOf(), S),
        item("outfit_tshirt_blue", "Camiseta Azul", Slot.OUTFIT, Rarity.COMMON, "TSHIRT", listOf(0xFF3D7BFD, 0xFF2A3A5C), S),
        item("outfit_tshirt_green", "Camiseta Verde", Slot.OUTFIT, Rarity.COMMON, "TSHIRT", listOf(0xFF34A853, 0xFF2A3A5C), S),
        item("shoes_white", "Tênis Branco", Slot.SHOES, Rarity.COMMON, "SNEAKER", listOf(0xFFF5F5F5, 0xFF3D7BFD), S),
        item("bg_sky", "Céu Claro", Slot.BACKGROUND, Rarity.COMMON, "SKY", listOf(), S),
        item("anim_breathe", "Respirar", Slot.ANIMATION, Rarity.COMMON, "BREATHE", listOf(), S),

        // ---------- Nível do jogador (plan §4.3) ----------
        item("face_big_smile", "Feliz", Slot.FACE, Rarity.COMMON, "BIG_SMILE", listOf(), lv(2)),
        item("hat_cap_red", "Boné Vermelho", Slot.HAT, Rarity.COMMON, "CAP", listOf(0xFFE53935, 0xFFFFFFFF), lv(3)),
        item("glasses_round", "Óculos Redondos", Slot.GLASSES, Rarity.UNCOMMON, "ROUND", listOf(0xFF3A2A1A), lv(4)),
        item("hat_explorer", "Chapéu Explorador", Slot.HAT, Rarity.UNCOMMON, "EXPLORER", listOf(0xFFC8A165, 0xFF6D4C2F), lv(5)),
        item("hair_bun_ginger", "Coque Ruivo", Slot.HAIR, Rarity.UNCOMMON, "BUN", listOf(0xFFD9622B), lv(6)),
        item("backpack_adventure", "Mochila Aventureira", Slot.BACKPACK, Rarity.UNCOMMON, "CLASSIC", listOf(0xFF8D6E63, 0xFFFFB74D), lv(7)),
        item("bg_forest", "Floresta", Slot.BACKGROUND, Rarity.UNCOMMON, "FOREST", listOf(), lv(8)),
        item("shoes_boots", "Botas de Trilha", Slot.SHOES, Rarity.UNCOMMON, "BOOTS", listOf(0xFF795548, 0xFF4E342E), lv(9)),
        item("outfit_hunter", "Traje Caçador de Palavras", Slot.OUTFIT, Rarity.RARE, "EXPLORER", listOf(0xFF7C9A5B, 0xFF4E5B3A), lv(10)),
        item("bg_night", "Noite Estrelada", Slot.BACKGROUND, Rarity.RARE, "NIGHT", listOf(), lv(12)),
        item("glasses_sun", "Óculos Escuros", Slot.GLASSES, Rarity.RARE, "SUN", listOf(0xFF1E1E1E), lv(13)),
        item("anim_magnifier", "Lupa Giratória", Slot.ANIMATION, Rarity.EPIC, "MAGNIFIER", listOf(), lv(15)),
        item("face_wink", "Piscadinha", Slot.FACE, Rarity.RARE, "WINK", listOf(), lv(17)),
        item("outfit_master", "Manto do Mestre", Slot.OUTFIT, Rarity.EPIC, "ROBE", listOf(0xFF5E35B1, 0xFFFFD54F), lv(20)),
        item("hat_master", "Chapéu do Mestre", Slot.HAT, Rarity.EPIC, "WIZARD", listOf(0xFF5E35B1, 0xFFFFD54F), lv(20)),
        item("face_stars", "Olhos de Estrela", Slot.FACE, Rarity.EPIC, "STARS", listOf(), lv(25)),
        item("shoes_gold", "Tênis Dourado", Slot.SHOES, Rarity.EPIC, "SNEAKER", listOf(0xFFFFC93C, 0xFFB98600), lv(25)),
        item("anim_aura", "Aura de Letras", Slot.ANIMATION, Rarity.LEGENDARY, "AURA", listOf(), lv(30)),

        // ---------- Conquistas (1 item cada) ----------
        item("hair_spiky_blond", "Espetado Loiro", Slot.HAIR, Rarity.COMMON, "SPIKY", listOf(0xFFF2C14E), ach("first_word")),
        item("outfit_hoodie_orange", "Moletom Laranja", Slot.OUTFIT, Rarity.UNCOMMON, "HOODIE", listOf(0xFFFF8A3D, 0xFF33415C), ach("words_100")),
        item("hat_crown_letters", "Coroa de Letras", Slot.HAT, Rarity.LEGENDARY, "CROWN", listOf(0xFFFFC93C, 0xFFE53935), ach("words_1000")),
        item("backpack_books", "Mochila de Livros", Slot.BACKPACK, Rarity.UNCOMMON, "BOOKS", listOf(0xFF3949AB, 0xFFE53935), ach("journey_10")),
        item("bg_library", "Biblioteca", Slot.BACKGROUND, Rarity.EPIC, "LIBRARY", listOf(), ach("journey_50")),
        item("glasses_star", "Óculos de Estrela", Slot.GLASSES, Rarity.RARE, "STAR", listOf(0xFFFF4FA3), ach("three_stars_10")),
        item("hair_mohawk", "Moicano", Slot.HAIR, Rarity.RARE, "MOHAWK", listOf(0xFF00BFA5), ach("no_hint_10")),
        item("bg_sunset", "Pôr do Sol em Chamas", Slot.BACKGROUND, Rarity.RARE, "SUNSET", listOf(), ach("combo_max")),
        item("hat_detective", "Chapéu de Detetive", Slot.HAT, Rarity.RARE, "DETECTIVE", listOf(0xFF8D6E63, 0xFF4E342E), ach("secret_10")),
        item("anim_wave", "Aceno", Slot.ANIMATION, Rarity.RARE, "WAVE", listOf(), ach("streak_7")),
        item("shoes_lightning", "Tênis Relâmpago", Slot.SHOES, Rarity.RARE, "SNEAKER", listOf(0xFFFFEB3B, 0xFF1E88E5), ach("speed_45")),
        item("outfit_veteran", "Jaqueta Veterano", Slot.OUTFIT, Rarity.RARE, "JACKET", listOf(0xFF37474F, 0xFFECEFF1), ach("level_10")),

        // ---------- Passe — trilha grátis ----------
        item("hair_short_ginger", "Franja Ruiva", Slot.HAIR, Rarity.COMMON, "SHORT", listOf(0xFFD9622B), free(2)),
        item("glasses_square", "Óculos Quadrados", Slot.GLASSES, Rarity.UNCOMMON, "SQUARE", listOf(0xFF1565C0), free(5)),
        item("face_surprised", "Surpreso", Slot.FACE, Rarity.COMMON, "SURPRISED", listOf(), free(7)),
        item("outfit_stripes", "Camiseta Listrada", Slot.OUTFIT, Rarity.UNCOMMON, "STRIPES", listOf(0xFFFFFFFF, 0xFFE53935), free(10)),
        item("shoes_red", "Tênis Vermelho", Slot.SHOES, Rarity.COMMON, "SNEAKER", listOf(0xFFE53935, 0xFFFFFFFF), free(12)),
        item("bg_beach", "Praia", Slot.BACKGROUND, Rarity.UNCOMMON, "BEACH", listOf(), free(15)),
        item("hat_beanie_blue", "Gorro Azul", Slot.HAT, Rarity.UNCOMMON, "BEANIE", listOf(0xFF1E88E5, 0xFFFFFFFF), free(17)),
        item("backpack_rocket", "Mochila Foguete", Slot.BACKPACK, Rarity.RARE, "ROCKET", listOf(0xFFECEFF1, 0xFFE53935), free(20)),
        item("hair_curly_purple", "Cacheado Roxo", Slot.HAIR, Rarity.RARE, "CURLY", listOf(0xFF8E44AD), free(22)),
        item("anim_bounce", "Pulinho", Slot.ANIMATION, Rarity.RARE, "BOUNCE", listOf(), free(25)),
        item("outfit_hoodie_galaxy", "Moletom Galáxia", Slot.OUTFIT, Rarity.RARE, "HOODIE", listOf(0xFF3F2A78, 0xFF1B1B3A), free(27)),
        item("hat_helmet", "Capacete Espacial", Slot.HAT, Rarity.EPIC, "HELMET", listOf(0xFFB3E5FC, 0xFFFFFFFF), free(30)),

        // ---------- Passe — trilha premium ----------
        item("outfit_space", "Traje Espacial", Slot.OUTFIT, Rarity.EPIC, "SPACE", listOf(0xFFF5F7FA, 0xFFFF7043), prem(1)),
        item("hair_neon", "Cabelo Neon", Slot.HAIR, Rarity.RARE, "SPIKY", listOf(0xFF00E5FF), prem(4)),
        item("glasses_visor", "Visor Futurista", Slot.GLASSES, Rarity.EPIC, "VISOR", listOf(0xFF00E5FF), prem(6)),
        item("face_cool", "Confiante", Slot.FACE, Rarity.RARE, "COOL", listOf(), prem(9)),
        item("bg_galaxy", "Galáxia", Slot.BACKGROUND, Rarity.EPIC, "GALAXY", listOf(), prem(10)),
        item("shoes_moon", "Botas Lunares", Slot.SHOES, Rarity.RARE, "BOOTS", listOf(0xFFECEFF1, 0xFF90A4AE), prem(12)),
        item("backpack_jetpack", "Jetpack", Slot.BACKPACK, Rarity.EPIC, "JETPACK", listOf(0xFF90A4AE, 0xFFFF7043), prem(15)),
        item("hat_antenna", "Antenas", Slot.HAT, Rarity.RARE, "ANTENNA", listOf(0xFF26C6DA, 0xFFFF4FA3), prem(18)),
        item("anim_float", "Flutuar", Slot.ANIMATION, Rarity.EPIC, "FLOAT", listOf(), prem(21)),
        item("hair_comet", "Cometa", Slot.HAIR, Rarity.EPIC, "LONG", listOf(0xFFFF4FA3, 0xFF7C4DFF), prem(24)),
        item("outfit_cape", "Capa Cósmica", Slot.OUTFIT, Rarity.EPIC, "CAPE", listOf(0xFF283593, 0xFFECEFF1), prem(26)),
        item("face_determined", "Determinado", Slot.FACE, Rarity.RARE, "DETERMINED", listOf(), prem(29)),
        item("anim_star_rain", "Chuva de Estrelas", Slot.ANIMATION, Rarity.LEGENDARY, "STAR_RAIN", listOf(), prem(30)),

        // ---------- Marcos da sequência diária (004-US20) ----------
        item("glasses_streak", "Óculos Brasa", Slot.GLASSES, Rarity.UNCOMMON, "ROUND", listOf(0xFFFF6D00), ItemSource.Streak(3)),
        item("hat_streak_cap", "Boné Flamejante", Slot.HAT, Rarity.RARE, "CAP", listOf(0xFFFF5722, 0xFFFFC107), ItemSource.Streak(7)),
        item("shoes_streak", "Tênis em Chamas", Slot.SHOES, Rarity.RARE, "SNEAKER", listOf(0xFFFF3D00, 0xFFFFD600), ItemSource.Streak(14)),
        item("outfit_streak", "Jaqueta da Constância", Slot.OUTFIT, Rarity.EPIC, "JACKET", listOf(0xFFD84315, 0xFF212121), ItemSource.Streak(30)),
        item("backpack_streak", "Foguete Dourado", Slot.BACKPACK, Rarity.EPIC, "ROCKET", listOf(0xFFFFD54F, 0xFFFF5722), ItemSource.Streak(60)),
        item("hat_streak_crown", "Coroa da Constância", Slot.HAT, Rarity.LEGENDARY, "CROWN", listOf(0xFFFF9100, 0xFFD50000), ItemSource.Streak(100)),
    )

    private val byId = items.associateBy { it.id }
    fun byId(id: String): CosmeticItem? = byId[id]
    fun bySlot(slot: Slot) = items.filter { it.slot == slot }

    val defaults: Map<Slot, String> = mapOf(
        Slot.SKIN to "skin_2",
        Slot.HAIR to "hair_short_brown",
        Slot.FACE to "face_smile",
        Slot.OUTFIT to "outfit_tshirt_blue",
        Slot.SHOES to "shoes_white",
        Slot.BACKGROUND to "bg_sky",
        Slot.ANIMATION to "anim_breathe",
    )

    fun levelItems(level: Int) = items.filter { (it.source as? ItemSource.Level)?.level == level }
}

/** Regras de inventário e aparência (003a-US14/US15). */
object Inventory {
    /**
     * Garante itens iniciais, itens de nível ≤ nível atual e itens de conquistas já obtidas
     * (retroativo para quem já jogava). Devolve o novo estado e os itens recém-concedidos (sem os iniciais).
     */
    fun grantProgressItems(p: PlayerProgress): Pair<PlayerProgress, List<CosmeticItem>> {
        val level = p.level
        val due = CosmeticCatalog.items.filter { item ->
            when (val s = item.source) {
                ItemSource.Starter -> true
                is ItemSource.Level -> s.level <= level
                is ItemSource.FromAchievement -> s.achievementId in p.achievements
                is ItemSource.Pass -> false
                is ItemSource.Streak -> s.days in p.streak.milestones
            }
        }
        val missing = due.filter { it.id !in p.inventory }
        val updated = p.copy(inventory = p.inventory + missing.map { it.id })
        return fixAppearance(updated) to missing.filter { it.source != ItemSource.Starter }
    }

    /** Aparência efetiva: o equipado (se possuído) ou o padrão para slots obrigatórios. */
    fun appearanceOf(p: PlayerProgress): Map<Slot, CosmeticItem> = buildMap {
        for (slot in Slot.entries) {
            val chosen = p.appearance[slot.name]?.takeIf { it in p.inventory }?.let { CosmeticCatalog.byId(it) }
            val item = chosen ?: if (slot.required) CosmeticCatalog.byId(CosmeticCatalog.defaults.getValue(slot)) else null
            if (item != null) put(slot, item)
        }
    }

    fun equip(p: PlayerProgress, itemId: String): PlayerProgress {
        val item = CosmeticCatalog.byId(itemId) ?: return p
        if (itemId !in p.inventory) return p
        return p.copy(appearance = p.appearance + (item.slot.name to itemId))
    }

    fun unequip(p: PlayerProgress, slot: Slot): PlayerProgress =
        if (slot.required) p else p.copy(appearance = p.appearance - slot.name)

    /** Concede um item; se já possuído, converte em moedas pela raridade. Devolve (estado, moedas de compensação). */
    fun grantItem(p: PlayerProgress, item: CosmeticItem): Pair<PlayerProgress, Int> =
        if (item.id in p.inventory) p.copy(coins = p.coins + item.rarity.duplicateCoins) to item.rarity.duplicateCoins
        else p.copy(inventory = p.inventory + item.id) to 0

    private fun fixAppearance(p: PlayerProgress): PlayerProgress {
        val fixed = p.appearance.filter { (_, id) -> id in p.inventory }.toMutableMap()
        for ((slot, id) in CosmeticCatalog.defaults) if (slot.name !in fixed) fixed[slot.name] = id
        return if (fixed == p.appearance) p else p.copy(appearance = fixed)
    }
}
