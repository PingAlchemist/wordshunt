# Tarefas — 001-caca-palavras

Legenda: `[P]` = pode rodar em paralelo · `[x]` = concluída · referência à história entre colchetes.

## Fase 1 — Setup
- [x] T001 Toolchain portátil: JDK 17, Android SDK 35, Gradle 8.11.1 em `C:\Users\diego.aragao\AndroidDev`
- [x] T002 Projeto Gradle (settings, build raiz, `app/build.gradle.kts`, wrapper, `gradle.properties`)
- [x] T003 [P] Manifesto, tema XML, ícone adaptativo vetorial

## Fase 2 — Fundação (domínio, bloqueia todas as histórias)
- [x] T004 [P] `Models.kt` (Cell, Direction, PlacedWord, Puzzle, LevelConfig, GameMode)
- [x] T005 [P] `TextNormalizer.kt` + `TextNormalizerTest`
- [x] T006 [P] `WordBank.kt` (≥ 18 temas × ≥ 20 palavras) + `WordBankTest` (FR-008)
- [x] T007 `PuzzleGenerator.kt` + `PuzzleGeneratorTest` (FR-001, FR-002, NFR-001)
- [x] T008 [P] `Selection.kt` + `SelectionTest` (FR-004)

## Fase 3 — US1 Jogar um nível 🎯
- [x] T009 `GameEngine.kt` (trySelection, combo, conclusão) + `GameEngineTest` (FR-005)
- [x] T010 `WordGrid` Canvas com gesto de arrasto e dois toques
- [x] T011 `GameScreen` (topo, grade, lista de palavras, "+XP" flutuante, combo)
- [x] T012 `GameController` com relógio de jogo (pausa ao sair do app)

## Fase 4 — US2 XP e níveis 🎯
- [x] T013 `Progression.kt` (curva, títulos, estrelas, breakdown, applyResult) + `ProgressionTest`
- [x] T014 `PlayerProgress` (domínio) + `ProgressRepository` (DataStore) (FR-006)
- [x] T015 `ResultScreen` com XP animado e overlay de Level Up com confete
- [x] T016 `HomeScreen` com cartão do jogador (nível, título, barra de XP, moedas)

## Fase 5 — US3 Jornada
- [x] T017 `LevelDesigner.journey(n)` + `LevelDesignerTest`
- [x] T018 Estrelas por nível, rejogar níveis, seletor de níveis na Home

## Fase 6 — US4/US7 Moedas, dicas e Palavra Dourada
- [x] T019 Dicas Letra/Palavra no `GameEngine` + botões com custo
- [x] T020 Palavra dourada (render dourado + XP ×2)

## Fase 7 — US5 Diário · US6 Contra o Tempo
- [x] T021 `LevelDesigner.daily(data)` + sequência em `applyResult`
- [x] T022 Contra o Tempo: contagem regressiva, +5 s/palavra, novo tabuleiro +15 s, recorde

## Fase 8 — US8/US9/US10 Conquistas, temas, perfil
- [x] T023 `Achievements.kt` + `AchievementsTest` + tela de conquistas + aviso de desbloqueio
- [x] T024 Paletas desbloqueáveis + `ThemesScreen`
- [x] T025 `ProfileScreen` com estatísticas e opção de vibração

## Fase 9 — Polimento e verificação
- [x] T026 `gradlew testDebugUnitTest` verde (47 testes)
- [x] T029 Teste manual no emulador (Pixel 6, API 34): Home, nível 1 completo, combo, Level Up, conquistas, Diário
- [x] T027 `gradlew assembleDebug` gera APK
- [x] T028 README com como compilar, instalar e estrutura SDD
