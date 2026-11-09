# Especificação — 003a Personagem, Inventário, Minds e Passe de Temporada

**Feature**: 003a · **Parte de**: `specs/003-gamificacao/plan.md` (Fases 1 e 3, antecipadas a pedido) · **Status**: Implementada para teste via APK

## Decisões do produto (registradas em 08/11/2026)
| # | Decisão |
|---|---|
| D1 | Emenda à Constituição aprovada: monetização só cosmética/conveniência. O app **será online** no futuro (servidor como fonte da verdade); por ora roda local para testes via APK. |
| D2 | Personagem desenhado em código, em camadas trocáveis. |
| D3 | **Vitalício NÃO inclui passes.** Dá no máximo um **desconto permanente** no passe de todas as temporadas (30%). |
| D4 | Compras **simuladas** ("Compra de teste — nada é cobrado") até existir servidor e Play Console. |
| D6 | Nova moeda premium **Minds** 🧠. Compra o Passe Premium. Ganha-se com dinheiro (pacotes) e nas recompensas do passe. A trilha premium devolve Minds suficientes para **recomprar o passe da próxima temporada**. |

## Histórias de usuário

### US13 — Personagem (P1)
1. O jogador tem um personagem desenhado em camadas: cenário, mochila, pernas/calçado, roupa, braços, cabeça/pele, expressão, cabelo, óculos, chapéu e animação.
2. O personagem aparece na Home (cartão do jogador), no Perfil (destaque, com nome editável) e no Inventário.
3. Começa com um visual inicial e itens básicos (5 tons de pele, 2 cabelos, 2 roupas, 1 calçado, 1 expressão, 1 cenário, 1 animação).

### US14 — Inventário (P1)
1. Lista todos os itens por categoria, com filtro por raridade.
2. Mostra itens possuídos e bloqueados; o bloqueado diz como se obtém ("Nível 10", "Conquista Purista", "Passe — nível 15 premium").
3. Tocar num item possuído equipa e o personagem atualiza **na hora**. Chapéu, óculos e mochila podem ser removidos; os outros slots sempre têm um item.
4. Raridades: Comum, Incomum, Raro, Épico, Lendário. Só mudam a moldura e a cor, nunca o jogo.

### US15 — Desbloqueio por progresso (P1)
1. Subir de nível e ganhar conquistas libera itens (tabela no `plan.md` §4.3; as 12 conquistas dão 1 item cada).
2. Jogadores que já tinham progresso recebem retroativamente os itens dos níveis e conquistas que já possuem.
3. Itens novos aparecem na tela de resultado e na celebração de Level Up.

### US16 — Minds (P1)
1. O saldo de Minds 🧠 aparece na Home ao lado das moedas 🪙.
2. Loja de Minds com pacotes (500 / 1.000 / 2.800) e Premium Vitalício. Todos com o selo "Compra de teste".
3. Minds compram **somente** o Passe Premium (por ora). Nunca XP ou níveis do passe.

### US17 — Passe de Temporada (P1)
1. Temporadas de 35 dias contínuas (a 1ª começa em 01/11/2026), com nome, contagem regressiva, 30 níveis e 1.000 XP por nível.
2. O XP do passe é **todo XP ganho no jogo** durante a temporada: grátis e premium sobem na mesma velocidade.
3. Duas trilhas, com recompensa em **todos** os 30 níveis de cada uma: moedas, Minds e itens.
4. Resgate manual por recompensa ou "Resgatar tudo". As recompensas grátis são resgatáveis sem premium.
5. Premium custa **950 Minds** (665 com o Vitalício). Comprar no meio da temporada libera retroativamente os prêmios premium já alcançados.
6. A trilha premium dá **1.000 Minds** no total: quem termina consegue comprar o próximo passe. A trilha grátis dá 300 Minds.
7. Fim da temporada: prêmios alcançados e não resgatados são resgatados automaticamente, e a temporada vai para o histórico.
8. Item repetido (já possuído) vira moedas: Comum 50 · Incomum 100 · Raro 200 · Épico 400 · Lendário 800.

### US18 — Ferramentas de teste (temporário)
O card "🧪 Modo teste" na tela do Passe tem os botões +1.000 XP, +500 Minds e "Reiniciar passe". Ele existe só enquanto `TEST_TOOLS = true` no build.
