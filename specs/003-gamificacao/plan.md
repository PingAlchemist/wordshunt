# Plano — 003 Gamificação, Personagem e Monetização Ética

**Status**: PROPOSTA, aguardando aprovação e as decisões da §10 · **Entrada**: pedido do produto + `research.md`

---

## 1. Emenda à Constituição (proposta, precisa de aprovação)

| Princípio | Hoje | Proposta |
|---|---|---|
| IV Offline-first e privacidade | "Sem anúncios, sem rastreadores" | Mantém. **Jogar continua 100% offline.** Só a compra (Fase 4) usa internet. Nenhum SDK de analytics/ads. |
| V Progressão justa | "Nada exige pagamento" / "sem loja" | **V-a** Todo XP e progresso vêm de jogar. **V-b** Pago = só cosmético, conveniência ou conteúdo extra, nunca XP, nível, estrelas ou velocidade. **V-c** Proibido: caixas aleatórias pagas, anúncios, contagem regressiva de oferta, recompensa grátis retirada. **V-d** O passe gratuito é completável sem pagar (verificado por teste). |
| novo IX Uma conta, duas experiências | — | Clássica e Completa compartilham o mesmo estado. A Completa é opcional. |
| novo X Premium derivado | — | Direito premium é sempre recalculado a partir das compras verificadas, nunca de um booleano salvo. |

## 2. Princípio de arquitetura: um único pipeline de recompensas

Hoje, `Progression.applyResult` concentra XP e moedas. Ele é estendido, sem duplicar nada:

```
LevelResult (partida)
   └─▶ Progression.applyResult ──▶ [eventos]  XpGranted · CoinsGranted · LevelReached · StreakUpdated
            ├─▶ MissionEngine.onEvents     → missões avançam/concluem → mais eventos (XP, moedas, item)
            ├─▶ SeasonPass.onEvents        → seasonXp += XpGranted (mesma fonte!) → níveis do passe
            ├─▶ Achievements.evaluate      → conquistas (já existe)
            └─▶ Unlocks.evaluate           → itens por nível/conquista/missão → ItemUnlocked
   ══▶ RewardSummary (tela de resultado) + uma única transação Room
```

- **XP do passe = XP ganho na temporada.** Grátis e Premium avançam na mesma velocidade, por construção.
- XP de missão é `XpGranted` como qualquer outro: conta para nível e passe, sem sistema paralelo.
- Tudo em `domain/`, Kotlin puro, testado na JVM (Constituição II).

## 3. Modelo de dados (Room v1 + migração)

| Tabela | Campos principais | Origem |
|---|---|---|
| `player` | id, name, totalXp, uiMode (CLASSIC/FULL), haptics, createdAt | `PlayerProgress` |
| `wallet` | coins, coinsEarnedTotal, coinsSpentTotal | `PlayerProgress.coins` |
| `journey_level` | level, bestStars, completedAt | `starsByLevel` |
| `stats` | wordsFound, secretFound, levelsCompleted, noHintLevels, maxCombo, fastest, bestTA | contadores atuais |
| `achievement` | id, unlockedAt | `achievements` |
| `inventory_item` | itemId, source, acquiredAt | novo (temas atuais migram para cá) |
| `appearance` | slot, itemId | novo |
| `daily_log` | epochDay, completed, seconds, hints, stars, weekend | novo (calendário e histórico) |
| `streak` | current, best, shields, lastEpochDay, milestonesClaimed | `dailyStreak`… |
| `mission_progress` | missionId, periodKey (dia/semana), progress, goal, claimed | novo |
| `season_progress` | seasonId, seasonXp, claimedFree[], claimedPremium[] | novo |
| `season_history` | seasonId, finalLevel, premium, endedAt | novo |
| `purchase_record` | orderId, productId, purchaseToken, state, verifiedAt | novo (Fase 4) |

**Migração:** na primeira abertura da nova versão, lê o JSON do DataStore, grava tudo numa transação e marca `migrated=true`. O JSON fica como backup por uma versão. Há teste instrumentado com JSON real da v1.

**Nuvem (futuro):** toda tabela tem `updatedAt`, e os ids são estáveis (strings). Isso basta para uma sincronização futura, sem implementá-la agora.

## 4. Personagem e cosméticos

### 4.1 Personagem
- Mascote próprio, desenhado em **camadas vetoriais em Compose**: corpo base → calçado → roupa → mochila → cabeça/expressão → cabelo → óculos → chapéu, sobre um cenário.
- Animações leves (respirar, acenar, comemorar, pular) feitas com transformações e sem assets pesados.
- A API `AvatarLayer(slot, draw)` permite trocar o desenho por arte profissional depois (risco R1).

### 4.2 Raridade (só visual)
Comum · Incomum · Raro · Épico · Lendário. Muda a moldura, o brilho e a animação de desbloqueio. **Nunca muda o jogo.**

### 4.3 Desbloqueios por nível do jogador (pela curva real `100·n^1,4`: nível 10 ≈ 9.200 XP ≈ 35–40 fases; nível 20 ≈ 52.000 XP ≈ 150–200 fases)
| Nível | Recompensa | Raridade |
|---|---|---|
| 1 | Personagem inicial + 2 cabelos + 2 roupas + cenário Clássico | Comum |
| 2 | Expressão "Feliz" | Comum |
| 3 | Cenário Oceano (tema já existente) + boné | Comum |
| 4 | Óculos redondos | Incomum |
| 5 | **Primeiro acessório**: chapéu explorador (+ Contra o Tempo, que já existe) | Incomum |
| 7 | Mochila de aventureiro | Incomum |
| 8 | Cenário Floresta (já existe) | Incomum |
| 10 | **Primeira roupa especial**: traje Caçador de Palavras | Raro |
| 12 | Cenário Noite (já existe) | Raro |
| 15 | **Animação exclusiva**: "Lupa giratória" + Cenário Pôr do Sol (já existe) | Épico |
| 20 | **Conjunto especial**: Mestre das Letras + Cenário Dourado (já existe) | Épico |
| 25 | Expressão + calçado épicos | Épico |
| 30 | **Evolução**: aura de letras flutuantes | Lendário |

Os 6 temas atuais continuam liberados nos **mesmos níveis** (nenhuma funcionalidade removida).

### 4.4 Origem dos itens (catálogo inicial ~60 itens)
| Origem | Quantidade | Exemplo |
|---|---|---|
| Nível do jogador | 18 | tabela acima |
| Conquistas | 12 | uma peça por conquista |
| Missões e eventos | 8 | chapéu "Semana Perfeita" |
| Passe gratuito (temporada) | 10 | — |
| Passe premium (temporada) | 10 | conjunto lendário da temporada |
| Loja por moedas | ~15 (rotação semanal de 4) | — |
| Loja premium (dinheiro) | pacotes cosméticos | — |

## 5. Missões

| Tipo | Quantidade | Exemplos (sorteio determinístico por data) | Recompensa |
|---|---|---|---|
| Diária | 3 | completar 3 fases · encontrar 30 palavras · fazer o desafio diário · 1 fase com 3★ | 60–100 XP + 20–40 🪙 |
| Semanal | 3 | 20 fases · 15 estrelas · jogar em 5 dias · 5 fases sem dica | 300–500 XP + 100–150 🪙 |
| Especial | evento/temporada | "Semana dos Animais": 40 palavras do tema · coleção completa | item cosmético |

Regras: no máximo ~20 min/dia para completar as diárias. Nenhuma missão exige gastar moedas ou dinheiro: "use uma dica" fica de fora, porque obriga a gastar. Missões não concluídas expiram sem penalidade. Há uma troca grátis de missão por dia.

## 6. Passe de temporada

| Regra | Valor proposto |
|---|---|
| Duração | configurável (padrão 35 dias), definida em `assets/seasons/*.json` |
| Níveis | 30 |
| XP por nível do passe | 1.000 XP de temporada (fixo) |
| Fonte do XP | todo `XpGranted` da temporada (partidas, missões, conquistas, diário) |
| Trilha grátis | recompensa em **todo nível**: moedas, cosméticos, 1 cenário e 1 item épico no nível 30 |
| Trilha premium | recompensa extra em todo nível: conjunto completo, animações, cenário premium, lendário no 30, moedas extras |
| Compra no meio da temporada | libera **retroativamente** todos os prêmios premium já alcançados |
| Fim da temporada | prêmios alcançados e não resgatados (grátis e premium, se tiver) são **resgatados automaticamente** e avisados na próxima abertura. Itens nunca expiram. |
| Histórico | nível final, se tinha premium, itens obtidos |

**Critério de aceite numérico (teste do simulador de economia):**
- Jogador **regular** (3 fases + diário + missões diárias, 5 dias/semana) completa os 30 níveis em ≤ 35 dias.
- Jogador **leve** (1 fase + diário por dia) chega a ≥ nível 15, com ≥ 8 cosméticos grátis.

## 7. Economia

| Fonte de 🪙 (grátis) | Valor |
|---|---|
| Fase concluída (atual) | 10 + 5×estrelas |
| Desafio diário (atual) | +30 |
| Subir de nível (atual) | 20 + 5×nível |
| Conquista (atual) | 25 |
| Missões | 20–40 / diária · 100–150 / semanal |
| Passe grátis | ~1.500 🪙 por temporada |

| Destino de 🪙 | Preço |
|---|---|
| Dica (atual) | 25 |
| Cosmético Comum / Incomum / Raro / Épico | 200 / 450 / 900 / 1.800 |
| Lendário | **não vendido por moedas nem por dinheiro avulso**: só passe, eventos e conquistas |
| Troca extra de missão | grátis 1×/dia (não monetizada) |

Meta: o jogador regular compra **um item raro por semana** só jogando.

**Pacotes de moedas (opcionais):** pequeno 1.000 · médio 2.500 · grande 6.500 🪙. Os preços em R$ são definidos no Play Console. Sem "bônus por tempo limitado". Moedas compradas compram **apenas** cosméticos e dicas, nunca XP ou níveis do passe.

## 8. Monetização e direitos (entitlements)

| Produto (Play) | Tipo | Preço de referência | Concede |
|---|---|---|---|
| `pass_s{N}` | compra única por temporada | R$ 14,90 | Trilha premium da temporada N |
| `premium_monthly` | assinatura | R$ 9,90/mês | Trilha premium enquanto ativa + coleções exclusivas + temas premium + estatísticas avançadas + 2 slots extras de visual salvo |
| `premium_annual` | assinatura | R$ 59,90/ano | igual à mensal |
| `premium_lifetime` | compra única | R$ 79,90–99,90 | benefícios da assinatura para sempre (**decisão D3**) |
| `coins_s/m/l` | consumível | configurável | moedas |
| `bundle_*` | compra única | configurável | pacote cosmético fixo e visível (sem sorteio) |

**Não cobrar duas vezes:**
- Assinante ativo ou vitalício não vê a oferta do passe; o card mostra "Incluso na sua assinatura".
- Quem comprou o passe e depois assina: a tela de assinatura avisa que o passe atual já está pago e destaca os outros benefícios.
- Estatísticas avançadas para assinantes são **extras**. Tudo o que o Perfil mostra hoje continua grátis.

**Segurança (pedido §12):** o Billing é consultado a cada abertura; o backend verifica o token, faz o *acknowledge* e recebe RTDN (cancelamento/reembolso). Moedas compradas só são creditadas após verificação. Um reembolso remove o direito premium futuro, mas não remove itens já resgatados.

## 9. Desafio diário, retenção e as duas experiências

**Diário (expansão):**
- Calendário do mês (✓ jogado, ★ estrelas, 🌟 fim de semana).
- Desafio especial de sábado e domingo: grade 12×12, todas as direções, recompensa maior.
- Histórico de desempenho.
- **Sequência não punitiva:** marcos de 3, 7, 14 e 30 dias dão recompensas que **ficam**. Ganha-se 1 escudo a cada 7 dias (máx. 2), e o escudo cobre um dia perdido. Sem escudo, a sequência cai pela metade em vez de zerar.

**Experiência Clássica:** a Home atual (Jornada, Diário, Contra o Tempo, Conquistas, Perfil simples). Nenhuma menção a loja, passe ou personagem.

**Experiência Completa:** Home com personagem em destaque, cards de Passe, Missões, Loja, Inventário, Coleções e Eventos.

**Uma conta só:** é uma preferência de navegação (`player.uiMode`). As recompensas continuam sendo ganhas nas duas experiências, e a Clássica mostra um aviso discreto de "3 itens novos no inventário" só se o jogador quiser ver.

## 10. Decisões necessárias antes de começar

| # | Decisão | Recomendação |
|---|---|---|
| D1 | Aprovar a emenda à Constituição (§1) | Aprovar |
| D2 | Arte do personagem: vetorial em código agora, ou esperar arte profissional? | Vetorial em código, com camadas trocáveis |
| D3 | A compra vitalícia inclui os passes premium das temporadas futuras? | Sim (é o que "compra definitiva" sugere) |
| D4 | Backend de verificação de compras (Fase 4): Firebase ou outro? Quem cria a conta do Play Console? | Firebase Functions; decidir só antes da Fase 4 |
| D5 | Nome e personalidade do mascote | Proposta na Fase 1 |

## 11. Ordem de desenvolvimento

| Fase | Entrega jogável | Principais testes |
|---|---|---|
| **0 — Fundação** (nova, pré-requisito) | Room + migração do DataStore; pipeline de eventos; Navigation Compose; preferência Clássica/Completa (só a chave, Completa ainda vazia) | migração com JSON real; todos os 48 testes atuais continuam verdes |
| **1 — Personagem, inventário, cosméticos** | Avatar em camadas no Perfil; Inventário com filtros e preview ao vivo; catálogo; desbloqueio por nível e conquista; temas atuais migrados para o inventário | regras de equipar; desbloqueio por nível; migração dos temas |
| **2 — Missões e progressão integrada** | Missões diárias e semanais; calendário diário; sequência com escudos e marcos; desafio de fim de semana | determinismo do sorteio; missões não duplicam XP; sequência não punitiva |
| **3 — Passe (grátis + premium "travado")** | Temporada 1 configurável; trilhas; resgate; fim de temporada; histórico. Premium aparece, mas a compra só liga na Fase 4 | **simulador de economia** (critérios da §6); resgate retroativo |
| **4 — Loja e Google Play Billing** | Loja de cosméticos por moedas; produtos pagos; assinatura; restauração; backend de verificação | `FakeBilling` em testes; teste interno na Play Store com contas de teste |
| **5 — Experiência Completa e polimento** | Home Completa; animações de desbloqueio, missão e passe; acessibilidade; desempenho em aparelho intermediário | teste de UI das duas experiências; perfil de desempenho (60 fps no arrasto) |

Cada fase vira uma pasta `specs/003x-*` com spec, plano e tarefas próprios antes do código.
