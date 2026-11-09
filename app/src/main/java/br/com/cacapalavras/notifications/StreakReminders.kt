package br.com.cacapalavras.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.cacapalavras.R
import br.com.cacapalavras.data.ProgressRepository
import br.com.cacapalavras.domain.DailyStreak
import br.com.cacapalavras.domain.PlayerProgress
import br.com.cacapalavras.ui.MainActivity
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Lembretes da sequência diária (004-US21).
 * Cada lembrete é um trabalho único do WorkManager que, ao disparar, decide se notifica
 * (só se o jogador ainda não jogou hoje) e se reprograma para o dia seguinte. Sobrevive a reinícios.
 */
object StreakReminders {
    const val CHANNEL_ID = "streak"
    private const val WORK_DAILY = "streak_reminder_daily"
    private const val WORK_LAST = "streak_reminder_last_chance"
    const val LAST_CHANCE_HOUR = 22
    private const val KEY_KIND = "kind"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Sequência diária", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Lembretes para não perder sua sequência 🔥"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun hasPermission(context: Context): Boolean =
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** (Re)programa os lembretes conforme as preferências. Chamado ao abrir o app e ao mudar a configuração. */
    fun schedule(context: Context, p: PlayerProgress) {
        val wm = WorkManager.getInstance(context)
        if (!p.reminderEnabled) {
            wm.cancelUniqueWork(WORK_DAILY); wm.cancelUniqueWork(WORK_LAST); return
        }
        enqueue(context, DailyStreak.ReminderKind.DAILY, LocalTime.of(p.reminderHour, p.reminderMinute))
        if (p.lastChanceEnabled) enqueue(context, DailyStreak.ReminderKind.LAST_CHANCE, LocalTime.of(LAST_CHANCE_HOUR, 0))
        else wm.cancelUniqueWork(WORK_LAST)
    }

    private fun enqueue(context: Context, kind: DailyStreak.ReminderKind, at: LocalTime, policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(at)
        if (!next.isAfter(now.plusMinutes(1))) next = next.plusDays(1)
        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(Duration.between(now, next))
            .setInputData(workDataOf(KEY_KIND to kind.name))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            if (kind == DailyStreak.ReminderKind.DAILY) WORK_DAILY else WORK_LAST, policy, req,
        )
    }

    /** Ferramenta de teste: mostra a notificação agora, com o texto real (ou um texto de exemplo se já jogou hoje). */
    fun showTestNow(context: Context, p: PlayerProgress) {
        val today = LocalDate.now()
        val text = DailyStreak.reminderText(p, today, DailyStreak.ReminderKind.DAILY)
            ?: DailyStreak.reminderText(p, today.plusDays(1), DailyStreak.ReminderKind.DAILY)
            ?: ("🔥 Teste de lembrete" to "Assim você será avisado para não perder a sequência.")
        notify(context, text.first, text.second, 99)
    }

    fun notify(context: Context, title: String, body: String, id: Int) {
        if (!hasPermission(context)) return
        createChannel(context)
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_flame)
            .setColor(0xFFFF6D00.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, n)
        } catch (_: SecurityException) {
            // Permissão revogada entre a checagem e o envio: ignora.
        }
    }

    /** Usado pelo próprio worker: encadeia o próximo disparo sem cancelar o trabalho em execução. */
    internal fun rescheduleSelf(context: Context, p: PlayerProgress, kind: DailyStreak.ReminderKind) {
        if (!p.reminderEnabled) return
        if (kind == DailyStreak.ReminderKind.LAST_CHANCE && !p.lastChanceEnabled) return
        val at = if (kind == DailyStreak.ReminderKind.DAILY) LocalTime.of(p.reminderHour, p.reminderMinute) else LocalTime.of(LAST_CHANCE_HOUR, 0)
        enqueue(context, kind, at, ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    internal fun kindOf(name: String?) = runCatching { DailyStreak.ReminderKind.valueOf(name ?: "") }.getOrDefault(DailyStreak.ReminderKind.DAILY)
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val kind = StreakReminders.kindOf(inputData.getString("kind"))
        val p = ProgressRepository(ctx).progress.first()
        if (p.reminderEnabled) {
            DailyStreak.reminderText(p, LocalDate.now(), kind)?.let { (title, body) ->
                StreakReminders.notify(ctx, title, body, if (kind == DailyStreak.ReminderKind.DAILY) 1 else 2)
            }
        }
        StreakReminders.rescheduleSelf(ctx, p, kind) // próximo dia
        return Result.success()
    }
}
