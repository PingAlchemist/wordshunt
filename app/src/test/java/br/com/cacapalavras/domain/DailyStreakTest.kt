package br.com.cacapalavras.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyStreakTest {
    private val d0 = LocalDate.of(2026, 11, 8)
    private fun play(p: PlayerProgress, day: LocalDate) = DailyStreak.register(p, day)

    @Test fun primeiroDiaEDiasSeguidos() {
        var p = PlayerProgress()
        val (p1, u1) = play(p, d0)
        assertTrue(u1.newDay); assertEquals(1, u1.current)
        assertEquals(130, p1.coins) // 100 inicial + 30 do dia 1
        p = play(p1, d0.plusDays(1)).first
        assertEquals(2, p.streak.current)
        assertEquals(2, p.streak.best)
    }

    @Test fun mesmoDiaNaoConta() {
        val p = play(PlayerProgress(), d0).first
        val (p2, u) = play(p, d0)
        assertFalse(u.newDay)
        assertEquals(p, p2)
    }

    @Test fun relogioVoltandoNaoMuda() {
        val p = play(PlayerProgress(), d0).first
        val (p2, u) = play(p, d0.minusDays(3))
        assertFalse(u.newDay)
        assertEquals(p, p2)
    }

    @Test fun ciclo7DiasComBauEProtetor() {
        var p = PlayerProgress()
        var last: StreakUpdate? = null
        for (i in 0 until 7) play(p, d0.plusDays(i.toLong())).let { p = it.first; last = it.second }
        assertEquals(7, p.streak.current)
        assertEquals(listOf(Reward.Coins(200), Reward.Minds(25)), last!!.rewards)
        assertTrue(last!!.shieldEarned)
        assertEquals(1, p.streak.shields)
        assertEquals(10 + 25, p.minds) // dia 4 + baú
    }

    @Test fun protetorCobreDiaPerdido() {
        val start = PlayerProgress(streak = StreakState(current = 9, best = 9, lastActiveEpochDay = d0.toEpochDay(), shields = 1))
        val (p, u) = play(start, d0.plusDays(2)) // perdeu 1 dia
        assertEquals(10, p.streak.current)
        assertEquals(1, u.shieldsUsed)
        assertEquals(0, p.streak.shields)
    }

    @Test fun semProtetorCaiPelaMetadeNaoZera() {
        val start = PlayerProgress(streak = StreakState(current = 20, best = 20, lastActiveEpochDay = d0.toEpochDay(), shields = 1))
        val (p, u) = play(start, d0.plusDays(4)) // perdeu 3 dias, só 1 protetor
        assertEquals(11, p.streak.current) // 20/2 + 1
        assertEquals(20, u.halvedFrom)
        assertEquals(1, p.streak.shields) // protetor não é desperdiçado
        assertEquals(20, p.streak.best)
    }

    @Test fun marcosDaoItensUmaVez() {
        var p = PlayerProgress()
        for (i in 0 until 3) p = play(p, d0.plusDays(i.toLong())).first
        assertTrue("glasses_streak" in p.inventory)
        assertTrue(3 in p.streak.milestones)
        assertEquals(6, DailyStreak.milestoneDays.size)
        DailyStreak.milestoneDays.forEach { assertNotNull("marco $it", DailyStreak.milestoneItem(it)) }
    }

    @Test fun projecaoExplicaOQueAconteceHoje() {
        val s = StreakState(current = 10, lastActiveEpochDay = d0.toEpochDay(), shields = 0)
        assertEquals(11, DailyStreak.projectedIfPlayToday(s, d0.plusDays(1)))
        assertEquals(6, DailyStreak.projectedIfPlayToday(s, d0.plusDays(3)))
        assertEquals(StreakStatus.AT_RISK, DailyStreak.status(s, d0.plusDays(1)))
        assertEquals(StreakStatus.MISSED_DAYS, DailyStreak.status(s, d0.plusDays(3)))
    }

    @Test fun migraSequenciaAntigaDoDiario() {
        val old = PlayerProgress(dailyStreak = 4, bestDailyStreak = 6, lastDailyEpochDay = 100)
        val p = DailyStreak.migrateLegacy(old)
        assertEquals(4, p.streak.current); assertEquals(6, p.streak.best); assertEquals(100L, p.streak.lastActiveEpochDay)
        assertEquals(p, DailyStreak.migrateLegacy(p)) // idempotente
    }

    @Test fun lembreteSoQuandoNaoJogouHoje() {
        val played = play(PlayerProgress(), d0).first
        assertNull(DailyStreak.reminderText(played, d0, DailyStreak.ReminderKind.DAILY))
        val tomorrow = DailyStreak.reminderText(played, d0.plusDays(1), DailyStreak.ReminderKind.DAILY)!!
        assertTrue(tomorrow.first, tomorrow.first.contains("1 dias") || tomorrow.first.contains("risco"))
        assertNull(DailyStreak.reminderText(PlayerProgress(), d0, DailyStreak.ReminderKind.LAST_CHANCE)) // nada em risco
        assertNotNull(DailyStreak.reminderText(PlayerProgress(), d0, DailyStreak.ReminderKind.DAILY)) // convite
    }

    @Test fun partidaRegistraSequenciaNoPipeline() {
        val r = LevelResult(GameMode.JOURNEY, 1, 4, 0, 0, 50, 110, 0, 80, 1)
        val (p, rewards) = RewardPipeline.applyGame(PlayerProgress(), r, d0)
        assertEquals(1, p.streak.current)
        assertTrue(rewards.streak.newDay)
    }
}
