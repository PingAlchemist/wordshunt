# Plano de Implementação — 001-caca-palavras

**Entrada**: `spec.md`, `research.md`, `contracts/progressao-xp.md`

## Contexto técnico
| Item | Escolha |
|---|---|
| Linguagem | Kotlin 2.1.0 (JVM 17) |
| UI | Jetpack Compose (BOM 2024.12.01) + Material 3 |
| Build | Gradle 8.11.1 + Android Gradle Plugin 8.7.3 |
| SDK | compileSdk/targetSdk 35, minSdk 26 |
| Persistência | DataStore Preferences + kotlinx.serialization (JSON) |
| Testes | JUnit 4 (domínio puro, roda na JVM sem emulador) |
| Navegação | Pilha própria em `AppViewModel` (sem dependência extra) |

## Checagem da Constituição
| Princípio | Como o plano atende |
|---|---|
| I Spec antes de código | Esta pasta `specs/001-*` precede o código |
| II Domínio puro | `domain/` não importa `android.*`; testes JVM |
| III Determinismo | `PuzzleGenerator(seed)` usa `kotlin.random.Random(seed)` |
| IV Offline | Sem permissão de internet no manifesto |
| V Progressão justa | Sem loja; moedas só por jogo |
| VI Acessível | Arrasto + dois toques; alvos ≥ 40dp; háptico opcional |
| VII PT-BR | Strings e palavras em PT-BR; normalização no domínio |
| VIII Original | Nomes, cores e ícone próprios |

Nenhuma exceção.

## Arquitetura
```
app/src/main/java/br/com/cacapalavras/
├── domain/                  ← Kotlin puro (testado)
│   ├── Models.kt            Cell, Direction, PlacedWord, Puzzle, LevelConfig, GameMode
│   ├── TextNormalizer.kt    remove acentos, maiúsculas
│   ├── PuzzleGenerator.kt   gera grade determinística
│   ├── LevelDesigner.kt     config da Jornada (nível N), Diário (data) e Contra o Tempo
│   ├── Selection.kt         snap em 8 direções
│   ├── GameEngine.kt        sessão: encontrar palavras, combo, dicas, resultado
│   ├── Progression.kt       curva de XP, títulos, desbloqueios, estrelas, recompensas
│   ├── Achievements.kt      definição e avaliação de conquistas
│   └── PlayerProgress.kt    estado persistido (@Serializable, Kotlin puro)
├── data/
│   ├── WordBank.kt          temas e palavras PT-BR
│   └── ProgressRepository.kt DataStore
└── ui/
    ├── MainActivity.kt
    ├── AppViewModel.kt      navegação (pilha) + progresso + aplicação de recompensas
    ├── GameController.kt    estado da partida, relógio, gesto, dicas (vive no AppViewModel)
    ├── theme/               paletas desbloqueáveis + tema M3
    ├── components/          WordGrid (Canvas), Common (barra de XP, selo de nível), Confetti
    └── screens/             Home, Game, Result, MetaScreens (Níveis, Conquistas, Temas, Perfil)
```

### Fluxo de uma partida
```
Home ─▶ AppViewModel.startJourney()
        └─ LevelDesigner.journey(n) ─▶ PuzzleGenerator.generate() ─▶ GameController(GameEngine)
WordGrid (gesto) ─▶ Selection.line() ─▶ GameEngine.trySelection() ─▶ Found(+XP, combo) ─▶ UI
GameEngine.result() ─▶ LevelResult ─▶ Progression.applyResult(progress) ─▶ RewardSummary
        └─ ProgressRepository.update() ─▶ ResultScreen (XP animado, Level Up, conquistas)
```

### Decisões
- **Tabuleiro em um único Canvas** com `TextMeasurer`: garante 60 fps (NFR-002) e permite desenhar
  destaques como linhas com ponta arredondada.
- **Gesto próprio com `awaitEachGesture`**: `detectDragGestures` só dispara após o *touch slop*, o que
  perderia a célula inicial.
- **Aplicação de recompensas pura** (`Progression.applyResult`): recebe progresso + resultado e devolve
  novo progresso + resumo; fácil de testar e sem efeitos colaterais.
- **Seeds**: Jornada `seed = 7919 × nível`; Diário `seed = yyyyMMdd`; Contra o Tempo `seed = tempo atual`.

## Estrutura de testes
`app/src/test/java/br/com/cacapalavras/` — `TextNormalizerTest`, `SelectionTest`, `PuzzleGeneratorTest`,
`LevelDesignerTest`, `GameEngineTest`, `ProgressionTest`, `AchievementsTest`, `WordBankTest` (47 testes).

## Como compilar
Ver `README.md` (toolchain portátil em `C:\Users\diego.aragao\AndroidDev`).
