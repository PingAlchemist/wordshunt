# Análise — Estado atual vs. Evolução de Gamificação (003)

Fase 0 do SDD para o pedido "Gamificação, Personagem e Monetização".
Base: código em `app/src/main/java/br/com/cacapalavras/` após as features 001 e 002.

## 1. O que existe hoje e como reaproveitar

| Sistema existente | Onde | Reaproveitamento |
|---|---|---|
| XP, nível do jogador, títulos | `domain/Progression.kt` (`levelInfo`, `title`, curva `100·n^1,4`) | **Fonte única de XP.** O "nível de personagem" pedido **é** o nível do jogador. Não haverá segundo XP. |
| Aplicação de recompensas | `Progression.applyResult` (`Progression.kt:141`) | Vira o **início do pipeline de recompensas**: resultado da partida → XP/moedas → missões → passe → conquistas → desbloqueios. |
| Moedas | `PlayerProgress.coins`; ganho em conclusão, nível, conquista, diário | Mantidas. Ganham novas fontes (missões, passe) e novos destinos (loja de cosméticos). |
| Dicas | `GameEngine.hintSecretInitial` (002) | Mantidas como estão (25 🪙). |
| Conquistas | `domain/Achievements.kt` (`all`, lambdas de progresso) | Mantidas. O mesmo padrão (meta + progresso derivado) serve de base para **missões**. |
| Desafio diário + sequência | `LevelDesigner.daily`, `applyResult` (`streak = when…`) | Expandido: calendário, marcos, fim de semana. A regra atual **zera a sequência** ao pular um dia → precisa mudar (pedido §10: não punitivo). |
| Temas visuais por nível | `Progression.palettes` + `ui/theme/Palettes` + `ThemesScreen` | Viram uma **categoria do catálogo de cosméticos** ("Cenários/Temas"). Os 6 temas atuais continuam grátis por nível. |
| Perfil e estatísticas | `ProfileScreen` | Recebe avatar, nome, coleções e histórico de temporadas. |
| Persistência | `data/ProgressRepository.kt` (DataStore + JSON de `PlayerProgress`) | Funciona, mas não escala para inventário, missões, temporadas e histórico (ver §3). |
| Navegação | Pilha própria em `AppViewModel` (`sealed interface Screen`) | Com 8+ telas novas, migrar para Navigation Compose. |
| Feedback visual | `Confetti`, overlay de Level Up, "+XP" flutuante | Reaproveitados para desbloqueio de item, missão concluída, nível do passe. |

## 2. O que precisa mudar

| Item | Mudança | Motivo |
|---|---|---|
| **Constituição IV e V** | Hoje: "sem anúncios, sem rastreadores" e "sem loja; moedas só por jogo". | O pedido introduz compras. Precisa de **emenda formal** (proposta no `plan.md`). "Sem anúncios" continua. |
| `PlayerProgress` (um JSON único) | Dividir em agregados: carteira, inventário, aparência, missões, temporada, preferências. | Inventário e histórico crescem sem limite. Migração precisa ser versionada. |
| Sequência diária | Deixa de voltar a 1 ao perder um dia. Entra **escudo de sequência** e marcos que nunca se perdem. | Pedido §10 (não punitivo). |
| `applyResult` | Retorna eventos (`XpGranted`, `CoinsGranted`, `ItemUnlocked`, …) em vez de só o resumo. | Missões e passe consomem os mesmos eventos: **não duplica XP**. |
| Home | Duas variantes: Clássica (atual) e Completa. | Pedido §8. |
| Conquistas | Algumas passam a dar cosméticos além de XP/moedas. | Colecionismo. |

## 3. Componentes novos

| Componente | Camada | Descrição |
|---|---|---|
| `CosmeticCatalog` | domain | Itens com id, categoria (cabelo, roupa, chapéu, óculos, mochila, calçado, expressão, animação, cenário), raridade e **origem** (nível, conquista, missão, passe grátis, passe premium, loja-moedas, loja-premium). |
| `Inventory` / `Appearance` | domain | Itens possuídos; um item equipado por slot; regras de equipar. |
| `AvatarRenderer` | ui | Personagem em camadas desenhado em Compose (vetores). Cada slot é uma camada trocável. |
| `MissionEngine` | domain | Missões diárias (3) e semanais (3), sorteadas de forma **determinística** pelo seed da data/semana. Progresso vem dos mesmos eventos de partida. |
| `SeasonPass` | domain | Temporada configurável (início, fim, 30 níveis, duas trilhas). XP do passe = XP ganho na temporada. Resgate, regra de fim de temporada, histórico. |
| `Economy` | domain | Tabela única de preços e fontes + **simulador** testado (jogador grátis "médio" em 35 dias). |
| `Entitlements` | domain | O que o jogador tem de premium (passe da temporada, assinatura, vitalícia), **derivado das compras** e nunca de um booleano salvo. |
| `BillingRepository` | data | Google Play Billing: consulta, compra, *acknowledge*, restauração, cancelamento. |
| Room (`AppDatabase`) | data | Tabelas: `wallet`, `inventory_item`, `appearance`, `mission_progress`, `season_progress`, `season_history`, `purchase_record`, `daily_log`. |
| Telas | ui | Inventário, Loja, Passe, Missões, Calendário diário, Configurações (modo Clássico/Completo, nome do jogador). |

## 4. Dependências externas

| Dependência | Para quê | Observação |
|---|---|---|
| `androidx.room` 2.6.x + KSP | Persistência relacional com migrações | KSP compatível com Kotlin 2.1.0 |
| `androidx.navigation:navigation-compose` 2.8.x | Navegação tipada | Substitui a pilha manual |
| `com.android.billingclient:billing-ktx` 7.x | Compras e assinaturas | Testar exige app no **Play Console** (no mínimo trilha de teste interno) |
| **Backend de verificação** (ex.: Firebase Functions + Google Play Developer API) | Validar *purchase tokens*, receber notificações de cancelamento/reembolso (RTDN) | Requisito do pedido §12 ("compras verificadas"). **Só a Fase 4 precisa dele.** |
| Conta Google Play Console + conta de comerciante | Produtos, preços em R$, assinaturas | Ação do dono do app |

## 5. Riscos técnicos

| # | Risco | Impacto | Mitigação |
|---|---|---|---|
| R1 | **Arte do personagem.** O sistema pede ~9 categorias × várias peças × 5 raridades. Não há artista nem assets. | Alto: personagem genérico ou feio derruba a proposta | Fase 1 com personagem **vetorial em camadas desenhado em código** (consistente, leve, sem assets) e API de camadas pronta para trocar por arte profissional (SVG/VectorDrawable) depois. |
| R2 | **Manipulação local.** App offline: em aparelho com root, dados locais (moedas) sempre podem ser editados. | Médio. Single-player sem ranking: só o próprio trapaceiro ganha. | Premium **derivado do Play Billing** a cada abertura (nunca de um flag local). Moedas compradas só são creditadas após verificação no backend. Verificação de integridade (HMAC com chave no Android Keystore) contra edição casual. |
| R3 | **Billing exige app publicado** (teste interno) e backend. | Bloqueia testes reais da Fase 4 | Fases 1–3 não dependem de dinheiro. Fase 4 começa com um `FakeBillingRepository` testável e só liga o real quando o Console estiver configurado. |
| R4 | **Migração DataStore → Room** com perda de progresso | Alto para quem já joga | Migração idempotente com teste instrumentado. O JSON antigo fica como backup por 1 versão. |
| R5 | **Escopo.** São 5 fases, cada uma do tamanho do jogo atual. | Prazo | Cada fase é entregável e jogável sozinha. Começar a Clássica/Completa já na Fase 1 evita retrabalho. |
| R6 | **Balanceamento** (passe completável sem pagar, loja sem pressão) | Médio | Simulador de economia em teste unitário com critérios de aceite numéricos. |
| R7 | **Regras legais e da Play Store** sobre assinaturas, reembolso e público menor de idade | Médio | Sem caixas aleatórias (já é regra do pedido). Preços e textos de assinatura claros. Revisar as políticas vigentes antes da Fase 4. |
