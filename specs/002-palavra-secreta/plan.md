# Plano — 002-palavra-secreta

## Checagem da Constituição
- II Domínio puro: a regra da dica fica em `GameEngine` e é testada na JVM. ✓
- III Determinismo: a escolha da palavra secreta usa o mesmo `Random(seed)`. ✓
- V Progressão justa: dica custa moedas ganhas jogando, mesmo custo da antiga "Letra" (25). ✓

## Mudanças
| Arquivo | Mudança |
|---|---|
| `domain/Models.kt` | `PlacedWord.golden` → `secret`; `LevelConfig.withGoldenWord` → `withSecretWord` |
| `domain/PuzzleGenerator.kt` | marca 1 secreta; ordena `secret` por último |
| `domain/LevelDesigner.kt` | `withSecretWord = true` em todos os modos |
| `domain/GameEngine.kt` | remove `hintLetter`/`hintWord`/`hintedCells`; adiciona `hintSecretInitial()` e `secretInitial` |
| `domain/Progression.kt` | `HINT_COST = 25`; remove custos antigos |
| `domain/PlayerProgress.kt`, `Achievements.kt` | `goldenFound` → `secretFound`; conquista "Detetive" |
| `ui/GameController.kt` | `useHint()` substitui as duas dicas |
| `ui/screens/GameScreen.kt` | um botão Dica; chip da secreta com tracinhos/inicial |
| `ui/components/WordGrid.kt` | remove o destaque pulsante de dica no quadro |
| `specs/001-*/contracts/progressao-xp.md` | §1 "dourada" → "secreta"; §10 Detetive |

## Compatibilidade de dados
`goldenFound` salvo na v1 é ignorado (`ignoreUnknownKeys`); `secretFound` começa em 0. Aceitável: app ainda não publicado.
