# Modelo de Dados — 001-caca-palavras

## Domínio (em memória)

| Entidade | Campos | Regras |
|---|---|---|
| `Cell` | row, col | 0 ≤ row, col < tamanho |
| `Direction` | dRow, dCol | 8 valores: E, SE, S, SW, W, NW, N, NE |
| `PlacedWord` | display (com acento), text (normalizado), cells, golden | `cells.size == text.length` |
| `Puzzle` | size, grid (List<String>), words, themeName, seed | grid[r][c] ∈ A–Z |
| `LevelConfig` | mode, levelNumber, size, wordCount, directions, themeId, seed, parSeconds, tier, goldenWord | ver `LevelDesigner` |
| `GameMode` | JOURNEY, DAILY, TIME_ATTACK | — |
| `FoundWord` | word, colorIndex, xp, byHint | — |
| `LevelResult` | mode, levelNumber, wordsFound, goldenFound, hintsUsed, elapsedSeconds, parSeconds, tier, wordXp, maxCombo, boardsCleared, isReplay | entrada de `Progression.applyResult` |
| `RewardSummary` | xpBreakdown (lista rótulo→XP), totalXp, coins, stars, levelBefore, levelAfter, unlocks, achievements, newRecord | saída para a tela de resultado |

## Persistido (`PlayerProgress`, JSON no DataStore, chave `progress_json`)

| Campo | Tipo | Padrão | Observação |
|---|---|---|---|
| totalXp | Long | 0 | nível e barra são derivados |
| coins | Int | 100 | |
| journeyUnlocked | Int | 1 | próximo nível jogável da Jornada |
| starsByLevel | Map<Int, Int> | {} | melhor estrela por nível |
| wordsFound | Int | 0 | |
| goldenFound | Int | 0 | |
| levelsCompleted | Int | 0 | soma de todos os modos |
| noHintLevels | Int | 0 | |
| threeStarLevels | Int | 0 | níveis distintos com 3★ (recalculado) |
| dailyStreak | Int | 0 | |
| bestDailyStreak | Int | 0 | |
| lastDailyEpochDay | Long | -1 | dia (epoch) da última conclusão diária |
| bestTimeAttackWords | Int | 0 | recorde |
| maxComboReached | Int | 0 | |
| fastestLevelSeconds | Int | 0 | só níveis com faixa ≥ 2; 0 = nunca |
| achievements | Set<String> | {} | ids desbloqueados |
| selectedPalette | String | "classic" | |
| hapticsEnabled | Boolean | true | |

### Transições
- `applyResult` → incrementa contadores, soma XP/moedas, atualiza estrelas, avança `journeyUnlocked`
  (apenas se `levelNumber == journeyUnlocked`), atualiza sequência diária, avalia conquistas em laço.
- `spendCoins(n)` → só se `coins ≥ n`.
