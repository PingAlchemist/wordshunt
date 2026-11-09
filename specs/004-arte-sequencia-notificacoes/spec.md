# Especificação — 004 Arte estilo comic, Sequência Diária e Lembretes

**Feature**: 004 · **Altera**: 003a (Avatar), 001 (sequência do Desafio Diário, contrato §3, conquista `streak_7`) · **Status**: Implementada

## Pedido
> "Redesenhe as artes: cartoon, mas mais detalhista — 3D stylized cartoon, comic style, highly expressive face,
> vibrant colors, smooth lighting, stylized proportions." (referência: heróis cartoon com contorno de quadrinho)
> "Coloque uma sequência diária para a pessoa se animar a jogar todos os dias, com notificações para não perder a sequência."

## US19 — Personagem estilo comic (P1)
1. Proporções de herói cartoon (~4,5 cabeças), não mais chibi.
2. Contorno de quadrinho (tinta escura) em todas as peças; sombreado em dois tons (cel-shading) e luz suave vinda do alto à esquerda.
3. Rosto expressivo: olhos com íris, pupila e dois brilhos; sobrancelhas que mudam por expressão; nariz; boca com dentes/língua quando aberta; **piscar** periódico.
4. Cabelos em mechas com gradiente de luz; roupas com dobras, costuras, golas, punhos, cintos e fivelas.
5. Os 68 itens existentes mantêm id, nome e raridade: só o desenho muda (sem perda de inventário).
6. Limite assumido: é vetor desenhado em código. O renderizador continua isolado por slot para receber arte em camadas (PNG) no futuro.

## US20 — Sequência diária 🔥 (P1)
1. **Dia ativo** = concluir pelo menos 1 fase em qualquer modo (Jornada, Diário ou Contra o Tempo).
2. A sequência sobe 1 por dia ativo seguido. Ela aparece em destaque na Home e numa tela própria, com a chama 🔥, o número de dias, o calendário da semana e a próxima recompensa.
3. **Recompensa a cada dia**, em ciclo de 7 dias: 30 🪙 · 50 🪙 · 75 🪙 · 10 🧠 · 100 🪙 · 150 🪙 · 🎁 baú (200 🪙 + 25 🧠). O valor de cada dia aparece antes.
4. **Marcos** com itens exclusivos: 3, 7, 14, 30, 60 e 100 dias.
5. **Protetor 🛡️**: ganha 1 a cada 7 dias de sequência (máximo 2). Cada dia perdido consome 1 protetor e a sequência continua.
6. **Não punitivo**: sem protetor suficiente, a sequência **cai pela metade** em vez de zerar. O recorde nunca se perde.
7. O bônus de XP do Desafio Diário (contrato §3) passa a usar esta sequência. A conquista "Constância" passa a ser "Mantenha 7 dias de sequência".
8. O jogador que já tinha sequência no Desafio Diário a mantém (migração).

## US21 — Lembretes (P1)
1. Notificação local diária no horário escolhido (padrão 19:00), **só se o jogador ainda não jogou hoje**.
2. Aviso de **última chance** às 22:00 (opcional, padrão ligado), só se houver sequência ≥ 1 em risco.
3. O texto é motivador e usa a sequência e a próxima recompensa ("🔥 Sua sequência de 6 dias está em risco! Amanhã tem 🎁 baú").
4. Tocar na notificação abre o jogo.
5. No Android 13+, a permissão é pedida num momento com contexto: card na Home e interruptor na tela da Sequência. Nunca na primeira abertura.
6. Configuração na tela da Sequência: ligar/desligar, horário (08:00, 12:00, 19:00, 21:00) e "última chance".
7. Os lembretes sobrevivem a reinício do aparelho (WorkManager).
8. Modo teste: botão "Testar notificação agora".

## Requisitos
- **FR-020** Regras da sequência em `domain/DailyStreak.kt`, Kotlin puro e testado (dia ativo, salto de dias, protetores, metade, relógio voltando).
- **FR-021** Agendamento em `notifications/`, com WorkManager e trabalho único reprogramado a cada disparo.
- **NFR-020** O personagem desenha a 60 fps na Home; nas miniaturas não há animação.
