package br.com.cacapalavras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cacapalavras.BuildConfig
import br.com.cacapalavras.domain.CosmeticCatalog
import br.com.cacapalavras.domain.CosmeticItem
import br.com.cacapalavras.domain.Inventory
import br.com.cacapalavras.domain.PlayerProgress
import br.com.cacapalavras.domain.Progression
import br.com.cacapalavras.domain.Rarity
import br.com.cacapalavras.domain.Reward
import br.com.cacapalavras.domain.SeasonPass
import br.com.cacapalavras.domain.Shop
import br.com.cacapalavras.domain.Slot
import br.com.cacapalavras.ui.components.Avatar
import br.com.cacapalavras.ui.components.GameCard
import br.com.cacapalavras.ui.components.Pill
import br.com.cacapalavras.ui.components.ProgressBar
import br.com.cacapalavras.ui.components.ScreenHeader
import br.com.cacapalavras.ui.theme.Gold
import br.com.cacapalavras.ui.theme.LocalPalette
import java.time.LocalDate

fun Rarity.color() = Color(argb)

/** Miniatura: o personagem atual vestindo [item] (é assim que o jogador "prova" a peça). */
@Composable
fun ItemThumb(item: CosmeticItem, base: Map<Slot, CosmeticItem>, modifier: Modifier = Modifier, dim: Boolean = false) {
    val palette = LocalPalette.current
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(lerp(palette.card, item.rarity.color(), 0.12f))
            .border(2.dp, item.rarity.color(), RoundedCornerShape(14.dp)),
    ) {
        Avatar(base + (item.slot to item), Modifier.fillMaxWidth(), animate = false, showBackground = item.slot == Slot.BACKGROUND)
        if (dim) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f)))
    }
}

@Composable
fun RarityLabel(r: Rarity) {
    Text(
        r.label.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp,
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(r.color()).padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

// ======================= Personagem / Inventário (003a-US14) =======================

@Composable
fun CharacterScreen(progress: PlayerProgress, onBack: () -> Unit, onEquip: (String) -> Unit, onUnequip: (Slot) -> Unit, onInfo: (String) -> Unit) {
    val palette = LocalPalette.current
    val look = Inventory.appearanceOf(progress)
    var slot by remember { mutableStateOf(Slot.HAIR) }
    var rarity by remember { mutableStateOf<Rarity?>(null) }
    val owned = progress.inventory
    val items = CosmeticCatalog.bySlot(slot)
        .filter { rarity == null || it.rarity == rarity }
        .sortedWith(compareBy({ it.id !in owned }, { it.rarity.ordinal }))

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Personagem", onBack) {
            Pill("${owned.size}/${CosmeticCatalog.items.size} itens")
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(150.dp).clip(RoundedCornerShape(22.dp))) { Avatar(look, Modifier.fillMaxWidth()) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(progress.playerName, style = MaterialTheme.typography.titleLarge, color = palette.text)
                Text("Nível ${progress.level} · ${Progression.title(progress.level)}", color = palette.muted)
                Spacer(Modifier.height(8.dp))
                Text("Equipado", color = palette.muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                look[slot]?.let { Text("${slot.emoji} ${it.name}", color = palette.text, fontWeight = FontWeight.Bold) }
                    ?: Text("${slot.emoji} Nenhum", color = palette.text)
            }
        }
        // Categorias
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Slot.entries.forEach { s ->
                val sel = s == slot
                Text(
                    "${s.emoji} ${s.label}",
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(if (sel) palette.accent else palette.card)
                        .clickable { slot = s }.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (sel) palette.onAccent else palette.text, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                )
            }
        }
        // Raridade
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (listOf<Rarity?>(null) + Rarity.entries).forEach { r ->
                val sel = r == rarity
                val col = r?.color() ?: palette.muted
                Text(
                    r?.label ?: "Todas",
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(if (sel) col else Color.Transparent)
                        .border(1.5.dp, col, RoundedCornerShape(50)).clickable { rarity = r }.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = if (sel) Color.White else col, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            if (!slot.required) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(14.dp)).background(palette.card)
                                .border(2.dp, if (look[slot] == null) palette.accent else palette.muted.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable { onUnequip(slot) },
                            contentAlignment = Alignment.Center,
                        ) { Text("🚫", fontSize = 30.sp) }
                        Text("Nenhum", fontSize = 12.sp, color = palette.text, fontWeight = FontWeight.Bold)
                    }
                }
            }
            items(items, key = { it.id }) { item ->
                val has = item.id in owned
                val equipped = look[slot]?.id == item.id
                Column(
                    Modifier.clickable { if (has) onEquip(item.id) else onInfo("🔒 ${item.name}: ${item.howToGet()}") },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box {
                        ItemThumb(item, look, dim = !has)
                        if (equipped) Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).clip(CircleShape).background(palette.accent).padding(horizontal = 7.dp, vertical = 1.dp))
                        if (!has) Text("🔒", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopEnd).padding(5.dp))
                    }
                    Spacer(Modifier.height(3.dp))
                    RarityLabel(item.rarity)
                    Text(item.name, fontSize = 12.sp, color = palette.text, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 14.sp)
                    if (!has) Text(item.howToGet(), fontSize = 10.sp, color = palette.muted, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 12.sp)
                }
            }
        }
    }
}

// ======================= Passe de Temporada (003a-US17) =======================

@Composable
fun PassScreen(
    progress: PlayerProgress,
    onBack: () -> Unit,
    onClaim: (Int, Boolean) -> Unit,
    onClaimAll: () -> Unit,
    onBuyPremium: () -> Unit,
    onShop: () -> Unit,
    onTestXp: () -> Unit,
    onTestMinds: () -> Unit,
    onTestReset: () -> Unit,
) {
    val palette = LocalPalette.current
    val today = LocalDate.now()
    val season = SeasonPass.current(today)
    val st = progress.season
    val level = SeasonPass.levelFor(st.xp)
    val look = Inventory.appearanceOf(progress)
    val price = SeasonPass.price(progress)
    val claimable = SeasonPass.claimableCount(progress)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Passe de Temporada", onBack) { Pill("🧠 ${progress.minds}") }
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                GameCard(Modifier.fillMaxWidth(), color = palette.accent) {
                    Column(Modifier.padding(18.dp)) {
                        Text("TEMPORADA ${season.number}", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 12.sp)
                        Text(season.name, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        Text("⏳ termina em ${season.daysLeft(today.toEpochDay())} dias", color = Color.White.copy(alpha = 0.9f))
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("Nível $level", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                            Text(" / ${SeasonPass.TIERS}", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp, modifier = Modifier.padding(bottom = 3.dp))
                            Spacer(Modifier.weight(1f))
                            if (level < SeasonPass.TIERS) Text("${st.xp % SeasonPass.XP_PER_TIER} / ${SeasonPass.XP_PER_TIER} XP", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        ProgressBar(SeasonPass.progressInTier(st.xp), Modifier.fillMaxWidth(), color = Gold)
                        Spacer(Modifier.height(6.dp))
                        Text("Todo XP que você ganha jogando sobe o passe.", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
            }
            item {
                GameCard(Modifier.fillMaxWidth(), color = lerp(palette.card, Gold, 0.18f)) {
                    Column(Modifier.padding(16.dp)) {
                        if (st.premium) {
                            Text("⭐ Premium ativo", style = MaterialTheme.typography.titleMedium, color = palette.text)
                            Text("A trilha premium devolve ${SeasonPass.totalMinds(true)} 🧠 no total: termine e compre o próximo passe.", color = palette.muted, fontSize = 13.sp)
                        } else {
                            Text("Trilha Premium", style = MaterialTheme.typography.titleMedium, color = palette.text)
                            Text(
                                "Recompensas extras em todos os níveis, incluindo ${SeasonPass.totalMinds(true)} 🧠 (o suficiente para comprar o próximo passe). " +
                                    "Prêmios já alcançados são liberados na hora.",
                                color = palette.muted, fontSize = 13.sp,
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onBuyPremium, enabled = progress.minds >= price,
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent), shape = RoundedCornerShape(14.dp),
                                ) { Text("Liberar · $price 🧠") }
                                OutlinedButton(onClick = onShop, shape = RoundedCornerShape(14.dp)) { Text("Obter Minds") }
                            }
                            if (progress.lifetimePremium) Text("Desconto Vitalício de 30% aplicado", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (claimable > 0) item {
                Button(
                    onClick = onClaimAll, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF3A2A00)),
                ) { Text("🎁 Resgatar tudo ($claimable)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Spacer(Modifier.width(52.dp))
                    Text("GRÁTIS", Modifier.weight(1f), color = palette.muted, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, fontSize = 12.sp)
                    Text("PREMIUM ⭐", Modifier.weight(1f), color = Gold, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, fontSize = 12.sp)
                }
            }
            items(SeasonPass.tiers, key = { it.level }) { tier ->
                val reached = level >= tier.level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(if (reached) palette.accent else palette.muted.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center,
                    ) { Text("${tier.level}", color = if (reached) Color.White else palette.muted, fontWeight = FontWeight.ExtraBold) }
                    Spacer(Modifier.width(10.dp))
                    RewardCell(
                        tier.free, look, Modifier.weight(1f),
                        claimed = tier.level in st.claimedFree, claimable = SeasonPass.canClaim(progress, tier.level, false),
                        locked = false, onClaim = { onClaim(tier.level, false) },
                    )
                    Spacer(Modifier.width(8.dp))
                    RewardCell(
                        tier.premium, look, Modifier.weight(1f),
                        claimed = tier.level in st.claimedPremium, claimable = SeasonPass.canClaim(progress, tier.level, true),
                        locked = !st.premium, onClaim = { onClaim(tier.level, true) },
                    )
                }
            }
            if (BuildConfig.TEST_TOOLS) item {
                GameCard(Modifier.fillMaxWidth(), color = lerp(palette.card, Color(0xFF7E57C2), 0.12f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🧪 Modo teste", style = MaterialTheme.typography.titleMedium, color = palette.text)
                        Text("Só existe na versão de testes.", color = palette.muted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onTestXp, shape = RoundedCornerShape(12.dp)) { Text("+1.000 XP") }
                            OutlinedButton(onClick = onTestMinds, shape = RoundedCornerShape(12.dp)) { Text("+500 🧠") }
                            OutlinedButton(onClick = onTestReset, shape = RoundedCornerShape(12.dp)) { Text("Reiniciar") }
                        }
                    }
                }
            }
            if (progress.seasonHistory.isNotEmpty()) item {
                GameCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Temporadas anteriores", style = MaterialTheme.typography.titleMedium, color = palette.text)
                        progress.seasonHistory.reversed().forEach {
                            Text("T${it.number} · ${it.name} — nível ${it.finalLevel}${if (it.premium) " ⭐" else ""}", color = palette.muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardCell(
    reward: Reward, look: Map<Slot, CosmeticItem>, modifier: Modifier,
    claimed: Boolean, claimable: Boolean, locked: Boolean, onClaim: () -> Unit,
) {
    val palette = LocalPalette.current
    val item = (reward as? Reward.Item)?.let { CosmeticCatalog.byId(it.itemId) }
    val border = when {
        claimable -> Gold
        item != null -> item.rarity.color()
        else -> palette.muted.copy(alpha = 0.3f)
    }
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.card)
            .border(if (claimable) 3.dp else 1.5.dp, border, RoundedCornerShape(16.dp))
            .clickable(enabled = claimable, onClick = onClaim)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.height(78.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (reward) {
                is Reward.Item -> if (item != null) Box(Modifier.width(62.dp)) { ItemThumb(item, look) }
                is Reward.Coins -> Text("🪙", fontSize = 36.sp)
                is Reward.Minds -> Text("🧠", fontSize = 36.sp)
            }
            if (locked) Text("🔒", fontSize = 22.sp, modifier = Modifier.align(Alignment.TopEnd))
            if (claimed) Text("✅", fontSize = 20.sp, modifier = Modifier.align(Alignment.TopEnd))
        }
        Text(
            when (reward) {
                is Reward.Coins -> "${reward.amount} moedas"
                is Reward.Minds -> "${reward.amount} Minds"
                is Reward.Item -> item?.name ?: ""
            },
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.text, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 14.sp,
        )
        if (item != null) RarityLabel(item.rarity)
        if (claimable) Text("RESGATAR", color = GoldText, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

private val GoldText = Color(0xFFB98600)

// ======================= Loja de Minds (003a-US16) =======================

@Composable
fun ShopScreen(progress: PlayerProgress, onBack: () -> Unit, onBuy: (String) -> Unit) {
    val palette = LocalPalette.current
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Loja de Minds", onBack) { Pill("🧠 ${progress.minds}") }
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text(
                    "🧪 Compras de teste — nada é cobrado. A cobrança real (Google Play) entra quando o servidor estiver pronto.",
                    color = Color(0xFF6A1B9A), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF3E5F5)).padding(12.dp),
                )
            }
            item {
                Text(
                    "Minds 🧠 compram o Passe Premium (${SeasonPass.PRICE_MINDS} 🧠). Quem termina a trilha premium ganha Minds suficientes para o próximo passe. " +
                        "Minds nunca compram XP nem níveis.",
                    color = palette.muted, fontSize = 13.sp,
                )
            }
            items(Shop.products) { prod ->
                val owned = prod.lifetime && progress.lifetimePremium
                GameCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (prod.lifetime) "♾️" else "🧠", fontSize = 34.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(prod.title, style = MaterialTheme.typography.titleMedium, color = palette.text)
                            Text(prod.description, color = palette.muted, fontSize = 13.sp)
                        }
                        if (owned) Text("Ativo ✓", color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold)
                        else Button(onClick = { onBuy(prod.id) }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = palette.accent)) {
                            Text(prod.priceLabel, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

