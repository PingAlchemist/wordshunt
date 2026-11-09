package br.com.cacapalavras.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CosmeticsTest {
    @Test fun idsUnicosEPadroesExistem() {
        val ids = CosmeticCatalog.items.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        CosmeticCatalog.defaults.forEach { (slot, id) ->
            val item = CosmeticCatalog.byId(id)
            assertNotNull(id, item)
            assertEquals(slot, item!!.slot)
            assertEquals(ItemSource.Starter, item.source)
        }
    }

    @Test fun todaConquistaDaUmItem() {
        Achievements.all.forEach { a ->
            assertEquals(a.id, 1, CosmeticCatalog.items.count { it.source == ItemSource.FromAchievement(a.id) })
        }
    }

    @Test fun jogadorNovoRecebeIniciaisEVisualPadrao() {
        val (p, newItems) = Inventory.grantProgressItems(PlayerProgress())
        assertTrue(newItems.isEmpty()) // iniciais não contam como "novos"
        assertTrue(CosmeticCatalog.items.filter { it.source == ItemSource.Starter }.all { it.id in p.inventory })
        val look = Inventory.appearanceOf(p)
        Slot.entries.filter { it.required }.forEach { assertTrue(it.name, it in look) }
        assertFalse(Slot.HAT in look)
    }

    @Test fun retroativoPorNivelEConquista() {
        val xpLevel10 = (1..9).sumOf { Progression.xpToNext(it) }
        val old = PlayerProgress(totalXp = xpLevel10, achievements = setOf("first_word"))
        val (p, newItems) = Inventory.grantProgressItems(old)
        assertTrue("outfit_hunter" in p.inventory) // nível 10
        assertTrue("hat_explorer" in p.inventory) // nível 5
        assertTrue("hair_spiky_blond" in p.inventory) // conquista
        assertFalse("bg_night" in p.inventory) // nível 12
        assertTrue(newItems.any { it.id == "outfit_hunter" })
    }

    @Test fun equiparSoItemPossuidoERemoverSoOpcional() {
        val p = Inventory.grantProgressItems(PlayerProgress()).first
        assertEquals(p, Inventory.equip(p, "hat_crown_letters")) // não possui
        val withCap = Inventory.equip(p.copy(inventory = p.inventory + "hat_cap_red"), "hat_cap_red")
        assertEquals("hat_cap_red", Inventory.appearanceOf(withCap)[Slot.HAT]?.id)
        assertFalse(Slot.HAT in Inventory.appearanceOf(Inventory.unequip(withCap, Slot.HAT)))
        assertEquals(withCap, Inventory.unequip(withCap, Slot.HAIR)) // obrigatório
    }

    @Test fun itemRepetidoViraMoedas() {
        val p = PlayerProgress(inventory = setOf("glasses_star"), coins = 0)
        val (np, dup) = Inventory.grantItem(p, CosmeticCatalog.byId("glasses_star")!!)
        assertEquals(Rarity.RARE.duplicateCoins, dup)
        assertEquals(dup, np.coins)
    }
}
