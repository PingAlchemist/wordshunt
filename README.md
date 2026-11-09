# Caça-Palavras 🔎

Jogo de caça-palavras para Android, em português, offline e sem anúncios, com **sistema de progressão de XP**:
cada palavra dá experiência, combos multiplicam, o jogador sobe de nível, ganha títulos, moedas,
conquistas e desbloqueia modos e temas visuais.

Desenvolvido com **SDD (Spec-Driven Development)**: a especificação vem antes do código.

## Funcionalidades
| | |
|---|---|
| 🧭 **Jornada** | Níveis infinitos e determinísticos; grade de 6×6 a 12×12, 4 a 12 palavras, direções liberadas aos poucos (→↓, depois diagonais, depois invertidas) |
| ✨ **XP e níveis** | XP por palavra (tamanho × combo × dourada), bônus de conclusão, sem dicas e velocidade; curva `100·n^1,4`; 6 títulos (Aprendiz → Lenda) |
| 🔥 **Combo** | Palavras com até 6 s de intervalo: ×1,2 → ×1,5 → ×2,0 |
| 🔒 **Palavra Secreta** | A última palavra da lista fica escondida (só tracinhos) e vale XP em dobro |
| ★ **Estrelas** | 1–3 por nível (dica e tempo-alvo); rejogue para melhorar |
| 💡 **Dica** | 25 🪙, uma por tabuleiro: mostra só a inicial da Palavra Secreta, sem marcar nada no quadro |
| 📅 **Desafio Diário** | Mesmo tabuleiro para todos no dia, XP ×1,5 + bônus de sequência |
| ⏱️ **Contra o Tempo** | Desbloqueia no nível 5: 90 s, +5 s por palavra, +15 s por tabuleiro limpo |
| 🏆 **Conquistas** | 12 conquistas com recompensa de XP e moedas |
| 🎨 **Temas** | 6 paletas desbloqueadas por nível (Clássico, Oceano, Floresta, Noite, Pôr do Sol, Dourado) |
| 👆 **Controles** | Arrasto com *snap* em 8 direções ou dois toques (início → fim); vibração configurável |

20 temas de palavras (Animais, Frutas, Países, Comidas Brasileiras, Cidades do Brasil, Fundo do Mar…), mais de 500 palavras.

## Estrutura SDD
```
.specify/memory/constitution.md         princípios do projeto
specs/001-caca-palavras/
├── research.md                         benchmark dos melhores jogos do gênero → decisões
├── spec.md                             histórias de usuário, critérios de aceite, requisitos
├── plan.md                             arquitetura e decisões técnicas
├── data-model.md                       entidades e estado persistido
├── contracts/progressao-xp.md          fórmulas de XP/nível/estrelas/moedas (fonte da verdade)
└── ...
specs/002-palavra-secreta/              palavra escondida + dica de inicial (altera US4/US7)
specs/003-gamificacao/                  análise e plano de gamificação/monetização (5 fases)
specs/003a-personagem-passe/            personagem, inventário, Minds e passe de temporada
specs/004-arte-sequencia-notificacoes/  arte comic, sequência diária e lembretes
└── tasks.md                            tarefas por fase/história
app/                                    código (Kotlin + Jetpack Compose)
```
Fluxo para novas features: copie `specs/001-*` como modelo em `specs/002-nome/`, escreva spec → plan → tasks e só então implemente.

## Compilar e instalar
O toolchain foi instalado de forma portátil (sem admin) em `C:\Users\diego.aragao\AndroidDev`
(JDK 17, Android SDK 35, Gradle 8.11.1, emulador).

```powershell
cd "C:\Users\diego.aragao\Documents\CACA PALAVRAS"
$env:JAVA_HOME = "C:\Users\diego.aragao\AndroidDev\jdk17"

.\gradlew.bat testDebugUnitTest      # 73 testes do domínio
.\gradlew.bat assembleDebug          # APK: app\build\outputs\apk\debug\app-debug.apk
.\gradlew.bat assembleRelease        # APK otimizado (R8), assinado com a chave de debug
```

**No celular**: copie o `app-debug.apk` para o aparelho e abra (permita "instalar apps desconhecidos"), ou com depuração USB:
```powershell
C:\Users\diego.aragao\AndroidDev\sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

**No emulador**:
```powershell
C:\Users\diego.aragao\AndroidDev\sdk\emulator\emulator.exe -avd caca
```

Também abre normalmente no Android Studio (File → Open → esta pasta).

## Requisitos
Android 8.0 (API 26) ou superior. Única permissão: vibração.
