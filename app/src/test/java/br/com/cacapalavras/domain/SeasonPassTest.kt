package br.com.cacapalavras.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonPassTest {
    private val day = LocalDate.of(2026, 11, 8)
    private fun fresh() = RewardPipeline.onAppStart(PlayerProgress(), day)

    @Test fun temporadasDe35Dias() {
        val s1 = SeasonPass.current(day)
        assertEquals(1, s1.number)
        assertEquals("Exploradores das Letras", s1.name)
        assertEquals(28, s1.daysLeft(day.toEpochDay()))
        assertEquals(2, SeasonPass.current(LocalDate.of(2026, 12, 6)).number)
        assertEquals(1, SeasonPass.current(LocalDate.of(2026, 1, 1)).number) // antes do início
    }

    @Test fun recompensaEmTodosOsNiveisDasDuasTrilhas() {
        assertEquals(30, SeasonPass.tiers.size)
        val passItems = CosmeticCatalog.items.filter { it.source is ItemSource.Pass }
        val rewarded = SeasonPass.tiers.flatMap { listOf(it.free, it.premium) }.filterIsInstance<Reward.Item>().map { it.itemId }
        assertEquals(passItems.map { it.id }.toSet(), rewarded.toSet())
    }

    // US17-6: quem termina o premium consegue recomprar o próximo
    @Test fun premiumDevolveMindsParaRecomprar() {
        assertTrue(SeasonPass.totalMinds(premium = true) >= SeasonPass.PRICE_MINDS)
        assertEquals(1_000, SeasonPass.totalMinds(premium = true))
        assertEquals(300, SeasonPass.totalMinds(premium = false))
    }

    @Test fun xpDoJogoSobeOPasse() {
        var p = fresh()
        val r = LevelResult(GameMode.JOURNEY, 1, 4, 0, 0, 50, 110, 0, 80, 1)
        val (np, rewards) = RewardPipeline.applyGame(p, r, day)
        assertEquals(rewards.summary.totalXp.toLong(), np.season.xp)
        assertEquals(rewards.summary.totalXp, rewards.passXp)
        p = RewardPipeline.grantXp(np, 3_000, day)
        assertEquals(3, SeasonPass.levelFor(p.season.xp))
    }

    @Test fun gratisResgataSemPremiumPremiumNao() {
        val p = RewardPipeline.grantXp(fresh(), 2_000, day)
        assertTrue(SeasonPass.canClaim(p, 2, premium = false))
        assertFalse(SeasonPass.canClaim(p, 3, premium = false)) // não alcançado
        assertFalse(SeasonPass.canClaim(p, 1, premium = true)) // sem premium
        val (np, _) = SeasonPass.claim(p, 2, premium = false)!!
        assertTrue("hair_short_ginger" in np.inventory)
        assertNull(SeasonPass.claim(np, 2, premium = false)) // não resgata duas vezes
    }

    @Test fun compraComMindsERetroativo() {
        var p = RewardPipeline.grantXp(fresh(), 5_000, day)
        assertNull(SeasonPass.buyPremium(p)) // sem Minds
        p = Shop.testPurchase(p, "minds_1000")!!
        p = SeasonPass.buyPremium(p)!!
        assertEquals(50, p.minds)
        assertTrue(p.season.premium)
        (1..5).forEach { assertTrue(SeasonPass.canClaim(p, it, premium = true)) }
        p = SeasonPass.claim(p, 1, premium = true)!!.first
        assertTrue("outfit_space" in p.inventory)
    }

    @Test fun vitalicioDaDescontoNaoDaPasse() {
        var p = Shop.testPurchase(fresh(), "premium_lifetime")!!
        assertFalse(p.season.premium)
        assertEquals(665, SeasonPass.price(p))
        p = SeasonPass.buyPremium(p.copy(minds = 700))!!
        assertEquals(35, p.minds)
        assertNull(Shop.testPurchase(p, "premium_lifetime")) // não compra duas vezes
    }

    @Test fun terminarPremiumPagaOProximoPasse() {
        var p = fresh().copy(minds = SeasonPass.PRICE_MINDS)
        p = SeasonPass.buyPremium(p)!!
        p = RewardPipeline.grantXp(p, 30_000, day)
        p = SeasonPass.claimAll(p)
        assertTrue(p.minds >= SeasonPass.PRICE_MINDS)
        assertEquals(0, SeasonPass.claimableCount(p))
    }

    @Test fun fimDaTemporadaResgataAutomaticamenteEGuardaHistorico() {
        var p = RewardPipeline.grantXp(fresh(), 10_000, day) // 10 níveis, nada resgatado
        val coinsBefore = p.coins
        p = SeasonPass.sync(p, LocalDate.of(2026, 12, 10)) // temporada 2
        assertEquals(2, p.season.number)
        assertEquals(0L, p.season.xp)
        assertEquals(listOf(SeasonRecord(1, "Exploradores das Letras", 10, false)), p.seasonHistory)
        assertTrue("outfit_stripes" in p.inventory) // grátis nível 10 auto-resgatado
        assertTrue(p.coins > coinsBefore)
    }
}
