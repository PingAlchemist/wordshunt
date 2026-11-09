# Pesquisa — O que os melhores caça-palavras fazem bem

Fase 0 do SDD. Levantamento de mecânicas recorrentes nos caça-palavras mais bem avaliados da
Play Store (ex.: *Word Search Explorer*, *Wordscapes Search*, *Words of Wonders: Search*,
*Word Search Quest*, *Word Search Pro*). Baseado em conhecimento do gênero, não em uma auditoria nova da loja.
Objetivo: escolher o que entra no nosso jogo e **por quê**, sem copiar identidade de ninguém (Constituição VIII).

## Mecânicas observadas

| # | Mecânica | Onde aparece | Por que funciona | Decisão |
|---|----------|--------------|------------------|---------|
| 1 | Arrastar o dedo em 8 direções com "snap" na diagonal | Todos | Seleção imprecisa no celular frustra; o snap perdoa | **Adotar** (P1) |
| 2 | Cada palavra encontrada ganha uma cor própria de destaque | Todos | Feedback visual claro, tabuleiro fica "bonito" ao terminar | **Adotar** (P1) |
| 3 | Lista de palavras por tema (Animais, Frutas…) | Todos | Contexto ajuda a buscar e ensina vocabulário | **Adotar** (P1) |
| 4 | Dificuldade progressiva: grade maior, mais palavras, direções invertidas | Explorer, Quest | Curva de aprendizado suave, sem paredes | **Adotar** (P1) |
| 5 | Jornada/mapa de níveis com estrelas | Wonders, Quest | Senso de avanço e rejogabilidade (buscar 3★) | **Adotar** (P1/P2) |
| 6 | Nível de jogador / XP / títulos | Quest, Explorer | Meta de longo prazo além do nível atual | **Adotar** — núcleo pedido (P1) |
| 7 | Moedas + dicas (revelar letra / palavra) | Todos | Evita travar e abandonar | **Adotar** (P2) |
| 8 | Desafio diário com sequência (streak) | Wordscapes Search, Pro | Principal motor de retorno diário | **Adotar** (P2) |
| 9 | Modo contra o tempo | Pro, Explorer | Variedade para jogadores experientes | **Adotar** desbloqueável (P2) |
| 10 | Conquistas | Quest, Pro | Metas secundárias e colecionáveis | **Adotar** (P3) |
| 11 | Temas visuais desbloqueáveis | Wonders, Explorer | Recompensa cosmética por progresso | **Adotar** como recompensa de nível (P3) |
| 12 | Combo por encontrar palavras em sequência rápida | Quest (bônus de velocidade) | Recompensa fluidez e cria picos de emoção | **Adotar** (P1, dentro do XP) |
| 13 | Palavra especial que vale mais | Wonders (palavras bônus) | Surpresa e "caça ao tesouro" | **Adotar** como "Palavra Dourada" (P2) |
| 14 | Anúncios recompensados / loja paga | Quase todos | Monetização; atrito para o jogador | **Rejeitar** (Constituição IV/V) |
| 15 | Login social / ranking online | Alguns | Exige backend e dados pessoais | **Adiar** (fora do escopo v1) |
| 16 | Seleção por dois toques (início e fim) | Alguns | Acessibilidade para quem tem dificuldade de arrastar | **Adotar** (P1) |

## Conclusões
1. O "loop" central é: **buscar → encontrar (cor + vibração + XP voando) → completar → recompensa**.
   Cada etapa precisa de feedback imediato.
2. Retenção vem de três camadas: nível atual (estrelas), meta diária (desafio + sequência) e meta longa
   (nível de jogador, títulos, temas, conquistas).
3. O XP deve recompensar habilidade (combo, velocidade, sem dicas) e constância (diário), nunca pagamento.

## Decisões técnicas pesquisadas
- **Kotlin + Jetpack Compose**: nativo, sem dependência pesada, Canvas próprio para o tabuleiro (desenho
  das cápsulas de destaque é trivial com `drawLine(cap = Round)`).
- **DataStore + kotlinx.serialization**: persistência local simples, assíncrona e tipada.
- **Gerador com `Random(seed)`**: determinístico, permite desafio diário idêntico para todos sem servidor.
