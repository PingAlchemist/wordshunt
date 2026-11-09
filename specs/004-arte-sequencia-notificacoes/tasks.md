# Tarefas — 004 Arte comic, Sequência Diária e Lembretes

- [x] T301 Spec 004 (US19 arte, US20 sequência, US21 lembretes)
- [x] T302 `Avatar.kt` reescrito em estilo comic: proporção de herói, contorno de tinta, cel-shading, olhos com íris/brilho, piscar, mechas de cabelo, dobras/golas/cintos/cadarços
- [x] T303 `domain/DailyStreak.kt`: dia ativo, ciclo de 7 recompensas, protetores, metade (não zera), marcos, migração do legado, textos dos lembretes + `DailyStreakTest` (11 testes)
- [x] T304 Contrato §3 e conquista `streak_7` passam a usar a nova sequência; `ProgressionTest` atualizado
- [x] T305 6 itens de marco (3, 7, 14, 30, 60, 100 dias) com `ItemSource.Streak`
- [x] T306 `notifications/StreakReminders.kt`: canal, WorkManager (diário + última chance 22h), reagendamento encadeado, permissão Android 13+
- [x] T307 UI: tela da Sequência, card na Home, convite de notificação com contexto, card na tela de resultado
- [x] T308 Verificado no emulador: arte nova, permissão, notificação real, toque abre o app, sequência acende após 1 fase
